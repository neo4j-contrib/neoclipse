package net.sourceforge.sqlexplorer.sqleditor.results;

import net.sourceforge.sqlexplorer.Messages;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class TableResultsTable extends AbstractResultsTable {
	
	private static final String ORIGINAL_COLUMN_INDEX = "original-column-index";
	
	public static class MyStructuredContentProvider extends StructuredContentProviderAdapter {
    	@Override
        public Object[] getElements(Object inputElement) {
            ResultProvider provider = (ResultProvider)inputElement;
            return provider.getRows();
        }
    };
	
	private static final class TableRow extends FlatCellRangeRow {
		private CellRangeRow row;
		private int offset;

		public TableRow(CellRangeRow row, int offset) {
			super();
			this.row = row;
			this.offset = offset;
		}

		public Object getCellValue(int colIndex) {
			return row.getCellValue(colIndex + offset);
		}
	}
	
	private final class TableCells implements CellRange {
		
		private Rectangle rect;
		private TableRow[] rows;

		public TableCells(int x1, int y1, int x2, int y2) {
			super();
			if (x2 < x1 || y2 < y1)
				throw new NegativeArraySizeException();
			rect = new Rectangle(x1, y1, x2 - x1 + 1, y2 - y1 + 1);
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
				rows = new TableRow[rect.height];
				for (int i = 0; i < rect.height; i++)
					rows[i] = new TableRow(src[i + rect.y], rect.x);
			}
			return rows;
		}

		public Column getColumn(int colIndex) {
			return TableResultsTable.this.getResultProvider().getColumn(colIndex + rect.x);
		}
	}
	
	protected final Listener SORT_LISTENER = new Listener() {
		public void handleEvent(Event e) {
			if (!isSortableByColumn())
				return;
			
			// determine new sort column and direction
			Table table = getTableViewer().getTable();
			TableColumn sortColumn = table.getSortColumn();
			TableColumn currentColumn = (TableColumn) e.widget;
			int dir = table.getSortDirection();
			if (sortColumn == currentColumn) {
				dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
			} else {
				table.setSortColumn(currentColumn);
				dir = SWT.UP;
			}
			
			// sort the data based on column and direction
			int colIndex = (Integer)currentColumn.getData(ORIGINAL_COLUMN_INDEX);
			if (getResultProvider().sortData(colIndex, dir)) {
				// update data displayed in table
				table.setSortDirection(dir);
				table.clearAll();
			}
		}
	};
	
	// Whether the table can be sorted according to a given column
	private boolean sortableByColumn = true;
	
	// Whether columns can be moved
	private boolean movableColumns = true;
	
	// The TableViewer
	private TableViewer tableViewer;
	private TableCursor cursor;
	
	// Status bar components
	private Label statusMessage;
	private Label statusPosition;

	public TableResultsTable(ResultProvider resultProvider) {
		super(resultProvider);
	}

	public TableViewer getTableViewer() {
		return tableViewer;
	}
	
	@Override
	public MenuManager createMenuManager() {
        MenuManager menuManager = new MenuManager("TreeResultsTabContextMenu");
        menuManager.setRemoveAllWhenShown(true);
        Menu contextMenu = menuManager.createContextMenu(tableViewer.getTable());        
        tableViewer.getControl().setMenu(contextMenu);
        cursor.setMenu(contextMenu);
        return menuManager;
	}
	
	@Override
	protected void createResultsTable(Composite parent) {
        tableViewer = new TableViewer(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.VIRTUAL | SWT.FULL_SELECTION);
        Table table = tableViewer.getTable();
        
        int numColumns = getResultProvider().getNumberOfColumns();
        String[] columnProps = new String[numColumns];
        for (int colIndex = 0; colIndex < numColumns; colIndex++) {
        	columnProps[colIndex] = Integer.toString(colIndex);
        	CellRange.Column column = getResultProvider().getColumn(colIndex);
        	createColumn(column);
        }
        tableViewer.setColumnProperties(columnProps);
        
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        ResultProvider provider = getResultProvider();
        if (provider != null) {
	        table.setItemCount(provider.getNumberOfRows());
	        tableViewer.setContentProvider(new MyStructuredContentProvider());
	        tableViewer.setLabelProvider(new MyTableLabelProvider());
	        tableViewer.setInput(provider);
        }

        // create a TableCursor to navigate around the table
        cursor = new TableCursor(table, SWT.NONE);
        cursor.setBackground(table.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
        cursor.setForeground(table.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT));
        cursor.setLayout(new FillLayout());
        cursor.setVisible(false);
        cursor.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                Table table = getTableViewer().getTable();
                
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
        table.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                Table t = (Table) e.widget;
                if (t.getItemCount() != 0) {
                    cursor.setVisible(true);
                }
            }           
        });
        cursor.addKeyListener(this);

        // refresh tab on F5, copy cell on CTRL-C, etc
        table.addKeyListener(this);

        // fill table to get correct pack results
        fillTable(table);

        // Pack the columns to best-fit size
        table.pack();
        for (TableColumn column : table.getColumns())
        {
            column.pack(); 
        }
	}
	
	private void fillTable(Table table)
	{
        int columnCount = table.getColumnCount();
        for(TableItem item : table.getItems())
        {
        	for(int i = 0; i < columnCount; i++)
        	{
        		item.getText(i);
        	}
        }
	}
	protected TableColumn createColumn(ResultProvider.Column columnDef) {
        TableColumn column = new TableColumn(getTableViewer().getTable(), SWT.LEFT);           
        column.setText(getResultProvider().getColumn(columnDef.getColumnIndex()).getCaption());
        column.setMoveable(isMovableColumns());
        column.setResizable(true);            
        column.addListener(SWT.Selection, SORT_LISTENER);
        column.setData(ORIGINAL_COLUMN_INDEX, new Integer(columnDef.getColumnIndex()));
        if (columnDef.isRightJustify())
        	column.setAlignment(SWT.RIGHT);
        return column;
	}
	
	@Override
	public CellRange getSelection(SelectionType selection) {
		Table table = getTableViewer().getTable();
		int rowIndex = cursor.getRow() != null ? table.indexOf(cursor.getRow()) : -1;
		switch(selection) {
		case CELL:
			if (rowIndex < 0)
				return null;
			return new TableCells(cursor.getColumn(), rowIndex, cursor.getColumn(), rowIndex);
			
		case ROW:
			if (rowIndex < 0)
				return null;
			return new TableCells(0, rowIndex, table.getColumnCount() - 1, rowIndex);
			
		case COLUMN:
			if (rowIndex < 0)
				return null;
			return new TableCells(cursor.getColumn(), 0, cursor.getColumn(), table.getItemCount() - 1);
			
		case SELECTION:
			int[] indecies = table.getSelectionIndices();
			if (indecies == null)
				return null;
			rowIndex = -1;
			int bottomRowIndex = -1;
			for (int i = 0; i < indecies.length; i++) {
				if (rowIndex < 0 || rowIndex > indecies[i])
					rowIndex = indecies[i];
				if (bottomRowIndex < indecies[i])
					bottomRowIndex = indecies[i];
			}
			return new TableCells(cursor.getColumn(), rowIndex, cursor.getColumn(), bottomRowIndex);
			
		case ENTIRE_TABLE:
			return new TableCells(0, 0, table.getColumnCount() - 1, table.getItemCount() - 1);
		}
		return null;
	}

	protected void onSelectCell(TableCursor cursor) {
        Table table = tableViewer.getTable();
        int rowIndex = table.indexOf(cursor.getRow()) + 1;
        
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
	public Shell createFindPopup() {
		Table table = tableViewer.getTable();
		
        // find out where to put the popup on screen
        Point popupLocation = table.toDisplay(10, 40);
        return new ColumnLocatorPopup(this, popupLocation);
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

	public TableCursor getCursor() {
		return cursor;
	}
	
}
