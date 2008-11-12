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
package examples.vehicleAssembly;

import org.junit.BeforeClass;
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
 * Example for parts inventory and assembly. Based off example in Database
 * Management Systems, 3rd edition, Raghu Ramakrishnan / Johannes Gehrke,
 * chapter 25.
 * @author Anders Nawroth
 */
public class VehicleAssembly extends NeoclipseExample
{
    @BeforeClass
    public static void copyIcons()
    {
        NeoclipseExample.copyIcons( "vehicleAssembly" );
    }

    @BeforeClass
    public static void createTrike()
    {
        Transaction tx = neo.beginTx();
        try
        {
            Node trike = createVehicle( "trike", 3 );
            Node motorcycle = createVehicle( "motorcycle", 2 );
            Node wheel = createPart( "wheel", 3, trike, 3, motorcycle, 2 );
            Node frame = createPart( "frame", 15, trike, 1, motorcycle, 1 );
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

    private static Node createVehicle( String name, int cost )
    {
        Node vehicle = neo.createNode();
        vehicle.setProperty( "name", name );
        vehicle.setProperty( "cost", cost );
        neo.getReferenceNode().createRelationshipTo( vehicle,
            VehicleRels.VEHICLE );
        return vehicle;
    }

    private static Node createPart( String name, int cost,
        Object... nodesAndQuantities )
    {
        Node part = neo.createNode();
        part.setProperty( "name", name );
        part.setProperty( "cost", cost );
        for ( int i = 0; i < nodesAndQuantities.length; i += 2 )
        {
            ((Node) nodesAndQuantities[i]).createRelationshipTo( part,
                VehicleRels.COMPOSED_BY ).setProperty( "quantity",
                (Integer) nodesAndQuantities[i + 1] );
        }
        return part;
    }

    @Test
    public void showParts()
    {
        Transaction tx = neo.beginTx();
        try
        {
            System.out.println( "Product components list" );
            for ( Relationship vehicleRel : neo.getReferenceNode()
                .getRelationships( Direction.OUTGOING ) )
            {
                Node vehicle = vehicleRel.getEndNode();
                System.out
                    .println( "Product: " + vehicle.getProperty( "name" ) );
                Traverser traverser = vehicle.traverse(
                    Traverser.Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH,
                    ReturnableEvaluator.ALL_BUT_START_NODE,
                    VehicleRels.COMPOSED_BY, Direction.OUTGOING );
                for ( Node part : traverser )
                {
                    int depth = traverser.currentPosition().depth();
                    for ( int i = 0; i < depth; i++ )
                    {
                        System.out.print( "  " );
                    }
                    System.out.print( part.getProperty( "name" ) );
                    Relationship rel = traverser.currentPosition()
                        .lastRelationshipTraversed();
                    System.out.println( " " + rel.getProperty( "quantity", 0 ) );
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
        Transaction tx = neo.beginTx();
        try
        {
            System.out.println( "Pricelist:" );
            for ( Relationship vehicles : neo.getReferenceNode()
                .getRelationships( Direction.OUTGOING ) )
            {
                Node vehicle = vehicles.getEndNode();
                System.out.println( vehicle.getProperty( "name" ) + ": "
                    + getCost( vehicle ) );
            }
            tx.success();
        }
        finally
        {
            tx.finish();
        }
    }

    private int getCost( Node part )
    {
        int sum = (Integer) part.getProperty( "cost", 0 );
        for ( Relationship rel : part.getRelationships( Direction.OUTGOING ) )
        {
            Node subPart = rel.getEndNode();
            int quantity = (Integer) rel.getProperty( "quantity", 1 );
            sum += getCost( subPart ) * quantity;
        }
        return sum;
    }
}
