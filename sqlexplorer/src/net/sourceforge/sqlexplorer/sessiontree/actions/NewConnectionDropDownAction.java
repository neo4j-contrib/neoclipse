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
package net.sourceforge.sqlexplorer.sessiontree.actions;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbproduct.Alias;
import net.sourceforge.sqlexplorer.dbproduct.User;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.util.ImageUtil;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * @author Aadi
 * 
 */
public class NewConnectionDropDownAction extends Action implements IMenuCreator, IViewActionDelegate {
    private Menu menu;


    public NewConnectionDropDownAction() {
        setText(Messages.getString("ConnectionsView.Actions.NewConnection"));
        setToolTipText(Messages.getString("ConnectionsView.Actions.NewConnectionToolTip"));
        setImageDescriptor(ImageUtil.getDescriptor("Images.NewConnectionIcon"));
        setMenuCreator(this);
    }

    public void dispose() {
        if (menu != null) {
            menu.dispose();
        }
    }

    public Menu getMenu(Control parent) {
        if (menu != null) {
            menu.dispose();
            menu = null;
        }

        for (Alias alias : SQLExplorerPlugin.getDefault().getAliasManager().getAliases())
        	for (User user : alias.getUsers()) {
        		if (menu == null)
        			menu = new Menu(parent);
                NewConnection action = new NewConnection(user);
                addActionToMenu(menu, action);
        	}

        return menu;
    }

    public Menu getMenu(Menu parent) {
        return null;
    }

    protected void addActionToMenu(Menu parent, Action action) {
        ActionContributionItem item = new ActionContributionItem(action);
        item.fill(parent, -1);
    }

    public void init(IViewPart view) {
        
    }

    public void run(IAction action) {
        // noop
    }

    public void selectionChanged(IAction action, ISelection selection) {
        // noop
    }

}