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
package examples.roles;

import org.junit.Test;
import org.neo4j.api.core.Direction;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.RelationshipType;
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
public class Roles extends NeoclipseExample
{
    private static final String NT_REFERENCE = "referenceNode";
    private static final String NT_GROUP = "group";
    private static final String NT_USER = "user";
    private static final String NAME = "NAME";
    private static final String NODE_TYPE = "NODE_TYPE";

    @Test
    public void createRoles()
    {
        Transaction tx = Transaction.begin();
        try
        {
            Node referenceNode = neo.getReferenceNode();
            referenceNode.setProperty( NAME, "referenceNode" );
            referenceNode.setProperty( NODE_TYPE, NT_REFERENCE );
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
            createUser( "Gül", managers );
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

    private Node createTopLevelGroup( String name )
    {
        return createNode( name, NT_GROUP, RoleRels.ROOT, neo
            .getReferenceNode() );
    }

    private Node createGroup( String name, Node... containedIn )
    {
        return createNode( name, NT_GROUP, RoleRels.CONTAINS, containedIn );
    }

    private Node createUser( String name, Node... containedIn )
    {
        return createNode( name, NT_USER, RoleRels.CONTAINS, containedIn );
    }

    private Node createNode( String name, String nodeType,
        RelationshipType relType, Node... containedIn )
    {
        Node node = neo.createNode();
        node.setProperty( NAME, name );
        node.setProperty( NODE_TYPE, nodeType );
        for ( Node parent : containedIn )
        {
            parent.createRelationshipTo( node, relType );
        }
        return node;
    }

    @Test
    public void getAllAdmins()
    {
        Transaction tx = Transaction.begin();
        try
        {
            System.out.println( "All admins:" );
            Node admins = neo.getNodeById( 1 ); // TODO search?
            Traverser traverser = admins.traverse(
                Traverser.Order.BREADTH_FIRST, StopEvaluator.END_OF_NETWORK,
                ReturnableEvaluator.ALL_BUT_START_NODE, RoleRels.CONTAINS,
                Direction.OUTGOING );
            for ( Node part : traverser )
            {
                System.out.println( part.getProperty( NAME ) + " "
                    + traverser.currentPosition().depth() );
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
        Transaction tx = Transaction.begin();
        try
        {
            System.out.println( "Jale's memberships:" );
            Node jale = neo.getNodeById( 16 ); // TODO search?
            Traverser traverser = jale.traverse( Traverser.Order.DEPTH_FIRST,
                StopEvaluator.END_OF_NETWORK,
                ReturnableEvaluator.ALL_BUT_START_NODE, RoleRels.CONTAINS,
                Direction.INCOMING );
            for ( Node part : traverser )
            {
                System.out.println( part.getProperty( NAME ) + " "
                    + traverser.currentPosition().depth() );
            }
            tx.success();
        }
        finally
        {
            tx.finish();
        }
    }
}
