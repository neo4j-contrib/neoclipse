/*
 * Copyright (C) 2006 SQL Explorer Development Team
 * http://sourceforge.net/projects/eclipsesql
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sourceforge.sqlexplorer.sqlpanel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbproduct.DatabaseProduct;
import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;
import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.parsers.ParserException;
import net.sourceforge.sqlexplorer.parsers.Query;
import net.sourceforge.sqlexplorer.parsers.QueryParser;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.Message;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.util.TextUtil;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Shell;

/**
 * Base class for SQL Executions.
 * 
 * The AbstractSQLExecution now operates on a QueryTokenizer directly instead of being passed the
 * SQL to execute; this is so that individual queries from a single SQLEditor can run synchronously -
 * this is essential for DDL queries. 
 * 
 * This has been decoupled slightly from SQLEditor (which is now refactored to include result tabs)
 * such that an AbstractSQLExecution can simply be told to run by calling startExecute().  The
 * constructor is given the SQLEditor instance that fired it off - and when a results tab is
 * required, SQLEditor.createResultsTab() is called to get one.  The old _composite and _parentTab
 * have been replaced by the accessor methods getParentComposite() and getParentTab() respectively
 * which now allocate a tab JIT.  The purpose behind this change is that the execution can spark
 * off several tabs and add entries to the Messages tab.
 *
 * @modified John Spackman
 */
public abstract class AbstractSQLExecution extends Job {
	
	// Maximum size of the files used to log queries for debugging
	private static final long MAX_DEBUG_LOG_SIZE = 64 * 1024;
	
	// Maximum length of the caption for query results windows when the preference
	//	IConstants.USE_LONG_CAPTIONS_ON_RESULTS is true
	public static final int MAX_CAPTION_LENGTH = 25;

	private SQLEditor _editor;

	protected Session _session;
	
	protected SQLConnection _connection;
	
	protected boolean reRun = false; // flag indicating the execution is re run, re use old result tab

    // Query tokenizer to get SQL statements from
	private QueryParser queryParser;
	
	/**
	 * Constructor
	 * @param _editor The SQLEditor that triggered the execution
	 * @param statement the SQL to be executed
	 * @param _session the session
	 */
	public AbstractSQLExecution(SQLEditor editor, QueryParser queryParser) {
		super(Messages.getString("SQLExecution.Progress"));
		this._editor = editor;
		this._session = editor.getSession();
		this.queryParser = queryParser;
	}
	
	public IStatus run(IProgressMonitor monitor) {
		monitor.setTaskName(Messages.getString("SQLExecution.Progress"));
		
		try {
			// Wait until we can get a free connection from the queue
			_connection = _session.grabConnection();
			
			// Update status
			_editor.getEditorToolBar().refresh();

			// Make sure the user hasn't tried to terminate us and then run the SQL
			if (monitor.isCanceled())
			{
				return Status.CANCEL_STATUS;
			}
			if (_connection != null) {
				doExecution(monitor);
				if (monitor.isCanceled())
				{
					return Status.CANCEL_STATUS;
				}
				checkForMessages(null);
			}

		} catch (final RuntimeException e) {
			errorDialog(Messages.getString("SQLResultsView.Error.Title"), e.getClass().getName() + ":" + e.getMessage());
			
		} catch (final Exception e) {
			// only log non-sql errors
			if (!(e instanceof java.sql.SQLException || e instanceof InterruptedException))
				SQLExplorerPlugin.error("Error executing.", e);
			errorDialog(Messages.getString("SQLResultsView.Error.Title"), e.getMessage());

		} finally {
			if (_connection != null)
				_session.releaseConnection(_connection);
			_connection = null;
			_editor.getEditorToolBar().refresh();
		}
		this.reRun = true;
		return new Status(IStatus.OK, getClass().getName(), IStatus.OK, "OK", null);
	}
	
	/**
	 * Main execution method.  Note that this method is called from a background thread
	 * and therefore many SWT operations will need to be done via Display.[a]syncExec()
	 * @param monitor
	 * @throws Exception
	 */
	protected abstract void doExecution(IProgressMonitor monitor) throws Exception;

