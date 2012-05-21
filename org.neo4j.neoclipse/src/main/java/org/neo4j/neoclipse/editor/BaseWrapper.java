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
