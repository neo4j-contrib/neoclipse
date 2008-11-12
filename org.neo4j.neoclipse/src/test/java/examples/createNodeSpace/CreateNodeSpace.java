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
package examples.createNodeSpace;

import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.Transaction;

import examples.NeoclipseExample;

public class CreateNodeSpace extends NeoclipseExample
{
    private static final String NAME = "NAME";

    @BeforeClass
    public static void copyIcons()
    {
        NeoclipseExample.copyIcons( "createNodeSpace" );
    }

    @BeforeClass
    public static void testCreateSimpleNodeSpace()
    {
        Transaction tx = neo.beginTx();
        try
        {
            Node referenceNode = neo.getReferenceNode();
            Node peter = neo.createNode();
            peter.setProperty( NAME, "Peter" );
            peter.setProperty( "int-test", 7 );
            peter.setProperty( "boolean-test", true );
            peter.setProperty( "byte-test", new Byte( "50" ) ); // radix10
            // parsed
            peter.setProperty( "short-test", (short) 50 );
            peter.setProperty( "long-test", 123456789123456789L );
            peter.setProperty( "float-test", 3.54362e-4F );
            peter.setProperty( "double-test", 3.54362e-120D );
            peter.setProperty( "char-test", 'X' );
            peter.setProperty( "String-test", "XYZ" );
            peter.setProperty( "multiline-test", "XYZ\nABC\nDEF\nGHI" );
            Node li = neo.createNode();
            li.setProperty( NAME, "Li" );
            li.setProperty( "int-test", new int[] { 1, 2, 3 } );
            li
                .setProperty( "boolean-test",
                    new boolean[] { true, false, true } );
            li.setProperty( "byte-test", new byte[] { new Byte( "50" ),
                new Byte( "60" ), new Byte( "50" ) } );
            li.setProperty( "short-test", new short[] { (short) 50, (short) 60,
                (short) 50 } );
            li.setProperty( "long-test", new long[] { 123482634623846234L,
                2234872349823742347L, 3234793242387923L } );
            li.setProperty( "float-test", new float[] { 1.0e6F, 3.3e-7F,
                27.327236236F } );
            li.setProperty( "double-test", new double[] { 1.0e6D, 3.3e-7D,
                27.327236236D } );
            li.setProperty( "char-test", new char[] { 'A', 'B', 'C' } );
            li
                .setProperty( "String-test",
                    new String[] { "ABC", "DEF", "GHI" } );
            Node blaff = neo.createNode();
            blaff.setProperty( NAME, "blaff" );
            Node woff = neo.createNode();
            woff.setProperty( NAME, "woff" );
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

    @Test
    public void dummy() throws Exception
    {
    }
}
