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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.neo4j.api.core.NeoService;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.neo.NeoServiceManager;

/**
 * Base class for actions on context menus.
 * @author Anders Nawroth
 */
abstract public class BaseContextAction extends Action
{
    public BaseContextAction( final String name, final ImageDescriptor image )
    {
        super( name, image );
    }

    /**
     * Get the current NeoService. Returns <code>null</code> on failure, after
     * showing appropriate error messages.
     * @return current neo service
     */
    public NeoService getNeoService()
    {
        NeoServiceManager sm = Activator.getDefault().getNeoServiceManager();
        NeoService ns = sm.getNeoService();
        if ( ns == null )
        {
            MessageDialog.openError( null, "Error",
                "The Neo service is not available." );
            return null;
        }
        return ns;
    }
}
