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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dataset.DataSet;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.TableNode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;

/**
 * @author Davy Vanherbergen
 * 
 */
public class PreviewTab extends AbstractDataSetTab {

    
    
    public String getLabelText() {
        return Messages.getString("DatabaseDetailView.Tab.Preview");
    }
 
    public DataSet getDataSet() throws Exception {                
        
        INode node = getNode();
        
        if (node == null) {
            return null;
        }
        
        if (node instanceof TableNode) {
            TableNode tableNode = (TableNode) node;

            int maxResults = SQLExplorerPlugin.getIntPref(IConstants.PRE_ROW_COUNT);
            if (maxResults == 0) {
                maxResults = 50;
            }
            
            SQLConnection connection = null;
            Statement statement = null;
            ResultSet resultSet = null;
            DataSet dataSet = null;
            try {
            	connection = tableNode.getSession().grabConnection();
                statement = connection.createStatement();
				try {
	    			statement.setMaxRows(maxResults);
				}catch(SQLException e) {
					// Nothing
				}
                statement.execute("select * from " + tableNode.getQualifiedName());
                resultSet = statement.getResultSet();
                
                dataSet = new DataSet(resultSet, null);
            } finally {
                if (resultSet != null)
                	try {
                		resultSet.close();
                	}catch(SQLException e) {
                		SQLExplorerPlugin.error("Error closing result set", e);
                	}
                if (statement != null)
                    try {
                        statement.close();
                    } catch (SQLException e) {
                        SQLExplorerPlugin.error("Error closing statement", e);
                    }
                if (connection != null)
                	getNode().getSession().releaseConnection(connection);
            }
            return dataSet;
        }
        
        return null;
    }
    
    
    public String getStatusMessage() {
        return Messages.getString("DatabaseDetailView.Tab.Preview.status") + " " + getNode().getQualifiedName();
    }
}
