/*
 * NeoPropertyDescriptor.java
 */
package org.neo4j.neoclipse.view;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

/**
 * Describes a single property of a Neo node or relationship.
 * 
 * @author	Peter H&auml;nsgen
 */
public class NeoPropertyDescriptor implements IPropertyDescriptor
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
     * The constructor.
     */
    public NeoPropertyDescriptor(Object key, String name, String category)
    {
        this.key = key;
        this.name = name;
        this.category = category;
    }

    public CellEditor createPropertyEditor(Composite parent)
    {
        return null;
    }

    public String getCategory()
    {
        return category;
    }

    public String getDescription()
    {
        return "The property with the name '" + key + "'.";
    }

    public String getDisplayName()
    {
        return name;
    }

    public String[] getFilterFlags()
    {
        return null;
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
        return null;
    }

    public boolean isCompatibleWith(IPropertyDescriptor anotherProperty)
    {
        return false;
    }
}
