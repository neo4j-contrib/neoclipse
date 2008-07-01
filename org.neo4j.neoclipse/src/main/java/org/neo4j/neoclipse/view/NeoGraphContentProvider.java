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
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;

/**
 * Provides the elements that must be displayed in the graph.
 * @author Peter H&auml;nsgen
 */
public class NeoGraphContentProvider implements
    IGraphEntityRelationshipContentProvider
{
    /**
     * The view.
     */
    protected NeoGraphViewPart view;

    /**
     * The constructor.
     */
    public NeoGraphContentProvider( NeoGraphViewPart view )
    {
        this.view = view;
    }

    /**
     * Returns the relationships between the given nodes.
     */
    public Object[] getRelationships( Object source, Object dest )
    {
        Node start = (Node) source;
        Node end = (Node) dest;
        List<Relationship> rels = new ArrayList<Relationship>();
        Iterable<Relationship> rs = start.getRelationships( Direction.OUTGOING );
        for ( Relationship r : rs )
        {
            if ( r.getEndNode().getId() == end.getId() )
            {
                rels.add( r );
            }
        }
        return rels.toArray();
    }

    /**
     * Returns all nodes the given node is connected with.
     */
    public Object[] getElements( Object inputElement )
    {
        Node node = (Node) inputElement;
        Set<Node> nodes = new HashSet<Node>();
        // add the start node too
        nodes.add( node );
        Set<Node> startList = new HashSet<Node>();
        startList.add( node );
        getElements( startList, nodes, view.getTraversalDepth() );
        return nodes.toArray();
    }

    /**
     * Determines the connected nodes within the given traversal depth.
     */
    private void getElements( Set<Node> oldNodes, Set<Node> nodes, int depth )
    {
        if ( depth > 0 )
        {
            Set<Node> newNodes = new HashSet<Node>();
            for ( Node oldNode : oldNodes )
            {
                Iterable<Relationship> rs;
                rs = oldNode.getRelationships( Direction.INCOMING );
                for ( Relationship r : rs )
                {
                    newNodes.add( r.getStartNode() );
                }
                rs = oldNode.getRelationships( Direction.OUTGOING );
                for ( Relationship r : rs )
                {
                    newNodes.add( r.getEndNode() );
                }
            }
            nodes.addAll( newNodes );
            getElements( newNodes, nodes, depth - 1 );
        }
    }

    public void dispose()
    {
    }

    public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
    {
    }
}
