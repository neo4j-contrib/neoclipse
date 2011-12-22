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

import java.util.Comparator;

import org.eclipse.swt.SWT;


/**
 * DataSetTableSorter. 
 * 
 * @author Davy Vanherbergen
 */
public class DataSetTableSorter implements Comparator<DataSetRow> {
   
	private DataSet dataSet;
	
    protected int[] _priorities;

    protected int[] _directions;

    public DataSetTableSorter(DataSet dataSet) {
    	this.dataSet = dataSet;
    	int length = dataSet.getColumns().length;
        _priorities = new int[length];
        _directions = new int[length];

        for (int i = 0; i < length; i++) {
        	_directions[i] = SWT.NONE;
            _priorities[i] = i;
        }

    }

    /**
     * @param column
     */
    public void setTopPriority(int priority, int direction) {
        if (priority < 0 || priority >= _priorities.length)
            return;

        int index = -1;
        for (int i = 0; i < _priorities.length; i++)
            if (_priorities[i] == priority) {
                index = i;
                break;
            }

        if (index == -1)
            return;

        // shift the array
        for (int i = index; i > 0; i--)
            _priorities[i] = _priorities[i - 1];
        
        _priorities[0] = priority;
        _directions[priority] = direction;
    }

    public int compare(DataSetRow e1, DataSetRow e2) {
        return compareColumnValue((DataSetRow) e1, (DataSetRow) e2, 0);
    }

    private int compareColumnValue(DataSetRow m1, DataSetRow m2, int depth) {
        if (depth >= _priorities.length)
            return 0;

        int columnNumber = _priorities[depth];
        int direction = _directions[columnNumber];
        int result = 0;

        // Get the values (may be null)
        Object o1 = m1.getCellValue(columnNumber);
        Object o2 = m2.getCellValue(columnNumber);        
        
        // sort based on null values
    	if (o1 == null || o2 == null) {
    		if (o1 == null && o2 != null)
    			result = 1;
    		else if (o1 != null && o2 == null)
    			result = -1;
    		else
                result = 0;
    		
            if (direction == SWT.DOWN)
            	result *= -1;
            
            return result;
    	}
        
    	if(o1 instanceof String || !(o1 instanceof Comparable))
    	{
    		// use ignore case string comparison
        	// Convert into a viewable, non-null string
            String s1 = dataSet.getColumn(columnNumber).getDisplayValue(o1);
            String s2 = dataSet.getColumn(columnNumber).getDisplayValue(o2);
        	
       		result = s1.compareToIgnoreCase(s2);
    	}
    	else
    	{
    		result = ((Comparable)o1).compareTo((Comparable)o2);
    	}
        if (result == 0)
            return compareColumnValue(m1, m2, depth + 1);
        
        if (direction == SWT.DOWN) {
        	return result * -1;
        }
        return result;
    }

}
