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
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.Icons;
import org.neo4j.neoclipse.decorate.SimpleGraphDecorator;
import org.neo4j.neoclipse.decorate.SimpleGraphDecorator.Settings;
import org.neo4j.neoclipse.decorate.SimpleGraphDecorator.ViewSettings;
import org.neo4j.neoclipse.preference.DecoratorPreferences;
import org.neo4j.neoclipse.reltype.RelationshipTypeControl;
import org.neo4j.neoclipse.reltype.RelationshipTypeEditingSupport;

/**
 * Provides the labels for graph elements.
 * 
 * @author Peter H&auml;nsgen
 * @author Anders Nawroth
 */
public class NeoGraphLabelProvider extends LabelProvider implements
        IConnectionStyleProvider, IColorProvider, ILabelProvider,
        ITableLabelProvider, ITableColorProvider, InputChangeListener
{
    /**
     * Handler for node/relationship decoration..
     */
    private SimpleGraphDecorator graphDecorator;
    /**
     * Settings for {@link SimpleGraphDecorator}
     */
    private final Settings settings = new Settings();
    /**
     * View settings for {@link SimpleGraphDecorator}
     */
    private final ViewSettings viewSettings = new ViewSettings();
    /**
     * Marked relationships.
     */
    private final Set<Relationship> markedRels = new HashSet<Relationship>();
    /**
     * Marked nodes.
     */
    private final Set<Node> markedNodes = new HashSet<Node>();
    private static final Image CHECKED = Icons.CHECKED.image();
    private static final Image UNCHECKED = Icons.UNCHECKED.image();
    private Node inputNode = null;

    public NeoGraphLabelProvider()
    {
        // read all preferences
        refreshNodeIconLocation();
        refreshNodePropertyNames();
        refreshRelPropertyNames();
        refreshNodeIconPropertyNames();
        // get reference node
        settings.setDirections( Arrays.asList( Direction.INCOMING,
                Direction.OUTGOING ) );
        // refresh relationship colors
        refreshGraphDecorator();
    }

    /**
     * Get the current view settings.
     */
    public ViewSettings getViewSettings()
    {
        return viewSettings;
    }

    /**
     * Check if a node is the reference node.
     * 
     * @param node
     * @return
     */
    private boolean isReferenceNode( final Node node )
    {
        Node referenceNode = Activator.getDefault().getReferenceNode();
        if ( referenceNode == null )
        {
            return false;
        }
        return referenceNode.equals( node );
    }

    /**
     * Check if the current node is the input node.
     * 
     * @param node
     * @return
     */
    private boolean isInputNode( final Node node )
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
    public void inputChange( final Node node )
    {
        inputNode = node;
    }

    /**
     * Mark relationships.
     * 
     * @param rels relationships to mark
     */
    public void addMarkedRels( final Collection<Relationship> rels )
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
     * 
     * @param nodes nodes to mark
     */
    public void addMarkedNodes( final Collection<Node> nodes )
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
    @Override
    public Image getImage( final Object element )
    {
        if ( element instanceof Node )
        {
            Node node = (Node) element;
            if ( viewSettings.isShowNodeIcons()
                 && !"".equals( settings.getNodeIconLocation() ) )
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
    @Override
    public String getText( final Object element )
    {
        if ( element instanceof Node )
        {
            Node node = (Node) element;
            return graphDecorator.getNodeText( node, isReferenceNode( node ) );
        }
        else if ( element instanceof Relationship )
        {
            Relationship rel = (Relationship) element;
            return graphDecorator.getRelationshipText( rel );
        }
        else if ( element instanceof RelationshipTypeControl )
        {
            RelationshipTypeControl typeControl = (RelationshipTypeControl) element;
            return typeControl.getRelType().name();
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
        graphDecorator = new SimpleGraphDecorator( settings, viewSettings );
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
        settings.setNodeIconLocation( Activator.getDefault().getPreferenceStore().getString(
                DecoratorPreferences.NODE_ICON_LOCATION ) );
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
        settings.setNodePropertyNames( Activator.getDefault().getPreferenceStore().getString(
                DecoratorPreferences.NODE_PROPERTY_NAMES ) );
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
        settings.setRelPropertyNames( Activator.getDefault().getPreferenceStore().getString(
                DecoratorPreferences.RELATIONSHIP_PROPERTY_NAMES ) );
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
        settings.setNodeIconPropertyNames( Activator.getDefault().getPreferenceStore().getString(
                DecoratorPreferences.NODE_ICON_PROPERTY_NAMES ) );
    }

    public Color getColor( final Object o )
    {
        if ( o instanceof Relationship )
        {
            Relationship rel = (Relationship) o;
            if ( !viewSettings.isShowRelationshipColors()
                 || !( o instanceof Relationship ) )
            {
                return graphDecorator.getRelationshipColor();
            }
            if ( markedRels.contains( rel ) )
            {
                return graphDecorator.getMarkedRelationshipColor( rel );
            }
            return graphDecorator.getRelationshipColor( rel );
        }
        else if ( o instanceof RelationshipType )
        {
            RelationshipType relType = (RelationshipType) o;
            return graphDecorator.getRelationshipColor( relType );
        }
        return null;
    }

    public int getConnectionStyle( final Object rel )
    {
        int style = 0;
        if ( viewSettings.isShowArrows() )
        {
            style |= ZestStyles.CONNECTIONS_DIRECTED;
        }
        if ( rel instanceof Relationship && markedRels.contains( rel ) )
        {
            style |= graphDecorator.getMarkedRelationshipStyle( rel );
        }
        return style;
    }

    public Color getHighlightColor( final Object rel )
    {
        return graphDecorator.getRelationshipHighlightColor( (Relationship) rel );
    }

    public int getLineWidth( final Object rel )
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

    public IFigure getTooltip( final Object entity )
    {
        // got this working only for rels. use a Label (draw2d).
        return null;
    }

    public Color getBackground( final Object element )
    {
        if ( element instanceof Node && viewSettings.isShowNodeColors() )
        {
            if ( markedNodes.contains( element ) )
            {
                return graphDecorator.getMarkedNodeColor( (Node) element );
            }
            return graphDecorator.getNodeColor( (Node) element );
        }
        return null;
    }

    public Color getForeground( final Object element )
    {
        if ( element instanceof Node )
        {
            Node node = (Node) element;
            return graphDecorator.getNodeForegroundColor( node,
                    isInputNode( node ) );
        }
        return null;
    }

    public Image getColumnImage( final Object element, final int index )
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

    public String getColumnText( final Object element, final int index )
    {
        if ( index == 0 && element instanceof RelationshipTypeControl )
        {
            RelationshipTypeControl control = (RelationshipTypeControl) element;
            return control.getRelType().name();
        }
        return null;
    }

    public Color getBackground( final Object element, final int index )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Color getForeground( final Object element, final int index )
    {
        if ( !viewSettings.isShowRelationshipColors() || index != 0
             || !( element instanceof RelationshipTypeControl ) )
        {
            return graphDecorator.getRelationshipColor();
        }
        RelationshipTypeControl control = (RelationshipTypeControl) element;
        return graphDecorator.getRelationshipColor( control.getRelType() );
    }

    /**
     * Get the relationship types that was decorated.
     * 
     * @return
     */
    public Set<RelationshipType> getRelationshipTypes()
    {
        return graphDecorator.getRelationshipTypes();
    }

    /**
     * Create the table columns of the Relationship types view.
     * 
     * @param tableViewer
     */
    public void createTableColumns( final TableViewer tableViewer )
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
        col.setToolTipText( "Filter incoming relationships of this relationship type." );
        col.setWidth( 60 );
        col.setImage( Icons.INCOMING.image() );
        col.setResizable( true );
        column.setEditingSupport( new RelationshipTypeEditingSupport(
                tableViewer, RelationshipTypeEditingSupport.ColumnType.IN ) );
        column = new TableViewerColumn( tableViewer, SWT.LEFT );
        col = column.getColumn();
        col.setText( "Out" );
        col.setToolTipText( "Filter outgoing relationships of this relationship type." );
        col.setWidth( 60 );
        col.setImage( Icons.OUTGOING.image() );
        col.setResizable( true );
        column.setEditingSupport( new RelationshipTypeEditingSupport(
                tableViewer, RelationshipTypeEditingSupport.ColumnType.OUT ) );
        table.setHeaderVisible( true );
        table.setLinesVisible( true );
    }

    public boolean propertyChanged( final PropertyChangeEvent event )
    {
        String property = event.getProperty();
        if ( DecoratorPreferences.NODE_PROPERTY_NAMES.equals( property ) )
        {
            readNodePropertyNames();
            return true;
        }
        else if ( DecoratorPreferences.RELATIONSHIP_PROPERTY_NAMES.equals( property ) )
        {
            readRelPropertyNames();
            return true;
        }
        else if ( DecoratorPreferences.NODE_ICON_LOCATION.equals( property ) )
        {
            readNodeIconLocation();
            return true;
        }
        else if ( DecoratorPreferences.NODE_ICON_PROPERTY_NAMES.equals( property ) )
        {
            readNodeIconPropertyNames();
            return true;
        }
        return false;
    }
}
