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
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.sqleditor.results.CellRange;
import net.sourceforge.sqlexplorer.sqleditor.results.ResultsTableAction;
import net.sourceforge.sqlexplorer.sqleditor.results.AbstractResultsTable.SelectionType;
import net.sourceforge.sqlexplorer.util.ImageUtil;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;

/**
 * Copy the column name of the selected column to the clipboard.
 * 
 * @author Davy Vanherbergen
 */
public class CopyColumnNameAction extends ResultsTableAction {

    public CopyColumnNameAction() {
    	super(Messages.getString("DataSetTable.Actions.CopyColumnName"), ImageUtil.getDescriptor("Images.ExportToClipBoardIcon"));
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run() {
        try {
            Clipboard clipBoard = new Clipboard(Display.getCurrent());
            TextTransfer textTransfer = TextTransfer.getInstance();
            
            CellRange cells = getResultsTable().getSelection(SelectionType.COLUMN);
            if (cells == null)
            	return;
            clipBoard.setContents(new Object[] { cells.getColumn(0).getCaption() }, new Transfer[] { textTransfer });
        } catch (Exception e) {
            SQLExplorerPlugin.error("Error exporting cell to clipboard ", e);
        }
    }


    /**
     * Only show action if something is selected
     * @see net.sourceforge.sqlexplorer.sqleditor.results.ResultsTableAction#isAvailable()
     */
    public boolean isAvailable() {
        return getResultsTable().getSelection(SelectionType.COLUMN) != null;
    }

    
    
}
