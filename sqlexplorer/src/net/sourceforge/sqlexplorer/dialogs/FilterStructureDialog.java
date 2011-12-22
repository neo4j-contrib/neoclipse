package net.sourceforge.sqlexplorer.dialogs;

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

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.nodes.CatalogNode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.DatabaseNode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.SchemaNode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class FilterStructureDialog extends Dialog {

    class TableContentProvider implements IStructuredContentProvider {

        public void dispose() {

        }


        public Object[] getElements(Object input) {

            if (input instanceof DatabaseNode) {
                return sort(((DatabaseNode) input).getChildNames());
            }

            if (input instanceof SchemaNode) {
                return sort(((SchemaNode) input).getChildNames());
            }

            if (input instanceof CatalogNode) {
                return sort(((CatalogNode) input).getChildNames());
            }

            return new Object[0];
        }

        private Object[] sort(String[] names)
        {
        	TreeSet<Object> checked = new TreeSet<Object>();
        	for(String name : names)
        	{
        		if(name.length() > 0)
        		{
        			checked.add(name);
        		}
        	}
        	return checked.toArray(new Object[checked.size()]);
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

        }
    }

    private DatabaseNode _db;

    private Text _filterText;

    private String[] _folderFilter;

    private Table _folderTable;

    private String _nameFilter;

    private Button _patternButton;

    private String[] _schemaFilter;

    private Table _schemaTable;


    public FilterStructureDialog() {
        super(SQLExplorerPlugin.getDefault().getDatabaseStructureView().getSite().getShell());
        _db = SQLExplorerPlugin.getDefault().getDatabaseStructureView().getSession().getRoot();
    }


    public boolean close() {

        // extract selections for schemas

        List<String> schemaSelection = new ArrayList<String>();
        TableItem[] items = _schemaTable.getItems();

        if (items != null) {
            for (int i = 0; i < items.length; i++) {
                if (items[i].getChecked()) {
                    schemaSelection.add(items[i].getText());
                }
            }
        }
        if (schemaSelection.size() != 0) {
            _schemaFilter = (String[]) schemaSelection.toArray(new String[] {});
        } else {
            _schemaFilter = null;
        }

        // extract selections for folders

        List<String> folderSelection = new ArrayList<String>();
        items = _folderTable.getItems();

        if (items != null) {
            for (int i = 0; i < items.length; i++) {
                if (items[i].getChecked()) {
                    folderSelection.add(items[i].getText());
                }
            }
        }
        if (folderSelection.size() != 0) {
            _folderFilter = (String[]) folderSelection.toArray(new String[] {});
        } else {
            _folderFilter = null;
        }

        // extract name pattern

        if (_patternButton.getSelection() && _filterText.getText() != null
                && _filterText.getText().trim().length() != 0) {
            _nameFilter = _filterText.getText().trim();
        } else {
            _nameFilter = null;
        }

        return super.close();
    }


    protected void configureShell(Shell shell) {

        super.configureShell(shell);
        String title = Messages.getString("FilterStructureDialog.Title.prefix") + " " + _db.getSession().toString()
                + " " + Messages.getString("FilterStructureDialog.Title.postfix");
        shell.setText(title);
    }


    protected void createButtonsForButtonBar(Composite parent) {

        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }


    protected Control createDialogArea(Composite parent) {

        final Composite composite = (Composite) super.createDialogArea(parent);

        try {

            GridLayout layout = new GridLayout();
            layout.numColumns = 2;
            layout.marginLeft = 10;
            layout.marginRight = 15;
            layout.horizontalSpacing = 0;
            layout.verticalSpacing = 5;
            layout.marginWidth = 0;
            layout.marginHeight = 5;

            GridData gridData = new GridData(GridData.FILL_BOTH);
            gridData.grabExcessHorizontalSpace = true;
            gridData.grabExcessVerticalSpace = true;
            gridData.widthHint = 380;
            
            composite.setLayout(layout);
            composite.setLayoutData(gridData);

            GridData tGridData = new GridData(GridData.FILL_HORIZONTAL);
            tGridData.horizontalSpan = 2;
            tGridData.horizontalAlignment = SWT.FILL;
            tGridData.widthHint = 280;

            GridData t2GridData = new GridData(GridData.FILL_HORIZONTAL);
            t2GridData.horizontalSpan = 2;
            t2GridData.horizontalAlignment = SWT.FILL;
            t2GridData.verticalIndent = 15;

            GridData bData = new GridData();
            bData.horizontalIndent = 10;

            // add schema text
            Label selectSchemaLabel = new Label(composite, SWT.WRAP);
            selectSchemaLabel.setLayoutData(tGridData);
            selectSchemaLabel.setText(Messages.getString("FilterStructureDialog.SelectSchema"));

            // add schema selection table
            final TableViewer schemaTable = new TableViewer(composite, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL);
            _schemaTable = schemaTable.getTable();
            GridData tableGridData = new GridData();
            tableGridData.horizontalSpan = 2;
            tableGridData.horizontalAlignment = SWT.FILL;
            tableGridData.heightHint = 50;
            tableGridData.grabExcessHorizontalSpace = true;
            schemaTable.getControl().setLayoutData(tableGridData);
            schemaTable.setContentProvider(new TableContentProvider());
            schemaTable.setInput(_db);

            // select correct values
            TableItem[] items = _schemaTable.getItems();
            if (_schemaFilter != null && items != null) {
                for (int i = 0; i < items.length; i++) {
                    for (int j = 0; j < _schemaFilter.length; j++) {
                        if (_schemaFilter[j].equalsIgnoreCase(items[i].getText())) {
                            items[i].setChecked(true);
                        }
                    }
                }
            }

            // add schema selection buttons
            Button selectAllSchemas = new Button(composite, SWT.PUSH);
            selectAllSchemas.setLayoutData(bData);
            selectAllSchemas.setText(Messages.getString("FilterStructureDialog.Buttons.SelectAll"));
            selectAllSchemas.addMouseListener(new MouseAdapter() {

                public void mouseUp(MouseEvent e) {

                    Table table = schemaTable.getTable();
                    TableItem[] items = table.getItems();
                    if (items == null) {
                        return;
                    }
                    for (int i = 0; i < items.length; i++) {
                        items[i].setChecked(true);
                    }
                }
            });

            Button deselectAllSchemas = new Button(composite, SWT.PUSH);
            deselectAllSchemas.setText(Messages.getString("FilterStructureDialog.Buttons.DeselectAll"));
            deselectAllSchemas.addMouseListener(new MouseAdapter() {

                public void mouseUp(MouseEvent e) {

                    Table table = schemaTable.getTable();
                    TableItem[] items = table.getItems();
                    if (items == null) {
                        return;
                    }
                    for (int i = 0; i < items.length; i++) {
                        items[i].setChecked(false);
                    }
                }
            });

            // add folder text
            Label selectFolderLabel = new Label(composite, SWT.WRAP);
            selectFolderLabel.setLayoutData(t2GridData);
            selectFolderLabel.setText(Messages.getString("FilterStructureDialog.SelectFolder"));

            // add folder selection table
            final TableViewer folderTable = new TableViewer(composite, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL);
            _folderTable = folderTable.getTable();
            folderTable.getControl().setLayoutData(tableGridData);
            folderTable.setContentProvider(new TableContentProvider());
            if (_db.getChildNodes() != null && _db.getChildNodes().length != 0) {
            	INode firstCatalog = null;
            	for(INode node : _db.getChildNodes())
            	{
            		if("catalog".equals(node.getType()) || "schema".equals(node.getType()))
            		{
            			firstCatalog = node;
            			break;
            		}
            	}
            	if(firstCatalog == null)
            	{
            		firstCatalog = _db.getChildNodes()[0];
            	}
                folderTable.setInput(firstCatalog);
            }

            // select correct values
            items = _folderTable.getItems();
            if (_folderFilter != null && items != null) {
                for (int i = 0; i < items.length; i++) {
                    for (int j = 0; j < _folderFilter.length; j++) {
                        if (_folderFilter[j].equalsIgnoreCase(items[i].getText())) {
                            items[i].setChecked(true);
                        }
                    }
                }
            }

            // add folder selection buttons
            Button selectAllFolders = new Button(composite, SWT.PUSH);
            selectAllFolders.setLayoutData(bData);
            selectAllFolders.setText(Messages.getString("FilterStructureDialog.Buttons.SelectAll"));
            selectAllFolders.addMouseListener(new MouseAdapter() {

                public void mouseUp(MouseEvent e) {

                    Table table = folderTable.getTable();
                    TableItem[] items = table.getItems();
                    if (items == null) {
                        return;
                    }
                    for (int i = 0; i < items.length; i++) {
                        items[i].setChecked(true);
                    }
                }
            });

            Button deselectAllFolders = new Button(composite, SWT.PUSH);
            deselectAllFolders.setText(Messages.getString("FilterStructureDialog.Buttons.DeselectAll"));
            deselectAllFolders.addMouseListener(new MouseAdapter() {

                public void mouseUp(MouseEvent e) {

                    Table table = folderTable.getTable();
                    TableItem[] items = table.getItems();
                    if (items == null) {
                        return;
                    }
                    for (int i = 0; i < items.length; i++) {
                        items[i].setChecked(false);
                    }
                }
            });

            t2GridData = new GridData(GridData.FILL_HORIZONTAL);
            t2GridData.horizontalSpan = 2;
            t2GridData.horizontalAlignment = SWT.FILL;
            t2GridData.verticalIndent = 15;
            // add filter text
            _patternButton = new Button(composite, SWT.CHECK | SWT.WRAP);
            _patternButton.setLayoutData(t2GridData);
            _patternButton.setText(Messages.getString("FilterStructureDialog.ElementPattern"));

            final Text pattern = new Text(composite, SWT.BORDER);
            _filterText = pattern;
            GridData textData = new GridData(GridData.FILL_HORIZONTAL);
            textData.horizontalSpan = 2;
            pattern.setLayoutData(textData);
            pattern.setEnabled(_patternButton.getSelection());

            // restore values
            if (_nameFilter != null) {
                _patternButton.setSelection(true);
                pattern.setEnabled(true);
                pattern.setText(_nameFilter);
            } else {
                _patternButton.setSelection(false);
            }

            // add filter help text
            final Label filterHelpLabel = new Label(composite, SWT.WRAP);
            GridData helpData = new GridData(GridData.FILL_HORIZONTAL);
            helpData.horizontalSpan = 2;
            filterHelpLabel.setLayoutData(helpData);
            filterHelpLabel.setText(Messages.getString("FilterStructureDialog.ElementPattern.help"));
            filterHelpLabel.setEnabled(_patternButton.getSelection());

            // activate field and help when checkbox is selected
            _patternButton.addSelectionListener(new SelectionAdapter() {

                public void widgetSelected(SelectionEvent e) {

                    pattern.setEnabled(((Button) e.widget).getSelection());
                    filterHelpLabel.setEnabled(((Button) e.widget).getSelection());
                }

            });

            composite.addListener(SWT.RESIZE, new Listener() {

                public void handleEvent(Event event) {

                    composite.layout();
                    composite.redraw();
                }

            });

            return composite;

        } catch (Exception e) {
            SQLExplorerPlugin.error("Couldn't create dialog.", e);
        }

        return null;
    }


    public String[] getFolderFilter() {

        return _folderFilter;
    }


    public String getNameFilter() {

        return _nameFilter;
    }


    public String[] getSchemaFilter() {

        return _schemaFilter;
    }


    public void setFolderFilter(String[] folderFilter) {

        _folderFilter = folderFilter;
    }


    public void setNameFilter(String nameFilter) {

        _nameFilter = nameFilter;
    }


    public void setSchemaFilter(String[] schemaFilter) {

        _schemaFilter = schemaFilter;
    }


    protected void setShellStyle(int newShellStyle) {

        super.setShellStyle(newShellStyle | SWT.RESIZE);
    }

}
