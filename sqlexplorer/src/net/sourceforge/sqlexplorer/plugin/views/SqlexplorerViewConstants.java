/*
 * Copyright (C) 2007 SQL Explorer Development Team
 * http://sourceforge.net/projects/eclipsesql
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sourceforge.sqlexplorer.plugin.views;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.sqlexplorer.connections.ConnectionsView;

/**
 * Constants that define all the available views in this plugin, and what we
 * want the default views to be. Each view constant is the string ID provided in
 * the plugin.xml file.
 * 
 * @author Macon Pegram
 */
public final class SqlexplorerViewConstants {

    private static SqlexplorerViewConstants singleton = null;

    public static final String SQLEXPLORER_CONNECTIONS = ConnectionsView.class.getName();

    public static final String SQLEXPLORER_DBSTRUCTURE = DatabaseStructureView.class.getName();
    
    public static final String SQLEXPLORER_DBDETAIL = DatabaseDetailView.class.getName();

    public static final String SQLEXPLORER_SQLHISTORY = SQLHistoryView.class.getName();

    
    
    /** Collection of the default views which this plugin offers */
    private List<String> defaultViewList = null;

    /** Collection of all the views which this plugin offers */
    private List<String> fullViewList = null;


    /**
     * Don't allow public construction.
     */
    private SqlexplorerViewConstants() {
        super();
        setupDefaultViewList();
        setupFullViewList();
    }


    /**
     * Sets up the list of views the user will should see by default
     * 
     * @return List - Collection of view ids used by default.
     */
    private void setupDefaultViewList() {
        defaultViewList = new ArrayList<String>(7);
        defaultViewList.add(SQLEXPLORER_CONNECTIONS);
        defaultViewList.add(SQLEXPLORER_DBSTRUCTURE);
        defaultViewList.add(SQLEXPLORER_DBDETAIL);
        defaultViewList.add(SQLEXPLORER_SQLHISTORY);
    }


    /**
     * Sets up the list of views the user will should see by default
     * 
     * @return List - Collection of view ids used by default.
     */
    private void setupFullViewList() {
        fullViewList = new ArrayList<String>(getDefaultViewList());
    }


    /**
     * Accessor for singleton instance. Lazy constructs the singleton if needed.
     * 
     * @return SqlexplorerViewConstants singleton
     */
    public static SqlexplorerViewConstants getInstance() {
        if (singleton == null)
            singleton = new SqlexplorerViewConstants();

        return singleton;
    }


    /**
     * Returns the list of default view Ids which we want to see by default when
     * a perspective is opened for the first time.
     * 
     * @return List
     */
    public List<String> getDefaultViewList() {
        return defaultViewList;
    }


    /**
     * Returns the List of all available view Ids
     * 
     * @return List
     */
    public List<String> getFullViewList() {
        return fullViewList;
    }

}
