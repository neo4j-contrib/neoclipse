package examples.vehicleAssembly;

import org.junit.Test;
import org.neo4j.api.core.Direction;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.ReturnableEvaluator;
import org.neo4j.api.core.StopEvaluator;
import org.neo4j.api.core.Transaction;
import org.neo4j.api.core.Traverser;

import examples.NeoclipseExample;

public class VehicleAssembly extends NeoclipseExample
{
    private static final String NT_VEHICLE = "vehicle";
    private static final String NT_PART = "part";
    private static final String NAME = "NAME";
    private static final String NODE_TYPE = "NODE_TYPE";
    private static final String COST = "COST";
    private static final String QUANTITY = "QUANTITY";

    @Test
    public void createTrike()
    {
        Transaction transaction = Transaction.begin();
        try
        {
            Node referenceNode = neo.getReferenceNode();
            referenceNode.setProperty( NAME, "referenceNode" );
            referenceNode.setProperty( NODE_TYPE, "referenceNode" );
            Node trike = neo.createNode();
            trike.setProperty( NAME, "trike" );
            trike.setProperty( NODE_TYPE, NT_VEHICLE );
            trike.setProperty( COST, 3 );
            referenceNode.createRelationshipTo( trike, VehicleRels.VEHICLE );
            Node wheel = neo.createNode();
            wheel.setProperty( NAME, "wheel" );
            wheel.setProperty( NODE_TYPE, NT_PART );
            wheel.setProperty( COST, 3 );
            trike.createRelationshipTo( wheel, VehicleRels.CONTAINS )
                .setProperty( QUANTITY, 3 );
            Node frame = neo.createNode();
            frame.setProperty( NAME, "frame" );
            frame.setProperty( NODE_TYPE, NT_PART );
            frame.setProperty( COST, 15 );
            trike.createRelationshipTo( frame, VehicleRels.CONTAINS )
                .setProperty( QUANTITY, 1 );
            Node spoke = neo.createNode();
            spoke.setProperty( NAME, "spoke" );
            spoke.setProperty( NODE_TYPE, NT_PART );
            spoke.setProperty( COST, 1 );
            wheel.createRelationshipTo( spoke, VehicleRels.CONTAINS )
                .setProperty( QUANTITY, 2 );
            Node tire = neo.createNode();
            tire.setProperty( NAME, "tire" );
            tire.setProperty( NODE_TYPE, NT_PART );
            tire.setProperty( COST, 2 );
            wheel.createRelationshipTo( tire, VehicleRels.CONTAINS )
                .setProperty( QUANTITY, 1 );
            Node rim = neo.createNode();
            rim.setProperty( NAME, "rim" );
            rim.setProperty( NODE_TYPE, NT_PART );
            rim.setProperty( COST, 2 );
            tire.createRelationshipTo( rim, VehicleRels.CONTAINS ).setProperty(
                QUANTITY, 1 );
            Node tube = neo.createNode();
            tube.setProperty( NAME, "tube" );
            tube.setProperty( NODE_TYPE, NT_PART );
            tube.setProperty( COST, 1 );
            tire.createRelationshipTo( tube, VehicleRels.CONTAINS )
                .setProperty( QUANTITY, 1 );
            Node seat = neo.createNode();
            seat.setProperty( NAME, "seat" );
            seat.setProperty( NODE_TYPE, NT_PART );
            seat.setProperty( COST, 4 );
            frame.createRelationshipTo( seat, VehicleRels.CONTAINS )
                .setProperty( QUANTITY, 1 );
            Node pedal = neo.createNode();
            pedal.setProperty( NAME, "pedal" );
            pedal.setProperty( NODE_TYPE, NT_PART );
            pedal.setProperty( COST, 1 );
            frame.createRelationshipTo( pedal, VehicleRels.CONTAINS )
                .setProperty( QUANTITY, 1 );
            transaction.success();
        }
        finally
        {
            transaction.finish();
        }
    }

    @Test
    public void showParts()
    {
        Transaction transaction = Transaction.begin();
        try
        {
            for ( Relationship vehicles : neo.getReferenceNode()
                .getRelationships( Direction.OUTGOING ) )
            {
                Node vehicle = vehicles.getEndNode();
                System.out.println( "Product: " + vehicle.getProperty( NAME ) );
                Traverser traverser = vehicle.traverse(
                    Traverser.Order.DEPTH_FIRST, StopEvaluator.END_OF_NETWORK,
                    ReturnableEvaluator.ALL_BUT_START_NODE,
                    VehicleRels.CONTAINS, Direction.OUTGOING );
                for ( Node node : traverser )
                {
                    int depth = traverser.currentPosition().depth();
                    Relationship rel = traverser.currentPosition()
                        .lastRelationshipTraversed();
                    for ( int i = 0; i < depth; i++ )
                    {
                        System.out.print( "  " );
                    }
                    System.out.print( node.getProperty( NAME ) );
                    if ( rel != null )
                    {
                        System.out.print( " " + rel.getProperty( QUANTITY, 0 ) );
                    }
                    System.out.println();
                }
            }
            transaction.success();
        }
        finally
        {
            transaction.finish();
        }
    }

    @Test
    public void productCosts()
    {
        Transaction transaction = Transaction.begin();
        try
        {
            for ( Relationship vehicles : neo.getReferenceNode()
                .getRelationships( Direction.OUTGOING ) )
            {
                Node vehicle = vehicles.getEndNode();
                System.out.println( vehicle.getProperty( NAME ) + ": "
                    + getCost( vehicle ) );
            }
            transaction.success();
        }
        finally
        {
            transaction.finish();
        }
    }

    private int getCost( Node node )
    {
        Integer sum = (Integer) node.getProperty( COST, 0 );
        for ( Relationship rel : node.getRelationships( Direction.OUTGOING ) )
        {
            sum += getCost( rel.getEndNode() )
                * ((Integer) rel.getProperty( QUANTITY ));
        }
        return sum;
    }
}
