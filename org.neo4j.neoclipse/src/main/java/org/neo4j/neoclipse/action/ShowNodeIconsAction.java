/*
 * ShowNodeIconsAction.java
 */
package org.neo4j.neoclipse.action;

import org.eclipse.jface.action.Action;
import org.neo4j.neoclipse.view.NeoGraphViewPart;

/**
 * This action handles the node icons setting.
 * @author Anders Nawroth
 */
public class ShowNodeIconsAction extends Action
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
    public ShowNodeIconsAction( NeoGraphViewPart view )
    {
        super( "Node icons", Action.AS_CHECK_BOX );
        this.view = view;
    }

    /**
     * Executes the action.
     */
    public void run()
    {
        view.showNodeIcons( this.isChecked() );
    }
}
