package net.sourceforge.sqlexplorer.connections.actions;

import java.sql.SQLException;
import java.util.Collection;

import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.dbproduct.User;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;

public class AutoCommitAction extends AbstractConnectionTreeAction {

    public AutoCommitAction() {
		super("SQLEditor.Options.AutoCommit", "SQLEditor.Options.AutoCommit.Tooltip", "Images.CommitIcon", AS_CHECK_BOX);
	}

    public void run() {
		try {
			boolean enabled = isChecked();
	    	for (User user : getView().getSelectedUsers(false)) {
	    		for (Session session : user.getSessions())
	   				session.setAutoCommit(enabled);
	    		user.setAutoCommit(enabled);
	    	}
		}catch(SQLException e) {
			SQLExplorerPlugin.error(e);
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
			if (user.isAutoCommit()) {
				setChecked(true);
				break;
			}

		return true;
    }
}
