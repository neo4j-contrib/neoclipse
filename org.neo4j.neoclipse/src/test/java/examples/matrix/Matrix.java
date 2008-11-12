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
package examples.matrix;

import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.api.core.Direction;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.ReturnableEvaluator;
import org.neo4j.api.core.StopEvaluator;
import org.neo4j.api.core.Transaction;
import org.neo4j.api.core.TraversalPosition;
import org.neo4j.api.core.Traverser;
import org.neo4j.api.core.Traverser.Order;

import examples.NeoclipseExample;

public class Matrix extends NeoclipseExample
{
    private static Node neoNode;

    @BeforeClass
    public static void copyIcons()
    {
        NeoclipseExample.copyIcons( "matrix" );
    }

    @BeforeClass
    public static void createMatrix()
    {
        Transaction tx = neo.beginTx();
        try
        {
            Node referenceNode = neo.getReferenceNode();
            Node thomas = neo.createNode();
            thomas.setProperty( "name", "Thomas Andersson" );
            thomas.setProperty( "age", 29 );
            neoNode = thomas;
            referenceNode.createRelationshipTo( neoNode,
                MyRelationshipTypes.ROOT );
            Node trinity = neo.createNode();
            trinity.setProperty( "name", "Trinity" );
            Relationship rel = thomas.createRelationshipTo( trinity,
                MyRelationshipTypes.KNOWS );
            rel.setProperty( "age", "3 days" );
            Node morpheus = neo.createNode();
            morpheus.setProperty( "name", "Morpheus" );
            morpheus.setProperty( "rank", "Captain" );
            morpheus.setProperty( "occupation", "Total badass" );
            thomas.createRelationshipTo( morpheus, MyRelationshipTypes.KNOWS );
            rel = morpheus.createRelationshipTo( trinity,
                MyRelationshipTypes.KNOWS );
            rel.setProperty( "age", "12 years" );
            Node cypher = neo.createNode();
            cypher.setProperty( "name", "Cypher" );
            cypher.setProperty( "last name", "Reagan" );
            rel = morpheus.createRelationshipTo( cypher,
                MyRelationshipTypes.KNOWS );
            rel.setProperty( "disclosure", "public" );
            Node smith = neo.createNode();
            smith.setProperty( "name", "Agent Smith" );
            smith.setProperty( "version", "1.0b" );
            smith.setProperty( "language", "C++" );
            rel = cypher
                .createRelationshipTo( smith, MyRelationshipTypes.KNOWS );
            rel.setProperty( "disclosure", "secret" );
            rel.setProperty( "age", "6 months" );
            Node architect = neo.createNode();
            architect.setProperty( "name", "The Architect" );
            smith
                .createRelationshipTo( architect, MyRelationshipTypes.CODED_BY );
            tx.success();
        }
        finally
        {
            tx.finish();
        }
    }

    @Test
    public void printNeoFriends() throws Exception
    {
        Transaction tx = neo.beginTx();
        try
        {
            printFriends( neoNode );
            tx.success();
        }
        finally
        {
            tx.finish();
        }
    }

    @Test
    public void printMatrixHackers() throws Exception
    {
        Transaction tx = neo.beginTx();
        try
        {
            findHackers( neoNode );
            tx.success();
        }
        finally
        {
            tx.finish();
        }
    }

    private static void printFriends( Node person )
    {
        System.out.println( person.getProperty( "name" ) + "'s friends:" );
        Traverser traverser = person.traverse( Order.BREADTH_FIRST,
            StopEvaluator.END_OF_GRAPH,
            ReturnableEvaluator.ALL_BUT_START_NODE, MyRelationshipTypes.KNOWS,
            Direction.OUTGOING );
        for ( Node friend : traverser )
        {
            TraversalPosition position = traverser.currentPosition();
            System.out.println( "At depth " + position.depth() + " => "
                + friend.getProperty( "name" ) );
        }
    }

    private static void findHackers( Node startNode )
    {
        System.out.println( "Hackers:" );
        Traverser traverser = startNode.traverse( Order.BREADTH_FIRST,
            StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator()
            {
                public boolean isReturnableNode(
                    TraversalPosition currentPosition )
                {
                    Relationship rel = currentPosition
                        .lastRelationshipTraversed();
                    if ( rel != null
                        && rel.isType( MyRelationshipTypes.CODED_BY ) )
                    {
                        return true;
                    }
                    return false;
                }
            }, MyRelationshipTypes.CODED_BY, Direction.OUTGOING,
            MyRelationshipTypes.KNOWS, Direction.OUTGOING );
        for ( Node hacker : traverser )
        {
            TraversalPosition position = traverser.currentPosition();
            System.out.println( "At depth " + position.depth() + " => "
                + hacker.getProperty( "name" ) );
        }
    }
}
