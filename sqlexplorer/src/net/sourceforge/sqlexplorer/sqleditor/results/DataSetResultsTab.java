package net.sourceforge.sqlexplorer.sqleditor.results;

import net.sourceforge.sqlexplorer.ExplorerException;
import net.sourceforge.sqlexplorer.dataset.DataSetRow;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.views.DataPreviewView;

import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbenchPage;

public class DataSetResultsTab extends TableResultsTable {
	
	public DataSetResultsTab(ResultProvider resultProvider) {
		super(resultProvider);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.sqlexplorer.sqleditor.results.TableResultsTab#onSelectCell(org.eclipse.swt.custom.TableCursor)
	 */
	@Override
	protected void onSelectCell(TableCursor cursor) 
	{
		super.onSelectCell(cursor);
        // Show the preview
		IWorkbenchPage page = SQLExplorerPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
		if (page != null) {
	        DataPreviewView view = (DataPreviewView)page.findView(DataPreviewView.class.getName());
	        if (view != null) {
	        	TableItem row = cursor.getRow();
	        	int column = cursor.getColumn();
	        	DataSetRow dsRow = (DataSetRow)row.getData();
	        	Object obj = dsRow.getCellValue(column);
	        	try {
	        		view.previewData(null, obj);
	        	}catch(ExplorerException ex) {
	        		SQLExplorerPlugin.error(ex.getMessage(), ex);
	        	}
	        }
		}
	}
}
