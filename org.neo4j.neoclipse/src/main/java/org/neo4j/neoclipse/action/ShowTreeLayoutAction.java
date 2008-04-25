/*
 * ShowTreeLayoutAction.java
 */
package org.neo4j.neoclipse.action;

import org.eclipse.jface.action.Action;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.TreeLayoutAlgorithm;
import org.neo4j.neoclipse.view.NeoGraphViewPart;

/**
 * This action switches the neo graph view to tree layout.
 * 
 * @author	Peter H&auml;nsgen
 */
public class ShowTreeLayoutAction extends Action
{
    /**
     * The view.
     */
    protected NeoGraphViewPart view;
    
    /**
     * The constructor.
     */
    public ShowTreeLayoutAction(NeoGraphViewPart view)
    {
        super("Tree Layout", Action.AS_RADIO_BUTTON);
        
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
                    new TreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);
        }
    }
}
