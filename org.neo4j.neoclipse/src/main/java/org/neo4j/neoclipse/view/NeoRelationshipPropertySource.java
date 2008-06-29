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
package org.neo4j.neoclipse.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.neo4j.api.core.Relationship;

/**
 * Resolves the properties for a Neo relationship.
 * @author Peter H&auml;nsgen
 */
public class NeoRelationshipPropertySource extends NeoPropertySource
{
    private static final String RELATIONSHIP_CATEGORY = "Relationship";
    private static final String RELATIONSHIP_ID = "Id";
    private static final String RELATIONSHIP_TYPE = "Type";

    public NeoRelationshipPropertySource( Relationship rs )
    {
        super( rs );
    }

    @Override
    protected List<IPropertyDescriptor> getHeadPropertyDescriptors()
    {
        List<IPropertyDescriptor> descs = new ArrayList<IPropertyDescriptor>();
        descs.add( new NeoPropertyDescriptor( RELATIONSHIP_ID, RELATIONSHIP_ID,
            RELATIONSHIP_CATEGORY ) );
        descs.add( new NeoPropertyDescriptor( RELATIONSHIP_TYPE,
            RELATIONSHIP_TYPE, RELATIONSHIP_CATEGORY ) );
        return descs;
    }

    @Override
    protected Object getValue( Object id )
    {
        if ( id == RELATIONSHIP_ID )
        {
            return String.valueOf( ((Relationship) container).getId() );
        }
        else if ( id == RELATIONSHIP_TYPE )
        {
            return String.valueOf( ((Relationship) container).getType().name() );
        }
        else
        {
            return super.getValue( id );
        }
    }

    @Override
    protected boolean isSet( Object id )
    {
        if ( id == RELATIONSHIP_ID )
        {
            return true;
        }
        else if ( id == RELATIONSHIP_TYPE )
        {
            return true;
        }
        else
        {
            return super.isSet( id );
        }
    }
}
