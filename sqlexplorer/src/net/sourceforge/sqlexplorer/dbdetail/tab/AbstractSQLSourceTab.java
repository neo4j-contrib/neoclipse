/**
 * 
 */
package net.sourceforge.sqlexplorer.dbdetail.tab;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;


/**
 * @author k709335
 *
 */
public abstract class AbstractSQLSourceTab extends AbstractSourceTab {

    public abstract String getSQL();
    
    public abstract Object[] getSQLParameters();


    public String getSource() {
        
        String source = null;
        
        SQLConnection connection = null;
        ResultSet rs = null;
        Statement stmt = null;
        PreparedStatement pStmt = null;
        
        int timeOut = SQLExplorerPlugin.getIntPref(IConstants.INTERACTIVE_QUERY_TIMEOUT);
        
        try {
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
                rs = stmt.executeQuery(getSQL());
                
            } else {
                
                // use prepared statement
                pStmt = connection.prepareStatement(getSQL());
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
        
            source = "";
            while (rs.next()) {
                
                source = source + rs.getString(1);
            }
            
            rs.close();
            
        } catch (Exception e) {
            
            SQLExplorerPlugin.error("Couldn't load source for: " + getNode().getName(), e);
            
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
        return source;
    }
    
}
