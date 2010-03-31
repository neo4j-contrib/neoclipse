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
package org.neo4j.neoclipse.graphdb;

import java.io.File;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.service.datalocation.Location;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.EmbeddedReadOnlyGraphDatabase;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.preference.Preferences;
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
    protected GraphDbServiceMode serviceMode;
    // protected GraphDatabaseLifecycle lifecycle; // TODO

    /**
     * Current thread for shutdown
     */
    protected Thread shutdownHook;

    /**
     * The registered service change listeners.
     */
    protected ListenerList listeners;
    private Transaction tx;
    private final IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();

    /**
     * The constructor.
     */
    public GraphDbServiceManager()
    {
        listeners = new ListenerList();
        serviceMode = GraphDbServiceMode.valueOf( preferenceStore.getString( Preferences.CONNECTION_MODE ) );
    }

    public boolean isReadOnlyMode()
    {
        return serviceMode == GraphDbServiceMode.READ_ONLY_EMBEDDED;
    }

    public void setGraphServiceMode( final GraphDbServiceMode gdbServiceMode )
    {
        serviceMode = gdbServiceMode;
    }

    /**
     * Starts the neo4j service.
     */
    public void startGraphDbService() throws Exception
    {
        if ( graphDb == null )
        {
            System.out.println( "trying to start/connect ..." );
            String dbLocation;
            switch ( serviceMode )
            {
            case READ_WRITE_EMBEDDED:
                dbLocation = getDbLocation();
                graphDb = new EmbeddedGraphDatabase( dbLocation );
                System.out.println( "connected to embedded neo4j" );
                break;
            case READ_ONLY_EMBEDDED:
                dbLocation = getDbLocation();
                graphDb = new EmbeddedReadOnlyGraphDatabase( dbLocation );
                System.out.println( "connected to embedded read-only neo4j" );
                break;
            case REMOTE:
                graphDb = new RemoteGraphDatabase( getResourceUri() );
                System.out.println( "connected to remote neo4j" );
                break;
            }
            // :TODO: save thread and remove shutdown on shutdown ...
            registerShutdownHook( graphDb );
            tx = graphDb.beginTx();
            fireServiceChangedEvent( GraphDbServiceStatus.STARTED );
        }
    }

    private void registerShutdownHook(
            final GraphDatabaseService graphDbInstance )
    {
        shutdownHook = new Thread()
        {
            @Override
            public void run()
            {
                if ( graphDbInstance == null )
                {
                    return;
                }
                graphDbInstance.shutdown();
            }
        };
        Runtime.getRuntime().addShutdownHook( shutdownHook );
    }

    private void removeShutdownhook()
    {
        Runtime.getRuntime().removeShutdownHook( shutdownHook );
    }

    // determine the neo4j directory from the preferences
    private String getDbLocation()
    {
        String location = preferenceStore.getString( Preferences.DATABASE_LOCATION );
        if ( ( location == null ) || ( location.trim().length() == 0 ) )
        {
            // if there's really no db dir, create one in the node space
            Location workspace = Platform.getInstanceLocation();
            if ( workspace == null )
            {
                throw new IllegalArgumentException(
                        "The database location is not correctly set." );
            }
            try
            {
                File dbDir = new File( workspace.getURL().toURI().getPath()
                                       + "/neo4j-db" );
                if ( !dbDir.exists() )
                {
                    if ( !dbDir.mkdir() )
                    {
                        throw new IllegalArgumentException(
                                "Could not create a database directory." );
                    }
                    System.out.println( "created: " + dbDir.getAbsolutePath() );
                }
                location = dbDir.getAbsolutePath();
                preferenceStore.setValue( Preferences.DATABASE_LOCATION,
                        location );
            }
            catch ( URISyntaxException e )
            {
                e.printStackTrace();
                throw new IllegalArgumentException(
                        "The database location is not correctly set." );
            }
        }
        File dir = new File( location );
        if ( !dir.exists() )
        {
            throw new IllegalArgumentException(
                    "The database location does not exist." );
        }
        if ( !dir.isDirectory() )
        {
            throw new IllegalArgumentException(
                    "The database location is not a directory." );
        }
        if ( !dir.canWrite() )
        {
            throw new IllegalAccessError(
                    "Writes are not allowed to the database location." );
        }
        System.out.println( "using location: " + location );
        return location;
    }

    private String getResourceUri()
    {
        String resourceUri = preferenceStore.getString( Preferences.DATABASE_RESOURCE_URI );
        if ( resourceUri == null || resourceUri.trim().length() == 0 )
        {
            throw new IllegalArgumentException(
                    "There is no resource URI defined." );
        }
        return resourceUri;
    }

    /**
     * Returns the graphdb service or null, if it isn't started.
     */
    public GraphDatabaseService getGraphDbService()
    {
        return graphDb;
    }

    /**
     * Stops the neo service.
     */
    public void stopGraphDbService()
    {
        if ( graphDb != null )
        {
            System.out.println( "trying to stop/disconnect ..." );
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
        removeShutdownhook();
        System.out.println( "stopped/disconnected" );
    }

    /**
     * Commit transaction.
     */
    public void commit()
    {
        if ( serviceMode == GraphDbServiceMode.READ_WRITE_EMBEDDED )
        {
            tx.success();
        }
        else
        {
            System.out.println( "Committing while not in write mode" );
        }
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
