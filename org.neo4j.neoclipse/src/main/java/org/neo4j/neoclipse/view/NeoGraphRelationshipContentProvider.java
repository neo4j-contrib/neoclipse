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
import org.neo4j.api.core.EmbeddedNeo;
import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.RelationshipType;
import org.neo4j.api.core.ReturnableEvaluator;
import org.neo4j.api.core.StopEvaluator;
import org.neo4j.api.core.TraversalPosition;
import org.neo4j.api.core.Traverser;
import org.neo4j.api.core.Traverser.Order;
import org.neo4j.neoclipse.Activator;

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
    public NeoGraphRelationshipContentProvider( NeoGraphViewPart view )
    {
        this.view = view;
    }

    public Object getDestination( Object rel )
    {
        return ((Relationship) rel).getEndNode();
    }

    @SuppressWarnings( "deprecation" )
    public Object[] getElements( Object input )
    {
        final int depth = view.getTraversalDepth() - 1;
        if ( depth == -1 )
        {
            // view.addCurrentNode();
            return EMPTY_REL_ARRAY;
        }
        List<Object> relDirList = new ArrayList<Object>();
        final NeoService neoService = Activator.getDefault()
            .getNeoServiceSafely();
        if ( neoService == null )
        {
            return EMPTY_REL_ARRAY;
        }
        for ( RelationshipType relType : ((EmbeddedNeo) neoService)
            .getRelationshipTypes() )
        {
            relDirList.add( relType );
            relDirList.add( Direction.BOTH );
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
                public boolean isStopNode( TraversalPosition currentPos )
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

    public Object getSource( Object rel )
    {
        return ((Relationship) rel).getStartNode();
    }

    public void dispose()
    {
        // TODO Auto-generated method stub
    }

    public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
    {
        // TODO Auto-generated method stub
    }
}
