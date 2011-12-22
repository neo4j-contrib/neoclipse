/*
 * Copyright (C) SQL Explorer Development Team
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
package net.sourceforge.sqlexplorer.plugin.views;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.history.SQLHistory;
import net.sourceforge.sqlexplorer.history.SQLHistoryChangedListener;
import net.sourceforge.sqlexplorer.history.SQLHistoryElement;
import net.sourceforge.sqlexplorer.history.SQLHistoryLabelProvider;
import net.sourceforge.sqlexplorer.history.SQLHistorySearchListener;
import net.sourceforge.sqlexplorer.history.actions.OpenInEditorAction;
import net.sourceforge.sqlexplorer.history.actions.RemoveFromHistoryAction;
import net.sourceforge.sqlexplorer.history.actions.SQLHistoryActionGroup;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.util.TextUtil;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 * SQL History view shows all succesfully executed sql statements. The list of
 * statements remains persistent between sessions.
 * 
 * @modified Davy Vanherbergen
 */
public class SQLHistoryView extends ViewPart implements SQLHistoryChangedListener {

    private Text _searchBox;

    private Table _table;

    private TableViewer _tableViewer;

    private Label _tipLabelText;

    private Point _tipPosition;

    private Shell _tipShell;

    private Widget _tipWidget;


    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.sqlexplorer.plugin.SqlHistoryChangedListener#changed()
     */
    public void changed() {

        _tableViewer.getTable().getDisplay().asyncExec(new Runnable() {

            public void run() {

                SQLHistory history = SQLExplorerPlugin.getDefault().getSQLHistory();
                _tableViewer.setItemCount(history.getEntryCount());
                _tableViewer.refresh();
            }
        });

    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(final Composite parent) {

        final SQLHistory history = SQLExplorerPlugin.getDefault().getSQLHistory();

        history.sort(1, SWT.DOWN);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, SQLExplorerPlugin.PLUGIN_ID + ".SQLHistoryView");

        history.addListener(this);

        Composite composite = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        layout.marginLeft = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 2;
        layout.marginWidth = 0;
        layout.marginHeight = 0;

        composite.setLayout(layout);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // add search box
        _searchBox = new Text(composite, SWT.BORDER);
        _searchBox.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        _searchBox.setText(Messages.getString("SQLHistoryView.SearchText"));
        _searchBox.selectAll();

        SQLHistorySearchListener searchListener = new SQLHistorySearchListener(history);
        _searchBox.addModifyListener(searchListener);
        _searchBox.addMouseListener(new MouseAdapter() {

            public void mouseDown(MouseEvent e) {

                Text searchbox = (Text) e.widget;
                if (searchbox.getText() != null
                        && searchbox.getText().equals(Messages.getString("SQLHistoryView.SearchText"))) {
                    searchbox.setText("");
                }
            }

        });

        _tableViewer = new TableViewer(composite, SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.MULTI
                | SWT.VIRTUAL);
        getSite().setSelectionProvider(_tableViewer);

        _table = _tableViewer.getTable();
        _table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        _table.setHeaderVisible(true);
        _table.setLinesVisible(true);
        _table.setItemCount(history.getEntryCount());

        _tableViewer.setLabelProvider(new SQLHistoryLabelProvider());
        _tableViewer.setContentProvider(new IStructuredContentProvider() {

            public void dispose() {

            }


            public Object[] getElements(Object inputElement) {

                return SQLExplorerPlugin.getDefault().getSQLHistory().toArray();
            }


            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

            }
        });

        _tableViewer.setInput(history);

        // create listener for sorting
        Listener sortListener = new Listener() {

            public void handleEvent(Event e) {

                // determine new sort column and direction
                TableColumn sortColumn = _table.getSortColumn();
                TableColumn currentColumn = (TableColumn) e.widget;
                int dir = _table.getSortDirection();
                if (sortColumn == currentColumn) {
                    dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
                } else {
                    _table.setSortColumn(currentColumn);
                    dir = SWT.UP;
                }

                sortColumn = _table.getSortColumn();
                TableColumn[] cols = _table.getColumns();
                for (int i = 0; i < cols.length; i++) {
                    if (cols[i] == sortColumn) {
                        history.sort(i, dir);
                        break;
                    }
                }

                // update data displayed in table
                _table.setSortDirection(dir);
                _tableViewer.refresh();
            }
        };

        String[] columnLabels = new String[] {Messages.getString("SQLHistoryView.Column.SQL"),
                Messages.getString("SQLHistoryView.Column.Time"),
                Messages.getString("SQLHistoryView.Column.Connection"),
                Messages.getString("SQLHistoryView.Column.Executions")};

        _tableViewer.setColumnProperties(columnLabels);

        // add all column headers to our table
        for (int i = 0; i < columnLabels.length; i++) {

            // add column header
            TableColumn column = new TableColumn(_table, SWT.LEFT);
            column.setText(columnLabels[i]);
            column.setMoveable(false);
            column.setResizable(true);
            column.addListener(SWT.Selection, sortListener);
        }

        _tableViewer.refresh();

        // add sizing weights to the different columns
        TableLayout tableLayout = new TableLayout();
        tableLayout.addColumnData(new ColumnWeightData(7, 150));
        tableLayout.addColumnData(new ColumnWeightData(2, 120));
        tableLayout.addColumnData(new ColumnWeightData(1, 50));
        tableLayout.addColumnData(new ColumnWeightData(1, 50));

        _table.setLayout(tableLayout);
        _table.layout();

