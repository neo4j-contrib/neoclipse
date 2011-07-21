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
package org.neo4j.neoclipse.decorate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.Icons;
import org.neo4j.neoclipse.graphdb.GraphDbServiceManager;
import org.neo4j.neoclipse.graphdb.GraphDbUtil;
import org.neo4j.neoclipse.preference.DecoratorPreferences;
import org.neo4j.neoclipse.property.PropertyTransform;
import org.neo4j.neoclipse.property.PropertyTransform.PropertyHandler;
import org.neo4j.neoclipse.view.ErrorMessage;

public class SimpleGraphDecorator
{
    /**
     * The icon for nodes.
     */
    private static final Image nodeImage = Icons.NEO.image();
    /**
     * The icon for the root node.
     */
    private static final Image rootImage = Icons.NEO_ROOT.image();
    /**
     * User icons for nodes.
     */
    private final UserIcons userIcons;
    /**
     * Default relationship "color" (gray).
     */
    private static final Color RELATIONSHIP_COLOR = new Color(
            Display.getDefault(), new RGB( 70, 70, 70 ) );
    /**
     * Default node background color.
     */
    private static final Color NODE_BACKGROUND_COLOR = new Color(
            Display.getDefault(), new RGB( 255, 255, 255 ) );
    /**
     * Color of node foreground/text.
     */
    private static final Color NODE_FOREGROUND_COLOR = new Color(
            Display.getDefault(), new RGB( 0, 0, 0 ) );
    /**
     * Color of node foreground/text.
     */
    private static final Color INPUTNODE_FOREGROUND_COLOR = new Color(
            Display.getDefault(), new RGB( 60, 60, 200 ) );
    /**
     * Highlight color for relationships.
     */
    private static final Color HIGHLIGHTED_RELATIONSHIP_COLOR = new Color(
            Display.getDefault(), new RGB( 0, 0, 0 ) );
    /**
     * Map colors to relationship types.
     */
    private final RelationshipTypeColorMapper colorMapper;
    /**
     * Settings for this decorator.
     */
    private final Settings settings;
    /**
     * View settings for this decorator.
     */
    private final ViewSettings viewSettings;

    public static class Settings
    {
        /**
         * List defining order of relationship lookups for nodes.
         */
        private List<Direction> directions;
        /**
         * Property names to look for in nodes.
         */
        private List<String> nodePropertyNames;
        /**
         * property names to look for in relationships.
         */
        private List<String> relPropertyNames;
        /**
         * Properties to look for icon information in.
         */
        private List<String> nodeIconPropertyNames;
        /**
         * Current location of icons.
         */
        private String nodeIconLocation;

        public List<Direction> getDirections()
        {
            return directions;
        }

        public void setDirections( final List<Direction> directions )
        {
            this.directions = directions;
        }

        public String getNodeIconLocation()
        {
            return nodeIconLocation;
        }

        public void setNodeIconLocation( final String nodeIconLocation )
        {
            this.nodeIconLocation = nodeIconLocation;
        }

        public List<String> getNodePropertyNames()
        {
            return nodePropertyNames;
        }

        public void setNodePropertyNames( final String nodePropertyNames )
        {
            this.nodePropertyNames = listFromString( nodePropertyNames );
        }

        public List<String> getRelPropertyNames()
        {
            return relPropertyNames;
        }

        public void setRelPropertyNames( final String relPropertyNames )
        {
            this.relPropertyNames = listFromString( relPropertyNames );
        }

        public List<String> getNodeIconPropertyNames()
        {
            return nodeIconPropertyNames;
        }

        public void setNodeIconPropertyNames( final String nodeIconPropertyNames )
        {
            this.nodeIconPropertyNames = listFromString( nodeIconPropertyNames );
        }

