/**
 * Licensed to Neo Technology under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Neo Technology licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.neo4j.neoclipse.connection;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.actions.ActionGroup;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.action.connect.StartAction;
import org.neo4j.neoclipse.action.connect.StopAction;
import org.neo4j.neoclipse.connection.actions.DeleteAliasAction;
import org.neo4j.neoclipse.connection.actions.EditAliasAction;
import org.neo4j.neoclipse.connection.actions.ExportToJsonAction;
import org.neo4j.neoclipse.connection.actions.ExportToXmlAction;
import org.neo4j.neoclipse.connection.actions.ForceStartAction;
import org.neo4j.neoclipse.connection.actions.NewAliasAction;
import org.neo4j.neoclipse.graphdb.GraphDbServiceManager;
import org.neo4j.neoclipse.view.NeoGraphViewPart;

/**
 * ActionGroup for Database Structure View. This group controls what context
 * menu actions are being shown for which node.
 * 
 * @author Radhakrishna Kalyan
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
        GraphDbServiceManager graphDbServiceManager = Activator.getDefault().getGraphDbServiceManager();

        Alias alias = view.getSelectedAlias();
        if ( alias == null )
        {
            addAction( menu, new NewAliasAction() );
            return;
        }

        if ( graphDbServiceManager.isRunning() && graphDbServiceManager.getCurrentAlias().equals( alias ) )
        {
            addAction( menu, new StopAction( neoGraphView ) );
            addAction( menu, new ExportToXmlAction() );
            addAction( menu, new ExportToJsonAction() );

        }
        else
        {
            if ( !graphDbServiceManager.isRunning() )
            {
                addAction( menu, new StartAction( neoGraphView ) );
                menu.add( new Separator() );
            }
            else
            {
                addAction( menu, new ForceStartAction( neoGraphView ) );
                menu.add( new Separator() );
            }
            addAction( menu, new NewAliasAction() );
            addAction( menu, new EditAliasAction() );
            addAction( menu, new DeleteAliasAction() );
        }
    }

    private boolean addAction( IMenuManager menu, Action action )
    {
        menu.add( action );
        action.setEnabled( true );
        return true;
    }
}
