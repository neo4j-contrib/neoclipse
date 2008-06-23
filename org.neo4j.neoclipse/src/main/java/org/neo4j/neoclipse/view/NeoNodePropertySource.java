/*
 * NeoNodePropertySource.java
 */
package org.neo4j.neoclipse.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.neo4j.api.core.Node;

/**
 * Resolves the properties for a Neo node.
 * @author Peter H&auml;nsgen
 */
public class NeoNodePropertySource extends NeoPropertySource
{
    private static final String NODE_CATEGORY = "Node";
    private static final String NODE_ID = "Id";

    /**
     * The constructor.
     */
    public NeoNodePropertySource( Node node )
    {
        super( node );
    }

    @Override
    protected List<IPropertyDescriptor> getHeadPropertyDescriptors()
    {
        List<IPropertyDescriptor> descs = new ArrayList<IPropertyDescriptor>();
        // standard properties for nodes
        descs
            .add( new NeoPropertyDescriptor( NODE_ID, NODE_ID, NODE_CATEGORY ) );
        return descs;
    }

    @Override
    protected Object getValue( Object id )
    {
        if ( id == NODE_ID )
        {
            return String.valueOf( ((Node) container).getId() );
        }
        else
        {
            return super.getValue( id );
        }
    }

    @Override
    protected boolean isSet( Object id )
    {
        if ( id == NODE_ID )
        {
            return true;
        }
        else
        {
            return super.isSet( id );
        }
    }
}
