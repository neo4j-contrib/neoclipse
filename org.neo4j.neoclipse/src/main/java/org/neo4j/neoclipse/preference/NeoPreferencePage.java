/*
 * NeoPreferencePage.java
 */
package org.neo4j.neoclipse.preference;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;

/**
 * The page for neo preferences.
 * @author Peter H&auml;nsgen
 * @author Anders Nawroth
 */
public class NeoPreferencePage extends AbstractPreferencePage
{
    /**
     * Initializes the several input fields.
     */
    protected void createFieldEditors()
    {
        DirectoryFieldEditor locationField = new DirectoryFieldEditor(
            NeoPreferences.DATABASE_LOCATION, "Neo Database Location:",
            getFieldEditorParent() );
        locationField.setEmptyStringAllowed( false );
        locationField.setErrorMessage( "The Neo Database Location is invalid." );
        addField( locationField );
        StringFieldEditor propertyNameField = new StringFieldEditor(
            NeoPreferences.NODE_PROPERTY_NAMES, "Node Label properties:",
            getFieldEditorParent() );
        propertyNameField.setEmptyStringAllowed( true );
        addField( propertyNameField );
        DirectoryFieldEditor iconLocationField = new DirectoryFieldEditor(
            NeoPreferences.NODE_ICON_LOCATION, "Node Icons Location:",
            getFieldEditorParent() );
        iconLocationField.setEmptyStringAllowed( true );
        iconLocationField.setErrorMessage( "The Icons Location is invalid." );
        addField( iconLocationField );
        StringFieldEditor iconPropertyNameField = new StringFieldEditor(
            NeoPreferences.NODE_ICON_PROPERTY_NAMES,
            "Node Icon filename properties:", getFieldEditorParent() );
        iconPropertyNameField.setEmptyStringAllowed( true );
        addField( iconPropertyNameField );
    }
}
