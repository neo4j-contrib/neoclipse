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

import java.io.IOException;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Composite;
import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.PropertyContainer;
import org.neo4j.api.core.Transaction;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.property.NeoPropertySheetPage;
import org.neo4j.neoclipse.property.PropertyTransform.PropertyHandler;

/**
 * Action to add a new property to a PropertyContainer.
 * @author Anders Nawroth
 */
public class NewAction extends PropertyAction
{
    private final PropertyHandler propertyHandler;

    public NewAction( final Composite parent,
        final NeoPropertySheetPage propertySheet,
        final PropertyHandler propertyHandler )
    {
        super( propertyHandler.name(), propertyHandler.descriptor(), parent,
            propertySheet );
        this.propertyHandler = propertyHandler;
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
        NeoService ns = Activator.getDefault().getNeoServiceSafely();
        if ( ns == null )
        {
            return;
        }
        InputDialog keyInput = new InputDialog( null, "Key entry",
            "Please enter the key of the new property", null, null );
        if ( keyInput.open() != OK || keyInput.getReturnCode() != OK )
        {
            return;
        }
        String key = keyInput.getValue();
        Transaction tx = ns.beginTx();
        try
        {
            if ( container.hasProperty( key ) )
            {
                if ( !MessageDialog
                    .openQuestion(
                        null,
                        "Key exists",
                        "The key \""
                            + key
                            + "\" already exists, do you want to overwrite the old value?" ) )
                {
                    tx.finish();
                    return;
                }
            }
            InputDialog valueInput = new InputDialog( null, "Value entry",
                "Please enter the value of the new property", propertyHandler
                    .render( propertyHandler.value() ), propertyHandler
                    .getValidator() );
            if ( valueInput.open() != OK && valueInput.getReturnCode() != OK )
            {
                tx.finish();
                return;
            }
            Object val = null;
            try
            {
                val = propertyHandler.parse( valueInput.getValue() );
            }
            catch ( IOException e )
            {
                MessageDialog
                    .openError( null, "Error message",
                        "Error parsing the input value, no changes will be performed." );
                tx.finish();
                return;
            }
            container.setProperty( key, val );
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
