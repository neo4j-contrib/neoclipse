package net.sourceforge.sqlexplorer.sqleditor.results;

import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.Messages;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

public class TreeResultsTable extends AbstractResultsTable {
	
	private static final String ORIGINAL_COLUMN_INDEX = "original-column-index";
	
	public class MyTreeContentProvider extends TreeContentProviderAdapter {
		
    	@Override
		public Object[] getElements(Object inputElement) {
            ResultProvider provider = (ResultProvider)inputElement;
            return provider.getRows();
		}

		@Override
		public Object[] getChildren(Object element) {
            CellRangeRow row = (CellRangeRow) element;
			return row.getChildRows();
		}

		@Override
		public Object getParent(Object element) {
            CellRangeRow row = (CellRangeRow) element;
			return row.getParentRow();
		}

		@Override
		public boolean hasChildren(Object element) {
            CellRangeRow row = (CellRangeRow) element;
			return row.hasChildRows();
		}
    };
	
	private static final class TreeRow implements CellRangeRow {
		private CellRangeRow row;
		private int offset;

		public TreeRow(CellRangeRow row, int offset) {
			super();
			this.row = row;
			this.offset = offset;
		}

		public Object getCellValue(int colIndex) {
			return row.getCellValue(colIndex + offset);
		}

		public CellRangeRow[] getChildRows() {
			if (!row.hasChildRows())
				return null;
			return row.getChildRows();
		}

		public CellRangeRow getParentRow() {
			return row.getParentRow();
		}

		public boolean hasChildRows() {
			return row.hasChildRows();
		}
	}
	
	private final class TreeCells implements CellRange {
		
		private Rectangle rect;
		private CellRangeRow[] rows;

		public TreeCells(int x1, int y1, int x2, int y2) {
			super();
			rect = new Rectangle(x1, y1, x2 - x1, y2 - y1);
		}

		public int getNumberOfColumns() {
			return rect.width;
		}

		public int getNumberOfRows() {
			return rect.height;
		}

		public CellRangeRow[] getRows() {
			if (rows == null) {
				CellRangeRow[] src = getResultProvider().getRows();
				rows = new CellRangeRow[rect.height];
				for (int i = 0; i < rect.height; i++)
					rows[i] = new TreeRow(src[i + rect.y], rect.x);
			}
			return rows;
		}

		public Column getColumn(int colIndex) {
			return TreeResultsTable.this.getResultProvider().getColumn(colIndex + rect.x);
		}
	}
	
	protected final Listener SORT_LISTENER = new Listener() {
		public void handleEvent(Event e) {
			if (!isSortableByColumn())
				return;
			
			// determine new sort column and direction
			Tree tree = getTreeViewer().getTree();
			TreeColumn sortColumn = tree.getSortColumn();
			TreeColumn currentColumn = (TreeColumn) e.widget;
			int dir = tree.getSortDirection();
			if (sortColumn == currentColumn) {
				dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
			} else {
				tree.setSortColumn(currentColumn);
				dir = SWT.UP;
			}
			
			// sort the data based on column and direction
			int colIndex = (Integer)currentColumn.getData(ORIGINAL_COLUMN_INDEX);
			if (sortData(colIndex, dir)) {
				// update data displayed in table
				tree.setSortDirection(dir);
				tree.clearAll(true);
			}
		}
	};
	
	// Whether the table can be sorted according to a given column
	private boolean sortableByColumn;
	
	// Whether columns can be moved
	private boolean movableColumns;
	
	// The TableViewer
	private TreeViewer treeViewer;
	private TreeTableCursor cursor;
	
	// Status bar components
	private Label statusMessage;
	private Label statusPosition;

	public TreeResultsTable(ResultProvider resultProvider) {
		super(resultProvider);
	}

	public TreeViewer getTreeViewer() {
		return treeViewer;
	}
	
	@Override
	public MenuManager createMenuManager() {
        MenuManager menuManager = new MenuManager("TreeResultsTabContextMenu");
        menuManager.setRemoveAllWhenShown(true);
        Menu contextMenu = menuManager.createContextMenu(treeViewer.getTree());        
        treeViewer.getControl().setMenu(contextMenu);
        return menuManager;
	}
	
	@Override
	protected void createResultsTable(Composite parent) {
        treeViewer = new TreeViewer(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.VIRTUAL);
        Tree tree = treeViewer.getTree();
        
        int numColumns = getResultProvider().getNumberOfColumns();
        String[] columnProps = new String[numColumns];
        for (int colIndex = 0; colIndex < numColumns; colIndex++) {
        	columnProps[colIndex] = Integer.toString(colIndex);
        	createColumn(colIndex);
        }
        treeViewer.setColumnProperties(columnProps);
        
        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);
        ResultProvider provider = getResultProvider();
        if (provider != null) {
	        tree.setItemCount(provider.getNumberOfRows());
	        treeViewer.setContentProvider(new MyTreeContentProvider());
	        treeViewer.setLabelProvider(new MyTableLabelProvider());
	        treeViewer.setInput(provider);
        }
        
