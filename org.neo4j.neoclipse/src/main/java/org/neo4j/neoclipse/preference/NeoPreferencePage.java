/*
 * NeoPreferencePage.java
 */
package org.neo4j.neoclipse.preference;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.neo4j.neoclipse.NeoIcons;

/**
 * The page for neo preferences.
 * @author Peter H&auml;nsgen
 * @author Anders Nawroth
 */
public class NeoPreferencePage extends AbstractPreferencePage
{
    // database location
    private static final String NEO_DATABASE_LOCATION_LABEL = "Neo Database Location:";
    private static final String NEO_DATABASE_LOCATION_ERROR = "The Neo Database Location is invalid.";
    private static final String DATABASE_LOCATION_EXPLANATION = "the directory where the database files are stored";
    // node label properties
    private static final String NODE_LABEL_PROPERTIES_LABEL = "Node Label properties:";
    private static final String PROPTERTY_NAMES_EXPLANATION = "comma-separated list of property names; will be evaluated from left to right, and the first non-empty value is used";
    // icon locations
    private static final String NODE_ICONS_LOCATION_LABEL = "Node Icons Location:";
    private static final String NODE_ICONS_LOCATION_ERROR = "The Icons Location is invalid.";
    private static final String ICON_LOCATION_EXPLANATION = "the icon filenames should correspond to the settings for node icon filename properties";
    // node icon filename properties
    private static final String NODE_ICON_FILENAME_PROPERTIES_LABEL = "Node Icon filename properties:";
    private static final String ICON_PROPERTY_NAMES_EXPLANATION_ADDITION = "; file extensions are added automatically to the property values found";
    // help icon text
    private static final String MORE_INFORMATION = "move over the field to see more information!";

    /**
     * Initializes the several input fields.
     */
    protected void createFieldEditors()
    {
        // database location
        DirectoryFieldEditor locationField = new DirectoryFieldEditor(
            NeoPreferences.DATABASE_LOCATION, NEO_DATABASE_LOCATION_LABEL,
            getFieldEditorParent() );
        locationField.setEmptyStringAllowed( false );
        locationField.setErrorMessage( NEO_DATABASE_LOCATION_ERROR );
        addDecorationOnDirectoryField( locationField,
            DATABASE_LOCATION_EXPLANATION );
        addField( locationField );
        // node label properties
        StringFieldEditor propertyNameField = new StringFieldEditor(
            NeoPreferences.NODE_PROPERTY_NAMES, NODE_LABEL_PROPERTIES_LABEL,
            getFieldEditorParent() );
        propertyNameField.setEmptyStringAllowed( true );
        addDecorationOnStringField( propertyNameField,
            PROPTERTY_NAMES_EXPLANATION );
        addField( propertyNameField );
        // icon locations
        DirectoryFieldEditor iconLocationField = new DirectoryFieldEditor(
            NeoPreferences.NODE_ICON_LOCATION, NODE_ICONS_LOCATION_LABEL,
            getFieldEditorParent() );
        iconLocationField.setEmptyStringAllowed( true );
        iconLocationField.setErrorMessage( NODE_ICONS_LOCATION_ERROR );
        addDecorationOnDirectoryField( iconLocationField,
            ICON_LOCATION_EXPLANATION );
        addField( iconLocationField );
        // node icon filename properties
        StringFieldEditor iconPropertyNameField = new StringFieldEditor(
            NeoPreferences.NODE_ICON_PROPERTY_NAMES,
            NODE_ICON_FILENAME_PROPERTIES_LABEL, getFieldEditorParent() );
        iconPropertyNameField.setEmptyStringAllowed( true );
        addDecorationOnStringField( iconPropertyNameField,
            PROPTERTY_NAMES_EXPLANATION
                + ICON_PROPERTY_NAMES_EXPLANATION_ADDITION );
        addField( iconPropertyNameField );
    }

    protected void addDecorationOnStringField( StringFieldEditor field,
        String helptext )
    {
        ControlDecoration fieldDecoration = new ControlDecoration( field
            .getTextControl( getFieldEditorParent() ), SWT.LEAD );
        fieldDecoration
            .setDescriptionText( MORE_INFORMATION );
        fieldDecoration.setImage( NeoIcons.getImage( NeoIcons.HELP ) );
        field.getTextControl( getFieldEditorParent() )
            .setToolTipText( helptext );
    }

    protected void addDecorationOnDirectoryField( DirectoryFieldEditor field,
        String helptext )
    {
        ControlDecoration fieldDecoration = new ControlDecoration( field
            .getTextControl( getFieldEditorParent() ), SWT.LEAD );
        fieldDecoration
            .setDescriptionText( MORE_INFORMATION );
        fieldDecoration.setImage( NeoIcons.getImage( NeoIcons.HELP ) );
        field.getTextControl( getFieldEditorParent() )
            .setToolTipText( helptext );
    }
}
