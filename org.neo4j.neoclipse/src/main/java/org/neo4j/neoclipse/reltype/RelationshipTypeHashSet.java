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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.neo4j.api.core.DynamicRelationshipType;
import org.neo4j.api.core.RelationshipType;

/**
 * A HashSet<RelationshipType> implementation that will work correctly together
 * with any RelationshipType implementation. Note: all RelationshipType items
 * returned from this class are instances of DynamicRelationshipType.
 * @author Anders Nawroth
 */
public class RelationshipTypeHashSet implements Set<RelationshipType>
{
    private final Set<String> set;

    public RelationshipTypeHashSet()
    {
        set = new HashSet<String>();
    }

    public boolean add( final RelationshipType relType )
    {
        return set.add( relType.name() );
    }

    public boolean addAll( final Collection<? extends RelationshipType> relTypes )
    {
        boolean modified = false;
        for ( RelationshipType relType : relTypes )
        {
            modified |= set.add( relType.name() );
        }
        return modified;
    }

    public void clear()
    {
        set.clear();
    }

    public boolean contains( final Object relType )
    {
        if ( !(relType instanceof RelationshipType) )
        {
            return false;
        }
        return set.contains( ((RelationshipType) relType).name() );
    }

    public boolean containsAll( final Collection<?> relTypes )
    {
        for ( Object relType : relTypes )
        {
            if ( !set.contains( relType ) )
            {
                return false;
            }
        }
        return true;
    }

    public boolean isEmpty()
    {
        return set.isEmpty();
    }

    public Iterator<RelationshipType> iterator()
    {
        return new Iterator<RelationshipType>()
        {
            private final Iterator<String> iter = RelationshipTypeHashSet.this.set
                .iterator();

            public boolean hasNext()
            {
                return iter.hasNext();
            }

            public RelationshipType next()
            {
                return DynamicRelationshipType.withName( iter.next() );
            }

            public void remove()
            {
                iter.remove();
            }
        };
    }

    public boolean remove( final Object relType )
    {
        if ( !(relType instanceof RelationshipType) )
        {
            return false;
        }
        return set.remove( ((RelationshipType) relType).name() );
    }

    public boolean removeAll( final Collection<?> relTypes )
    {
        boolean modified = false;
        for ( Object relType : relTypes )
        {
            if ( relType instanceof RelationshipType )
            {
                modified |= set.remove( ((RelationshipType) relType).name() );
            }
        }
        return modified;
    }

    public boolean retainAll( final Collection<?> relTypes )
    {
        throw new UnsupportedOperationException();
    }

    public int size()
    {
        return set.size();
    }

    public Object[] toArray()
    {
        List<RelationshipType> list = new ArrayList<RelationshipType>( size() );
        for ( String name : set )
        {
            list.add( DynamicRelationshipType.withName( name ) );
        }
        return list.toArray();
    }

    public <T> T[] toArray( final T[] a )
    {
        List<RelationshipType> list = new ArrayList<RelationshipType>( size() );
        for ( String name : set )
        {
            list.add( DynamicRelationshipType.withName( name ) );
        }
        return list.toArray( a );
    }
}
