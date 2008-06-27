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

public class NeoGraphRelationshipContentProvider implements IGraphContentProvider
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
