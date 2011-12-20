/**
 * Licensed to Neo Technology under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Neo Technology licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.neo4j.neoclipse;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.neo4j.neoclipse.connection.AliasManager;
import org.neo4j.neoclipse.connection.ConnectionsView;
import org.neo4j.neoclipse.graphdb.GraphDbServiceManager;
import org.neo4j.neoclipse.view.NeoGraphViewPart;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 */
public class Activator extends AbstractUIPlugin
{
    /**
     * The plug-in ID.
     */
    public static final String PLUGIN_ID = "org.neo4j.neoclipse";
    /**
     * The graphdb manager.
     */
    protected GraphDbServiceManager graphDbManager;
    private AliasManager neo4JConnectionManager;
    private ConnectionsView connectionsView;
    private NeoGraphViewPart neoGraphViewPart;

    /**
     * The shared instance.
     */
    private static Activator PLUGIN;

    /**
     * Starts up the plug-in and initializes the neo service.
     */
    @Override
    public void start( final BundleContext context ) throws Exception
    {
        super.start( context );
        PLUGIN = this;
        graphDbManager = new GraphDbServiceManager();
        neo4JConnectionManager = new AliasManager();
    }

    /**
     * Stops the plug-in and shuts down the neo4j service.
     */
    @Override
    public void stop( final BundleContext context ) throws Exception
    {
        graphDbManager.shutdownGraphDbService();
        graphDbManager.stopExecutingTasks();
        neo4JConnectionManager.closeAllConnections();
        PLUGIN = null;
        super.stop( context );
    }

    /**
     * Returns the shared instance.
     * 
     * @return the shared instance
     */
    public static Activator getDefault()
    {
        return PLUGIN;
    }

    /**
     * Returns the service manager.
     */
    public GraphDbServiceManager getGraphDbServiceManager()
    {
        return graphDbManager;
    }

    /**
     * Show the Neo4j preference page.
     * 
     * @param filtered only show Neo4j properties when true
     * @return
     */
    public int showPreferenceDialog( final boolean filtered )
    {
        PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn( null,
                "org.neo4j.neoclipse.preference.NeoPreferencePage", ( filtered ? new String[] {} : null ), null );
        if ( pref != null )
        {
            return pref.open();
        }
        return 1;
    }

    /**
     * Show the Neo4j Decorator preference page.
     * 
     * @param filtered only show Neo4j properties when true
     * @return
     */
    public int showDecoratorPreferenceDialog( final boolean filtered )
    {
        PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn( null,
                "org.neo4j.neoclipse.preference.NeoDecoratorPreferencePage", ( filtered ? new String[] {} : null ),
                null );
        if ( pref != null )
        {
            return pref.open();
        }
        return 1;
    }

    public AliasManager getAliasManager()
    {
        return neo4JConnectionManager;
    }

    public ConnectionsView getConnectionsView()
    {
        return getConnectionsView( true );
    }

    private IWorkbenchPage getActivePage()
    {
        if ( getWorkbench() != null && getWorkbench().getActiveWorkbenchWindow() != null )
        {
            return getWorkbench().getActiveWorkbenchWindow().getActivePage();
        }
        return null;
    }

    public ConnectionsView getConnectionsView( boolean create )
    {
        if ( connectionsView == null )
        {
            IWorkbenchPage page = getActivePage();
            if ( page != null )
            {
                connectionsView = (ConnectionsView) page.findView( ConnectionsView.class.getName() );
                if ( connectionsView == null && create )
                {
                    try
                    {
                        connectionsView = (ConnectionsView) page.showView( ConnectionsView.class.getName() );
                    }
                    catch ( PartInitException e )
                    {
                        throw new RuntimeException( e );
                    }
                }
            }
        }

        return connectionsView;
    }

    public void setConnectionsView( ConnectionsView connectionsView )
    {
        this.connectionsView = connectionsView;
    }

    public NeoGraphViewPart getNeoGraphViewPart()
    {
        if ( neoGraphViewPart == null )
        {
            IWorkbenchPage page = getActivePage();
            if ( page != null )
            {
                neoGraphViewPart = (NeoGraphViewPart) page.findView( NeoGraphViewPart.class.getName() );
                if ( neoGraphViewPart == null )
                {
                    try
                    {
                        neoGraphViewPart = (NeoGraphViewPart) page.showView( NeoGraphViewPart.class.getName() );
                    }
                    catch ( PartInitException e )
                    {
                        throw new RuntimeException( e );
                    }
                }
            }
        }
        return neoGraphViewPart;
    }

    public void setNeoGraphViewPart( NeoGraphViewPart neoGraphViewPart )
    {
        this.neoGraphViewPart = neoGraphViewPart;
    }

}
