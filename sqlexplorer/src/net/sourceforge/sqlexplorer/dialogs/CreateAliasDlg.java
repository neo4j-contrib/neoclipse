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
package net.sourceforge.sqlexplorer.dialogs;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeSet;

import net.sourceforge.sqlexplorer.ExplorerException;
import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbproduct.Alias;
import net.sourceforge.sqlexplorer.dbproduct.DriverManager;
import net.sourceforge.sqlexplorer.dbproduct.ManagedDriver;
import net.sourceforge.sqlexplorer.dbproduct.User;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.util.ImageUtil;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

/**
 * Modified by Davy Vanherbergen to include metadata filter expression.
 * 
 */
public class CreateAliasDlg extends TitleAreaDialog
{

    private static final int SIZING_TEXT_FIELD_WIDTH = 250;

    public enum Type
    {
        CREATE,
        CHANGE,
        COPY
    }

    private Type type;
    private Alias alias;
    private HashMap<Integer, ManagedDriver> comboDriverIndexes = new HashMap<Integer, ManagedDriver>();

    private Text nameField;
    private Combo cboDriver;
    private Text urlField;
    private Button noUsernameRequired;
    private Text userField;
    private Text passwordField;
    private Button autoLogonButton;
    private Button logonAtStartupButton;
    private Button autoCommitButton;
    private Button commitOnCloseButton;

    public CreateAliasDlg( Shell parentShell, Type type, Alias alias )
    {
        super( parentShell );
        this.alias = alias;
        this.type = type;
    }

    @Override
    protected void configureShell( Shell shell )
    {

        super.configureShell( shell );
        if ( type == Type.CREATE )
        {
            shell.setText( Messages.getString( "AliasDialog.Create.Title" ) ); //$NON-NLS-1$
        }
        else if ( type == Type.CHANGE )
        {
            shell.setText( Messages.getString( "AliasDialog.Change.Title" ) ); //$NON-NLS-1$
        }
        else if ( type == Type.COPY )
        {
            shell.setText( Messages.getString( "AliasDialog.Copy.Title" ) ); //$NON-NLS-1$
        }
    }

    @Override
    protected void createButtonsForButtonBar( Composite parent )
    {

        super.createButtonsForButtonBar( parent );
        validate();
    }

    @Override
    protected Control createContents( Composite parent )
    {

        Control contents = super.createContents( parent );

        if ( type == Type.CREATE )
        {
            setTitle( Messages.getString( "AliasDialog.Create.Title" ) ); //$NON-NLS-1$
        }
        else if ( type == Type.CHANGE )
        {
            setTitle( Messages.getString( "AliasDialog.Change.Title" ) ); //$NON-NLS-1$
            setMessage( Messages.getString( "AliasDialog.Change.SubTitle" ) ); //$NON-NLS-1$			
        }
        else if ( type == Type.COPY )
        {
            setTitle( Messages.getString( "AliasDialog.Copy.Title" ) ); //$NON-NLS-1$
            setMessage( Messages.getString( "AliasDialog.Copy.SubTitle" ) ); //$NON-NLS-1$						
        }

        Image image = ImageUtil.getImage( "Images.WizardLogo" ); //$NON-NLS-1$
        if ( image != null )
        {
            setTitleImage( image );
        }
        contents.addDisposeListener( new DisposeListener()
        {

            public void widgetDisposed( DisposeEvent e )
            {

                ImageUtil.disposeImage( "Images.WizardLogo" ); //$NON-NLS-1$
            }
        } );
        PlatformUI.getWorkbench().getHelpSystem().setHelp( parent,
                SQLExplorerPlugin.HELP_PLUGIN_ID + ".connection_view" ); //$NON-NLS-1$

        return contents;
    }

