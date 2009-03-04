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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.neo4j.api.core.PropertyContainer;
import org.neo4j.neoclipse.view.NeoGraphViewPart;

abstract public class AbstractContextAction extends AbstractBaseContextAction
{
    protected NeoGraphViewPart graphView;

    public AbstractContextAction( final String name,
        final ImageDescriptor image, final NeoGraphViewPart neoGraphViewPart )
    {
        super( name, image );
        this.graphView = neoGraphViewPart;
    }

    @Override
    public void run()
    {
        List<PropertyContainer> propertyContainers = getPropertyContainers();
        if ( propertyContainers == null )
        {
            return;
        }
        performOperation( propertyContainers );
    }

    /**
     * Perform an operation that requires only property containers. Is called by
     * the default <code>run()</code> implementation.
     * @param containers
     * @param key
     */
    protected void performOperation( final List<PropertyContainer> containers )
    {
        throw new UnsupportedOperationException();
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
     * Get the PropertyContainers of the current graph view. Returns
     * <code>null</code> on failure, after showing appropriate error messages.
     * @return selected property containers
     */
    protected List<PropertyContainer> getPropertyContainers()
    {
        ISelection selected = graphView.getViewer().getSelection();
        if ( !(selected instanceof IStructuredSelection) )
        {
            MessageDialog.openError( null, "Error",
                "Unknown error in graph view selection type." );
            return null;
        }
        IStructuredSelection parSs = (IStructuredSelection) selected;
        List<PropertyContainer> items = new ArrayList<PropertyContainer>();
        Iterator<?> iter = parSs.iterator();
        while ( iter.hasNext() )
        {
            Object o = iter.next();
            if ( o instanceof PropertyContainer )
            {
                items.add( (PropertyContainer) o );
            }
        }
        if ( items.isEmpty() )
        {
            MessageDialog.openError( null, "Error",
                "No nodes or relationships are selected." );
            return null;
        }
        return items;
    }
}