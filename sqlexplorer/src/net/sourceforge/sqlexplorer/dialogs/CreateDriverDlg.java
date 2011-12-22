package net.sourceforge.sqlexplorer.dialogs;

/*
 * Copyright (C) 2002-2004 Andrea Mazzolini
 * andreamazzolini@users.sourceforge.net
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

import java.io.File;
import java.sql.Driver;
import java.util.StringTokenizer;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbproduct.ManagedDriver;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.util.ImageUtil;
import net.sourceforge.sqlexplorer.util.MyURLClassLoader;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

public class CreateDriverDlg extends TitleAreaDialog {
	
	public enum Type {
		CREATE, MODIFY, COPY
	}

    private ManagedDriver driver;

	private static final int SIZING_TEXT_FIELD_WIDTH = 250;

    Button _extraClasspathDeleteBtn;

    private Button _extraClasspathUpBtn;

    private Button _extraClasspathDownBtn;

    private Button newBtn;

    Button _javaClasspathListDriversBtn;

    Button _extraClasspathListDriversBtn;

    DefaultFileListBoxModel defaultModel = new DefaultFileListBoxModel();

    ListViewer extraClassPathList;

    ListViewer javaClassPathList;

    Type type;

    Text nameField;

    Button jarSearch;

    Combo combo;

    Text exampleUrlField;


    public CreateDriverDlg(Shell parentShell, Type type, ManagedDriver driver) {
        super(parentShell);
        
        if (type == Type.COPY) {
        	this.driver = new ManagedDriver(SQLExplorerPlugin.getDefault().getDriverModel().createUniqueId());

        	this.driver.setName("Copy of " + driver.getName());
        	this.driver.setDriverClassName(driver.getDriverClassName());
        	this.driver.setUrl(driver.getUrl());
        	for (String jar : driver.getJars())
        		this.driver.getJars().add(jar);
        } 
        else 
        	this.driver = driver;
        	
        this.type = type;
    }


    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        if (type == Type.CREATE) {
            shell.setText(Messages.getString("DriverDialog.Create.WindowTitle"));//$NON-NLS-1$
        } else if (type == Type.MODIFY) {
            shell.setText(Messages.getString("DriverDialog.Modify.WindowTitle"));//$NON-NLS-1$
        } else if (type == Type.COPY) {
            shell.setText(Messages.getString("DriverDialog.Copy.WindowTitle"));//$NON-NLS-1$
        }
    }


    protected Control createContents(Composite parent) {

        Control contents = super.createContents(parent);

        if (type == Type.CREATE) {
            setTitle(Messages.getString("DriverDialog.Create.Title"));//$NON-NLS-1$
            setMessage(Messages.getString("DriverDialog.Create.Message"));//$NON-NLS-1$
        } else if (type == Type.MODIFY) {
            setTitle(Messages.getString("DriverDialog.Modify.Title"));//$NON-NLS-1$
            setMessage(Messages.getString("DriverDialog.Modify.Message"));//$NON-NLS-1$
        } else if (type == Type.COPY) {
            setTitle(Messages.getString("DriverDialog.Copy.Title"));//$NON-NLS-1$
            setMessage(Messages.getString("DriverDialog.Copy.Message"));//$NON-NLS-1$
        }

        Image image = ImageUtil.getImage("Images.WizardLogo");//$NON-NLS-1$
        
        if (image != null) {
            setTitleImage(image);
        }
        // Bug # 1569762 : Driver list corrupt
        /*contents.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                ImageUtil.disposeImage("Images.WizardLogo");                
            }            
        });
       */        
        return contents;
    }

    /**
     * returns the edited driver. This is Helpful for using Type.COPY and Type.CREATE
     * @return the edited driver
     */
    public ManagedDriver getDriver() {
		return driver;
	}

    protected void okPressed() {
        String name = nameField.getText().trim();
        String driverClassName = (String) combo.getText();
        driverClassName = (driverClassName != null ? driverClassName.trim() : ""); //$NON-NLS-1$
        String url = exampleUrlField.getText().trim();
        if (name.equals("")) { //$NON-NLS-1$
            MessageDialog.openError(this.getShell(), Messages.getString("Error..."), Messages.getString("DriverDialog.ErrNameEmpty"));  //$NON-NLS-1$//$NON-NLS-2$
            return;
        }
        if (driverClassName.equals("")) { //$NON-NLS-1$
            MessageDialog.openError(this.getShell(), Messages.getString("Error..."), Messages.getString("DriverDialog.ErrDriverClassEmpty"));  //$NON-NLS-1$//$NON-NLS-2$
            return;
        }
        if (url.equals("")) { //$NON-NLS-1$
            MessageDialog.openError(this.getShell(), Messages.getString("Error..."), Messages.getString("DriverDialog.ErrURLEmpty"));  //$NON-NLS-1$//$NON-NLS-2$
            return;
        }

        if (driver == null) // Type.CREATE
        	driver = new ManagedDriver(SQLExplorerPlugin.getDefault().getDriverModel().createUniqueId());
        
        driver.setName(name);
        driver.setJars(defaultModel.getFileNames());
        driver.setDriverClassName(driverClassName);
        driver.setUrl(url);

        if (type != Type.MODIFY)
            SQLExplorerPlugin.getDefault().getDriverModel().addDriver(driver);

        close();
    }


    void validate() {
        if ((nameField.getText().trim().length() > 0) && (exampleUrlField.getText().trim().length() > 0)
                && (combo.getText().trim().length() > 0))
            setDialogComplete(true);
        else
            setDialogComplete(false);
    }


    protected void setDialogComplete(boolean value) {
        Button okBtn = getButton(IDialogConstants.OK_ID);
        if (okBtn != null)
            okBtn.setEnabled(value);
    }


    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        validate();
    }

    protected Control createDialogArea(Composite parent) {
        // top level composite
        Composite parentComposite = (Composite) super.createDialogArea(parent);

        // create a composite with standard margins and spacing
        Composite composite = new Composite(parentComposite, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        composite.setFont(parentComposite.getFont());

        Composite nameGroup = new Composite(composite, SWT.NONE);
        layout = new GridLayout();
        layout.numColumns = 3;
        layout.marginWidth = 10;
        nameGroup.setLayout(layout);
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
        nameGroup.setLayoutData(data);

        Composite topComposite = new Composite(nameGroup, SWT.NONE);
        data = new GridData(GridData.FILL_VERTICAL | GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 3;
        topComposite.setLayoutData(data);
        topComposite.setLayout(new GridLayout());

        Group topGroup = new Group(topComposite, SWT.NULL);
        topGroup.setText(Messages.getString("Driver")); //$NON-NLS-1$

        data = new GridData(GridData.FILL_VERTICAL | GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 3;
        data.widthHint = SIZING_TEXT_FIELD_WIDTH;
        topGroup.setLayoutData(data);
        layout = new GridLayout();
        layout.numColumns = 3;
        layout.marginWidth = 5;
        topGroup.setLayout(layout);

        Label label = new Label(topGroup, SWT.WRAP);
        label.setText(Messages.getString("Name")); //$NON-NLS-1$
        nameField = new Text(topGroup, SWT.BORDER);
        data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
        data.horizontalSpan = 2;
        data.widthHint = SIZING_TEXT_FIELD_WIDTH;
        nameField.setLayoutData(data);

        nameField.addKeyListener(new KeyListener() {

            public void keyPressed(org.eclipse.swt.events.KeyEvent e) {
                CreateDriverDlg.this.validate();
            };

            public void keyReleased(org.eclipse.swt.events.KeyEvent e) {
                CreateDriverDlg.this.validate();
            };
        });

        Label label5 = new Label(topGroup, SWT.WRAP);
        label5.setText(Messages.getString("DriverDialog.ExampleURL")); //$NON-NLS-1$
        exampleUrlField = new Text(topGroup, SWT.BORDER);
        data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
        data.widthHint = SIZING_TEXT_FIELD_WIDTH;
        data.horizontalSpan = 2;
        exampleUrlField.setLayoutData(data);
        exampleUrlField.addKeyListener(new KeyListener() {

            public void keyPressed(org.eclipse.swt.events.KeyEvent e) {
                CreateDriverDlg.this.validate();
            };


            public void keyReleased(org.eclipse.swt.events.KeyEvent e) {
                CreateDriverDlg.this.validate();
            };
        });

        Composite centralComposite = new Composite(nameGroup, SWT.NONE);
        data = new GridData(GridData.FILL_VERTICAL | GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 3;
        data.verticalSpan = 4;
//        data.heightHint = 200;
        centralComposite.setLayoutData(data);
        centralComposite.setLayout(new FillLayout());

        TabFolder tabFolder = new TabFolder(centralComposite, SWT.NULL);
        TabItem item1 = new TabItem(tabFolder, SWT.NULL);
        item1.setText(Messages.getString("DriverDialog.JavaClassPath")); //$NON-NLS-1$
        TabItem item2 = new TabItem(tabFolder, SWT.NULL);
        item2.setText(Messages.getString("DriverDialog.ExtraClassPath")); //$NON-NLS-1$
        createJavaClassPathPanel(tabFolder, item1);
        createExtraClassPathPanel(tabFolder, item2);

        Label label4 = new Label(nameGroup, SWT.WRAP);
        label4.setText(Messages.getString("DriverDialog.DriverClassName")); //$NON-NLS-1$
        combo = new Combo(nameGroup, SWT.BORDER | SWT.DROP_DOWN);
        data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
        data.widthHint = SIZING_TEXT_FIELD_WIDTH;
        data.horizontalSpan = 2;
        combo.setLayoutData(data);

        combo.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {

            public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
            }


            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                CreateDriverDlg.this.validate();
            };
        });

        combo.addKeyListener(new KeyListener() {

            public void keyPressed(org.eclipse.swt.events.KeyEvent e) {
                CreateDriverDlg.this.validate();
            };


            public void keyReleased(org.eclipse.swt.events.KeyEvent e) {
                CreateDriverDlg.this.validate();
            };
        });

        nameGroup.layout();
        loadData();
        return parentComposite;
    }


    private void loadData() {
    	if (driver == null)
    		return;
    	
        nameField.setText(driver.getName());
        if (driver.getDriverClassName() != null)
            combo.setText(driver.getDriverClassName());
        exampleUrlField.setText(driver.getUrl());

        for (String jar : driver.getJars())
            defaultModel.addFile(new File(jar));

        if (extraClassPathList != null) {
            extraClassPathList.refresh();
            if (defaultModel.size() > 0)
                extraClassPathList.getList().setSelection(0);
        }

        if (defaultModel.size() > 0) {
            Object obj = (defaultModel.toArray())[0];
            StructuredSelection sel = new StructuredSelection(obj);
            extraClassPathList.setSelection(sel);

        }
    }

    private void createJavaClassPathPanel(TabFolder tabFolder, TabItem tabItem) {
        Composite parent = new Composite(tabFolder, SWT.NULL);
        parent.setLayout(new FillLayout());
        tabItem.setControl(parent);
        Composite cmp = new Composite(parent, SWT.NULL);
        GridLayout grid = new GridLayout();
        grid.numColumns = 2;

        cmp.setLayout(grid);
        javaClassPathList = new ListViewer(cmp, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);

        GridData data = new GridData();
        data.grabExcessVerticalSpace = true;
        data.horizontalAlignment = GridData.FILL;
        data.verticalAlignment = GridData.FILL;

        data.grabExcessHorizontalSpace = true;

        javaClassPathList.getControl().setLayoutData(data);
        javaClassPathList.setContentProvider(new FileContentProvider());
        javaClassPathList.setLabelProvider(new FileLabelProvider());
        ClassPathListModel model = new ClassPathListModel();
        javaClassPathList.setInput(model);

        Composite left = new Composite(cmp, SWT.NULL);
        data = new GridData();
        data.horizontalSpan = 1;
        data.grabExcessVerticalSpace = true;
//        data.widthHint = 120;
        data.horizontalAlignment = GridData.FILL;
        data.verticalAlignment = GridData.FILL;

        left.setLayoutData(data);

        GridLayout gridLayout = new GridLayout();

        gridLayout.numColumns = 1;

        left.setLayout(gridLayout);

        _javaClasspathListDriversBtn = new Button(left, SWT.NULL);
        _javaClasspathListDriversBtn.setText(Messages.getString("DriverDialog.ListDrivers")); //$NON-NLS-1$
        _javaClasspathListDriversBtn.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent event) {
                combo.removeAll();
                File file = (File) ((IStructuredSelection) javaClassPathList.getSelection()).getFirstElement();
                if (file != null) {
                    try {

                        MyURLClassLoader cl = new MyURLClassLoader(file.toURI().toURL());
                        Class<?>[] classes = cl.getAssignableClasses(Driver.class);
                        for (int i = 0; i < classes.length; ++i) {
                            combo.add(classes[i].getName());
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();

                    }
                }
                if (combo.getItemCount() > 0) {
                    combo.setText(combo.getItem(0));
                }

            }
        });

        data = new GridData();
        data.grabExcessHorizontalSpace = true;
        data.horizontalAlignment = GridData.FILL;
        _javaClasspathListDriversBtn.setLayoutData(data);

        javaClassPathList.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                File f = (File) selection.getFirstElement();
                if (f != null) {
                    if (f.isFile())
                        _javaClasspathListDriversBtn.setEnabled(true);
                    else
                        _javaClasspathListDriversBtn.setEnabled(false);
                } else
                    _javaClasspathListDriversBtn.setEnabled(false);
            }
        });
        if (model.size() > 0) {
            Object obj = (model.toArray())[0];
            StructuredSelection sel = new StructuredSelection(obj);
            javaClassPathList.setSelection(sel);
        }

    }


    private void createExtraClassPathPanel(final TabFolder tabFolder, TabItem tabItem) {
        Composite parent = new Composite(tabFolder, SWT.NULL);
        parent.setLayout(new FillLayout());
        tabItem.setControl(parent);
        Composite cmp = new Composite(parent, SWT.NULL);
        GridLayout grid = new GridLayout();
        grid.numColumns = 2;

        cmp.setLayout(grid);
        extraClassPathList = new ListViewer(cmp, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);

        GridData data = new GridData();
        data.grabExcessVerticalSpace = true;
        data.horizontalAlignment = GridData.FILL;
        data.verticalAlignment = GridData.FILL;

        data.grabExcessHorizontalSpace = true;

        extraClassPathList.getControl().setLayoutData(data);

        extraClassPathList.setContentProvider(new FileContentProvider());
        extraClassPathList.setLabelProvider(new FileLabelProvider());

        extraClassPathList.setInput(defaultModel);

        Composite left = new Composite(cmp, SWT.NULL);
        data = new GridData();
        data.horizontalSpan = 1;
        data.grabExcessVerticalSpace = true;
//        data.widthHint = 120;
        data.horizontalAlignment = GridData.FILL;
        data.verticalAlignment = GridData.FILL;

        left.setLayoutData(data);

        GridLayout gridLayout = new GridLayout();

        gridLayout.numColumns = 1;

        left.setLayout(gridLayout);

        newBtn = new Button(left, SWT.NULL);
        newBtn.setText(Messages.getString("DriverDialog.New")); //$NON-NLS-1$
        newBtn.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent event) {
                FileDialog dlg = new FileDialog(tabFolder.getShell(), SWT.OPEN);
                dlg.setFilterExtensions(new String[] {"*.jar;*.zip"}); //$NON-NLS-1$
                String str = dlg.open();
                if (str != null) {
                    File obj = new File(str);
                    defaultModel.add(obj);
                    extraClassPathList.refresh();
                    StructuredSelection sel = new StructuredSelection(obj);
                    extraClassPathList.setSelection(sel);
                }
            }
        });
        data = new GridData();
        data.grabExcessHorizontalSpace = true;
        data.horizontalAlignment = GridData.FILL;
        newBtn.setLayoutData(data);

        _extraClasspathListDriversBtn = new Button(left, SWT.NULL);
        _extraClasspathListDriversBtn.setText(Messages.getString("DriverDialog.ListDrivers")); //$NON-NLS-1$
        _extraClasspathListDriversBtn.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent event) {
                combo.removeAll();
                File file = (File) ((IStructuredSelection) extraClassPathList.getSelection()).getFirstElement();
                if (file != null) {
                    try {
                        MyURLClassLoader cl = new MyURLClassLoader(file.toURI().toURL());
                        Class<?>[] classes = cl.getAssignableClasses(Driver.class);

                        for (int i = 0; i < classes.length; ++i) {
                            combo.add(classes[i].getName());
                        }
                    } catch (Exception ex) {
                    	SQLExplorerPlugin.error(ex);
                    }
                }
                if (combo.getItemCount() > 0) {
                    combo.setText(combo.getItem(0));
                }
                validate();
            }
        });

        data = new GridData();
        data.grabExcessHorizontalSpace = true;
        data.horizontalAlignment = GridData.FILL;
        _extraClasspathListDriversBtn.setLayoutData(data);

        _extraClasspathUpBtn = new Button(left, SWT.NULL);
        _extraClasspathUpBtn.setText(Messages.getString("DriverDialog.Up")); //$NON-NLS-1$
        _extraClasspathUpBtn.setEnabled(false);
        data = new GridData();
        data.grabExcessHorizontalSpace = true;
        data.horizontalAlignment = GridData.FILL;
        _extraClasspathUpBtn.setLayoutData(data);

        _extraClasspathDownBtn = new Button(left, SWT.NULL);
        _extraClasspathDownBtn.setText(Messages.getString("DriverDialog.Down")); //$NON-NLS-1$
        _extraClasspathDownBtn.setEnabled(false);
        data = new GridData();
        data.grabExcessHorizontalSpace = true;
        data.horizontalAlignment = GridData.FILL;
        _extraClasspathDownBtn.setLayoutData(data);

        _extraClasspathDeleteBtn = new Button(left, SWT.NULL);
        _extraClasspathDeleteBtn.setText(Messages.getString("DriverDialog.Delete")); //$NON-NLS-1$
        _extraClasspathDeleteBtn.setEnabled(false);
        _extraClasspathDeleteBtn.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent event) {
                File f = (File) ((IStructuredSelection) extraClassPathList.getSelection()).getFirstElement();
                if (f != null) {
                    defaultModel.remove(f);
                    extraClassPathList.refresh();
                    if (defaultModel.size() > 0) {
                        Object obj = (defaultModel.toArray())[0];
                        StructuredSelection sel = new StructuredSelection(obj);
                        extraClassPathList.setSelection(sel);
                    }
                }
            }
        });
        data = new GridData();
        data.grabExcessHorizontalSpace = true;
        data.horizontalAlignment = GridData.FILL;
        _extraClasspathDeleteBtn.setLayoutData(data);
        extraClassPathList.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                File f = (File) selection.getFirstElement();
                if (f != null) {
                    _extraClasspathDeleteBtn.setEnabled(true);
                    _extraClasspathListDriversBtn.setEnabled(true);
                } else {
                    _extraClasspathListDriversBtn.setEnabled(false);
                    _extraClasspathDeleteBtn.setEnabled(false);
                }
            }
        });

    }
    
    protected void setShellStyle(int newShellStyle) {
        super.setShellStyle(newShellStyle | SWT.RESIZE);
    }

}

