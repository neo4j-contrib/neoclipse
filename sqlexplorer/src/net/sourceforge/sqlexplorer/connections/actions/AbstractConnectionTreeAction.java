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

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.connections.ConnectionsView;
import net.sourceforge.sqlexplorer.dbproduct.AliasManager;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.util.ImageUtil;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * Abstract implementation for a context menu action in the connection view.
 * Extend this class to add actions.
 * 
 * @author Davy Vanherbergen
 */
public abstract class AbstractConnectionTreeAction extends Action implements IViewActionDelegate {
	
	public AbstractConnectionTreeAction(String textId, String toolTipId, String imageId) {
		this(textId, toolTipId, imageId, SWT.NONE);
	}
	
	public AbstractConnectionTreeAction(String textId, String toolTipId, String imageId, int style) {
		super(Messages.getString(textId));
		setToolTipText(Messages.getString((toolTipId != null) ? toolTipId : textId));
		if (imageId != null) {
			ImageDescriptor image = ImageUtil.getDescriptor(imageId);
			setImageDescriptor(image);
			setHoverImageDescriptor(image);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		run();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
        action.setEnabled(isAvailable());
	}

	/**
     * Implement this method to return true when your action is available for
     * the selected node(s). When true, the action will be included in the
     * context menu, when false it will be ignored.
     * 
     * 
     * @return true if the action should be included in the context menu
     */
    public boolean isAvailable() {
        return true;
    }
    
    /**
     * Shorthand helper method to return all aliases
     * @return
     */
    public AliasManager getAliases() {
    	return SQLExplorerPlugin.getDefault().getAliasManager();
    }

    /**
     * Shorthand helper method to return the view
     * @return
     */
	protected ConnectionsView getView() {
		return SQLExplorerPlugin.getDefault().getConnectionsView();
	}
}
