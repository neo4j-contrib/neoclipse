package net.sourceforge.sqlexplorer.preferences;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.Messages;

import org.eclipse.jface.preference.IntegerFieldEditor;

public class HistoryPreferencePage extends AbstractPreferencePage{
	
	public HistoryPreferencePage() {
        super(Messages.getString("Preferences.History.Title"), GRID); 
	}

	@Override
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(
				IConstants.LOG_SQL_HISTORY, 
				Messages.getString("Preferences.SQLExplorer.LogSQLHistory"), 
				getFieldEditorParent()));
		addField(new IntegerFieldEditor(
				IConstants.HISTORY_MAX_ENTRIES, 
				Messages.getString("Preferences.History.MaxEntries"), 
				getFieldEditorParent(),
				5));
		addField(new IntegerFieldEditor(
				IConstants.HISTORY_AUTOSAVE_AFTER, 
				Messages.getString("Preferences.History.AutoSaveAfter"), 
				getFieldEditorParent(),
				5));
		
	}
	
}
