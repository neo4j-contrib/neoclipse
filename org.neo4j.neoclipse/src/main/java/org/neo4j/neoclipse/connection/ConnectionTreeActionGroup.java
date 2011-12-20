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
package org.neo4j.neoclipse.connection;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.actions.ActionGroup;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.action.connect.StartAction;
import org.neo4j.neoclipse.action.connect.StopAction;
import org.neo4j.neoclipse.view.NeoGraphViewPart;

/**
 * ActionGroup for Database Structure View. This group controls what context
 * menu actions are being shown for which node.
 * 
 * @author Davy Vanherbergen
 */
public class ConnectionTreeActionGroup extends ActionGroup
{

    /**
     * Fill the node context menu with all the correct actions.
     * 
     * @see org.eclipse.ui.actions.ActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
     */
    @Override
    public void fillContextMenu( IMenuManager menu )
    {

        ConnectionsView view = Activator.getDefault().getConnectionsView();
        NeoGraphViewPart neoGraphView = Activator.getDefault().getNeoGraphViewPart();

        Object[] selection = ( view == null ) ? null : view.getSelected();

        if ( selection[0] instanceof Alias )
        {
            Alias alias = (Alias) selection[0];

            addAction( menu, new StartAction( neoGraphView ) );
            addAction( menu, new StopAction( neoGraphView ) );

        }
    }

    private boolean addAction( IMenuManager menu, Action action )
    {
        menu.add( action );
        action.setEnabled( true );
        return true;
    }
}
