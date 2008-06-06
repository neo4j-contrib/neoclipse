/*
 * NeoGraphLabelProvider.java
 */
package org.neo4j.neoclipse.view;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.draw2d.IFigure;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.zest.core.viewers.IConnectionStyleProvider;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.RelationshipType;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.NeoIcons;
import org.neo4j.neoclipse.action.ShowRelationshipDirectionsAction;
import org.neo4j.neoclipse.action.ShowNodeNamesAction;
import org.neo4j.neoclipse.action.ShowRelationshipColorsAction;
import org.neo4j.neoclipse.action.ShowRelationshipTypesAction;
import org.neo4j.neoclipse.preference.NeoPreferences;

/**
 * Provides the labels for graph elements.
 * @author Peter H&auml;nsgen
 */
public class NeoGraphLabelProvider extends LabelProvider implements
    IConnectionStyleProvider
{
    /**
     * The icon for nodes.
     */
    private Image nodeImage = NeoIcons.getImage( NeoIcons.NEO );
    /**
     * The icon for the root node.
     */
    private Image rootImage = NeoIcons.getImage( NeoIcons.NEO_ROOT );
    /**
     * Keep track of relationship types display on/off.
     */
    private boolean showRelationshipTypes = ShowRelationshipTypesAction.DEFAULT_STATE;
    /**
     * Keep track of relationship colors display on/off.
     */
    private boolean showRelationshipColors = ShowRelationshipColorsAction.DEFAULT_STATE;
    /**
     * Keep track of arrows display on/off.
     */
    private boolean showArrows = ShowRelationshipDirectionsAction.DEFAULT_STATE;
    /**
     * Keep track of node names display on/off.
     */
    private boolean showNames = ShowNodeNamesAction.DEFAULT_STATE;
    /**
     * Map RelationshipTypes to Colors for the graph.
     */
    private Map<RelationshipType,Color> colors = new HashMap<RelationshipType,Color>();
    /**
     * Tool that creates colors that differ as much as possible regarding hue.
     */
    private NeoGraphColorGenerator colorGenerator = new NeoGraphColorGenerator();

    /**
     * Returns the icon for an element.
     */
    public Image getImage( Object element )
    {
        if ( element instanceof Node )
        {
            Long id = ((Node) element).getId();
            if ( id.longValue() == 0L )
            {
                return rootImage;
            }
            else
            {
                return nodeImage;
            }
        }
        return null;
    }

    /**
     * Returns the text for an element.
     */
    public String getText( Object element )
    {
        if ( element instanceof Node )
        {
            Node node = (Node) element;
            String defaultProperties = Activator.getDefault()
                .getPreferenceStore().getString(
                    NeoPreferences.NODE_PROPERTY_NAMES ).trim();
            if ( !showNames || defaultProperties == "" )
            {
                // don't look for the default property
                if ( node.getId() == 0 )
                {
                    return ("Reference Node");
                }
                else
                {
                    return ("Node " + String.valueOf( ((Node) element).getId() ));
                }
            }
            else
            {
                // show the default property
                String propertyValue;
                if ( node.getId() == 0 )
                {
                    propertyValue = "Reference Node ";
                }
                else
                {
                    propertyValue = "Node ";
                }
                for ( String propertyName : defaultProperties.split( "," ) )
                {
                    propertyName = propertyName.trim();
                    if ( propertyName == "" )
                    {
                        continue;
                    }
                    String tmpPropVal = (String) ((Node) element).getProperty(
                        propertyName, "" );
                    if ( tmpPropVal != "" ) // no empty strings
                    {
                        propertyValue = tmpPropVal + " #";
                        break;
                    }
                }
                return propertyValue
                    + String.valueOf( ((Node) element).getId() );
            }
        }
        else if ( element instanceof Relationship )
        {
            if ( showRelationshipTypes )
            {
                return ((Relationship) element).getType().toString() + " #"
                    + String.valueOf( ((Relationship) element).getId() );
            }
            else
            {
                return String.valueOf( ((Relationship) element).getId() );
            }
        }
        return element.toString();
    }

    /**
     * Show or hide relationship types.
     * @param showRelationshipTypes
     *            set true to display
     */
    public void setShowRelationshipTypes( boolean showRelationshipTypes )
    {
        this.showRelationshipTypes = showRelationshipTypes;
    }

    /**
     * Show or hide relationship colors.
     * @param showRelationshipTypes
     *            set true to display
     */
    public void setShowRelationshipColors( boolean showRelationshipColors )
    {
        this.showRelationshipColors = showRelationshipColors;
    }

    /**
     * Show or hide arrows.
     * @param showRelationshipTypes
     *            set true to display
     */
    public void setShowArrows( boolean showArrows )
    {
        this.showArrows = showArrows;
    }

    /**
     * Show or hide names.
     * @param showRelationshipTypes
     *            set true to display
     */
    public void setShowNames( boolean showNames )
    {
        this.showNames = showNames;
    }

    @Override
    public Color getColor( Object rel )
    {
        if ( !showRelationshipColors )
        {
            return null;
        }
        RelationshipType type = ((Relationship) rel).getType();
        Color color = colors.get( type );
        if ( color == null )
        {
            color = colorGenerator.next();
            colors.put( type, color );
        }
        return color;
    }

    @Override
    public int getConnectionStyle( Object rel )
    {
        if ( showArrows )
        {
            return ZestStyles.CONNECTIONS_DIRECTED;
        }
        return 0;
    }

    @Override
    public Color getHighlightColor( Object rel )
    {
        return null;
    }

    @Override
    public int getLineWidth( Object rel )
    {
        return -1;
    }

    @Override
    public IFigure getTooltip( Object entity )
    {
        return null;
    }
}
