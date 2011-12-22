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
import net.sourceforge.sqlexplorer.sqlpanel.SQLBatchExecution;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * @author Heiko
 *
 */
public class ExecSQLBatchHandler extends AbstractHandler implements IHandler {
	public static final String COMMAND_ID = "net.sourceforge.sqlexplorer.executeSQLBatch";

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
    private void execute(final SQLEditor pEditor) {
        Session session = getSession(pEditor);
        if (session == null)
            return;

        QueryParser qt = getQueryParser(session, pEditor);
        try {
            qt.parse();
        }catch(final ParserException e) {
            pEditor.getSite().getShell().getDisplay().asyncExec(new Runnable() {
                public void run() {
                    MessageDialog.openError(pEditor.getSite().getShell(), Messages.getString("SQLResultsView.Error.Title"), e.getMessage());
                }
            });
        }
        
        if (qt.iterator().hasNext()) {
        	boolean clearResults = SQLExplorerPlugin.getDefault().getPreferenceStore().getBoolean(IConstants.CLEAR_RESULTS_ON_EXECUTE);
        	if (clearResults)
        		pEditor.clearResults();
        	AbstractSQLExecution job = new SQLBatchExecution(pEditor, qt);
        	job.schedule();
        }
    }

    private QueryParser getQueryParser(Session session, SQLEditor pEditor)
    {
    	return  session.getDatabaseProduct().getQueryParser(pEditor.getSQLToBeExecuted(false), pEditor.getSQLLineNumber(false));
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
