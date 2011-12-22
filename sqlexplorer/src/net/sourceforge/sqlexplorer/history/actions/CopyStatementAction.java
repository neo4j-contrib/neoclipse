package net.sourceforge.sqlexplorer.history.actions;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.history.SQLHistoryElement;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.util.ImageUtil;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableItem;

public class CopyStatementAction extends AbstractHistoryContextAction {

    private ImageDescriptor _imageCopy = ImageUtil.getDescriptor("Images.CopyIcon");


    public ImageDescriptor getImageDescriptor() {

        return _imageCopy;
    }


    public String getText() {

        return Messages.getString("SQLHistoryView.CopyToClipboard");
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

                if (ti.length > 0) {
                    copiedText.append(queryDelimiter);
                    copiedText.append("\n");
                }
            }

            Clipboard cb = new Clipboard(Display.getCurrent());
            TextTransfer textTransfer = TextTransfer.getInstance();
            cb.setContents(new Object[] {copiedText.toString()}, new Transfer[] {textTransfer});

        } catch (Throwable e) {
            SQLExplorerPlugin.error("Error copying to clipboard", e);
        }
    }

}
