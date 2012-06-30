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
package org.neo4j.neoclipse.editor;

import java.io.Serializable;
import java.util.Map;

public abstract class BaseWrapper implements Serializable
{
    private static final long serialVersionUID = 1L;

    private long id;
    private Map<String, Object> propertyMap;

    public BaseWrapper()
    {
    }
    public BaseWrapper( long id )
    {
        super();
        this.id = id;
    }


    public Map<String, Object> getPropertyMap()
    {

        return propertyMap;
    }

    public void setPropertyMap( Map<String, Object> propertyMap )
    {
        this.propertyMap = propertyMap;
    }

    public Long getId()
    {
        return id;
    }

    public void setId( long id )
    {
        this.id = id;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) ( id ^ ( id >>> 32 ) );
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj == null )
        {
            return false;
        }
        if ( getClass() != obj.getClass() )
        {
            return false;
        }
        BaseWrapper other = (BaseWrapper) obj;
        if ( id != other.id )
        {
            return false;
        }
        return true;
    }



}
