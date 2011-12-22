/*
 * Copyright (C) 2007 SQL Explorer Development Team
 * http://sourceforge.net/projects/eclipsesql
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sourceforge.sqlexplorer.connections.actions;

import java.util.Collection;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import net.sourceforge.sqlexplorer.SQLCannotConnectException;
import net.sourceforge.sqlexplorer.dbproduct.User;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.views.DatabaseStructureView;

/**
 * Implements "Examine Database Structure"
 * @author John Spackman
 *
 */
public class NewDatabaseStructureViewAction extends AbstractConnectionTreeAction {

	public NewDatabaseStructureViewAction() {
		super("ConnectionsView.Actions.NewDatabaseStructure", "ConnectionsView.Actions.NewDatabaseStructure.Tooltip", "Images.NewDatabaseStructure");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		DatabaseStructureView view = SQLExplorerPlugin.getDefault().showDatabaseStructureView();
		if (view == null)
			return;

		Collection<User> users = getView().getSelectedUsers(true);
		for (User user : users)
			try {
				view.addUser(user);
			}catch(SQLCannotConnectException e) {
	        	MessageDialog.openError(Display.getDefault().getActiveShell(), "Cannot connect", e.getMessage());
			}
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.sqlexplorer.connections.actions.AbstractConnectionTreeAction#isAvailable()
	 */
	@Override
	public boolean isAvailable() {
		Collection<User> users = getView().getSelectedUsers(true);
		return !users.isEmpty();
	}

}
