/*
 * ShowReferenceNodeAction.java
 */
package org.neo4j.neoclipse.action;

import org.eclipse.jface.action.Action;
import org.neo4j.neoclipse.view.NeoGraphViewPart;

/**
 * This action focuses the graph viewer on the neo reference node.
 * 
 * @author	Peter H&auml;nsgen
 */
public class ShowReferenceNodeAction extends Action
{
    /**
     * The view.
     */
    protected NeoGraphViewPart view;
    
    /**
     * The constructor.
     */
    public ShowReferenceNodeAction(NeoGraphViewPart view)
    {
        super("Show Reference Node", Action.AS_PUSH_BUTTON);
        
        this.view = view;
    }

    /**
     * Executes the action.
     */
    public void run()
    {
        view.showReferenceNode();        
    }
}
