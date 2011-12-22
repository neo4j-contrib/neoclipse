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
package net.sourceforge.sqlexplorer.sqleditor.results;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Abstract implementation for a context menu action uses in a ResultsTable.
 * Extend this class to add new actions to the ResultsTable.
 * 
 * @author Davy Vanherbergen
 */
public abstract class ResultsTableAction extends GenericAction {

    private AbstractResultsTable resultsTable;

	public ResultsTableAction() {
		super();
	}

	public ResultsTableAction(String text, ImageDescriptor image) {
		super(text, image);
	}

	public ResultsTableAction(String text, int style) {
		super(text, style);
	}

	public ResultsTableAction(String text) {
		super(text);
	}

	public AbstractResultsTable getResultsTable() {
		return resultsTable;
	}

	public void setResultsTable(AbstractResultsTable resultsTab) {
		this.resultsTable = resultsTab;
	}
    
}
