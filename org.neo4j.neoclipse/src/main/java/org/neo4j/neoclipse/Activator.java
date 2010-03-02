/*
 * Licensed to "Neo Technology," Network Engine for Objects in Lund AB
 * (http://neotechnology.com) under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership. Neo Technology licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License
 * at (http://www.apache.org/licenses/LICENSE-2.0). Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.neo4j.neoclipse;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.neoclipse.neo.GraphDbServiceManager;
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
    }

    /**
     * Stops the plug-in and shuts down the neo service.
     */
    @Override
    public void stop( final BundleContext context ) throws Exception
    {
        graphDbManager.stopGraphDbService();
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
     * Get the current GraphDatabaseService. Returns <code>null</code> on
     * failure, after showing appropriate error messages.
     * 
     * @return current neo service
     */
    public GraphDatabaseService getGraphDbServiceSafely()
    {
        GraphDbServiceManager sm = Activator.getDefault().getGraphDbServiceManager();
        if ( sm == null )
        {
            MessageDialog.openError( null, "Error",
                    "The Neo service manager is not available." );
            return null;
        }
        GraphDatabaseService ns = null;
        try
        {
            ns = sm.getGraphDbService();
        }
        catch ( RuntimeException rte )
        {
            MessageDialog.openInformation( null, "Database problem",
                    "The database seem to be in use. "
                            + "Please make sure that it is not in use or "
                            + "change to another database location." );
            try
            {
                ns = sm.getGraphDbService();
            }
            catch ( RuntimeException rte2 )
            {
                // just continue
            }
        }
        if ( ns == null )
        {
            MessageDialog.openInformation(
                    null,
                    "Database location problem",
                    "Please make sure that the database location is correctly set. "
                            + "To create an empty database, point the location to an empty directory." );
            showPreferenceDialog( true );
            try
            {
                ns = sm.getGraphDbService();
            }
            catch ( RuntimeException rte )
            {
                // just continue
            }
            if ( ns == null )
            {
                MessageDialog.openError( null, "Error message",
                        "The Neo service is not available." );
                return null;
            }
        }
        return ns;
    }

    /**
     * Get the reference node.
     * 
     * @return the reference node
     */
    public Node getReferenceNode()
    {
        GraphDatabaseService ns = getGraphDbServiceSafely();
        if ( ns == null )
        {
            return null;
        }
        return ns.getReferenceNode();
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
                "org.neo4j.neoclipse.preference.NeoPreferencePage",
                ( filtered ? new String[] {} : null ), null );
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
                "org.neo4j.neoclipse.preference.NeoDecoratorPreferencePage",
                ( filtered ? new String[] {} : null ), null );
        if ( pref != null )
        {
            return pref.open();
        }
        return 1;
    }

    /**
     * Restart the graphdb service from a new location.
     */
    public void restartGraphDb()
    {
        graphDbManager.stopGraphDbService();
        // start the service using new location
        getGraphDbServiceSafely();
    }
}
