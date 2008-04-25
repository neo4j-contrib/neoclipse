/*
 * NeoPreferencePage.java
 */
package org.neo4j.neoclipse.preference;

import org.eclipse.jface.preference.DirectoryFieldEditor;

/**
 * The page for neo preferences.
 * 
 * @author	Peter H&auml;nsgen
 */
public class NeoPreferencePage extends AbstractPreferencePage
{
    /**
     * Initializes the several input fields.
     */
    protected void createFieldEditors()
    {
        DirectoryFieldEditor locationField = new DirectoryFieldEditor(NeoPreferences.DATABASE_LOCATION,
                "Neo Database Location:",
                getFieldEditorParent());
        locationField.setEmptyStringAllowed(false);
        locationField.setErrorMessage("The Neo Database Location is invalid.");
        addField(locationField);        
    }
}