	/**
	 * This method will be called from the UI thread when execution is cancelled
	 * and the tab will be disposed. Do any cleanups required in here.  Note that this 
	 * method is called from a background thread and therefore many SWT operations will
	 * need to be done via Display.[a]syncExec()
	 * @throws Exception
	 */
	protected abstract void doStop() throws Exception;
	
	/**
	 * Creates a new tab for the results in SQLEditor
	 * @return
	 */
	protected CTabItem allocateResultsTab(Query query) {
		CTabItem resultsTab = _editor.createResultsTab(this, query, this.reRun);
		if (resultsTab == null)
			return null;
		this.reRun = false;
		boolean longCaptions = SQLExplorerPlugin.getDefault().getPreferenceStore().getBoolean(IConstants.USE_LONG_CAPTIONS_ON_RESULTS);
		String caption = resultsTab.getText();
		if (longCaptions) {
			int pos = caption.indexOf(" [");
			if(pos > 0)
			{
				caption = caption.substring(0, pos);
			}
			resultsTab.setText(caption + " [" + TextUtil.compressWhitespace(query.getQuerySql(), MAX_CAPTION_LENGTH) + "]");
		}
		resultsTab.setToolTipText(caption + " [" + query.getQuerySql() + "]");
		return resultsTab;
	}

	/**
	 * Checks the database server for messages
	 */
	protected boolean checkForMessages(Query query) throws SQLException {
    	LinkedList<Message> messages = new LinkedList<Message>();
        Collection<Message> messagesTmp;
        if (query != null) {
        	messagesTmp = _session.getDatabaseProduct().getErrorMessages(_connection, query);
	        if (messagesTmp != null)
	        	messages.addAll(messagesTmp);
        }
        messagesTmp = _session.getDatabaseProduct().getServerMessages(_connection);
        if (messagesTmp != null)
        	messages.addAll(messagesTmp);
        boolean hasMessages = false;
    	for (Message msg : messages) {
    		msg.setLineNo(getQueryParser().adjustLineNo(msg.getLineNo()));
    		if (msg.getStatus() != Message.Status.SUCCESS)
    			hasMessages = true;
    	}
        
        addMessages(messages);
        return hasMessages;
	}
	
	/**
	 * Handles a SQLException by parsing the message and populating the messages tab;
	 * where error messages from the server are numbered, they start relative to the
	 * line number of the query that was sent; lineNoOffset is added to each line
	 * number so that they relate to the line in SQLEditor
	 * @param e
	 */
	protected void logException(SQLException e, String sql) throws SQLException {
		Collection<Message> messages = _session.getDatabaseProduct().getErrorMessages(_connection, e, 0);
		if (messages == null)
			return;
		for (Message message : messages) {
			int lineNo = message.getLineNo();
			lineNo = queryParser.adjustLineNo(lineNo);
			message.setLineNo(lineNo);
			message.setSql(sql);
		}
		addMessages(messages);
	}
	
	/**
	 * Handles a SQLException by parsing the message and populating the messages tab;
	 * where error messages from the server are numbered, they start relative to the
	 * line number of the query that was sent; lineNoOffset is added to each line
	 * number so that they relate to the line in SQLEditor
	 * @param e
	 */
	protected void logException(ParserException e, Query query) throws SQLException {
		LinkedList<Message> messages = new LinkedList<Message>();
		messages.add(new Message(Message.Status.FAILURE, e.getLineNo(), e.getCharNo(), query.getQuerySql(), e.getMessage()));
		addMessages(messages);
	}
	
