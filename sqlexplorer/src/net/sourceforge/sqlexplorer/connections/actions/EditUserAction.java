package net.sourceforge.sqlexplorer.connections.actions;

import org.eclipse.swt.widgets.Display;

import net.sourceforge.sqlexplorer.dbproduct.User;
import net.sourceforge.sqlexplorer.dialogs.EditUserDlg;

public class EditUserAction extends AbstractConnectionTreeAction {

	public EditUserAction() {
		super("ConnectionsView.Actions.EditUser", null, "Images.ConnectionsView.EditUser");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
    	User user = getView().getSelectedUser(false);
		EditUserDlg dlg = new EditUserDlg(Display.getCurrent().getActiveShell(), EditUserDlg.Type.EDIT, user.getAlias(), user);
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
