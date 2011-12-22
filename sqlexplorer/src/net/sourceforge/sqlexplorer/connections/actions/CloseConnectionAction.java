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

import java.util.Set;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;
import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.util.ImageUtil;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.ui.IViewActionDelegate;

/**
 * @author Davy Vanherbergen
 * 
 */
public class CloseConnectionAction extends AbstractConnectionTreeAction implements IViewActionDelegate {

    public CloseConnectionAction() {
    	super("ConnectionsView.Actions.CloseConnection", "ConnectionsView.Actions.CloseConnectionToolTip", "Images.CloseConnIcon");
    	setDisabledImageDescriptor(ImageUtil.getDescriptor("Images.DisabledCloseConnIcon"));
    }

    public void run() {
    	boolean confirm = SQLExplorerPlugin.getBooleanPref(IConstants.CONFIRM_BOOL_CLOSE_CONNECTION);
		for (SQLConnection connection : getView().getSelectedConnections(false)) {
			Session session = connection.getSession();
			if (session != null && !session.isConnectionInUse()) {
		    	if (confirm) {
			    	MessageDialogWithToggle dialog = MessageDialogWithToggle.openYesNoQuestion(getView().getSite().getShell(), 
			    			Messages.getString("ConnectionsView.Actions.Close.Confirm.Title"), 
			    			Messages.getString("ConnectionsView.Actions.Close.Confirm.Message"), 
			    			Messages.getString("ConnectionsView.Actions.Close.Confirm.Toggle"), 
			    			false, null, null);

			    	if (dialog.getToggleState() && dialog.getReturnCode() == IDialogConstants.YES_ID)
			    		SQLExplorerPlugin.setPref(IConstants.CONFIRM_BOOL_CLOSE_CONNECTION, false);
			    	if (dialog.getReturnCode() != IDialogConstants.YES_ID)
			    		return;
		    	}
				session.disposeConnection();
			} else if (session == null)
				connection.getUser().releaseFromPool(connection);
		}

        getView().refresh();
    }

    /**
     * Action is available when there is at least one session selected.
     * 
     * @see net.sourceforge.sqlexplorer.connections.actions.AbstractConnectionTreeAction#isAvailable()
     */
    public boolean isAvailable() {
    	if (getView() == null)
    		return false;
    	Set<SQLConnection> connections = getView().getSelectedConnections(false);
    	for (SQLConnection connection : connections) {
    		if (connection.getSession() == null || !connection.getSession().isConnectionInUse())
    			return true;
    	}
    	return false;
    }

}
