package net.sourceforge.sqlexplorer.history.actions;

import net.sourceforge.sqlexplorer.history.SQLHistory;
import net.sourceforge.sqlexplorer.plugin.views.SQLHistoryView;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.ui.actions.ActionGroup;

public class SQLHistoryActionGroup extends ActionGroup {

    private AbstractHistoryContextAction _appendToEditorAction;

    private AbstractHistoryContextAction _clearHistoryAction;

    private AbstractHistoryContextAction _copyAction;

    private AbstractHistoryContextAction _openInEditorAction;

    private AbstractHistoryContextAction _removeFromHistoryAction;

    private TableViewer _tableViewer;


    /**
     * Construct a new action group for sql history
     * 
     * @param tableViewer used for history
     */
    public SQLHistoryActionGroup(SQLHistoryView view, SQLHistory history, TableViewer tableViewer, IToolBarManager toolbarMgr) {

        _tableViewer = tableViewer;

        _openInEditorAction = new OpenInEditorAction();
        _appendToEditorAction = new AppendToEditorAction();
        _removeFromHistoryAction = new RemoveFromHistoryAction();
        _clearHistoryAction = new ClearHistoryAction();
        _copyAction = new CopyStatementAction();

        _openInEditorAction.setTableViewer(tableViewer);
        _appendToEditorAction.setTableViewer(tableViewer);
        _removeFromHistoryAction.setTableViewer(tableViewer);
        _clearHistoryAction.setTableViewer(tableViewer);
        _copyAction.setTableViewer(tableViewer);

        _openInEditorAction.setHistory(history);
        _appendToEditorAction.setHistory(history);
        _removeFromHistoryAction.setHistory(history);
        _clearHistoryAction.setHistory(history);
        _copyAction.setHistory(history);

        _openInEditorAction.setView(view);
        _appendToEditorAction.setView(view);
        _removeFromHistoryAction.setView(view);
        _clearHistoryAction.setView(view);
        _copyAction.setView(view);
        
        toolbarMgr.add(_openInEditorAction);
        toolbarMgr.add(_appendToEditorAction);
        toolbarMgr.add(_removeFromHistoryAction);
        toolbarMgr.add(_clearHistoryAction);
        toolbarMgr.add(new Separator());
        toolbarMgr.add(_copyAction);

    }



    /**
     * Fill the node context menu with all the correct actions.
     * 
     * @see org.eclipse.ui.actions.ActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
     */
    public void fillContextMenu(IMenuManager menu) {

        // find our target node..
        IStructuredSelection selection = (IStructuredSelection) _tableViewer.getSelection();

        // check if we have a valid selection
        if (selection == null) {
            return;
        }

        menu.add(_openInEditorAction);
        menu.add(_appendToEditorAction);
        menu.add(_removeFromHistoryAction);
        menu.add(_clearHistoryAction);
        menu.add(new Separator());
        menu.add(_copyAction);

    }


    public void refresh() {

        _openInEditorAction.setEnabled(_openInEditorAction.isEnabled());
        _appendToEditorAction.setEnabled(_appendToEditorAction.isEnabled());
        _removeFromHistoryAction.setEnabled(_removeFromHistoryAction.isEnabled());
        _clearHistoryAction.setEnabled(_clearHistoryAction.isEnabled());
        _copyAction.setEnabled(_copyAction.isEnabled());
    }
}
