package net.sourceforge.sqlexplorer.preferences;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.Messages;

import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;

public class BatchResultsPreferencePage extends AbstractPreferencePage{
	
	public BatchResultsPreferencePage() {
        super(Messages.getString("Preferences.BatchResults.Title"), GRID); 
	}

	@Override
	protected void createFieldEditors() {
		addField(new FontFieldEditor(
				IConstants.BATCH_RESULT_FONT, 
				Messages.getString("Preferences.BatchResults.Font"), 
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(
				IConstants.BATCH_RESULT_APPEND_ROWS_AFFECTED, 
				Messages.getString("Preferences.BatchResults.AppendRowsAffected"), 
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(
				IConstants.BATCH_RESULT_APPEND_QUERY, 
				Messages.getString("Preferences.BatchResults.AppendQuery"), 
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(
				IConstants.BATCH_RESULT_APPEND_EXEC_TIME, 
				Messages.getString("Preferences.BatchResults.AppendExecTime"), 
				getFieldEditorParent()));
		addField(new IntegerFieldEditor(
				IConstants.BATCH_RESULT_MAX_DISPLAY_WIDTH, 
				Messages.getString("Preferences.BatchResults.MaxDisplayWidth"), 
				getFieldEditorParent(),
				5));
		
	}
	
}
