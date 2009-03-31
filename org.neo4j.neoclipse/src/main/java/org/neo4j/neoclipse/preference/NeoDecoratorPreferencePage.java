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

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;

/**
 * The page for neo preferences.
 * @author Peter H&auml;nsgen
 * @author Anders Nawroth
 */
public class NeoDecoratorPreferencePage extends AbstractPreferencePage
{
    // node label properties
    private static final String NODE_LABEL_PROPERTIES_LABEL = "Node label properties:";
    private static final String PROPTERTY_NAMES_NOTE = "comma-separated list of property keys; will be evaluated from left to right, and the first non-empty value is used";
    // relationship label properties
    private static final String RELATIONSHIP_LABEL_PROPERTIES_LABEL = "Relationship label properties:";
    // icon locations
    private static final String NODE_ICONS_LOCATION_LABEL = "Node icons location:";
    private static final String NODE_ICONS_LOCATION_ERROR = "The Node icons location is invalid.";
    private static final String ICON_LOCATION_NOTE = "the icon filenames should correspond to the settings for node icon filename properties";
    // node icon filename properties
    private static final String NODE_ICON_FILENAME_PROPERTIES_LABEL = "Node icon filename properties:";
    private static final String ICON_PROPERTY_NAMES_NOTE = "comma-separated list (see node labels); file EXTENSIONS are added automatically to the property values found";

    /**
     * Initializes the several input fields.
     */
    protected void createFieldEditors()
    {
        // node label properties
        StringFieldEditor propertyNameField = new StringFieldEditor(
            NeoDecoratorPreferences.NODE_PROPERTY_NAMES,
            NODE_LABEL_PROPERTIES_LABEL, getFieldEditorParent() );
        propertyNameField.setEmptyStringAllowed( true );
        addField( propertyNameField, PROPTERTY_NAMES_NOTE );

        // node label properties
        StringFieldEditor relPropertyNameField = new StringFieldEditor(
            NeoDecoratorPreferences.RELATIONSHIP_PROPERTY_NAMES,
            RELATIONSHIP_LABEL_PROPERTIES_LABEL, getFieldEditorParent() );
        relPropertyNameField.setEmptyStringAllowed( true );
        addField( relPropertyNameField, PROPTERTY_NAMES_NOTE );

        // icon locations
        DirectoryFieldEditor iconLocationField = new DirectoryFieldEditor(
            NeoDecoratorPreferences.NODE_ICON_LOCATION,
            NODE_ICONS_LOCATION_LABEL, getFieldEditorParent() );
        iconLocationField.setEmptyStringAllowed( true );
        iconLocationField.setErrorMessage( NODE_ICONS_LOCATION_ERROR );
        addField( iconLocationField, ICON_LOCATION_NOTE );

        // node icon filename properties
        StringFieldEditor iconPropertyNameField = new StringFieldEditor(
            NeoDecoratorPreferences.NODE_ICON_PROPERTY_NAMES,
            NODE_ICON_FILENAME_PROPERTIES_LABEL, getFieldEditorParent() );
        iconPropertyNameField.setEmptyStringAllowed( true );
        addField( iconPropertyNameField, ICON_PROPERTY_NAMES_NOTE );
    }
}
