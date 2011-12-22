/*
 * Copyright (C) 2006 Davy Vanherbergen
 * dvanherbergen@users.sourceforge.net
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
package net.sourceforge.sqlexplorer.dbstructure.nodes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.squirrel_sql.fw.sql.ITableInfo;
import net.sourceforge.squirrel_sql.fw.sql.IndexInfo;

public class IndexFolderNode extends AbstractFolderNode {

    private ITableInfo _tableInfo;


    /**
     * Create new database table node.
     * 
     * @param parent node
     * @param name of this node
     * @param sessionNode session for this node
     */
    public IndexFolderNode(INode parent, ITableInfo tableInfo) {
    	super(parent, Messages.getString("DatabaseStructureView.node.Indexes"), parent.getSession(), "index_folder");
        _tableInfo = tableInfo;
    }


    /**
     * @return List of column names for this table.
     */
    public List<String> getIndexNames() {

        List<String> indexNames = new ArrayList<String>();

        try {
            List<IndexInfo> infos = _session.getMetaData().getIndexInfo(_tableInfo);
            for (IndexInfo info : infos) {
                String name = info.getSimpleName();
                if (!(name == null || indexNames.contains(name))) {
                    indexNames.add(name);
                }
            }

        } catch (Exception e) {
            SQLExplorerPlugin.error("Could not load index names", e);
        }
        return indexNames;
    }


    public String getName() {

        return _name;
    }


    /**
     * @return Qualified table name
     */
    public String getQualifiedName() {

        return getParent().getQualifiedName() + "." + getType();
    }
    /**
     * 
     * 
     * @see net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractNode#loadChildren()
     */
    public void loadChildren() {

        try {
            Iterator<String> it = getIndexNames().iterator();
            while (it.hasNext()) {
                addChildNode(new IndexNode(this, (String) it.next(), _session, (TableNode) getParent()));
            }
        } catch (Exception e) {
            SQLExplorerPlugin.error("Could not create child nodes for " + getName(), e);
        }
    }

}
