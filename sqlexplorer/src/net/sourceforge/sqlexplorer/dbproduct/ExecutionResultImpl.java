/**
 * 
 */
package net.sourceforge.sqlexplorer.dbproduct;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dataset.DataSet;
import net.sourceforge.sqlexplorer.dbproduct.DatabaseProduct.ExecutionResults;
import net.sourceforge.sqlexplorer.parsers.NamedParameter;

public final class ExecutionResultImpl implements ExecutionResults {
	
	// Current state - IE, which set of results we're currently looking for
	private enum State {
		PRIMARY_RESULTS,		// We're providing the main results, from Statement.getResults()
		SECONDARY_RESULTS,		// We're providing resultsets from Statement.getMoreResults()
		PARAMETER_RESULTS,		// We're returning resultsets from output parameters
		OUTPUT_PARAMETERS,		// We're returning a fake result set listing output parameters
		CLOSED					// All done
	}
	
	/*
	 * Temporary class used by nextDataSet() to collate parameters
	 */
	private class ParamValues {
		private NamedParameter param;
		private ArrayList<Integer> columnIndexes = new ArrayList<Integer>();
		
		public ParamValues(NamedParameter param, int columnIndex) {
			super();
			this.param = param;
			add(columnIndex);
		}
		public void add(int columnIndex) {
			this.columnIndexes.add(new Integer(columnIndex));
		}
	}
	
	private State state = State.PRIMARY_RESULTS;
	private DatabaseProduct product;
	private Statement stmt;
	private LinkedList<NamedParameter> parameters;
	private int maxRows;
	private int paramColumnIndex;
	private Iterator<NamedParameter> paramIter;
	private int updateCount;
	private ResultSet currentResultSet;
	private boolean hasResults;

	public ExecutionResultImpl(DatabaseProduct product, Statement stmt, boolean hasResults, LinkedList<NamedParameter> parameters, int maxRows) throws SQLException {
		super();
		this.product = product;
		this.stmt = stmt;
		this.parameters = parameters;
		this.maxRows = maxRows;
		this.hasResults = hasResults;
		this.state = State.PRIMARY_RESULTS;
	}

	public List<String> getWarnings()
	{
		List<String> result = new ArrayList<String>();
		try {
			SQLWarning warning = (currentResultSet == null) ? stmt.getWarnings() : currentResultSet.getWarnings();
			while(warning != null)
			{
				if(warning.getMessage().trim().length() > 0)
				{
					String msg = warning.getLocalizedMessage();
					if(! (warning.getSQLState() == null && warning.getErrorCode() == 0))
					{
						// no simple text message from Sybase/MsSql, add codes
						msg = "Warning("+warning.getSQLState()+"/"+warning.getErrorCode()+"): "+msg;
					}
					result.add(msg);
				}
				warning = warning.getNextWarning();
				
			}
		} catch (SQLException e) {
			// ignore it
		}
		try {
			if(currentResultSet == null)
			{
				stmt.clearWarnings();
			}
			else
			{
				currentResultSet.clearWarnings();
			}
		} catch (SQLException e) {
			// ignore it;
		}
		return result;
	}
	
