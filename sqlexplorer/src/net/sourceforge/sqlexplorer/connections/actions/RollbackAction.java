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
package net.sourceforge.sqlexplorer.connections.actions;

import java.sql.SQLException;

import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import org.eclipse.ui.IViewActionDelegate;

/**
 * @author Davy Vanherbergen
 * 
 */
public class RollbackAction extends AbstractConnectionTreeAction implements IViewActionDelegate {
	
	public RollbackAction() {
		super("ConnectionsView.Actions.Rollback", null, "Images.RollbackIcon");
	}

    public void run() {
    	for (SQLConnection connection: getView().getSelectedConnections(true))
			try {
				if (!connection.getAutoCommit())
    				connection.rollback();
			}catch(SQLException e) {
				SQLExplorerPlugin.error("Cannot rollback session", e);
			}
    }

    /**
     * Action is available when there is at least one session without autocommit
     * selected
     * 
     * @see net.sourceforge.sqlexplorer.connections.actions.AbstractConnectionTreeAction#isAvailable()
     */
    public boolean isAvailable() {
    	if (getView() == null)
    		return false;
    	for (SQLConnection connection: getView().getSelectedConnections(true))
			try {
				if (!connection.getAutoCommit())
    				return true;
			}catch(SQLException e) {
//				SQLExplorerPlugin.error("Cannot query auto commit state", e);
			}

        return false;
    }
}
