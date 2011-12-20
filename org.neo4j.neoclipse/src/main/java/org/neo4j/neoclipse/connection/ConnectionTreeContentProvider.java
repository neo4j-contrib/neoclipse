package org.neo4j.neoclipse.connection;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.neo4j.neoclipse.Activator;

/**
 * Content provider for database structure outline.
 * 
 * @author Radhakrishna Kalyan
 */
public class ConnectionTreeContentProvider implements ITreeContentProvider
{

    @Override
    public void dispose()
    {
        // noop
    }

    @Override
    public Object[] getChildren( Object parentElement )
    {

        if ( parentElement instanceof AliasManager )
        {
            AliasManager aliases = (AliasManager) parentElement;

            Object[] children = aliases.getAliases().toArray();
            return children;

        }

        return null;
    }

    @Override
    public Object[] getElements( Object inputElement )
    {
        return getChildren( inputElement );
    }

    @Override
    public Object getParent( Object element )
    {

        // this is root node
        if ( element instanceof AliasManager )
        {
            return null;
        }
        else if ( element instanceof Alias )
        {
            return Activator.getDefault().getAliasManager();
        }

        return null;
    }

    @Override
    public boolean hasChildren( Object element )
    {
        Object[] tmp = getChildren( element );

        return tmp != null && tmp.length != 0;
    }

    @Override
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
    {
        // noop
    }

}
