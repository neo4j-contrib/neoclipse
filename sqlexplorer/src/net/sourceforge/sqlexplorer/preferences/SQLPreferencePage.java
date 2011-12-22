/*
 * Copyright (C) 2002-2004 Andrea Mazzolini
 * andreamazzolini@users.sourceforge.net
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
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.preferences.OverlayPreferenceStore.OverlayKey;
import net.sourceforge.sqlexplorer.sqleditor.SQLTextViewer;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Preferences for SQL Substantial reworking of the preferences page for bug
 * fixing and simplicity; see GeneralPreferencePage for more details.
 * 
 * This page uses OverlayPreferenceStore (copied from the Eclipse source because
 * it's package private) to act as a buffer between the controls onscreen and the
 * IPreferenceStore provided by Eclipse for us to keep the user's choices in. When 
 * the user click's OK, the settings in the OverlayPreferenceStore are copied 
 * into the Eclipse-provided store.
 * 
 * This allows us to feed the preference store to other code so that we can
 * preview the settings before they are commited.
 * 
 * When adding new fields, add an entry into the PREFERENCES array for the new
 * type of it will not be persisted.
 * 
 * @modified John Spackman
 */
public class SQLPreferencePage extends OverlaidPreferencePage implements IWorkbenchPreferencePage {

	private static final String BOLD = "_bold";

	/*
	 * Class which models the syntax highlighting settings for a given aspect of
	 * syntax
	 */
	private class Highlight {
		// Preference ID
		private String id;

		// Caption - eg "Table name"
		private String caption;

		public Highlight(String caption, String id) {
			super();
			this.id = id;
			this.caption = caption;
		}
	};
	
