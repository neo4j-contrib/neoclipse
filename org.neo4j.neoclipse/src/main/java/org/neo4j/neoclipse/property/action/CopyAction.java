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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.IPropertySheetEntry;
import org.neo4j.api.core.PropertyContainer;
import org.neo4j.neoclipse.action.Actions;
import org.neo4j.neoclipse.property.NeoPropertySheetPage;

/**
 * Action to copy the text representation of a property value.
 * @author Anders Nawroth
 */
public class CopyAction extends PropertyAction
{
    private final Shell shell;
    private static final Transfer[] TRANSFER_TYPES = new Transfer[] { TextTransfer
        .getInstance() };

    public CopyAction( final Composite parent,
        final NeoPropertySheetPage propertySheet )
    {
        super( Actions.COPY, parent, propertySheet );
        shell = propertySheet.getControl().getShell();
    }

    protected void performOperation( PropertyContainer container,
        IPropertySheetEntry entry )
    {
        final String key = entry.getDisplayName();
        Object value = container.getProperty( key, null );
        if ( value == null )
        {
            MessageDialog.openError( shell, "Error",
                "Problem reading the value to copy." );
            return;
        }
        ClipboardUtil cu = new ClipboardUtil( value.getClass(), key, value );
        Object[] data = new Object[] { cu.getRepresentation() };
        try
        {
            Clipboard clipboard = new Clipboard( shell.getDisplay() );
            clipboard.setContents( data, TRANSFER_TYPES );
            clipboard.dispose();
            propertySheet.enablePaste();
        }
        catch ( SWTError e )
        {
            MessageDialog.openError( shell, "Error",
                "Could not copy the value to the clipboard." );
        }
    }

    public void selectionChanged( IStructuredSelection sel )
    {
        setEnabled( !sel.isEmpty() );
    }
}
