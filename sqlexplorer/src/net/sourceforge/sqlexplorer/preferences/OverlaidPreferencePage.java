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

import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * Implementation of AbstractPreferencePage which uses the OverlayPreferenceStore 
 * as its IPreferenceStore implementation.  Derived classes should override
 * createFieldEditors() as normal, but also have to implement getSupportedPreferences();
 * you must list all preference in the results of getSupportedPreferences(). 
 * 
 * OverlayPreferenceStore acts as a buffer between the controls onscreen and the
 * IPreferenceStore provided by Eclipse for us to persist the users choices to. When 
 * the user clicks OK, the settings in the OverlayPreferenceStore are copied 
 * into the Eclipse-provided store.  This is useful because it allows us to feed 
 * the preference store to other code so that we can preview the settings before 
 * they are commited.
 * 
 * The FieldEditor implementation assume that they have to keep the value until 
 * their store() is called, so this is only really usefull if you need to preview
 * settings without persisting them back to Eclipse; if you do not need to preview
 * then you should use AbstractPreferencePage instead.
 * 
 * OverlayPreferenceStore was copied from the Eclipse source. 
 * 
 * @author John Spackman
 */
public abstract class OverlaidPreferencePage extends AbstractPreferencePage {

	public OverlaidPreferencePage(int style) {
		super(style);
		initialise();
	}

	public OverlaidPreferencePage(String title, ImageDescriptor image, int style) {
		super(title, image, style);
		initialise();
	}

	public OverlaidPreferencePage(String title, int style) {
		super(title, style);
		initialise();
	}

	/**
	 * Provides an EXHAUSTIVE list of preferences (ID plus type) that this page 
	 * supports, ANYTHING not listed will NOT be stored
	 * @return
	 */
	protected abstract OverlayPreferenceStore.OverlayKey[] getSupportedPreferences();

	/**
	 * Initialises the overlay store, etc
	 */
	protected void initialise() {
		// Create the new store and load values into it
		OverlayPreferenceStore store = new OverlayPreferenceStore(SQLExplorerPlugin.getDefault().getPreferenceStore(), getSupportedPreferences());
		store.start();
		store.load();
		
		// Set as default
		setPreferenceStore(store);
	}
	
	/**
	 * Returns the OverlayPreferenceStore
	 * @return
	 */
	public OverlayPreferenceStore getOverlayStore() {
		return (OverlayPreferenceStore)super.getPreferenceStore();
	}
	
	/* (non-JavaDoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#dispose()
	 */
	public void dispose() {
		getOverlayStore().stop();
		super.dispose();
	}

	/* (non-JavaDoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#addField(org.eclipse.jface.preference.FieldEditor)
	 */
	protected void addField(final FieldEditor editor) {
		// Make sure that FieldEditors automatically write their changes straight to the store
		editor.setPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				editor.store();
			}
		});
		super.addField(editor);
	}

	/* (non-JavaDoc)
	 * @see net.sourceforge.sqlexplorer.preferences.AbstractPreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		super.performDefaults();
		getOverlayStore().loadDefaults();
	}

	/* (non-JavaDoc)
	 * @see net.sourceforge.sqlexplorer.preferences.AbstractPreferencePage#performOk()
	 */
	public boolean performOk() {
		boolean ok = super.performOk();
		// Copy the overlay store into the Eclipse store
		if (ok)
			getOverlayStore().propagate();
		return ok;
	}
}
