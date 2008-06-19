package examples.animals;

import org.junit.Test;
import org.neo4j.api.core.Direction;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.ReturnableEvaluator;
import org.neo4j.api.core.StopEvaluator;
import org.neo4j.api.core.Transaction;
import org.neo4j.api.core.Traverser;

import examples.NeoclipseExample;

/**
 * Example on modeling DAGs from Kemal Erdogan, 
 * http://www.codeproject.com/KB/database/Modeling_DAGs_on_SQL_DBs.aspx
 * @author Anders Nawroth
 */
public class Animals extends NeoclipseExample
{
    private static final String NT_REFERENCE = "referenceNode";
    private static final String NT_CATEGORY = "category";
    private static final String NT_SPECIES = "species";
    private static final String NT_RACE = "race";
    private static final String NAME = "NAME";
    private static final String NODE_TYPE = "NODE_TYPE";

    @Test
    public void createAnimals()
    {
        Transaction tx = Transaction.begin();
        try
        {
            Node referenceNode = neo.getReferenceNode();
            referenceNode.setProperty( NAME, "referenceNode" );
            referenceNode.setProperty( NODE_TYPE, NT_REFERENCE );
            Node animal = createCategory( "Animal", referenceNode );
            Node pet = createCategory( "Pet", animal );
            Node livestock = createCategory( "Livestock", animal );
            createSpecies( "Cat", pet );
            Node dog = createSpecies( "Dog", pet, livestock );
            createRace( "Doberman", dog );
            createRace( "Bulldog", dog );
            createSpecies( "Sheep", pet, livestock );
            createSpecies( "Cow", livestock );
            tx.success();
        }
        finally
        {
            tx.finish();
        }
    }

    private Node createCategory( String name, Node... containedIn )
    {
        return createNode( name, NT_CATEGORY, containedIn );
    }

    private Node createSpecies( String name, Node... containedIn )
    {
        return createNode( name, NT_SPECIES, containedIn );
    }

    private Node createRace( String name, Node... containedIn )
    {
        return createNode( name, NT_RACE, containedIn );
    }

    private Node createNode( String name, String nodeType, Node... containedIn )
    {
        Node node = neo.createNode();
        node.setProperty( NAME, name );
        node.setProperty( NODE_TYPE, nodeType );
        for ( Node parent : containedIn )
        {
            parent.createRelationshipTo( node, AnimalRels.CONTAINS );
        }
        return node;
    }

    @Test
    public void getAllLivestock()
    {
        Transaction tx = Transaction.begin();
        try
        {
            Node livestock = neo.getNodeById( 3 );
            Traverser traverser = livestock.traverse(
                Traverser.Order.DEPTH_FIRST, StopEvaluator.END_OF_NETWORK,
                ReturnableEvaluator.ALL_BUT_START_NODE, AnimalRels.CONTAINS,
                Direction.OUTGOING );
            for ( Node part : traverser )
            {
                int depth = traverser.currentPosition().depth();
                for ( int i = 0; i < depth; i++ )
                {
                    System.out.print( "  " );
                }
                System.out.println( part.getProperty( NAME ) );
            }
            tx.success();
        }
        finally
        {
            tx.finish();
        }
    }
}
