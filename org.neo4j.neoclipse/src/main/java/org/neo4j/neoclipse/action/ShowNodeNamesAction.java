/*
 * ShowNodeNamesAction.java
 */
package org.neo4j.neoclipse.action;

import org.eclipse.jface.action.Action;
import org.neo4j.neoclipse.view.NeoGraphViewPart;

/**
 * This action handles if node names are shown or not.
 * 
 * @author  Anders Nawroth
 */
public class ShowNodeNamesAction extends Action
{
    /**
     * Default state for this view menu alternative.
     */
    public final static boolean DEFAULT_STATE = true;
    /**
     * The view.
     */
    protected NeoGraphViewPart view;
    
    /**
     * The constructor.
     */
    public ShowNodeNamesAction(NeoGraphViewPart view)
    {
        super("Node names", Action.AS_CHECK_BOX);    
        this.view = view;
    }

    /**
     * Executes the action.
     */
    public void run()
    {
        view.showNames( this.isChecked() );
    }
}
