/*
 * RefreshAction.java
 */
package org.neo4j.neoclipse.action;

import org.eclipse.jface.action.Action;
import org.neo4j.neoclipse.view.NeoGraphViewPart;

/**
 * This action refreshes the graph view, e.g. it sets the current node again
 * as input source.
 * 
 * @author	Peter H&auml;nsgen
 */
public class RefreshAction extends Action
{
    /**
     * The view.
     */
    protected NeoGraphViewPart view;
    
    /**
     * The constructor.
     */
    public RefreshAction(NeoGraphViewPart view)
    {
        super("Refresh", Action.AS_PUSH_BUTTON);
        
        this.view = view;
    }

    /**
     * Executes the action.
     */
    public void run()
    {
        view.getViewer().refresh();
    }
}
