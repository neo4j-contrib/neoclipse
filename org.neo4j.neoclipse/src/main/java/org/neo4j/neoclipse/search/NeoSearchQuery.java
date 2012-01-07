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
package org.neo4j.neoclipse.search;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.graphdb.GraphCallable;
import org.neo4j.neoclipse.graphdb.GraphDbServiceManager;
import org.neo4j.neoclipse.view.ErrorMessage;
import org.neo4j.neoclipse.view.UiHelper;

/**
 * This class represents a search query for Neo objects.
 * 
 * @author Peter H&auml;nsgen
 * @author Anders Nawroth
 */
public class NeoSearchQuery implements ISearchQuery
{
    /**
     * The found matches.
     */
    private final NeoSearchResult result;

    private final IndexSearch search;

    /**
     * The constructor.
     */
    public NeoSearchQuery( final IndexSearch search )
    {
        this.search = search;
        // initialize an empty result
        result = new NeoSearchResult( this );
    }

    /**
     * Returns a String form of the search expression.
     */
    public String getExpression()
    {
        return search.getValueOrQuery();
    }

    /**
     * Returns true.
     */
    @Override
    public boolean canRerun()
    {
        return true;
    }

    /**
     * Returns true.
     */
    @Override
    public boolean canRunInBackground()
    {
        return true;
    }

    /**
     * Returns a label.
     */
    @Override
    public String getLabel()
    {
        return "Neo4j Search";
    }

    /**
     * Returns the search result.
     */
    @Override
    public ISearchResult getSearchResult()
    {
        return result;
    }

    /**
     * Executes the search.
     */
    @Override
    public IStatus run( final IProgressMonitor monitor ) throws OperationCanceledException
    {
        final GraphDbServiceManager gsm = Activator.getDefault().getGraphDbServiceManager();
        if ( !gsm.isRunning() )
        {
            return new Status( IStatus.ERROR, Activator.PLUGIN_ID, "There is no active Neo4j service." );
        }

        try
        {
            gsm.submitTask( new GraphCallable<Boolean>()
            {
                @Override
                public Boolean call( final GraphDatabaseService graphDb )
                {
                    final Iterable<PropertyContainer> matches = getMatchingNodesFromIndices( monitor, graphDb );
                    UiHelper.asyncExec( new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            result.setMatches( matches );
                        }
                    } );
                    return true;
                }
            }, "run search" ).get();
            if ( monitor.isCanceled() )
            {
                return new Status( IStatus.CANCEL, Activator.PLUGIN_ID, "Cancelled." );
            }
            else
            {
                return new Status( IStatus.OK, Activator.PLUGIN_ID, "OK" );
            }
        }
        catch ( Exception e )
        {
            String message = ErrorMessage.getErrorMessage( e );
            if ( message.indexOf( "org.apache.lucene.index.CorruptIndexException: Unknown format version" ) != -1 )
            {
                ErrorMessage.showDialog( "Search error", "The index can't be read as the Neo4j database "
                                                         + "isn't compatible with this version of Neoclipse." );
            }
            else
            {
                ErrorMessage.showDialog( "Search error", e );
            }
        }
        return null;
    }

    private Iterable<PropertyContainer> getMatchingNodesFromIndices( final IProgressMonitor monitor,
            final GraphDatabaseService graphDb )
    {
        List<PropertyContainer> matches = new LinkedList<PropertyContainer>();
        IndexManager indexManager = graphDb.index();
        for ( String indexName : search.getNodeIndexNames() )
        {
            if ( !indexManager.existsForNodes( indexName ) )
            {
                continue;
            }
            Index<Node> nodeIndex = indexManager.forNodes( indexName );
            Iterable<Node> hits;
            switch ( search.getMode() )
            {
            case EXACT_MATCH:
                hits = nodeIndex.get( search.getKey(), search.getValueOrQuery() );
                break;
            case QUERY:
                String key = search.getKey();
                hits = nodeIndex.query( key, search.getValueOrQuery() );
                break;
            default:
                hits = null;
            }
            for ( Node hit : hits )
            {
                matches.add( hit );
            }
        }
        for ( String indexName : search.getRelationshipIndexNames() )
        {
            if ( !indexManager.existsForRelationships( indexName ) )
            {
                continue;
            }
            Index<Relationship> relIndex = indexManager.forRelationships( indexName );
            IndexHits<Relationship> hits;
            switch ( search.getMode() )
            {
            case EXACT_MATCH:
                hits = relIndex.get( search.getKey(), search.getValueOrQuery() );
                break;
            case QUERY:
                hits = relIndex.query( search.getKey(), search.getValueOrQuery() );
                break;
            default:
                hits = null;
            }
            for ( Relationship hit : hits )
            {
                matches.add( hit );
            }
        }
        return matches;
    }
}
