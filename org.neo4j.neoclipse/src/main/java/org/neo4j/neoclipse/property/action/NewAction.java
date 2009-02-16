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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.PropertyContainer;
import org.neo4j.api.core.Transaction;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.NeoIcons;
import org.neo4j.neoclipse.neo.NeoServiceManager;
import org.neo4j.neoclipse.property.NeoPropertySheetPage;

public class NewAction extends Action
{
    private static final int OK = 0;
    private NeoPropertySheetPage propertySheet;
    private Object value;

    public NewAction( final NeoPropertySheetPage propertySheet,
        Object defaultValue )
    {
        super( defaultValue.getClass().getSimpleName(), NeoIcons
            .getDescriptor( NeoIcons.NEW ) );
        this.propertySheet = propertySheet;
        this.value = defaultValue;
    }

    @Override
    public void run()
    {
        ISelection parSel = propertySheet.getNeoGraphViewPart().getViewer()
            .getSelection();
        if ( parSel instanceof IStructuredSelection )
        {
            IStructuredSelection parSs = (IStructuredSelection) parSel;
            Object parFirstElement = parSs.getFirstElement();
            if ( parFirstElement instanceof PropertyContainer )
            {
                addProperty( (PropertyContainer) parFirstElement );
            }
        }
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
                Transaction tx = ns.beginTx();
                try
                {
                    // add property here
                    tx.success();
                    container.setProperty( input.getValue(), value );
                    propertySheet.refresh();
                    propertySheet.getNeoGraphViewPart().refreshPreserveLayout();
                }
                finally
                {
                    tx.finish();
                }
            }
        }
    }
}
