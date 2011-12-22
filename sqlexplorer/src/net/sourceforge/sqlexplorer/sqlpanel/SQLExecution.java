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

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dataset.DataSet;
import net.sourceforge.sqlexplorer.dbproduct.DatabaseProduct;
import net.sourceforge.sqlexplorer.dbproduct.Execution;
import net.sourceforge.sqlexplorer.parsers.ExecutionContext;
import net.sourceforge.sqlexplorer.parsers.Query;
import net.sourceforge.sqlexplorer.parsers.QueryParser;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.Message;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.sqleditor.results.DataSetResultsTab;
import net.sourceforge.sqlexplorer.sqleditor.results.EditorResultsTab;
import net.sourceforge.sqlexplorer.sqleditor.results.GenericAction;
import net.sourceforge.sqlexplorer.sqleditor.results.GenericActionGroup;
import net.sourceforge.sqlexplorer.sqleditor.results.ResultsTableAction;
import net.sourceforge.sqlexplorer.sqleditor.results.actions.ReRunAction;
import net.sourceforge.sqlexplorer.sqleditor.results.export.ExportAction;
import net.sourceforge.sqlexplorer.sqleditor.results.export.ExporterCSV;
import net.sourceforge.sqlexplorer.sqleditor.results.export.ExporterHTML;
import net.sourceforge.sqlexplorer.sqleditor.results.export.ExporterXLS;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.custom.CTabItem;

/**
 * Executes one or more SQL statements in sequence, displaying the results in tabs
 * and/or in the Messages tab
 * @modified John Spackman
 *
 */
public class SQLExecution extends AbstractSQLExecution {
	
	// Whether the editor has any messages
	private boolean hasMessages;

	// Maximum number of rows to return
    protected int _maxRows;

    // The Statement being used to execute the current query
    protected Statement _stmt;

    private Execution execution;
    

    /**
     * Constructor
     * @param _editor
     * @param queryParser
     * @param maxRows
     * @param _session
     */
    public SQLExecution(SQLEditor _editor, QueryParser queryParser, int maxRows) {
		super(_editor, queryParser);
    	_maxRows = maxRows;
	}


	/**
     * Display SQL Results in result pane
     * @param sqlResult the results of the query
     */
    protected void displayResults(final DataSet dataSet) {

    	// Switch to the UI thread to execute this
    	getEditor().getSite().getShell().getDisplay().asyncExec(new Runnable() {

            public void run() {
            	
                int resultCount = dataSet.hasData() ? dataSet.getRows().length : dataSet.getUpdateCount();
                String statusMessageSmall = Messages.getString("SQLResultsView.Time.Prefix") + " "
                        + dataSet.getExecutionTime() + " "
                        + Messages.getString("SQLResultsView.Time.Postfix");
                String statusMessageLarge =  statusMessageSmall;                   
                if(resultCount >= 0)
                {
                	statusMessageLarge = statusMessageLarge + "  " + 
                    (dataSet.hasData() ? Messages.getString("SQLResultsView.Count.Prefix") : Messages.getString("SQLResultsView.Update.Prefix"))
                    + " " + resultCount;
                }
            	if(dataSet.hasData())
            	{
	            	final CTabItem tabItem = allocateResultsTab(dataSet.getQuery());
	            	if (tabItem == null)
	            		return;
	            	
	            	final DataSetResultsTab table = new DataSetResultsTab(dataSet);
	            	table.setHasStatusBar(true);
	            	table.setStatusMessage(statusMessageLarge);
	            	EditorResultsTab resultsTab = new EditorResultsTab(tabItem, table);
	            	String caption = dataSet.getCaption();
	            	if (caption != null)
	            		resultsTab.setTabTitle(caption);
	
	                // add context menu to table & cursor
	                final GenericActionGroup actionGroup = new GenericActionGroup("dataSetTableContextAction", getEditor().getSite().getShell()) {
	        			@Override
	        			public void initialiseAction(GenericAction action) {
	        				super.initialiseAction(action);
	        				ResultsTableAction dsAction = (ResultsTableAction)action;
	        				dsAction.setResultsTable(table);
	        			}
	                };
	                table.getMenuManager().addMenuListener(new IMenuListener() {
	                    public void menuAboutToShow(IMenuManager manager) {
	                        actionGroup.fillContextMenu(manager);
	                        manager.add(new Separator());
	                        manager.add(new ExportAction(new ExporterCSV(),table));
	                        manager.add(new ExportAction(new ExporterHTML(),table));
	                        manager.add(new ExportAction(new ExporterXLS(),table));
	                        manager.add(new Separator());
	                        manager.add(new ReRunAction(tabItem));
	                    }
	                });
            	}
                try {
                    // set initial message
                    setProgressMessage(Messages.getString("SQLResultsView.ConnectionWait"));
                    
                    getEditor().setMessage(statusMessageSmall);
                    
                    Query sql = dataSet.getQuery();
                    int lineNo = sql.getLineNo();
                    lineNo = getQueryParser().adjustLineNo(lineNo);
                    
                    if(getQueryParser().getContext().isOn(ExecutionContext.LOG_SUCCESS))
                    {
                    	getEditor().addMessage(new Message(Message.Status.SUCCESS, lineNo, 0, sql.getQuerySql(), statusMessageLarge));
                    }
                    
                    // reset to start message in case F5 will be used
                    setProgressMessage(Messages.getString("SQLResultsView.ConnectionWait"));

                } catch (Exception e) {
                	MessageDialog.openError(getEditor().getSite().getShell(), "Error creating result tab", e.getMessage());
                    SQLExplorerPlugin.error("Error creating result tab", e);
                }
            };
        });
    }


