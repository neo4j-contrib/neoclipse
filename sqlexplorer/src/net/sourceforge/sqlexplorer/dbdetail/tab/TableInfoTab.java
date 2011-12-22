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
package net.sourceforge.sqlexplorer.dbdetail.tab;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dataset.DataSet;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.TableNode;
import net.sourceforge.squirrel_sql.fw.sql.ITableInfo;

/**
 * @author Davy Vanherbergen
 * 
 */
public class TableInfoTab extends AbstractDataSetTab {

    
    
    public String getLabelText() {
        return Messages.getString("DatabaseDetailView.Tab.Info");
    }
 
    public DataSet getDataSet() throws Exception {                
        
        INode node = getNode();
        
        if (node == null) {
            return null;
        }
        
        if (node instanceof TableNode) {
            TableNode tableNode = (TableNode) node;
            
            ITableInfo tableInfo = tableNode.getTableInfo();
                       
            String[] header = new String[2];
            header[0] = Messages.getString("DatabaseDetailView.Tab.Info.Property");
            header[1] = Messages.getString("DatabaseDetailView.Tab.Info.Value");
            
            String[][] data = new String[6][2];
            
            data[0][0] = Messages.getString("DatabaseDetailView.Tab.Info.Name");
            data[0][1] = tableInfo.getSimpleName();
            data[1][0] = Messages.getString("DatabaseDetailView.Tab.Info.QualifiedName");
            data[1][1] = tableInfo.getQualifiedName();
            data[2][0] = Messages.getString("DatabaseDetailView.Tab.Info.Catalog");
            data[2][1] = tableInfo.getCatalogName();
            data[3][0] = Messages.getString("DatabaseDetailView.Tab.Info.Schema");
            data[3][1] = tableInfo.getSchemaName();
            data[4][0] = Messages.getString("DatabaseDetailView.Tab.Info.Type");
            data[4][1] = tableInfo.getType();
            data[5][0] = Messages.getString("DatabaseDetailView.Tab.Info.Remarks");
            data[5][1] = tableInfo.getRemarks();
            
            DataSet dataSet = new DataSet(header, data);
                       
            return dataSet;
        }
        
        return null;
    }
    
    
    public String getStatusMessage() {
        return Messages.getString("DatabaseDetailView.Tab.Info.status") + " " + getNode().getQualifiedName();
    }
}
