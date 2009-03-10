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
package org.neo4j.neoclipse.property;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.neo4j.api.core.Node;

/**
 * Resolves the properties for a Neo node.
 * @author Peter H&auml;nsgen
 */
public class NodePropertySource extends PropertySource
{
    public static final String NODE_CATEGORY = "Node";
    private static final String NODE_ID = "Id";

    /**
     * The constructor.
     * @param propertySheet
     */
    public NodePropertySource( Node node, NeoPropertySheetPage propertySheet )
    {
        super( node, propertySheet );
    }

    @Override
    protected List<IPropertyDescriptor> getHeadPropertyDescriptors()
    {
        List<IPropertyDescriptor> descs = new ArrayList<IPropertyDescriptor>();
        // standard properties for nodes
        descs.add( new PropertyDescriptor( ID_KEY, NODE_ID, NODE_CATEGORY ) );
        return descs;
    }

    @Override
    protected Object getValue( Object id )
    {
        if ( id == ID_KEY )
        {
            return ((Node) container).getId();
        }
        else
        {
            return super.getValue( id );
        }
    }

    @Override
    protected boolean isSet( Object id )
    {
        if ( id == ID_KEY )
        {
            return true;
        }
        else
        {
            return super.isSet( id );
        }
    }
}
