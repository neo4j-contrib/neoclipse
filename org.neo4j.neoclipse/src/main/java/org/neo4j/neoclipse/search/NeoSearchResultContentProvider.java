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
package org.neo4j.neoclipse.search;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.neo4j.graphdb.PropertyContainer;

/**
 * This is the content provider for populating the result list tree viewer.
 * 
 * @author Peter H&auml;nsgen
 */
public class NeoSearchResultContentProvider implements ITreeContentProvider
{
    private final List<PropertyContainer> list = new ArrayList<PropertyContainer>();

    /**
     * Called when the input has changed, does nothing.
     */
    @Override
    public void inputChanged( final Viewer viewer, final Object oldInput,
            final Object newInput )
    {
        // does nothing
    }

    /**
     * Expects the results of a neo search.
     */
    @Override
    public Object[] getElements( final Object inputElement )
    {
        NeoSearchResult result = (NeoSearchResult) inputElement;
        Iterable<PropertyContainer> matches = result.getMatches();
        // TODO make search results being added to the list
        // and the UI updated during the search.
        list.clear();
        // the actual search are not performed until
        // this iterator is performed.
        // so this should be run with frequent
        // updates of the UI somehow.
        for ( PropertyContainer entity : matches )
        {
            list.add( entity );
        }
        return list.toArray();
    }

    /**
     * Returns an empty array, as there is no hierarchical structure.
     */
    @Override
    public Object[] getChildren( final Object parentElement )
    {
        return new Object[0];
    }

    /**
     * Returns null, as there is no hierarchical structure.
     */
    @Override
    public Object getParent( final Object element )
    {
        return null;
    }

    /**
     * Returns false, no hierarchical views supported.
     */
    @Override
    public boolean hasChildren( final Object element )
    {
        return false;
    }

    @Override
    public void dispose()
    {
        // nothing here
    }
}