        /**
         * Convert a string containing a comma-separated list of names to a list
         * of strings. Ignores "" as a name.
         * 
         * @param names comma-separated names
         * @return list of names
         */
        public static List<String> listFromString( final String names )
        {
            final List<String> list = new ArrayList<String>();
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

    public static class ViewSettings
    {
        /**
         * Keep track of relationship types display on/off.
         */
        private boolean showRelationshipTypes;
        /**
         * Keep track of relationship names display on/off.
         */
        private boolean showRelationshipNames;
        /**
         * Keep track of relationship properties display on/off.
         */
        private boolean showRelationshipPropertyKeys;
        /**
         * Keep track of relationship id's display on/off.
         */
        private boolean showRelationshipIds;
        /**
         * Keep track of relationship colors display on/off.
         */
        private boolean showRelationshipColors;
        /**
         * Keep track of arrows display on/off.
         */
        private boolean showArrows;
        /**
         * Keep track of node id's display on/off.
         */
        private boolean showNodeIds;
        /**
         * Keep track of node names display on/off.
         */
        private boolean showNodeNames;
        /**
         * Keep track of node icons display on/off.
         */
        private boolean showNodeIcons;
        /**
         * Keep track of node colors display on/off.
         */
        private boolean showNodeColors;
        /**
         * Current preference store.
         */
        private final IPreferenceStore preferenceStore;
        private boolean showNodePropertyKeys;
        private boolean filterNodeProperties;
        private boolean filterRelationshipProperties;

        /**
         * Create instance, load values from preference store.
         */
        public ViewSettings()
        {
            preferenceStore = Activator.getDefault()
                    .getPreferenceStore();
            showRelationshipTypes = preferenceStore.getBoolean( DecoratorPreferences.SHOW_RELATIONSHIP_TYPES );
            showRelationshipNames = preferenceStore.getBoolean( DecoratorPreferences.SHOW_RELATIONSHIP_NAMES );
            showRelationshipPropertyKeys = preferenceStore.getBoolean( DecoratorPreferences.SHOW_RELATIONSHIP_PROPERTY_KEYS );
            showRelationshipIds = preferenceStore.getBoolean( DecoratorPreferences.SHOW_RELATIONSHIP_IDS );
            showRelationshipColors = preferenceStore.getBoolean( DecoratorPreferences.SHOW_RELATIONSHIP_COLORS );
            showArrows = preferenceStore.getBoolean( DecoratorPreferences.SHOW_ARROWS );
            showNodeIds = preferenceStore.getBoolean( DecoratorPreferences.SHOW_NODE_IDS );
            showNodeNames = preferenceStore.getBoolean( DecoratorPreferences.SHOW_NODE_NAMES );
            showNodeIcons = preferenceStore.getBoolean( DecoratorPreferences.SHOW_NODE_ICONS );
            showNodeColors = preferenceStore.getBoolean( DecoratorPreferences.SHOW_NODE_COLORS );
            showNodePropertyKeys = preferenceStore.getBoolean( DecoratorPreferences.SHOW_NODE_PROPERTY_KEYS );
            filterNodeProperties = preferenceStore.getBoolean( DecoratorPreferences.FILTER_NODE_PROPERTIES );
            filterRelationshipProperties = preferenceStore.getBoolean( DecoratorPreferences.FILTER_RELATIONSHIP_PROPERTIES );
        }

        public boolean isShowRelationshipTypes()
        {
            return showRelationshipTypes;
        }

        public void setShowRelationshipTypes(
                final boolean showRelationshipTypes )
        {
            this.showRelationshipTypes = showRelationshipTypes;
            preferenceStore.setValue(
                    DecoratorPreferences.SHOW_RELATIONSHIP_TYPES,
                    showRelationshipTypes );
        }

        public boolean isShowRelationshipNames()
        {
            return showRelationshipNames;
        }

        public void setShowRelationshipNames(
                final boolean showRelationshipNames )
        {
            this.showRelationshipNames = showRelationshipNames;
            preferenceStore.setValue(
                    DecoratorPreferences.SHOW_RELATIONSHIP_NAMES,
                    showRelationshipNames );
        }

        public boolean isShowRelationshipPropertyKeys()
        {
            return showRelationshipPropertyKeys;
        }

        public void setShowRelationshipPropertyKeys(
                final boolean showRelationshipPropertyKeys )
        {
            this.showRelationshipPropertyKeys = showRelationshipPropertyKeys;
            preferenceStore.setValue(
                    DecoratorPreferences.SHOW_RELATIONSHIP_PROPERTY_KEYS,
                    showRelationshipPropertyKeys );
        }

        public boolean isFilterRelationshipProperties()
        {
            return filterRelationshipProperties;
        }

        public void setFilterRelationshipProperties(
                final boolean filterRelationshipProperties )
        {
            this.filterRelationshipProperties = filterRelationshipProperties;
            preferenceStore.setValue(
                    DecoratorPreferences.FILTER_RELATIONSHIP_PROPERTIES,
                    filterRelationshipProperties );
        }

        public boolean isShowRelationshipIds()
        {
            return showRelationshipIds;
        }

        public void setShowRelationshipIds( final boolean showRelationshipIds )
        {
            this.showRelationshipIds = showRelationshipIds;
            preferenceStore.setValue(
                    DecoratorPreferences.SHOW_RELATIONSHIP_IDS,
                    showRelationshipIds );
        }

        public boolean isShowRelationshipColors()
        {
            return showRelationshipColors;
        }

        public void setShowRelationshipColors(
                final boolean showRelationshipColors )
        {
            this.showRelationshipColors = showRelationshipColors;
            preferenceStore.setValue(
                    DecoratorPreferences.SHOW_RELATIONSHIP_COLORS,
                    showRelationshipColors );
        }

        public boolean isShowArrows()
        {
            return showArrows;
        }

        public void setShowArrows( final boolean showArrows )
        {
            this.showArrows = showArrows;
            preferenceStore.setValue( DecoratorPreferences.SHOW_ARROWS,
                    showArrows );
        }

        public boolean isShowNodeIds()
        {
            return showNodeIds;
        }

        public void setShowNodeIds( final boolean showNodeIds )
        {
            this.showNodeIds = showNodeIds;
            preferenceStore.setValue( DecoratorPreferences.SHOW_NODE_IDS,
                    showNodeIds );
        }

        public boolean isShowNodeNames()
        {
            return showNodeNames;
        }

        public void setShowNodeNames( final boolean showNodeNames )
        {
            this.showNodeNames = showNodeNames;
            preferenceStore.setValue( DecoratorPreferences.SHOW_NODE_NAMES,
                    showNodeNames );
        }

        public boolean isShowNodePropertyKeys()
        {
            return showNodePropertyKeys;
        }

        public void setShowNodePropertyKeys( final boolean showNodePropertyKeys )
        {
            this.showNodePropertyKeys = showNodePropertyKeys;
            preferenceStore.setValue(
                    DecoratorPreferences.SHOW_NODE_PROPERTY_KEYS,
                    showNodePropertyKeys );
        }

        public boolean isFilterNodeProperties()
        {
            return filterNodeProperties;
        }

        public void setFilterNodeProperties( final boolean filterNodeProperties )
        {
            this.filterNodeProperties = filterNodeProperties;
            preferenceStore.setValue(
                    DecoratorPreferences.FILTER_NODE_PROPERTIES,
                    filterNodeProperties );
        }

        public boolean isShowNodeIcons()
        {
            return showNodeIcons;
        }

        public void setShowNodeIcons( final boolean showNodeIcons )
        {
            this.showNodeIcons = showNodeIcons;
            preferenceStore.setValue( DecoratorPreferences.SHOW_NODE_ICONS,
                    showNodeIcons );
        }

        public boolean isShowNodeColors()
        {
            return showNodeColors;
        }

        public void setShowNodeColors( final boolean showNodeColors )
        {
            this.showNodeColors = showNodeColors;
            preferenceStore.setValue( DecoratorPreferences.SHOW_NODE_COLORS,
                    showNodeColors );
        }
    }

    public SimpleGraphDecorator( final Settings settings,
            final ViewSettings viewSettings )
    {
        if ( settings.getDirections() == null )
        {
            throw new IllegalArgumentException( "Null directions list given." );
        }
        if ( settings.getDirections()
                .isEmpty() )
        {
            throw new IllegalArgumentException( "Empty directions list given." );
        }
        this.settings = settings;
        this.viewSettings = viewSettings;
        colorMapper = new RelationshipTypeColorMapper( ColorCategory.values() );
        userIcons = new UserIcons( settings.getNodeIconLocation() );
    }

    public Color getNodeColor()
    {
        return NODE_BACKGROUND_COLOR;
    }

    public Color getNodeColor( final Node node )
    {
        return getNodeColor( node, false );
    }

    /**
     * Get color of node.
     * 
     * @param node
     * @param marked true if the node is marked
     * @return
     */
    private Color getNodeColor( final Node node, final boolean marked )
    {
        GraphDbServiceManager gsm = Activator.getDefault()
                .getGraphDbServiceManager();
        try
        {
            return gsm.submitTask( new Callable<Color>()
            {
                @Override
                public Color call() throws Exception
                {
                    Relationship randomRel = null;
                    Direction randomDir = null;
                    for ( Direction direction : settings.getDirections() )
                    {
                        for ( Relationship rel : node.getRelationships( direction ) )
                        {
                            RelationshipType type = rel.getType();
                            if ( !colorMapper.colorExists( type ) )
                            {
                                if ( randomRel == null )
                                {
                                    randomRel = rel;
                                    randomDir = direction;
                                }
                                continue;
                            }
                            else
                            {
                                return getColorFromDirection( type, direction,
                                        marked );
                            }
                        }
                    }
                    if ( randomRel != null )
                    {
                        return getColorFromDirection( randomRel.getType(),
                                randomDir, marked );
                    }
                    return getNodeColor();
                }
            }, "get node color" )
                    .get();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get color connected to relationship type depending on direction and if
     * it's marked or not.
     * 
     * @param type
     * @param direction
     * @param marked
     * @return
     */
    private Color getColorFromDirection( final RelationshipType type,
            final Direction direction, final boolean marked )
    {
        switch ( direction )
        {
        case INCOMING:
            if ( marked )
            {
                return colorMapper.getColor( type,
                        ColorCategory.NODE_INCOMING_MARKED );
            }
            else
            {
                return colorMapper.getColor( type, ColorCategory.NODE_INCOMING );
            }
        case OUTGOING:
            if ( marked )
            {
                return colorMapper.getColor( type,
                        ColorCategory.NODE_OUTGOING_MARKED );
            }
            else
            {
                return colorMapper.getColor( type, ColorCategory.NODE_OUTGOING );
            }
        default:
            if ( marked )
            {
                return colorMapper.getColor( type,
                        ColorCategory.RELATIONSHIP_MARKED );
            }
            else
            {
                return colorMapper.getColor( type, ColorCategory.RELATIONSHIP );
            }
        }
    }

    public Color getRelationshipColor()
    {
        return RELATIONSHIP_COLOR;
    }

    public Color getRelationshipColor( final Relationship rel )
    {
        return colorMapper.getColor( rel.getType(), ColorCategory.RELATIONSHIP );
    }

    public Color getRelationshipColor( final RelationshipType relType )
    {
        return colorMapper.getColor( relType, ColorCategory.RELATIONSHIP );
    }

    private String getSimpleNodeText( final Node node,
            final boolean isReferenceNode )
    {
        if ( isReferenceNode )
        {
            return "Reference Node";
        }
        else
        {
            return "Node";
        }
    }

    public String getNodeText( final Node node, final boolean isReferenceNode )
    {
        if ( viewSettings.isShowNodeNames() )
        {
            if ( viewSettings.isShowNodePropertyKeys() )
            {
                if ( viewSettings.isFilterNodeProperties()
                     && !settings.getNodePropertyNames()
                             .isEmpty() )
                {
                    return readPropertiesWithKeys( node,
                            settings.getNodePropertyNames(),
                            viewSettings.isShowNodeIds() );
                }
                else
                {
                    return readPropertiesWithKeys( node,
                            viewSettings.isShowNodeIds() );
                }
            }
            else
            {
                if ( viewSettings.isFilterNodeProperties()
                     && !settings.getNodePropertyNames()
                             .isEmpty() )
                {
                    return readProperties( node,
                            settings.getNodePropertyNames(),
                            viewSettings.isShowNodeIds() );
                }
                else
                {
                    return readProperties( node, viewSettings.isShowNodeIds() );
                }
            }
        }
        else
        {
            // don't look for properties
            StringBuilder str = new StringBuilder( 48 );
            str.append( getSimpleNodeText( node, isReferenceNode ) );
            if ( viewSettings.isShowNodeIds() )
            {
                if ( str.length() > 0 )
                {
                    str.append( ", " );
                }
                str.append( node.getId() );
            }
            return str.toString();
        }
    }

    private String readProperties( final PropertyContainer container,
            final List<String> propertyNames, final boolean includeId )
    {
        Map<String, Object> props = GraphDbUtil.getProperties( container,
                propertyNames );
        return readPropertyValues( container, props, includeId );
    }

    private String readProperties( final PropertyContainer container,
            final boolean includeId )
    {
        Map<String, Object> props = GraphDbUtil.getProperties( container );
        return readPropertyValues( container, props, includeId );
    }

    private String readPropertyValues( final PropertyContainer container,
            final Map<String, Object> props, final boolean includeId )
    {
        List<String> values = new ArrayList<String>();
        for ( Object propertyValue : props.values() )
        {
            if ( propertyValue == null )
            {
                continue;
            }
            if ( propertyValue instanceof String )
            {
                if ( ( (String) propertyValue ).length() > 0 )
                {
                    values.add( (String) propertyValue );
                }
            }
            else
            {
                // get a proper String from other types
                String render = PropertyTransform.render( propertyValue );
                values.add( render );
            }
        }
        if ( includeId )
        {
            if ( container instanceof Node )
            {
                values.add( String.valueOf( ( (Node) container ).getId() ) );
            }
            else if ( container instanceof Relationship )
            {
                values.add( String.valueOf( ( (Relationship) container ).getId() ) );
            }
        }
        if ( values.size() > 0 )
        {
            String result = values.toString();
            return result.substring( 1, result.length() - 1 );
        }
        return null;
    }

    private String readPropertiesWithKeys( final PropertyContainer container,
            final boolean includeId )
    {
        Map<String, Object> props = GraphDbUtil.getProperties( container );
        return readPropertiesAndKeys( container, includeId, props );
    }

    private String readPropertiesWithKeys( final PropertyContainer container,
            List<String> propertyKeys, final boolean includeId )
    {
        Map<String, Object> props = GraphDbUtil.getProperties( container,
                propertyKeys );
        return readPropertiesAndKeys( container, includeId, props );
    }

    private String readPropertiesAndKeys( final PropertyContainer container,
            final boolean includeId, Map<String, Object> props )
    {
        SortedSet<String> values = new TreeSet<String>();
        for ( Entry<String, Object> entry : props.entrySet() )
        {
            String key = entry.getKey();
            Object value = entry.getValue();
            values.add( key + ": " + PropertyTransform.render( value ) );
        }
        StringBuilder str = new StringBuilder( 128 );
        if ( includeId )
        {
            if ( container instanceof Node )
            {
                str.append( "id: " )
                        .append( ( (Node) container ).getId() )
                        .append( '\n' );
            }
            else if ( container instanceof Relationship )
            {
                str.append( "id: " )
                        .append( ( (Relationship) container ).getId() )
                        .append( '\n' );
            }
        }
        if ( values.size() > 0 )
        {
            for ( String value : values )
            {
                str.append( value )
                        .append( '\n' );
            }
        }
        if ( str.length() > 1 )
        {
            return str.substring( 0, str.length() - 1 );
        }
        return "";
    }

    public String getRelationshipText( final Relationship rel )
    {
        StringBuilder str = new StringBuilder( 48 );
        if ( viewSettings.isShowRelationshipTypes() )
        {
            str.append( rel.getType()
                    .name() );
        }
        if ( viewSettings.isShowRelationshipNames() )
        {
            if ( viewSettings.isShowRelationshipTypes() )
            {
                str.append( '\n' );
            }
            if ( viewSettings.isShowRelationshipPropertyKeys() )
            {
                if ( viewSettings.isFilterRelationshipProperties()
                     && !settings.relPropertyNames.isEmpty() )
                {
                    str.append( readPropertiesWithKeys( rel,
                            settings.relPropertyNames,
                            viewSettings.isShowRelationshipIds() ) );
                }
                else
                {
                    str.append( readPropertiesWithKeys( rel,
                            viewSettings.isShowRelationshipIds() ) );
                }
            }
            else
            {
                if ( viewSettings.isFilterRelationshipProperties()
                     && !settings.relPropertyNames.isEmpty() )
                {
                    str.append( readProperties( rel, settings.relPropertyNames,
                            viewSettings.isShowRelationshipIds() ) );
                }
                else
                {
                    str.append( readProperties( rel,
                            viewSettings.isShowRelationshipIds() ) );
                }
            }
        }
        else if ( viewSettings.isShowRelationshipIds() )
        {
            if ( str.length() > 0 )
            {
                str.append( ", " );
            }
            str.append( rel.getId() );
        }
        return str.toString();
    }

    public Image getNodeImage( final Node node, final boolean isReferenceNode )
    {
        Image img;
        if ( isReferenceNode )
        {
            img = rootImage;
        }
        else
        {
            img = nodeImage;
        }
        return img;
    }

    public Image getNodeImageFromProperty( final Node node,
            final boolean isReferenceNode )
    {
        Image img = null;
        // look in properties
        for ( String key : settings.getNodeIconPropertyNames() )
        {
            Map<String, Object> props = GraphDbUtil.getProperties( node );
            if ( !props.containsKey( key ) )
            {
                continue;
            }
            Object value = props.get( key );
            PropertyHandler handler = PropertyTransform.getHandler( value );
            if ( !handler.isArray() )
            {
                String tmpPropVal = handler.render( value );
                if ( !"".equals( tmpPropVal ) ) // no empty strings
                {
                    img = userIcons.getImage( tmpPropVal );
                    if ( img != null )
                    {
                        return img;
                    }
                }
            }
        }
        // look in relations
        GraphDbServiceManager gsm = Activator.getDefault()
                .getGraphDbServiceManager();
        try
        {
            img = gsm.submitTask( new Callable<Image>()
            {
                @Override
                public Image call() throws Exception
                {
                    Image img = null;
                    for ( Direction direction : settings.getDirections() )
                    {
                        for ( Relationship rel : node.getRelationships( direction ) )
                        {
                            img = userIcons.getImage( rel.getType(), direction );
                            if ( img != null )
                            {
                                return img;
                            }
                        }
                    }
                    return img;
                }
            }, "find icons from relationships" )
                    .get();
            if ( img != null )
            {
                return img;
            }
        }
        catch ( Exception e )
        {
            ErrorMessage.showDialog( "Error retrieving relationships", e );
        }
        return getNodeImage( node, isReferenceNode );
    }

    public Color getRelationshipHighlightColor( final Relationship rel )
    {
        return HIGHLIGHTED_RELATIONSHIP_COLOR;
    }

    public Color getNodeForegroundColor( final Node node,
            final boolean isInputNode )
    {
        if ( isInputNode )
        {
            return INPUTNODE_FOREGROUND_COLOR;
        }
        else
        {
            return NODE_FOREGROUND_COLOR;
        }
    }

    public Set<RelationshipType> getRelationshipTypes()
    {
        return colorMapper.getKeys();
    }

    public Color getMarkedRelationshipColor( final Relationship rel )
    {
        return colorMapper.getColor( rel.getType(),
                ColorCategory.RELATIONSHIP_MARKED );
    }

    public int getMarkedRelationshipStyle( final Object rel )
    {
        return 0;
    }

    public Color getMarkedNodeColor( final Node element )
    {
        return getNodeColor( element, true );
    }

    public int getMarkedLineWidth()
    {
        return 2;
    }

    public int getLineWidth()
    {
        return -1;
    }
}
