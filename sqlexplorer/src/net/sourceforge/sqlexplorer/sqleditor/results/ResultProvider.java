package net.sourceforge.sqlexplorer.sqleditor.results;

/**
 * ResultProvider is the source of a grid of data
 * @author John Spackman
 *
 */
public interface ResultProvider extends CellRange {

	/**
	 * Sorts the data
	 * @param colIndex
	 * @param direction
	 * @return True if sort was possible
	 */
	public boolean sortData(int colIndex, int direction);
	
	/**
	 * Refreshes the data
	 * @return True if refresh was possible
	 */
	public boolean refresh();
}
