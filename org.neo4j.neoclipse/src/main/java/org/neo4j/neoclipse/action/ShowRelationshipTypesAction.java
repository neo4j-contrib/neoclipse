/*
 * ShowRelationshipTypeAction.java
 */
package org.neo4j.neoclipse.action;

import org.eclipse.jface.action.Action;
import org.neo4j.neoclipse.view.NeoGraphViewPart;

/**
 * This action handles the relationship type setting.
 * @author Anders Nawroth
 */
public class ShowRelationshipTypesAction extends Action
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
    public ShowRelationshipTypesAction( NeoGraphViewPart view )
    {
        super( "Relationship Types", Action.AS_CHECK_BOX );
        this.view = view;
    }

    /**
     * Executes the action.
     */
    public void run()
    {
        view.showRelationshipTypes( this.isChecked() );
    }
}
