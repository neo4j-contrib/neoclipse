package org.neo4j.neoclipse.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.neo4j.api.core.PropertyContainer;
import org.neo4j.api.core.Transaction;
import org.neo4j.neoclipse.view.NeoPropertyTransform.Parser;
import org.neo4j.neoclipse.view.NeoPropertyTransform.Renderer;

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
                Object value = container.getProperty( (String) key );
                Class<?> c = value.getClass();
                descs.add( new NeoPropertyDescriptor( key, key,
                    PROPERTIES_CATEGORY, c ) );
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
        Renderer renderer = NeoPropertyTransform.rendererMap.get( value
            .getClass() );
        if ( renderer != null )
        {
            return renderer.transform( value );
        }
        else
        {
            return "(no rendering available)";
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
                Parser parser = NeoPropertyTransform.parserMap.get( c );
                if ( parser != null )
                {
                    try
                    {
                        Object o = parser.transform( value );
                        if ( o != null )
                        {
                            container.setProperty( (String) id, o );
                            tx.success();
                        }
                    }
                    catch ( Exception e )
                    {
                        // TODO: handle exception
                        e.printStackTrace();
                    }
                }
            }
            else
            {
                container.setProperty( (String) id, value );
                tx.success();
            }
        }
        finally
        {
            tx.finish();
        }
    }
}
