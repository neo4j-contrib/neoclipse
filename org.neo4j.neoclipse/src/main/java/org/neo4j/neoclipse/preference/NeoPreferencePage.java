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
import org.eclipse.jface.preference.StringFieldEditor;

/**
 * The page for neo preferences.
 * @author Peter H&auml;nsgen
 * @author Anders Nawroth
 */
public class NeoPreferencePage extends AbstractPreferencePage
{
    private static final String HELP_ON_START_LABEL = "Show help view on startup";
    // database location
    private static final String NEO_DATABASE_LOCATION_LABEL = "Database location:";
    private static final String NEO_DATABASE_LOCATION_ERROR = "The database location is invalid.";
    // resource uri
    private static final String DATABASE_RESOURCE_URI_LABEL = "Database resource URI:";

    /**
     * Initializes the several input fields.
     */
    @Override
    protected void createFieldEditors()
    {
        // database location
        DirectoryFieldEditor locationField = new DirectoryFieldEditor(
            NeoPreferences.DATABASE_LOCATION, NEO_DATABASE_LOCATION_LABEL,
            getFieldEditorParent() );
        locationField.setEmptyStringAllowed( false );
        locationField.setErrorMessage( NEO_DATABASE_LOCATION_ERROR );
        addField( locationField );
        // database resource uri
        StringFieldEditor resourceUriField = new StringFieldEditor(
            NeoPreferences.DATABASE_RESOURCE_URI, DATABASE_RESOURCE_URI_LABEL,
            getFieldEditorParent() );
        resourceUriField.setEmptyStringAllowed( true );
        addField( resourceUriField, "highly experimental" );
        // show help view on startup
        BooleanFieldEditor helpOnStart = new BooleanFieldEditor(
            NeoPreferences.HELP_ON_START, HELP_ON_START_LABEL,
            getFieldEditorParent() );
        addField( helpOnStart );
    }
}
