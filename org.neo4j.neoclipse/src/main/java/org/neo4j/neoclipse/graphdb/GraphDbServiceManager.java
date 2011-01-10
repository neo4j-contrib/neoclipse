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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.service.datalocation.Location;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.EmbeddedReadOnlyGraphDatabase;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.preference.Preferences;
import org.neo4j.neoclipse.view.ErrorMessage;
import org.neo4j.neoclipse.view.UiHelper;
import org.neo4j.remote.RemoteGraphDatabase;
import org.neo4j.util.GraphDatabaseLifecycle;

/**
 * This manager controls the neo4j service.
 * 
 * @author Peter H&auml;nsgen
 * @author Anders Nawroth
 */
public class GraphDbServiceManager
{
    private static final String NEOCLIPSE_PACKAGE = "org.neo4j.neoclipse.";
    private static Logger logger = Logger.getLogger( GraphDbServiceManager.class.getName() );

    static
    {
        logger.setUseParentHandlers( false );
        logger.setLevel( Level.INFO );
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel( Level.INFO );
        logger.addHandler( handler );
    }

    private class Tasks
    {
        final Runnable START = new Runnable()
        {
            public void run()
            {
                if ( lifecycle != null )
                {
                    throw new IllegalStateException(
                            "Can't start new database: the old one isn't shutdown properly." );
                }
                logInfo( "trying to start/connect ..." );
                String dbLocation;
                GraphDatabaseService graphDb = null;
                switch ( serviceMode )
                {
                case READ_WRITE_EMBEDDED:
                    dbLocation = getDbLocation();
                    graphDb = new EmbeddedGraphDatabase( dbLocation );
                    logInfo( "connected to embedded neo4j" );
                    break;
                case READ_ONLY_EMBEDDED:
                    dbLocation = getDbLocation();
                    graphDb = new EmbeddedReadOnlyGraphDatabase( dbLocation );
                    logInfo( "connected to embedded read-only neo4j" );
                    break;
                case REMOTE:
                    try
                    {
                        graphDb = new RemoteGraphDatabase( getResourceUri() );
                        logInfo( "connected to remote neo4j" );
                    }
                    catch ( URISyntaxException e )
                    {
                        ErrorMessage.showDialog( "URI syntax error", e );
                    }
                    break;
                }
                lifecycle = new GraphDatabaseLifecycle( graphDb );
                logFine( "starting tx" );
                tx = graphDb.beginTx();
                fireServiceChangedEvent( GraphDbServiceStatus.STARTED );
            }
        };

        final Runnable STOP = new Runnable()
        {
            public void run()
            {
                logInfo( "stopping/disconnecting ..." );
                if ( lifecycle == null )
                {
                    throw new IllegalStateException(
                            "Can't stop the database: there is no running database." );
                }
                fireServiceChangedEvent( GraphDbServiceStatus.STOPPING );
                // TODO give the UI some time to deal with it here?
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
                    lifecycle.manualShutdown();
                }
                finally
                {
                    lifecycle = null;
                    fireServiceChangedEvent( GraphDbServiceStatus.STOPPED );
                }
                logInfo( "stopped/disconnected" );
            }
        };

        final Runnable SHUTDOWN = new Runnable()
        {
            public void run()
            {
                if ( lifecycle != null )
                {
                    STOP.run();
                }
            }
        };

        final Runnable COMMIT = new Runnable()
        {
            public void run()
            {
                if ( serviceMode == GraphDbServiceMode.READ_WRITE_EMBEDDED )
                {
                    tx.success();
                }
                else
                {
                    logFine( "Committing while not in write mode." );
                }
                tx.finish();
                tx = lifecycle.graphDb().beginTx();
                fireServiceChangedEvent( GraphDbServiceStatus.COMMIT );
            }
        };

