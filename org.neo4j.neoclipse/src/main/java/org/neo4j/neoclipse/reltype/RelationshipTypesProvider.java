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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
    private boolean viewAll = true;
    private Map<String,RelationshipType> fakeTypes = new HashMap<String,RelationshipType>();

    public RelationshipTypesProvider()
    {
    }

    @SuppressWarnings( "deprecation" )
    public Object[] getElements( Object inputElement )
    {
        if ( viewAll )
        {
            Set<RelationshipType> relDirList = new HashSet<RelationshipType>();
            NeoService ns = Activator.getDefault().getNeoServiceSafely();
            if ( ns == null )
            {
                // todo
                return new Object[0];
            }
            for ( RelationshipType relType : ((EmbeddedNeo) ns)
                .getRelationshipTypes() )
            {
                relDirList.add( relType );
                if ( fakeTypes.containsKey( relType.name() ) )
                {
                    fakeTypes.remove( relType.name() );
                }
            }
            relDirList.addAll( fakeTypes.values() );
            return relDirList.toArray();
        }
        else
        {
            Set<RelationshipType> relationshipTypes = NeoGraphLabelProviderWrapper
                .getInstance().getRelationshipTypes();
            relationshipTypes.addAll( fakeTypes.values() );
            return relationshipTypes.toArray();
        }
    }

    public void addFakeType( RelationshipType relType )
    {
        fakeTypes.put( relType.name(), relType );
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
