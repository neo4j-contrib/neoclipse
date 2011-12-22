/*
 * Copyright (C) 2007 SQL Explorer Development Team
 * http://sourceforge.net/projects/eclipsesql
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
package net.sourceforge.sqlexplorer.preferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import net.sourceforge.sqlexplorer.ExplorerException;
import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbproduct.DriverManager;
import net.sourceforge.sqlexplorer.dbproduct.ManagedDriver;
import net.sourceforge.sqlexplorer.dialogs.CreateDriverDlg;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.util.ImageUtil;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

/**
 * This class is used to define a preference page for JDBC drivers.
 * 
 * The intention is to remove the need for the &quot;Driver&quot; view and to
 * replace this with a &quot;JDBC Drivers&quot; preference page. The UI is
 * identical to the view except for the removal of the toolbar and the addition
 * of Add, Edit, Copy, Remove and Set Default buttons.
 * 
 * @author <A HREF="mailto:dbulua@progress.com">Don Bulua</A>
 * @modified Davy Vanherbergen
 */
public class DriverPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    private TableViewer _tableViewer;

    private Font _boldfont;

    private IPreferenceStore _prefs;

    private DriverManager _driverModel;


    /**
     * 
     */
    public DriverPreferencePage() {
        super();
    }


    /**
     * @param title
     */
    public DriverPreferencePage(String title) {
        super(title);
    }


    /**
     * @param title
     * @param image
     */
    public DriverPreferencePage(String title, ImageDescriptor image) {
        super(title, image);

    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     *      The UI is defined in the DriverContainerGroup class. Note: The
     *      [Restore Default] and [Apply] buttons have been removed using the
     *      noDefaultandApplyButton method as these don't apply since all
     *      updates are made in the corresponding dialogs.
     */
    protected Control createContents(Composite parent) {

        noDefaultAndApplyButton();
        _driverModel = SQLExplorerPlugin.getDefault().getDriverModel();

        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, SQLExplorerPlugin.PLUGIN_ID + ".DriverContainerGroup");

        _prefs = SQLExplorerPlugin.getDefault().getPreferenceStore();

        GridLayout parentLayout = new GridLayout(1, false);
        parentLayout.marginTop = parentLayout.marginBottom = 0;
        parentLayout.marginHeight = 0;
        parentLayout.verticalSpacing = 10;
        parent.setLayout(parentLayout);
        
        GridLayout layout;

        Composite myComposite = new Composite(parent, SWT.NONE);

        // Define layout.
        layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginWidth = layout.marginHeight = 0;
        layout.horizontalSpacing = 20;
        layout.verticalSpacing = 10;
        myComposite.setLayout(layout);

        myComposite.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, true));
        
        GridData gid = new GridData(GridData.FILL_BOTH);
        gid.grabExcessHorizontalSpace = gid.grabExcessVerticalSpace = true;
        gid.horizontalAlignment = gid.verticalAlignment = GridData.FILL;
        gid.verticalSpan = 6;
        _tableViewer = new TableViewer(myComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        _tableViewer.getControl().setLayoutData(gid);
        _tableViewer.setContentProvider(new DriverContentProvider());
        final DriverLabelProvider dlp = new DriverLabelProvider();
        _tableViewer.setLabelProvider(dlp);
        _tableViewer.getTable().addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                dlp.dispose();
                if (_boldfont != null)
                    _boldfont.dispose();

            }
        });
        _tableViewer.getTable().addMouseListener(new MouseAdapter() {

            public void mouseDoubleClick(MouseEvent e) {
                changeDriver();
            }
        });

        _tableViewer.setInput(_driverModel);
        selectFirst();
        final Table table = _tableViewer.getTable();

        myComposite.layout();
        parent.layout();

        // Add Buttons
        gid = new GridData(GridData.FILL);
        gid.widthHint = 75;
        Button add = new Button(myComposite, SWT.PUSH);
        add.setText(Messages.getString("Preferences.Drivers.Button.Add"));
        add.setLayoutData(gid);
        add.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                CreateDriverDlg dlg = new CreateDriverDlg(getShell(), CreateDriverDlg.Type.CREATE, null);

                int retCode = dlg.open();
                if (retCode == IDialogConstants.OK_ID) {
                	_tableViewer.refresh();
                	// select the new driver
                    select(dlg.getDriver()); 
                }
            }
        });

        gid = new GridData(GridData.FILL);
        gid.widthHint = 75;
        Button edit = new Button(myComposite, SWT.PUSH);
        edit.setText(Messages.getString("Preferences.Drivers.Button.Edit"));
        edit.setLayoutData(gid);
        edit.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                changeDriver();
                _tableViewer.refresh();
            }
        });

        gid = new GridData(GridData.FILL);
        gid.widthHint = 75;
        Button copy = new Button(myComposite, SWT.PUSH);
        copy.setText(Messages.getString("Preferences.Drivers.Button.Copy"));
        copy.setLayoutData(gid);
        copy.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                StructuredSelection sel = (StructuredSelection) _tableViewer.getSelection();
                ManagedDriver dv = (ManagedDriver) sel.getFirstElement();
                if (dv != null) {
                    CreateDriverDlg dlg = new CreateDriverDlg(getShell(), CreateDriverDlg.Type.COPY, dv);
                    int retCode = dlg.open();
                    if (retCode == IDialogConstants.OK_ID) {
                    	_tableViewer.refresh();
                    	// select the new driver
                        select(dlg.getDriver()); 
                    }
                }
            }
        });

        gid = new GridData(GridData.FILL);
        gid.widthHint = 75;
        Button remove = new Button(myComposite, SWT.PUSH);
        remove.setText(Messages.getString("Preferences.Drivers.Button.Remove"));
        remove.setLayoutData(gid);
        remove.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {

                boolean okToDelete = MessageDialog.openConfirm(getShell(), Messages.getString("Preferences.Drivers.ConfirmDelete.Title"),
                        Messages.getString("Preferences.Drivers.ConfirmDelete.Prefix") + _tableViewer.getTable().getSelection()[0].getText()
                                + Messages.getString("Preferences.Drivers.ConfirmDelete.Postfix"));
                if (okToDelete) {
                    StructuredSelection sel = (StructuredSelection) _tableViewer.getSelection();
                    ManagedDriver dv = (ManagedDriver) sel.getFirstElement();
                    if (dv != null) {
                        _driverModel.removeDriver(dv);
                        _tableViewer.refresh();
                        selectFirst();
                    }
                }
            }
        });

        gid = new GridData(GridData.FILL);
        gid.widthHint = 73;
        Button bdefault = new Button(myComposite, SWT.PUSH);
        bdefault.setText(Messages.getString("Preferences.Drivers.Button.Default"));
        bdefault.setLayoutData(gid);
        // Remove bold font on all elements, and make selected element bold
        bdefault.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                for (int i = 0; i < _tableViewer.getTable().getItemCount(); i++) {

                    _tableViewer.getTable().getItem(i).setFont(0, table.getFont());
                }
                _boldfont = new Font(_tableViewer.getTable().getDisplay(), table.getFont().toString(),
                        table.getFont().getFontData()[0].getHeight(), SWT.BOLD);
                _tableViewer.getTable().getSelection()[0].setFont(0, _boldfont);
                _prefs.setValue(IConstants.DEFAULT_DRIVER, _tableViewer.getTable().getSelection()[0].getText());
            }
        });

        // add button to restore default drivers
        Button bRestore = new Button(parent, SWT.PUSH);
        bRestore.setText(Messages.getString("Preferences.Drivers.Button.RestoreDefault"));
        bRestore.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
            	try {
	                _driverModel.restoreDrivers();
	                _tableViewer.refresh();
	                selectFirst();
            	}catch(ExplorerException ex) {
            		SQLExplorerPlugin.error("Cannot restore default driver configuration", ex);
            	}
            }
        });

        bRestore.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false, false));
        
        selectDefault(table);

        return parent;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench) {

    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.PreferencePage#noDefaultAndApplyButton()
     */

    /**
     * @return Returns the prefs.
     */

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.IPreferencePage#performOk()
     */
    public boolean performOk() {

        return super.performOk();
    }


    private void changeDriver() {
        StructuredSelection sel = (StructuredSelection) _tableViewer.getSelection();
        ManagedDriver dv = (ManagedDriver) sel.getFirstElement();
        if (dv != null) {
            CreateDriverDlg dlg = new CreateDriverDlg(getShell(), CreateDriverDlg.Type.MODIFY, dv);
            int retCode = dlg.open();
            
            if (retCode == IDialogConstants.OK_ID) {
            	_tableViewer.refresh();
            	select(dv);
            	//ASHINGER selectFirst();
            }
        }
    }

    void select(ManagedDriver managedDriver) {
    	if (_driverModel.getDrivers().contains(managedDriver)) {
    		StructuredSelection sel = new StructuredSelection(managedDriver);
            _tableViewer.setSelection(sel);
    	}
    }
    
    void selectFirst() {
    	// we have to select the first item in the view (Table) and not the first in the model 
    	if (_tableViewer.getTable().getItemCount() > 0) {
    		_tableViewer.getTable().select(0);
    	}
//    	Iterator<ManagedDriver> iter = _driverModel.getDrivers().iterator();
//        if (iter.hasNext()) {
//            StructuredSelection sel = new StructuredSelection(iter.next());
//            _tableViewer.setSelection(sel);
//        }
    }


    // Bold the default driver element
    void selectDefault(Table table) {
        String defaultDriver = _prefs.getString(IConstants.DEFAULT_DRIVER);
        if (defaultDriver == null)
            return;

        int index = 0;
        for (ManagedDriver driver : _driverModel.getDrivers()) {
            if (driver.getName().toLowerCase().startsWith(defaultDriver.toLowerCase())) {
                _boldfont = new Font(_tableViewer.getTable().getDisplay(), table.getFont().toString(),
                        table.getFont().getFontData()[0].getHeight(), SWT.BOLD);
                _tableViewer.getTable().getItem(index).setFont(0, _boldfont);
                _tableViewer.getTable().pack(true);
                break;
            }
            index++;
        }
    }
}

