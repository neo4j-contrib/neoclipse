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
 * 
 * @author	Peter H&auml;nsgen
 */
public class NeoRelationshipPropertySource implements IPropertySource
{
    private static final String RELATIONSHIP_CATEGORY   = "Relationship";
    private static final String PROPERTIES_CATEGORY     = "Properties";
    
    private static final String RELATIONSHIP_ID         = "Id";
    private static final String RELATIONSHIP_TYPE       = "Type";
    
    /**
     * The relationship.
     */
    private Relationship rs;

    /**
     * The constructor.
     */
    public NeoRelationshipPropertySource(Relationship rs)
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
            descs.add(new NeoPropertyDescriptor(RELATIONSHIP_ID, RELATIONSHIP_ID, RELATIONSHIP_CATEGORY));
            descs.add(new NeoPropertyDescriptor(RELATIONSHIP_TYPE, RELATIONSHIP_TYPE, RELATIONSHIP_CATEGORY));
            
            // custom properties for relationships
            Iterable<String> keys = rs.getPropertyKeys();
            for (String key : keys)
            {
                descs.add(new NeoPropertyDescriptor(key, key, PROPERTIES_CATEGORY));
            }

            return descs.toArray(new IPropertyDescriptor[descs.size()]);
        }
        finally
        {
            txn.finish();
        }
    }

    /**
     * Returns the value of the given property.
     */
    public Object getPropertyValue(Object id)
    {
        Transaction txn = Transaction.begin();

        try
        {
            if (id == RELATIONSHIP_ID)
            {
                return String.valueOf(rs.getId());
            }
            else if (id == RELATIONSHIP_TYPE)
            {
                return String.valueOf(rs.getType().name());
            }
            else
            {
                return rs.getProperty((String) id);
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
    public boolean isPropertySet(Object id)
    {
        Transaction txn = Transaction.begin();

        try
        {
            if (id == RELATIONSHIP_ID)
            {
                return true;
            }
            else if (id == RELATIONSHIP_TYPE)
            {
                return true;
            }
            else
            {
                return rs.hasProperty((String) id);
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
    public void resetPropertyValue(Object id)
    {
    }

    /**
     * Does nothing.
     */
    public void setPropertyValue(Object id, Object value)
    {
    }
}
