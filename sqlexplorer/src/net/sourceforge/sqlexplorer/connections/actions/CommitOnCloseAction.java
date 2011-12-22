package net.sourceforge.sqlexplorer.connections.actions;

import java.util.Collection;

import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.dbproduct.User;

public class CommitOnCloseAction extends AbstractConnectionTreeAction {

    public CommitOnCloseAction() {
		super("SQLEditor.Options.CommitOnClose", "SQLEditor.Options.CommitOnClose.Tooltip", "Images.CommitIcon", AS_CHECK_BOX);
	}

    public void run() {
    	boolean enabled = isChecked();
		for (User user : getView().getSelectedUsers(false)) {
    		for (Session session : user.getSessions())
   				session.setCommitOnClose(enabled);
    		user.setCommitOnClose(enabled);
    	}
    }

    /**
     * Only show action when there is 1 alias selected
     * 
     * @see net.sourceforge.sqlexplorer.connections.actions.AbstractConnectionTreeAction#isAvailable()
     */
    public boolean isAvailable() {
		Collection<User> users = getView().getSelectedUsers(false);
    	if (users.size() != 1)
    		return false;
		for (User user : users)
			if (user.isAutoCommit())
				return false;
			else if (user.isCommitOnClose()) {
				setChecked(true);
				break;
			}

		return true;
    }
}
