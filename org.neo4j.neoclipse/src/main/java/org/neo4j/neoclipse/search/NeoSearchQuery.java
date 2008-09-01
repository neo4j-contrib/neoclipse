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
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.neo4j.api.core.Direction;
import org.neo4j.api.core.EmbeddedNeo;
import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.RelationshipType;
import org.neo4j.api.core.ReturnableEvaluator;
import org.neo4j.api.core.StopEvaluator;
import org.neo4j.api.core.Transaction;
import org.neo4j.api.core.TraversalPosition;
import org.neo4j.api.core.Traverser.Order;
import org.neo4j.neoclipse.Activator;

/**
 * This class represents a search query for Neo objects.
 * @author Peter H&auml;nsgen
 */
public class NeoSearchQuery implements ISearchQuery
{
    /**
     * The search expression.
     */
    private NeoSearchExpression expression;

    /**
     * The found matches.
     */
    private NeoSearchResult result;

    private NeoService neoService;

    /**
     * The constructor.
     */
    public NeoSearchQuery( NeoSearchExpression expression )
    {
        this.expression = expression;

        // initialize an empty result
        result = new NeoSearchResult( this );
    }

    /**
     * Returns a String form of the search expression.
     */
    public String getExpression()
    {
        return expression.getExpression();
    }

    /**
     * Returns true.
     */
    public boolean canRerun()
    {
        return true;
    }

    /**
     * Returns true.
     */
    public boolean canRunInBackground()
    {
        return true;
    }

    /**
     * Returns a label.
     */
    public String getLabel()
    {
        return "Neo4j Search";
    }

    /**
     * Returns the search result.
     */
    public ISearchResult getSearchResult()
    {
        return result;
    }

    /**
     * Executes the search.
     */
    public IStatus run( IProgressMonitor monitor )
        throws OperationCanceledException
    {
        neoService = Activator.getDefault().getNeoServiceManager()
            .getNeoService();
        if ( neoService == null )
        {
            return new Status( IStatus.ERROR, Activator.PLUGIN_ID,
                "There is no active Neo4j service." );
        }

        Transaction txn = neoService.beginTx();
        try
        {
            // TODO here we should do some real search using Neo's index service
            // for now simply navigate along the graph
            Node root = neoService.getReferenceNode();

            result.setMatches( getMatchingNodes( root, monitor ) );

            txn.success();

            if ( monitor.isCanceled() )
            {
                return new Status( IStatus.CANCEL, Activator.PLUGIN_ID,
                    "Cancelled." );
            }
            else
            {
                return new Status( IStatus.OK, Activator.PLUGIN_ID, "OK" );
            }
        }
        finally
        {
            txn.finish();
        }
    }

    /**
     * Finds all nodes matching the search criteria.
     */
    @SuppressWarnings( "deprecation" )
    protected Iterable<Node> getMatchingNodes( Node node,
        IProgressMonitor monitor )
    {
        List<Object> relDirList = new ArrayList<Object>();
        for ( RelationshipType relType : ((EmbeddedNeo) neoService)
            .getRelationshipTypes() )
        {
            relDirList.add( relType );
            relDirList.add( Direction.BOTH );
        }

        return node.traverse( Order.DEPTH_FIRST, StopEvaluator.END_OF_NETWORK,
            new ReturnableEvaluator()
            {
                public boolean isReturnableNode( TraversalPosition currentPos )
                {
                    Node currentNode = currentPos.currentNode();
                    // for completeness, also check the id of the node
                    if ( expression.matches( currentNode.getId() ) )
                    {
                        return true;
                    }
                    else
                    {
                        // find at least one property whose value matches the
                        // given
                        // expression
                        for ( Object value : currentNode.getPropertyValues() )
                        {
                            if ( expression.matches( value ) )
                            {
                                return true;
                            }
                        }
                    }
                    return false;
                }
            }, relDirList.toArray() );
    }

    /**
     * Checks if a node matches the search criteria and visits all connected
     * nodes.
     */
    protected void checkNode( Node node, Set<Node> visitedNodes,
        List<Node> matches, IProgressMonitor monitor )
    {
        if (monitor.isCanceled())
        {
            return;
        }
        
        if (!visitedNodes.add(node))
        {
            // we have already been here
            return;
        }

        // for completeness, also check the id of the node
        if (expression.matches(node.getId()))
        {
            matches.add(node);
        }
        else
        {
            // find at least one property whose value matches the given expression
            for (Object value : node.getPropertyValues())
            {
                if (expression.matches(value))
                {
                    matches.add(node);
                    break;
                }            
            }
        }        

        // recursively follow all connections
        for (Relationship r : node.getRelationships(Direction.BOTH))
        {
            Node end = r.getOtherNode( node );

            checkNode(end, visitedNodes, matches, monitor);
        }
    }
}
