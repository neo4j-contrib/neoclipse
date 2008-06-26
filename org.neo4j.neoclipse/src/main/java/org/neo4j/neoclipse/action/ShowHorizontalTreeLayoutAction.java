package org.neo4j.neoclipse.action;

import org.eclipse.jface.action.Action;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.HorizontalTreeLayoutAlgorithm;
import org.neo4j.neoclipse.view.NeoGraphViewPart;

/**
 * This action sets the layout of the graph viewer to horizontal tree layout.
 * @author Anders Nawroth
 */
public class ShowHorizontalTreeLayoutAction extends Action
{
    /**
     * The view.
     */
    protected NeoGraphViewPart view;

    /**
     * The constructor.
     */
    public ShowHorizontalTreeLayoutAction( NeoGraphViewPart view )
    {
        super( "Horizontal Tree Layout", Action.AS_RADIO_BUTTON );
        this.view = view;
    }

    /**
     * Executes the action.
     */
    public void run()
    {
        if ( isChecked() )
        {
            view.getViewer().setLayoutAlgorithm(
                new HorizontalTreeLayoutAlgorithm(
                    LayoutStyles.NO_LAYOUT_NODE_RESIZING ), true );
        }
    }
}
