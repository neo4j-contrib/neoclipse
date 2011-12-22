/**
 * 
 */
package net.sourceforge.sqlexplorer.sqleditor.handlers;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.parsers.ParserException;
import net.sourceforge.sqlexplorer.parsers.QueryParser;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.sqlpanel.AbstractSQLExecution;
import net.sourceforge.sqlexplorer.sqlpanel.SQLExecution;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * @author Heiko Hilbert
 *
 */
public class ExecSQLHandler extends AbstractHandler implements IHandler {
	public static final String COMMAND_ID = "net.sourceforge.sqlexplorer.executeSQL";

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent pEvent) throws ExecutionException {
		final IEditorPart editor = HandlerUtil.getActiveEditor(pEvent);
		if(editor instanceof SQLEditor)
		{
	        try {
	        	execute((SQLEditor) editor);
	        } catch (final Exception e) {
	        	SQLExplorerPlugin.error(e);
	            editor.getSite().getShell().getDisplay().syncExec(new Runnable() {
	
	                public void run() {
	                    MessageDialog.openError(editor.getSite().getShell(), Messages.getString("SQLResultsView.Error.Title"), e.getClass().getCanonicalName() + ": " + e.getMessage());
	                }
	            });
	        }
		}
		return null;
	}

    private void execute(final SQLEditor pEditor) throws Exception {
    	// Find out how much to restrict results by
        Integer iMax = pEditor.getLimitResults();
        if (iMax == null)
	        pEditor.getSite().getShell().getDisplay().syncExec(new Runnable() {
	            public void run() {
	                MessageDialog.openError(pEditor.getSite().getShell(), Messages.getString("SQLEditor.Error.InvalidRowLimit.Title"), Messages.getString("SQLEditor.Error.InvalidRowLimit"));
	            }
	        });
        final int maxresults = (iMax == null) ? 0 : iMax.intValue();
        if (maxresults < 0)
            throw new Exception(Messages.getString("SQLEditor.LimitRows.Error"));

        
        boolean confirmWarnLargeMaxrows = SQLExplorerPlugin.getBooleanPref(IConstants.CONFIRM_BOOL_WARN_LARGE_MAXROWS);
        int warnLimit = SQLExplorerPlugin.getIntPref(IConstants.WARN_LIMIT);

        // Confirm with the user if they've left it too large
        if (confirmWarnLargeMaxrows && (maxresults == 0 || maxresults > warnLimit)) {
            pEditor.getSite().getShell().getDisplay().syncExec(new Runnable() {

                public void run() {

                    MessageDialogWithToggle dlg = MessageDialogWithToggle.openOkCancelConfirm(pEditor.getSite().getShell(),
                            Messages.getString("SQLEditor.LimitRows.ConfirmNoLimit.Title"),
                            Messages.getString("SQLEditor.LimitRows.ConfirmNoLimit.Message"),
                            Messages.getString("SQLEditor.LimitRows.ConfirmNoLimit.Toggle"),
                            false, null, null);
                    if (dlg.getReturnCode() == IDialogConstants.OK_ID) {
                    	if (dlg.getToggleState())
                    		SQLExplorerPlugin.setPref(IConstants.CONFIRM_BOOL_WARN_LARGE_MAXROWS, false);
                        execute(pEditor, maxresults);
                    }
                }
            });
            
        // Run it
        } else {
            execute(pEditor, maxresults);
        }

    }

    protected QueryParser getQueryParser(Session session, SQLEditor pEditor)
    {
    	return  session.getDatabaseProduct().getQueryParser(pEditor.getSQLToBeExecuted(false), pEditor.getSQLLineNumber(false));
    }
    
    protected void execute(final SQLEditor _editor, int maxRows) {
        Session session = getSession(_editor); 
        if (session == null)
            return;
        QueryParser qt = getQueryParser(session, _editor);
        try {
            qt.parse();
        }catch(final ParserException e) {
            _editor.getSite().getShell().getDisplay().asyncExec(new Runnable() {
                public void run() {
                    MessageDialog.openError(_editor.getSite().getShell(), Messages.getString("SQLResultsView.Error.Title"), e.getMessage());
                }
            });
        }
        
        if (qt.iterator().hasNext()) {
        	boolean clearResults = SQLExplorerPlugin.getDefault().getPreferenceStore().getBoolean(IConstants.CLEAR_RESULTS_ON_EXECUTE);
        	if (clearResults)
        		_editor.clearResults();
        	AbstractSQLExecution job = new SQLExecution(_editor, qt, maxRows);
        	job.schedule();
        }
    }
	
    private Session getSession(final SQLEditor pEditor) {
    	Session session = pEditor.getSession();
        if (session != null && session.isConnectionInUse()) {
            pEditor.getSite().getShell().getDisplay().syncExec(new Runnable() {
                public void run() {
                    MessageDialog.openError(pEditor.getSite().getShell(), Messages.getString("SQLResultsView.Error.InUseTitle"), Messages.getString("SQLResultsView.Error.InUse"));
                }
            });
            return null;
        }
        return session;
    }
    
}
