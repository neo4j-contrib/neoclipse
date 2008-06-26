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

public class NeoRelationshipContentProvider implements IGraphContentProvider
{
    /**
     * The view.
     */
    protected NeoGraphViewPart view;

    /**
     * The constructor.
     */
    public NeoRelationshipContentProvider( NeoGraphViewPart view )
    {
        this.view = view;
    }

    @Override
    public Object getDestination( Object rel )
    {
        return ((Relationship) rel).getEndNode();
    }

    @Override
    public Object[] getElements( Object input )
    {
        Map<Long,Relationship> rels = new HashMap<Long,Relationship>();
        Set<Long> nodes = new TreeSet<Long>();
        getElements( (Node) input, rels, nodes, view.getTraversalDepth() );
        return rels.values().toArray();
    }

    @Override
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
        nodes.add( node.getId() );
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
                        for (Relationship otherRel : other.getRelationships(Direction.BOTH))
                        {
                            if (nodes.contains( otherRel.getOtherNode( other ).getId() ))
                            {
                                rels.put( otherRel.getId(), otherRel );
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void dispose()
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
    {
        // TODO Auto-generated method stub
    }
}
