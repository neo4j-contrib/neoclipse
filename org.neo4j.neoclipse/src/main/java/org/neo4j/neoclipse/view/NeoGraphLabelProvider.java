/*
 * NeoGraphLabelProvider.java
 */
package org.neo4j.neoclipse.view;

import org.eclipse.draw2d.IFigure;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.zest.core.viewers.IConnectionStyleProvider;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.NeoIcons;
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
            String defaultPropery = Activator.getDefault().getPreferenceStore()
                .getString( NeoPreferences.DEFAULT_PROPERTY_NAME ).trim();
            if ( defaultPropery == "" )
            {
                // don't look for the default property
                if ( node.getId() == 0 )
                {
                    // the only difference from other nodes is the text
                    return ("Reference Node #" + String
                        .valueOf( ((Node) element).getId() ));
                }
                else
                {
                    return ("Node #" + String
                        .valueOf( ((Node) element).getId() ));
                }
            }
            else
            {
                // show the default property
                if ( node.getId() == 0 )
                {
                    // the only difference from other nodes is the default text
                    return ((Node) element).getProperty( defaultPropery,
                        "Reference Node" )
                        + " #" + String.valueOf( ((Node) element).getId() );
                }
                else
                {
                    return ((Node) element)
                        .getProperty( defaultPropery, "Node" )
                        + " #" + String.valueOf( ((Node) element).getId() );
                }
            }
        }
        else if ( element instanceof Relationship )
        {
            return ((Relationship) element).getType().toString() + " #"
                + String.valueOf( ((Relationship) element).getId() );
        }
        return element.toString();
    }

    @Override
    public Color getColor( Object rel )
    {
        return null;
    }

    @Override
    public int getConnectionStyle( Object rel )
    {
        return ZestStyles.CONNECTIONS_DIRECTED;
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
