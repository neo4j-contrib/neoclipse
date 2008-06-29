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
import org.neo4j.api.core.EmbeddedNeo;
import org.neo4j.api.core.NeoService;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.preference.NeoPreferences;

/**
 * This manager controls the neo service.
 * @author Peter H&auml;nsgen
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
    public void startNeoService()
    {
        if ( neo == null )
        {
            // determine the neo directory from the preferences
            String location = Activator.getDefault().getPreferenceStore()
                .getString( NeoPreferences.DATABASE_LOCATION );
            if ( (location != null) && (location.trim().length() > 0) )
            {
                // seems to be a valid directory
                try
                {
                    neo = new EmbeddedNeo( location );
                }
                catch ( Exception e )
                {
                    e.printStackTrace(); // TODO real solution on exceptions
                    System.exit( 1 );
                }
                // notify listeners
                fireServiceChangedEvent( NeoServiceStatus.STARTED );
            }
        }
    }

    /**
     * Returns the neo service or null, if it could not be started (due to
     * configuration problems).
     */
    public NeoService getNeoService()
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
     * Registers a service listener.
     */
    public void addServiceEventListener( NeoServiceEventListener listener )
    {
        listeners.add( listener );
    }

    /**
     * Unregisters a service listener.
     */
    public void removeServiceEventListener( NeoServiceEventListener listener )
    {
        listeners.remove( listener );
    }

    /**
     * Notifies all registered listeners about the new service status.
     */
    protected void fireServiceChangedEvent( NeoServiceStatus status )
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
                    public void handleException( Throwable exception )
                    {
                    }

                    public void run() throws Exception
                    {
                        l.serviceChanged( e );
                    }
                };
                SafeRunner.run( job );
            }
        }
    }
}
