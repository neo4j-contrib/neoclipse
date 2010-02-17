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
package examples.roles;

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
public class Roles extends NeoclipseExample
{
    @BeforeClass
    public static void copyIcons()
    {
        NeoclipseExample.copyIcons( "roles" );
    }

    @BeforeClass
    public static void createRoles()
    {
        Transaction tx = neo.beginTx();
        try
        {
            // add the top level groups
            Node admins = createTopLevelGroup( "Admins" );
            Node users = createTopLevelGroup( "Users" );
            // add other groups
            Node helpDesk = createGroup( "HelpDesk", admins );
            Node managers = createGroup( "Managers", users );
            Node technicians = createGroup( "Technicians", users );
            Node abcTechnicians = createGroup( "ABCTechnicians", technicians );
            // add the users
            createUser( "Ali", admins, users );
            createUser( "Burcu", users );
            createUser( "Can", users );
            createUser( "Demet", helpDesk );
            createUser( "Engin", helpDesk, users );
            createUser( "Fuat", managers );
            createUser( "Gul", managers );
            createUser( "Hakan", technicians );
            createUser( "Irmak", technicians );
            createUser( "Jale", abcTechnicians );
            tx.success();
        }
        finally
        {
            tx.finish();
        }
    }

    private static Node createTopLevelGroup( final String name )
    {
        return createNode( name, RoleRels.ROOT, neo.getReferenceNode() );
    }

    private static Node createGroup( final String name,
        final Node... containedIn )
    {
        return createNode( name, RoleRels.PART_OF, containedIn );
    }

    private static Node createUser( final String name,
        final Node... containedIn )
    {
        return createNode( name, RoleRels.MEMBER_OF, containedIn );
    }

    private static Node createNode( final String name,
        final RelationshipType relType, final Node... containedIn )
    {
        Node node = neo.createNode();
        node.setProperty( "name", name );
        for ( Node parent : containedIn )
        {
            node.createRelationshipTo( parent, relType );
        }
        return node;
    }

    @Test
    public void getAllAdmins()
    {
        Transaction tx = neo.beginTx();
        try
        {
            System.out.println( "All admins:" );
            Node admins = neo.getNodeById( 1 ); // TODO search?
            Traverser traverser = admins.traverse(
                Traverser.Order.BREADTH_FIRST, StopEvaluator.END_OF_GRAPH,
                ReturnableEvaluator.ALL_BUT_START_NODE, RoleRels.PART_OF,
                Direction.INCOMING, RoleRels.MEMBER_OF, Direction.INCOMING );
            for ( Node part : traverser )
            {
                System.out.println( part.getProperty( "name" ) + " "
                    + (traverser.currentPosition().depth() - 1) );
            }
            tx.success();
        }
        finally
        {
            tx.finish();
        }
    }

    @Test
    public void getJalesMemberships() throws Exception
    {
        Transaction tx = neo.beginTx();
        try
        {
            System.out.println( "Jale's memberships:" );
            Node jale = neo.getNodeById( 16 ); // TODO search?
            Traverser traverser = jale.traverse( Traverser.Order.DEPTH_FIRST,
                StopEvaluator.END_OF_GRAPH,
                ReturnableEvaluator.ALL_BUT_START_NODE, RoleRels.MEMBER_OF,
                Direction.OUTGOING, RoleRels.PART_OF, Direction.OUTGOING );
            for ( Node membership : traverser )
            {
                System.out.println( membership.getProperty( "name" ) + " "
                    + (traverser.currentPosition().depth() - 1) );
            }
            tx.success();
        }
        finally
        {
            tx.finish();
        }
    }

    @Test
    public void getAllGroups() throws Exception
    {
        Transaction tx = neo.beginTx();
        try
        {
            System.out.println( "All groups:" );
            Node referenceNode = neo.getReferenceNode();
            Traverser traverser = referenceNode.traverse(
                Traverser.Order.BREADTH_FIRST, StopEvaluator.END_OF_GRAPH,
                ReturnableEvaluator.ALL_BUT_START_NODE, RoleRels.ROOT,
                Direction.INCOMING, RoleRels.PART_OF, Direction.INCOMING );
            for ( Node group : traverser )
            {
                System.out.println( group.getProperty( "name" ) );
            }
            tx.success();
        }
        finally
        {
            tx.finish();
        }
    }

    @Test
    public void getAllMembers() throws Exception
    {
        Transaction tx = neo.beginTx();
        try
        {
            System.out.println( "All members:" );
            Node referenceNode = neo.getReferenceNode();
            Traverser traverser = referenceNode.traverse(
                Traverser.Order.BREADTH_FIRST, StopEvaluator.END_OF_GRAPH,
                ReturnableEvaluator.ALL_BUT_START_NODE, RoleRels.ROOT,
                Direction.INCOMING, RoleRels.PART_OF, Direction.INCOMING,
                RoleRels.MEMBER_OF, Direction.INCOMING );
            for ( Node group : traverser )
            {
                if ( traverser.currentPosition().lastRelationshipTraversed()
                    .isType( RoleRels.MEMBER_OF ) )
                {
                    System.out.println( group.getProperty( "name" ) );
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
