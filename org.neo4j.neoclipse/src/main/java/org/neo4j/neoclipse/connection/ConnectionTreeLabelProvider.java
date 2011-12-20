package org.neo4j.neoclipse.connection;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.neo4j.neoclipse.Icons;

/**
 * Label provider for database structure outline.
 * 
 * @author Radhakrishna kalyan
 */
public class ConnectionTreeLabelProvider extends LabelProvider
{

    private Image _inactiveAliasImage = Icons.NEW_CONNECTION_DISABLED.image();

    private Image _activeAliasImage = Icons.NEW_CONNECTION_ENABLED.image();

    @Override
    public void dispose()
    {
        super.dispose();
    }

    /**
     * Return the image used for the given node.
     * 
     * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
     */
    @Override
    public Image getImage( Object element )
    {
        if ( element instanceof Alias )
        {
            Alias alias = (Alias) element;
            if ( alias.getNeo4JDbLocation() != null )
            {
                return _activeAliasImage;

            }

        }
        return _inactiveAliasImage;
    }

    /**
     * Return the text to display
     * 
     * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
     */
    @Override
    public String getText( Object element )
    {
        if ( element instanceof Alias )
        {
            Alias alias = (Alias) element;

            String label = alias.getName();
            int numSessions = 0;

            return label + "( " + alias.getNeo4JDbLocation() + " )";

        }

        return null;
    }
}
