package net.sourceforge.sqlexplorer.connections.actions;

import net.sourceforge.sqlexplorer.dbproduct.User;
import net.sourceforge.sqlexplorer.dialogs.EditUserDlg;

import org.eclipse.swt.widgets.Display;

public class CopyUserAction extends AbstractConnectionTreeAction {

	public CopyUserAction() {
		super("ConnectionsView.Actions.CopyUser", "ConnectionsView.Actions.CopyUser.ToolTip", "Images.ConnectionsView.CopyUser");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
    	User user = getView().getSelectedUser(false);
		EditUserDlg dlg = new EditUserDlg(Display.getCurrent().getActiveShell(), EditUserDlg.Type.COPY, user.getAlias(), user);
        dlg.open();
        getView().refresh();
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.sqlexplorer.connections.actions.AbstractConnectionTreeAction#isAvailable()
	 */
	@Override
	public boolean isAvailable() {
    	if (getView() == null)
    		return false;
    	User user = getView().getSelectedUser(false);
    	return user != null && !user.getAlias().hasNoUserName();
	}
}
