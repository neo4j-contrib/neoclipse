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
package org.neo4j.neoclipse.view;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.draw2d.IFigure;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.zest.core.viewers.IConnectionStyleProvider;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.neo4j.api.core.Direction;
import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.RelationshipType;
import org.neo4j.api.core.Transaction;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.NeoIcons;
import org.neo4j.neoclipse.action.decorate.node.ShowNodeColorsAction;
import org.neo4j.neoclipse.action.decorate.node.ShowNodeIconsAction;
import org.neo4j.neoclipse.action.decorate.node.ShowNodeIdsAction;
import org.neo4j.neoclipse.action.decorate.node.ShowNodeLabelAction;
import org.neo4j.neoclipse.action.decorate.rel.ShowRelationshipColorsAction;
import org.neo4j.neoclipse.action.decorate.rel.ShowRelationshipDirectionsAction;
import org.neo4j.neoclipse.action.decorate.rel.ShowRelationshipIdsAction;
import org.neo4j.neoclipse.action.decorate.rel.ShowRelationshipLabelAction;
import org.neo4j.neoclipse.action.decorate.rel.ShowRelationshipTypesAction;
import org.neo4j.neoclipse.decorate.SimpleGraphDecorator;
import org.neo4j.neoclipse.decorate.SimpleGraphDecorator.Settings;
import org.neo4j.neoclipse.preference.NeoPreferences;
import org.neo4j.neoclipse.reltype.RelationshipTypeControl;
import org.neo4j.neoclipse.reltype.RelationshipTypeEditingSupport;

/**
 * Provides the labels for graph elements.
 * @author Peter H&auml;nsgen
 * @author Anders Nawroth
 */
