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
package org.neo4j.neoclipse.util;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.neoclipse.editor.NodeWrapper;
import org.neo4j.neoclipse.editor.RelationshipWrapper;

import com.google.gson.Gson;

public class ApplicationUtil
{

    public static final String NEOCLIPSE_SETTINGS_DIR = System.getProperty( "user.home" ) + File.separator
                                                        + ".neoclipse";

    private static final Gson gson = new Gson();

    public static String toJson( Object object )
    {
        return gson.toJson( object );
    }

    public static <T> T toJson( String json, Class<T> clazz )
    {
        return gson.fromJson( json, clazz );
    }

    public static File dirInWorkspace( final String... elements )
    {
        String path = NEOCLIPSE_SETTINGS_DIR;
        for ( String element : elements )
        {
            path += File.separator + element;
        }
        File dir = new File( path );
        if ( !dir.exists() && !dir.mkdirs() )
        {
            throw new IllegalArgumentException( "Could not create directory: " + dir );
        }
        return dir;
    }

    public static String returnEmptyIfBlank( String pString )
    {
        return pString == null ? "" : pString;
    }

    public static boolean isBlank( String string )
    {
        return ( string == null || string.trim().isEmpty() );
    }

    public static NodeWrapper extractToNodeWrapper( Node node, boolean includeRelation )
    {

        NodeWrapper nodeWrapper = new NodeWrapper( node.getId() );
        
        Map<String, Object> oMap = extractToMapFromProperties( node );
        nodeWrapper.setPropertyMap( oMap );
        if ( node.hasRelationship() && includeRelation )
        {
            for ( Relationship relationship : node.getRelationships( Direction.OUTGOING ) )
            {
                RelationshipWrapper rw = new RelationshipWrapper( relationship.getId() );
                relationship.getType().name();
                rw.setEndNodeId( relationship.getEndNode().getId()  );
                rw.setPropertyMap( extractToMapFromProperties( relationship ) );
                rw.setRelationshipType( relationship.getType().name() );
                nodeWrapper.addRelation( rw );
            }
        }
        
        return nodeWrapper;
    }

    private static Map<String, Object> extractToMapFromProperties( PropertyContainer propertyContainer )
    {
        Map<String, Object> oMap = new LinkedHashMap<String, Object>();
        for ( String propertyName : propertyContainer.getPropertyKeys() )
        {
            boolean containsKey = oMap.containsKey( propertyName );
            if ( containsKey )
            {
                throw new IllegalArgumentException( "Duplicate propertyName : " + propertyName );
            }
            oMap.put( propertyName, propertyContainer.getProperty( propertyName ) );
        }
        return oMap;
    }

    @SuppressWarnings( "unchecked" )
    public static String getPropertyValue( Object value )
    {
        if ( value == null )
        {
            return "";
        }
        Class<? extends Object> valueClass = value.getClass();
        if ( valueClass.isPrimitive() )
        {
            return value.toString();
        }
        else if ( List.class.isAssignableFrom( valueClass ) )
        {
            StringBuilder sb = new StringBuilder();
            for ( Object obj : (List<?>) value )
            {
                sb.append( getPropertyValue( obj ) );
            }
            return sb.toString();
        }
        else if ( Map.class.isAssignableFrom( valueClass ) )
        {
            Map<String, Object> map = (Map<String, Object>) value;
            StringBuilder sb = new StringBuilder();
            for ( Entry<String, Object> entry : map.entrySet() )
            {
                sb.append( getPropertyValue( entry ) );
            }
            return sb.toString();
        }
        else if ( Entry.class.isAssignableFrom( valueClass ) )
        {
            Entry<Object, Object> entry = (Entry<Object, Object>) value;
            StringBuilder sb = new StringBuilder();
            sb.append( "," + entry.getKey() + ":" + getPropertyValue( entry.getValue() ) );
            return sb.toString();
        }
        else if ( valueClass.isArray() )
        {
            String stringValue = "null";

            if ( valueClass == byte[].class )
            {
                stringValue = Arrays.toString( (byte[]) value );
            }
            else if ( valueClass == short[].class )
            {
                stringValue = Arrays.toString( (short[]) value );
            }
            else if ( valueClass == int[].class )
            {
                stringValue = Arrays.toString( (int[]) value );
            }
            else if ( valueClass == long[].class )
            {
                stringValue = Arrays.toString( (long[]) value );
            }
            else if ( valueClass == char[].class )
            {
                stringValue = Arrays.toString( (char[]) value );
            }
            else if ( valueClass == float[].class )
            {
                stringValue = Arrays.toString( (float[]) value );
            }
            else if ( valueClass == double[].class )
            {
                stringValue = Arrays.toString( (double[]) value );
            }
            else if ( valueClass == boolean[].class )
            {
                stringValue = Arrays.toString( (boolean[]) value );
            }
            return stringValue;
        }
        return toJson( value );
    }

}
