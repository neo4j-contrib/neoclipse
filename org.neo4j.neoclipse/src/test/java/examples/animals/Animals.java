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
package examples.animals;

import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.Traverser;

import examples.NeoclipseExample;

/**
 * Example on modeling DAGs from Kemal Erdogan,
 * http://www.codeproject.com/KB/database/Modeling_DAGs_on_SQL_DBs.aspx
 * @author Anders Nawroth
 */
public class Animals extends NeoclipseExample
{
    private static final String NAME = "NAME";

    @BeforeClass
    public static void copyIcons()
    {
        NeoclipseExample.copyIcons( "animals" );
    }

    @BeforeClass
    public static void createAnimals()
    {
        Transaction tx = neo.beginTx();
        try
        {
            Node referenceNode = neo.getReferenceNode();
            Node animal = createNode( "Animal", AnimalRels.ANIMAL,
                referenceNode );
            Node pet = createNode( "Pet", AnimalRels.CATEGORY, animal );
            Node livestock = createNode( "Livestock", AnimalRels.CATEGORY,
                animal );
            createNode( "Cat", AnimalRels.SPECIES, pet );
            Node dog = createNode( "Dog", AnimalRels.SPECIES, pet, livestock );
            createNode( "Doberman", AnimalRels.RACE, dog );
            createNode( "Bulldog", AnimalRels.RACE, dog );
            createNode( "Sheep", AnimalRels.SPECIES, pet, livestock );
            createNode( "Cow", AnimalRels.SPECIES, livestock );
            tx.success();
        }
        finally
        {
            tx.finish();
        }
    }

    private static Node createNode( final String name,
        final RelationshipType relType, final Node... containedIn )
    {
        Node node = neo.createNode();
        node.setProperty( NAME, name );
        for ( Node parent : containedIn )
        {
            parent.createRelationshipTo( node, relType );
        }
        return node;
    }

    @Test
    public void getAllLivestock()
    {
        System.out.println( "List of all livestock:" );
        Transaction tx = neo.beginTx();
        try
        {
            Node livestock = neo.getNodeById( 3 );
            Traverser traverser = livestock.traverse(
                Traverser.Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH,
                ReturnableEvaluator.ALL_BUT_START_NODE, AnimalRels.ANIMAL,
                Direction.OUTGOING, AnimalRels.CATEGORY, Direction.OUTGOING,
                AnimalRels.SPECIES, Direction.OUTGOING, AnimalRels.RACE,
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
