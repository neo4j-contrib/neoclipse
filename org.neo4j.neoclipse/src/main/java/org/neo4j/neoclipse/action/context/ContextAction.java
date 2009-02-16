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
package org.neo4j.neoclipse.action.context;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.neo4j.api.core.PropertyContainer;
import org.neo4j.api.core.Transaction;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.view.NeoGraphViewPart;

abstract public class ContextAction extends BaseContextAction
{
    protected NeoGraphViewPart graphView;

    public ContextAction( String name, ImageDescriptor image,
        NeoGraphViewPart neoGraphViewPart )
    {
        super( name, image );
        this.graphView = neoGraphViewPart;
    }

    /**
     * Get the PropertyContainer of the current graph view. Returns
     * <code>null</code> on failure, after showing appropriate error messages.
     * @return selected property container
     */
    protected PropertyContainer getPropertyContainer()
    {
        ISelection selected = graphView.getViewer().getSelection();
        if ( !(selected instanceof IStructuredSelection) )
        {
            MessageDialog.openError( null, "Error",
                "Unknown error in graph view selection type." );
            return null;
        }
        IStructuredSelection parSs = (IStructuredSelection) selected;
        Object parFirstElement = parSs.getFirstElement();
        if ( !(parFirstElement instanceof PropertyContainer) )
        {
            MessageDialog.openError( null, "Error",
                "The graph view item is not a Node or Relationship." );
            return null;
        }
        return (PropertyContainer) parFirstElement;
    }

    /**
     * Get a new transaction from the current NeoService. Returns
     * <code>null</code> on failure, after showing appropriate error messages.
     * @return current neo service
     */
    protected Transaction getNeoTx()
    {
        Transaction tx = null;
        try
        {
            tx = Activator.getDefault().beginNeoTx();
        }
        catch ( Exception e )
        {
            tx = null;
        }
        if ( tx == null )
        {
            MessageDialog.openError( null, "Error",
                "The Neo service is not available." );
            return null;
        }
        return tx;
    }
}