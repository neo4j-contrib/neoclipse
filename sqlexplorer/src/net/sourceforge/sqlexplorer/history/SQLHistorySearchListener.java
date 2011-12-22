package net.sourceforge.sqlexplorer.history;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Text;

public class SQLHistorySearchListener implements ModifyListener {

    private SQLHistory _history;


    public SQLHistorySearchListener(SQLHistory history) {

        _history = history;
    }


    public void modifyText(ModifyEvent e) {

        Text t = (Text) e.widget;
        int results = _history.setQryString(t.getText());

        if (results == 0) {
            // highlight search bar
            t.setBackground(new Color(t.getDisplay(), 255, 102, 102));
            t.setForeground(t.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        } else {
            // undo highlight
            t.setBackground(t.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
            t.setForeground(t.getDisplay().getSystemColor(SWT.COLOR_BLACK));
        }

    }

}
