/*
 * Licensed to "Neo Technology," Network Engine for Objects in Lund AB
 * (http://neotechnology.com) under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership. Neo Technology licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License
 * at (http://www.apache.org/licenses/LICENSE-2.0). Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.neo4j.neoclipse.preference;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.graphdb.GraphDbServiceMode;

/**
 * The page for neo4j preferences.
 * 
 * @author Peter H&auml;nsgen
 * @author Anders Nawroth
 */
public class PreferencePage extends AbstractPreferencePage
{
    private static final String NEO4J_CONNECTION_MODE = "Connection mode:";
    private static final String HELP_ON_START_LABEL = "Show help view on startup";
    // database location
    private static final String NEO4J_DATABASE_LOCATION_LABEL = "Database location:";
    private static final String NEO4J_DATABASE_LOCATION_ERROR = "The database location is invalid.";
    // resource uri
    private static final String DATABASE_RESOURCE_URI_LABEL = "Database resource URI:";
    private DirectoryFieldEditor locationField;
    private StringFieldEditor resourceUriField;
    private RadioGroupFieldEditor connectionMode;

    /**
     * Initializes the several input fields.
     */
    @Override
    protected void createFieldEditors()
    {
        // connection mode
        String[][] labels = {
            { "read/write embedded",
                GraphDbServiceMode.READ_WRITE_EMBEDDED.name() },
            { "read-only embedded",
                GraphDbServiceMode.READ_ONLY_EMBEDDED.name() },
            { "remote", GraphDbServiceMode.REMOTE.name() } };
        connectionMode = new RadioGroupFieldEditor(
                Preferences.CONNECTION_MODE, NEO4J_CONNECTION_MODE, 1,
                labels, getFieldEditorParent() );
        addField( connectionMode );

        // location
        locationField = new DirectoryFieldEditor(
                Preferences.DATABASE_LOCATION,
                NEO4J_DATABASE_LOCATION_LABEL, getFieldEditorParent() );
        locationField.setEmptyStringAllowed( false );
        locationField.setErrorMessage( NEO4J_DATABASE_LOCATION_ERROR );
        addField( locationField );

        // resource uri
        resourceUriField = new StringFieldEditor(
                Preferences.DATABASE_RESOURCE_URI,
                DATABASE_RESOURCE_URI_LABEL, getFieldEditorParent() );
        resourceUriField.setEmptyStringAllowed( true );
        addField( resourceUriField, "experimental" );

        // show help view on startup
        BooleanFieldEditor helpOnStart = new BooleanFieldEditor(
                Preferences.HELP_ON_START, HELP_ON_START_LABEL,
                getFieldEditorParent() );
        addField( helpOnStart );

        // initialize UI
        final IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
        String mode = preferenceStore.getString( Preferences.CONNECTION_MODE );
        setStateForConnectionMode( mode );
    }

    private void enableEmbedded()
    {
        locationField.setEnabled( true, getFieldEditorParent() );
        resourceUriField.setEnabled( false, getFieldEditorParent() );
    }

    private void enableRemote()
    {
        locationField.setEnabled( false, getFieldEditorParent() );
        resourceUriField.setEnabled( true, getFieldEditorParent() );
    }

    private void setStateForConnectionMode( GraphDbServiceMode mode )
    {
        switch ( mode )
        {
        case READ_WRITE_EMBEDDED:
        case READ_ONLY_EMBEDDED:
            enableEmbedded();
            break;
        case REMOTE:
            enableRemote();
            break;
        }
    }

    private void setStateForConnectionMode( String mode )
    {
        try
        {
            GraphDbServiceMode newConnectionMode;
            newConnectionMode = GraphDbServiceMode.valueOf( mode );
            setStateForConnectionMode( newConnectionMode );
        }
        catch ( IllegalArgumentException e )
        {
            // do nothing
        }
    }

    @Override
    public void propertyChange( PropertyChangeEvent event )
    {
        super.propertyChange( event );
        setStateForConnectionMode( (String) event.getNewValue() );
    }
}
