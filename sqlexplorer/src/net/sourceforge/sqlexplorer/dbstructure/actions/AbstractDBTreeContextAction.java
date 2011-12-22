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

import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.views.DatabaseStructureView;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;


/**
 * Abstract implementation for a context menu action in the database structure view.
 * Extend this class to add actions to the structure view.
 * 
 * @author Davy Vanherbergen
 */
public abstract class AbstractDBTreeContextAction extends Action {

    protected INode[] _selectedNodes;
    
    protected TreeViewer _treeViewer;
    
    /**
     * Store nodes for use in the actions.
     * @param nodes
     */
    public final void setSelectedNodes(INode[] nodes) {
        _selectedNodes = nodes;        
    }


    /**
     * Store treeViewer for use in the actions
     * @param treeViewer
     */
    public void setTreeViewer(TreeViewer treeViewer) {
       _treeViewer = treeViewer;        
    }


    /**
     * Implement this method to return true when your action is available
     * for the selected node(s).  When true, the action will be included in the
     * context menu, when false it will be ignored.
     * 
     * 
     * @return true if the action should be included in the context menu
     */
    public boolean isAvailable() {
        return true;
    }

    /**
     * Implement this method to return true when your action is the default action
     * for the selected node.  When true, the action will be run when a double click
     * on a node occurs. 
     * 
     * 
     * @return true if the action should be included in the context menu
     */
    public boolean isDefault() {
        return false;
    }
    
	protected DatabaseStructureView getView() {
		return SQLExplorerPlugin.getDefault().getDatabaseStructureView();
	}
}
