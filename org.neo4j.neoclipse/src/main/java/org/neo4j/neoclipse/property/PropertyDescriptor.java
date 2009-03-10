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

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.neo4j.neoclipse.property.PropertyTransform.PropertyHandler;

/**
 * Describes a single property of a Neo node or relationship.
 * @author Peter H&auml;nsgen
 * @author Anders Nawroth
 */
public class PropertyDescriptor implements IPropertyDescriptor
{
    /**
     * The key for identifying the value of the property.
     */
    private Object key;
    /**
     * The name of the property.
     */
    private String name;
    /**
     * The category of the property.
     */
    private String category;
    /**
     * Class of property content.
     */
    private Class<?> cls = null;
    private PropertyHandler propertyHandler;
    private final static ILabelProvider labelProvider = new PropertyLabelProvider();
    private final static ILabelProvider containerLabelProvider = new ContainerLabelProvider();
    /**
     * A constant, empty array, to be used instead of a null array.
     */
    private static final String[] EMPTY_ARRAY = new String[0];

    /**
     * Create a Neo property cell.
     * @param key
     *            the key of the property
     * @param name
     *            the name of the property
     * @param category
     *            the category of the property
     * @param allowEdit
     *            choose if this cell should be possible to edit
     */
    public PropertyDescriptor( Object key, String name, String category,
        Class<?> cls )
    {
        this.key = key;
        this.name = name;
        this.category = category;
        this.cls = cls;
        this.propertyHandler = PropertyTransform.getHandler( cls );
    }

    /**
     * Create a Neo property cell without editing capabilities. Use this for id
     * and relationship types "fake properties".
     * @param key
     *            the key of the property
     * @param name
     *            the name of the property
     * @param category
     *            the category of the property
     * @param allowEdit
     *            choose if this cell should be possible to edit
     */
    public PropertyDescriptor( Object key, String name, String category )
    {
        this.key = key;
        this.name = name;
        this.category = category;
    }

    public CellEditor createPropertyEditor( Composite parent )
    {
        if ( propertyHandler != null )
        {
            return propertyHandler.getEditor( parent );
        }
        return null;
    }

    public String getCategory()
    {
        return category;
    }

    public String getDescription()
    {
        if ( cls != null )
        {
            return "The property '" + key + "' is of type "
                + cls.getSimpleName();
        }
        return "";
    }

    public String getDisplayName()
    {
        return name;
    }

    public String[] getFilterFlags()
    {
        return EMPTY_ARRAY;
    }

    public Object getHelpContextIds()
    {
        return null;
    }

    public Object getId()
    {
        return key;
    }

    public ILabelProvider getLabelProvider()
    {
        if ( NodePropertySource.NODE_CATEGORY == category
            || RelationshipPropertySource.RELATIONSHIP_CATEGORY == category )
        {
            return containerLabelProvider;
        }
        return labelProvider;
    }

    public boolean isCompatibleWith( IPropertyDescriptor anotherProperty )
    {
        return false;
    }
}
