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
package org.neo4j.neoclipse.property.action;

import java.io.IOException;

import org.neo4j.neoclipse.property.PropertyTransform;

public class ClipboardUtil
{
    private static final char KEY_SEP = ';'; // '\t';
    private static final char CLS_SEP = ':';
    private Class<?> cls;
    private String key;
    private Object value;

    public ClipboardUtil( final Class<?> cls, final String key,
            final Object value )
    {
        this.cls = cls;
        this.key = key;
        this.value = value;
    }

    public ClipboardUtil( final String content )
    {
        int keyPos = content.indexOf( CLS_SEP );
        if ( keyPos == -1 )
        {
            clearFields();
            return;
        }
        String type = content.substring( 0, keyPos );
        try
        {
            cls = Class.forName( type );
        }
        catch ( ClassNotFoundException e )
        {
            e.printStackTrace();
            clearFields();
            return;
        }
        int contentPos = content.indexOf( KEY_SEP, keyPos );
        if ( contentPos == -1 || contentPos == content.length() - 1 )
        {
            clearFields();
            return;
        }
        key = content.substring( keyPos + 1, contentPos );
        String representation = content.substring( contentPos + 1 );
        try
        {
            value = PropertyTransform.getHandler( cls ).parse( representation );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
            clearFields();
            return;
        }
    }

    private void clearFields()
    {
        cls = null;
        key = null;
        value = null;
    }

    public String getRepresentation()
    {
        String type = cls.getName();
        String representation = PropertyTransform.getHandler( cls ).render(
                value );
        return type + CLS_SEP + key + KEY_SEP + representation;
    }

    public String getKey()
    {
        return key;
    }

    public Object getValue()
    {
        return value;
    }

    @Override
    public String toString()
    {
        String res = "type: " + cls + "\n";
        res += "key: " + key + "\n";
        res += "value: " + value + "\n";
        return res;
    }
}
