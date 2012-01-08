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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * This enum manages neo icons.
 * 
 * @author Peter H&auml;nsgen
 * @author Anders Nawroth
 * @author Radhakrishna Kalyan
 */
public enum Icons
{
    NEO( "small", path( "enabled/neo.gif" ) ),
    NEO_ROOT( "root", path( "enabled/home.gif" ) ),
    HELP( "help", shared( ISharedImages.IMG_LCL_LINKTO_HELP ) ),
    PREFERENCES_ENABLED( "preferences_enabled", path( "enabled/preferences.gif" ) ),
    PREFERENCES_DISABLED( "preferences_disabled", path( "disabled/preferences.gif" ) ),
    PROPERTIES_ENABLED( "properties_enabled", path( "enabled/properties.gif" ) ),
    PROPERTIES_DISABLED( "properties_disabled", path( "disabled/properties.gif" ) ),
    GRAPH_ENABLED( "graph_enabled", path( "enabled/neo.gif" ) ),
    GRAPH_DISABLED( "graph_disabled", path( "disabled/neo.gif" ) ),
    HELP_VIEW( "help_view", path( "obj16/help_view.gif" ) ),
    HELP_WINDOW_ENABLED( "help_window_enabled", path( "enabled/help_window.gif" ) ),
    HELP_WINDOW_DISABLED( "help_window_disabled", path( "disabled/help_window.gif" ) ),
    SEARCH_ENABLED( "search_enabled", path( "enabled/search.gif" ) ),
    SEARCH_DISABLED( "search_disabled", path( "disabled/search.gif" ) ),
    CONNECTIONS_ENABLED( "connections_enabled", path( "enabled/newconnection.gif" ) ),
    CONNECTIONS_DISABLED( "connection_disabled", path( "disabled/newconnection.gif" ) ),
    // connectivity
    NEW_ALIAS_ENABLED( "new_alias_enabled", path( "enabled/new_alias.gif" ) ),
    NEW_ALIAS_DISABLED( "new_alias_disabled", path( "disabled/new_alias.gif" ) ),
    EDIT_ALIAS_ENABLED( "edit_alias_enabled", path( "enabled/edit_alias.gif" ) ),
    EDIT_ALIAS_DISABLED( "edit_alias_disabled", path( "disabled/edit_alias.gif" ) ),
    START_ENABLED( "start_enabled", path( "enabled/start.gif" ) ),
    STOP_ENABLED( "stop_enabled", path( "enabled/stop.gif" ) ),
    START_DISABLED( "start_disabled", path( "disabled/start.gif" ) ),
    STOP_DISABLED( "stop_disabled", path( "disabled/stop.gif" ) ),
    // navigation
    HOME( "home", path( "enabled/home.gif" ) ),
    BACK_ENABLED( "back_enabled", shared( ISharedImages.IMG_TOOL_BACK ) ),
    BACK_DISABLED( "back_disabled", shared( ISharedImages.IMG_TOOL_BACK_DISABLED ) ),
    FORWARD_ENABLED( "forward_enabled", shared( ISharedImages.IMG_TOOL_FORWARD ) ),
    FORWARD_DISABLED( "forward_disabled", shared( ISharedImages.IMG_TOOL_FORWARD_DISABLED ) ),
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
    COPY_ENABLED( "copy_enabled", path( "enabled/copy.gif" ) ),
    COPY_DISABLED( "copy_disabled", path( "disabled/copy.gif" ) ),
    NEW_ENABLED( "new_enabled", path( "enabled/new.gif" ) ),
    NEW_DISABLED( "new_disabled", path( "disabled/new.gif" ) ),
    RENAME_ENABLED( "rename_enabled", path( "enabled/rename.gif" ) ),
    RENAME_DISABLED( "rename_disabled", path( "disabled/rename.gif" ) ),
    PASTE_ENABLED( "paste_enabled", path( "enabled/paste.gif" ) ),
    PASTE_DISABLED( "paste_disabled", path( "disabled/paste.gif" ) ),
    CHECKED( "checked", path( "obj16/box_checked.gif" ) ),
    UNCHECKED( "unchecked", path( "obj16/box_unchecked.gif" ) ),
    COMMIT_ENABLED( "commit_enabled", path( "enabled/commit.gif" ) ),
    COMMIT_DISABLED( "commit_disabled", path( "disabled/commit.gif" ) ),
    ROLLBACK_ENABLED( "rollback_enabled", path( "enabled/rollback.gif" ) ),
    ROLLBACK_DISABLED( "rollback_disabled", path( "disabled/rollback.gif" ) ),
    SYNC_ENABLED( "sync-enabled", path( "enabled/sync.gif" ) ),
    SYNC_DISABLED( "sync-disabled", path( "disabled/sync.gif" ) ),
    ADD_NODE_LABEL_ENABLED( "add-label-enabled", path( "enabled/filter.gif" ) ),
    ADD_NODE_LABEL_DISABLED( "add-label-enabled", path( "disabled/filter.gif" ) ),
    // relationship types actions
    ADD_ENABLED( "add_enabled", path( "enabled/add-relationship.gif" ) ),
    ADD_DISABLED( "add_disabled", path( "disabled/add-relationship.gif" ) ),
    ADD_INCOMING_ENABLED( "add_incoming_enabled", path( "enabled/add-relationship-incoming.gif" ) ),
    ADD_INCOMING_DISABLED( "add_incoming_disabled", path( "disabled/add-relationship-incoming.gif" ) ),
    ADD_OUTGOING_ENABLED( "add_outgoing_enabled", path( "enabled/add-relationship-outgoing.gif" ) ),
    ADD_OUTGOING_DISABLED( "add_outgoing_disabled", path( "disabled/add-relationship-outgoing.gif" ) ),
    ADD_LOOP_ENABLED( "add_loop_enabled", path( "enabled/add-loop.gif" ) ),
    ADD_LOOP_DISABLED( "add_loop_disabled", path( "disabled/add-loop.gif" ) ),
    HIGHLIGHT_ENABLED( "highlight_enabled", path( "enabled/highlight.gif" ) ),
    HIGHLIGHT_DISABLED( "highlight_disabled", path( "disabled/highlight.gif" ) ),
    HIGHLIGHT_INCOMING_ENABLED( "highlight_incoming_enabled", path( "enabled/highlight-incoming.gif" ) ),
    HIGHLIGHT_INCOMING_DISABLED( "highlight_incoming_disabled", path( "disabled/highlight-incoming.gif" ) ),
    HIGHLIGHT_OUTGOING_ENABLED( "highlight_outgoing_enabled", path( "enabled/highlight-outgoing.gif" ) ),
    HIGHLIGHT_OUTGOING_DISABLED( "highlight_outgoing_disabled", path( "disabled/highlight-outgoing.gif" ) ),
    INCOMING( "incoming", path( "obj16/incoming.gif" ) ),
    OUTGOING( "outgoing", path( "obj16/outgoing.gif" ) ),
    CLEAR_ENABLED( "clear_enabled", path( "enabled/clear.gif" ) ),
    CLEAR_DISABLED( "clear_disabled", path( "disabled/clear.gif" ) ),
    ADD_ICON_INCOMING( "add_icon_in_enabled", path( "enabled/add.gif" ) ),
    ADD_ICON_OUTGOING( "add_icon_out_enabled", path( "enabled/add.gif" ) ),
    DELETE_ICON_INCOMING( "del_icon_in_enabled", path( "enabled/delete.gif" ) ),
    DELETE_ICON_OUTGOING( "del_icon_out_enabled", path( "enabled/delete.gif" ) ),
    // filter
    FILTER_ENABLED( "filter_enabled", path( "enabled/filter.gif" ) ),
    FILTER_DISABLED( "filter_disabled", path( "disabled/filter.gif" ) ),
    // types
    TYPE_BOOLEAN( "boolean", path( "types/type-boolean.gif" ) ),
    TYPE_BYTE( "byte", path( "types/type-byte.gif" ) ),
    TYPE_CHAR( "char", path( "types/type-char.gif" ) ),
    TYPE_DOUBLE( "double", path( "types/type-double.gif" ) ),
    TYPE_FLOAT( "float", path( "types/type-float.gif" ) ),
    TYPE_INT( "int", path( "types/type-int.gif" ) ),
    TYPE_LONG( "long", path( "types/type-long.gif" ) ),
    TYPE_SHORT( "short", path( "types/type-short.gif" ) ),
    TYPE_STRING( "string", path( "types/type-string.gif" ) ),
    TYPE_BOOLEANS( "booleans", path( "types/type-booleans.gif" ) ),
    TYPE_BYTES( "bytes", path( "types/type-bytes.gif" ) ),
    TYPE_CHARS( "chars", path( "types/type-chars.gif" ) ),
    TYPE_DOUBLES( "doubles", path( "types/type-doubles.gif" ) ),
    TYPE_FLOATS( "floats", path( "types/type-floats.gif" ) ),
    TYPE_INTS( "ints", path( "types/type-ints.gif" ) ),
    TYPE_LONGS( "longs", path( "types/type-longs.gif" ) ),
    TYPE_SHORTS( "shorts", path( "types/type-shorts.gif" ) ),
    TYPE_STRINGS( "strings", path( "types/type-strings.gif" ) ),
    TYPE_ID( "id", path( "obj16/key.gif" ) ),
    TYPES_ENABLED( "types_enabled", path( "enabled/types.gif" ) ),
    TYPES_DISABLED( "types_disabled", path( "disabled/types.gif" ) ),
    NEW_TYPE_ENABLED( "new_type_enabled", path( "enabled/add-type.gif" ) ),
    NEW_TYPE_DISABLED( "new_type_disabled", path( "disabled/add-type.gif" ) ),
    CYPHER_EDITOR_ENABLED( "cypher_editor_enabled", path( "enabled/cypher_editor.gif" ) ),
    CYPHER_EDITOR_DISABLED( "cypher_editor_disabled", path( "disabled/cypher_editor.gif" ) ),
    FORCE_START_DISABLED( "force_start_disabled", path( "disabled/force_start.gif" ) ),
    FORCE_START_ENABLED( "force_start_enabled", path( "enabled/force_start.gif" ) ),
    CSV( "csv", path( "enabled/csv.gif" ) ),
    JSON( "json", path( "enabled/json.gif" ) ),
    XML( "xml", path( "enabled/xml.gif" ) ),
    EXECUTE_SQL( "executesql", path( "enabled/execute.gif" ) ),

    ;

    private static final String ICON_LOCATION = "icons/";
    private static ImageRegistry reg = null;
    private static ISharedImages sharedImages = null;

    private final String key;

    private Icons( final String key, final ImageDescriptor imgDes )
    {
        this.key = key;
        getRegistry().put( key, imgDes );
    }

    private Icons( final String key, final Image image )
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
        return Activator.imageDescriptorFromPlugin( Activator.PLUGIN_ID, ICON_LOCATION + path );
    }

    /**
     * Get image descriptor of icon.
     * 
     * @return icon image descriptor
     */
    public ImageDescriptor descriptor()
    {
        return getRegistry().getDescriptor( key );
    }

    /**
     * Get icon image.
     * 
     * @return icon image
     */
    public Image image()
    {
        return getRegistry().get( key );
    }
}