    @Override
    protected Control createDialogArea( Composite parent )
    {

        // top level composite
        Composite parentComposite = (Composite) super.createDialogArea( parent );

        // create a composite with standard margins and spacing
        Composite composite = new Composite( parentComposite, SWT.NONE );
        GridLayout layout = new GridLayout();
        layout.marginHeight = convertVerticalDLUsToPixels( IDialogConstants.VERTICAL_MARGIN );
        layout.marginWidth = convertHorizontalDLUsToPixels( IDialogConstants.HORIZONTAL_MARGIN );
        layout.verticalSpacing = convertVerticalDLUsToPixels( IDialogConstants.VERTICAL_SPACING );
        layout.horizontalSpacing = convertHorizontalDLUsToPixels( IDialogConstants.HORIZONTAL_SPACING );
        composite.setLayout( layout );
        // final GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, true,
        // true);
        // gd_composite.heightHint = 238;
        // composite.setLayoutData(gd_composite);
        composite.setLayoutData( new GridData( GridData.FILL_BOTH ) );
        composite.setFont( parentComposite.getFont() );

        Composite nameGroup = new Composite( composite, SWT.NONE );
        layout = new GridLayout();
        layout.numColumns = 3;
        layout.marginWidth = 10;
        nameGroup.setLayout( layout );
        GridData data = new GridData( SWT.FILL, SWT.CENTER, true, false );
        nameGroup.setLayoutData( data );

        Label label = new Label( nameGroup, SWT.WRAP );
        label.setText( Messages.getString( "Name" ) ); //$NON-NLS-1$
        nameField = new Text( nameGroup, SWT.BORDER );
        if ( type != Type.CREATE )
        {
            nameField.setText( alias.getName() );
        }
        data = new GridData( GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL );
        data.horizontalSpan = 2;
        data.widthHint = SIZING_TEXT_FIELD_WIDTH;
        nameField.setLayoutData( data );
        nameField.addKeyListener( new KeyListener()
        {

            public void keyPressed( org.eclipse.swt.events.KeyEvent e )
            {

                CreateAliasDlg.this.validate();
            };

            public void keyReleased( org.eclipse.swt.events.KeyEvent e )
            {

                CreateAliasDlg.this.validate();
            };
        } );

        Label label2 = new Label( nameGroup, SWT.WRAP );
        label2.setText( Messages.getString( "Choose DB" ) ); //$NON-NLS-1$
        cboDriver = new Combo( nameGroup, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY );
        final GridData gd_driver = new GridData( GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL );
        gd_driver.widthHint = SIZING_TEXT_FIELD_WIDTH;
        cboDriver.setLayoutData( gd_driver );

        String defaultDriverName = SQLExplorerPlugin.getStringPref( IConstants.DEFAULT_DRIVER );
        ManagedDriver defaultDriver = null;
        int defaultDriverIndex = 0;
        populateCombo();
        for ( Entry<Integer, ManagedDriver> entry : comboDriverIndexes.entrySet() )
        {
            ManagedDriver driver = entry.getValue();
            if ( driver.getName().startsWith( defaultDriverName ) )
            {
                defaultDriver = driver;
                defaultDriverIndex = entry.getKey();
                break;
            }
        }

        Button btnListDrivers = new Button( nameGroup, SWT.NULL );
        btnListDrivers.setText( Messages.getString( "AliasDialog.Browse" ) ); //$NON-NLS-1$
        final GridData gd_btnListDrivers = new GridData( GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL );
        btnListDrivers.setLayoutData( gd_btnListDrivers );
        btnListDrivers.addSelectionListener( new SelectionAdapter()
        {

            @Override
            public void widgetSelected( SelectionEvent event )
            {

                DirectoryDialog dialog = new DirectoryDialog(
                        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.OPEN | SWT.SHEET );
                dialog.setText( "Neo4j Database Location" );
                dialog.setMessage( "Please choose the database location." );
                String platform = SWT.getPlatform();
                dialog.setFilterPath( platform.equals( "win32" ) || platform.equals( "wpf" ) ? "c:\\" : "/" );

                String dbLocation = dialog.open();
                if ( dbLocation != null && !dbLocation.isEmpty() )
                {
                    // populateCombo();
                    cboDriver.add( dbLocation );
                }

                // PreferenceDialog dlg =
                // PreferencesUtil.createPreferenceDialogOn(getShell(), null,
                // new String[] { DriverPreferencePage.class.getName() }, null);
                // if (dlg.open() == IDialogConstants.OK_ID)
            }
        } );

        Label label3 = new Label( nameGroup, SWT.WRAP );
        label3.setText( Messages.getString( "Url" ) ); //$NON-NLS-1$
        urlField = new Text( nameGroup, SWT.BORDER );
        data = new GridData( GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL );
        data.horizontalSpan = 2;
        data.widthHint = SIZING_TEXT_FIELD_WIDTH;
        urlField.setLayoutData( data );
        urlField.addKeyListener( new KeyListener()
        {

            public void keyPressed( org.eclipse.swt.events.KeyEvent e )
            {

                CreateAliasDlg.this.validate();
            };

            public void keyReleased( org.eclipse.swt.events.KeyEvent e )
            {

                CreateAliasDlg.this.validate();
            };
        } );
        new Label( nameGroup, SWT.NONE );

        noUsernameRequired = new Button( nameGroup, SWT.CHECK );
        noUsernameRequired.setText( Messages.getString( "AliasDialog.UserNameNotRequiredForDb" ) ); //$NON-NLS-1$
        data = new GridData( GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL );
        data.horizontalSpan = 2;
        noUsernameRequired.setLayoutData( data );

        new Label( nameGroup, SWT.NONE );

        Composite connectionPropertiesComposite = new Composite( nameGroup, SWT.NONE );
        connectionPropertiesComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.marginWidth = 0;
        connectionPropertiesComposite.setLayout( gridLayout );
        data = new GridData( GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL );
        data.horizontalSpan = 2;
        connectionPropertiesComposite.setLayoutData( data );

        autoLogonButton = new Button( connectionPropertiesComposite, SWT.CHECK );
        autoLogonButton.setToolTipText( Messages.getString( "AliasDialog.AutoLogonToolTip" ) ); //$NON-NLS-1$
        GridData gd_autoLogonButton = new GridData( 158, SWT.DEFAULT );
        autoLogonButton.setLayoutData( gd_autoLogonButton );
        autoLogonButton.setText( Messages.getString( "AliasDialog.AutoLogon" ) ); //$NON-NLS-1$

        logonAtStartupButton = new Button( connectionPropertiesComposite, SWT.CHECK );
        logonAtStartupButton.setToolTipText( Messages.getString( "AliasDialog.StartupLogonToolTip" ) ); //$NON-NLS-1$
        logonAtStartupButton.setText( Messages.getString( "AliasDialog.StartupLogon" ) ); //$NON-NLS-1$

        Label label4 = new Label( nameGroup, SWT.WRAP );
        label4.setText( Messages.getString( "User" ) ); //$NON-NLS-1$
        userField = new Text( nameGroup, SWT.BORDER );
        data = new GridData( GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL );
        data.horizontalSpan = 2;
        data.widthHint = SIZING_TEXT_FIELD_WIDTH;
        userField.setLayoutData( data );

        userField.addKeyListener( new KeyListener()
        {

            public void keyPressed( org.eclipse.swt.events.KeyEvent e )
            {

                CreateAliasDlg.this.validate();
            };

            public void keyReleased( org.eclipse.swt.events.KeyEvent e )
            {

                CreateAliasDlg.this.validate();
            };
        } );

        Label label5 = new Label( nameGroup, SWT.WRAP );
        label5.setText( Messages.getString( "Password" ) ); //$NON-NLS-1$
        passwordField = new Text( nameGroup, SWT.BORDER );
        passwordField.setEchoChar( '*' );
        data = new GridData( GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL );
        data.horizontalSpan = 2;
        data.widthHint = SIZING_TEXT_FIELD_WIDTH;
        passwordField.setLayoutData( data );

        cboDriver.addSelectionListener( new SelectionAdapter()
        {
            @Override
            public void widgetSelected( org.eclipse.swt.events.SelectionEvent e )
            {
                int selIndex = cboDriver.getSelectionIndex();
                ManagedDriver driver = comboDriverIndexes.get( selIndex );
                urlField.setText( driver.getUrl() );
                CreateAliasDlg.this.validate();
            };
        } );

        if ( !comboDriverIndexes.isEmpty() && defaultDriver != null )
        {
            cboDriver.select( defaultDriverIndex );
            urlField.setText( defaultDriver.getUrl() );
        }
        new Label( nameGroup, SWT.NONE );

        connectionPropertiesComposite = new Composite( nameGroup, SWT.NONE );
        connectionPropertiesComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
        gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.marginWidth = 0;
        connectionPropertiesComposite.setLayout( gridLayout );
        data = new GridData( GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL );
        data.horizontalSpan = 2;
        connectionPropertiesComposite.setLayoutData( data );

        autoCommitButton = new Button( connectionPropertiesComposite, SWT.CHECK );
        autoCommitButton.setToolTipText( Messages.getString( "AliasDialog.ForNewEditorsHint" ) ); //$NON-NLS-1$
        autoCommitButton.setText( Messages.getString( "AutoCommit" ) ); //$NON-NLS-1$
        data = new GridData( 158, SWT.DEFAULT );
        autoCommitButton.setLayoutData( data );

        commitOnCloseButton = new Button( connectionPropertiesComposite, SWT.CHECK );
        commitOnCloseButton.setToolTipText( Messages.getString( "AliasDialog.ForNewEditorsHint" ) ); //$NON-NLS-1$
        commitOnCloseButton.setText( Messages.getString( "Commit_On_Close" ) ); //$NON-NLS-1$

        autoLogonButton.addSelectionListener( new SelectionAdapter()
        {
            @Override
            public void widgetSelected( SelectionEvent event )
            {
                boolean checked = autoLogonButton.getSelection();
                logonAtStartupButton.setEnabled( checked );
                passwordField.setEnabled( checked );
                if ( !checked )
                {
                    logonAtStartupButton.setSelection( false );
                    passwordField.setText( "" ); //$NON-NLS-1$
                }
            }
        } );

        autoCommitButton.addSelectionListener( new SelectionAdapter()
        {
            @Override
            public void widgetSelected( SelectionEvent e )
            {
                checkCommitBoxes();
            }
        } );
        commitOnCloseButton.addSelectionListener( new SelectionAdapter()
        {
            @Override
            public void widgetSelected( SelectionEvent e )
            {
                checkCommitBoxes();
            }
        } );

        logonAtStartupButton.setEnabled( alias.isAutoLogon() );
        logonAtStartupButton.setSelection( alias.isConnectAtStartup() );
        autoLogonButton.setSelection( alias.isAutoLogon() );
        User user = alias.getDefaultUser();
        if ( user != null )
        {
            autoCommitButton.setSelection( user.isAutoCommit() );
            commitOnCloseButton.setEnabled( !user.isAutoCommit() );
            commitOnCloseButton.setSelection( user.isCommitOnClose() );
        }

        noUsernameRequired.addSelectionListener( new SelectionAdapter()
        {
            @Override
            public void widgetSelected( final SelectionEvent e )
            {
                boolean checked = noUsernameRequired.getSelection();
                userField.setEnabled( !checked );
                passwordField.setEnabled( ( !checked ) && autoLogonButton.getSelection() );
                if ( checked )
                {
                    userField.setText( "" ); //$NON-NLS-1$
                    passwordField.setText( "" ); //$NON-NLS-1$
                    autoLogonButton.setSelection( true );
                    logonAtStartupButton.setEnabled( true );
                }
                autoLogonButton.setEnabled( !checked );
            }
        } );

        if ( alias.hasNoUserName() )
        {
            noUsernameRequired.setSelection( true );
            userField.setEnabled( false );
            passwordField.setEnabled( false );
            autoLogonButton.setSelection( true );
            logonAtStartupButton.setEnabled( true );
            autoLogonButton.setEnabled( false );
        }
        else
        {
            noUsernameRequired.setSelection( false );
            autoLogonButton.setEnabled( true );
            userField.setEnabled( true );
            passwordField.setEnabled( alias.isAutoLogon() );
            if ( alias.getDefaultUser() != null )
            {
                userField.setText( alias.getDefaultUser().getUserName() );
                passwordField.setText( alias.getDefaultUser().getPassword() );
            }
        }

        if ( type != Type.CREATE )
        {
            if ( alias.getDriver() != null )
            {
                cboDriver.setText( alias.getDriver().getName() );
            }
            if ( alias.getUrl() != null )
            {
                urlField.setText( alias.getUrl() );
            }
        }
        return parentComposite;
    }