	private DataSet createDataSet(ResultSet resultSet) throws SQLException
	{
		DataSet result = new DataSet(resultSet, null, maxRows);
		return result;
	}
	private DataSet createDataSet(int updateCount) throws SQLException
	{
		DataSet result = new DataSet(updateCount);
		return result;
	}
	public DataSet nextDataSet() throws SQLException {
		// Close the current one
		if (currentResultSet != null) {
			currentResultSet.close();
			currentResultSet = null;
		}
		
		// Anything more to do?
		if (state == State.CLOSED)
			return null;
		
		// Get the first set
		if (state == State.PRIMARY_RESULTS) 
		{
			state = State.SECONDARY_RESULTS;
			if(this.hasResults)
			{
				currentResultSet = stmt.getResultSet();
				return createDataSet(currentResultSet);
			}
			else
			{
				int affectedRows = stmt.getUpdateCount();
				if(affectedRows < 0)
				{
					state = State.PARAMETER_RESULTS;
				}
				else
				{
					this.updateCount += affectedRows;
					return createDataSet(updateCount);
				}
			}
		}
		// While we have more secondary results (i.e. those that come directly from Statement but after the first getResults())
		while (state == State.SECONDARY_RESULTS) 
		{
			if (stmt.getMoreResults()) 
			{
				currentResultSet = stmt.getResultSet();
				if (currentResultSet != null)
				{
					return createDataSet(currentResultSet);
				}
			} else {
				int affectedRows = stmt.getUpdateCount();
				if(affectedRows < 0)
				{
					state = State.PARAMETER_RESULTS;
				}
				else
				{
					this.updateCount += affectedRows;
					return createDataSet(updateCount);
				}
				
			}
		}
		
		// Got one? Then exit
		if (currentResultSet != null) {
			this.updateCount += stmt.getUpdateCount();
			return createDataSet(currentResultSet);
		}
		
		// Look for output parameters which return resultsets
		if (state == State.PARAMETER_RESULTS && parameters != null) {
			CallableStatement stmt = (CallableStatement)this.stmt;
			if (paramIter == null) {
				paramIter = parameters.iterator();
				paramColumnIndex = 1;
			}
			while (paramIter.hasNext()) {
				NamedParameter param = paramIter.next();
				if (param.getDataType() == NamedParameter.DataType.CURSOR)
					currentResultSet = product.getResultSet(stmt, param, paramColumnIndex);
				paramColumnIndex++;
				if (currentResultSet != null)
				{
					DataSet result = new DataSet(Messages.getString("DataSet.Cursor") + ' ' + param.getName(), currentResultSet, null, maxRows);
					return result;
				}
				
			}
		}

		// Generate a dataset for output parameters
		state = State.CLOSED;
		if (parameters == null)
			return null;
		if (!(stmt instanceof CallableStatement))
			return null;
		CallableStatement stmt = (CallableStatement)this.stmt;
		TreeMap<NamedParameter, ParamValues> params = new TreeMap<NamedParameter, ParamValues>();
		int columnIndex = 1;
		int numValues = 0;
		for (NamedParameter param : parameters) {
			if (param.getDataType() != NamedParameter.DataType.CURSOR && param.isOutput()) {
				ParamValues pv = params.get(param);
				if (pv == null)
					params.put(param, new ParamValues(param, columnIndex));
				else
					pv.add(columnIndex);
				numValues++;
			}
			columnIndex++;
		}
		if (numValues == 0)
			return null;
		Comparable<?>[][] rows = new Comparable[numValues][2];
		columnIndex = 1;
		int rowIndex = 0;
		for (ParamValues pv : params.values()) {
			int valueIndex = 1;
			for (Integer index : pv.columnIndexes) {
				Comparable<?>[] row = rows[rowIndex++];
				row[0] = pv.param.getName();
				if (pv.columnIndexes.size() > 1)
					row[0] = (pv.param.getName() + '[' + valueIndex + ']');
				else
					row[0] = pv.param.getName();
				row[1] = stmt.getString(index);
				valueIndex++;
			}
		}
		DataSet result = new DataSet(Messages.getString("DataSet.Parameters"), new String[] { 
				Messages.getString("SQLExecution.ParameterName"),
				Messages.getString("SQLExecution.ParameterValue")
			}, rows);
		return result;
	}
	
	public void close() throws SQLException {
		try {
			stmt.close();
		} catch(SQLException e) {
			// Nothing
		}
		if (currentResultSet != null)
		{
			try
			{
				currentResultSet.close();
			}
			catch(SQLException ignored)
			{
			}
		}
	}

	public int getUpdateCount() throws SQLException {
		return updateCount;
	}
}