/**
 * Licensed to Neo Technology under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Neo Technology licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.neo4j.neoclipse.property.action;

import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.IPropertySheetEntry;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.action.Actions;
import org.neo4j.neoclipse.decorate.SimpleGraphDecorator;
import org.neo4j.neoclipse.preference.DecoratorPreferences;
import org.neo4j.neoclipse.property.NeoPropertySheetPage;

/**
 * Action to delete a property from a PropertyContainer.
 * 
 * @author Anders Nawroth
 */
public class AddNodeLabelAction extends PropertyAction
{
    public AddNodeLabelAction( final Composite parent,
            final NeoPropertySheetPage propertySheet )
    {
        super( Actions.ADD_NODE_LABEL, parent, propertySheet );
    }

    @Override
    protected void performOperation( final PropertyContainer container,
            final IPropertySheetEntry entry )
    {
        String key = entry.getDisplayName();
        IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
        String propNameSetting = preferenceStore.getString( DecoratorPreferences.NODE_PROPERTY_NAMES );
        List<String> propNames = SimpleGraphDecorator.Settings.listFromString( propNameSetting );
        for ( String name : propNames )
        {
            if ( key.equals( name ) )
            {
                return;
            }
        }
        if ( propNameSetting.trim().length() > 0 )
        {
            propNameSetting += ", ";
        }
        propNameSetting += key;
        preferenceStore.setValue( DecoratorPreferences.NODE_PROPERTY_NAMES,
                propNameSetting );
        propertySheet.fireChangeEvent( container, null, true );
    }
}
