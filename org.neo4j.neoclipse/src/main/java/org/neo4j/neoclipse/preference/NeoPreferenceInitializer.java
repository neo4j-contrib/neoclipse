/*
 * NeoPreferenceInitializer.java
 */
package org.neo4j.neoclipse.preference;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.neo4j.neoclipse.Activator;

/**
 * Initializes neo preferences with their default values.
 * 
 * @author	Peter H&auml;nsgen
 */
public class NeoPreferenceInitializer extends AbstractPreferenceInitializer
{
    /**
     * Initializes the neo preferences.
     */
    public void initializeDefaultPreferences()
    {
        IPreferenceStore pref = Activator.getDefault().getPreferenceStore();
        
        pref.setDefault(NeoPreferences.DATABASE_LOCATION, "");
        pref.setDefault(NeoPreferences.DEFAULT_PROPERTY_NAME, "NAME");
    }
}
