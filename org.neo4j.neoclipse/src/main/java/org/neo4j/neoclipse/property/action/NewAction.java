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
package org.neo4j.neoclipse.property.action;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Composite;
import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.PropertyContainer;
import org.neo4j.api.core.Transaction;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.NeoIcons;
import org.neo4j.neoclipse.neo.NeoServiceManager;
import org.neo4j.neoclipse.property.NeoPropertySheetPage;

/**
 * Action to add a new property to a PropertyContainer.
 * @author Anders Nawroth
 */
public class NewAction extends PropertyAction
{
    private Object value;

    public NewAction( final Composite parent,
        final NeoPropertySheetPage propertySheet, final Object defaultValue )
    {
        super( defaultValue.getClass().getSimpleName(), NeoIcons.NEW
            .getDescriptor(), parent, propertySheet );
        this.value = defaultValue;
    }

    @Override
    public void run()
    {
        PropertyContainer propertyContainer = getPropertyContainer();
        if ( propertyContainer == null )
        {
            return;
        }
        addProperty( propertyContainer );
    }

    /**
     * @param entry
     * @param parFirstElement
     */
    private void addProperty( PropertyContainer container )
    {
        NeoServiceManager sm = Activator.getDefault().getNeoServiceManager();
        NeoService ns = sm.getNeoService();
        if ( ns != null )
        {
            InputDialog input = new InputDialog( null, "Key entry",
                "Please enter the key of the new property", null, null );
            if ( input.open() == OK && input.getReturnCode() == OK )
            {
                String key = null;
                Transaction tx = ns.beginTx();
                try
                {
                    key = input.getValue();
                    container.setProperty( key, value );
                    tx.success();
                }
                finally
                {
                    tx.finish();
                }
                propertySheet.fireChangeEvent( container, key );
                propertySheet.refresh();
            }
        }
    }
}
