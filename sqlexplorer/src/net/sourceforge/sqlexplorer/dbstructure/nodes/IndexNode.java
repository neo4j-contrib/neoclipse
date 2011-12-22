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

import java.util.Comparator;
import java.util.List;

import net.sourceforge.sqlexplorer.dbproduct.MetaDataSession;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.squirrel_sql.fw.sql.IndexInfo;
import net.sourceforge.squirrel_sql.fw.sql.IndexInfo.SortOrder;

/**
 * @author Davy Vanherbergen
 * 
 */
public class IndexNode extends AbstractNode {

    private TableNode _parentTable;


    public IndexNode(INode parent, String name, MetaDataSession session, TableNode parentTable) {
    	super(parent, name, session, "index");
        _parentTable = parentTable;
        setImageKey("Images.IndexIcon");
    }


    public Comparator<INode> getComparator() {

        // we don't want any sorting here.
        return null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.sqlexplorer.dbstructure.nodes.INode#getUniqueIdentifier()
     */
    public String getQualifiedName() {

        return _parent.getParent().getName() + "." + _name;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.sqlexplorer.dbstructure.nodes.INode#getUniqueIdentifier()
     */
    public String getUniqueIdentifier() {

        return _parent.getParent().getQualifiedName() + "." + _name;
    }


    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractNode#loadChildren()
     */
    public void loadChildren() {

        try {
            List<IndexInfo> infos = _session.getMetaData().getIndexInfo(_parentTable.getTableInfo());
            for (IndexInfo info : infos) {

                String indexName = info.getSimpleName();
                SortOrder sort = info.getSortOrder();
                                
                if (indexName != null && indexName.equalsIgnoreCase(_name)) {
                    ColumnNode col = new ColumnNode(this, info.getColumnName(), _session, _parentTable, true);
                    if (sort == null || sort == SortOrder.ASC) {
                        col.setLabelDecoration("ASC");
                    } else {
                        col.setLabelDecoration("DESC");
                    }
                    addChildNode(col);
                }
            }

        } catch (Exception e) {
            SQLExplorerPlugin.error("Could not load column names", e);
        }

    }

}
