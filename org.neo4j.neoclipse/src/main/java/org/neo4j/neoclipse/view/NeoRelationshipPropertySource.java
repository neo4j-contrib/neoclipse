/*
 * NeoRelationshipPropertySource.java
 */
package org.neo4j.neoclipse.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.neo4j.api.core.Relationship;

/**
 * Resolves the properties for a Neo relationship.
 * @author Peter H&auml;nsgen
 */
public class NeoRelationshipPropertySource extends NeoPropertySource
{
    private static final String RELATIONSHIP_CATEGORY = "Relationship";
    private static final String RELATIONSHIP_ID = "Id";
    private static final String RELATIONSHIP_TYPE = "Type";

    public NeoRelationshipPropertySource( Relationship rs )
    {
        super( rs );
    }

    @Override
    protected List<IPropertyDescriptor> getHeadPropertyDescriptors()
    {
        List<IPropertyDescriptor> descs = new ArrayList<IPropertyDescriptor>();
        descs.add( new NeoPropertyDescriptor( RELATIONSHIP_ID, RELATIONSHIP_ID,
            RELATIONSHIP_CATEGORY ) );
        descs.add( new NeoPropertyDescriptor( RELATIONSHIP_TYPE,
            RELATIONSHIP_TYPE, RELATIONSHIP_CATEGORY ) );
        return descs;
    }

    @Override
    protected Object getValue( Object id )
    {
        if ( id == RELATIONSHIP_ID )
        {
            return String.valueOf( ((Relationship) container).getId() );
        }
        else if ( id == RELATIONSHIP_TYPE )
        {
            return String.valueOf( ((Relationship) container).getType().name() );
        }
        else
        {
            return super.getValue( id );
        }
    }

    @Override
    protected boolean isSet( Object id )
    {
        if ( id == RELATIONSHIP_ID )
        {
            return true;
        }
        else if ( id == RELATIONSHIP_TYPE )
        {
            return true;
        }
        else
        {
            return super.isSet( id );
        }
    }
}
