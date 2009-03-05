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
package org.neo4j.neoclipse.neo;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.PropertyContainer;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.RelationshipType;
import org.neo4j.api.core.Transaction;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.view.NeoGraphViewPart;

/**
 * Utility class to handle node space manipulations.
 * @author Anders Nawroth
 */
public class NodeSpaceUtil
{
    private static final String CONFIRM_DELETE_TITLE = "Confirm delete";
    private static final String ADDING_REL_TYPECOUNT_WARNING_MESSAGE = "There has to be exactly one selected relationship type to add a relationship.";
    private static final String ADDING_REL_WARNING_MESSAGE = "Two nodes must be selected in the database graph to add a relationship.";
    private static final String ADDING_REL_WARNING_LABEL = "Adding relationship";
    private static final String ADDING_NODE_WARNING_LABEL = "Adding node";
    private static final String ADDING_NODE_WARNING_MESSAGE = "At least one node must be selected in the database graph to add a new node.";

    private NodeSpaceUtil()
    {
        // no instances
    }

    /**
     * Create a relationship between two nodes
     * @param source
     *            start node of the relationship
     * @param dest
     *            end node of the relationship
     * @param relType
     *            type of relationship
     * @param graphView
     *            current database graph view
     */
    public static void createRelationship( Node source, Node dest,
        RelationshipType relType, NeoGraphViewPart graphView )
    {
        List<Node> sourceNodes = null;
        if ( source != null )
        {
            sourceNodes = new ArrayList<Node>();
            sourceNodes.add( source );
        }
        List<Node> destNodes = null;
        if ( dest != null )
        {
            destNodes = new ArrayList<Node>();
            destNodes.add( dest );
        }
        createRelationship( sourceNodes, destNodes, relType, graphView );
    }

    /**
     * Create relationship between two nodes. One node can be created, but not
     * both
     * @param sourceNodes
     *            source, is created if <code>null</code> is given
     * @param destNodes
     *            destination, is created if <code>null</code> is given
     * @param relType
     *            type of relationship
     * @param graphView
     *            current database graph view
     */
    public static void createRelationship( List<Node> sourceNodes,
        List<Node> destNodes, RelationshipType relType,
        NeoGraphViewPart graphView )
    {
        if ( relType == null )
        {
            throw new IllegalArgumentException(
                "RelationshipType can not be null" );
        }
        if ( sourceNodes == null && destNodes == null )
        {
            throw new IllegalArgumentException(
                "Both soure and destination can not be null" );
        }
        NeoService ns = Activator.getDefault().getNeoServiceSafely();
        if ( ns == null )
        {
            return;
        }
        Transaction tx = ns.beginTx();
        try
        {
            if ( destNodes == null )
            {
                destNodes = new ArrayList<Node>();
                destNodes.add( ns.createNode() );
            }
            else if ( sourceNodes == null )
            {
                sourceNodes = new ArrayList<Node>();
                sourceNodes.add( ns.createNode() );
            }
            for ( Node source : sourceNodes )
            {
                for ( Node dest : destNodes )
                {
                    source.createRelationshipTo( dest, relType );
                }
            }
            tx.success();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        finally
        {
            tx.finish();
        }
        if ( graphView != null )
        {
            graphView.refreshPreserveLayout();
        }
    }

    public static boolean confirmDelete( int count )
    {
        return MessageDialog.openConfirm( null, CONFIRM_DELETE_TITLE,
            "Do you really want to delete the selected " + count + " items?" );
    }

    public static void deletePropertyContainers(
        final List<? extends PropertyContainer> containers,
        final NeoGraphViewPart graphView )
    {
        NeoService ns = Activator.getDefault().getNeoServiceSafely();
        if ( ns == null )
        {
            return;
        }
        Transaction tx = ns.beginTx();
        try
        {
            for ( PropertyContainer container : containers )
            {
                if ( container instanceof Node )
                {
                    Node node = (Node) container;
                    if ( node.equals( ns.getReferenceNode() ) )
                    {
                        boolean confirmation = MessageDialog
                            .openConfirm( null, CONFIRM_DELETE_TITLE,
                                "Do you really, really want to delete the REFERENCE NODE?" );
                        if ( !confirmation )
                        {
                            return;
                        }
                    }
                    for ( Relationship rel : node.getRelationships() )
                    {
                        rel.delete();
                    }
                    node.delete();
                }
                else if ( container instanceof Relationship )
                {
                    ((Relationship) container).delete();
                }
            }
            tx.success();
        }
        catch ( Exception e )
        {
            MessageDialog.openError( null, "Error", "Error when deleting: "
                + e.getMessage() );
        }
        finally
        {
            tx.finish();
        }
        graphView.refreshPreserveLayout();
    }

    public static void addRelationshipAction( List<RelationshipType> relTypes,
        NeoGraphViewPart graphView )
    {
        List<Node> currentSelectedNodes = graphView.getCurrentSelectedNodes();
        if ( currentSelectedNodes.size() != 2 )
        {
            MessageDialog.openWarning( null, ADDING_REL_WARNING_LABEL,
                ADDING_REL_WARNING_MESSAGE );
            return;
        }
        if ( !isOneRelTypeSelected( relTypes ) )
        {
            return;
        }
        RelationshipType relType = relTypes.get( 0 );
        Node source = currentSelectedNodes.get( 0 );
        Node dest = currentSelectedNodes.get( 1 );
        createRelationship( source, dest, relType, graphView );
    }

    public static void addOutgoingNodeAction( List<RelationshipType> relTypes,
        NeoGraphViewPart graphView )
    {
        if ( !isOneRelTypeSelected( relTypes ) )
        {
            return;
        }
        List<Node> currentSelectedNodes = graphView.getCurrentSelectedNodes();
        if ( !isOneOrMoreNodesSelected( currentSelectedNodes ) )
        {
            return;
        }
        RelationshipType relType = relTypes.get( 0 );
        createRelationship( currentSelectedNodes, null, relType, graphView );
    }

    public static void addIncomingNodeAction( List<RelationshipType> relTypes,
        NeoGraphViewPart graphView )
    {
        if ( !isOneRelTypeSelected( relTypes ) )
        {
            return;
        }
        List<Node> currentSelectedNodes = graphView.getCurrentSelectedNodes();
        if ( !isOneOrMoreNodesSelected( currentSelectedNodes ) )
        {
            return;
        }
        RelationshipType relType = relTypes.get( 0 );
        createRelationship( null, currentSelectedNodes, relType, graphView );
    }

    /**
     * Test precondition for operations.
     * @return
     */
    private static boolean isOneRelTypeSelected( List<RelationshipType> relTypes )
    {
        if ( relTypes.size() != 1 )
        {
            MessageDialog.openWarning( null, ADDING_REL_WARNING_LABEL,
                ADDING_REL_TYPECOUNT_WARNING_MESSAGE );
            return false;
        }
        return true;
    }

    /**
     * Test precondition for operations.
     * @return
     */
    private static boolean isOneOrMoreNodesSelected(
        List<Node> currentSelectedNodes )
    {
        if ( currentSelectedNodes.size() < 1 )
        {
            MessageDialog.openWarning( null, ADDING_NODE_WARNING_LABEL,
                ADDING_NODE_WARNING_MESSAGE );
            return false;
        }
        return true;
    }

}
