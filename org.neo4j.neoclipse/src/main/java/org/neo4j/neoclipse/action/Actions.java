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
package org.neo4j.neoclipse.action;

import org.eclipse.jface.action.Action;
import org.neo4j.neoclipse.Icons;

/**
 * Enum that encapsulates labels, tooltips and icons of actions.
 */
public enum Actions
{
    // platform
    PREFERENCES( "Preferences", Icons.PREFERENCES_ENABLED,
            Icons.PREFERENCES_DISABLED ),
    PROPERTIES( "Properties view", Icons.PROPERTIES_ENABLED,
            Icons.PROPERTIES_DISABLED ),
    GRAPH_VIEW( "Graph view", Icons.GRAPH_ENABLED, Icons.GRAPH_DISABLED ),
    RELTYPES_VIEW( "Relationship types view", Icons.TYPES_ENABLED,
            Icons.TYPES_DISABLED ),
    HELP_VIEW( "Help view", Icons.HELP_VIEW ),
    HELP_WINDOW( "Help window", Icons.HELP_WINDOW_ENABLED,
            Icons.HELP_WINDOW_DISABLED ),
    SEARCH( "Search", Icons.SEARCH_ENABLED, Icons.SEARCH_DISABLED ),
    // connect
    START( "Start/Connect database", Icons.START_ENABLED, Icons.START_DISABLED ),
    STOP( "Stop/Disconnect database", Icons.STOP_ENABLED, Icons.STOP_DISABLED ),
    // add rel
    ADD_RELATIONSHIP(
            "Create relationship between two existing nodes",
            "Create a directed relationship from the first selected node to the second.",
            Icons.ADD_ENABLED, Icons.ADD_DISABLED ),
    ADD_INCOMING_ICON( "Set end node icon for type", Icons.ICON_INCOMING ),
    ADD_OUTGOING_ICON( "Set start node icon for type", Icons.ICON_OUTGOING ),
    // add node
    ADD_INCOMING_NODE( "Create new node as start node",
            "Create a new node with a relationship; "
                    + "the new node is the start node of the relationship(s).",
            Icons.ADD_INCOMING_ENABLED, Icons.ADD_INCOMING_DISABLED ),
    ADD_OUTGOING_NODE( "Create new node as end node",
            "Create a new node with a relationship; "
                    + "the new node is the end node of the relationship(s).",
            Icons.ADD_OUTGOING_ENABLED, Icons.ADD_OUTGOING_DISABLED ),
    // add reltype
    NEW_RELATIONSHIP_TYPE( "Create new relationship type",
            "Create a new relationship type.", Icons.NEW_TYPE_ENABLED,
            Icons.NEW_TYPE_DISABLED ),
    // filter
    FILTER_NONE( "Follow no direction",
            "Follow no relationships regardless of direction.",
            Icons.FILTER_ENABLED, Icons.FILTER_DISABLED ),
    FILTER_ALL( "Follow BOTH directions",
            "Follow all relationships regardless of their direction.",
            Icons.FILTER_ENABLED, Icons.FILTER_DISABLED ),
    FILTER_OUTGOING( "Follow OUTGOING direction",
            "Follow only relationships in the outgoing direction.",
            Icons.FILTER_ENABLED, Icons.FILTER_DISABLED ),
    FILTER_INCOMING( "Follow INCOMING direction",
            "Follow only relationships in the incoming direction.",
            Icons.FILTER_ENABLED, Icons.FILTER_DISABLED ),
    // highlight
    HIGHLIGHT_RELATIONSHIPS( "Highlight relationships",
            "Highlight relationships of the selected types.",
            Icons.HIGHLIGHT_ENABLED, Icons.HIGHLIGHT_DISABLED ),
    HIGHLIGHT_INCOMING( "Highlight end nodes",
            "Highlight end nodes for relationships of the selected types.",
            Icons.HIGHLIGHT_INCOMING_ENABLED, Icons.HIGHLIGHT_INCOMING_DISABLED ),
    HIGHLIGHT_OUTGOING( "Highlight start nodes",
            "Highlight start nodes for relationships of the selected types.",
            Icons.HIGHLIGHT_OUTGOING_ENABLED, Icons.HIGHLIGHT_OUTGOING_DISABLED ),
    HIGHLIGHT_CLEAR( "Remove highlighting",
            "Remove all curent highlighting of nodes and relationships.",
            Icons.CLEAR_ENABLED, Icons.CLEAR_DISABLED ),
    // navigation
    GO_BACK( "Go back", Icons.BACK_ENABLED, Icons.BACK_DISABLED ),
    GO_FORWARD( "Go forward", Icons.FORWARD_ENABLED, Icons.FORWARD_DISABLED ),
    REFRESH( "Refresh", Icons.REFRESH ),
    SHOW_REFERENCE_NODE( "Show reference node", Icons.HOME ),
    // edit
    DELETE( "Delete", Icons.DELETE_ENABLED, Icons.DELETE_DISABLED ),
    COMMIT( "Commit", Icons.COMMIT_ENABLED, Icons.COMMIT_DISABLED ),
    ROLLBACK( "Rollback", Icons.ROLLBACK_ENABLED, Icons.ROLLBACK_DISABLED ),
    // view node settings
    NODE_COLORS( "Node colors" ),
    NODE_ICONS( "Node icons" ),
    NODE_ID( "Node id" ),
    NODE_LABELS( "Node labels" ),
    NODE_EXPANDED( "Expanded node mode" ),
    // view relationships settings
    RELATIONSHIP_COLORS( "Relationship colors" ),
    RELATIONSHIP_DIRECTIONS( "Relationship directions" ),
    RELATIONSHIP_ID( "Relationship id" ),
    RELATIONSHIP_LABELS( "Relationship labels" ),
    RELATIONSHIP_EXPANDED( "Expanded relationship mode" ),
    RELATIONSHIP_TYPES( "Relationship types" ),
    // layout
    GRID_LAYOUT( "Grid layout", "Grid layout", Icons.GRID ),
    HORIZONTAL_SHIFT_LAYOUT( "Horizontal shift layout" ),
    HORIZONTAL_TREE_LAYOUT( "Horizontal tree layout" ),
    RADIAL_LAYOUT( "Radial layout", Icons.RADIAL ),
    SPRING_LAYOUT( "Spring layout", Icons.SPRING ),
    TREE_LAYOUT( "Tree layout", Icons.TREE ),
    // traversal depth
    DECREASE_TRAVERSAL_DEPTH( "Decrease traversal depth", Icons.MINUS_ENABLED,
            Icons.MINUS_DISABLED ),
    INCREASE_TRAVERSAL_DEPTH( "Increase traversal depth", Icons.PLUS_ENABLED,
            Icons.PLUS_DISABLED ),
    // zoom
    ZOOM( "Zoom", Icons.ZOOM ),
    // property
    COPY( "Copy", Icons.COPY_ENABLED, Icons.COPY_DISABLED ),
    REMOVE( "Remove", Icons.DELETE_ENABLED, Icons.DELETE_DISABLED ),
    PASTE( "Paste", Icons.PASTE_ENABLED, Icons.PASTE_DISABLED ),
    RENAME( "Rename", Icons.RENAME_ENABLED, Icons.RENAME_DISABLED );