    private void checkCommitBoxes()
    {
        boolean checked = autoCommitButton.getSelection();
        if ( checked )
        {
            commitOnCloseButton.setSelection( false );
        }
        commitOnCloseButton.setEnabled( !checked );
        checked = commitOnCloseButton.getSelection();
        if ( checked )
        {
            autoCommitButton.setSelection( false );
        }
        autoCommitButton.setEnabled( !checked );
    }

    private void populateCombo()
    {
        String previous = cboDriver.getText();
        if ( previous != null )
        {
            previous = previous.trim();
            if ( previous.length() == 0 )
            {
                previous = null;
            }
        }
        if ( previous != null )
        {
            previous = previous.toLowerCase();
        }
        DriverManager driverModel = SQLExplorerPlugin.getDefault().getDriverModel();
        cboDriver.removeAll();
        TreeSet<ManagedDriver> drivers = new TreeSet<ManagedDriver>();
        drivers.addAll( driverModel.getDrivers() );
        int index = 0;
        for ( ManagedDriver driver : drivers )
        {
            if ( driver.getUrl() != null ) // try to find a method to ping to
                                           // the url
            {
                cboDriver.add( driver.getName() );
                comboDriverIndexes.put( new Integer( index ), driver );
                if ( previous != null && driver.getName().toLowerCase().startsWith( previous ) )
                {
                    cboDriver.select( index );
                }
                index++;
            }
        }
    }

