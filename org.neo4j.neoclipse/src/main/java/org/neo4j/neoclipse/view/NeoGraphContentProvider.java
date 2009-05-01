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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.zest.core.viewers.IGraphEntityRelationshipContentProvider;
import org.neo4j.api.core.Direction;
import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.NotFoundException;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.RelationshipType;
import org.neo4j.api.core.ReturnableEvaluator;
import org.neo4j.api.core.StopEvaluator;
import org.neo4j.api.core.TraversalPosition;
import org.neo4j.api.core.Traverser;
import org.neo4j.api.core.Traverser.Order;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.reltype.RelationshipTypesProvider;
import org.neo4j.neoclipse.reltype.RelationshipTypesProviderWrapper;

/**
 * Provides the elements that must be displayed in the graph.
 * @author Peter H&auml;nsgen
 */
public class NeoGraphContentProvider implements
    IGraphEntityRelationshipContentProvider
{
    /**
     * Limit the number of nodes returned.
     */
    private static final int MAXIMUM_NODES_RETURNED = 500;
    private final RelationshipTypesProvider relTypesProvider = RelationshipTypesProviderWrapper
        .getInstance();
    /**
     * The view.
     */
    protected NeoGraphViewPart view;
    private final Set<RelationshipType> relTypes = new HashSet<RelationshipType>();

    /**
     * The constructor.
     */
    public NeoGraphContentProvider( final NeoGraphViewPart view )
    {
        this.view = view;
    }

    /**
     * Returns the relationships between the given nodes.
     */
    public Object[] getRelationships( final Object source, final Object dest )
    {
        Node start = (Node) source;
        Node end = (Node) dest;
        List<Relationship> rels = new ArrayList<Relationship>();
        if ( !relTypes.isEmpty() )
        {
            for ( RelationshipType relType : relTypes )
            {
                for ( Relationship r : start.getRelationships( relType,
                    Direction.OUTGOING ) )
                {
                    if ( r.getEndNode().equals( end ) )
                    {
                        rels.add( r );
                    }
                }
            }
        }
        else
        {
            for ( Relationship r : start.getRelationships( Direction.OUTGOING ) )
            {
                if ( r.getEndNode().equals( end ) )
                {
                    rels.add( r );
                }
            }
        }
        return rels.toArray();
    }

    /**
     * Returns all nodes the given node is connected with.
     */
    public Object[] getElements( final Object inputElement )
    {
        Node node = (Node) inputElement;
        final NeoService neoService = Activator.getDefault()
            .getNeoServiceSafely();
        if ( neoService == null )
        {
            return new Node[] { node };
        }

        List<Object> relDirList;
        try
        {
            relDirList = relTypesProvider.getFilteredRelTypesDirections();
        }
        catch ( NotFoundException nfe )
        {
            // (no relationship types found by the provider)
            // we'll end up here when the reltypes are not initialized,
            // and we don't want them to initialize first
            // (traversal gives better coloring!)
            relDirList = new ArrayList<Object>();
            for ( RelationshipType relType : RelationshipTypesProviderWrapper
                .getInstance().getRelationshipTypesFromNeo() )
            {
                relDirList.add( relType );
                relDirList.add( Direction.BOTH );
            }
        }
        if ( relDirList.isEmpty() )
        {
            // if there are no relationship types,
            // there can't be any relationships ...
            return new Node[] { node };
        }

        Object[] relDirListArray = relDirList.toArray();

        relTypes.clear();
        for ( Object o : relDirListArray )
        {
            if ( o instanceof RelationshipType )
            {
                relTypes.add( (RelationshipType) o );
            }
        }

        final int depth = view.getTraversalDepth();
        List<Node> nodes = new ArrayList<Node>();
        try
        {
            Traverser trav = node.traverse( Order.BREADTH_FIRST,
                new StopEvaluator()
                {
                    public boolean isStopNode(
                        final TraversalPosition currentPos )
                    {
                        return currentPos.depth() >= depth;
                    }
                }, ReturnableEvaluator.ALL, relDirListArray );
            for ( Node currentNode : trav )
            {
                if ( nodes.size() >= MAXIMUM_NODES_RETURNED )
                {
                    break;
                }
                nodes.add( currentNode );
            }

        }
        catch ( NotFoundException nfe )
        {
            // this happens when the start node has been removed
            // somehow (could be a rollback operation)
            // just return an empty array then
        }
        return nodes.toArray();
    }

    public void dispose()
    {
    }

    public void inputChanged( final Viewer viewer, final Object oldInput,
        final Object newInput )
    {
    }
}
