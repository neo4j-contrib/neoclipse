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

    public String toString()
    {
        String res = "type: " + cls + "\n";
        res += "key: " + key + "\n";
        res += "value: " + value + "\n";
        return res;
    }
}