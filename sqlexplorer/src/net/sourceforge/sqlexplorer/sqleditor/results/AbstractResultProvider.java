package net.sourceforge.sqlexplorer.sqleditor.results;

import java.util.ArrayList;

/**
 * Implementation of ResultProvider that stores arbitrary columns
 * @author John Spackman
 *
 */
public abstract class AbstractResultProvider implements ResultProvider {
	
	// The columns
	private ArrayList<Column> columns = new ArrayList<Column>();
	
	/**
	 * Adds a column
	 * @param column
	 */
	public void addColumn(Column column) {
		columns.add(column);
	}

	public Column getColumn(int colIndex) {
		return columns.get(colIndex);
	}

	public int getNumberOfColumns() {
		return columns.size();
	}

	public boolean refresh() {
		return false;
	}

	public boolean sortData(int colIndex, int direction) {
		return false;
	}

}
