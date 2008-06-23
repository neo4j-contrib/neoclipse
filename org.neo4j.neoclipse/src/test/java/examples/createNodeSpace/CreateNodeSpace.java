package examples.createNodeSpace;

import org.junit.Test;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.Transaction;

import examples.NeoclipseExample;

public class CreateNodeSpace extends NeoclipseExample
{
    private static final String NAME = "NAME";
    private static final String NODE_TYPE = "NODE_TYPE";

    @Test
    public void testCreateSimpleNodeSpace()
    {
        Transaction tx = Transaction.begin();
        try
        {
            Node referenceNode = neo.getReferenceNode();
            referenceNode.setProperty( NAME, "referenceNode" );
            referenceNode.setProperty( NODE_TYPE, "referenceNode" );
            Node peter = neo.createNode();
            peter.setProperty( NAME, "Peter" );
            peter.setProperty( NODE_TYPE, "human" );
            peter.setProperty( "int-test", 7 );
            peter.setProperty( "a-int-test", new int[] { 1, 2, 3 } );
            Node li = neo.createNode();
            li.setProperty( NAME, "Li" );
            li.setProperty( NODE_TYPE, "human" );
            Node blaff = neo.createNode();
            blaff.setProperty( NAME, "blaff" );
            blaff.setProperty( NODE_TYPE, "animal" );
            Node woff = neo.createNode();
            woff.setProperty( NAME, "woff" );
            woff.setProperty( NODE_TYPE, "animal" );
            Relationship peterRootRel = referenceNode.createRelationshipTo(
                peter, MyRels.ROOT );
            peterRootRel.setProperty( "TEST_PROPERTY", "test value" );
            referenceNode.createRelationshipTo( li, MyRels.ROOT );
            li.createRelationshipTo( peter, MyRels.KNOWS );
            peter.createRelationshipTo( woff, MyRels.OWNS );
            li.createRelationshipTo( blaff, MyRels.OWNS );
            tx.success();
        }
        finally
        {
            tx.finish();
        }
    }
}
