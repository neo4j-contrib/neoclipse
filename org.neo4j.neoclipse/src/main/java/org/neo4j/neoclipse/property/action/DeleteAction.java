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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.IPropertySheetEntry;
import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.PropertyContainer;
import org.neo4j.api.core.Transaction;
import org.neo4j.neoclipse.NeoIcons;
import org.neo4j.neoclipse.property.NeoPropertySheetPage;

/**
 * Action to delete a property from a PropertyContainer.
 * @author Anders Nawroth
 */
public class DeleteAction extends PropertyAction
{
    public DeleteAction( final Composite parent,
        final NeoPropertySheetPage propertySheet )
    {
        super( "Remove", NeoIcons.DELETE.getDescriptor(), parent, propertySheet );
    }

    @Override
    protected void performOperation( PropertyContainer container,
        IPropertySheetEntry entry )
    {
        String key = entry.getDisplayName();
        NeoService ns = getNeoService();
        if ( ns == null )
        {
            return;
        }
        boolean confirmation = MessageDialog.openQuestion( null,
            "Confirm delete",
            "Are you sure you want to delete the selected property?" );
        if ( !confirmation )
        {
            return;
        }
        Transaction tx = ns.beginTx();
        try
        {
            container.removeProperty( key );
            tx.success();
        }
        catch ( Exception e )
        {
            MessageDialog.openError( null, "Error", "Error in Neo service: "
                + e.getMessage() );
        }
        finally
        {
            tx.finish();
        }
        propertySheet.fireChangeEvent( container, key );
        propertySheet.refresh();
    }
}
