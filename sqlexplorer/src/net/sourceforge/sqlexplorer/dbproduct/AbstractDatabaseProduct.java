package net.sourceforge.sqlexplorer.dbproduct;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.sourceforge.sqlexplorer.parsers.NamedParameter;

public abstract class AbstractDatabaseProduct implements DatabaseProduct {
	
	/* (non-Javadoc)
	 * @see net.sourceforge.sqlexplorer.dbproduct.DatabaseProduct#describeConnection(java.sql.Connection)
	 */
	public String describeConnection(Connection connection) throws SQLException {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.sqlexplorer.dbproduct.DatabaseProduct#describeConnection(java.sql.Connection)
	 */
	public String getCurrentCatalog(Connection connection) throws SQLException {
		return connection.getCatalog();
	}
	/**
	 * Configures the statement with a given parameter at a given ordinal index
	 * @param stmt
	 * @param param
	 * @param columnIndex
	 * @throws SQLException 
	 */
	public void configureStatement(CallableStatement stmt, NamedParameter param, int columnIndex) throws SQLException {
		param.configureStatement(stmt, columnIndex);
	}
	
	/**
	 * Override this method if the underlying database supports parameters returning resultsets (ie cursors)
	 * @param stmt 
	 * @param param
	 * @param columnIndex 
	 * @return
	 * @throws SQLException 
	 */
	public ResultSet getResultSet(CallableStatement stmt, NamedParameter param, int columnIndex) throws SQLException {
		return null;
	}
	
	/**
	 * set catalog in given connection
	 * 
	 * @param connection the SQLConnection to the database
	 * @param catalogName name of catalog to set
	 * @return
	 * @throws SQLException 
	 */
	public void setCurrentCatalog(SQLConnection connection, String catalogName) throws SQLException
	{
		connection.setCatalog(catalogName);
	}
	
}
