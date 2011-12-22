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
package net.sourceforge.sqlexplorer.sqleditor.results.actions;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.sqleditor.results.AbstractResultsTable.SelectionType;
import net.sourceforge.sqlexplorer.util.ImageUtil;

/**
 * Copy an entire datasettable to the clipboard.
 * 
 * @author Davy Vanherbergen
 */
public class CopyTableAction extends AbstractCopyAction {

	public CopyTableAction() {
		super(Messages.getString("DataSetTable.Actions.CopyToClipboard"), ImageUtil.getDescriptor("Images.CopyIcon"));
	}

	@Override
	protected SelectionType getSelectionType() {
		return SelectionType.ENTIRE_TABLE;
	}

}