	// This is a list of all the preferences and their types which available on this page; the OverlayPreferenceStore uses
	//	this to determine whether it should store updated values, and if so which method to invoke
	public static final OverlayPreferenceStore.OverlayKey[] PREFERENCES = new OverlayPreferenceStore.OverlayKey[] {
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, IConstants.FONT),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, IConstants.SQL_COLOR_MULTILINE_COMMENT),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, IConstants.SQL_COLOR_MULTILINE_COMMENT + BOLD),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, IConstants.SQL_COLOR_TABLE),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, IConstants.SQL_COLOR_TABLE + BOLD),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, IConstants.SQL_COLOR_COLUMS),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, IConstants.SQL_COLOR_COLUMS + BOLD),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, IConstants.SQL_COLOR_SINGLE_LINE_COMMENT),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, IConstants.SQL_COLOR_SINGLE_LINE_COMMENT + BOLD),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, IConstants.SQL_COLOR_DEFAULT),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, IConstants.SQL_COLOR_DEFAULT + BOLD),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, IConstants.SQL_COLOR_STRING),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, IConstants.SQL_COLOR_STRING + BOLD),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, IConstants.SQL_COLOR_KEYWORD),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, IConstants.SQL_COLOR_KEYWORD + BOLD),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, IConstants.CLIP_EXPORT_COLUMNS),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, IConstants.CLIP_EXPORT_SEPARATOR)
	};

	// Syntax highlighting
	private final Highlight[] highlights = new Highlight[] {
			new Highlight(Messages.getString("SQL_Table_1"), IConstants.SQL_COLOR_TABLE),
			new Highlight(Messages.getString("SQL_Column_2"), IConstants.SQL_COLOR_COLUMS),
			new Highlight(Messages.getString("SQL_Keyword_3"), IConstants.SQL_COLOR_KEYWORD),
			new Highlight(Messages.getString("SQL_Single_Line_Comment_4"), IConstants.SQL_COLOR_SINGLE_LINE_COMMENT),
			new Highlight(Messages.getString("SQL_Multi_Line_Comment_5"), IConstants.SQL_COLOR_MULTILINE_COMMENT),
			new Highlight(Messages.getString("String_6"), IConstants.SQL_COLOR_STRING),
			new Highlight(Messages.getString("Others_7"), IConstants.SQL_COLOR_DEFAULT) };

	// FieldEditor for fonts
	private FontFieldEditor fontFieldEditor;

	/**
	 * Constructor
	 */
	public SQLPreferencePage() {
		super(Messages.getString("Sql_Editor_Preferences_2"), GRID);
	}

	protected OverlayKey[] getSupportedPreferences() {
		return PREFERENCES;
	}

	/*
	 * (non-JavaDoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	protected void createFieldEditors() {
		try {
			// Font picker
			fontFieldEditor = new FontFieldEditor(IConstants.FONT, Messages.getString("Text_Font__3"), getFieldEditorParent());
			addField(fontFieldEditor);
	
			/*
			 * Text Properties group
			 */
			Group colorGroup = new Group(getFieldEditorParent(), SWT.NULL);
			colorGroup.setLayout(new GridLayout());
			GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
			gd.horizontalSpan = 3;
			colorGroup.setLayoutData(gd);
			colorGroup.setText(Messages.getString("Text_Properties_6"));
	
			Composite editorComposite = new Composite(colorGroup, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.numColumns = 2;
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			editorComposite.setLayout(layout);
			gd = new GridData(GridData.FILL_BOTH);
			editorComposite.setLayoutData(gd);
	
			final List syntaxColorList = new List(editorComposite, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
			gd = new GridData(GridData.FILL_BOTH);
			gd.heightHint = convertHeightInCharsToPixels(5);
			syntaxColorList.setLayoutData(gd);
			for (int i = 0; i < highlights.length; i++)
				syntaxColorList.add(highlights[i].caption);
	
			Composite stylesComposite = new Composite(editorComposite, SWT.NONE);
			layout = new GridLayout();
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			layout.numColumns = 2;
			stylesComposite.setLayout(layout);
			stylesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
	
			Label label = new Label(stylesComposite, SWT.LEFT);
			label.setText(Messages.getString("Color_9"));
			gd = new GridData();
			gd.horizontalAlignment = GridData.BEGINNING;
			label.setLayoutData(gd);
	
			final ColorEditor syntaxForegroundColorEditor = new ColorEditor(stylesComposite);
			Button foregroundColorButton = syntaxForegroundColorEditor.getButton();
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalAlignment = GridData.BEGINNING;
			foregroundColorButton.setLayoutData(gd);
			foregroundColorButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					int i = syntaxColorList.getSelectionIndex();
					PreferenceConverter.setValue(getPreferenceStore(), highlights[i].id, syntaxForegroundColorEditor.getColorValue());
				}
			});
	
			final Button boldCheckBox = new Button(stylesComposite, SWT.CHECK);
			boldCheckBox.setText(Messages.getString("Bold_10"));
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalAlignment = GridData.BEGINNING;
			gd.horizontalSpan = 2;
			boldCheckBox.setLayoutData(gd);
			boldCheckBox.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					int i = syntaxColorList.getSelectionIndex();
					getPreferenceStore().setValue(highlights[i].id + BOLD, boldCheckBox.getSelection());
				}
			});
	
			syntaxColorList.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					int i = syntaxColorList.getSelectionIndex();
					if (i > -1) {
						boldCheckBox.setSelection(getPreferenceStore().getBoolean(highlights[i].id + BOLD));
						syntaxForegroundColorEditor.setColorValue(PreferenceConverter.getColor(getPreferenceStore(), highlights[i].id));
					}
				}
			});
	
			/*
			 * Preview group
			 */
			Group previewGroup = new Group(getFieldEditorParent(), SWT.NULL);
			previewGroup.setLayout(new GridLayout());
			gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
			gd.horizontalSpan = 3;
			previewGroup.setLayoutData(gd);
			previewGroup.setText(Messages.getString("Preview_7"));
	
			Control previewer = createPreviewer(previewGroup);
			gd = new GridData(GridData.FILL_BOTH);
			previewer.setLayoutData(gd);
	
			/*
			 * Export To Clipboard group
			 */
			Group exportGroup = new Group(getFieldEditorParent(), SWT.NULL);
			exportGroup.setLayout(new GridLayout());
			exportGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
			exportGroup.setText(Messages.getString("Export_to_Clipboard_1"));
			Label lbt1 = new Label(exportGroup, SWT.NULL);
			lbt1.setText("Separator");
			final Button semiColon = new Button(exportGroup, SWT.RADIO);
			semiColon.setText(";");
			final Button pipe = new Button(exportGroup, SWT.RADIO);
			pipe.setText("|");
			final Button tab = new Button(exportGroup, SWT.RADIO);
			tab.setText("\\t [TAB]");
	
			addAccessor(new Accessor() {
				public void load() {
					String value = getPreferenceStore().getString(IConstants.CLIP_EXPORT_SEPARATOR);
					if (value == null || value.length() < 1)
						value = ";";
					loadValue(value.charAt(0));
				}
	
				public void loadDefaults() {
					String value = getPreferenceStore().getDefaultString(IConstants.CLIP_EXPORT_SEPARATOR);
					if (value == null || value.length() < 1)
						value = ";";
					loadValue(value.charAt(0));
				}
	
				private void loadValue(char c) {
					semiColon.setSelection(c == ';');
					pipe.setSelection(c == '|');
					tab.setSelection(c == '\t');
				}
	
				public void store() {
					String separator;
					if (semiColon.getSelection())
						separator = ";";
					else if (semiColon.getSelection())
						separator = "|";
					else
						separator = "\t";
					getPreferenceStore().setValue(IConstants.CLIP_EXPORT_SEPARATOR, separator);
				}
			});
	
			addField(new BooleanFieldEditor(IConstants.CLIP_EXPORT_COLUMNS, "Export column names", exportGroup));
		}catch(Exception e) {
			SQLExplorerPlugin.error("Could not create SQL preference page", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Creates the synatx highlighting previewer
	 * 
	 * @param parent
	 * @return
	 */
	private Control createPreviewer(Composite parent) {
		final String separator = System.getProperty("line.separator");
		final String content = 
			Messages.getString("select_*_from_MyTable_--_single_line_comment_12") + separator + 
			Messages.getString("/*_multi_line_comment_13") + separator + //$NON-NLS-2$
			Messages.getString("select_*_14") + separator + 
			Messages.getString("end_multi_line_comment*/_15") + separator + 
			Messages.getString("where_A___1___16"); //$NON-NLS-2$

		// Get a text viewer and load our sample into it
		final SQLTextViewer fPreviewViewer = new SQLTextViewer(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER, getPreferenceStore(), null);
		fPreviewViewer.setEditable(false);
		IDocument document = new Document(content);
		fPreviewViewer.setDocument(document);

        getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
        	public void propertyChange(PropertyChangeEvent event) {
        		// String p= event.getProperty();
        		fPreviewViewer.invalidateTextPresentation();
        		}
        	});
        
        return fPreviewViewer.getControl();
	}

}
