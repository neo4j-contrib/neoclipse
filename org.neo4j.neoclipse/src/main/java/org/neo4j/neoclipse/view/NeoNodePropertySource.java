/*
 * NeoNodePropertySource.java
 */
package org.neo4j.neoclipse.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Transaction;

/**
 * Resolves the properties for a Neo node.
 * @author Peter H&auml;nsgen
 */
public class NeoNodePropertySource implements IPropertySource
{
    private static final String NODE_CATEGORY = "Node";
    private static final String PROPERTIES_CATEGORY = "Properties";
    private static final String NODE_ID = "Id";
    /**
     * The node.
     */
    private Node node;

    /**
     * The constructor.
     */
    public NeoNodePropertySource( Node node )
    {
        this.node = node;
    }

    public Object getEditableValue()
    {
        return null;
    }

    /**
     * Returns the descriptors for the properties of the node.
     */
    public IPropertyDescriptor[] getPropertyDescriptors()
    {
        Transaction txn = Transaction.begin();
        try
        {
            List<IPropertyDescriptor> descs = new ArrayList<IPropertyDescriptor>();
            // standard properties for nodes
            descs.add( new NeoPropertyDescriptor( NODE_ID, NODE_ID,
                NODE_CATEGORY ) );
            // custom properties for nodes
            Iterable<String> keys = node.getPropertyKeys();
            for ( String key : keys )
            {
                descs.add( new NeoPropertyDescriptor( key, key,
                    PROPERTIES_CATEGORY, true ) );
            }
            return descs.toArray( new IPropertyDescriptor[descs.size()] );
        }
        finally
        {
            txn.finish();
        }
    }

    /**
     * Returns the value of the given property from the node.
     */
    public Object getPropertyValue( Object id )
    {
        Transaction txn = Transaction.begin();
        try
        {
            if ( id == NODE_ID )
            {
                return String.valueOf( node.getId() );
            }
            else
            {
                return String.valueOf( node.getProperty( (String) id ) );
            }
        }
        finally
        {
            txn.finish();
        }
    }

    /**
     * Checks if the node has a given property.
     */
    public boolean isPropertySet( Object id )
    {
        Transaction txn = Transaction.begin();
        try
        {
            if ( id == NODE_ID )
            {
                return true;
            }
            else
            {
                return node.hasProperty( (String) id );
            }
        }
        finally
        {
            txn.finish();
        }
    }

    /**
     * Does nothing.
     */
    public void resetPropertyValue( Object id )
    {
    }

    /**
     * Sets property.
     */
    public void setPropertyValue( Object id, Object value )
    {
        Transaction tx = Transaction.begin();
        try
        {
            if ( node.hasProperty( (String) id ) )
            {
                // try to keep the same type as the previous value
                Class<?> c = node.getProperty( (String) id ).getClass();
                if ( c.equals( Integer.class ) )
                {
                    node.setProperty( (String) id, Integer
                        .parseInt( (String) value ) );
                }
                else if ( c.equals( Double.class ) )
                {
                    node.setProperty( (String) id, Double
                        .parseDouble( (String) value ) );
                }
                else if ( c.equals( Float.class ) )
                {
                    node.setProperty( (String) id, Float
                        .parseFloat( (String) value ) );
                }
                else if ( c.equals( Boolean.class ) )
                {
                    node.setProperty( (String) id, Boolean
                        .parseBoolean( (String) value ) );
                }
                else if ( c.equals( Byte.class ) )
                {
                    node.setProperty( (String) id, Byte
                        .parseByte( (String) value ) );
                }
                else if ( c.equals( Short.class ) )
                {
                    node.setProperty( (String) id, Short
                        .parseShort( (String) value ) );
                }
                else if ( c.equals( Long.class ) )
                {
                    node.setProperty( (String) id, Long
                        .parseLong( (String) value ) );
                }
                else if ( c.equals( Character.class ) )
                {
                    String s = (String) value;
                    if ( s.length() > 0 )
                    {
                        node.setProperty( (String) id, ((String) value)
                            .charAt( 0 ) );
                    }
                    // else we can't set the char property, or? TODO?
                }
                else
                {
                    node.setProperty( (String) id, value );
                }
            }
            else
            {
                node.setProperty( (String) id, value );
            }
            tx.success();
        }
        finally
        {
            tx.finish();
        }
    }
}
