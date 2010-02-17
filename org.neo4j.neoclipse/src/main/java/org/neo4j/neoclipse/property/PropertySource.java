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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.neoclipse.property.PropertyTransform.PropertyHandler;

/**
 * Common property handling for nodes and relationships.
 * @author Peter H&auml;nsgen
 * @author Anders Nawroth
 */
public class PropertySource implements IPropertySource
{
    public static final String ID_KEY = "neoclipse.id";
    protected static final String PROPERTIES_CATEGORY = "Properties";
    /**
     * The container of the properties (either Relationship or Node).
     */
    protected PropertyContainer container;
    protected NeoPropertySheetPage propertySheet;

    /**
     * The constructor.
     * @param propertySheet
     */
    public PropertySource( final PropertyContainer container,
        final NeoPropertySheetPage propertySheet )
    {
        this.container = container;
        this.propertySheet = propertySheet;
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
        List<IPropertyDescriptor> descs = new ArrayList<IPropertyDescriptor>();
        descs.addAll( getHeadPropertyDescriptors() );
        Iterable<String> keys = container.getPropertyKeys();
        for ( String key : keys )
        {
            Object value = container.getProperty( key );
            Class<?> c = value.getClass();
            descs
                .add( new PropertyDescriptor( key, key, PROPERTIES_CATEGORY, c ) );
        }
        return descs.toArray( new IPropertyDescriptor[descs.size()] );
    }

    protected List<IPropertyDescriptor> getHeadPropertyDescriptors()
    {
        return null;
    }

    /**
     * Returns the value of the given property.
     */
    public Object getPropertyValue( final Object id )
    {
        return getValue( id );
    }

    /**
     * Performs the real getting of the property value.
     * @param id
     *            id of the property
     * @return value of the property
     */
    protected Object getValue( final Object id )
    {
        return container.getProperty( (String) id );
    }

    /**
     * Checks if the property is set.
     */
    public boolean isPropertySet( final Object id )
    {
        return isSet( id );
    }

    /**
     * Performs the real testing of if a property is set.
     * @param id
     *            id of the property
     * @return true if set
     */
    protected boolean isSet( final Object id )
    {
        return container.hasProperty( (String) id );
    }

    /**
     * Does nothing.
     */
    public void resetPropertyValue( final Object id )
    {
    }

    /**
     * Sets property value.
     */
    public void setPropertyValue( final Object id, final Object value )
    {
        if ( container.hasProperty( (String) id ) )
        {
            // try to keep the same type as the previous value
            Class<?> c = container.getProperty( (String) id ).getClass();
            PropertyHandler propertyHandler = PropertyTransform.getHandler( c );
            if ( propertyHandler == null )
            {
                MessageDialog.openError( null, "Error",
                    "No property handler was found for type "
                        + c.getSimpleName() + "." );
                return;
            }
            Object o = null;
            try
            {
                o = propertyHandler.parse( value );
            }
            catch ( Exception e )
            {
                MessageDialog.openError( null, "Error",
                    "Could not parse the input as type " + c.getSimpleName()
                        + "." );
                return;
            }
            if ( o == null )
            {
                MessageDialog.openError( null, "Error",
                    "Input parsing resulted in null value." );
                return;
            }
            try
            {
                container.setProperty( (String) id, o );
            }
            catch ( Exception e )
            {
                MessageDialog.openError( null, "Error",
                    "Error in Neo service: " + e.getMessage() );
            }
        }
        else
        {
            // simply set the value
            try
            {
                container.setProperty( (String) id, value );
            }
            catch ( Exception e )
            {
                MessageDialog.openError( null, "Error",
                    "Error in Neo service: " + e.getMessage() );
            }
        }
        propertySheet.fireChangeEvent( container, (String) id );
    }
}
