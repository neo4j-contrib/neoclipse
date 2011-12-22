/*
 * Copyright (C) 2006 SQL Explorer Development Team
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
package net.sourceforge.sqlexplorer.connections;

import net.sourceforge.sqlexplorer.connections.actions.AbstractConnectionTreeAction;
import net.sourceforge.sqlexplorer.connections.actions.ChangeAliasAction;
import net.sourceforge.sqlexplorer.connections.actions.CloseAllConnectionsAction;
import net.sourceforge.sqlexplorer.connections.actions.CloseConnectionAction;
import net.sourceforge.sqlexplorer.connections.actions.CommitAction;
import net.sourceforge.sqlexplorer.connections.actions.ConnectAliasAction;
import net.sourceforge.sqlexplorer.connections.actions.NewUserAction;
import net.sourceforge.sqlexplorer.connections.actions.CopyAliasAction;
import net.sourceforge.sqlexplorer.connections.actions.CopyUserAction;
import net.sourceforge.sqlexplorer.connections.actions.DeleteAction;
import net.sourceforge.sqlexplorer.connections.actions.EditUserAction;
import net.sourceforge.sqlexplorer.connections.actions.NewAliasAction;
import net.sourceforge.sqlexplorer.connections.actions.NewDatabaseStructureViewAction;
import net.sourceforge.sqlexplorer.connections.actions.NewEditorAction;
import net.sourceforge.sqlexplorer.connections.actions.RollbackAction;
import net.sourceforge.sqlexplorer.dbproduct.Alias;
import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;
import net.sourceforge.sqlexplorer.dbproduct.User;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.actions.ActionGroup;

/**
 * ActionGroup for Database Structure View. This group controls what context
 * menu actions are being shown for which node.
 * 
 * @author Davy Vanherbergen
 */
public class ConnectionTreeActionGroup extends ActionGroup {
	
    /**
     * Fill the node context menu with all the correct actions.
     * 
     * @see org.eclipse.ui.actions.ActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
     */
    public void fillContextMenu(IMenuManager menu) {

    	ConnectionsView view = SQLExplorerPlugin.getDefault().getConnectionsView();
    	Object[] selection = (view == null) ? null : view.getSelected();

        // If nothing is selected, then show the default ones
        if (selection == null || selection.length != 1) {
        	addAction(menu, new NewAliasAction());
            return;
        }

        if (selection[0] instanceof Alias) {
        	Alias alias = (Alias)selection[0];
        	
            addAction(menu, new NewEditorAction());
            addAction(menu, new NewDatabaseStructureViewAction());
            addAction(menu, new ConnectAliasAction());
            menu.add(new Separator());
            for (User user : alias.getUsers())
            	if (!user.isAutoCommit()) {
                    addAction(menu, new CommitAction());
                    addAction(menu, new RollbackAction());
                    menu.add(new Separator());
            		break;
            	}
            if(view.getSelectedConnections(true).size() > 0)
            {
                addAction(menu, new CloseAllConnectionsAction());
            	menu.add(new Separator());
            }
            
            addAction(menu, new NewUserAction());
            addAction(menu, new ChangeAliasAction());
            addAction(menu, new CopyAliasAction());
            addAction(menu, new DeleteAction());
            
        } else if (selection[0] instanceof User) {
        	User user = (User) selection[0];
        	
            addAction(menu, new NewEditorAction());
            addAction(menu, new NewDatabaseStructureViewAction());
            addAction(menu, new ConnectAliasAction());
            
            menu.add(new Separator());
        	if (!user.isAutoCommit()) {
                addAction(menu, new CommitAction());
                addAction(menu, new RollbackAction());
                menu.add(new Separator());
        	}
            if(view.getSelectedConnections(true).size() > 0)
            {
                addAction(menu, new CloseAllConnectionsAction());
            	menu.add(new Separator());
            }
        	
            addAction(menu, new NewUserAction());
            addAction(menu, new EditUserAction());
            addAction(menu, new CopyUserAction());
            addAction(menu, new DeleteAction());
            
        } else if (selection[0] instanceof SQLConnection) {
        	SQLConnection connection = (SQLConnection)selection[0];
        	
            addAction(menu, new NewEditorAction());
            addAction(menu, new NewDatabaseStructureViewAction());
            menu.add(new Separator());
            
        	if (!connection.getUser().isAutoCommit()) {
                addAction(menu, new CommitAction());
                addAction(menu, new RollbackAction());
                menu.add(new Separator());
        	}
        	
            addAction(menu, new CloseConnectionAction());
        }
    }
    
    private boolean addAction(IMenuManager menu, AbstractConnectionTreeAction action) {
		if (action.isAvailable()){
			menu.add(action);
			action.setEnabled(true);
			return true;
		}
		return false;
    }
}
