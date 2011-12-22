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

import net.sourceforge.sqlexplorer.dbproduct.User;
import net.sourceforge.sqlexplorer.util.ImageUtil;
import org.eclipse.ui.IViewActionDelegate;

/**
 * @author Davy Vanherbergen
 * 
 */
public class NewEditorAction extends AbstractConnectionTreeAction implements IViewActionDelegate {

    public NewEditorAction() {
    	super("ConnectionsView.Actions.NewEditor", "ConnectionsView.Actions.NewEditorToolTip", "Images.OpenSQLIcon");
    	setDisabledImageDescriptor(ImageUtil.getDescriptor("Images.AliasIcon"));
    }
    
    public void run() {
    	Set<User> users = getView().getSelectedUsers(true);
    	for (User user : users)
    		getView().openNewEditor(user);
        getView().refresh();
    }

    /**
     * Only show action when there is at least 1 item selected
     * 
     * @see net.sourceforge.sqlexplorer.connections.actions.AbstractConnectionTreeAction#isAvailable()
     */
    public boolean isAvailable() {
    	if (getView() == null)
    		return false;
    	return getView().getSelectedUsers(true) != null;
    }
}
