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
        shared( ISharedImages.IMG_TOOL_FORWARD_DISABLED ) ),
    REFRESH( "refresh", path( "enabled/refresh.gif" ) ),
    ZOOM( "zoom", path( "enabled/zoom.gif" ) ),
    // traversal depth
    PLUS_ENABLED( "plus_enabled", path( "enabled/plus.gif" ) ),
    PLUS_DISABLED( "plus_disabled", path( "disabled/plus.gif" ) ),
    MINUS_ENABLED( "minus_enabled", path( "enabled/minus.gif" ) ),
    MINUS_DISABLED( "minus_disabled", path( "disabled/minus.gif" ) ),
    // layouts
    GRID( "grid", path( "enabled/grid.gif" ) ),
    RADIAL( "radial", path( "enabled/radial.gif" ) ),
    SPRING( "spring", path( "enabled/spring.gif" ) ),
    TREE( "tree", path( "enabled/tree.gif" ) ),
    // edit actions
    DELETE_ENABLED( "delete_enabled", path( "enabled/delete.gif" ) ),
    DELETE_DISABLED( "delete_disabled", path( "disabled/delete.gif" ) ),
    COPY_ENABLED( "copy_enabled", path( "enabled/copy.gif" ) ), COPY_DISABLED(
        "copy_disabled", path( "disabled/copy.gif" ) ), NEW_ENABLED(
        "new_enabled", path( "enabled/new.gif" ) ), NEW_DISABLED(
        "new_disabled", path( "disabled/new.gif" ) ), RENAME( "rename",
        path( "obj16/change.gif" ) ), PASTE_ENABLED( "paste_enabled",
        path( "enabled/paste.gif" ) ), PASTE_DISABLED( "paste_disabled",
        path( "disabled/paste.gif" ) ), CHECKED( "checked",
        path( "obj16/box_checked.gif" ) ), UNCHECKED( "unchecked",
        path( "obj16/box_unchecked.gif" ) ), COMMIT_ENABLED( "commit_enabled",
        path( "enabled/commit.gif" ) ), COMMIT_DISABLED( "commit_disabled",
        path( "disabled/commit.gif" ) ), ROLLBACK_ENABLED( "rollback_enabled",
        path( "enabled/rollback.gif" ) ), ROLLBACK_DISABLED(
        "rollback_disabled", path( "disabled/rollback.gif" ) ),
    // relationship types actions
    ADD_ENABLED( "add_enabled", path( "enabled/add.gif" ) ), ADD_DISABLED(
        "add_disabled", path( "disabled/add.gif" ) ), ADD_INCOMING(
        "add_incoming", path( "obj16/add_incoming.gif" ) ), ADD_OUTGOING(
        "add_outgoing", path( "obj16/add_outgoing.gif" ) ), HIGHLIGHT(
        "highlight", path( "obj16/highlight.gif" ) ), HIGHLIGHT_INCOMING(
        "highlight_incoming", path( "obj16/highlight_incoming.gif" ) ),
    HIGHLIGHT_OUTGOING( "highlight_outgoing",
        path( "obj16/highlight_outgoing.gif" ) ), INCOMING( "incoming",
        path( "obj16/incoming.gif" ) ), OUTGOING( "outgoing",
        path( "obj16/outgoing.gif" ) ), CLEAR_ENABLED( "clear_enabled",
        path( "enabled/clear.gif" ) ), CLEAR_DISABLED( "clear_disabled",
        path( "disabled/clear.gif" ) ), ICON_INCOMING( "icon_in",
        path( "obj16/icon_incoming.gif" ) ), ICON_OUTGOING( "icon_out",
        path( "obj16/icon_outgoing.gif" ) ),
    // types
    TYPE_BOOLEAN( "boolean", path( "types/type-boolean.gif" ) ), TYPE_BYTE(
        "byte", path( "types/type-byte.gif" ) ), TYPE_CHAR( "char",
        path( "types/type-char.gif" ) ), TYPE_DOUBLE( "double",
        path( "types/type-double.gif" ) ), TYPE_FLOAT( "float",
        path( "types/type-float.gif" ) ), TYPE_INT( "int",
        path( "types/type-int.gif" ) ), TYPE_LONG( "long",
        path( "types/type-long.gif" ) ), TYPE_SHORT( "short",
        path( "types/type-short.gif" ) ), TYPE_STRING( "string",
        path( "types/type-string.gif" ) ), TYPE_BOOLEANS( "booleans",
        path( "types/type-booleans.gif" ) ), TYPE_BYTES( "bytes",
        path( "types/type-bytes.gif" ) ), TYPE_CHARS( "chars",
        path( "types/type-chars.gif" ) ), TYPE_DOUBLES( "doubles",
        path( "types/type-doubles.gif" ) ), TYPE_FLOATS( "floats",
        path( "types/type-floats.gif" ) ), TYPE_INTS( "ints",
        path( "types/type-ints.gif" ) ), TYPE_LONGS( "longs",
        path( "types/type-longs.gif" ) ), TYPE_SHORTS( "shorts",
        path( "types/type-shorts.gif" ) ), TYPE_STRINGS( "strings",
        path( "types/type-strings.gif" ) ), TYPE_ID( "id",
        path( "obj16/key.gif" ) ), TYPES( "types", path( "types/types.gif" ) );

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
