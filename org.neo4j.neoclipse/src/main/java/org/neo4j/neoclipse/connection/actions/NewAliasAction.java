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

import org.eclipse.swt.widgets.Display;
import org.neo4j.neoclipse.action.Actions;
import org.neo4j.neoclipse.connection.AbstractConnectionTreeAction;
import org.neo4j.neoclipse.connection.dialogs.CreateAliasDialog;

/**
 * @author Davy Vanherbergen
 * 
 */
public class NewAliasAction extends AbstractConnectionTreeAction
{

    public NewAliasAction()
    {
        super( Actions.NEW_CONNECTION );
    }

    @Override
    public void run()
    {
        CreateAliasDialog dlg = new CreateAliasDialog( Display.getCurrent().getActiveShell(),
                CreateAliasDialog.Type.CREATE );
        dlg.open();
        getConnectionView().refresh();
    }
}
