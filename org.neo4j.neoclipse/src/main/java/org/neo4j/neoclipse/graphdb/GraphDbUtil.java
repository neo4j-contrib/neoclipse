/**
 * Licensed to Neo Technology under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Neo Technology licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.neo4j.neoclipse.graphdb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.property.NeoPropertySheetPage;
import org.neo4j.neoclipse.property.PropertyTransform.PropertyHandler;
import org.neo4j.neoclipse.view.Dialog;
import org.neo4j.neoclipse.view.ErrorMessage;
import org.neo4j.neoclipse.view.NeoGraphViewPart;

/**
 * Utility class to handle node space manipulations.
 * 
 * @author Anders Nawroth
 */
public class GraphDbUtil
{
    private static final int OK = 0;
    private static final String CONFIRM_DELETE_TITLE = "Confirm delete";
    private static final String ADDING_REL_TYPECOUNT_WARNING_MESSAGE = "There has to be exactly one selected relationship type to add a relationship.";
    private static final String ADDING_REL_WARNING_MESSAGE = "Two nodes must be selected in the database graph to add a relationship.";
    private static final String ADDING_REL_WARNING_LABEL = "Adding relationship";
    private static final String ADDING_NODE_WARNING_LABEL = "Adding node";
    private static final String ADDING_NODE_WARNING_MESSAGE = "At least one node must be selected in the database graph to add a new node.";

    private GraphDbUtil()
    {
        // no instances
    }

    /**
     * Create a relationship between two nodes
     * 
     * @param source start node of the relationship
     * @param dest end node of the relationship
     * @param relType type of relationship
     * @param graphView current database graph view
     */
    public static void createRelationship( final Node source, final Node dest,
            final RelationshipType relType, final NeoGraphViewPart graphView )
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
     * 
     * @param sourceNodes source, is created if <code>null</code> is given
     * @param destNodes destination, is created if <code>null</code> is given
     * @param relType type of relationship
     * @param graphView current database graph view
     * @return
     */
    private static void createRelationship( final List<Node> sourceNodes,
            final List<Node> destNodes, final RelationshipType relType,
            final NeoGraphViewPart graphView )
    {
        try
        {
            Activator.getDefault()
                    .getGraphDbServiceManager()
                    .submitTask( new GraphRunnable()
                    {
                        @Override
                        public void run( final GraphDatabaseService graphDb )
                        {
                            createTheRelationship( sourceNodes, destNodes,
                                    relType, graphView, graphDb );
                        }
                    }, "create relationship" )
                    .get();
        }
        catch ( Exception e )
        {
            ErrorMessage.showDialog( "Create relationship(s)", e );
        }
    }

