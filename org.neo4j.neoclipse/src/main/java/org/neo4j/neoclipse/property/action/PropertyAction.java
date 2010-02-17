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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.IPropertySheetEntry;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.neoclipse.action.AbstractBaseAction;
import org.neo4j.neoclipse.action.Actions;
import org.neo4j.neoclipse.property.NeoPropertySheetPage;

/**
 * Base class for actions on properties.
 * @author Anders Nawroth
 */
abstract public class PropertyAction extends AbstractBaseAction
{
    protected static final int OK = 0;
    protected final Composite parent;
    protected final NeoPropertySheetPage propertySheet;

    public PropertyAction( final Actions action, final Composite parent,
        final NeoPropertySheetPage propertySheet )
    {
        super( action );
        this.parent = parent;
        this.propertySheet = propertySheet;
    }

    public PropertyAction( final String name, final ImageDescriptor image,
        final Composite parent, final NeoPropertySheetPage propertySheet )
    {
        super( name, image );
        this.parent = parent;
        this.propertySheet = propertySheet;
    }

    @Override
    public void run()
    {
        IPropertySheetEntry entry = getPropertySheetEntry();
        if ( entry == null )
        {
            return;
        }
        PropertyContainer propertyContainer = getPropertyContainer();
        if ( propertyContainer == null )
        {
            return;
        }
        performOperation( propertyContainer, entry );
    }

    /**
     * Perform an operation that requires only the property container and the
     * property entry. Is called by the default <code>run()</code>
     * implementation.
     * @param container
     * @param key
     */
    protected void performOperation( final PropertyContainer container,
        final IPropertySheetEntry entry )
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the PropertyContainer of the current property sheet. Returns
     * <code>null</code> on failure, after showing appropriate error messages.
     * @return selected property container
     */
    protected PropertyContainer getPropertyContainer()
    {
        PropertyContainer container = propertySheet.getPropertyContainer();
        if ( container == null )
        {
            MessageDialog.openError( null, "Error",
                "Selected item is not a Node or Relationship." );
            return null;
        }
        return container;
    }

    /**
     * Get the current selected editable property sheet entry, if available.
     * Returns <code>null</code> on failure, after showing appropriate error
     * messages.
     * @return selected property sheet entry
     */
    protected IPropertySheetEntry getPropertySheetEntry()
    {
        ISelection selection = propertySheet.getSelection();
        if ( selection.isEmpty() )
        {
            MessageDialog.openError( null, "Error", "Nothing is selected." );
            return null;
        }
        if ( !(selection instanceof IStructuredSelection) )
        {
            MessageDialog.openError( null, "Error", "Error in selection type." );
            return null;
        }
        IStructuredSelection ss = (IStructuredSelection) selection;
        Object firstElement = ss.getFirstElement();
        if ( !(firstElement instanceof IPropertySheetEntry) )
        {
            MessageDialog.openError( null, "Error",
                "The selection is not in a property sheet." );
            return null;
        }
        IPropertySheetEntry entry = (IPropertySheetEntry) firstElement;
        if ( entry.getEditor( parent ) == null )
        {
            MessageDialog.openError( null, "Error",
                "This item can not be changed." );
            return null;
        }
        return entry;
    }
}
