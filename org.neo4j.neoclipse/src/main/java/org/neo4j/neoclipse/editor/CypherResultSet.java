package org.neo4j.neoclipse.editor;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CypherResultSet implements Serializable
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final List<Map<String, Object>> iterator;
    private final Collection<String> columns;
    private final String message;

    public CypherResultSet( List<Map<String, Object>> resultsList, Collection<String> columns, String message )
    {
        this.iterator = resultsList;
        this.columns = columns;
        this.message = message;
    }

    public List<Map<String, Object>> getIterator()
    {
        return iterator;
    }

    public Collection<String> getColumns()
    {
        return columns;
    }

    public String getMessage()
    {
        return message;
    }

}