        // create a TableCursor to navigate around the table
        cursor = new TreeTableCursor(tree);
        /*
        cursor = new TableCursor(tree, SWT.NONE);
        cursor.setBackground(tree.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
        cursor.setForeground(tree.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT));
        cursor.setLayout(new FillLayout());
        cursor.setVisible(false);
        cursor.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                Table table = treeViewer.getTable();
                
                // when the TableEditor is over a cell, select the corresponding row in 
                // the table
                table.setSelection(new TableItem[] {cursor.getRow()});
                cursor.setVisible(true);
                onSelectCell(cursor);
            }
        });

        // add resize listener for cursor, to stop cursor from
        // taking strange shapes after being table is resized
        cursor.addControlListener(new ControlAdapter() {
            public void controlResized(ControlEvent e) {
                if (cursor != null) {
                    if (cursor.getRow() == null) {
                        cursor.setVisible(false);
                    } else {
                        cursor.layout();
                        cursor.redraw();
                        cursor.setVisible(true);
                    }
                }
            }
        });
        
        // Redisplay the cursor when we get focus
        tree.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                Table t = (Table) e.widget;
                if (t.getItemCount() != 0) {
                    cursor.setVisible(true);
                }
            }           
        });
        cursor.addKeyListener(this);
        cursor.setMenu(contextMenu);
        */

        // refresh tab on F5, copy cell on CTRL-C, etc
        tree.addKeyListener(this);
                        
        // Pack the columns to best-fit size
        tree.pack();
        for (TreeColumn column : tree.getColumns())
            column.pack(); 
	}
	
	protected TreeColumn createColumn(int colIndex) {
        TreeColumn column = new TreeColumn(getTreeViewer().getTree(), SWT.LEFT);           
        column.setText(getResultProvider().getColumn(colIndex).getCaption());
        column.setMoveable(isMovableColumns());
        column.setResizable(true);            
        column.addListener(SWT.Selection, SORT_LISTENER);
        column.setData(ORIGINAL_COLUMN_INDEX, new Integer(colIndex));
        return column;
	}
	
	protected boolean sortData(int colIndex, int direction) {
		return false;
	}
	
	protected void onSelectCell(TableCursor cursor) {
        Tree tree = treeViewer.getTree();
        TreeItem[] items = tree.getSelection();
        if (items == null || items.length == 0)
        	return;
        int rowIndex = tree.indexOf(items[0]) + 1;
        
        // update label with row/column position
        statusPosition.setText(Messages.getString("DatabaseDetailView.Tab.RowPrefix") + " " + rowIndex + 
        		Messages.getString("DatabaseDetailView.Tab.ColumnPrefix") + " " + (cursor.getColumn() + 1));                
        statusPosition.getParent().layout();
        statusPosition.redraw();
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.sqlexplorer.sqleditor.results.ResultsTab#createStatusBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Composite createStatusBar(Composite parent) {
		Composite statusBar = new Composite(parent, SWT.NONE);
		statusBar.setLayout(new GridLayout(2, false));
		
        // add status bar labels
        statusMessage = new Label(statusBar, SWT.NONE);
        statusMessage.setLayoutData(new GridData(SWT.LEFT, SWT.NULL, true, false));
        if (getStatusMessage() != null)
        	statusMessage.setText(getStatusMessage());
        
        statusPosition = new Label(statusBar, SWT.NULL);
        statusPosition.setText("");
        statusPosition.setLayoutData(new GridData(SWT.RIGHT, SWT.NULL, true, false));
        
		return statusBar;
	}
	
	@Override
	public void copyToClipboard() {
		Clipboard clipBoard = SQLExplorerPlugin.getDefault().getConnectionsView().getClipboard();
		TextTransfer textTransfer = TextTransfer.getInstance();

		Tree tree = treeViewer.getTree();
		TreeItem[] items = tree.getSelection();

		if (items == null || items.length == 0) {
			return;
		}

		int columnIndex = cursor.getColumn();
		clipBoard.setContents(new Object[] { items[0].getText(columnIndex) }, new Transfer[] { textTransfer });
	}
	
	@Override
	public CellRange getSelection(SelectionType selection) {
		Tree tree = treeViewer.getTree();
		return new TreeCells(0, 0, tree.getColumnCount() - 1, tree.getItemCount() - 1);
	}

	@Override
	public void refreshContent() {
		// Nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.sqlexplorer.sqleditor.results.ResultsTab#setStatusMessage(java.lang.String)
	 */
	@Override
	public void setStatusMessage(String text) {
		super.setStatusMessage(text);
		if (statusMessage != null)
			statusMessage.setText(text);
	}

	/**
	 * @return the sortableByColumn
	 */
	public boolean isSortableByColumn() {
		return sortableByColumn;
	}

	/**
	 * @param sortableByColumn the sortableByColumn to set
	 */
	public void setSortableByColumn(boolean sortableByColumn) {
		this.sortableByColumn = sortableByColumn;
	}

	/**
	 * @return the movableColumns
	 */
	public boolean isMovableColumns() {
		return movableColumns;
	}

	/**
	 * @param movableColumns the movableColumns to set
	 */
	public void setMovableColumns(boolean movableColumns) {
		this.movableColumns = movableColumns;
	}
	
}
