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
package org.neo4j.neoclipse.action.connect;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.action.AbstractGraphAction;
import org.neo4j.neoclipse.action.Actions;
import org.neo4j.neoclipse.graphdb.GraphDbServiceManager;
import org.neo4j.neoclipse.graphdb.GraphRunnable;
import org.neo4j.neoclipse.view.ErrorMessage;
import org.neo4j.neoclipse.view.NeoGraphViewPart;

/**
 * Action to sync the database.
 * 
 * @author Anders Nawroth
 */
public class SyncAction extends AbstractGraphAction
{
    public SyncAction( final NeoGraphViewPart neoGraphViewPart )
    {
        super( Actions.SYNC, neoGraphViewPart );
        setEnabled( false );
    }

    @Override
    public void run()
    {
        Node currentNode = graphView.getCurrentNode();
        final long nodeId = currentNode == null ? -1 : currentNode.getId();
        graphView.cleanPropertySheetBeforeShutdown();
        final GraphDbServiceManager gsm = Activator.getDefault()
                .getGraphDbServiceManager();
        try
        {
            gsm.restartGraphDbService()
                    .get();
            gsm.executeTask( new GraphRunnable()
            {
                @Override
                public void run( final GraphDatabaseService graphDb )
                {
                    if ( nodeId == -1 )
                    {
                        return;
                    }
                    try
                    {
                        final Node node = graphDb.getNodeById( nodeId );
                        gsm.submitDisplayTask( new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                graphView.setInput( node );
                            }
                        }, "Set the current node" );
                    }
                    catch ( NotFoundException nfe )
                    {
                        gsm.submitDisplayTask( new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                graphView.showSomeNode();
                            }
                        }, "Show some node" );
                        ErrorMessage.showDialog(
                                "Could not show the same node again", nfe );
                    }
                }
            }, "Get the current node" );
        }
        catch ( Exception e )
        {
            ErrorMessage.showDialog( "Database problem", e );
        }
    }
}
