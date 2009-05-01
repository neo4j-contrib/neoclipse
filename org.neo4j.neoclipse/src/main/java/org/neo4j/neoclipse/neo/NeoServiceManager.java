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
import org.neo4j.api.core.EmbeddedNeo;
import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.Transaction;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.preference.NeoPreferences;
import org.neo4j.remote.RemoteNeo;

/**
 * This manager controls the neo service.
 * @author Peter H&auml;nsgen
 * @author Anders Nawroth
 */
public class NeoServiceManager
{
    /**
     * The service instance.
     */
    protected NeoService neo;
    /**
     * The registered service change listeners.
     */
    protected ListenerList listeners;
    private Transaction tx;

    /**
     * The constructor.
     */
    public NeoServiceManager()
    {
        listeners = new ListenerList();
    }

    /**
     * Starts the neo service.
     */
    public void startNeoService() throws RuntimeException
    {
        System.out.println( "checking service ..." );
        if ( neo == null )
        {
            System.out.println( "starting neo" );
            final IPreferenceStore preferenceStore = Activator.getDefault()
                .getPreferenceStore();
            // try the resource URI first
            String resourceUri = preferenceStore
                .getString( NeoPreferences.DATABASE_RESOURCE_URI );
            if ( (resourceUri != null) && (resourceUri.trim().length() != 0) )
            {
                // let's try the resource URI
                try
                {
                    System.out.println( "trying remote neo" );
                    neo = new RemoteNeo( resourceUri );
                    System.out.println( "connected to remote neo" );
                }
                catch ( Exception e )
                {
                    e.printStackTrace();
                }
            }
            else
            {
                // determine the neo directory from the preferences
                String location = preferenceStore
                    .getString( NeoPreferences.DATABASE_LOCATION );
                if ( (location == null) || (location.trim().length() == 0) )
                {
                    return;
                }
                // seems to be a valid directory, try starting neo
                neo = new EmbeddedNeo( location );
                System.out.println( "connected to embedded neo" );
            }
            tx = neo.beginTx();
            // notify listeners
            fireServiceChangedEvent( NeoServiceStatus.STARTED );
        }
    }

    /**
     * Returns the neo service or null, if it could not be started (due to
     * configuration problems).
     */
    public NeoService getNeoService() throws RuntimeException
    {
        if ( neo == null )
        {
            startNeoService();
        }
        return neo;
    }

    /**
     * Stops the neo service.
     */
    public void stopNeoService()
    {
        if ( neo != null )
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
                neo.shutdown();
                // notify listeners
                fireServiceChangedEvent( NeoServiceStatus.STOPPED );
            }
            finally
            {
                neo = null;
            }
        }
    }

    /**
     * Commit Neo transaction.
     */
    public void commit()
    {
        tx.success();
        tx.finish();
        tx = neo.beginTx();
        fireServiceChangedEvent( NeoServiceStatus.COMMIT );
    }

    /**
     * Rollback neo transaction.
     */
    public void rollback()
    {
        tx.failure();
        tx.finish();
        tx = neo.beginTx();
        fireServiceChangedEvent( NeoServiceStatus.ROLLBACK );
    }

    /**
     * Registers a service listener.
     */
    public void addServiceEventListener( final NeoServiceEventListener listener )
    {
        listeners.add( listener );
    }

    /**
     * Unregisters a service listener.
     */
    public void removeServiceEventListener(
        final NeoServiceEventListener listener )
    {
        listeners.remove( listener );
    }

    /**
     * Notifies all registered listeners about the new service status.
     */
    protected void fireServiceChangedEvent( final NeoServiceStatus status )
    {
        Object[] changeListeners = listeners.getListeners();
        if ( changeListeners.length > 0 )
        {
            final NeoServiceEvent e = new NeoServiceEvent( this, status );
            for ( int i = 0; i < changeListeners.length; i++ )
            {
                final NeoServiceEventListener l = (NeoServiceEventListener) changeListeners[i];
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
