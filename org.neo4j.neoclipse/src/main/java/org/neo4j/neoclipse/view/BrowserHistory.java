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
package org.neo4j.neoclipse.view;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.NotFoundException;
import org.neo4j.neoclipse.Activator;

/**
 * Keep track of browsing history and preserve states.
 * @author Anders Nawroth
 */
public class BrowserHistory
{
    /**
     * A list of browser states.
     */
    private final List<BrowserState> states = new LinkedList<BrowserState>();
    /**
     * Position of last added item.
     */
    private int position = -1;

    /**
     * Class to save one browser state.
     */
    private static class BrowserState
    {
        /**
         * Id of current node in state.
         */
        private final long id;

        /**
         * Create a state.
         * @param node
         *            the starting point of this state
         */
        public BrowserState( final Node node )
        {
            id = node.getId();
        }

        /**
         * Only instantiate with node info.
         */
        protected BrowserState()
        {
            throw new IllegalArgumentException(
                "Must be invoked with a Node as argument." );
        }

        /**
         * Get starting node of this state.
         * @return starting node or null if it doesn't exist any more
         */
        public Node getNode()
        {
            NeoService neoService = Activator.getDefault()
                .getNeoServiceSafely();
            if ( neoService != null )
            {
                try
                {
                    return neoService.getNodeById( id );
                }
                catch ( NotFoundException e )
                {
                    return null;
                }
            }
            return null;
        }
    }

    /**
     * Move backwards in history.
     * @return previous starting point or null
     */
    public Node getPrevious()
    {
        Node node = null;
        while ( node == null && hasPrevious() )
        {
            --position;
            node = fetchPrevious();
        }
        return node;
    }

    /**
     * Move forward in history.
     * @return next starting point or null
     */
    public Node getNext()
    {
        Node node = null;
        while ( node == null && hasNext() )
        {
            node = fetchNext();
            ++position;
        }
        return node;
    }

    /**
     * Get next node.
     * @return next node or null
     */
    private Node fetchNext()
    {
        return getNode( position + 1 );
    }

    /**
     * Get previous node.
     * @return previous node or null
     */
    private Node fetchPrevious()
    {
        return getNode( position );
    }

    /**
     * Get node in list from position.
     * @param pos
     *            position in state list
     * @return node at the position or null
     */
    private Node getNode( final int pos )
    {
        return states.get( pos ).getNode();
    }

    /**
     * Check for existence of previous state.
     * @return true if previous state exist
     */
    public boolean hasPrevious()
    {
        return position > 0 && position <= states.size() - 1;
    }

    /**
     * Check for existence of forward state.
     * @return true if forward state exist
     */
    public boolean hasNext()
    {
        return position > -1 && position + 1 <= states.size() - 1;
    }

    /**
     * Add a new state.
     * @param node
     *            starting point of state
     */
    public void add( final Node node )
    {
        if ( node == null )
        {
            throw new IllegalArgumentException(
                "Node in history can't be null." );
        }
        if ( hasPrevious() && node.equals( fetchPrevious() ) )
        {
            // nothing new to add at this position
            return;
        }
        if ( hasNext() && node.equals( fetchNext() ) )
        {
            position++;
            return;
        }

        BrowserState state = new BrowserState( node );
        // clear rest of list before adding
        position++;
        if ( states.size() > position && position >= 0 )
        {
            ListIterator<BrowserState> iter = states.listIterator( position );
            while ( iter.hasNext() )
            {
                iter.next();
                iter.remove();
            }
            position = states.size();
        }
        states.add( state );
    }
}
