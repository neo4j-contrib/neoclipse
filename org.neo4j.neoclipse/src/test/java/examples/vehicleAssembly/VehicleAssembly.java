/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

/**
 * Example for parts inventory and assembly. From Database Management Systems,
 * 2nd edition, Raghu Ramakrishnan / Johannes Gehrke, page 799 ff.
 * @author Anders Nawroth
 */
public class VehicleAssembly extends NeoclipseExample
{
    private static final String NT_REFERENCE = "referenceNode";
    private static final String NT_VEHICLE = "vehicle";
    private static final String NT_PART = "part";
    private static final String NAME = "NAME";
    private static final String NODE_TYPE = "NODE_TYPE";
    private static final String COST = "COST";
    private static final String QUANTITY = "QUANTITY";

    @Test
    public void createTrike()
    {
        Transaction tx = Transaction.begin();
        try
        {
            Node referenceNode = neo.getReferenceNode();
            referenceNode.setProperty( NAME, "referenceNode" );
            referenceNode.setProperty( NODE_TYPE, NT_REFERENCE );
            Node trike = neo.createNode();
            trike.setProperty( NAME, "trike" );
            trike.setProperty( NODE_TYPE, NT_VEHICLE );
            trike.setProperty( COST, 3 );
            referenceNode.createRelationshipTo( trike, VehicleRels.VEHICLE );
            Node wheel = createPart( "wheel", 3, trike, 3 );
            Node frame = createPart( "frame", 15, trike, 1 );
            createPart( "spoke", 1, wheel, 2 );
            Node tire = createPart( "tire", 2, wheel, 1 );
            createPart( "rim", 2, tire, 1 );
            createPart( "tube", 1, tire, 1 );
            createPart( "seat", 4, frame, 1 );
            createPart( "pedal", 1, frame, 1 );
            tx.success();
        }
        finally
        {
            tx.finish();
        }
    }

    private Node createPart( String name, int cost, Node containedIn,
        int quantity )
    {
        Node part = neo.createNode();
        part.setProperty( NAME, name );
        part.setProperty( NODE_TYPE, NT_PART );
        part.setProperty( COST, cost );
        containedIn.createRelationshipTo( part, VehicleRels.CONTAINS )
            .setProperty( QUANTITY, quantity );
        return part;
    }

    @Test
    public void showParts()
    {
        Transaction tx = Transaction.begin();
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
                for ( Node part : traverser )
                {
                    int depth = traverser.currentPosition().depth();
                    for ( int i = 0; i < depth; i++ )
                    {
                        System.out.print( "  " );
                    }
                    System.out.print( part.getProperty( NAME ) );
                    Relationship rel = traverser.currentPosition()
                        .lastRelationshipTraversed();
                    if ( rel != null )
                    {
                        System.out.print( " " + rel.getProperty( QUANTITY, 0 ) );
                    }
                    System.out.println();
                }
            }
            tx.success();
        }
        finally
        {
            tx.finish();
        }
    }

    @Test
    public void productCosts()
    {
        Transaction tx = Transaction.begin();
        try
        {
            for ( Relationship vehicles : neo.getReferenceNode()
                .getRelationships( Direction.OUTGOING ) )
            {
                Node vehicle = vehicles.getEndNode();
                System.out.println( vehicle.getProperty( NAME ) + ": "
                    + getCost( vehicle ) );
            }
            tx.success();
        }
        finally
        {
            tx.finish();
        }
    }

    private int getCost( Node node )
    {
        Integer sum = (Integer) node.getProperty( COST, 0 );
        for ( Relationship rel : node.getRelationships( Direction.OUTGOING ) )
        {
            sum += getCost( rel.getEndNode() )
                * ((Integer) rel.getProperty( QUANTITY, 1 ));
        }
        return sum;
    }
}
