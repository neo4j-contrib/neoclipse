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
package org.neo4j.neoclipse.reltype;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.RelationshipType;

/**
 * A HashMap<RelationshipType,T> implementation that will work correctly
 * together with any RelationshipType implementation. Note: all RelationshipType
 * items returned from this class are instances of DynamicRelationshipType.
 * @author Anders Nawroth
 */
public class RelationshipTypeHashMap<T> implements Map<RelationshipType,T>
{
    private final Map<String,T> map;

    public RelationshipTypeHashMap()
    {
        map = new HashMap<String,T>();
    }

    @Override
    public void clear()
    {
        map.clear();
    }

    @Override
    public boolean containsKey( final Object key )
    {
        if ( !(key instanceof RelationshipType) )
        {
            return false;
        }
        return map.containsKey( ((RelationshipType) key).name() );
    }

    @Override
    public boolean containsValue( final Object value )
    {
        return map.containsValue( value );
    }

    @Override
    public Set<Entry<RelationshipType,T>> entrySet()
    {
        Set<Entry<RelationshipType,T>> entrySet = new HashSet<Entry<RelationshipType,T>>();
        for ( Entry<String,T> entry : map.entrySet() )
        {
            entrySet.add( new EntryImpl( DynamicRelationshipType
                .withName( entry.getKey() ), entry.getValue() ) );
        }
        return entrySet;
    }

    class EntryImpl implements Entry<RelationshipType,T>
    {
        private final RelationshipType key;
        private final T value;

        EntryImpl( final RelationshipType key, final T value )
        {
            this.key = key;
            this.value = value;
        }

        @Override
        public RelationshipType getKey()
        {
            return key;
        }

        @Override
        public T getValue()
        {
            return value;
        }

        @Override
        public T setValue( final T value )
        {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public T get( final Object key )
    {
        if ( !(key instanceof RelationshipType) )
        {
            return null;
        }
        return map.get( ((RelationshipType) key).name() );
    }

    @Override
    public boolean isEmpty()
    {
        return map.isEmpty();
    }

    @Override
    public Set<RelationshipType> keySet()
    {
        Set<RelationshipType> keySet = new RelationshipTypeHashSet();
        for ( String name : map.keySet() )
        {
            keySet.add( DynamicRelationshipType.withName( name ) );
        }
        return keySet;
    }

    @Override
    public T put( final RelationshipType key, final T value )
    {
        return map.put( key.name(), value );
    }

    @Override
    public void putAll( final Map<? extends RelationshipType,? extends T> t )
    {
        for ( Entry<? extends RelationshipType,? extends T> entry : t
            .entrySet() )
        {
            map.put( entry.getKey().name(), entry.getValue() );
        }
    }

    @Override
    public T remove( final Object key )
    {
        if ( !(key instanceof RelationshipType) )
        {
            return null;
        }
        return map.remove( ((RelationshipType) key).name() );
    }

    @Override
    public int size()
    {
        return map.size();
    }

    @Override
    public Collection<T> values()
    {
        return map.values();
    }
}
