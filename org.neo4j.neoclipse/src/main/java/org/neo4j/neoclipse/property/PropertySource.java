/*
 * Licensed to "Neo Technology," Network Engine for Objects in Lund AB
 * (http://neotechnology.com) under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership. Neo Technology licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License
 * at (http://www.apache.org/licenses/LICENSE-2.0). Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.neo4j.neoclipse.property;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.neo4j.api.core.PropertyContainer;
import org.neo4j.api.core.Transaction;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.property.PropertyTransform.PropertyHandler;

/**
 * Common property handling for nodes and relationships.
 * @author Peter H&auml;nsgen
 * @author Anders Nawroth
 */
public class PropertySource implements IPropertySource
{
    protected static final String PROPERTIES_CATEGORY = "Properties";
    /**
     * The container of the properties (either Relationship or Node).
     */
    protected PropertyContainer container;

    /**
     * The constructor.
     */
    public PropertySource( PropertyContainer container )
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
        Transaction tx = Activator.getDefault().beginNeoTx();
        try
        {
            List<IPropertyDescriptor> descs = new ArrayList<IPropertyDescriptor>();
            descs.addAll( getHeadPropertyDescriptors() );
            Iterable<String> keys = container.getPropertyKeys();
            for ( String key : keys )
            {
                Object value = container.getProperty( (String) key );
                Class<?> c = value.getClass();
                descs.add( new PropertyDescriptor( key, key,
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
        return null;
    }

    /**
     * Returns the value of the given property.
     */
    public Object getPropertyValue( Object id )
    {
        Transaction tx = Activator.getDefault().beginNeoTx();
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
        PropertyHandler propertyHandler = PropertyTransform
            .getPropertyHandler( value.getClass() );
        if ( propertyHandler != null )
        {
            return propertyHandler.render( value );
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
        Transaction tx = Activator.getDefault().beginNeoTx();
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
     * Performs the real testing of if a property is set.
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
     * Sets property value.
     */
    public void setPropertyValue( Object id, Object value )
    {
        Transaction tx = Activator.getDefault().beginNeoTx();
        try
        {
            if ( container.hasProperty( (String) id ) )
            {
                // try to keep the same type as the previous value
                Class<?> c = container.getProperty( (String) id ).getClass();
                PropertyHandler propertyHandler = PropertyTransform
                    .getPropertyHandler( c );
                if ( propertyHandler != null )
                {
                    try
                    {
                        Object o = propertyHandler.parse( value );
                        if ( o != null )
                        {
                            container.setProperty( (String) id, o );
                            tx.success();
                        }
                    }
                    catch ( Exception e )
                    {
                        // TODO: handle exception
                        //e.printStackTrace();
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