class DefaultFileListBoxModel extends java.util.Vector<File> {

    public static final long serialVersionUID = 1;


    public void addFile(File file) {
        addElement(file);
    }


    /**
     * Return the File at the passed index.
     * 
     * @param idx Index to return File for.
     * 
     * @return The File at <TT>idx</TT>.
     * 
     * @throws ArrayInexOutOfBoundsException Thrown if <TT>idx</TT> < 0 or >=
     *             <TT>getSize()</TT>.
     */
    public File getFile(int idx) {
        return get(idx);
    }


    /**
     * Return array of File names in list.
     * 
     * @return array of File names in list.
     */
    public String[] getFileNames() {
        String[] fileNames = new String[this.size()];
        for (int i = 0, limit = fileNames.length; i < limit; ++i) {
            fileNames[i] = getFile(i).getAbsolutePath();
        }
        return fileNames;
    }


    public void insertFileAt(File file, int idx) {
        insertElementAt(file, idx);
    }


    public File removeFile(int idx) {
        return remove(idx);
    }
}

class ClassPathListModel extends DefaultFileListBoxModel {

    public static final long serialVersionUID = 1;


    /**
     * Default ctor.
     */
    public ClassPathListModel() {
        super();
        load();
    }


    /**
     * Build list.
     */
    private void load() {
        removeAllElements();
        String cp = System.getProperty("java.class.path"); //$NON-NLS-1$
        StringTokenizer strtok = new StringTokenizer(cp, File.pathSeparator);
        while (strtok.hasMoreTokens()) {
            addFile(new File(strtok.nextToken()));
        }
    }

}

class FileContentProvider implements IStructuredContentProvider {

    public Object[] getElements(Object input) {
        return ((DefaultFileListBoxModel) input).toArray();
    }


    public void dispose() {
    }


    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }
}

class FileLabelProvider implements ILabelProvider {

    FileLabelProvider() {
    };


    public Image getImage(Object elementx) {
        return null;
    }


    public String getText(Object element) {

        return ((File) element).toString();
    }


    public boolean isLabelProperty(Object element, String property) {
        return true;
    }


    public void dispose() {
    }


    public void removeListener(ILabelProviderListener listener) {
    }


    public void addListener(ILabelProviderListener listener) {
    }

}
