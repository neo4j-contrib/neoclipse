package org.neo4j.neoclipse.graphdb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.TraversalPosition;
import org.neo4j.graphdb.Traverser;
import org.neo4j.graphdb.Traverser.Order;
import org.neo4j.neoclipse.reltype.DirectedRelationship;
import org.neo4j.neoclipse.reltype.RelationshipTypeHashSet;

public class DefaultTraverser implements TraversalStrategy
{
    private final Set<RelationshipType> relTypes = new RelationshipTypeHashSet();

    public Collection<Node> getNodes( final Node node, final int depth,
            final int nodeLimit )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<Node> getNodes( final Node node,
            final Collection<? extends DirectedRelationship> directedRels,
            final int depth, final int nodeLimit )
    {
        List<Node> nodes = new ArrayList<Node>();
        relTypes.clear();
        List<Object> traverseTypes = new ArrayList<Object>();
        for ( DirectedRelationship directedRel : directedRels )
        {
            if ( !directedRel.hasDirection() )
            {
                continue;
            }
            relTypes.add( directedRel.getRelType() );
            traverseTypes.add( directedRel.getRelType() );
            traverseTypes.add( directedRel.getDirection() );
        }
        Object[] relDirListArray = traverseTypes.toArray();
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
                if ( nodes.size() >= nodeLimit )
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
        return nodes;
    }

    public Collection<Relationship> getRelationships( final Node start,
            final Node end )
    {
        List<Relationship> rels = new ArrayList<Relationship>();
        if ( relTypes.isEmpty() )
        {
            for ( Relationship r : start.getRelationships( Direction.OUTGOING ) )
            {
                if ( r.getEndNode().equals( end ) )
                {
                    rels.add( r );
                }
            }
        }
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
        return rels;
    }
}
