package net.sourceforge.sqlexplorer.history.actions;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.history.SQLHistoryElement;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.util.ImageUtil;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbenchPage;

public class AppendToEditorAction extends AbstractHistoryContextAction {

    private ImageDescriptor _imageOpenInEditor = ImageUtil.getDescriptor("Images.AppendToEditor");


    public ImageDescriptor getImageDescriptor() {

        return _imageOpenInEditor;
    }


    public String getText() {

        return Messages.getString("SQLHistoryView.AppendToEditor");
    }


    public boolean isEnabled() {

        TableItem[] ti = _table.getSelection();
        if (ti == null || ti.length == 0) {
            return false;
        }
        
        IWorkbenchPage page = SQLExplorerPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
        if (page == null) {
            return false;
        }
        
        if (page.getActiveEditor() == null) {
            return false;
        } 
        
        if (!(page.getActiveEditor() instanceof SQLEditor)) {
            return false;
        }      
        
        return true;
    }


    public void run() {

        try {
            TableItem[] ti = _table.getSelection();
            if (ti == null || ti.length == 0) {
                return;
            }

            String queryDelimiter = SQLExplorerPlugin.getStringPref(
                    IConstants.SQL_QRY_DELIMITER);
            StringBuffer copiedText = new StringBuffer();

            for (int i = 0; i < ti.length; i++) {

                SQLHistoryElement el = (SQLHistoryElement) ti[i].getData();
                copiedText.append(el.getRawSQLString());
                copiedText.append(queryDelimiter);

                if (ti.length > 1) {
                    copiedText.append("\n");
                }
            }

            IWorkbenchPage page = SQLExplorerPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
            if (page == null) {
                return;
            }
            SQLEditor editorPart = (SQLEditor) page.getActiveEditor();
            editorPart.setText(editorPart.getSQLToBeExecuted() + "\n" + copiedText.toString());

        } catch (Throwable e) {
            SQLExplorerPlugin.error("Error creating sql editor", e);
        }
    }
}
