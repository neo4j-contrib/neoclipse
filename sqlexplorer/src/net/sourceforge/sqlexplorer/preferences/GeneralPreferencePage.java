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
package net.sourceforge.sqlexplorer.preferences;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.IConstants;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;

/**
 * Contains general preferences.
 * 
 * NOTE RE PREVIOUS VERSIONS: This class has changes somewhat from the previous version, which
 * used OverlayPreferenceStore as a proxy to access the IPreferenceStore and to provide the automation 
 * given by FieldEditors to ordinary Controls where a FieldEditor was not suitable.
 * 
 * Although it is usually undesirable to replace a piece of code en-masse it was difficult to interpret 
 * what was going on, there was no comments to help, and fields were handled inconsistently, and
 * it generally very complex for a simple requirement.
 * 
 * The design has been drastically simplified; no external code was affected.
 *  
 * @modified John Spackman
 *
 */
public class GeneralPreferencePage extends AbstractPreferencePage {
	
	private Button autoCommitBox;
	private Button commitOnCloseBox;

	public GeneralPreferencePage() {
        super(Messages.getString("General_Preferences_1"), GRID); 
	}

	protected void createFieldEditors() {
		IntegerFieldEditor iEdit;
		StringFieldEditor sEdit;
		
		iEdit = new IntegerFieldEditor(IConstants.PRE_ROW_COUNT,
				Messages.getString("Preview_Max_Rows_3"), getFieldEditorParent(), 5);
		iEdit.setValidRange(1, 100);
		iEdit.setErrorMessage(Messages.getString("Accepted_Range_is__1_-_100_1"));
		iEdit.setEmptyStringAllowed(false);
		addField(iEdit);

		
		iEdit = new IntegerFieldEditor(IConstants.MAX_SQL_ROWS, Messages.getString("SQL_Limit_Rows_2"), getFieldEditorParent());
		iEdit.setValidRange(100, 5000);
		iEdit.setErrorMessage(Messages.getString("Accepted_Range_is__100_-_5000_3"));
		addField(iEdit);
		
		iEdit = new IntegerFieldEditor(IConstants.CLOSE_UNUSED_CONNECTIONS_AFTER, Messages.getString("Preferences.SQLExplorer.CloseStaleConnectionsAfter"), getFieldEditorParent());
		addField(iEdit);

		BooleanFieldEditor bfe;
		addField(bfe = new BooleanFieldEditor(IConstants.AUTO_COMMIT, Messages.getString("GeneralPreferencePage.AutoCommit_1"), getFieldEditorParent()));
		this.autoCommitBox = bfe.getCheckbox();
		addField(bfe = new BooleanFieldEditor(IConstants.COMMIT_ON_CLOSE, Messages.getString("GeneralPreferencePage.Commit_On_Close_2"), getFieldEditorParent()));
		this.commitOnCloseBox = bfe.getCheckbox();


		autoCommitBox.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				checkCommitBoxes();
			}
		});
		commitOnCloseBox.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				checkCommitBoxes();
			}
		});

		addField(new BooleanFieldEditor(IConstants.SQL_ASSIST, Messages.getString("GeneralPreferencePage.Tables_and_columns_auto-completing_assistance._Use_only_with_fast_database_connections_1"), getFieldEditorParent()));

		
		sEdit = new StringFieldEditor(IConstants.SQL_QRY_DELIMITER, Messages.getString("Preferences.SQLExplorer.QueryDelimiter"), getFieldEditorParent());
		sEdit.setEmptyStringAllowed(true);
		sEdit.setTextLimit(1);
		sEdit.setErrorMessage(Messages.getString("Preferences.SQLExplorer.QueryDelimiter.Error"));
		addField(sEdit);
		if (sEdit.getStringValue() == null || sEdit.getStringValue().length() == 0)
			sEdit.loadDefault();

		
		sEdit = new StringFieldEditor(IConstants.SQL_ALT_QRY_DELIMITER, Messages.getString("Preferences.SQLExplorer.AltQueryDelimiter"), getFieldEditorParent());
		sEdit.setEmptyStringAllowed(true);
		sEdit.setTextLimit(4);
		addField(sEdit);

		
		sEdit = new StringFieldEditor(IConstants.SQL_SL_COMMENT, Messages.getString("Preferences.SQLExplorer.SLComment"), getFieldEditorParent());
		sEdit.setEmptyStringAllowed(true);
		sEdit.setTextLimit(4);
		sEdit.setErrorMessage(Messages.getString("Preferences.SQLExplorer.SLComment.Error"));
		addField(sEdit);
		if (sEdit.getStringValue() == null)
			sEdit.loadDefault();

		sEdit = new StringFieldEditor(IConstants.SQL_ML_COMMENT_START, Messages.getString("Preferences.SQLExplorer.MLCommentStart"), getFieldEditorParent());
		sEdit.setEmptyStringAllowed(true);
		sEdit.setTextLimit(4);
		sEdit.setErrorMessage(Messages.getString("Preferences.SQLExplorer.MLCommentStart.Error"));
		addField(sEdit);
		if (sEdit.getStringValue() == null)
			sEdit.loadDefault();

		sEdit = new StringFieldEditor(IConstants.SQL_ML_COMMENT_END, Messages.getString("Preferences.SQLExplorer.MLCommentEnd"), getFieldEditorParent());
		sEdit.setEmptyStringAllowed(true);
		sEdit.setTextLimit(4);
		sEdit.setErrorMessage(Messages.getString("Preferences.SQLExplorer.MLCommentEnd.Error"));
		addField(sEdit);
		if (sEdit.getStringValue() == null)
			sEdit.loadDefault();

		sEdit = new StringFieldEditor(IConstants.SQL_QUOTE_ESCAPE_CHAR, Messages.getString("Preferences.SQLExplorer.QuoteEscapeChar"), getFieldEditorParent());
		sEdit.setEmptyStringAllowed(true);
		sEdit.setTextLimit(1);
		sEdit.setErrorMessage(Messages.getString("Preferences.SQLExplorer.QuoteEscapeChar.Error"));
		addField(sEdit);
		if (sEdit.getStringValue() == null)
			sEdit.loadDefault();

		
		addField(new BooleanFieldEditor(IConstants.WORD_WRAP, Messages.getString("Preferences.SQLExplorer.WordWrap"), getFieldEditorParent()));
		addField(new BooleanFieldEditor(IConstants.AUTO_OPEN_EDITOR, Messages.getString("Preferences.SQLExplorer.OpenEditorOnConnection"), getFieldEditorParent()));
		addField(new BooleanFieldEditor(IConstants.TREAT_NEW_AS_SCRATCH, Messages.getString("Preferences.SQLExplorer.TreatNewAsScratch"), getFieldEditorParent()));
		addField(new BooleanFieldEditor(IConstants.STOP_ON_ERROR, Messages.getString("Preferences.SQLExplorer.StopOnError"), getFieldEditorParent()));
		addField(new BooleanFieldEditor(IConstants.LOG_SUCCESS_MESSAGES, Messages.getString("Preferences.SQLExplorer.LogSuccessMessages"), getFieldEditorParent()));
		addField(new BooleanFieldEditor(IConstants.LOG_SQL_WARNINGS, Messages.getString("Preferences.SQLExplorer.LogSqlWarnings"), getFieldEditorParent()));
		addField(new BooleanFieldEditor(IConstants.REQUIRE_SAVE_ON_CLOSE_EDITOR, Messages.getString("Preferences.SQLExplorer.RequireSaveOnClose"), getFieldEditorParent()));
		addField(new BooleanFieldEditor(IConstants.ENABLE_STRUCTURED_COMMENTS, Messages.getString("Preferences.SQLExplorer.EnableStructuredComments"), getFieldEditorParent()));
		addField(new BooleanFieldEditor(IConstants.SHOW_SCHEMA_ON_TABLES, Messages.getString("Preferences.SQLExplorer.ShowSchemasOnTables"), getFieldEditorParent()));
		addField(new BooleanFieldEditor(IConstants.SORT_COLUMNS_IN_TREE, Messages.getString("Preferences.DbStructure.SortColumns"), getFieldEditorParent()));
		addField(new BooleanFieldEditor(IConstants.SHOW_SESSION_IN_EDITOR_TITLE, Messages.getString("Preferences.SQLEditor.ShowSessionInTitle"), getFieldEditorParent()));
		addField(new BooleanFieldEditor(IConstants.STRIP_COMMENTS, Messages.getString("Preferences.SQLEditor.StripComments"), getFieldEditorParent()));
		
		final String[][] DEBUG_LEVEL_VALUES = new String[][] {
				{ Messages.getString("Preferences.SQLExplorer.DebugLevelOff"), IConstants.QUERY_DEBUG_OFF },
				{ Messages.getString("Preferences.SQLExplorer.DebugLevelFailed"), IConstants.QUERY_DEBUG_FAILED },
				{ Messages.getString("Preferences.SQLExplorer.DebugLevelAll"), IConstants.QUERY_DEBUG_ALL }
		};
		ComboFieldEditor combo = new ComboFieldEditor(IConstants.QUERY_DEBUG_LOG_LEVEL, Messages.getString("Preferences.SQLExplorer.QueryDebugLog"), DEBUG_LEVEL_VALUES, getFieldEditorParent());
		addField(combo);
	}

	@Override
	protected void initialize() {
		super.initialize();
		checkCommitBoxes();
	}

	private void checkCommitBoxes() {
		
    	boolean checked = autoCommitBox.getSelection();
    	if(checked)
    	{
    		commitOnCloseBox.setSelection(false);
    	}
    	commitOnCloseBox.setEnabled(!checked);
    	checked = commitOnCloseBox.getSelection();
    	if(checked)
    	{
    		autoCommitBox.setSelection(false);
    	}
    	autoCommitBox.setEnabled(!checked);
	}
	
}
