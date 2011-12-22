/*
 * Copyright (C) 2006 Davy Vanherbergen
 * dvanherbergen@users.sourceforge.net
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

package net.sourceforge.sqlexplorer.dbstructure.actions;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbproduct.Alias;
import net.sourceforge.sqlexplorer.util.ImageUtil;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Refresh this node from the alias metadata filter.
 * 
 * @author Davy Vanherbergen
 * 
 */
public class RemoveNodeFilterAction extends AbstractDBTreeContextAction {

    private static final ImageDescriptor _image = ImageUtil.getDescriptor("Images.FilterIcon");


    /**
     * Custom image for action
     * 
     * @see org.eclipse.jface.action.IAction#getImageDescriptor()
     */
    public ImageDescriptor getImageDescriptor() {
        return _image;
    }


    /**
     * Set the text for the menu entry.
     * 
     * @see org.eclipse.jface.action.IAction#getText()
     */
    public String getText() {
        return Messages.getString("DatabaseStructureView.Actions.RemoveNodeFilter");
    }


    /**
     * Refresh selected node and descendants.
     * 
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run() {

        Alias alias = (Alias) _selectedNodes[0].getSession().getUser().getAlias();
        alias.setNameFilterExpression("");
        alias.setFolderFilterExpression("");
        alias.setSchemaFilterExpression("");
        
        getView().refreshSessionTrees(_selectedNodes[0].getSession());
    }


    /**
     * Action is availble when a node is selected and filter is active
     * 
     * @see net.sourceforge.sqlexplorer.dbstructure.actions.AbstractDBTreeContextAction#isAvailable()
     */
    public boolean isAvailable() {

        if (_selectedNodes.length == 0) {
            return false;
        }

        Alias alias = (Alias) _selectedNodes[0].getSession().getUser().getAlias();
        if (alias.isFiltered()) {
            return true;
        }

        return false;
    }
}
