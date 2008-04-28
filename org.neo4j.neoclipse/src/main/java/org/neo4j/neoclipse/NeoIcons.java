/*
 * NeoIcons.java
 */
package org.neo4j.neoclipse;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

/**
 * This class manages neo icons.
 * 
 * @author	Peter H&auml;nsgen
 */
public class NeoIcons
{
    /*
     * Some constants representing icons.
     */
    public static final String HOME     = "home";
    public static final String REFRESH  = "refresh";    
    public static final String ZOOM     = "zoom";

    public static final String PLUS_ENABLED     = "plus_enabled";
    public static final String PLUS_DISABLED    = "plus_disabled";
    public static final String MINUS_ENABLED    = "minus_enabled";
    public static final String MINUS_DISABLED   = "minus_disabled";

    public static final String GRID     = "grid";
    public static final String RADIAL   = "radial";
    public static final String SPRING   = "spring";
    public static final String TREE     = "tree";
    
    public static final String NEO      = "small";
    public static final String NEO_ROOT = "root";
    
    /**
     * The image registry.
     */
    protected static ImageRegistry reg;
    
    /**
     * Initializes the neo images.
     */
    public static void init(Activator activator)
    {
        reg = activator.getImageRegistry();
        
        // TODO use neo icons
        reg.put(NeoIcons.NEO, Activator.imageDescriptorFromPlugin(
                Activator.PLUGIN_ID, "icons/obj16/neo.ico"));

        reg.put(NeoIcons.NEO_ROOT, Activator.imageDescriptorFromPlugin(
                Activator.PLUGIN_ID, "icons/obj16/neo_red.ico"));

        // misc
        reg.put(NeoIcons.HOME, Activator.imageDescriptorFromPlugin(
                Activator.PLUGIN_ID, "icons/enabled/home.gif"));

        reg.put(NeoIcons.REFRESH, Activator.imageDescriptorFromPlugin(
                Activator.PLUGIN_ID, "icons/enabled/refresh.gif"));

        reg.put(NeoIcons.ZOOM, Activator.imageDescriptorFromPlugin(
                Activator.PLUGIN_ID, "icons/enabled/zoom.gif"));

        // traversal depth
        reg.put(NeoIcons.PLUS_ENABLED, Activator.imageDescriptorFromPlugin(
                Activator.PLUGIN_ID, "icons/enabled/plus.gif"));

        reg.put(NeoIcons.PLUS_DISABLED, Activator.imageDescriptorFromPlugin(
                Activator.PLUGIN_ID, "icons/disabled/plus.gif"));

        reg.put(NeoIcons.MINUS_ENABLED, Activator.imageDescriptorFromPlugin(
                Activator.PLUGIN_ID, "icons/enabled/minus.gif"));
        
        reg.put(NeoIcons.MINUS_DISABLED, Activator.imageDescriptorFromPlugin(
                Activator.PLUGIN_ID, "icons/disabled/minus.gif"));

        // layouts
        reg.put(NeoIcons.GRID, Activator.imageDescriptorFromPlugin(
                Activator.PLUGIN_ID, "icons/enabled/grid.gif"));
        
        reg.put(NeoIcons.RADIAL, Activator.imageDescriptorFromPlugin(
                Activator.PLUGIN_ID, "icons/enabled/radial.gif"));
        
        reg.put(NeoIcons.SPRING, Activator.imageDescriptorFromPlugin(
                Activator.PLUGIN_ID, "icons/enabled/spring.gif"));

        reg.put(NeoIcons.TREE, Activator.imageDescriptorFromPlugin(
                Activator.PLUGIN_ID, "icons/enabled/tree.gif"));
    }
    
    /**
     * Looks up the image for the given name.
     */
    public static Image getImage(String name)
    {
        return reg.get(name);
    }
    
    /**
     * Looks up the image descriptor for the given name.
     */
    public static ImageDescriptor getDescriptor(String name)
    {
        return reg.getDescriptor(name);
    }
}
