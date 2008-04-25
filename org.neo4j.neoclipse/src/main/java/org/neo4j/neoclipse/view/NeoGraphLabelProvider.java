/*
 * NeoGraphLabelProvider.java
 */
package org.neo4j.neoclipse.view;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.neoclipse.NeoIcons;

/**
 * Provides the labels for graph elements.
 * 
 * @author	Peter H&auml;nsgen
 */
public class NeoGraphLabelProvider extends LabelProvider
{
    /**
     * The icon for nodes.
     */
    private Image nodeImage = NeoIcons.getImage(NeoIcons.NEO);

    /**
     * The icon for the root node.
     */
    private Image rootImage = NeoIcons.getImage(NeoIcons.NEO_ROOT);

    /**
     * Returns the icon for an element.
     */
    public Image getImage(Object element)
    {
        if (element instanceof Node)
        {
            Long id = ((Node) element).getId();
            if (id.longValue() == 0L)
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
    public String getText(Object element)
    {
        if (element instanceof Node)
        {
            Node node = (Node) element;
            if (node.getId() == 0)
            {
                return "Reference Node";
            }
            else
            {
                return "Node " + String.valueOf(((Node) element).getId());
            }
        }
        else if (element instanceof Relationship)
        {
            return String.valueOf(((Relationship) element).getId());
        }

        return element.toString();
    }
}
