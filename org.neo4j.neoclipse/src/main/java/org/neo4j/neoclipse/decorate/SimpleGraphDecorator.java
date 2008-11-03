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
package org.neo4j.neoclipse.decorate;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.neo4j.api.core.Direction;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.PropertyContainer;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.RelationshipType;
import org.neo4j.neoclipse.NeoIcons;

public class SimpleGraphDecorator
{
    private final int RELATIONSHIP = 0;
    private final int NODE_INCOMING = 1;
    private final int NODE_OUTGOING = 2;

    /**
     * The icon for nodes.
     */
    private Image nodeImage = NeoIcons.getImage( NeoIcons.NEO );
    /**
     * The icon for the root node.
     */
    private Image rootImage = NeoIcons.getImage( NeoIcons.NEO_ROOT );
    /**
     * User icons for nodes.
     */
    private UserIcons userIcons;
    /**
     * Default relationship "color" (gray).
     */
    private static final Color RELATIONSHIP_COLOR = new Color( Display
        .getDefault(), new RGB( 70, 70, 70 ) );
    /**
     * Default node background color.
     */
    private static final Color NODE_BACKGROUND_COLOR = new Color( Display
        .getDefault(), new RGB( 255, 255, 255 ) );
    /**
     * Color of node foreground/text.
     */
    private static final Color NODE_FOREGROUND_COLOR = new Color( Display
        .getDefault(), new RGB( 0, 0, 0 ) );
    /**
     * Highlight color for relationships.
     */
    private static final Color HIGHLIGHTED_RELATIONSHIP_COLOR = new Color(
        Display.getDefault(), new RGB( 0, 0, 0 ) );
    /**
     * Map colors to relationship types.
     */
    private SimpleColorMapper<RelationshipType> colorMapper;
    /**
     * Settings for this decorator.
     */
    private Settings settings;

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
         * The current reference node.
         */
        private Node referenceNode;
        /**
         * Current location of icons.
         */
        private String nodeIconLocation;

        public List<Direction> getDirections()
        {
            return directions;
        }

        public void setDirections( List<Direction> directions )
        {
            this.directions = directions;
        }

        public Node getReferenceNode()
        {
            return referenceNode;
        }

        public void setReferenceNode( Node referenceNode )
        {
            this.referenceNode = referenceNode;
        }

        public String getNodeIconLocation()
        {
            return nodeIconLocation;
        }

        public void setNodeIconLocation( String nodeIconLocation )
        {
            this.nodeIconLocation = nodeIconLocation;
        }

        public List<String> getNodePropertyNames()
        {
            return nodePropertyNames;
        }

        public void setNodePropertyNames( String nodePropertyNames )
        {
            this.nodePropertyNames = listFromString( nodePropertyNames );
        }

        public List<String> getRelPropertyNames()
        {
            return relPropertyNames;
        }

        public void setRelPropertyNames( String relPropertyNames )
        {
            this.relPropertyNames = listFromString( relPropertyNames );
        }

        public List<String> getNodeIconPropertyNames()
        {
            return nodeIconPropertyNames;
        }

        public void setNodeIconPropertyNames( String nodeIconPropertyNames )
        {
            this.nodeIconPropertyNames = listFromString( nodeIconPropertyNames );
        }

