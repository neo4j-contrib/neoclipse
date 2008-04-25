/*
 * ShowRadialLayoutAction.java
 */
package org.neo4j.neoclipse.action;

import org.eclipse.jface.action.Action;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.RadialLayoutAlgorithm;
import org.neo4j.neoclipse.view.NeoGraphViewPart;

/**
 * This action switches the graph view to radial layout.
 * 
 * @author	Peter H&auml;nsgen
 */
public class ShowRadialLayoutAction extends Action
{
    /**
     * The view.
     */
    protected NeoGraphViewPart view;
    
    /**
     * The constructor.
     */
    public ShowRadialLayoutAction(NeoGraphViewPart view)
    {
        super("Radial Layout", Action.AS_RADIO_BUTTON);
        
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
                    new RadialLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);
        }
    }
}
