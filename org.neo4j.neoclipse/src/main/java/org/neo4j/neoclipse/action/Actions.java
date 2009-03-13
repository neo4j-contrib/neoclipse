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
import org.neo4j.neoclipse.NeoIcons;

/**
 * Enum that encapsulates labels, tooltips and icons of actions.
 */
public enum Actions
{
    // platform
    PREFERENCES( "Preferences", NeoIcons.PREFERENCES ),
    // add rel
    ADD_RELATIONSHIP(
        "Create relationship",
        "Create a directed relationship from the first selected node to the second.",
        NeoIcons.ADD_ENABLED, NeoIcons.ADD_DISABLED ),
    ADD_INCOMING_ICON( "Set end node icon for type", NeoIcons.ICON_INCOMING ),
    ADD_OUTGOING_ICON( "Set start node icon for type", NeoIcons.ICON_OUTGOING ),
    // add node
    ADD_INCOMING_NODE( "Create node as start node",
        "Create a node with a relationship; "
            + "the new node is the start node of the relationship(s).",
        NeoIcons.ADD_INCOMING ),
    ADD_OUTGOING_NODE( "Create node as end node",
        "Create a node with a relationship; "
            + "the new node is the end node of the relationship(s).",
        NeoIcons.ADD_OUTGOING ),
    // add reltype
    NEW_RELATIONSHIP_TYPE( "Create relationship type",
        "Create a new relationship type.", NeoIcons.NEW_ENABLED,
        NeoIcons.NEW_DISABLED ),
    // highlight
    HIGHLIGHT_RELATIONSHIPS( "Highlight relationships",
        "Highlight all relationships of the selected types.",
        NeoIcons.HIGHLIGHT ),
    HIGHLIGHT_INCOMING( "Highlight end nodes",
        "Highlight end nodes for relationships of the selected types.",
        NeoIcons.HIGHLIGHT_INCOMING ),
    HIGHLIGHT_OUTGOING( "Highlight start nodes",
        "Highlight start nodes for relationships of the selected types.",
        NeoIcons.HIGHLIGHT_OUTGOING ),
    HIGHLIGHT_CLEAR( "Remove highlighting",
        "Remove all curent highlighting of nodes and relationships.",
        NeoIcons.CLEAR_ENABLED, NeoIcons.CLEAR_DISABLED ),
    // navigation
    GO_BACK( "Go back", NeoIcons.BACK_ENABLED, NeoIcons.BACK_DISABLED ),
    GO_FORWARD( "Go forward", NeoIcons.FORWARD_ENABLED,
        NeoIcons.FORWARD_DISABLED ),
    REFRESH( "Refresh", NeoIcons.REFRESH ),
    SHOW_REFERENCE_NODE( "Show reference node", NeoIcons.HOME ),
    // edit
    DELETE( "Delete", NeoIcons.DELETE_ENABLED, NeoIcons.DELETE_DISABLED ),
    // view node settings
    NODE_COLORS( "Node colors" ),
    NODE_ICONS( "Node icons" ),
    NODE_ID( "Node id" ),
    NODE_LABELS( "Node labels" ),
    // view rels settings
    RELATIONSHIP_COLORS( "Relationship colors" ), RELATIONSHIP_DIRECTIONS(
        "Relationship directions" ),
    RELATIONSHIP_ID( "Relationship id" ),
    RELATIONSHIP_LABELS( "Relationship labels" ),
    RELATIONSHIP_TYPES( "Relationship types" ),
    // layout
    GRID_LAYOUT( "Grid layout", "Grid layout", NeoIcons.GRID ),
    HORIZONTAL_SHIFT_LAYOUT( "Horizontal shift layout" ),
    HORIZONTAL_TREE_LAYOUT( "Horizontal tree layout" ), RADIAL_LAYOUT(
        "Radial layout", NeoIcons.RADIAL ), SPRING_LAYOUT( "Spring layout",
        NeoIcons.SPRING ), TREE_LAYOUT( "Tree layout", NeoIcons.TREE ),
    // traversal depth
    DECREASE_TRAVERSAL_DEPTH( "Decrease traversal depth",
        NeoIcons.MINUS_ENABLED, NeoIcons.MINUS_DISABLED ),
    INCREASE_TRAVERSAL_DEPTH( "Increase traversal depth",
        NeoIcons.PLUS_ENABLED, NeoIcons.PLUS_DISABLED ),
    // zoom
    ZOOM( "Zoom", NeoIcons.ZOOM ),
    // property
    COPY( "Copy", NeoIcons.COPY_ENABLED, NeoIcons.COPY_DISABLED ), REMOVE(
        "Remove", NeoIcons.DELETE_ENABLED, NeoIcons.DELETE_DISABLED ), PASTE(
        "Paste", NeoIcons.PASTE_ENABLED, NeoIcons.PASTE_DISABLED ), RENAME(
        "Rename", NeoIcons.RENAME );

    private final String label;
    private final String tooltip;
    private NeoIcons icon;
    private NeoIcons disabledIcon = null;

    private Actions( String label, String tooltip, NeoIcons icon )
    {
        this.label = label;
        this.tooltip = tooltip;
        this.icon = icon;
    }

    private Actions( String label, String tooltip, NeoIcons icon,
        NeoIcons disabledIcon )
    {
        this( label, tooltip, icon );
        this.disabledIcon = disabledIcon;
    }

    private Actions( String label )
    {
        this.label = label;
        this.tooltip = label;
    }

    private Actions( String label, NeoIcons icon )
    {
        this( label );
        this.icon = icon;
    }

    private Actions( String label, NeoIcons icon, NeoIcons disabledIcon )
    {
        this( label, icon );
        this.disabledIcon = disabledIcon;
    }

    /**
     * Initialize action using default values.
     * @param action
     */
    public void initialize( Action action )
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
     * @return
     */
    public String label()
    {
        return label;
    }
}
