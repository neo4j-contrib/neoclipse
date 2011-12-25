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
package org.neo4j.neoclipse.event;

import java.util.Iterator;

import org.eclipse.core.runtime.ListenerList;

/**
 * Class to handle a list of neoclipse event listeners in a type safe way.
 * Removal of listeners is not available.
 * 
 * @author Anders Nawroth
 */
public class NeoclipseListenerList implements Iterable<NeoclipseEventListener>
{
    private final ListenerList list = new ListenerList( ListenerList.IDENTITY );
    private boolean inhibit = false;

    /**
     * Add an event listener.
     * 
     * @param listener
     */
    public void add( final NeoclipseEventListener listener )
    {
        list.add( listener );
    }

    /**
     * Notify listeners something changed.
     * 
     * @param event
     */
    public void notifyListeners( final NeoclipseEvent event )
    {
        if ( inhibit )
        {
            return;
        }
        for ( NeoclipseEventListener listener : this )
        {
            listener.stateChanged( event );
        }
    }

    public void notifyListeners()
    {
        if ( inhibit )
        {
            return;
        }
        for ( NeoclipseEventListener listener : this )
        {
            listener.stateChanged( null );
        }
    }

    /**
     * Set the inhibit status. True means all notifications are inhibited until
     * the status is flipped back to false.
     * 
     * @param inhibit
     */
    public void setInhibit( final boolean inhibit )
    {
        this.inhibit = inhibit;
    }

    /**
     * Iterate over the listeners.
     */
    @Override
    public Iterator<NeoclipseEventListener> iterator()
    {
        final Object[] listeners = list.getListeners();
        return new Iterator<NeoclipseEventListener>()
        {
            private int pos = 0;

            @Override
            public boolean hasNext()
            {
                return pos < listeners.length;
            }

            @Override
            public NeoclipseEventListener next()
            {
                return (NeoclipseEventListener) listeners[pos++];
            }

            @Override
            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }
}