        /**
         * Convert a string containing a comma-separated list of names to a list
         * of strings. Ignores "" as a name.
         * @param names
         *            comma-separated names
         * @return list of names
         */
        private List<String> listFromString( String names )
        {
            List<String> list = new ArrayList<String>();
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

    public SimpleGraphDecorator( Settings settings )
    {
        if ( settings.getDirections() == null )
        {
            throw new IllegalArgumentException( "Null directions list given." );
        }
        if ( settings.getDirections().size() == 0 )
        {
            throw new IllegalArgumentException( "Empty directions list given." );
        }
        this.settings = settings;
        final float[] saturations = new float[3];
        final float[] brightnesses = new float[3];
        saturations[RELATIONSHIP] = 0.8f;
        brightnesses[RELATIONSHIP] = 0.7f;
        saturations[NODE_INCOMING] = 0.17f;
        brightnesses[NODE_INCOMING] = 1.0f;
        saturations[NODE_OUTGOING] = 0.08f;
        brightnesses[NODE_OUTGOING] = 0.95f;
        colorMapper = new SimpleColorMapper<RelationshipType>( saturations,
            brightnesses );
        userIcons = new UserIcons( settings.getNodeIconLocation() );
    }

    public Color getNodeColor()
    {
        return NODE_BACKGROUND_COLOR;
    }

    public Color getNodeColor( final Node node )
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
                    return getColorFromDirection( type, direction );
                }
            }
        }
        if ( randomRel != null )
        {
            return getColorFromDirection( randomRel.getType(), randomDir );
        }
        return getNodeColor();
    }

    private Color getColorFromDirection( final RelationshipType type,
        final Direction direction )
    {
        switch ( direction )
        {
            case INCOMING:
                return colorMapper.getColor( type, NODE_INCOMING );
            case OUTGOING:
                return colorMapper.getColor( type, NODE_OUTGOING );
            default:
                return colorMapper.getColor( type, RELATIONSHIP );
        }
    }

    public Color getRelationshipColor()
    {
        return RELATIONSHIP_COLOR;
    }

    public Color getRelationshipColor( final Relationship rel )
    {
        return colorMapper.getColor( rel.getType(), RELATIONSHIP );
    }

    public String getNodeText( final Node node )
    {
        if ( settings.getReferenceNode().equals( node ) )
        {
            return "Reference Node";
        }
        else
        {
            return "Node";
        }
    }

    public String getNodeTextFromProperty( final Node node )
    {
        String propertyValue = readProperties( node, settings
            .getNodePropertyNames() );
        if ( propertyValue != null )
        {
            return propertyValue;
        }
        return getNodeText( node );
    }

    private String readProperties( final PropertyContainer primitive,
        final List<String> propertyNames )
    {
        String propertyValue = null;
        for ( String propertyName : propertyNames )
        {
            String tmpPropVal = (String) primitive.getProperty( propertyName,
                "" );
            if ( tmpPropVal != "" ) // no empty strings
            {
                propertyValue = tmpPropVal;
                break;
            }
        }
        return propertyValue;
    }

    public String getRelationshipTypeText( final Relationship rel )
    {
        return rel.getType().name();
    }

    public String getRelationshipNameTextFromProperty( final Relationship rel )
    {
        return readProperties( rel, settings.getRelPropertyNames() );
    }

    public Image getNodeImage( final Node node )
    {
        Image img;
        if ( settings.getReferenceNode().equals( node ) )
        {
            img = rootImage;
        }
        else
        {
            img = nodeImage;
        }
        return img;
    }

    public Image getNodeImageFromProperty( final Node node )
    {
        Image img = null;
        // look in properties
        for ( String propertyName : settings.getNodeIconPropertyNames() )
        {
            String tmpPropVal = (String) node.getProperty( propertyName, "" );
            if ( tmpPropVal != "" ) // no empty strings
            {
                img = userIcons.getImage( tmpPropVal );
                if ( img != null )
                {
                    return img;
                }
            }
        }
        // look in relations
        for ( Direction direction : settings.getDirections() )
        {
            for ( Relationship rel : node.getRelationships( direction ) )
            {
                String typeName = rel.getType().name();
                img = userIcons.getImage( typeName + "." + direction.name() );
                if ( img != null )
                {
                    return img;
                }
            }
        }
        return getNodeImage( node );
    }

    public Color getRelationshipHighlightColor( Relationship rel )
    {
        return HIGHLIGHTED_RELATIONSHIP_COLOR;
    }

    public Color getNodeForegroundColor( Node node )
    {
        return NODE_FOREGROUND_COLOR;
    }
}
