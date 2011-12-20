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
package org.neo4j.neoclipse.connection.actions;

import org.eclipse.ui.IViewActionDelegate;
import org.neo4j.neoclipse.action.Actions;
import org.neo4j.neoclipse.connection.AbstractConnectionTreeAction;

/**
 * @author Davy Vanherbergen
 * 
 */
public class NewEditorAction extends AbstractConnectionTreeAction implements IViewActionDelegate
{

    public NewEditorAction()
    {
        super( Actions.NEW_CYPHER_EDITOR );
    }

    @Override
    public void run()
    {
        getConnectionView().openNewEditor();
        getConnectionView().refresh();
    }

    /**
     * Only show action when there is at least 1 item selected
     * 
     * @see net.sourceforge.sqlexplorer.connections.actions.AbstractConnectionTreeAction#isAvailable()
     */
    public boolean isAvailable()
    {
        if ( getConnectionView() == null )
        {
            return false;
        }
        return getConnectionView().getSelectedAlias() != null;
    }
}