    private void closeStatement() {
        
        if (_stmt == null) {
            return;
        }
        if (_stmt != null) {
            try {
                _stmt.close();
            } catch (Exception e) {
                SQLExplorerPlugin.error("Error closing statement.", e);
            }
        }
        _stmt = null;
        
    }
    
    protected void doExecution(IProgressMonitor monitor) throws Exception {
        int numErrors = 0;
        SQLException lastSQLException = null;

        try {
        	long overallUpdateCount = 0;
            long overallStartTime = System.currentTimeMillis();
        	boolean stripComments = SQLExplorerPlugin.getDefault().getPreferenceStore().getBoolean(IConstants.STRIP_COMMENTS);
        	DatabaseProduct product = getEditor().getSession().getDatabaseProduct();
        	this.execution = new Execution(product);
        	for (Query query : getQueryParser()) {
            	if (monitor.isCanceled())
            		break;
            	if (getEditor().isClosed())
            		break;
            	
            	// Get the next bit of SQL to run and store it as "current"
            	if (query == null)
            		break;
            	if(stripComments)
            	{
            		query.stripComments();
            	}
            	String querySQL = query.getQuerySql().toString();
            	if (querySQL == null || querySQL.length() == 0)
            		continue;

            	startQuery(querySQL);

            	long startTime = System.currentTimeMillis();
                
                // Run it
	            DatabaseProduct.ExecutionResults results = null;
	            try {
	            	try {
		            	results = this.execution.executeQuery(_connection, query, _maxRows);
	            	}catch(RuntimeException e) {
	            		throw new SQLException(e.getMessage());
	            	}
	            	if (monitor.isCanceled())
	            	{
	            		break;
	            	}
	            	DataSet dataSet;
	            	boolean warningsChecked = false;
	            	while ((dataSet = results.nextDataSet()) != null) 
	            	{

	                    // update sql result
	            		dataSet.setQuery(query);
	            		dataSet.setExecutionTime(System.currentTimeMillis() - startTime);
	                    startTime = System.currentTimeMillis();
	

	                    if (monitor.isCanceled())
	                        return;

	            		showWarnings(results.getWarnings(), querySQL);
	            		warningsChecked = true;
	                    checkForMessages(query);
	                    
	                    // show results..
	                    displayResults(dataSet);
	            	}
	            	
	            	if (!warningsChecked)
	            	{
	            		showWarnings(results.getWarnings(), querySQL);
	                    checkForMessages(query);	            		
	            	}
	            	overallUpdateCount += results.getUpdateCount();
	            	
		            debugLogQuery(query, null);
	
	            } catch(final SQLException e) {
		            debugLogQuery(query, e);
	            	boolean stopOnError = SQLExplorerPlugin.getDefault().getPreferenceStore().getBoolean(IConstants.STOP_ON_ERROR);
	                logException(e, query, stopOnError);
	                closeStatement();
	                hasMessages = true;
	            	if (stopOnError) {
	            		if(SQLExplorerPlugin.getDefault().getPreferenceStore().getBoolean(IConstants.CONFIRM_BOOL_SHOW_DIALOG_ON_QUERY_ERROR))
	            		{
	            			errorDialog(Messages.getString("SQLResultsView.Error.Title"), e.getMessage());
	            		}
	        			return;
	            	}
	            	numErrors++;
	            	lastSQLException = e;
	            	
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
            }
            if (!hasMessages ) {
                long overallTime = System.currentTimeMillis() - overallStartTime;
                String message = Messages.getString("SQLEditor.Overall.Prefix") + " " + 
    				Long.toString(overallTime) + " " + Messages.getString("SQLEditor.Update.Postfix");
                logOverallExecution(message);
            }
        } catch (Exception e) {
            closeStatement();
            throw e;
        }
        if (numErrors == 1)
        	throw lastSQLException;
        else if (numErrors > 1 && SQLExplorerPlugin.getDefault().getPreferenceStore().getBoolean(IConstants.CONFIRM_BOOL_SHOW_DIALOG_ON_QUERY_ERROR))
            getEditor().getSite().getShell().getDisplay().asyncExec(new Runnable() {
                public void run() {
        	    	MessageDialogWithToggle dialog = MessageDialogWithToggle.openInformation(getEditor().getSite().getShell(), 
        	    			Messages.getString("SQLExecution.Error.Title"), 
        	    			Messages.getString("SQLExecution.Error.Message"), 
        	    			Messages.getString("SQLExecution.Error.Toggle"), 
        	    			false, null, null);
        	    	
        	    	if (dialog.getToggleState() && dialog.getReturnCode() == IDialogConstants.OK_ID)
        	    		SQLExplorerPlugin.setPref(IConstants.CONFIRM_BOOL_SHOW_DIALOG_ON_QUERY_ERROR, false);
                }
            });
    }
    

    protected void logHistory(String querySQL) {
        // Save successfull query
		if(getQueryParser().getContext().isOn(ExecutionContext.LOG_SQL))
		{
			SQLExplorerPlugin.getDefault().getSQLHistory().addSQL(querySQL, _session);
		}
		
	}


	protected void logOverallExecution(String message) {

    	addMessage(new Message(Message.Status.STATUS, -1, 0, "", message));
		
	}


	protected void startQuery(String querySQL) {
    	// Initialise
        setProgressMessage(Messages.getString("SQLResultsView.Executing"));
		logHistory(querySQL);
	}


	private void showWarnings(List<String> warnings, String querySql)
    {
		if(!SQLExplorerPlugin.getDefault().getPreferenceStore().getBoolean(IConstants.LOG_SQL_WARNINGS))
		{
			return;
		}
    	boolean showQuery = true;
        for(String msg : warnings)
        {
        	if(showQuery)
        	{
        		addMessage(new Message(Message.Status.STATUS, -1, 0, querySql, msg));
        		showQuery = false;
        	}
        	else
        	{
        		addMessage(new Message(Message.Status.STATUS, -1, 0, "",msg));
        	}
        }
    	
    }
	
    @Override
	protected void canceling() {
		super.canceling();
		this.execution.cancel();
	}


	/**
     * Cancel sql execution and close execution tab.
     */
    public void doStop() {

        if (_stmt != null) {
            try {
                _stmt.cancel();
            } catch (Exception e) {
                SQLExplorerPlugin.error("Error cancelling statement.", e);
            }
            try {
                closeStatement();
            } catch (Exception e) {
                SQLExplorerPlugin.error("Error closing statement.", e);
            }
        }

    }

}
