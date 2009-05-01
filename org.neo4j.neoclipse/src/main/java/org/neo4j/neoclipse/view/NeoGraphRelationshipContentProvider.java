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
import org.eclipse.zest.core.viewers.IGraphContentProvider;
import org.neo4j.api.core.Direction;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.NotFoundException;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.RelationshipType;
import org.neo4j.api.core.ReturnableEvaluator;
import org.neo4j.api.core.StopEvaluator;
import org.neo4j.api.core.TraversalPosition;
import org.neo4j.api.core.Traverser;
import org.neo4j.api.core.Traverser.Order;
import org.neo4j.neoclipse.reltype.RelationshipTypesProvider;
import org.neo4j.neoclipse.reltype.RelationshipTypesProviderWrapper;

/**
 * Get content through relations. TODO: view.addCurrentNode(); calls has to be
 * used to get it working
 */
public class NeoGraphRelationshipContentProvider implements
    IGraphContentProvider
{
    private static final Relationship[] EMPTY_REL_ARRAY = new Relationship[0];
    /**
     * The view.
     */
    protected NeoGraphViewPart view;

    /**
     * The constructor.
     */
    public NeoGraphRelationshipContentProvider( final NeoGraphViewPart view )
    {
        this.view = view;
    }

    public Object getDestination( final Object rel )
    {
        return ((Relationship) rel).getEndNode();
    }

    public Object[] getElements( final Object input )
    {
        final int depth = view.getTraversalDepth() - 1;
        if ( depth == -1 )
        {
            // view.addCurrentNode();
            return EMPTY_REL_ARRAY;
        }
        List<Object> relDirList;
        try
        {
            RelationshipTypesProvider relTypesProvider = RelationshipTypesProviderWrapper
                .getInstance();
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
            // view.addCurrentNode();
            return EMPTY_REL_ARRAY;
        }
        Node node = (Node) input;
        Traverser trav = node.traverse( Order.BREADTH_FIRST,
            new StopEvaluator()
            {
                public boolean isStopNode( final TraversalPosition currentPos )
                {
                    return currentPos.depth() >= depth;
                }
            }, ReturnableEvaluator.ALL, relDirList.toArray() );
        Set<Relationship> rels = new HashSet<Relationship>();
        for ( Node current : trav )
        {
            if ( trav.currentPosition().depth() != depth )
            {
                for ( Relationship rel : current
                    .getRelationships( Direction.OUTGOING ) )
                {
                    rels.add( rel );
                }
            }
            else
            {
                for ( Relationship rel : current.getRelationships() )
                {
                    rels.add( rel );
                }
            }
        }
        if ( rels.isEmpty() )
        {
            // view.addCurrentNode();
            return EMPTY_REL_ARRAY;
        }
        return rels.toArray( EMPTY_REL_ARRAY );
    }

    public Object getSource( final Object rel )
    {
        return ((Relationship) rel).getStartNode();
    }

    public void dispose()
    {
        // TODO Auto-generated method stub
    }

    public void inputChanged( final Viewer viewer, final Object oldInput,
        final Object newInput )
    {
        // TODO Auto-generated method stub
    }
}
