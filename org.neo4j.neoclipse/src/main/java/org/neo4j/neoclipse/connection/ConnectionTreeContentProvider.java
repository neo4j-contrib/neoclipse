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
package org.neo4j.neoclipse.connection;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.neo4j.neoclipse.Activator;

/**
 * Content provider for database structure outline.
 * 
 * @author Radhakrishna Kalyan
 */
public class ConnectionTreeContentProvider implements ITreeContentProvider
{

    @Override
    public void dispose()
    {
        // noop
    }

    @Override
    public Object[] getChildren( Object parentElement )
    {
        if ( parentElement instanceof AliasManager )
        {
            AliasManager aliases = (AliasManager) parentElement;
            Object[] children = aliases.getAliases().toArray();
            return children;
        }

        return null;
    }

    @Override
    public Object[] getElements( Object inputElement )
    {
        return getChildren( inputElement );
    }

    @Override
    public Object getParent( Object element )
    {
        if ( element instanceof AliasManager )
        {
            return null;
        }
        else if ( element instanceof Alias )
        {
            return Activator.getDefault().getAliasManager();
        }

        return null;
    }

    @Override
    public boolean hasChildren( Object element )
    {
        Object[] tmp = getChildren( element );
        return tmp != null && tmp.length != 0;
    }

    @Override
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
    {
        // noop
    }

}
