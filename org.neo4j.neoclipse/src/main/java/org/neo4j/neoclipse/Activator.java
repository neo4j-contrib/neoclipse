/*
 * Activator.java
 */
package org.neo4j.neoclipse;

import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.neo4j.neoclipse.neo.NeoServiceManager;
import org.neo4j.neoclipse.preference.NeoPreferences;
import org.neo4j.neoclipse.view.NeoGraphLabelProvider;
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
     * The neo manager.
     */
    protected NeoServiceManager neoManager;
    /**
     * The shared instance.
     */
    private static Activator plugin;

    /**
     * Starts up the plug-in and initializes the neo service.
     */
    public void start(BundleContext context) throws Exception
    {
        super.start(context);
        plugin = this;
        
        NeoIcons.init(this);

        neoManager = new NeoServiceManager();
        plugin.getPluginPreferences().addPropertyChangeListener(
            new IPropertyChangeListener()
            {
                /**
                 * Handles neo property change events 
                 */
                public void propertyChange(PropertyChangeEvent event)
                {
                    String property = event.getProperty();
                    if (NeoPreferences.DATABASE_LOCATION.equals(property))
                    {
                        // restart neo with the new location
                        neoManager.stopNeoService();
                        neoManager.startNeoService();
                    }
                    else if (NeoPreferences.NODE_PROPERTY_NAMES.equals(property))
                    {
                        NeoGraphLabelProvider.readNodePropertyNames();
                    }
                    else if (NeoPreferences.NODE_ICON_LOCATION.equals(property))
                    {
                        NeoGraphLabelProvider.readNodeIconLocation();
                    }
                    else if (NeoPreferences.NODE_ICON_PROPERTY_NAMES.equals(property))
                    {
                        NeoGraphLabelProvider.readNodeIconPropertyNames();
                    }
                }});

    }

    /**
     * Stops the plug-in and shuts down the neo service.
     */
    public void stop( BundleContext context ) throws Exception
    {
        plugin = null;
        neoManager.stopNeoService();
        super.stop( context );
    }

    /**
     * Returns the shared instance.
     * @return the shared instance
     */
    public static Activator getDefault()
    {
        return plugin;
    }

    /**
     * Returns the service manager.
     */
    public NeoServiceManager getNeoServiceManager()
    {
        return neoManager;
    }
}
