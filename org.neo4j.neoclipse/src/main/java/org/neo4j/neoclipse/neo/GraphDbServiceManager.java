/*
 * Licensed to "Neo Technology," Network Engine for Objects in Lund AB
 * (http://neotechnology.com) under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership. Neo Technology licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License
 * at (http://www.apache.org/licenses/LICENSE-2.0). Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.neo4j.neoclipse.neo;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.preference.IPreferenceStore;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.preference.Neo4jPreferences;
import org.neo4j.remote.RemoteGraphDatabase;

/**
 * This manager controls the neo4j service.
 * 
 * @author Peter H&auml;nsgen
 * @author Anders Nawroth
 */
public class GraphDbServiceManager
{
    /**
     * The service instance.
     */
    protected GraphDatabaseService graphDb;
    /**
     * The registered service change listeners.
     */
    protected ListenerList listeners;
    private Transaction tx;

    /**
     * The constructor.
     */
    public GraphDbServiceManager()
    {
        listeners = new ListenerList();
    }

    /**
     * Starts the neo4j service.
     */
    public void startGraphDbService() throws RuntimeException
    {
        System.out.println( "checking service ..." );
        if ( graphDb == null )
        {
            System.out.println( "starting neo4j" );
            final IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
            // try the resource URI first
            String resourceUri = preferenceStore.getString( Neo4jPreferences.DATABASE_RESOURCE_URI );
            if ( ( resourceUri != null ) && ( resourceUri.trim().length() != 0 ) )
            {
                // let's try the resource URI
                try
                {
                    System.out.println( "trying remote graphdb" );
                    graphDb = new RemoteGraphDatabase( resourceUri );
                    System.out.println( "connected to remote neo4j" );
                }
                catch ( Exception e )
                {
                    e.printStackTrace();
                }
            }
            else
            {
                // determine the neo4j directory from the preferences
                String location = preferenceStore.getString( Neo4jPreferences.DATABASE_LOCATION );
                // TODO actually check if the directoryt STILL exists
                if ( ( location == null ) || ( location.trim().length() == 0 ) )
                {
                    return;
                }
                // seems to be a valid directory, try starting neo4j
                graphDb = new EmbeddedGraphDatabase( location );
                System.out.println( "connected to embedded neo4j" );
            }
            tx = graphDb.beginTx();
            // notify listeners
            fireServiceChangedEvent( GraphDbServiceStatus.STARTED );
        }
    }

    /**
     * Returns the graphdb service or null, if it could not be started (due to
     * configuration problems).
     */
    public GraphDatabaseService getGraphDbService() throws RuntimeException
    {
        if ( graphDb == null )
        {
            startGraphDbService();
        }
        return graphDb;
    }

    /**
     * Stops the neo service.
     */
    public void stopGraphDbService()
    {
        if ( graphDb != null )
        {
            try
            {
                tx.failure();
                tx.finish();
            }
            catch ( Exception e )
            {
                e.printStackTrace();
            }
            try
            {
                graphDb.shutdown();
                // notify listeners
                fireServiceChangedEvent( GraphDbServiceStatus.STOPPED );
            }
            finally
            {
                graphDb = null;
            }
        }
    }

    /**
     * Commit transaction.
     */
    public void commit()
    {
        tx.success();
        tx.finish();
        tx = graphDb.beginTx();
        fireServiceChangedEvent( GraphDbServiceStatus.COMMIT );
    }

    /**
     * Rollback transaction.
     */
    public void rollback()
    {
        tx.failure();
        tx.finish();
        tx = graphDb.beginTx();
        fireServiceChangedEvent( GraphDbServiceStatus.ROLLBACK );
    }

    /**
     * Registers a service listener.
     */
    public void addServiceEventListener(
            final GraphDbServiceEventListener listener )
    {
        listeners.add( listener );
    }

    /**
     * Unregisters a service listener.
     */
    public void removeServiceEventListener(
            final GraphDbServiceEventListener listener )
    {
        listeners.remove( listener );
    }

    /**
     * Notifies all registered listeners about the new service status.
     */
    protected void fireServiceChangedEvent( final GraphDbServiceStatus status )
    {
        Object[] changeListeners = listeners.getListeners();
        if ( changeListeners.length > 0 )
        {
            final GraphDbServiceEvent e = new GraphDbServiceEvent( this, status );
            for ( Object changeListener : changeListeners )
            {
                final GraphDbServiceEventListener l = (GraphDbServiceEventListener) changeListener;
                ISafeRunnable job = new ISafeRunnable()
                {
                    public void handleException( final Throwable exception )
                    {
                        // do nothing
                    }

                    public void run() throws RuntimeException
                    {
                        l.serviceChanged( e );
                    }
                };
                SafeRunner.run( job );
            }
        }
    }
}
