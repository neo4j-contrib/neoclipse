package net.sourceforge.sqlexplorer.plugin.actions;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.util.ImageUtil;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPage;

public class CopyTextToEditorAction extends Action 
{

    private ImageDescriptor _image = ImageUtil.getDescriptor("Images.CopyTextToEditor");
	private CopyTextProvider _provider;

    public CopyTextToEditorAction(CopyTextProvider pProvider){
    	this._provider = pProvider;
    }

    public ImageDescriptor getImageDescriptor() {

        return _image;
    }


    public String getText() {

        return Messages.getString("SQLEditor.Actions.CopyTextToEditor");
    }


    public boolean isEnabled() {

        String text = this._provider.getCopyText();
        if(text == null || text.length() == 0) {
        	return false;
        }
        
        SQLEditor editor = getCurrentSQLEditor();
        if (editor == null) {
            return false;
        }
        
        return true;
    }

	private SQLEditor getCurrentSQLEditor() {
		IWorkbenchPage page = SQLExplorerPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
        if (page == null) {
            return null;
        }
        
        if (!(page.getActiveEditor() instanceof SQLEditor)) {
            return null;
        }      
		return (SQLEditor)page.getActiveEditor();
	}


    public void run() {

        try {
            String text = this._provider.getCopyText();
            if(text == null || text.length() == 0) {
            	return;
            }

            SQLEditor editor = getCurrentSQLEditor();
            if (editor == null) {
                return;
            }
            editor.insertText(text);

        } catch (Throwable e) {
            SQLExplorerPlugin.error("Error inserting text in sql editor", e);
        }
    }
}
