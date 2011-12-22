package net.sourceforge.sqlexplorer.history.actions;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.util.ImageUtil;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;

public class ClearHistoryAction extends AbstractHistoryContextAction {

    private ImageDescriptor _imageRemoveAll = ImageUtil.getDescriptor("Images.RemoveAllIcon");


    public ImageDescriptor getImageDescriptor() {

        return _imageRemoveAll;
    }


    public String getText() {

        return Messages.getString("SQLHistoryView.ClearHistory");
    }


    public void run() {

        try {

            boolean ok = MessageDialog.openConfirm(_table.getShell(),
                    Messages.getString("SQLHistoryView.ClearHistory"),
                    Messages.getString("SQLHistoryView.ClearHistory.Confirm"));

            if (ok) {
                _history.clear();
                _table.deselectAll();
                setEnabled(false);
            }
        } catch (Throwable e) {
            SQLExplorerPlugin.error("Error clearing sql history", e);
        }
    }
}