        final Runnable ROLLBACK = new Runnable()
        {
            public void run()
            {
                tx.finish();
                tx = lifecycle.graphDb().beginTx();
                fireServiceChangedEvent( GraphDbServiceStatus.ROLLBACK );
            }
        };
    }

    private class TaskWrapper<T> implements Callable<T>
    {
        private final GraphCallable<T> callable;

        public TaskWrapper( final GraphCallable<T> callable )
        {
            this.callable = callable;
        }

        public T call() throws Exception
        {
            GraphDatabaseService graphDb = null;
            if ( lifecycle != null )
            {
                graphDb = lifecycle.graphDb();
            }
            return callable.call( graphDb );
        }
    }

    private class RunnableWrapper implements Runnable
    {
        private final GraphRunnable runnable;
        private final String name;

        public RunnableWrapper( final GraphRunnable runnable, final String name )
        {
            this.runnable = runnable;
            this.name = name;
        }

        public void run()
        {
            GraphDatabaseService graphDb = null;
            if ( lifecycle != null )
            {
                graphDb = lifecycle.graphDb();
            }
            logFine( "running: " + name );
            runnable.run( graphDb );
            logFine( "finished running: " + name );
        }
    }

    private class DisplayRunnable implements Runnable
    {
        private final Runnable runnable;
        private final String name;

        public DisplayRunnable( final Runnable runnable, final String name )
        {
            this.runnable = runnable;
            this.name = name;
        }

        public void run()
        {
            logFine( "sending display task: " + name );
            UiHelper.asyncExec( runnable );
        }
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Tasks tasks = new Tasks();

    /**
     * The service instance.
     */
    protected GraphDbServiceMode serviceMode;
    protected GraphDatabaseLifecycle lifecycle = null;

    /**
     * The registered service change listeners.
     */
    private final ListenerList listeners = new ListenerList();
    private Transaction tx;
    private final IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();

    /**
     * The constructor.
     */
    public GraphDbServiceManager()
    {
        serviceMode = GraphDbServiceMode.valueOf( preferenceStore.getString( Preferences.CONNECTION_MODE ) );
        logInfo( "Starting " + this.getClass().getSimpleName() );
    }

    private void logFine( final String message )
    {
        logger.fine( message );
    }

    private void logInfo( final String message )
    {
        logger.info( message );
    }

    private Tasks tasks()
    {
        return tasks;
    }

    private void printTask( final Object task, final String type,
            final String info )
    {
        String name = task.getClass().getName();
        if ( name.startsWith( NEOCLIPSE_PACKAGE ) )
        {
            name = name.substring( NEOCLIPSE_PACKAGE.length() );
        }
        logFine( type + " -> " + name + ":\n" + info );
    }

    public <T> Future<T> submitTask( final Callable<T> task, final String info )
    {
        printTask( task, "C", info );
        return executor.submit( task );
    }

    public <T> Future<T> submitTask( final GraphCallable<T> callable,
            final String info )
    {
        printTask( callable, "GC", info );
        TaskWrapper<T> wrapped = new TaskWrapper<T>( callable );
        return executor.submit( wrapped );
    }

    public Future<?> submitTask( final Runnable runnable, final String info )
    {
        printTask( runnable, "R", info );
        return executor.submit( runnable );
    }

    public Future<?> submitTask( final GraphRunnable runnable, final String info )
    {
        printTask( runnable, "GR", info );
        RunnableWrapper wrapped = new RunnableWrapper( runnable, info );
        return executor.submit( wrapped );
    }

    /**
     * Submit a task that should be performed by the UI thread after the tasks
     * in the execution queue have executed.
     * 
     * @param runnable runnable to execute
     * @param info short discription of the task
     */
    public void submitDisplayTask( final Runnable runnable, final String info )
    {
        DisplayRunnable wrapped = new DisplayRunnable( runnable, info );
        executor.submit( wrapped );
    }

    public void executeTask( final GraphRunnable runnable, final String info )
    {
        logFine( "starting: " + info );
        runnable.run( lifecycle.graphDb() );
        logFine( "finishing: " + info );
    }

    public <T> T executeTask( final GraphCallable<T> callable, final String info )
    {
        logFine( "calling: " + info );
        return callable.call( lifecycle.graphDb() );
    }

    public void stopExecutingTasks()
    {
        if ( !executor.isShutdown() )
        {
            executor.shutdown();
        }
    }

    public boolean isRunning()
    {
        return lifecycle != null && lifecycle.graphDb() != null;
    }

    public boolean isReadOnlyMode()
    {
        return serviceMode == GraphDbServiceMode.READ_ONLY_EMBEDDED;
    }

    public boolean isLocal()
    {
        return serviceMode == GraphDbServiceMode.READ_WRITE_EMBEDDED
               || serviceMode == GraphDbServiceMode.READ_ONLY_EMBEDDED;
    }

    public void setGraphServiceMode( final GraphDbServiceMode gdbServiceMode )
    {
        serviceMode = gdbServiceMode;
    }

    /**
     * Starts the neo4j service.
     * 
     * @return
     */
    public Future<?> startGraphDbService() throws Exception
    {
        return submitTask( tasks().START, "start db" );
    }

    /**
     * Stops the neo4j service.
     * 
     * @return
     */
    public Future<?> stopGraphDbService()
    {
        return submitTask( tasks().STOP, "stop db" );
    }

    /**
     * Shuts down the Neo4j service if it's running.
     * 
     * @return
     */
    public Future<?> shutdownGraphDbService()
    {
        return submitTask( tasks().SHUTDOWN, "shutdown db" );
    }

    /**
     * Commit transaction.
     * 
     * @return
     */
    public Future<?> commit()
    {
        return submitTask( tasks().COMMIT, "commit" );
    }

    /**
     * Roll back transaction.
     * 
     * @return
     */
    public Future<?> rollback()
    {
        return submitTask( tasks().ROLLBACK, "rollback" );
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

    // determine the neo4j directory from the preferences
    private String getDbLocation()
    {
        String location = preferenceStore.getString( Preferences.DATABASE_LOCATION );
        if ( ( location == null ) || ( location.trim().length() == 0 ) )
        {
            // if there's really no db dir, create one in the workspace
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
                    logInfo( "created: " + dbDir.getAbsolutePath() );
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
        logFine( "using location: " + location );
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
     * Notifies all registered listeners about the new service status. Actually
     * just queues up the task so running tasks can finish first.
     */
    protected void fireServiceChangedEvent( final GraphDbServiceStatus status )
    {
        submitTask( new Runnable()
        {
            public void run()
            {
                fireTheServiceChangedEvent( status );
            }
        }, "fire changed event" );
    }

    private void fireTheServiceChangedEvent( final GraphDbServiceStatus status )
    {
        Object[] changeListeners = listeners.getListeners();
        if ( changeListeners.length > 0 )
        {
            final GraphDbServiceEvent e = new GraphDbServiceEvent( this, status );
            for ( Object changeListener : changeListeners )
            {
                final GraphDbServiceEventListener l = (GraphDbServiceEventListener) changeListener;
                l.serviceChanged( e );
            }
        }
    }
}
