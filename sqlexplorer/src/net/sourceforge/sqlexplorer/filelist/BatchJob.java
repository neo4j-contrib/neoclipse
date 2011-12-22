package net.sourceforge.sqlexplorer.filelist;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbproduct.DatabaseProduct;
import net.sourceforge.sqlexplorer.dbproduct.Execution;
import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;
import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.dbproduct.User;
import net.sourceforge.sqlexplorer.parsers.ParserException;
import net.sourceforge.sqlexplorer.parsers.Query;
import net.sourceforge.sqlexplorer.parsers.QueryParser;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.Message;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Performs batched execution of a series of SQL scripts against a given alias/user
 * @author John Spackman
 *
 */
public class BatchJob extends Job {
	
    private FileListEditor editor;
	private User user;
	
	private Session session;
	
	private List<File> files;
	private Execution execution;

	public BatchJob(FileListEditor editor, User user, List<File> files) {
		this(editor, Messages.getString("BatchJob.Title"), user, files);
	}

	public BatchJob(FileListEditor editor, String name, User user, List<File> files) {
		super(name);
		this.editor = editor;
		this.user = user;
		this.files = files;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask(Messages.getString("BatchJob.ExecutingScripts"), files.size());
		DatabaseProduct product = user.getAlias().getDatabaseProduct();
		this.execution = new Execution(product);
		SQLConnection connection = null;
		try {
			if (session == null)
				session = user.createSession();
			connection = session.grabConnection();

			int index = 0;
			for (File file : files) {
				if (monitor.isCanceled())
				{
					return Status.CANCEL_STATUS;
				}
				monitor.worked(index++);
				monitor.subTask(file.getName());
	
				String sql = null;
				try {
					char[] buffer = new char[(int)file.length() + 10];
					FileReader reader = new FileReader(file);
					int length = reader.read(buffer);
					reader.close();
					if (length < 0 || length >= buffer.length) {
						editor.addMessage("Cannot read from file " + file.getAbsolutePath());
						continue;
					}
					// Normalise this to have standard \n in strings.  \r confuses Oracle and
					//	isn't normally needed internally anyway
			    	StringBuffer sb = new StringBuffer(new String(buffer, 0, length));
			    	buffer = null;
			    	for (int i = 0; i < sb.length(); i++) {
			    		if (sb.charAt(i) == '\r') {
			    			sb.deleteCharAt(i);
			    			i--;
			    		}
			    	}
			    	sql = sb.toString();
			    	sb = null;
				}catch(IOException e) {
					editor.addMessage("Cannot read from file " + file.getAbsolutePath() + ": " + e.getMessage());
					continue;
				}
				
				editor.addMessage("Loading file " + file.getAbsolutePath());
				QueryParser parser = product.getQueryParser(sql, 1);
				parser.parse();
				for (Query query : parser) {
					if (monitor.isCanceled())
					{
						return Status.CANCEL_STATUS;
					}
		            DatabaseProduct.ExecutionResults results = null;
	            	StringBuilder errors = new StringBuilder();
		            try {
		            	results = this.execution.executeQuery(connection, query, -1);
		            	while (results.nextDataSet() != null) {
		            		
	                    	LinkedList<Message> messages = new LinkedList<Message>();
		                    Collection<Message> messagesTmp = session.getDatabaseProduct().getErrorMessages(connection, query);
		                    if (messagesTmp != null)
		                    	messages.addAll(messagesTmp);
		                    messagesTmp = session.getDatabaseProduct().getServerMessages(connection);
		                    if (messagesTmp != null)
		                    	messages.addAll(messagesTmp);
	                    	for (Message msg : messages)
	                    		msg.setLineNo(parser.adjustLineNo(msg.getLineNo()));
		                    for (Message msg : messages) {
		                    	if (msg.getMessage() != null) {
			                    	StringBuilder sb = new StringBuilder(msg.getMessage());
			                    	for (int i = 0; i < sb.length(); i++) {
			                    		if ("\r\n".indexOf(sb.charAt(i)) > -1)
			                    			sb.setCharAt(i, ' ');
			                    	}
			                    	errors.append("line " + msg.getLineNo() + ": " + sb.toString());
		                    	}
		                    }
		            	}
		            }catch(SQLException e) {
		            	errors.append("Exception: " + e.getMessage());
		            } finally {
		            	try {
		            		if (results != null) {
		            			results.close();
		            			results = null;
		            		}
		            	}catch(SQLException e) {
		            		// Nothing
		            	}
		            }
		            if (errors.length() > 0) {
		            	errors.append("\n");
		            	editor.addMessage(errors.toString());
		            }
				}
			}
			monitor.done();
		}catch(SQLException e) {
			SQLExplorerPlugin.error(e);
		}catch(ParserException e) {
			SQLExplorerPlugin.error(e);
		} finally {
			if (connection != null)
				session.releaseConnection(connection);
		}
        return new Status(IStatus.OK, getClass().getName(), IStatus.OK, Messages.getString("BatchJob.Success"), null);
	}

	@Override
	protected void canceling() {
		super.canceling();
		this.execution.cancel();
	}


}
