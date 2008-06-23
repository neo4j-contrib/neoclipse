package org.neo4j.neoclipse.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.neo4j.api.core.PropertyContainer;
import org.neo4j.api.core.Transaction;

@SuppressWarnings( "serial" )
public class NeoPropertySource implements IPropertySource
{
    protected static final String PROPERTIES_CATEGORY = "Properties";
    /**
     * The container of the properties (either Relationship or Node).
     */
    protected PropertyContainer container;

    protected interface Transformer
    {
        Object transform( Object o );
    }

    protected static final Map<Class<?>,Transformer> parserMap = new HashMap<Class<?>,Transformer>()
    {
        {
            put( Integer.class, new Transformer()
            {
                public Object transform( Object o )
                {
                    return Integer.parseInt( (String) o );
                }
            } );
            put( Double.class, new Transformer()
            {
                public Object transform( Object o )
                {
                    return Double.parseDouble( (String) o );
                }
            } );
            put( Float.class, new Transformer()
            {
                public Object transform( Object o )
                {
                    return Float.parseFloat( (String) o );
                }
            } );
            put( Boolean.class, new Transformer()
            {
                public Object transform( Object o )
                {
                    return Boolean.parseBoolean( (String) o );
                }
            } );
            put( Byte.class, new Transformer()
            {
                public Object transform( Object o )
                {
                    return Byte.parseByte( (String) o );
                }
            } );
            put( Short.class, new Transformer()
            {
                public Object transform( Object o )
                {
                    return Short.parseShort( (String) o );
                }
            } );
            put( Long.class, new Transformer()
            {
                public Object transform( Object o )
                {
                    return Long.parseLong( (String) o );
                }
            } );
            put( Character.class, new Transformer()
            {
                public Object transform( Object o )
                {
                    String s = (String) o;
                    if ( s.length() > 0 )
                    {
                        return ((String) o).charAt( 0 );
                    }
                    return null;
                }
            } );
        }
    };

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
        Object value = container.getProperty( (String) id );
        if ( value.getClass().isArray() )
        {
            if ( value instanceof int[] )
            {
                return Arrays.toString( (int[]) value );
            }
            return value.getClass().getComponentType();
        }
        else
        {
            return String.valueOf( value );
        }
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
                Transformer transformer = parserMap.get( c );
                if ( transformer != null )
                {
                    Object o = transformer.transform( value );
                    if ( o != null )
                    {
                        container.setProperty( (String) id, o );
                    }
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
