/**
 * 
 */
package net.sourceforge.sqlexplorer.sqleditor.results;

/**
 * CellRangeRow represents a row in a CellRange; this works with CellRangeRow to
 * allow selections to be implemented.
 * 
 * CellRangeRow supports tree heirarchies, but this is optional.
 * 
 * @author John Spackman
 *
 */
public interface CellRangeRow {
	
	/**
	 * Returns the value of a given cell
	 * @param colIndex The zero-based column number
	 * @return
	 */
	public Object getCellValue(int colIndex);
	
	public boolean hasChildRows();
	
	public CellRangeRow[] getChildRows();
	
	public CellRangeRow getParentRow();
	
}