/*
 * ShowNodeIdsAction.java
 */
package org.neo4j.neoclipse.action;

import org.eclipse.jface.action.Action;
import org.neo4j.neoclipse.view.NeoGraphViewPart;

/**
 * This action handles the node id setting.
 * 
 * @author  Anders Nawroth
 */
public class ShowNodeIdsAction extends Action
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
    public ShowNodeIdsAction(NeoGraphViewPart view)
    {
        super("Node id", Action.AS_CHECK_BOX);
        this.view = view;
    }

    /**
     * Executes the action.
     */
    public void run()
    {
        view.showNodeIds( this.isChecked() );
    }
}
