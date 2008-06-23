package org.neo4j.neoclipse.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.neo4j.api.core.PropertyContainer;
import org.neo4j.api.core.Transaction;

public class NeoPropertySource implements IPropertySource
{
    protected static final String PROPERTIES_CATEGORY = "Properties";
    /**
     * The container of the properties (either Relationship or Node).
     */
    protected PropertyContainer container;

    /**
     * The constructor.
     */
    public NeoPropertySource( PropertyContainer container )
    {
        this.container = container;
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
        Transaction tx = Transaction.begin();
        try
        {
            List<IPropertyDescriptor> descs = new ArrayList<IPropertyDescriptor>();
            descs.addAll( getHeadPropertyDescriptors() );
            Iterable<String> keys = container.getPropertyKeys();
            for ( String key : keys )
            {
                descs.add( new NeoPropertyDescriptor( key, key,
                    PROPERTIES_CATEGORY, true ) );
            }
            return descs.toArray( new IPropertyDescriptor[descs.size()] );
        }
        finally
        {
            tx.finish();
        }
    }

    protected List<IPropertyDescriptor> getHeadPropertyDescriptors()
    {
        return new ArrayList<IPropertyDescriptor>();
    }

    /**
     * Returns the value of the given property.
     */
    public Object getPropertyValue( Object id )
    {
        Transaction tx = Transaction.begin();
        try
        {
            return getValue( id );
        }
        finally
        {
            tx.finish();
        }
    }

    /**
     * Performs the real getting of the property value.
     * @param id
     *            id of the property
     * @return value of the property
     */
    protected Object getValue( Object id )
    {
        return String.valueOf( container.getProperty( (String) id ) );
    }

    /**
     * Checks if the property is set.
     */
    public boolean isPropertySet( Object id )
    {
        Transaction tx = Transaction.begin();
        try
        {
            return isSet( id );
        }
        finally
        {
            tx.finish();
        }
    }

    /**
     * Performs the real testing if a property is set.
     * @param id
     *            id of the property
     * @return true if set
     */
    protected boolean isSet( Object id )
    {
        return container.hasProperty( (String) id );
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
            if ( container.hasProperty( (String) id ) )
            {
                // try to keep the same type as the previous value
                Class<?> c = container.getProperty( (String) id ).getClass();
                if ( c.equals( Integer.class ) )
                {
                    container.setProperty( (String) id, Integer
                        .parseInt( (String) value ) );
                }
                else if ( c.equals( Double.class ) )
                {
                    container.setProperty( (String) id, Double
                        .parseDouble( (String) value ) );
                }
                else if ( c.equals( Float.class ) )
                {
                    container.setProperty( (String) id, Float
                        .parseFloat( (String) value ) );
                }
                else if ( c.equals( Boolean.class ) )
                {
                    container.setProperty( (String) id, Boolean
                        .parseBoolean( (String) value ) );
                }
                else if ( c.equals( Byte.class ) )
                {
                    container.setProperty( (String) id, Byte
                        .parseByte( (String) value ) );
                }
                else if ( c.equals( Short.class ) )
                {
                    container.setProperty( (String) id, Short
                        .parseShort( (String) value ) );
                }
                else if ( c.equals( Long.class ) )
                {
                    container.setProperty( (String) id, Long
                        .parseLong( (String) value ) );
                }
                else if ( c.equals( Character.class ) )
                {
                    String s = (String) value;
                    if ( s.length() > 0 )
                    {
                        container.setProperty( (String) id, ((String) value)
                            .charAt( 0 ) );
                    }
                    // else we can't set the char property, or? TODO?
                }
                else
                {
                    container.setProperty( (String) id, value );
                }
            }
            else
            {
                container.setProperty( (String) id, value );
            }
            tx.success();
        }
        finally
        {
            tx.finish();
        }
    }
}
