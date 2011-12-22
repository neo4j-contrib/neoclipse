package org.neo4j.neoclipse.connection;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.neo4j.neoclipse.action.AbstractBaseAction;
import org.neo4j.neoclipse.action.Actions;

/**
 * Abstract implementation for a context menu action in the connection view.
 * Extend this class to add actions.
 * 
 * @author Radhakrishan kalyan
 */
public abstract class AbstractConnectionTreeAction extends AbstractBaseAction implements IViewActionDelegate
{

    public AbstractConnectionTreeAction( Actions action )
    {
        super( action );
    }

    @Override
    public void init( IViewPart view )
    {
    }

    @Override
    public void run( IAction action )
    {
        run();
    }

    @Override
    public void selectionChanged( IAction action, ISelection selection )
    {
        action.setEnabled( isAvailable() );
    }

}
