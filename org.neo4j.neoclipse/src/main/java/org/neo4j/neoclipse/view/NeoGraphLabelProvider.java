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

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.draw2d.IFigure;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.zest.core.viewers.IConnectionStyleProvider;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.neo4j.api.core.Direction;
import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.Transaction;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.action.ShowNodeColorsAction;
import org.neo4j.neoclipse.action.ShowNodeIconsAction;
import org.neo4j.neoclipse.action.ShowNodeIdsAction;
import org.neo4j.neoclipse.action.ShowNodeNamesAction;
import org.neo4j.neoclipse.action.ShowRelationshipColorsAction;
import org.neo4j.neoclipse.action.ShowRelationshipDirectionsAction;
import org.neo4j.neoclipse.action.ShowRelationshipIdsAction;
import org.neo4j.neoclipse.action.ShowRelationshipTypesAction;
import org.neo4j.neoclipse.decorate.SimpleGraphDecorator;
import org.neo4j.neoclipse.neo.NeoServiceManager;
import org.neo4j.neoclipse.preference.NeoPreferences;

/**
 * Provides the labels for graph elements.
 * @author Peter H&auml;nsgen
 * @author Anders Nawroth
 */
public class NeoGraphLabelProvider extends LabelProvider implements
    IConnectionStyleProvider, IColorProvider
{
    /**
     * Keep track of relationship types display on/off.
     */
    private boolean showRelationshipTypes = ShowRelationshipTypesAction.DEFAULT_STATE;
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
    private boolean showNodeNames = ShowNodeNamesAction.DEFAULT_STATE;
    /**
     * Keep track of node icons display on/off.
     */
    private boolean showNodeIcons = ShowNodeIconsAction.DEFAULT_STATE;
    /**
     * Keep track of node colors display on/off.
     */
    private boolean showNodeColors = ShowNodeColorsAction.DEFAULT_STATE;
    /**
     * Location of node icons.
     */
    private String nodeIconLocation;
    /**
     * Names of properties to look up for node labels.
     */
    private ArrayList<String> nodePropertyNames;
    /**
     * Names of properties to look up for node icon names.
     */
    private ArrayList<String> nodeIconPropertyNames;
    /**
     * Color generator for relationships.
     */
    private SimpleGraphDecorator graphDecorator;
    /**
     * Current instance of the reference node.
     */
    private Node referenceNode;

    public NeoGraphLabelProvider()
    {
        // read all preferences
        readNodeIconLocation();
        readNodePropertyNames();
        readNodeIconPropertyNames();
        // refresh relationship colors
        refreshRelationshipColors();
        NeoServiceManager sm = Activator.getDefault().getNeoServiceManager();
        NeoService ns = sm.getNeoService();
        if ( ns != null )
        {
            Transaction txn = Transaction.begin();
            try
            {
                referenceNode = Activator.getDefault().getNeoServiceManager()
                    .getNeoService().getReferenceNode();
            }
            finally
            {
                txn.finish();
            }
        }
    }

    /**
     * Returns the icon for an element.
     */
    public Image getImage( Object element )
    {
        if ( element instanceof Node )
        {
            Node node = (Node) element;
            if ( showNodeIcons && nodeIconLocation != "" )
            {
                return graphDecorator.getNodeImage( node, referenceNode,
                    nodeIconPropertyNames, nodeIconLocation );
            }
            else
            {
                return graphDecorator.getNodeImage( node, referenceNode );
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
            if ( !showNodeNames || nodePropertyNames.size() == 0 )
            {
                // don't look for the default property
                text = graphDecorator.getNodeText( node, referenceNode );
            }
            else
            {
                // show the default property
                text = graphDecorator.getNodeText( node, referenceNode,
                    nodePropertyNames );
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
                text += graphDecorator.getRelationshipText( rel );
            }
            if ( showRelationshipIds )
            {
                text += " " + String.valueOf( rel.getId() );
            }
            return text;
        }
        return element.toString();
    }

    /**
     * Remove relationship colors, start over creating new ones.
     */
    final public void refreshRelationshipColors()
    {
        graphDecorator = new SimpleGraphDecorator( Arrays.asList(
            Direction.INCOMING, Direction.OUTGOING ) );
    }

    /**
     * Read the location of node icons from preferences.
     */
    final public void readNodeIconLocation()
    {
        nodeIconLocation = Activator.getDefault().getPreferenceStore()
            .getString( NeoPreferences.NODE_ICON_LOCATION );
    }

    /**
     * Read the names of properties to look up for node labels from preferences.
     */
    final public void readNodePropertyNames()
    {
        String names = Activator.getDefault().getPreferenceStore().getString(
            NeoPreferences.NODE_PROPERTY_NAMES ).trim();
        nodePropertyNames = listFromString( names );
    }

    /**
     * Read the names of properties to look up for node icon names from
     * preferences.
     */
    final public void readNodeIconPropertyNames()
    {
        String names = Activator.getDefault().getPreferenceStore().getString(
            NeoPreferences.NODE_ICON_PROPERTY_NAMES ).trim();
        nodeIconPropertyNames = listFromString( names );
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

    public Color getColor( Object rel )
    {
        if ( !showRelationshipColors || !(rel instanceof Relationship) )
        {
            return graphDecorator.getRelationshipColor();
        }
        return graphDecorator.getRelationshipColor( (Relationship) rel );
    }

    public int getConnectionStyle( Object rel )
    {
        if ( showArrows )
        {
            return ZestStyles.CONNECTIONS_DIRECTED;
        }
        return 0;
    }

    public Color getHighlightColor( Object rel )
    {
        return graphDecorator
            .getRelationshipHighlightColor( (Relationship) rel );
    }

    public int getLineWidth( Object rel )
    {
        return -1;
    }

    public IFigure getTooltip( Object entity )
    {
        return null;
    }

    public Color getBackground( Object element )
    {
        if ( element instanceof Node && showNodeColors )
        {
            return graphDecorator.getNodeColor( (Node) element );
        }
        return null;
    }

    public Color getForeground( Object element )
    {
        if ( element instanceof Node )
        {
            return graphDecorator.getNodeForegroundColor( (Node) element );
        }
        return null;
    }

    /**
     * Convert a string containing a comma-separated list of names to a list of
     * strings. Ignores "" as a name.
     * @param names
     *            comma-separated names
     * @return list of names
     */
    private ArrayList<String> listFromString( String names )
    {
        ArrayList<String> list = new ArrayList<String>();
        for ( String name : names.split( "," ) )
        {
            name = name.trim();
            if ( "".equals( name ) )
            {
                continue;
            }
            list.add( name );
        }
        return list;
    }
}
