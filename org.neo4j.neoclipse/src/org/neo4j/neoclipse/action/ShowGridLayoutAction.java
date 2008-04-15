/*
 * ShowGridLayoutAction.java
 */
package org.neo4j.neoclipse.action;

import org.eclipse.jface.action.Action;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.GridLayoutAlgorithm;
import org.neo4j.neoclipse.view.NeoGraphViewPart;

/**
 * This action switches the graph view to grid layout.
 * 
 * @author  Peter H&auml;nsgen
 */
public class ShowGridLayoutAction extends Action
{
    /**
     * The view.
     */
    protected NeoGraphViewPart view;
    
    /**
     * The constructor.
     */
    public ShowGridLayoutAction(NeoGraphViewPart view)
    {
        super("Grid Layout", Action.AS_RADIO_BUTTON);
        
        this.view = view;
    }
    
    /**
     * Executes the action.
     */
    public void run()
    {
        if (isChecked())
        {
            view.getViewer().setLayoutAlgorithm(
                    new GridLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);
        }
    }
}
