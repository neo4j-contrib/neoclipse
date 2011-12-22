package net.sourceforge.sqlexplorer.dbdetail.tab;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.dataset.DataSet;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public abstract class AbstractSQLTab extends AbstractDataSetTab {
   
    protected static final Log _logger = LogFactory.getLog(AbstractSQLTab.class);

    public final DataSet getDataSet() throws Exception {
        
        DataSet dataSet = null;
        int timeOut = SQLExplorerPlugin.getIntPref(IConstants.INTERACTIVE_QUERY_TIMEOUT);
        
        SQLConnection connection = null;
        ResultSet rs = null;
        Statement stmt = null;
        PreparedStatement pStmt = null;
        
        try {
            // this is done before grabbing the connection, because some implementations
            // require SQL Execution to determine correct SQL
            String sql = getSQL();

            connection = getNode().getSession().grabConnection();
                    
            Object[] params = getSQLParameters();
            if (params == null || params.length == 0) {
                
                
                // use normal statement
                stmt = connection.createStatement();
                try
                {
                	stmt.setQueryTimeout(timeOut);
                }
                catch(Exception ignored)
                {
                	// some postgreSQL drivers does not implement this method
                	// silently ignore this
                }
                rs = stmt.executeQuery(sql);
                
            } else {
                
                // use prepared statement
                pStmt = connection.prepareStatement(sql);
                try
                {
                	pStmt.setQueryTimeout(timeOut);
                }
                catch(Exception ignored)
                {
                	// some postgreSQL drivers does not implement this method
                	// silently ignore this
                }
                
                for (int i = 0; i < params.length; i++) {
                    
                    if (params[i] instanceof String) {
                        pStmt.setString(i + 1, (String) params[i]);
                    } else if (params[i] instanceof Integer) {
                        pStmt.setInt(i + 1, ((Integer) params[i]).intValue());
                    } else if (params[i] instanceof String) {
                        pStmt.setLong(i + 1, ((Long) params[i]).longValue());
                    }                     
                }
                
                rs = pStmt.executeQuery();
            }
        
            dataSet = new DataSet(rs, null);
            
            rs.close();
            rs = null;
            
        } catch (Exception e) {
            
        	dataSet = new DataSet(e.getLocalizedMessage());
            SQLExplorerPlugin.error("Couldn't load source for: " + getLabelText() + " node: " + getNode().getName(), e);
            
        } finally {
            if (rs != null)
            	try {
            		rs.close();
            	}catch(SQLException e) {
            		SQLExplorerPlugin.error("Error closing result set", e);
            	}
            if (stmt != null)
                try {
                    stmt.close();
                } catch (SQLException e) {
                    SQLExplorerPlugin.error("Error closing statement", e);
                }
            if (pStmt != null)
                try {
                    pStmt.close();
                } catch (SQLException e) {
                    SQLExplorerPlugin.error("Error closing statement", e);
                }
            if (connection != null)
            	getNode().getSession().releaseConnection(connection);
        }
        return dataSet;
        
    }

    public abstract String getLabelText();
    
    public abstract String getSQL();
    
    public Object[] getSQLParameters() {
        return null;
    }
}
