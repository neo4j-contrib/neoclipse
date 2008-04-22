/*
 * IncreaseTraversalDepthAction.java
 */
package org.neo4j.neoclipse.action;

import org.eclipse.jface.action.Action;
import org.neo4j.neoclipse.view.NeoGraphViewPart;

/**
 * Increases the traversal depth of the neo graph view.
 * 
 * @author	Peter H&auml;nsgen
 */
public class IncreaseTraversalDepthAction extends Action
{
    /**
     * The view.
     */
    protected NeoGraphViewPart view;
    
    /**
     * The constructor.
     */
    public IncreaseTraversalDepthAction(NeoGraphViewPart view)
    {
        super("Increase Traversal Depth", Action.AS_PUSH_BUTTON);
        
        this.view = view;
    }

    /**
     * Executes the action.
     */
    public void run()
    {
        view.incTraversalDepth();
    }
}
