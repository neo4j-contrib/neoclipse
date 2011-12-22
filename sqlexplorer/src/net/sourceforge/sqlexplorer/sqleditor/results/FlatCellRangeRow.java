package net.sourceforge.sqlexplorer.sqleditor.results;

/**
 * Implementation of CellRangeRow that does not support heirarchy children
 * @author John Spackman
 *
 */
public abstract class FlatCellRangeRow implements CellRangeRow {

	public CellRangeRow[] getChildRows() {
		return null;
	}

	public CellRangeRow getParentRow() {
		return null;
	}

	public boolean hasChildRows() {
		return false;
	}

}
