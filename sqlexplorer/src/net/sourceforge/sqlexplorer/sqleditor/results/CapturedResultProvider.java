package net.sourceforge.sqlexplorer.sqleditor.results;

import java.util.ArrayList;

/**
 * Implementation of ResultProvider that stores all the rows
 * @author John Spackman
 *
 */
public class CapturedResultProvider extends AbstractResultProvider {
	
	private ArrayList<CellRangeRow> rows = new ArrayList<CellRangeRow>();

	/**
	 * Adds a row
	 * @param row
	 */
	public void addRow(CellRangeRow row) {
		rows.add(row);
	}
	
	public int getNumberOfRows() {
		return rows.size();
	}

	public CellRangeRow[] getRows() {
		return rows.toArray(new CellRangeRow[0]);
	}

}