        // redraw table if view is resized
        parent.addControlListener(new ControlAdapter() {

            public void controlResized(ControlEvent e) {

                super.controlResized(e);

                // reset weights in case of view resizing
                TableLayout tableLayout = new TableLayout();
                tableLayout.addColumnData(new ColumnWeightData(7, 150));
                tableLayout.addColumnData(new ColumnWeightData(2, 120));
                tableLayout.addColumnData(new ColumnWeightData(1, 50));
                tableLayout.addColumnData(new ColumnWeightData(1, 50));

                _table.setLayout(tableLayout);
            }

        });

        // create action bar
        final IToolBarManager toolBarMgr = getViewSite().getActionBars().getToolBarManager();

        final SQLHistoryActionGroup actionGroup = new SQLHistoryActionGroup(this, history, _tableViewer, toolBarMgr);

        _tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {

                actionGroup.refresh();
                toolBarMgr.update(true);
            }
        });

        // add context menus
        final MenuManager menuMgr = new MenuManager("#HistoryPopupMenu");
        menuMgr.setRemoveAllWhenShown(true);

        Menu historyContextMenu = menuMgr.createContextMenu(_table);
        _table.setMenu(historyContextMenu);

        menuMgr.addMenuListener(new IMenuListener() {

            public void menuAboutToShow(IMenuManager manager) {

                toolBarMgr.markDirty();
                actionGroup.fillContextMenu(manager);
            }
        });

        // also add action as default when an entry is doubleclicked.
        final OpenInEditorAction openInEditorAction = new OpenInEditorAction();
        openInEditorAction.setTableViewer(_tableViewer);
        openInEditorAction.setView(this);
        
        _tableViewer.addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {

                openInEditorAction.run();
            }
        });

        // add remove action on delete key
        final RemoveFromHistoryAction removeFromHistoryAction = new RemoveFromHistoryAction();
        removeFromHistoryAction.setTableViewer(_tableViewer);
        _table.addKeyListener(new KeyAdapter() {

            public void keyReleased(KeyEvent e) {

                // delete entry
                if (e.keyCode == SWT.DEL) {
                    removeFromHistoryAction.run();
                }
            }

        });

        // Set multi-line tooltip
        final Display display = parent.getDisplay();
        _tipShell = new Shell(parent.getShell(), SWT.ON_TOP);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.marginWidth = 2;
        gridLayout.marginHeight = 2;

        _tipShell.setLayout(gridLayout);
        _tipShell.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));

        _tipLabelText = new Label(_tipShell, SWT.WRAP | SWT.LEFT);
        _tipLabelText.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
        _tipLabelText.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));

        GridData gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER);
        _tipLabelText.setLayoutData(gridData);

        _table.addMouseListener(new MouseAdapter() {

            public void mouseDown(MouseEvent e) {

                if (_tipShell.isVisible()) {
                    _tipShell.setVisible(false);
                    _tipWidget = null;
                }
            }
        });

        _table.addMouseTrackListener(new MouseTrackAdapter() {

            public void mouseExit(MouseEvent e) {

                if (_tipShell.isVisible())
                    _tipShell.setVisible(false);
                _tipWidget = null;
            }


            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.swt.events.MouseTrackListener#mouseHover(org.eclipse.swt.events.MouseEvent)
             */
            public void mouseHover(MouseEvent event) {

                Point pt = new Point(event.x, event.y);
                Widget widget = event.widget;
                TableItem tableItem = null;

                if (widget instanceof Table) {
                    Table table = (Table) widget;
                    widget = table.getItem(pt);
                }
                if (widget instanceof TableItem) {
                    tableItem = (TableItem) widget;
                }
                if (widget == null) {
                    _tipShell.setVisible(false);
                    _tipWidget = null;
                    return;
                }
                if (widget == _tipWidget)
                    return;
                _tipWidget = widget;
                _tipPosition = _table.toDisplay(pt);

                SQLHistoryElement sqlString = (SQLHistoryElement) tableItem.getData();
                String text = TextUtil.getWrappedText(sqlString.getRawSQLString());

                if (text == null || text.equals("")) {
                    _tipWidget = null;
                    return;
                }
                // Set off the table tooltip as we provide our own
                _table.setToolTipText("");
                _tipLabelText.setText(text);
                _tipShell.pack();
                setHoverLocation(_tipShell, _tipPosition, _tipLabelText.getBounds().height);
                _tipShell.setVisible(true);

            }
        });

        _tableViewer.setSelection(null);

        composite.layout();
        parent.layout();

    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPart#dispose()
     */
    public void dispose() {

        super.dispose();
        SQLExplorerPlugin.getDefault().getSQLHistory().removeListener(this);

    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPart#setFocus()
     */
    public void setFocus() {

        // set focus to the search box
        _searchBox.setFocus();

    }


    /**
     * Sets the location for a hovering shell
     * 
     * @param shell the object that is to hover
     * @param position the position of a widget to hover over
     * @return the top-left location for a hovering box
     */
    private void setHoverLocation(Shell shell, Point position, int labelHeight) {

        Rectangle displayBounds = shell.getDisplay().getBounds();
        Rectangle shellBounds = shell.getBounds();
        shellBounds.x = Math.max(Math.min(position.x, displayBounds.width - shellBounds.width), 0);
        shellBounds.y = Math.max(Math.min(position.y + 10, displayBounds.height - shellBounds.height), 0);

        if (shellBounds.y + labelHeight + 10 > displayBounds.height) {
            shellBounds.y = Math.max(position.y - labelHeight - 10, 0);
        }

        shell.setBounds(shellBounds);
    }

}
