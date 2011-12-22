package net.sourceforge.sqlexplorer.sqleditor.results.actions;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.sqleditor.results.CellRange;
import net.sourceforge.sqlexplorer.sqleditor.results.ResultsTableAction;
import net.sourceforge.sqlexplorer.sqleditor.results.CellRangeRow;
import net.sourceforge.sqlexplorer.sqleditor.results.AbstractResultsTable.SelectionType;

public abstract class AbstractCopyAction extends ResultsTableAction {

	public AbstractCopyAction(String text, ImageDescriptor image) {
		super(text, image);
	}

	public AbstractCopyAction(String text, int style) {
		super(text, style);
	}

	public AbstractCopyAction(String text) {
		super(text);
	}

	public AbstractCopyAction() {
		super();
	}

	protected abstract SelectionType getSelectionType();
	
	@Override
	public void run() {
        BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
            public void run() {
				Clipboard clipBoard = SQLExplorerPlugin.getDefault().getConnectionsView().getClipboard();
				TextTransfer textTransfer = TextTransfer.getInstance();
		
				CellRange cells = getResultsTable().getSelection(getSelectionType());
				if (cells == null)
					return;
				String value;
				if (cells.getNumberOfColumns() == 1 && cells.getNumberOfRows() == 1)
					value = cells.getColumn(0).getDisplayValue(cells.getRows()[0].getCellValue(0));
				else {
		            // get preferences
		            String lineSeparator = System.getProperty("line.separator");
		            String columnSeparator = SQLExplorerPlugin.getDefault().getPreferenceStore().getString(IConstants.CLIP_EXPORT_SEPARATOR);
		            boolean includeColumnNames = SQLExplorerPlugin.getDefault().getPreferenceStore().getBoolean(IConstants.CLIP_EXPORT_COLUMNS);
		            
					StringBuffer sb = new StringBuffer();
					if (includeColumnNames) {
						for (int colIndex = 0; colIndex < cells.getNumberOfColumns(); colIndex++) {
							String str = cells.getColumn(colIndex).getCaption();
							if (str != null && (str.indexOf('"') > -1 || str.indexOf('\'') > -1))
								sb.append('"').append(str).append('"');
							else
								sb.append(str);
							if (colIndex < cells.getNumberOfColumns() - 1)
								sb.append(columnSeparator);
						}
						sb.append(lineSeparator);
					}
		
					CellRangeRow[] rows = cells.getRows();
					for (int rowIndex = 0; rowIndex < cells.getNumberOfRows(); rowIndex++) {
						CellRangeRow row = rows[rowIndex];
						for (int colIndex = 0; colIndex < cells.getNumberOfColumns(); colIndex++) {
							String str = cells.getColumn(colIndex).getDisplayValue(row.getCellValue(colIndex));
							if (str != null) {
								str = str.trim();
								if (str.indexOf('"') > -1 || str.indexOf('\'') > -1)
									sb.append('"').append(str).append('"');
								else
									sb.append(str);
							}
							if (colIndex < cells.getNumberOfColumns() - 1)
								sb.append(columnSeparator);
						}
						if (rowIndex < cells.getNumberOfRows() - 1)
							sb.append(lineSeparator);
					}
					
					value = sb.toString();
				}
				if (value != null)
					value = value.trim();
						
				clipBoard.setContents(new Object[] { value }, new Transfer[] { textTransfer });
            }
        });
	}

	@Override
	public boolean isAvailable() {
		return getResultsTable().getSelection(getSelectionType()) != null;
	}
	
}
