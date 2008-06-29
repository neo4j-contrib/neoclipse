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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.zest.core.viewers.IGraphContentProvider;
import org.neo4j.api.core.Direction;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;

public class NeoGraphRelationshipContentProvider implements
    IGraphContentProvider
{
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

    public Object[] getElements( Object input )
    {
        Map<Long,Relationship> rels = new HashMap<Long,Relationship>();
        Set<Long> nodes = new TreeSet<Long>();
        getElements( (Node) input, rels, nodes, view.getTraversalDepth() );
        return rels.values().toArray();
    }

    public Object getSource( Object rel )
    {
        return ((Relationship) rel).getStartNode();
    }

    /**
     * Determines the connected nodes within the given traversal depth.
     */
    private void getElements( Node node, Map<Long,Relationship> rels,
        Set<Long> nodes, int depth )
    {
        if ( depth > 0 )
        {
            for ( Relationship r : node.getRelationships( Direction.BOTH ) )
            {
                if ( !rels.containsKey( r.getId() ) )
                {
                    rels.put( r.getId(), r );
                    Node other = r.getOtherNode( node );
                    getElements( other, rels, nodes, depth - 1 );
                    if ( depth == 1 )
                    {
                        for ( Relationship otherRel : other
                            .getRelationships( Direction.BOTH ) )
                        {
                            if ( nodes.contains( otherRel.getOtherNode( other )
                                .getId() ) )
                            {
                                rels.put( otherRel.getId(), otherRel );
                            }
                        }
                    }
                }
            }
        }
        else
        {
            nodes.add( node.getId() );
        }
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
