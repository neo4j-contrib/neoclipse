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
package org.neo4j.neoclipse.graphdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.preference.IPreferenceStore;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.connection.Alias;
import org.neo4j.neoclipse.connection.ConnectionMode;
import org.neo4j.neoclipse.editor.CypherResultSet;
import org.neo4j.neoclipse.editor.NodeWrapper;
import org.neo4j.neoclipse.preference.Preferences;
import org.neo4j.neoclipse.util.ApplicationUtil;
import org.neo4j.neoclipse.view.UiHelper;
import org.neo4j.rest.graphdb.RestGraphDatabase;
import org.neo4j.rest.graphdb.query.RestCypherQueryEngine;


/**
 * This manager controls the neo4j service.
 * 
 * @author Peter H&auml;nsgen
 * @author Anders Nawroth
 * @author Radhakrishan Kalyan
 */
public class GraphDbServiceManager
{
    private static final String FIND_REFERENCE_NODE_WITH_RELS_STATEMENT = "START n=node(*) MATCH n--() RETURN id(n) as id LIMIT 1";
    private static final String FIND_REFERENCE_NODE_STATEMENT = "START n=node(*) RETURN id(n) as id LIMIT 1";
    private static final String NEOCLIPSE_PACKAGE = "org.neo4j.neoclipse.";
    private static Logger logger = Logger.getLogger( GraphDbServiceManager.class.getName() );
    private Alias currentAlias;

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
            @Override
            public void run()
            {
                if ( lifecycle != null )
                {
                    throw new IllegalStateException(
                            "Can't start new database: There is already a serice running or isn't properly shutdown." );
                }
                logInfo( "trying to start/connect ..." );
                GraphDatabaseService graphDb = null;
                ConnectionMode connectionMode = currentAlias.getConnectionMode();

                switch ( connectionMode )
                {
                case REMOTE:
                {
                    graphDb = new RestGraphDatabase( currentAlias.getUri(), currentAlias.getUserName(),
                            currentAlias.getPassword() );
                    logInfo( "connected to remote neo4j using neo4j rest api." );
                    break;

                }
                case LOCAL:
                {
                    GraphDatabaseBuilder databaseBuilder = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder( currentAlias
                            .getUri() );
                    Boolean allowStoreUpgrade = Boolean.parseBoolean( currentAlias
                            .getConfigurationByKey( GraphDatabaseSettings.allow_store_upgrade.name() ) );
                    graphDb = databaseBuilder.setConfig( GraphDatabaseSettings.read_only, Boolean.toString( isReadOnlyMode() ) )
                            .setConfig( GraphDatabaseSettings.allow_store_upgrade, allowStoreUpgrade.toString() )
                            .newGraphDatabase();
                    logInfo( isReadOnlyMode() ? "connected to embedded read-only neo4j" : "connected to embedded neo4j" );
                    break;
                }
                default:
                {
                    throw new UnsupportedOperationException( "Connection mode is required" );
                }

                }

                lifecycle = new GraphDbLifecycle( graphDb );
                if ( !isReadOnlyMode() )
                {
                    logFine( "starting tx" );
                    tx = graphDb.beginTx();
                }
                fireServiceChangedEvent( GraphDbServiceStatus.STARTED );
            }
        };

        final Runnable STOP = new Runnable()
        {
            @Override
            public void run()
            {
                logInfo( "stopping/disconnecting ..." );
                if ( lifecycle == null )
                {
                    throw new IllegalStateException( "Can not stop the database: there is no running database." );
                }
                fireServiceChangedEvent( GraphDbServiceStatus.STOPPING );
                // TODO give the UI some time to deal with it here?
                try
                {
                    if ( !isReadOnlyMode() )
                    {
                        tx.failure();
                        tx.finish();
                    }
                }
                catch ( Exception e )
                {
                    e.printStackTrace();
                }
                try
                {
                    lifecycle.manualShutdown();
                    logInfo( "stopped/disconnected" );
                }
                catch ( Exception e )
                {
                    throw new RuntimeException( "Can not stop the database. The reason is not known." );
                }
                finally
                {
                    lifecycle = null;
                    fireServiceChangedEvent( GraphDbServiceStatus.STOPPED );
                }
            }
        };

        final Runnable RESTART = new Runnable()
        {
            @Override
            public void run()
            {
                STOP.run();
                START.run();
            }
        };

        final Runnable SHUTDOWN = new Runnable()
        {
            @Override
            public void run()
            {
                if ( lifecycle != null )
                {
                    fireServiceChangedEvent( GraphDbServiceStatus.SHUTTING_DOWN );
                    STOP.run();
                }
            }
        };

        final Runnable COMMIT = new Runnable()
        {
            @Override
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
                if ( !isReadOnlyMode() )
                {
                    tx.finish();
                    tx = lifecycle.graphDb().beginTx();
                    fireServiceChangedEvent( GraphDbServiceStatus.COMMIT );
                }
            }
        };

        final Runnable ROLLBACK = new Runnable()
        {
            @Override
            public void run()
            {
                if ( !isReadOnlyMode() )
                {
                    tx.finish();
                    tx = lifecycle.graphDb().beginTx();
                    fireServiceChangedEvent( GraphDbServiceStatus.ROLLBACK );
                }
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

        @Override
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

        @Override
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

        @Override
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
    private GraphDbServiceMode serviceMode;
    private GraphDbLifecycle lifecycle = null;

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

    private void printTask( final Object task, final String type, final String info )
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

    public <T> Future<T> submitTask( final GraphCallable<T> callable, final String info )
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
     * @param info short description of the task
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

    public void setGraphServiceMode( final GraphDbServiceMode gdbServiceMode )
    {
        serviceMode = gdbServiceMode;
    }

    /**
     * Starts the neo4j service.
     * 
     * @return
     */
    public Future<?> startGraphDbService( Alias alias )
    {
        if ( alias == null )
        {
            throw new IllegalAccessError( "PLease select the database to start." );
        }
        if ( isRunning() )
        {
            throw new IllegalAccessError( "Database is already running." );
        }
        currentAlias = alias;
        Future<?> submitTask = submitTask( tasks().START, "start db" );
        return submitTask;
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
     * Restarts the neo4j service.
     * 
     * @return
     */
    public Future<?> restartGraphDbService() throws Exception
    {
        return submitTask( tasks().RESTART, "restart db" );
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
     * ExecuteCypher query.
     * 
     * @param cypherSql
     * @return CypherResultSet
     * @throws Exception
     */
    public CypherResultSet executeCypher( final String cypherSql ) throws Exception
    {
        return submitTask( new GraphCallable<CypherResultSet>()
        {
            @Override
            public CypherResultSet call( GraphDatabaseService graphDb )
            {
                if ( !isRunning() )
                {
                    throw new RuntimeException( "Please start the graphdb." );
                }
                final String cypherQuery = cypherSql.replace( '\"', '\'' ).replace( '\n', ' ' );
                String message = null;
                Iterator<Map<String, Object>> iterator = null;
                List<String> columns = new ArrayList<String>();
                //
                if ( currentAlias.getConnectionMode() != ConnectionMode.REMOTE )
                {
                    ExecutionEngine engine = new ExecutionEngine( graphDb );
                    ExecutionResult result = engine.execute( cypherQuery );
                    // message = result.toString().substring( result.toString().lastIndexOf( "+" ) + 1 ).trim();
                    columns = result.columns();
                    iterator = result.iterator();
                }
                else if ( currentAlias.getConnectionMode() == ConnectionMode.REMOTE )
                {
                    Iterable<Map<String, Object>> execute = new RestCypherQueryEngine(( (RestGraphDatabase) graphDb ).getRestAPI()).query(cypherQuery,
                            new HashMap<String, Object>() );
                    iterator = execute.iterator();
                }

                final LinkedList<Map<String, Object>> resultList = new LinkedList<Map<String, Object>>();
                while ( iterator.hasNext() )
                {
                    Map<String, Object> resultMap = iterator.next();
                    LinkedHashMap<String, Object> newMap = new LinkedHashMap<String, Object>();
                    Set<Entry<String, Object>> entrySet = resultMap.entrySet();
                    for ( Entry<String, Object> entry : entrySet )
                    {
                        if ( !columns.contains( entry.getKey() ) )
                        {
                            columns.add( entry.getKey() );
                        } 
                        Object objectNode = entry.getValue();
                        if ( objectNode == null )
                        {
                            continue;
                        }

                        Object obj = null;
                        if ( objectNode instanceof Node )
                        {
                            Node node = (Node) objectNode;
                            NodeWrapper oMap = ApplicationUtil.extractToNodeWrapper( node, true );
                            obj = oMap;
                        }
                        else
                        {
                            obj = objectNode;
                        }
                        newMap.put( entry.getKey(), obj );
                    }
                    resultList.add( newMap );
                }
                return new CypherResultSet( resultList, columns, message );
            }

            private List<String> getColumns( String cypherQuery )
            {
                int indexOf = cypherQuery.toLowerCase().indexOf( "return" );
                String[] columns = cypherQuery.substring( indexOf + 7 ).split( "," );
                return Arrays.asList( columns );
            }

        }, "execute cypher query" ).get();
    }

    
    /**
     * getAllNodes
     * 
     * @return List<Map<String, Object>>
     * @throws Exception
     */
    public List<NodeWrapper> getAllNodes() throws Exception
    {
        return submitTask( new GraphCallable<List<NodeWrapper>>()
        {
            @Override
            public List<NodeWrapper> call( GraphDatabaseService graphDb )
            {
                final LinkedList<NodeWrapper> list = new LinkedList<NodeWrapper>();
                Iterable<Node> iterable =  graphDb.getAllNodes();
                for ( Node node : iterable )
                {
                    NodeWrapper nodeWrapper = ApplicationUtil.extractToNodeWrapper( node, true );
                    list.add( nodeWrapper );
                }
                return list;
            }

        }, "get all nodes" ).get();
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
    public void addServiceEventListener( final GraphDbServiceEventListener listener )
    {
        listeners.add( listener );
    }

    /**
     * Unregisters a service listener.
     */
    public void removeServiceEventListener( final GraphDbServiceEventListener listener )
    {
        listeners.remove( listener );
    }

    /**
     * Notifies all registered listeners about the new service status. Actually
     * just queues up the task so running tasks can finish first.
     */
    public void fireServiceChangedEvent( final GraphDbServiceStatus status )
    {
        submitTask( new Runnable()
        {
            @Override
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

    public Alias getCurrentAlias()
    {
        return currentAlias;
    }

    public Node getAnyReferenceNode() throws Exception
    {
        List<Map<String, Object>> result = executeCypher( FIND_REFERENCE_NODE_WITH_RELS_STATEMENT ).getIterator();
        if ( result.isEmpty() ) {
            result = executeCypher( FIND_REFERENCE_NODE_STATEMENT ).getIterator();
        }
        if ( result.isEmpty() ) {
            return null;
        }
        final long id = ((Number) result.get( 0 ).get( "id" )).longValue();
        return getNodeById( id );
    }

    public Node getNodeById( final long id )
    {
        return executeTask( new GraphCallable<Node>()
        {

            @Override
            public Node call( GraphDatabaseService graphDb )
            {
                Transaction tx = graphDb.beginTx();
                Node nodeById = graphDb.getNodeById( id );
                tx.success();
                return nodeById;
            }
        }, "retrieving node with id "+id );
    }

    public boolean isRemote()
    {
        return currentAlias.getConnectionMode() == ConnectionMode.REMOTE;
    }
}
