package org.neo4j.neoclipse.editor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class CypherResultSet implements Serializable
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final List<Map<String, Object>> iterator;
    private final List<String> columns;
    private final String message;

    public CypherResultSet( List<Map<String, Object>> resultsList, List<String> columns, String message )
    {
        this.iterator = resultsList;
        this.columns = columns;
        this.message = message;
    }

    public List<Map<String, Object>> getIterator()
    {
        return iterator;
    }

    public List<String> getColumns()
    {
        return columns;
    }

    public String getMessage()
    {
        return message;
    }

}