	/**
	 * Handles a SQLException by parsing the message and populating the messages tab;
	 * where error messages from the server are numbered, they start relative to the
	 * line number of the query that was sent; lineNoOffset is added to each line
	 * number so that they relate to the line in SQLEditor
	 * @param e The exception
	 * @param query The Query that triggered the exception
	 * @param positionEditor Whether to reposition the text caret of the editor to the first message
	 */
	protected void logException(SQLException e, Query query, boolean positionEditor) throws SQLException {
		DatabaseProduct product = _session.getDatabaseProduct();
		if (product == null)
			return;
		final Collection<Message> messages = product.getErrorMessages(_connection, e, query.getLineNo() - 1);
		if (messages == null)
			return;
		for (Message message : messages) {
			int lineNo = message.getLineNo();
			lineNo = queryParser.adjustLineNo(lineNo);
			message.setLineNo(lineNo);
			message.setSql(query.getQuerySql());
		}
		addMessages(messages);
		
		if (positionEditor) {
			final Shell shell = getEditor().getSite().getShell();
			shell.getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (messages.size() > 0) {
						Message msg = messages.iterator().next();
						getEditor().setCursorPosition(msg.getLineNo(), msg.getCharNo());
					}
				}
			});
		}		
	}
	
	/**
	 * Called to add messages to the message tab
	 * @param messages a collection of SQLEditor.Message objects
	 */
	protected void addMessages(final Collection<Message> messages) {
		_editor.getSite().getShell().getDisplay().asyncExec(new Runnable() {

            public void run() {
        		for(Message message : messages) {
        			_editor.addMessage(message);
        		}
            }
        });
	}
	
	/**
	 * Called to add messages to the message tab
	 * @param messages a collection of SQLEditor.Message objects
	 */
	protected void addMessage(final Message message) {
		_editor.getSite().getShell().getDisplay().asyncExec(new Runnable() {
            public void run() {
       			_editor.addMessage(message);
            }
        });
	}
	
	/**
	 * Helper method to set the progress message - switches to the UI thread
	 * @param progressMessage
	 */
	public final void setMessage(final String message) {
        getEditor().getSite().getShell().getDisplay().asyncExec(new Runnable() {
            public void run() {
                if (getEditor() != null)
                	getEditor().setMessage(message);
            }
        });
	}

	/**
	 * Helper method to set the progress message - switches to the UI thread
	 * @param progressMessage
	 */
	public final void setProgressMessage(final String message) {
	}

	/**
	 * Logs the query to the debug log file, but only if the preferences require
	 * it.  If the query failed, the exception should be included too. 
	 * @param query
	 * @param e
	 */
	protected void debugLogQuery(Query query, SQLException sqlException) {
		// Get the logging level
		String level = SQLExplorerPlugin.getDefault().getPreferenceStore().getString(IConstants.QUERY_DEBUG_LOG_LEVEL);
		if (level == null || level.equals(IConstants.QUERY_DEBUG_OFF))
			return;
		if (sqlException == null && level.equals(IConstants.QUERY_DEBUG_FAILED))
			return;
		
		// Get the log files; if the current log is too big, retire it
		File dir = SQLExplorerPlugin.getDefault().getStateLocation().toFile();
		File log = new File(dir.getAbsolutePath() + '/' + "query-debug.log");
		File oldLog = new File(dir.getAbsolutePath() + '/' + "query-debug.old.log");
		
		// Too big?  Then delete the old and archive the current  
		if (log.exists() && log.length() > MAX_DEBUG_LOG_SIZE) {
			oldLog.delete();
			log.renameTo(oldLog);
		}
		
		// Copy it to the output
		PrintWriter writer = null;
		try {
			FileWriter fw = new FileWriter(log, true);
			writer = new PrintWriter(fw);
			try {
				writer.write("==============================================\r\n");
				StringBuffer sb = new StringBuffer(query.toString());
				for (int i = 0; i < sb.length(); i++)
					if (sb.charAt(i) == '\n')
						sb.insert(i++, '\r');
				sb.append("\r\n");
				writer.write(sb.toString());
				if (sqlException != null)
					writer.write("FAILED: " + sqlException.getMessage() + "\r\n");
			} finally {
				writer.flush();
				writer.close();
			}
		} catch(IOException e) {
			SQLExplorerPlugin.error("Failed to log query", e);
		}
	}

	/**
	 * Helper method that switches to the UI thread and presents an Error dialog
	 * @param title
	 * @param message
	 */
	protected void errorDialog(final String title, final String message) {
		final Shell shell = getEditor().getSite().getShell();
		shell.getDisplay().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openError(shell, title, message);
			}
		});
	}

	/**
	 * @return the _editor
	 */
	public SQLEditor getEditor() {
		return _editor;
	}

	/**
	 * @return the queryParser
	 */
	public QueryParser getQueryParser() {
		return queryParser;
	}

}
