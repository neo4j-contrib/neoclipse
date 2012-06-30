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
package org.neo4j.neoclipse.connection.actions;

import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.connection.Alias;
import org.neo4j.neoclipse.connection.ConnectionsView;
import org.neo4j.neoclipse.event.NeoclipseEvent;
import org.neo4j.neoclipse.event.NeoclipseEventListener;
import org.neo4j.neoclipse.graphdb.GraphDbServiceManager;
import org.neo4j.neoclipse.view.ErrorMessage;
import org.neo4j.neoclipse.view.NeoGraphViewPart;
import org.neo4j.neoclipse.view.UiHelper;

/**
 * Handle change in the relationship color settings.
 */
public class ForceStartHandler implements NeoclipseEventListener
{
    @Override
    public void stateChanged( final NeoclipseEvent event )
    {
        UiHelper.asyncExec( new Runnable()
        {
            @Override
            public void run()
            {
                GraphDbServiceManager gsm = Activator.getDefault().getGraphDbServiceManager();
                NeoGraphViewPart graphView = Activator.getDefault().getNeoGraphViewPart();
                ConnectionsView connectionsView = Activator.getDefault().getConnectionsView();
                try
                {
                    Alias selectedAlias = connectionsView.getSelectedAlias();
                    gsm.startGraphDbService( selectedAlias ).get();
                    graphView.showSomeNode();
                }
                catch ( Exception e )
                {
                    ErrorMessage.showDialog( "Database problem", e );
                }
                Activator.getDefault().getAliasManager().notifyListners();
            }
        } );
    }
}
