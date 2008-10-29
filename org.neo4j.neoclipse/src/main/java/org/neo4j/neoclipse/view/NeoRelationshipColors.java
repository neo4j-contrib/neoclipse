package org.neo4j.neoclipse.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.neo4j.api.core.Direction;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.RelationshipType;

public class NeoRelationshipColors
{
    private static class ColorTriad
    {
        public Color relationship;
        public Color nodeIn;
        public Color nodeOut;

        public Color getByDirection( final Direction direction )
        {
            switch ( direction )
            {
                case INCOMING:
                    return nodeIn;
                case OUTGOING:
                    return nodeOut;
                default:
                    return nodeIn;
            }
        }
    }

    /**
     * Brightness of relationship stroke colors.
     */
    private static final float RELATIONSHIP_BRIGHTNESS = 0.7f;
    /**
     * Saturation of relationship stroke colors.
     */
    private static final float RELATIONSHIP_SATURATION = 0.8f;
    /**
     * Hue of the first relationship stroke color.
     */
    private static final float HUE = 60.0f;
    /**
     * Default relationship "color" (gray).
     */
    private static final Color RELATIONSHIP_COLOR = new Color( Display
        .getDefault(), new RGB( 85, 85, 85 ) );
    /**
     * Create colors for relationships.
     */
    private final NeoGraphColorGenerator colorGenerator = new NeoGraphColorGenerator(
        HUE, RELATIONSHIP_SATURATION, RELATIONSHIP_BRIGHTNESS );
    /**
     * Brightness of node background colors.
     */
    private static final float NODE_BRIGHTNESS = 1.0f;
    /**
     * Saturation of node background colors.
     */
    private static final float NODE_IN_SATURATION = 0.17f;
    /**
     * Saturation of node background colors.
     */
    private static final float NODE_OUT_SATURATION = 0.08f;
    /**
     * Default node background color.
     */
    private static final Color NODE_BACKGROUND_COLOR = new Color( Display
        .getDefault(), new RGB( 255, 255, 255 ) );
    /**
     * Map RelationshipTypes to Colors for the graph.
     */
    private final Map<RelationshipType,ColorTriad> colorMap = new HashMap<RelationshipType,ColorTriad>();
    /**
     * List defining order of relationship lookups for nodes.
     */
    private final List<Direction> directions;

    public NeoRelationshipColors( final List<Direction> directions )
    {
        this.directions = directions;
    }

    public Color getRelationshipColor()
    {
        return RELATIONSHIP_COLOR;
    }

    public Color getRelationshipColor( final RelationshipType type )
    {
        if ( type == null )
        {
            return getRelationshipColor();
        }
        return getColor( type ).relationship;
    }

    public Color getNodeColor()
    {
        return NODE_BACKGROUND_COLOR;
    }

    public Color getNodeColor( final Node node )
    {
        ColorTriad colors;
        Relationship randomRel = null;
        Direction randomDir = null;
        for ( Direction direction : directions )
        {
            for ( Relationship rel : node.getRelationships( direction ) )
            {
                colors = colorMap.get( rel.getType() );
                if ( colors == null )
                {
                    if ( randomRel == null )
                    {
                        randomRel = rel;
                        randomDir = direction;
                    }
                    continue;
                }
                return colors.getByDirection( direction );
            }
        }
        if ( randomRel != null )
        {
            return getColor( randomRel.getType() ).getByDirection( randomDir );
        }
        return getNodeColor();
    }

    private ColorTriad getColor( final RelationshipType type )
    {
        ColorTriad colors = colorMap.get( type );
        if ( colors != null )
        {
            return colors;
        }
        final float hue = colorGenerator.nextHue();
        colors = new ColorTriad();
        colors.relationship = new Color( Display.getDefault(), new RGB( hue,
            RELATIONSHIP_SATURATION, RELATIONSHIP_BRIGHTNESS ) );
        colors.nodeIn = new Color( Display.getDefault(), new RGB( hue,
            NODE_IN_SATURATION, NODE_BRIGHTNESS ) );
        colors.nodeOut = new Color( Display.getDefault(), new RGB( hue,
            NODE_OUT_SATURATION, NODE_BRIGHTNESS ) );
        colorMap.put( type, colors );
        return colors;
    }
}
