/*
 * ShowArrowsAction.java
 */
package org.neo4j.neoclipse.action;

import org.eclipse.jface.action.Action;
import org.neo4j.neoclipse.view.NeoGraphViewPart;

/**
 * This action handles if arrows are showed or not.
 * 
 * @author  Anders Nawroth
 */
public class ShowArrowsAction extends Action
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
    public ShowArrowsAction(NeoGraphViewPart view)
    {
        super("Arrows", Action.AS_CHECK_BOX);
        this.view = view;
    }

    /**
     * Executes the action.
     */
    public void run()
    {
        view.showArrows( this.isChecked() );
    }
}
