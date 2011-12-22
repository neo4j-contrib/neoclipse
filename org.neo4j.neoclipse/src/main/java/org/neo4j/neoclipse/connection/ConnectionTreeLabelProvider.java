package org.neo4j.neoclipse.connection;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.Icons;
import org.neo4j.neoclipse.graphdb.GraphDbServiceManager;

/**
 * Label provider for database structure outline.
 * 
 * @author Radhakrishna kalyan
 */
public class ConnectionTreeLabelProvider extends LabelProvider
{

    private Image _inactiveAliasImage = Icons.NEW_ALIAS_DISABLED.image();

    private Image _activeAliasImage = Icons.NEW_ALIAS_ENABLED.image();

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
            GraphDbServiceManager graphDbServiceManager = Activator.getDefault().getGraphDbServiceManager();
            if ( graphDbServiceManager.isRunning() && graphDbServiceManager.getCurrentAlias().equals( alias ) )
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
