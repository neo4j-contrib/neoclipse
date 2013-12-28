/**
 * Licensed to Neo Technology under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Neo Technology licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.neo4j.neoclipse.connection.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.connection.Alias;
import org.neo4j.neoclipse.connection.ConnectionsView;
import org.neo4j.neoclipse.preference.Preferences;
import org.neo4j.neoclipse.util.ApplicationUtil;
import org.neo4j.neoclipse.view.ErrorMessage;

/**
 * @author Radhakrishna Kalyan
 * 
 */
public class CreateAliasDialog extends TitleAreaDialog
{

    private static final int SIZING_TEXT_FIELD_WIDTH = 250;

    public enum Type
    {
        CREATE( "New" ),
        EDIT( "Edit" );

        private String name;

        Type( String str )
        {
            name = str;
        }

        public String getName()
        {
            return name;
        }
    }

    private final Type type;
    private Text nameField;
    private DirectoryFieldEditor urlField;
    private Button autoConnectButton;
    private Button allowUpgrade;
    private Text userField;
    private Text passwordField;

    public CreateAliasDialog( Shell parentShell, Type type )
    {
        super( parentShell );
        this.type = type;
    }

    @Override
    protected void configureShell( Shell shell )
    {

        super.configureShell( shell );
        if ( type == Type.CREATE )
        {
            shell.setText( "Create new connection" );
        }
        else if ( type == Type.EDIT )
        {
            shell.setText( "Edit connection" );
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
            setTitle( "Create new connection" );
        }
        else if ( type == Type.EDIT )
        {
            setTitle( "Edit connection" );
            Alias selectedAlias = Activator.getDefault().getConnectionsView().getSelectedAlias();
            nameField.setEnabled( false );
            autoConnectButton.setEnabled( false );
            allowUpgrade.setSelection( Boolean.parseBoolean( selectedAlias
                    .getConfigurationByKey( GraphDatabaseSettings.allow_store_upgrade.name() ) ) );
            nameField.setText( selectedAlias.getName() );
            urlField.setStringValue( selectedAlias.getUri() );
            userField.setText( ApplicationUtil.returnEmptyIfBlank( selectedAlias.getUserName() ) );
            passwordField.setEchoChar( '*' );
            passwordField.setText( ApplicationUtil.returnEmptyIfBlank( selectedAlias.getPassword() ) );
        }

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
        label.setText( ( "Name *" ) );
        nameField = new Text( nameGroup, SWT.BORDER );
        data = new GridData( GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL );
        data.horizontalSpan = 2;
        data.widthHint = SIZING_TEXT_FIELD_WIDTH;
        nameField.setLayoutData( data );
        nameField.addKeyListener( new KeyListener()
        {

            @Override
            public void keyPressed( org.eclipse.swt.events.KeyEvent e )
            {

                validate();
            };

            @Override
            public void keyReleased( org.eclipse.swt.events.KeyEvent e )
            {

                validate();
            };
        } );

        urlField = new DirectoryFieldEditor( Preferences.DATABASE_LOCATION, "URI *", nameGroup );
        urlField.getTextControl( nameGroup ).addKeyListener( new KeyListener()
        {

            @Override
            public void keyReleased( KeyEvent arg0 )
            {
                validate();
            }

            @Override
            public void keyPressed( KeyEvent arg0 )
            {
                validate();
            }
        } );
        urlField.setPropertyChangeListener( new IPropertyChangeListener()
        {

            @Override
            public void propertyChange( PropertyChangeEvent event )
            {
                validate();

            }
        } );

        new Label( nameGroup, SWT.NONE );
        Label label3 = new Label( nameGroup, SWT.WRAP );
        label3.setText( ( "i.e http://localhost:7474/db/data/ or C:/neo4j/db " ) );
        data = new GridData( GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL );
        data.horizontalSpan = 2;
        data.widthHint = SIZING_TEXT_FIELD_WIDTH;
        label3.setLayoutData( data );

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

        autoConnectButton = new Button( connectionPropertiesComposite, SWT.CHECK );
        GridData gd_autoLogonButton = new GridData( 108, SWT.DEFAULT );
        autoConnectButton.setLayoutData( gd_autoLogonButton );
        autoConnectButton.setText( "Auto Connect" );

        allowUpgrade = new Button( connectionPropertiesComposite, SWT.CHECK );
        GridData gd_allowUpgrade = new GridData( 128, SWT.DEFAULT );
        allowUpgrade.setLayoutData( gd_allowUpgrade );
        allowUpgrade.setText( GraphDatabaseSettings.allow_store_upgrade.name() );
        allowUpgrade.setSelection( true );

        Label label4 = new Label( nameGroup, SWT.WRAP );
        label4.setText( ( "User" ) );
        userField = new Text( nameGroup, SWT.BORDER );
        data = new GridData( GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL );
        data.horizontalSpan = 2;
        data.widthHint = SIZING_TEXT_FIELD_WIDTH;
        userField.setLayoutData( data );

        Label label5 = new Label( nameGroup, SWT.WRAP );
        label5.setText( ( "Password" ) );
        passwordField = new Text( nameGroup, SWT.BORDER );
        passwordField.setEchoChar( '*' );
        data = new GridData( GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL );
        data.horizontalSpan = 2;
        data.widthHint = SIZING_TEXT_FIELD_WIDTH;
        passwordField.setLayoutData( data );

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

        return parentComposite;
    }

    @Override
    protected void okPressed()
    {

        try
        {
            Alias alias = new Alias( nameField.getText(), urlField.getStringValue(), userField.getText(),
                    passwordField.getText() );
            alias.addConfiguration( GraphDatabaseSettings.allow_store_upgrade.name(),
                    Boolean.toString( allowUpgrade.getSelection() ) );
            if ( type == Type.EDIT )
            {
                Alias selectedAlias = Activator.getDefault().getConnectionsView().getSelectedAlias();
                Activator.getDefault().getAliasManager().removeAlias( selectedAlias );
            }
            Activator.getDefault().getAliasManager().addAlias( alias );

            ConnectionsView connectionsView = Activator.getDefault().getConnectionsView();
            if ( autoConnectButton.getSelection() )
            {
                connectionsView.startOrStopConnection( alias );
            }
            close();
        }
        catch ( Exception e )
        {
            ErrorMessage.showDialog( type.getName() + " connection problem", e );
        }
    }

    @Override
    protected void setShellStyle( int newShellStyle )
    {
        super.setShellStyle( newShellStyle | SWT.RESIZE );
    }

    private void validate()
    {
        boolean enableDisable = false;

        if ( !urlField.getStringValue().trim().isEmpty() && ( nameField.getText().trim().length() > 0 ) )
        {
            enableDisable = true;
        }

        Button okBtn = getButton( IDialogConstants.OK_ID );
        if ( okBtn != null )
        {
            okBtn.setEnabled( enableDisable );
        }
    }

}
