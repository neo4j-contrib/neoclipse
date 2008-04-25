/*
 * NeoGraphPropertySourceProvider.java
 */
package org.neo4j.neoclipse.view;

import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.IPropertySourceProvider;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;

/**
 * Resolves the properties for Neo nodes and relationships.
 * 
 * @author Peter H&auml;nsgen
 */
public class NeoGraphPropertySourceProvider implements IPropertySourceProvider
{
    public IPropertySource getPropertySource(Object source)
    {
        if (source instanceof Node)
        {
            return new NeoNodePropertySource((Node) source);
        }
        else if (source instanceof Relationship)
        {
            return new NeoRelationshipPropertySource((Relationship) source);
        }
        else
        {
            return null;
        }
    }
}
