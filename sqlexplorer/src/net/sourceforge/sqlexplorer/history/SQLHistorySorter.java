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
package net.sourceforge.sqlexplorer.history;

import java.util.Comparator;

import org.eclipse.swt.SWT;

/**
 * SQLHistorySorter.
 * 
 * @author Davy Vanherbergen
 */
public class SQLHistorySorter implements Comparator<SQLHistoryElement> {

    private int _direction = SWT.DOWN;

    private int _sortColumn = 1;


    public SQLHistorySorter() {

    }


    public int compare(SQLHistoryElement e1, SQLHistoryElement e2) {

        SQLHistoryElement el1 = (SQLHistoryElement) e1;
        SQLHistoryElement el2 = (SQLHistoryElement) e2;

        int result = 0;

        switch (_sortColumn) {

            case 1:
                if (el1.getTime() == el2.getTime()) {
                    result = 0;
                } else if (el1.getTime() < el2.getTime()) {
                    result = -1;
                } else {
                    result = 1;
                }
                break;
            case 2:
                result = el1.getSessionDescription().compareTo(el2.getSessionDescription());
                break;
            case 3:
                result = new Integer(el1.getExecutionCount()).compareTo(new Integer(el2.getExecutionCount()));
                break;
            default:
                result = el1.getSingleLineText().compareTo(el2.getSingleLineText());
                break;

        }

        if (_direction == SWT.DOWN) {
            return result * -1;
        }
        return result;

    }


    /**
     * @param column
     */
    public void setSortColumn(int column, int direction) {

        _sortColumn = column;
        _direction = direction;

    }

}
