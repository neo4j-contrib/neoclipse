package org.neo4j.neoclipse.connection.actions;

import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.connection.Alias;
import org.neo4j.neoclipse.connection.ConnectionsView;
import org.neo4j.neoclipse.event.NeoclipseEvent;
import org.neo4j.neoclipse.event.NeoclipseEventListener;
import org.neo4j.neoclipse.graphdb.GraphDbServiceManager;
import org.neo4j.neoclipse.view.ErrorMessage;
import org.neo4j.neoclipse.view.NeoGraphViewPart;
import org.neo4j.neoclipse.view.UiHelper;

/**
 * Handle change in the relationship color settings.
 */
public class ForceStartHandler implements NeoclipseEventListener
{
    @Override
    public void stateChanged( final NeoclipseEvent event )
    {
        UiHelper.asyncExec( new Runnable()
        {
            @Override
            public void run()
            {
                GraphDbServiceManager gsm = Activator.getDefault().getGraphDbServiceManager();
                NeoGraphViewPart graphView = Activator.getDefault().getNeoGraphViewPart();
                ConnectionsView connectionsView = Activator.getDefault().getConnectionsView();
                try
                {
                    Alias selectedAlias = connectionsView.getSelectedAlias();
                    gsm.startGraphDbService( selectedAlias ).get();
                    graphView.showSomeNode();
                }
                catch ( Exception e )
                {
                    ErrorMessage.showDialog( "Database problem", e );
                }
                Activator.getDefault().getAliasManager().notifyListners();
            }
        } );
    }
}
