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

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.Icons;
import org.neo4j.neoclipse.graphdb.GraphDbServiceManager;

/**
 * Label provider for database structure outline.
 * 
 * @author Radhakrishna kalyan
 */
public class ConnectionTreeLabelProvider extends LabelProvider
{

    @Override
    public void dispose()
    {
        super.dispose();
    }

    /**
     * Return the image used for the given node.
     * 
     * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
     */
    @Override
    public Image getImage( Object element )
    {
        if ( element instanceof Alias )
        {
            Alias alias = (Alias) element;
            GraphDbServiceManager graphDbServiceManager = Activator.getDefault().getGraphDbServiceManager();
            if ( graphDbServiceManager.isRunning() && graphDbServiceManager.getCurrentAlias().equals( alias ) )
            {
                return Icons.NEW_ALIAS_ENABLED.image();

            }

        }
        return Icons.NEW_ALIAS_DISABLED.image();
    }

    /**
     * Return the text to display
     * 
     * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
     */
    @Override
    public String getText( Object element )
    {
        if ( element instanceof Alias )
        {
            Alias alias = (Alias) element;

            String label = alias.getName();

            return label + "( " + alias.getUri() + " )";

        }

        return null;
    }
}
