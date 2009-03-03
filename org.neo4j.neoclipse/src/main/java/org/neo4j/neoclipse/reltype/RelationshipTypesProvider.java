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
package org.neo4j.neoclipse.reltype;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.neo4j.api.core.EmbeddedNeo;
import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.RelationshipType;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.view.NeoGraphLabelProviderWrapper;

public class RelationshipTypesProvider implements IContentProvider,
    IStructuredContentProvider
{

    private static class RelationshipTypeImpl implements RelationshipType
    {
        // TODO: this is a nice hack for now, but depends on the
        // impl in EmbeddedNeo. (the code is from there)
        private String name;

        RelationshipTypeImpl( String name )
        {
            assert name != null;
            this.name = name;
        }

        public String name()
        {
            return name;
        }

        public String toString()
        {
            return name;
        }

        public boolean equals( Object o )
        {
            if ( !(o instanceof RelationshipType) )
            {
                return false;
            }
            return name.equals( ((RelationshipType) o).name() );
        }

        public int hashCode()
        {
            return name.hashCode();
        }
    }

    private boolean viewAll = true;
    private Set<RelationshipType> fakeTypes = new HashSet<RelationshipType>();

    public RelationshipTypesProvider()
    {
    }

    public Object[] getElements( Object inputElement )
    {
        Set<RelationshipType> relationshipTypes;
        if ( viewAll )
        {
            relationshipTypes = new HashSet<RelationshipType>();
            NeoService ns = Activator.getDefault().getNeoServiceSafely();
            if ( ns == null )
            {
                // todo
                return new Object[0];
            }
            @SuppressWarnings( "deprecation" )
            Iterable<RelationshipType> relationshipTypesIterable = ((EmbeddedNeo) ns)
                .getRelationshipTypes();
            for ( RelationshipType relType : relationshipTypesIterable )
            {
                relationshipTypes.add( relType );
            }
            relationshipTypes.addAll( fakeTypes );
        }
        else
        {
            relationshipTypes = NeoGraphLabelProviderWrapper.getInstance()
                .getRelationshipTypes();
            relationshipTypes.addAll( fakeTypes );
        }
        List<RelationshipTypeControl> list = new ArrayList<RelationshipTypeControl>();
        for ( RelationshipType relType : relationshipTypes )
        {
            list.add( new RelationshipTypeControl( relType ) );
        }
        return list.toArray();
    }

    public void addFakeType( final String name )
    {
        RelationshipType relType = new RelationshipTypeImpl( name );
        fakeTypes.add( relType );
    }

    public void dispose()
    {
    }

    public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
    {
    }

    public void setViewAll()
    {
        viewAll = true;
    }

    public void setViewTraversed()
    {
        viewAll = false;
    }
}
