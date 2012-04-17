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

    public long getId()
    {
        return id;
    }

    public void setId( long id )
    {
        this.id = id;
    }


}
