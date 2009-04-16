/*
 * Licensed to "Neo Technology," Network Engine for Objects in Lund AB
 * (http://neotechnology.com) under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership. Neo Technology licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License
 * at (http://www.apache.org/licenses/LICENSE-2.0). Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.neo4j.neoclipse.preference;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.neo4j.neoclipse.Activator;

/**
 * Initializes neo preferences with their default values.
 * @author Peter H&auml;nsgen
 * @author Anders Nawroth
 */
public class NeoDecoratorPreferenceInitializer extends
    AbstractPreferenceInitializer
{
    /**
     * Initializes the neo preferences.
     */
    public void initializeDefaultPreferences()
    {
        IPreferenceStore pref = Activator.getDefault().getPreferenceStore();
        pref.setDefault( NeoDecoratorPreferences.NODE_PROPERTY_NAMES, "" );
        pref.setDefault( NeoDecoratorPreferences.RELATIONSHIP_PROPERTY_NAMES,
            "" );
        pref.setDefault( NeoDecoratorPreferences.NODE_ICON_LOCATION, "" );
        pref.setDefault( NeoDecoratorPreferences.NODE_ICON_PROPERTY_NAMES, "" );
        // view menu settings
        pref.setDefault( NeoDecoratorPreferences.SHOW_RELATIONSHIP_TYPES, true );
        pref.setDefault( NeoDecoratorPreferences.SHOW_NODE_COLORS, true );
        pref.setDefault( NeoDecoratorPreferences.SHOW_NODE_NAMES, true );
        pref.setDefault( NeoDecoratorPreferences.SHOW_RELATIONSHIP_NAMES, true );
        pref.setDefault( NeoDecoratorPreferences.SHOW_RELATIONSHIP_PROPERTIES,
            false );
        pref
            .setDefault( NeoDecoratorPreferences.SHOW_RELATIONSHIP_COLORS, true );
        pref.setDefault( NeoDecoratorPreferences.SHOW_RELATIONSHIP_IDS, false );
        pref.setDefault( NeoDecoratorPreferences.SHOW_ARROWS, true );
        pref.setDefault( NeoDecoratorPreferences.SHOW_NODE_PROPERTIES, false );
        pref.setDefault( NeoDecoratorPreferences.SHOW_NODE_ICONS, true );
        pref.setDefault( NeoDecoratorPreferences.SHOW_NODE_IDS, false );

    }
}
