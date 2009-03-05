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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * This enum manages neo icons.
 * @author Peter H&auml;nsgen
 * @author Anders Nawroth
 */
public enum NeoIcons
{
    NEO( "small", path( "obj16/neo-16.png" ) ),
    NEO_ROOT( "root", path( "enabled/home.gif" ) ),
    HELP( "help", shared( ISharedImages.IMG_LCL_LINKTO_HELP ) ),
    PREFERENCES( "preferences", path( "obj16/preferences.gif" ) ),
    // navigation
    HOME( "home", path( "enabled/home.gif" ) ),
    BACK_ENABLED( "back_enabled", shared( ISharedImages.IMG_TOOL_BACK ) ),
    BACK_DISABLED( "back_disabled",
        shared( ISharedImages.IMG_TOOL_BACK_DISABLED ) ),
    FORWARD_ENABLED( "forward_enabled", shared( ISharedImages.IMG_TOOL_FORWARD ) ),
    FORWARD_DISABLED( "forward_disabled",
        shared( ISharedImages.IMG_TOOL_FORWARD_DISABLED ) ), REFRESH(
        "refresh", path( "enabled/refresh.gif" ) ),
    ZOOM( "zoom", path( "enabled/zoom.gif" ) ),
    // traversal depth
    PLUS_ENABLED( "plus_enabled", path( "enabled/plus.gif" ) ), PLUS_DISABLED(
        "plus_disabled", path( "disabled/plus.gif" ) ), MINUS_ENABLED(
        "minus_enabled", path( "enabled/minus.gif" ) ),
    MINUS_DISABLED( "minus_disabled", path( "disabled/minus.gif" ) ),
    // layouts
    GRID( "grid", path( "enabled/grid.gif" ) ), RADIAL( "radial",
        path( "enabled/radial.gif" ) ), SPRING( "spring",
        path( "enabled/spring.gif" ) ),
    TREE( "tree", path( "enabled/tree.gif" ) ),
    // edit actions
    DELETE( "delete", shared( ISharedImages.IMG_TOOL_DELETE ) ), COPY( "copy",
        shared( ISharedImages.IMG_TOOL_COPY ) ), NEW( "new",
        shared( ISharedImages.IMG_TOOL_NEW_WIZARD ) ), RENAME( "rename",
        path( "obj16/change.gif" ) ), PASTE( "paste",
        shared( ISharedImages.IMG_TOOL_PASTE ) ), CHECKED( "checked",
        path( "obj16/box_checked.gif" ) ), UNCHECKED( "unchecked",
        path( "obj16/box_unchecked.gif" ) ),
    // relationship types actions
    ADD( "add", path( "obj16/add.gif" ) ), ADD_INCOMING( "add_incoming",
        path( "obj16/add_incoming.gif" ) ), ADD_OUTGOING( "add_outgoing",
        path( "obj16/add_outgoing.gif" ) ), HIGHLIGHT( "highlight",
        path( "obj16/highlight.gif" ) ), HIGHLIGHT_INCOMING(
        "highlight_incoming", path( "obj16/highlight_incoming.gif" ) ),
    HIGHLIGHT_OUTGOING( "highlight_outgoing",
        path( "obj16/highlight_outgoing.gif" ) ), INCOMING( "incoming",
        path( "obj16/incoming.gif" ) ), OUTGOING( "outgoing",
        path( "obj16/outgoing.gif" ) ), CLEAR_ENABLED( "clear_enabled",
        path( "enabled/clear.gif" ) ), CLEAR_DISABLED( "clear_disabled",
        path( "disabled/clear.gif" ) );

    private static final String ICON_LOCATION = "icons/";
    private static ImageRegistry reg = null;
    private static ISharedImages sharedImages = null;

    private final String key;

    private NeoIcons( final String key, final ImageDescriptor imgDes )
    {
        this.key = key;
        getRegistry().put( key, imgDes );
    }

    private NeoIcons( final String key, final Image image )
    {
        this.key = key;
        getRegistry().put( key, image );
    }

    private ImageRegistry getRegistry()
    {
        if ( reg == null )
        {
            reg = Activator.getDefault().getImageRegistry();
        }
        return reg;
    }

    private static Image shared( final String name )
    {
        return getShared().getImage( name );
    }

    private static ISharedImages getShared()
    {
        if ( sharedImages == null )
        {
            sharedImages = PlatformUI.getWorkbench().getSharedImages();
        }
        return sharedImages;
    }

    private static ImageDescriptor path( final String path )
    {
        return Activator.imageDescriptorFromPlugin( Activator.PLUGIN_ID,
            ICON_LOCATION + path );
    }

    /**
     * Get image descriptor of icon.
     * @return icon image descriptor
     */
    public ImageDescriptor descriptor()
    {
        return getRegistry().getDescriptor( key );
    }

    /**
     * Get icon image.
     * @return icon image
     */
    public Image image()
    {
        return getRegistry().get( key );
    }
}
