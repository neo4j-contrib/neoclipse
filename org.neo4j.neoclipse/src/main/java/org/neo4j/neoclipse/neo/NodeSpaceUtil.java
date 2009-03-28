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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.PropertyContainer;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.RelationshipType;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.property.NeoPropertySheetPage;
import org.neo4j.neoclipse.property.PropertyTransform.PropertyHandler;
import org.neo4j.neoclipse.view.NeoGraphViewPart;

/**
 * Utility class to handle node space manipulations.
 * @author Anders Nawroth
 */
public class NodeSpaceUtil
{
    private static final int OK = 0;
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
    private static void createRelationship( List<Node> sourceNodes,
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
        Node newInputNode = null;
        Node createNode = null;
        try
        {
            if ( destNodes == null )
            {
                destNodes = new ArrayList<Node>();
                createNode = ns.createNode();
                destNodes.add( createNode );
                newInputNode = sourceNodes.get( 0 );
            }
            else if ( sourceNodes == null )
            {
                sourceNodes = new ArrayList<Node>();
                createNode = ns.createNode();
                sourceNodes.add( createNode );
                newInputNode = destNodes.get( 0 );
            }
            for ( Node source : sourceNodes )
            {
                for ( Node dest : destNodes )
                {
                    source.createRelationshipTo( dest, relType );
                }
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        if ( graphView != null )
        {
            graphView.setDirty( true );
            if ( destNodes.size() > 1 || sourceNodes.size() > 1 )
            {
                graphView.setInput( createNode );
            }
            else if ( newInputNode != null )
            {
                graphView.setInput( newInputNode );
            }
            else
            {
                graphView.refreshPreserveLayout();
            }
        }
    }

    /**
     * Ask the user to confirm delete.
     * @param count
     *            numbe rof items to delete
     * @return true on yes to delete
     */
    public static boolean confirmDelete( int count )
    {
        return MessageDialog.openConfirm( null, CONFIRM_DELETE_TITLE,
            "Do you really want to delete the selected " + count + " items?" );
    }

    /**
     * Delete nodes and relationships from database.
     * @param containers
     *            node and relationships
     * @param graphView
     *            the current graph view
     */
    public static void deletePropertyContainers(
        final List<? extends PropertyContainer> containers,
        final NeoGraphViewPart graphView )
    {
        if ( containers.isEmpty() )
        {
            return;
        }

        try
        {
            Node inputNode = graphView.getCurrentNode();
            Node newInputNode = null;
            Iterator<? extends PropertyContainer> iter = containers.iterator();
            while ( iter.hasNext() )
            {
                PropertyContainer container = iter.next();
                if ( container instanceof Node )
                {
                    Node node = (Node) container;
                    if ( node
                        .equals( Activator.getDefault().getReferenceNode() ) )
                    {
                        boolean confirmation = MessageDialog
                            .openConfirm( null, CONFIRM_DELETE_TITLE,
                                "Do you really, really want to delete the REFERENCE NODE?" );
                        if ( !confirmation )
                        {
                            return;
                        }
                    }
                    if ( node.equals( inputNode ) && node.hasRelationship() )
                    {
                        newInputNode = node.getRelationships().iterator()
                            .next().getOtherNode( node );
                    }
                    for ( Relationship rel : node.getRelationships() )
                    {
                        rel.delete();
                    }
                    iter.remove(); // remove from list to not mess up the list
                    node.delete();
                }
                else if ( container instanceof Relationship )
                {
                    ((Relationship) container).delete();
                }
                graphView.setDirty( true );
                if ( newInputNode != null )
                {
                    graphView.setInput( newInputNode );
                }
            }
        }
        catch ( Exception e )
        {
            MessageDialog.openError( null, "Error", "Error when deleting: "
                + e.getMessage() );
        }
    }

    /**
     * Add a relationship between two nodes.
     * @param relTypes
     *            relationships types to use (should only be one item)
     * @param graphView
     *            the current graph view
     */
    public static void addRelationshipAction( List<RelationshipType> relTypes,
        NeoGraphViewPart graphView )
    {
        if ( !isOneRelTypeSelected( relTypes ) )
        {
            return;
        }
        addRelationshipAction( relTypes.get( 0 ), graphView );
    }

    /**
     * Add relationship between the selected two nodes.
     * @param relTypes
     *            relationships types to use (should only be one item)
     * @param graphView
     *            the current graph view
     */
    public static void addRelationshipAction( RelationshipType relType,
        NeoGraphViewPart graphView )
    {
        List<Node> currentSelectedNodes = graphView.getCurrentSelectedNodes();
        if ( currentSelectedNodes.size() != 2 )
        {
            MessageDialog.openWarning( null, ADDING_REL_WARNING_LABEL,
                ADDING_REL_WARNING_MESSAGE );
            return;
        }
        Node source = currentSelectedNodes.get( 0 );
        Node dest = currentSelectedNodes.get( 1 );
        createRelationship( source, dest, relType, graphView );
    }

    /**
     * Add outgoing relationships pointing to a new node.
     * @param relTypes
     *            relationships types to use (should only be one item)
     * @param graphView
     */
    public static void addOutgoingNodeAction( List<RelationshipType> relTypes,
        NeoGraphViewPart graphView )
    {
        if ( !isOneRelTypeSelected( relTypes ) )
        {
            return;
        }
        addOutgoingNodeAction( relTypes.get( 0 ), graphView );
    }

    /**
     * Add outgoing relationships pointing to a new node.
     * @param relType
     *            relationship type to use
     * @param graphView
     *            the current graph view
     */
    public static void addOutgoingNodeAction( RelationshipType relType,
        NeoGraphViewPart graphView )
    {
        List<Node> currentSelectedNodes = graphView.getCurrentSelectedNodes();
        if ( !isOneOrMoreNodesSelected( currentSelectedNodes ) )
        {
            return;
        }
        createRelationship( currentSelectedNodes, null, relType, graphView );
    }

    /**
     * Add incoming relationships coming from a new node.
     * @param relTypes
     *            relationship types to use
     * @param graphView
     */
    public static void addIncomingNodeAction( List<RelationshipType> relTypes,
        NeoGraphViewPart graphView )
    {
        if ( !isOneRelTypeSelected( relTypes ) )
        {
            return;
        }
        addIncomingNodeAction( relTypes.get( 0 ), graphView );
    }

    /**
     * Add incoming relationships coming from a new node.
     * @param relTypes
     *            relationships types to use (should only be one item)
     * @param graphView
     */
    public static void addIncomingNodeAction( RelationshipType relType,
        NeoGraphViewPart graphView )
    {
        List<Node> currentSelectedNodes = graphView.getCurrentSelectedNodes();
        if ( !isOneOrMoreNodesSelected( currentSelectedNodes ) )
        {
            return;
        }
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

    /**
     * Remove a property from Node/Relationship.
     * @param container
     * @param key
     * @param propertySheet
     */
    public static void removeProperty( final PropertyContainer container,
        final String key, final NeoPropertySheetPage propertySheet )
    {
        boolean confirmation = MessageDialog.openConfirm( null,
            "Confirm removal",
            "Do you really want to remove the selected property?" );
        if ( !confirmation )
        {
            return;
        }
        try
        {
            container.removeProperty( key );
        }
        catch ( Exception e )
        {
            MessageDialog.openError( null, "Error", "Error in Neo service: "
                + e.getMessage() );
        }
        propertySheet.fireChangeEvent( container, key );
        propertySheet.refresh();
    }

    /**
     * Add a property to Node/Relationship. The user will be asked for
     * confirmation if the key already exists.
     * @param container
     * @param key
     * @param propertyHandler
     * @param propertySheet
     */
    public static void addProperty( final PropertyContainer container,
        final String key, final PropertyHandler propertyHandler,
        final NeoPropertySheetPage propertySheet )
    {
        if ( container.hasProperty( key ) )
        {
            if ( !MessageDialog.openQuestion( null, "Key exists", "The key \""
                + key
                + "\" already exists, do you want to overwrite the old value?" ) )
            {
                return;
            }
        }
        InputDialog valueInput = new InputDialog( null, "Value entry",
            "Please enter the value of the new property", propertyHandler
                .render( propertyHandler.value() ), propertyHandler
                .getValidator() );
        if ( valueInput.open() != OK && valueInput.getReturnCode() != OK )
        {
            return;
        }
        Object val = null;
        try
        {
            val = propertyHandler.parse( valueInput.getValue() );
        }
        catch ( IOException e )
        {
            MessageDialog.openError( null, "Error message",
                "Error parsing the input value, no changes will be performed." );
            return;
        }
        setProperty( container, key, val, propertySheet );
    }

    /**
     * Set a property value, no questions asked.
     * @param container
     * @param key
     * @param value
     * @param propertySheet
     */
    public static void setProperty( PropertyContainer container,
        final String key, final Object value,
        final NeoPropertySheetPage propertySheet )
    {
        try
        {
            container.setProperty( key, value );
        }
        catch ( Exception e )
        {
            MessageDialog.openError( null, "Error", "Error in Neo service: "
                + e.getMessage() );
            e.printStackTrace();
        }
        propertySheet.fireChangeEvent( container, key );
        propertySheet.refresh();
    }

    /**
     * Rename a property key on Node/Relationship.
     * @param container
     *            Node/Relationship
     * @param key
     *            old key
     * @param newKey
     *            new key
     * @param propertySheet
     */
    public static void renameProperty( PropertyContainer container, String key,
        String newKey, final NeoPropertySheetPage propertySheet )
    {
        try
        {
            container.setProperty( newKey, container.getProperty( key ) );
            container.removeProperty( key );
        }
        catch ( Exception e )
        {
            MessageDialog.openError( null, "Error", "Error in Neo service: "
                + e.getMessage() );
        }
        propertySheet.fireChangeEvent( container, newKey );
        propertySheet.refresh();
    }
}
