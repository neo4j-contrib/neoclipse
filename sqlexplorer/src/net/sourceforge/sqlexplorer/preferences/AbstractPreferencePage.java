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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Base class for preference pages; adds a few methods to automate the load and saving of 
 * preferences of ordinary fields alongside FieldEditors.
 * 
 * Derived classes must implement createFieldEditors() as usual, but where controls have to 
 * be manually created (and loaded/saved from store), an instance of 
 * AbstractPreferencePage.Accessor is created and given to addAccessor.  All loading and 
 * saving takes place automatically.
 * 
 * @author John Spackman
 *
 */
public abstract class AbstractPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	// This flag determines whether the store's contents will be dumped to stdout
	//	after loading and saving.  It's not meant to be dynamic, just an easy way
	//	to enable/disable it
	private static final boolean DUMP_STORE = false;
	
	/*
	 * Where FieldEditors cannot be used, an instance of Accessor is
	 * used to read and write the field to the store
	 */
	protected static interface Accessor {
		
		/**
		 * Loads the control with values from the store
		 */
		public void load();
		
		/**
		 * Saves the control to the store
		 */
		public void store();
		
		/**
		 * Loads the control with default values from the store
		 */
		public void loadDefaults();
	}

	/*
	 * Stubbed out Accessor
	 */
	protected static class AccessorAdapter implements Accessor {
		public void load() {
		}

		public void loadDefaults() {
		}

		public void store() {
		}
	}

	/*
	 * Accessor with ID and control
	 */
	protected static abstract class AbstractAccessor implements Accessor {
		// Preference ID
		protected String id;
		
		// Control
		protected Control control;
		
		/**
		 * Constructor
		 * @param id preference ID represented the control
		 * @param control the control
		 */
		public AbstractAccessor(String id, Control control) {
			super();
			this.id = id;
			this.control = control;
		}
	}
	
	/*
	 * Accessor for CheckBoxes
	 */
	protected class CheckBoxAccessor extends AbstractAccessor {

		public CheckBoxAccessor(String id, Control control) {
			super(id, control);
		}

		public void load() {
			((Button)control).setSelection(getPreferenceStore().getBoolean(id));
		}

		public void loadDefaults() {
			((Button)control).setSelection(getPreferenceStore().getDefaultBoolean(id));
		}

		public void store() {
			getPreferenceStore().setValue(id, ((Button)control).getSelection());
		}
	}

	// List of Accessors
	private List<Accessor> accessors = new LinkedList<Accessor>();
	
	// Common construction
	{
		setPreferenceStore(SQLExplorerPlugin.getDefault().getPreferenceStore());
	}
	
	public AbstractPreferencePage(int style) {
		super(style);
	}

	public AbstractPreferencePage(String title, ImageDescriptor image, int style) {
		super(title, image, style);
	}

	public AbstractPreferencePage(String title, int style) {
		super(title, style);
	}

	/**
	 * Called internally to add an Accessor
	 * @param accessor
	 */
	protected void addAccessor(Accessor accessor) {
		accessors.add(accessor);
		accessor.load();
	}
	
	/* (non-JavaDoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		// Nothing
	}

	/* (non-JavaDoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		Control result = super.createContents(parent);
		if (DUMP_STORE)
			dumpStore(getPreferenceStore());
        return result;
	}

	/* (non-JavaDoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		super.performDefaults();
		for (Iterator<Accessor> iter = accessors.iterator(); iter.hasNext();) {
			Accessor accessor = (Accessor)iter.next();
			accessor.loadDefaults();
		}
	}

	/* (non-JavaDoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	public boolean performOk() {
		super.performOk();
		for (Iterator<Accessor> iter = accessors.iterator(); iter.hasNext();) {
			Accessor accessor = (Accessor)iter.next();
			accessor.store();
		}
		if (DUMP_STORE)
			dumpStore(getPreferenceStore());
		return true;
	}
	
	/**
	 * Dumps all values from the store to stdout; uses reflection to get a list of
	 * all field names from IConstants, and then outputs them one by one
	 * @param store
	 */
	protected void dumpStore(IPreferenceStore store) {
		try {
			final int STATIC_REQUIRED_MODIFIERS = Modifier.STATIC | Modifier.PUBLIC;

			// Use reflection to get all fields in IConstants, and then iterate through them
			Field[] fields = IConstants.class.getFields();
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				
				// Field must be static and public, and be a string
				if ((field.getModifiers() & STATIC_REQUIRED_MODIFIERS) == STATIC_REQUIRED_MODIFIERS && field.getType().isAssignableFrom(String.class)) {
					
					// Get it
					String id = (String)field.get(null);
					String value = store.getString(id);
					System.out.println(id + ": " + value);
				}
			}
		}catch(Exception e) {
			SQLExplorerPlugin.error("Cannot dump store", e);
		}
	}
}
