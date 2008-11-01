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

import java.util.List;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.neo4j.api.core.Direction;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.RelationshipType;
import org.neo4j.neoclipse.NeoIcons;
import org.neo4j.neoclipse.view.NeoUserIcons;

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
    private NeoUserIcons userIcons = new NeoUserIcons();
    /**
     * Default relationship "color" (gray).
     */
    private static final Color RELATIONSHIP_COLOR = new Color( Display
        .getDefault(), new RGB( 85, 85, 85 ) );
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
     * List defining order of relationship lookups for nodes.
     */
    private final List<Direction> directions;
    private SimpleColorMapper<RelationshipType> colorMapper;

    public SimpleGraphDecorator( final List<Direction> directions )
    {
        if ( directions == null )
        {
            throw new IllegalArgumentException( "Null directions list given." );
        }
        if ( directions.size() == 0 )
        {
            throw new IllegalArgumentException( "Empty directions list given." );
        }
        this.directions = directions;
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
    }

    public Color getNodeColor()
    {
        return NODE_BACKGROUND_COLOR;
    }

    public Color getNodeColor( final Node node )
    {
        Relationship randomRel = null;
        Direction randomDir = null;
        for ( Direction direction : directions )
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

    public String getNodeText( final Node node, final Node referenceNode )
    {
        String text;
        if ( referenceNode.equals( node ) )
        {
            text = "Reference Node";
        }
        else
        {
            text = "Node" + String.valueOf( node.getId() );
        }
        return text;
    }

    public String getNodeText( final Node node, final Node referenceNode,
        List<String> nodePropertyNames )
    {
        String propertyValue;
        if ( referenceNode.equals( node ) )
        {
            propertyValue = "Reference Node";
        }
        else
        {
            propertyValue = "Node";
        }
        for ( String propertyName : nodePropertyNames )
        {
            String tmpPropVal = (String) node.getProperty( propertyName, "" );
            if ( tmpPropVal != "" ) // no empty strings
            {
                propertyValue = tmpPropVal;
                break;
            }
        }
        return propertyValue;
    }

    public String getRelationshipText( final Relationship rel )
    {
        return rel.getType().name();
    }

    public Image getNodeImage( final Node node, final Node referenceNode )
    {
        Image img;
        if ( referenceNode.equals( node ) )
        {
            img = rootImage;
        }
        else
        {
            img = nodeImage;
        }
        return img;
    }

    public Image getNodeImage( final Node node, final Node referenceNode,
        List<String> nodeIconPropertyNames, String nodeIconLocation )
    {
        Image img = null;
        for ( String propertyName : nodeIconPropertyNames )
        {
            String tmpPropVal = (String) node.getProperty( propertyName, "" );
            if ( tmpPropVal != "" ) // no empty strings
            {
                Image userImg = userIcons.getImage( tmpPropVal,
                    nodeIconLocation );
                if ( userImg != null )
                {
                    img = userImg;
                    break;
                }
            }
        }
        if ( img != null )
        {
            return img;
        }
        return getNodeImage( node, referenceNode );
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