    private final String label;
    private final String tooltip;
    private Icons icon;
    private Icons disabledIcon = null;

    private Actions( final String label, final String tooltip, final Icons icon )
    {
        this.label = label;
        this.tooltip = tooltip;
        this.icon = icon;
    }

    private Actions( final String label, final String tooltip,
            final Icons icon, final Icons disabledIcon )
    {
        this( label, tooltip, icon );
        this.disabledIcon = disabledIcon;
    }

    private Actions( final String label )
    {
        this.label = label;
        this.tooltip = label;
    }

    private Actions( final String label, final Icons icon )
    {
        this( label );
        this.icon = icon;
    }

    private Actions( final String label, final Icons icon,
            final Icons disabledIcon )
    {
        this( label, icon );
        this.disabledIcon = disabledIcon;
    }

    /**
     * Initialize action using default values.
     * 
     * @param action
     */
    public void initialize( final Action action )
    {
        action.setText( label );
        action.setToolTipText( tooltip );
        if ( icon != null )
        {
            action.setImageDescriptor( icon.descriptor() );
        }
        if ( disabledIcon != null )
        {
            action.setDisabledImageDescriptor( disabledIcon.descriptor() );
        }
    }

    /**
     * Get label of action.
     * 
     * @return
     */
    public String label()
    {
        return label;
    }

    /**
     * Icon if this action.
     * 
     * @return icon or null
     */
    public Icons icon()
    {
        return icon;
    }
}
