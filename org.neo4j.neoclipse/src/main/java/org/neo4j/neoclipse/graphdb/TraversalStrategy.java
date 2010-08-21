package org.neo4j.neoclipse.graphdb;

import java.util.Collection;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.neoclipse.reltype.DirectedRelationship;

public interface TraversalStrategy
{
    Collection<Node> getNodes( Node node, int depth, int nodeLimit );

    Collection<Node> getNodes( Node node,
            Collection<? extends DirectedRelationship> directedRels, int depth,
            int nodeLimit );

    Collection<Relationship> getRelationships( Node start, Node end );
}
