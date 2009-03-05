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
package org.neo4j.neoclipse.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.ISearchResultListener;
import org.neo4j.api.core.Node;
import org.neo4j.neoclipse.NeoIcons;

/**
 * This class represents the result of a Neo search.
 * @author Peter H&auml;nsgen
 */
public class NeoSearchResult implements ISearchResult
{
    /**
     * The query to which this result belongs.
     */
    private final NeoSearchQuery query;

    /**
     * The found matches.
     */
    private Iterable<Node> matches;

    /**
     * The observers.
     */
    private final List<ISearchResultListener> listeners;

    /**
     * The constructor.
     */
    public NeoSearchResult( final NeoSearchQuery query )
    {
        this.query = query;

        // we have to initialize an empty list, as the result will already be
        // shown before
        // the search is actually started
        this.matches = Collections.emptyList();

        listeners = new ArrayList<ISearchResultListener>();
    }

    /**
     * Returns the found nodes for the search expression.
     */
    public Iterable<Node> getMatches()
    {
        return matches;
    }

    /**
     * Sets the matches. The registered listeners will be notified.
     */
    public void setMatches( final Iterable<Node> matches )
    {
        this.matches = matches;

        fireSearchResultEvent();
    }

    /**
     * Returns a neo image descriptor.
     */
    public ImageDescriptor getImageDescriptor()
    {
        return NeoIcons.NEO.descriptor();
    }

    /**
     * Returns a label for the search result, which will be shown in the search
     * history.
     */
    public String getLabel()
    {
        return "Neo4j - '" + query.getExpression() + "'";
    }

    /**
     * Returns the query that produced this result.
     */
    public ISearchQuery getQuery()
    {
        return query;
    }

    /**
     * Returns the tooltip.
     */
    public String getTooltip()
    {
        return null;
    }

    /**
     * Adds a listener.
     */
    public void addListener( final ISearchResultListener listener )
    {
        if ( !listeners.contains( listener ) )
        {
            listeners.add( listener );
        }
    }

    /**
     * Removes a listener.
     */
    public void removeListener( final ISearchResultListener listener )
    {
        listeners.remove( listener );
    }

    /**
     * Notifies the registered listeners about changes in the search result.
     */
    protected void fireSearchResultEvent()
    {
        final NeoSearchResultEvent e = new NeoSearchResultEvent( this );

        for ( int i = 0; i < listeners.size(); i++ )
        {
            final ISearchResultListener l = listeners.get( i );

            ISafeRunnable job = new ISafeRunnable()
            {
                public void handleException( Throwable exception )
                {
                    // already being logged in SafeRunner#run()
                }

                public void run() throws Exception
                {
                    l.searchResultChanged( e );
                }
            };

            SafeRunner.run( job );
        }
    }
}
