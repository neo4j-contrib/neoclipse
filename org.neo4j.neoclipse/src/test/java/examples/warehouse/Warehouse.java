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
package examples.warehouse;

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
 * chapter 25. Warehouses added in this one.
 * @author Anders Nawroth
 */
public class Warehouse extends NeoclipseExample
{
    @BeforeClass
    public static void copyIcons()
    {
        NeoclipseExample.copyIcons( "warehouse" );
    }

    @BeforeClass
    public static void createTrike()
    {
        Transaction tx = neo.beginTx();
        try
        {
            Node trike = createVehicle( "trike", 3 );
            Node wheel = createPart( "wheel", 3, trike, 3 );
            Node frame = createPart( "frame", 15, trike, 1 );
            Node spoke = createPart( "spoke", 1, wheel, 2 );
            Node tire = createPart( "tire", 2, wheel, 1 );
            Node rim = createPart( "rim", 2, tire, 1 );
            Node tube = createPart( "tube", 1, tire, 1 );
            Node seat = createPart( "seat", 4, frame, 1 );
            Node pedal = createPart( "pedal", 1, frame, 1 );
            // create warehouses and add stuff to them
            Node mainWarehouse = createWarehouse( "mainstore" );
            Node frameWarehouse = createWarehouse( "framestore" );
            Node wheelWarehouse = createWarehouse( "wheelstore" );
            linkPartAndWarehouse( trike, mainWarehouse, 50 );
            linkPartAndWarehouse( wheel, wheelWarehouse, 200 );
            linkPartAndWarehouse( frame, frameWarehouse, 30 );
            linkPartAndWarehouse( spoke, wheelWarehouse, 70 );
            linkPartAndWarehouse( tire, wheelWarehouse, 60 );
            linkPartAndWarehouse( rim, frameWarehouse, 60 );
            linkPartAndWarehouse( rim, wheelWarehouse, 100 );
            linkPartAndWarehouse( tube, wheelWarehouse, 50 );
            linkPartAndWarehouse( seat, frameWarehouse, 35 );
            linkPartAndWarehouse( pedal, frameWarehouse, 30 );
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
            WarehouseRels.VEHICLE );
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
                WarehouseRels.COMPOSED_BY ).setProperty( "quantity",
                (Integer) nodesAndQuantities[i + 1] );
        }
        return part;
    }

    private static Node createWarehouse( String name )
    {
        Node warehouse = neo.createNode();
        warehouse.setProperty( "name", name );
        neo.getReferenceNode().createRelationshipTo( warehouse,
            WarehouseRels.WAREHOUSE );
        return warehouse;
    }

    private static void linkPartAndWarehouse( Node part, Node warehouse,
        int quantity )
    {
        Relationship rel = part.createRelationshipTo( warehouse,
            WarehouseRels.STORED_IN );
        rel.setProperty( "quantity", quantity );
    }

    @Test
    public void showParts()
    {
        Transaction tx = neo.beginTx();
        try
        {
            System.out.println( "Product components list" );
            Traverser vehicles = neo.getReferenceNode().traverse(
                Traverser.Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE,
                ReturnableEvaluator.ALL_BUT_START_NODE, WarehouseRels.VEHICLE,
                Direction.OUTGOING );
            for ( Node vehicle : vehicles )
            {
                System.out
                    .println( "Product: " + vehicle.getProperty( "name" ) );
                Traverser traverser = vehicle.traverse(
                    Traverser.Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH,
                    ReturnableEvaluator.ALL_BUT_START_NODE,
                    WarehouseRels.COMPOSED_BY, Direction.OUTGOING );
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
                    System.out.print( " " + rel.getProperty( "quantity", 0 )
                        + " ( " );
                    Traverser warehouses = part.traverse(
                        Traverser.Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE,
                        ReturnableEvaluator.ALL_BUT_START_NODE,
                        WarehouseRels.STORED_IN, Direction.OUTGOING );
                    for ( Node warehouse : warehouses )
                    {
                        String name = (String) warehouse.getProperty( "name",
                            "" );
                        int quantity = (Integer) warehouses.currentPosition()
                            .lastRelationshipTraversed().getProperty(
                                "quantity", 0 );
                        System.out.print( name + " " + quantity + " " );
                    }
                    System.out.println( ")" );
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
            Traverser vehicles = neo.getReferenceNode().traverse(
                Traverser.Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE,
                ReturnableEvaluator.ALL_BUT_START_NODE, WarehouseRels.VEHICLE,
                Direction.OUTGOING );
            for ( Node vehicle : vehicles )
            {
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
        Traverser subParts = part.traverse( Traverser.Order.BREADTH_FIRST,
            StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE,
            WarehouseRels.COMPOSED_BY, Direction.OUTGOING );
        for ( Node subPart : subParts )
        {
            Relationship rel = subParts.currentPosition()
                .lastRelationshipTraversed();
            int quantity = (Integer) rel.getProperty( "quantity", 1 );
            sum += getCost( subPart ) * quantity;
        }
        return sum;
    }

    @Test
    public void inventory()
    {
        Transaction tx = neo.beginTx();
        try
        {
            System.out.println( "Inventory list" );
            Traverser warehouses = neo.getReferenceNode().traverse(
                Traverser.Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE,
                ReturnableEvaluator.ALL_BUT_START_NODE,
                WarehouseRels.WAREHOUSE, Direction.OUTGOING );
            for ( Node warehouse : warehouses )
            {
                System.out.println( "Warehouse: "
                    + warehouse.getProperty( "name" ) );
                Traverser traverser = warehouse.traverse(
                    Traverser.Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE,
                    ReturnableEvaluator.ALL_BUT_START_NODE,
                    WarehouseRels.STORED_IN, Direction.INCOMING );
                for ( Node part : traverser )
                {
                    System.out.print( " " + part.getProperty( "name" ) );
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
}