public class NeoGraphLabelProvider extends LabelProvider implements
    IConnectionStyleProvider, IColorProvider, ILabelProvider,
    ITableLabelProvider, ITableColorProvider, InputChangeListener
{
    /**
     * Keep track of relationship types display on/off.
     */
    private boolean showRelationshipTypes = ShowRelationshipTypesAction.DEFAULT_STATE;
    /**
     * Keep track of relationship names display on/off.
     */
    private boolean showRelationshipNames = ShowRelationshipLabelAction.DEFAULT_STATE;
    /**
     * Keep track of relationship id's display on/off.
     */
    private boolean showRelationshipIds = ShowRelationshipIdsAction.DEFAULT_STATE;
    /**
     * Keep track of relationship colors display on/off.
     */
    private boolean showRelationshipColors = ShowRelationshipColorsAction.DEFAULT_STATE;
    /**
     * Keep track of arrows display on/off.
     */
    private boolean showArrows = ShowRelationshipDirectionsAction.DEFAULT_STATE;
    /**
     * Keep track of node id's display on/off.
     */
    private boolean showNodeIds = ShowNodeIdsAction.DEFAULT_STATE;
    /**
     * Keep track of node names display on/off.
     */
    private boolean showNodeNames = ShowNodeLabelAction.DEFAULT_STATE;
    /**
     * Keep track of node icons display on/off.
     */
    private boolean showNodeIcons = ShowNodeIconsAction.DEFAULT_STATE;
    /**
     * Keep track of node colors display on/off.
     */
    private boolean showNodeColors = ShowNodeColorsAction.DEFAULT_STATE;
    /**
     * Handler for node/relationship decoration..
     */
    private SimpleGraphDecorator graphDecorator;
    /**
     * Settings for {@link SimpleGraphDecorator}
     */
    private Settings settings = new Settings();
    /**
     * Marked relationships.
     */
    private Set<Relationship> markedRels = new HashSet<Relationship>();
    /**
     * Marked nodes.
     */
    private Set<Node> markedNodes = new HashSet<Node>();
    private static final Image CHECKED = NeoIcons.CHECKED.image();
    private static final Image UNCHECKED = NeoIcons.UNCHECKED.image();
    private Node referenceNode = null;
    private Node inputNode = null;

    public NeoGraphLabelProvider()
    {
        // read all preferences
        refreshNodeIconLocation();
        refreshNodePropertyNames();
        refreshRelPropertyNames();
        refreshNodeIconPropertyNames();
        // get reference node
        NeoService ns = Activator.getDefault().getNeoServiceSafely();
        if ( ns != null )
        {
            Transaction txn = ns.beginTx();
            try
            {
                referenceNode = ns.getReferenceNode();
            }
            finally
            {
                txn.finish();
            }
        }
        settings.setDirections( Arrays.asList( Direction.INCOMING,
            Direction.OUTGOING ) );
        // refresh relationship colors
        refreshGraphDecorator();
    }

    /**
     * Check if a node is the reference node.
     * @param node
     * @return
     */
    private boolean isReferenceNode( final Node node )
    {
        if ( referenceNode == null )
        {
            return false;
        }
        return referenceNode.equals( node );
    }

    /**
     * Check if the current node is the input node.
     * @param node
     * @return
     */
    private boolean isInputNode( Node node )
    {
        if ( inputNode == null )
        {
            return false;
        }
        return inputNode.equals( node );
    }

    /**
     * Handle change of node in graph view.
     */
    public void inputChange( Node node )
    {
        inputNode = node;
    }

    /**
     * Mark relationships.
     * @param rels
     *            relationships to mark
     */
    public void addMarkedRels( Collection<Relationship> rels )
    {
        markedRels.addAll( rels );
    }

    /**
     * Clear marked relationships.
     */
    public void clearMarkedRels()
    {
        markedRels.clear();
    }

    /**
     * Mark nodes.
     * @param nodes
     *            nodes to mark
     */
    public void addMarkedNodes( Collection<Node> nodes )
    {
        markedNodes.addAll( nodes );
    }

    /**
     * Clear marked nodes.
     */
    public void clearMarkedNodes()
    {
        markedNodes.clear();
    }

    /**
     * Returns the icon for an element.
     */
    public Image getImage( Object element )
    {
        if ( element instanceof Node )
        {
            Node node = (Node) element;
            if ( showNodeIcons && settings.getNodeIconLocation() != "" )
            {
                return graphDecorator.getNodeImageFromProperty( node,
                    isReferenceNode( node ) );
            }
            else
            {
                return graphDecorator.getNodeImage( node,
                    isReferenceNode( node ) );
            }
        }
        return null;
    }

    /**
     * Returns the text for an element.
     */
    public String getText( Object element )
    {
        String text = "";
        if ( element instanceof Node )
        {
            Node node = (Node) element;
            if ( !showNodeNames || settings.getNodePropertyNames().size() == 0 )
            {
                // don't look for the default property
                text = graphDecorator
                    .getNodeText( node, isReferenceNode( node ) );
            }
            else
            {
                // show the default property
                text = graphDecorator.getNodeTextFromProperty( node,
                    isReferenceNode( node ) );
            }
            if ( showNodeIds )
            {
                text += " " + String.valueOf( node.getId() );
            }
            return text;
        }
        else if ( element instanceof Relationship )
        {
            Relationship rel = (Relationship) element;
            if ( showRelationshipTypes )
            {
                text += graphDecorator.getRelationshipTypeText( rel );
            }
            if ( showRelationshipIds )
            {
                text += " " + String.valueOf( rel.getId() );
            }
            if ( showRelationshipNames )
            {
                String names = graphDecorator
                    .getRelationshipNameTextFromProperty( rel );
                if ( names != null )
                {
                    if ( !"".equals( text ) )
                    {
                        text += ", ";
                    }
                    text += names;
                }
            }
            return text;
        }
        return element.toString();
    }

    /**
     * Remove relationship colors, start over creating new ones.
     */
    public void refreshRelationshipColors()
    {
        refreshGraphDecorator();
    }

    private final void refreshGraphDecorator()
    {
        graphDecorator = new SimpleGraphDecorator( settings );
    }

    /**
     * Read the location of node icons from preferences.
     */
    public void readNodeIconLocation()
    {
        refreshNodeIconLocation();
        refreshGraphDecorator();
    }

    private final void refreshNodeIconLocation()
    {
        settings
            .setNodeIconLocation( Activator.getDefault().getPreferenceStore()
                .getString( NeoPreferences.NODE_ICON_LOCATION ) );
    }

    /**
     * Read the names of properties to look up for node labels from preferences.
     */
    public void readNodePropertyNames()
    {
        refreshNodePropertyNames();
        refreshGraphDecorator();
    }

    private final void refreshNodePropertyNames()
    {
        settings.setNodePropertyNames( Activator.getDefault()
            .getPreferenceStore()
            .getString( NeoPreferences.NODE_PROPERTY_NAMES ) );
    }

    /**
     * Read the names of properties to look up for relationship labels from
     * preferences.
     */
    public void readRelPropertyNames()
    {
        refreshRelPropertyNames();
        refreshGraphDecorator();
    }

    private final void refreshRelPropertyNames()
    {
        settings.setRelPropertyNames( Activator.getDefault()
            .getPreferenceStore().getString(
                NeoPreferences.RELATIONSHIP_PROPERTY_NAMES ) );
    }

    /**
     * Read the names of properties to look up for node icon names from
     * preferences.
     */
    public void readNodeIconPropertyNames()
    {
        refreshNodeIconPropertyNames();
        refreshGraphDecorator();
    }

    private final void refreshNodeIconPropertyNames()
    {
        settings.setNodeIconPropertyNames( Activator.getDefault()
            .getPreferenceStore().getString(
                NeoPreferences.NODE_ICON_PROPERTY_NAMES ) );
    }

    /**
     * Show or hide relationship types.
     * @param state
     *            set true to display
     */
    public void setShowRelationshipTypes( boolean state )
    {
        showRelationshipTypes = state;
    }

    /**
     * Show or hide relationship names.
     * @param state
     *            set true to display
     */
    public void setShowRelationshipNames( boolean state )
    {
        showRelationshipNames = state;
    }

    /**
     * Show or hide relationship id's.
     * @param state
     *            set true to display
     */
    public void setShowRelationshipIds( boolean state )
    {
        showRelationshipIds = state;
    }

    /**
     * Show or hide relationship colors.
     * @param state
     *            set true to display
     */
    public void setShowRelationshipColors( boolean state )
    {
        showRelationshipColors = state;
    }

    /**
     * Show or hide arrows.
     * @param state
     *            set true to display
     */
    public void setShowArrows( boolean state )
    {
        showArrows = state;
    }

    /**
     * Show or hide node id's.
     * @param state
     *            set true to display
     */
    public void setShowNodeIds( boolean state )
    {
        showNodeIds = state;
    }

    /**
     * Show or hide names.
     * @param state
     *            set true to display
     */
    public void setShowNodeNames( boolean state )
    {
        showNodeNames = state;
    }

    /**
     * Show or hide node icons.
     * @param state
     *            set true to display
     */
    public void setShowNodeIcons( boolean state )
    {
        showNodeIcons = state;
    }

    /**
     * Show or hide node colors.
     * @param state
     *            set true to display
     */
    public void setShowNodeColors( boolean state )
    {
        showNodeColors = state;
    }

    public Color getColor( Object o )
    {
        Relationship rel = (Relationship) o;
        if ( !showRelationshipColors || !(o instanceof Relationship) )
        {
            return graphDecorator.getRelationshipColor();
        }
        if ( markedRels.contains( rel ) )
        {
            return graphDecorator.getMarkedRelationshipColor( rel );
        }
        return graphDecorator.getRelationshipColor( rel );
    }

    public int getConnectionStyle( Object rel )
    {
        int style = 0;
        if ( showArrows )
        {
            style |= ZestStyles.CONNECTIONS_DIRECTED;
        }
        if ( rel instanceof Relationship && markedRels.contains( rel ) )
        {
            style |= graphDecorator.getMarkedRelationshipStyle( rel );
        }
        return style;
    }

    public Color getHighlightColor( Object rel )
    {
        return graphDecorator
            .getRelationshipHighlightColor( (Relationship) rel );
    }

    public int getLineWidth( Object rel )
    {
        if ( rel instanceof Relationship && markedRels.contains( rel ) )
        {
            return graphDecorator.getMarkedLineWidth();
        }
        else
        {
            return graphDecorator.getLineWidth();
        }
    }

    public IFigure getTooltip( Object entity )
    {
        // got this working only for rels. use a Label (draw2d).
        return null;
    }

    public Color getBackground( Object element )
    {
        if ( element instanceof Node && showNodeColors )
        {
            if ( markedNodes.contains( element ) )
            {
                return graphDecorator.getMarkedNodeColor( (Node) element );
            }
            return graphDecorator.getNodeColor( (Node) element );
        }
        return null;
    }

    public Color getForeground( Object element )
    {
        if ( element instanceof Node )
        {
            Node node = (Node) element;
            return graphDecorator.getNodeForegroundColor( node,
                isInputNode( node ) );
        }
        return null;
    }

    public Image getColumnImage( Object element, int index )
    {
        if ( element instanceof RelationshipTypeControl )
        {
            RelationshipTypeControl control = (RelationshipTypeControl) element;
            if ( index == 1 )
            {
                return control.isIn() ? CHECKED : UNCHECKED;
            }
            if ( index == 2 )
            {
                return control.isOut() ? CHECKED : UNCHECKED;
            }
        }
        return null;
    }

    public String getColumnText( Object element, int index )
    {
        if ( index == 0 && element instanceof RelationshipTypeControl )
        {
            RelationshipTypeControl control = (RelationshipTypeControl) element;
            return control.getRelType().name();
        }
        return null;
    }

    public Color getBackground( Object element, int index )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Color getForeground( Object element, int index )
    {
        if ( !showRelationshipColors || index != 0
            || !(element instanceof RelationshipTypeControl) )
        {
            return graphDecorator.getRelationshipColor();
        }
        RelationshipTypeControl control = (RelationshipTypeControl) element;
        return graphDecorator.getRelationshipColor( control.getRelType() );
    }

    /**
     * Get the relationship types that was decorated.
     * @return
     */
    public Set<RelationshipType> getRelationshipTypes()
    {
        return graphDecorator.getRelationshipTypes();
    }

    /**
     * Create the table columns of the Relationship types view.
     * @param tableViewer
     */
    public void createTableColumns( TableViewer tableViewer )
    {
        Table table = tableViewer.getTable();

        TableViewerColumn column = new TableViewerColumn( tableViewer, SWT.LEFT );
        TableColumn col = column.getColumn();
        col.setText( "Relationship type" );
        col.setWidth( 200 );
        col.setResizable( true );
        column.setEditingSupport( new RelationshipTypeEditingSupport(
            tableViewer, RelationshipTypeEditingSupport.ColumnType.HEADING ) );

        column = new TableViewerColumn( tableViewer, SWT.LEFT );
        col = column.getColumn();
        col.setText( "In" );
        col
            .setToolTipText( "Filter incoming relationships of this relationship type." );
        col.setWidth( 60 );
        col.setImage( NeoIcons.INCOMING.image() );
        col.setResizable( true );
        column.setEditingSupport( new RelationshipTypeEditingSupport(
            tableViewer, RelationshipTypeEditingSupport.ColumnType.IN ) );

        column = new TableViewerColumn( tableViewer, SWT.LEFT );
        col = column.getColumn();
        col.setText( "Out" );
        col
            .setToolTipText( "Filter outgoing relationships of this relationship type." );
        col.setWidth( 60 );
        col.setImage( NeoIcons.OUTGOING.image() );
        col.setResizable( true );
        column.setEditingSupport( new RelationshipTypeEditingSupport(
            tableViewer, RelationshipTypeEditingSupport.ColumnType.OUT ) );

        table.setHeaderVisible( true );
        table.setLinesVisible( true );
    }

    public boolean propertyChanged( PropertyChangeEvent event )
    {
        String property = event.getProperty();
        if ( NeoPreferences.NODE_PROPERTY_NAMES.equals( property ) )
        {
            readNodePropertyNames();
            return true;
        }
        else if ( NeoPreferences.RELATIONSHIP_PROPERTY_NAMES.equals( property ) )
        {
            readRelPropertyNames();
            return true;
        }
        else if ( NeoPreferences.NODE_ICON_LOCATION.equals( property ) )
        {
            readNodeIconLocation();
            return true;
        }
        else if ( NeoPreferences.NODE_ICON_PROPERTY_NAMES.equals( property ) )
        {
            readNodeIconPropertyNames();
            return true;
        }
        return false;
    }
}
