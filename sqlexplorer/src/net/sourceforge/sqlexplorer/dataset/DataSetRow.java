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
package net.sourceforge.sqlexplorer.dataset;

import net.sourceforge.sqlexplorer.sqleditor.results.FlatCellRangeRow;

/**
 * DataSetRow, represents one row in a dataSet.
 * 
 * @author Davy Vanherbergen
 */
public class DataSetRow extends FlatCellRangeRow {

	private DataSet dataset;
    private Comparable<?>[] _values;

	/**
     * Create new DataSetRow with columnCount values
     * 
     * @param columnCount number of columns
     */
    public DataSetRow(DataSet dataset) {
    	this.dataset = dataset;
        _values = new Comparable[dataset.getColumns().length];
    }

    /**
     * Create initialized dataSetRow
     * 
     * @param values
     */
    public DataSetRow(DataSet dataset, Comparable<?>[] values) {
    	this.dataset = dataset;
        _values = values;
    }

    /**
     * Returns the raw value of given column.
     * @param column first column is 0
     * @return Raw column value, maybe <tt>null</tt>.
     */
    public Object getCellValue(int column) {
    	return _values[column];
    }

    /**
     * Set the value for a given column
     * 
     * @param column first column is 0
     * @param value
     */
    public void setValue(int column, Comparable<?> value) {
        _values[column] = value;
    }
    
    /**
     * @return number of columns in this row
     */
    public int length() {
        if (_values == null)
            return 0;
        return _values.length;
    }

    public DataSet getDataset() {
		return dataset;
	}
}