    private static void createTheRelationship( List<Node> sourceNodes,
            List<Node> destNodes, final RelationshipType relType,
            final NeoGraphViewPart graphView, final GraphDatabaseService graphDb )
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
        if ( graphDb == null )
        {
            throw new IllegalStateException(
                    "No active GraphDatabaseService was found" );
        }
        Node newInputNode = null;
        Node createNode = null;
        try
        {
            if ( destNodes == null )
            {
                destNodes = new ArrayList<Node>();
                createNode = graphDb.createNode();
                destNodes.add( createNode );
                newInputNode = sourceNodes.get( 0 );
            }
            else if ( sourceNodes == null )
            {
                sourceNodes = new ArrayList<Node>();
                createNode = graphDb.createNode();
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
     * Ask the user to confirm delete. Note that this method should only be
     * called from inside the UI thread.
     * 
     * @param count numbe rof items to delete
     * @return true on yes to delete
     */
    public static boolean confirmDelete( final int count )
    {
        return MessageDialog.openConfirm( null, CONFIRM_DELETE_TITLE,
                "Do you really want to delete the selected " + count
                        + " items?" );
    }

    /**
     * Delete nodes and relationships from database.
     * 
     * @param containers node and relationships
     * @param graphView the current graph view
     * @return
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
            Activator.getDefault()
                    .getGraphDbServiceManager()
                    .submitTask( new GraphRunnable()
                    {
                        @Override
                        public void run( final GraphDatabaseService graphDb )
                        {
                            deleteThePropertyContainers( containers, graphView,
                                    graphDb );
                        }
                    }, "delete property containers" )
                    .get();
        }
        catch ( Exception e )
        {
            ErrorMessage.showDialog( "Delete", e );
        }
    }

    private static void deleteThePropertyContainers(
            final List<? extends PropertyContainer> containers,
            final NeoGraphViewPart graphView, final GraphDatabaseService graphDb )
    {
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
                    if ( node.getId() == 0 )
                    {
                        boolean confirmed = MessageDialog.openConfirm( null,
                                CONFIRM_DELETE_TITLE,
                                "Do you really, really want to delete the REFERENCE NODE?" );
                        if ( !confirmed )
                        {
                            return;
                        }
                    }
                    if ( node.equals( inputNode ) && node.hasRelationship() )
                    {
                        newInputNode = node.getRelationships()
                                .iterator()
                                .next()
                                .getOtherNode( node );
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
                    ( (Relationship) container ).delete();
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
            ErrorMessage.showDialog( "Error when deleting", e );
        }
    }

    /**
     * Add a relationship between two nodes.
     * 
     * @param relTypes relationships types to use (should only be one item)
     * @param graphView the current graph view
     */
    public static void addRelationshipAction(
            final List<RelationshipType> relTypes,
            final NeoGraphViewPart graphView )
    {
        if ( !isOneRelTypeSelected( relTypes ) )
        {
            return;
        }
        addRelationshipAction( relTypes.get( 0 ), graphView );
    }

    /**
     * Add relationship between the selected two nodes.
     * 
     * @param relTypes relationships types to use (should only be one item)
     * @param graphView the current graph view
     */
    public static void addRelationshipAction( final RelationshipType relType,
            final NeoGraphViewPart graphView )
    {
        List<Node> currentSelectedNodes = graphView.getCurrentSelectedNodes();
        if ( currentSelectedNodes.size() != 2 )
        {
            Dialog.openWarning( ADDING_REL_WARNING_LABEL,
                    ADDING_REL_WARNING_MESSAGE );
            return;
        }
        Node source = currentSelectedNodes.get( 0 );
        Node dest = currentSelectedNodes.get( 1 );
        createRelationship( source, dest, relType, graphView );
    }

    /**
     * Add outgoing relationships pointing to a new node.
     * 
     * @param relTypes relationships types to use (should only be one item)
     * @param graphView
     */
    public static void addOutgoingNodeAction(
            final List<RelationshipType> relTypes,
            final NeoGraphViewPart graphView )
    {
        if ( !isOneRelTypeSelected( relTypes ) )
        {
            return;
        }
        addOutgoingNodeAction( relTypes.get( 0 ), graphView );
    }

    /**
     * Add outgoing relationships pointing to a new node.
     * 
     * @param relType relationship type to use
     * @param graphView the current graph view
     */
    public static void addOutgoingNodeAction( final RelationshipType relType,
            final NeoGraphViewPart graphView )
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
     * 
     * @param relTypes relationship types to use
     * @param graphView
     */
    public static void addIncomingNodeAction(
            final List<RelationshipType> relTypes,
            final NeoGraphViewPart graphView )
    {
        if ( !isOneRelTypeSelected( relTypes ) )
        {
            return;
        }
        addIncomingNodeAction( relTypes.get( 0 ), graphView );
    }

    /**
     * Add incoming relationships coming from a new node.
     * 
     * @param relTypes relationships types to use (should only be one item)
     * @param graphView
     */
    public static void addIncomingNodeAction( final RelationshipType relType,
            final NeoGraphViewPart graphView )
    {
        List<Node> currentSelectedNodes = graphView.getCurrentSelectedNodes();
        if ( !isOneOrMoreNodesSelected( currentSelectedNodes ) )
        {
            return;
        }
        createRelationship( null, currentSelectedNodes, relType, graphView );
    }

    /**
     * Add loop to a node.
     * 
     * @param relTypes relationships types to use (should only be one item)
     * @param graphView
     */
    public static void addLoopNodeAction( final RelationshipType relType,
            final NeoGraphViewPart graphView )
    {
        List<Node> currentSelectedNodes = graphView.getCurrentSelectedNodes();
        if ( !isOneOrMoreNodesSelected( currentSelectedNodes ) )
        {
            return;
        }
        List<Node> node = Collections.singletonList( currentSelectedNodes.get( 0 ) );
        createRelationship( node, node, relType, graphView );
    }

    /**
     * Test precondition for operations.
     * 
     * @return
     */
    private static boolean isOneRelTypeSelected(
            final List<RelationshipType> relTypes )
    {
        if ( relTypes.size() != 1 )
        {
            Dialog.openWarning( ADDING_REL_WARNING_LABEL,
                    ADDING_REL_TYPECOUNT_WARNING_MESSAGE );
            return false;
        }
        return true;
    }

    /**
     * Test precondition for operations.
     * 
     * @return
     */
    private static boolean isOneOrMoreNodesSelected(
            final List<Node> currentSelectedNodes )
    {
        if ( currentSelectedNodes.size() < 1 )
        {
            Dialog.openWarning( ADDING_NODE_WARNING_LABEL,
                    ADDING_NODE_WARNING_MESSAGE );
            return false;
        }
        return true;
    }

    /**
     * Remove a property from Node/Relationship.
     * 
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
            Activator.getDefault()
                    .getGraphDbServiceManager()
                    .submitTask( new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            container.removeProperty( key );
                        }
                    }, "removing a property" );
        }
        catch ( Exception e )
        {
            Dialog.openError( "Error",
                    "Error in Neo service: " + e.getMessage() );
        }
        stateChanged( container, key, true, propertySheet );
    }

    /**
     * Add a property to Node/Relationship. The user will be asked for
     * confirmation if the key already exists.
     * 
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
            if ( !MessageDialog.openQuestion(
                    null,
                    "Key exists",
                    "The key \""
                            + key
                            + "\" already exists, do you want to overwrite the old value?" ) )
            {
                return;
            }
        }
        InputDialog valueInput = new InputDialog( null, "Value entry",
                "Please enter the value of the new property",
                propertyHandler.render( propertyHandler.value() ),
                propertyHandler.getValidator() );
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
            Dialog.openError( "Error message",
                    "Error parsing the input value, no changes will be performed." );
            return;
        }
        if ( setTheProperty( container, key, val ) )
        {
            stateChanged( container, key, true, propertySheet );
        }
    }

    /**
     * Set a property value, no questions asked.
     * 
     * @param container
     * @param key
     * @param value
     * @param propertySheet
     */
    public static void setProperty( final PropertyContainer container,
            final String key, final Object value,
            final NeoPropertySheetPage propertySheet )
    {
        if ( setTheProperty( container, key, value ) )
        {
            stateChanged( container, key, false, propertySheet );
        }
    }

    private static boolean setTheProperty( final PropertyContainer container,
            final String key, final Object value )
    {
        try
        {
            Activator.getDefault()
                    .getGraphDbServiceManager()
                    .submitTask( new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            container.setProperty( key, value );
                        }
                    }, "set property" )
                    .get();
        }
        catch ( Exception e )
        {
            ErrorMessage.showDialog( "Set property", e );
            return false;
        }
        return true;
    }

