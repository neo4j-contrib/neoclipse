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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.IPropertySheetEntry;
import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.PropertyContainer;
import org.neo4j.api.core.Transaction;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.NeoIcons;
import org.neo4j.neoclipse.neo.NeoServiceManager;
import org.neo4j.neoclipse.property.NeoPropertySheetPage;

public class DeleteAction extends Action
{
    private Composite parent;
    private NeoPropertySheetPage propertySheet;

    public DeleteAction( final Composite parent,
        final NeoPropertySheetPage propertySheet )
    {
        super( "Remove", NeoIcons.getDescriptor( NeoIcons.DELETE ) );
        this.parent = parent;
        this.propertySheet = propertySheet;
    }

    @Override
    public void run()
    {
        ISelection selection = propertySheet.getSelection();
        if ( selection.isEmpty() )
        {
            return;
        }

        if ( selection instanceof IStructuredSelection )
        {
            IStructuredSelection ss = (IStructuredSelection) selection;
            Object firstElement = ss.getFirstElement();
            if ( firstElement instanceof IPropertySheetEntry )
            {
                IPropertySheetEntry entry = (IPropertySheetEntry) firstElement;
                if ( entry.getEditor( parent ) == null )
                {
                    return;
                }
                ISelection parSel = propertySheet.getNeoGraphViewPart()
                    .getViewer().getSelection();
                if ( parSel instanceof IStructuredSelection )
                {
                    IStructuredSelection parSs = (IStructuredSelection) parSel;
                    Object parFirstElement = parSs.getFirstElement();
                    if ( parFirstElement instanceof PropertyContainer )
                    {
                        removeProperty( (PropertyContainer) parFirstElement,
                            entry.getDisplayName() );
                    }
                }
            }
        }
    }

    /**
     * @param entry
     * @param parFirstElement
     */
    private void removeProperty( PropertyContainer container, String key )
    {
        NeoServiceManager sm = Activator.getDefault().getNeoServiceManager();
        NeoService ns = sm.getNeoService();
        if ( ns != null )
        {
            Transaction tx = ns.beginTx();
            try
            {
                container.removeProperty( key );
                tx.success();
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