class DriverContentProvider implements IStructuredContentProvider {

    public Object[] getElements(Object input) {
    	ArrayList<ManagedDriver> drivers = new ArrayList<ManagedDriver>();
    	drivers.addAll(((DriverManager) input).getDrivers());
    	Collections.sort(drivers, new Comparator<ManagedDriver>() {
			public int compare(ManagedDriver left, ManagedDriver right) {
				return left.getName().compareTo(right.getName());
			}
    	});
        return drivers.toArray();
    }


    public void dispose() {
    }


    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }
}

class DriverLabelProvider extends LabelProvider implements ITableLabelProvider {


    DriverLabelProvider() {
    };


    public Image getColumnImage(Object element, int i) {
        ManagedDriver dv = (ManagedDriver) element;
        
        try {
        	dv.registerSQLDriver();
        } catch(ClassNotFoundException e) {
        	// Nothing
        }
        if (dv.isDriverClassLoaded() == true) {
            return ImageUtil.getImage("Images.OkDriver");
        } else {
            return ImageUtil.getImage("Images.ErrorDriver");
        }
    }


    public void dispose() {
        
        super.dispose();
        ImageUtil.disposeImage("Images.OkDriver");    
        ImageUtil.disposeImage("Images.ErrorDriver");
        
    }


    public String getColumnText(Object element, int i) {
        ManagedDriver dv = (ManagedDriver) element;
        return dv.getName();
    }


    public boolean isLabelProperty(Object element, String property) {
        return true;
    }

    public void removeListener(ILabelProviderListener listener) {
    }


    public void addListener(ILabelProviderListener listener) {
    }

}
