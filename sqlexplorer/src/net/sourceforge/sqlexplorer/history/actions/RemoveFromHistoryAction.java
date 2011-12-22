package net.sourceforge.sqlexplorer.history.actions;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.history.SQLHistoryElement;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.util.ImageUtil;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.TableItem;

public class RemoveFromHistoryAction extends AbstractHistoryContextAction {

    private ImageDescriptor _imageRemove = ImageUtil.getDescriptor("Images.RemoveIcon");


    public ImageDescriptor getImageDescriptor() {

        return _imageRemove;
    }


    public String getText() {

        return Messages.getString("SQLHistoryView.RemoveFromHistory");
    }


    public boolean isEnabled() {

        TableItem[] ti = _table.getSelection();
        if (ti == null || ti.length == 0) {
            return false;
        }
        return true;
    }


    public void run() {

        try {
            TableItem[] selections = _table.getSelection();
            if (selections != null && selections.length != 0) {
                for (int i = 0; i < selections.length; i++) {
                    SQLHistoryElement el = (SQLHistoryElement) selections[i].getData();
                    if (el != null) {
                        _history.remove(el);
                    }
                }
            }
            _table.deselectAll();
            setEnabled(false);

        } catch (Throwable e) {
            SQLExplorerPlugin.error("Error removing item from clipboard", e);
        }
    }

}
