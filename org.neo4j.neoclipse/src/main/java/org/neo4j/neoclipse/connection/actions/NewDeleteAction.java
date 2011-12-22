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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.action.Actions;
import org.neo4j.neoclipse.connection.AbstractConnectionTreeAction;
import org.neo4j.neoclipse.connection.Alias;
import org.neo4j.neoclipse.graphdb.GraphDbServiceManager;

/**
 * @author Radhakrishna Kalyan
 * 
 */
public class NewDeleteAction extends AbstractConnectionTreeAction
{

    public NewDeleteAction()
    {
        super( Actions.DELETE );
    }

    @Override
    public void run()
    {
        Alias selectedAlias = getConnectionView().getSelectedAlias();
        if ( selectedAlias == null )
        {
            MessageDialog.openWarning( Display.getCurrent().getActiveShell(), "Delete Connection",
                    "Please select a connection to delete" );
            return;
        }

        GraphDbServiceManager graphDbServiceManager = Activator.getDefault().getGraphDbServiceManager();
        if ( graphDbServiceManager.isRunning() && graphDbServiceManager.getCurrentAlias().equals( selectedAlias ) )
        {
            MessageDialog.openWarning( Display.getCurrent().getActiveShell(), "Delete Connection",
                    "Please stop the service before deleting." );
            return;
        }

        boolean okToDelete = MessageDialog.openConfirm( Display.getCurrent().getActiveShell(), "Delete Connection",
                "Are you sure you want to delete the connection?" );

        if ( !okToDelete )
        {
            return;
        }
        selectedAlias.remove();
        getConnectionView().refresh();
    }
}