    @Override
    protected void okPressed()
    {

        try
        {
            User previousUser = alias.getDefaultUser();

            alias.setName( nameField.getText().trim() );
            int selIndex = cboDriver.getSelectionIndex();
            ManagedDriver driver = comboDriverIndexes.get( selIndex );
            alias.setDriver( driver );
            alias.setUrl( urlField.getText().trim() );
            if ( noUsernameRequired.getSelection() )
            {
                alias.setHasNoUserName( true );
            }
            else
            {
                alias.setHasNoUserName( false );
                if ( userField.getText().trim().length() > 0 )
                {
                    alias.setDefaultUser( new User( userField.getText().trim(), passwordField.getText().trim() ) );
                }
            }
            alias.setName( this.nameField.getText().trim() );
            alias.setSchemaFilterExpression( "" ); //$NON-NLS-1$
            alias.setNameFilterExpression( "" ); //$NON-NLS-1$
            alias.setFolderFilterExpression( "" ); //$NON-NLS-1$
            alias.setConnectAtStartup( logonAtStartupButton.getSelection() );
            alias.setAutoLogon( autoLogonButton.getSelection() );

            if ( type != Type.CHANGE )
            {
                SQLExplorerPlugin.getDefault().getAliasManager().addAlias( alias );
            }
            else if ( alias.getDefaultUser() != previousUser )
            {
                if ( !previousUser.isInUse() )
                {
                    alias.removeUser( previousUser );
                }
            }

            User user = alias.getDefaultUser();
            if ( user != null )
            {
                user.setAutoCommit( autoCommitButton.getSelection() );
                user.setCommitOnClose( commitOnCloseButton.getSelection() );
            }

        }
        catch ( ExplorerException excp )
        {
            SQLExplorerPlugin.error( "Validation Exception", excp );//$NON-NLS-1$
            // System.out.println(Messages.getString("Error_Validation_Exception_4"));//$NON-NLS-1$
        }

        // Notify that ther has been changes
        SQLExplorerPlugin.getDefault().getAliasManager().modelChanged();

        close();
    }

    protected void setDialogComplete( boolean value )
    {

        Button okBtn = getButton( IDialogConstants.OK_ID );
        if ( okBtn != null )
        {
            okBtn.setEnabled( value );
        }
    }

    @Override
    protected void setShellStyle( int newShellStyle )
    {

        super.setShellStyle( newShellStyle | SWT.RESIZE );// Make the dialog
        // resizable
    }

    void validate()
    {

        if ( ( urlField.getText().trim().length() > 0 ) && ( nameField.getText().trim().length() > 0 ) )
        {
            setDialogComplete( true );
        }
        else
        {
            setDialogComplete( false );
        }
    }

}
