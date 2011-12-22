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

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbproduct.Alias;
import net.sourceforge.sqlexplorer.dbproduct.User;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.util.ImageUtil;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class PasswordConnDlg extends TitleAreaDialog {

    private static final int SIZING_TEXT_FIELD_WIDTH = 250;

    // Alias; this MUST match the user's alias (if there is a User object)
    private Alias alias;
    
    // User
    private User user;

    private Text userTxt;

    private Text pswdTxt;

    private Button fAutoCommitBox;
    private Button fCommitOnCloseBox;

    private String userName;
    private String passwd;

    private boolean autoCommit = false;

    private boolean commitOnClose = false;

    public PasswordConnDlg(Shell parentShell, Alias alias, User user) {
        super(parentShell);
        this.alias = alias;
        this.user = user;
    }

    protected void setShellStyle(int newShellStyle) {
        super.setShellStyle(newShellStyle | SWT.RESIZE);// Make the dialog resizable
    }
    
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(Messages.getString("PasswordConnDlg.Login")); //$NON-NLS-1$
    }

    protected Control createContents(Composite parent) {

        Control contents = super.createContents(parent);

        setTitle(Messages.getString("PasswordConnDlg.Login")); //$NON-NLS-1$
        setMessage(Messages.getString("PasswordConnDlg.Insert_Password")); //$NON-NLS-1$

        Image image = ImageUtil.getImage("Images.WizardLogo"); //$NON-NLS-1$
        if (image != null) {
            setTitleImage(image);
        }
        
        contents.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                ImageUtil.disposeImage("Images.WizardLogo");                 //$NON-NLS-1$
            }            
        });
        
        return contents;
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
        layout.numColumns = 2;
        layout.marginWidth = 10;
        nameGroup.setLayout(layout);
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
        nameGroup.setLayoutData(data);

        Label label = new Label(nameGroup, SWT.WRAP);
        label.setText(Messages.getString("Alias")); //$NON-NLS-1$
        Label aliasTxt = new Label(nameGroup, SWT.WRAP);
        aliasTxt.setText(alias.getName());
        Label label2 = new Label(nameGroup, SWT.WRAP);
        label2.setText(Messages.getString("Driver")); //$NON-NLS-1$
        Label driverTxt = new Label(nameGroup, SWT.WRAP);
        driverTxt.setText(alias.getDriver().getName());
        Label label3 = new Label(nameGroup, SWT.WRAP);
        label3.setText(Messages.getString("Url")); //$NON-NLS-1$
        Label urlTxt = new Label(nameGroup, SWT.WRAP);
        urlTxt.setText(alias.getUrl());
        Label label4 = new Label(nameGroup, SWT.WRAP);
        label4.setText(Messages.getString("User")); //$NON-NLS-1$
        userTxt = new Text(nameGroup, SWT.BORDER);
        data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
        data.widthHint = SIZING_TEXT_FIELD_WIDTH;
        data.horizontalSpan = 1;
        userTxt.setLayoutData(data);

        Label label5 = new Label(nameGroup, SWT.WRAP);
        label5.setText(Messages.getString("Password")); //$NON-NLS-1$
        pswdTxt = new Text(nameGroup, SWT.BORDER);
        pswdTxt.setEchoChar('*');

        data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
        data.widthHint = SIZING_TEXT_FIELD_WIDTH;
        data.horizontalSpan = 1;
        pswdTxt.setLayoutData(data);
        pswdTxt.setFocus();

        new Label(nameGroup, SWT.None);
        fAutoCommitBox = new Button(nameGroup, SWT.CHECK);
        fAutoCommitBox.setText(Messages.getString("AutoCommit")); //$NON-NLS-1$
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment = GridData.BEGINNING;
        fAutoCommitBox.setLayoutData(gd);

        new Label(nameGroup, SWT.None);
        fCommitOnCloseBox = new Button(nameGroup, SWT.CHECK);
        fCommitOnCloseBox.setText(Messages.getString("Commit_On_Close")); //$NON-NLS-1$
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment = GridData.BEGINNING;
        fCommitOnCloseBox.setLayoutData(gd);

        fAutoCommitBox.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                checkCommitBoxes();
            }
        });

        if (user != null)
        {
        	userTxt.setText(user.getUserName());
        	pswdTxt.setText(user.getPassword());
        	fAutoCommitBox.setSelection(user.isAutoCommit());
        	fCommitOnCloseBox.setSelection(user.isCommitOnClose());
            checkCommitBoxes();
        }
        else
        {
        	IPreferenceStore store = SQLExplorerPlugin.getDefault().getPreferenceStore();
            fCommitOnCloseBox.setSelection(store.getBoolean(IConstants.COMMIT_ON_CLOSE));//$NON-NLS-1$
            fAutoCommitBox.setSelection(store.getBoolean(IConstants.AUTO_COMMIT));//$NON-NLS-1$        	
        }

        return parentComposite;
    }

	private void checkCommitBoxes() {
    	boolean checked = fAutoCommitBox.getSelection();
    	if(checked)
    	{
    		fCommitOnCloseBox.setSelection(false);
    	}
    	fCommitOnCloseBox.setEnabled(!checked);
    	checked = fCommitOnCloseBox.getSelection();
    	if(checked)
    	{
    		fAutoCommitBox.setSelection(false);
    	}
    	fAutoCommitBox.setEnabled(!checked);
	}

    public String getPassword() {
        return passwd;
    }

    protected void okPressed() {
        passwd = pswdTxt.getText();
        userName = userTxt.getText();
        autoCommit = fAutoCommitBox.getSelection();
        commitOnClose = fCommitOnCloseBox.getSelection();
        super.okPressed();
    }

    public String getUserName() {
        return userName;
    }

    public boolean getAutoCommit() {
        return autoCommit;
    }

    public boolean getCommitOnClose() {
        return commitOnClose;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#open()
	 */
	@Override
	public int open() {
		setBlockOnOpen(false);
		super.open();
		getShell().forceActive();
		runEventLoop(getShell());
		return getReturnCode();
	}

	/**
	 * Runs the event loop for the given shell.
	 * 
	 * @param loopShell
	 *            the shell
	 */
	private void runEventLoop(Shell loopShell) {

		//Use the display provided by the shell if possible
		Display display;
		if (getShell() == null) {
			display = Display.getCurrent();
		} else {
			display = loopShell.getDisplay();
		}

		while (loopShell != null && !loopShell.isDisposed()) {
			try {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			} catch (Exception e) {
				SQLExplorerPlugin.error(e);
			}
		}
		display.update();
	}

}