    private static void stateChanged( final PropertyContainer container,
            final String key, final boolean refresh,
            final NeoPropertySheetPage propertySheet )
    {
        propertySheet.fireChangeEvent( container, key, refresh );
    }

    /**
     * Rename a property key on Node/Relationship.
     * 
     * @param container Node/Relationship
     * @param key old key
     * @param newKey new key
     * @param propertySheet
     */
    public static void renameProperty( final PropertyContainer container,
            final String key, final String newKey,
            final NeoPropertySheetPage propertySheet )
    {
        try
        {
            Activator.getDefault()
                    .getGraphDbServiceManager()
                    .submitTask( new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            container.setProperty( newKey,
                                    container.getProperty( key ) );
                            container.removeProperty( key );
                        }
                    }, "rename property" )
                    .get();
        }
        catch ( Exception e )
        {
            ErrorMessage.showDialog( "Rename property", e );
        }
        stateChanged( container, newKey, true, propertySheet );
    }

    public static Object getProperty( final PropertyContainer container,
            final String key )
    {
        try
        {
            return Activator.getDefault()
                    .getGraphDbServiceManager()
                    .submitTask( new Callable<Object>()
                    {
                        @Override
                        public Object call() throws Exception
                        {
                            return container.getProperty( key, null );
                        }
                    }, "get property" )
                    .get();
        }
        catch ( Exception e )
        {
            ErrorMessage.showDialog( "Get property", e );
        }
        return null;
    }

    public static Map<String, Object> getProperties(
            final PropertyContainer container )
    {
        try
        {
            return Activator.getDefault()
                    .getGraphDbServiceManager()
                    .submitTask( new Callable<Map<String, Object>>()
                    {
                        @Override
                        public Map<String, Object> call() throws Exception
                        {
                            Map<String, Object> props = new HashMap<String, Object>();
                            for ( String key : container.getPropertyKeys() )
                            {
                                props.put( key, container.getProperty( key ) );
                            }
                            return props;
                        }
                    }, "get properties" )
                    .get();
        }
        catch ( Exception e )
        {
            ErrorMessage.showDialog( "Get properties", e );
        }
        return null;
    }

    public static Map<String, Object> getProperties(
            final PropertyContainer container, final List<String> propertyKeys )
    {
        try
        {
            return Activator.getDefault()
                    .getGraphDbServiceManager()
                    .submitTask( new Callable<Map<String, Object>>()
                    {
                        @Override
                        public Map<String, Object> call() throws Exception
                        {
                            Map<String, Object> props = new HashMap<String, Object>();
                            for ( String key : propertyKeys )
                            {
                                if ( container.hasProperty( key ) )
                                {
                                    props.put( key, container.getProperty( key ) );
                                }
                            }
                            return props;
                        }
                    }, "get properties" )
                    .get();
        }
        catch ( Exception e )
        {
            ErrorMessage.showDialog( "Get properties", e );
        }
        return null;
    }

    /**
     * Get all relationships from the database. Note that relationship types not
     * more in use can show up in the result.
     * 
     * @param graphDb the graphdb instance
     * @return the relationship types
     */
    public static Set<RelationshipType> getRelationshipTypesFromDb(
            final GraphDatabaseService graphDb )
    {
        if ( graphDb == null )
        {
            return Collections.emptySet();
        }
        Set<RelationshipType> relationshipTypes;
        relationshipTypes = new HashSet<RelationshipType>();
        for ( RelationshipType relType : graphDb.getRelationshipTypes() )
        {
            relationshipTypes.add( relType );
        }
        return relationshipTypes;
    }
}
