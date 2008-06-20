/*
 * NeoRelationshipPropertySource.java
 */
package org.neo4j.neoclipse.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.Transaction;

/**
 * Resolves the properties for a Neo relationship.
 * @author Peter H&auml;nsgen
 */
public class NeoRelationshipPropertySource implements IPropertySource
{
    private static final String RELATIONSHIP_CATEGORY = "Relationship";
    private static final String PROPERTIES_CATEGORY = "Properties";
    private static final String RELATIONSHIP_ID = "Id";
    private static final String RELATIONSHIP_TYPE = "Type";
    /**
     * The relationship.
     */
    private Relationship rs;

    /**
     * The constructor.
     */
    public NeoRelationshipPropertySource( Relationship rs )
    {
        this.rs = rs;
    }

    public Object getEditableValue()
    {
        return null;
    }

    /**
     * Returns the descriptors for the properties of the relationship.
     */
    public IPropertyDescriptor[] getPropertyDescriptors()
    {
        Transaction txn = Transaction.begin();
        try
        {
            List<IPropertyDescriptor> descs = new ArrayList<IPropertyDescriptor>();
            // standard properties for relationships
            descs.add( new NeoPropertyDescriptor( RELATIONSHIP_ID,
                RELATIONSHIP_ID, RELATIONSHIP_CATEGORY ) );
            descs.add( new NeoPropertyDescriptor( RELATIONSHIP_TYPE,
                RELATIONSHIP_TYPE, RELATIONSHIP_CATEGORY ) );
            // custom properties for relationships
            Iterable<String> keys = rs.getPropertyKeys();
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
     * Returns the value of the given property.
     */
    public Object getPropertyValue( Object id )
    {
        Transaction txn = Transaction.begin();
        try
        {
            if ( id == RELATIONSHIP_ID )
            {
                return String.valueOf( rs.getId() );
            }
            else if ( id == RELATIONSHIP_TYPE )
            {
                return String.valueOf( rs.getType().name() );
            }
            else
            {
                return String.valueOf( rs.getProperty( (String) id ) );
            }
        }
        finally
        {
            txn.finish();
        }
    }

    /**
     * Checks if the property is set.
     */
    public boolean isPropertySet( Object id )
    {
        Transaction txn = Transaction.begin();
        try
        {
            if ( id == RELATIONSHIP_ID )
            {
                return true;
            }
            else if ( id == RELATIONSHIP_TYPE )
            {
                return true;
            }
            else
            {
                return rs.hasProperty( (String) id );
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
     * Sets value.
     */
    public void setPropertyValue( Object id, Object value )
    {
        Transaction tx = Transaction.begin();
        try
        {
            if ( rs.hasProperty( (String) id ) )
            {
                // try to keep the same type as the previous value
                Class<?> c = rs.getProperty( (String) id ).getClass();
                if ( c.equals( Integer.class ) )
                {
                    rs.setProperty( (String) id, Integer
                        .parseInt( (String) value ) );
                }
                else if ( c.equals( Double.class ) )
                {
                    rs.setProperty( (String) id, Double
                        .parseDouble( (String) value ) );
                }
                else if ( c.equals( Float.class ) )
                {
                    rs.setProperty( (String) id, Float
                        .parseFloat( (String) value ) );
                }
                else if ( c.equals( Boolean.class ) )
                {
                    rs.setProperty( (String) id, Boolean
                        .parseBoolean( (String) value ) );
                }
                else if ( c.equals( Byte.class ) )
                {
                    rs.setProperty( (String) id, Byte
                        .parseByte( (String) value ) );
                }
                else if ( c.equals( Short.class ) )
                {
                    rs.setProperty( (String) id, Short
                        .parseShort( (String) value ) );
                }
                else if ( c.equals( Long.class ) )
                {
                    rs.setProperty( (String) id, Long
                        .parseLong( (String) value ) );
                }
                else if ( c.equals( Character.class ) )
                {
                    String s = (String) value;
                    if ( s.length() > 0 )
                    {
                        rs.setProperty( (String) id, ((String) value)
                            .charAt( 0 ) );
                    }
                    // else we can't set the char property, or? TODO?
                }
                else
                {
                    rs.setProperty( (String) id, value );
                }
            }
            else
            {
                rs.setProperty( (String) id, value );
            }
            tx.success();
        }
        finally
        {
            tx.finish();
        }
    }
}
