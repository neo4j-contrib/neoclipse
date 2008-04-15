/*
 * ShowSpringLayoutAction.java
 */
package org.neo4j.neoclipse.action;

import org.eclipse.jface.action.Action;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.SpringLayoutAlgorithm;
import org.neo4j.neoclipse.view.NeoGraphViewPart;

/**
 * This action sets the layout of the graph viewer to spring layout.
 * 
 * @author	Peter H&auml;nsgen
 */
public class ShowSpringLayoutAction extends Action
{
    /**
     * The view.
     */
    protected NeoGraphViewPart view;
    
    /**
     * The constructor.
     */
    public ShowSpringLayoutAction(NeoGraphViewPart view)
    {
        super("Spring Layout", Action.AS_RADIO_BUTTON);
        
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
                    new SpringLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);
        }
    }
}
