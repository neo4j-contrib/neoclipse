package org.neo4j.neoclipse.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.neo4j.api.core.EmbeddedNeo;
import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.RelationshipType;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.neo.NeoServiceManager;

public class RelationshipTypesProvider implements IContentProvider,
    IStructuredContentProvider
{

    private List<RelationshipType> relDirList;

    @SuppressWarnings( "deprecation" )
    public RelationshipTypesProvider()
    {
        NeoServiceManager sm = Activator.getDefault().getNeoServiceManager();
        NeoService ns = sm.getNeoService();
        if ( ns == null )
        {
            // todo
            return;
        }
        relDirList = new ArrayList<RelationshipType>();
        for ( RelationshipType relType : ((EmbeddedNeo) ns)
            .getRelationshipTypes() )
        {
            relDirList.add( relType );
        }

    }

    public Object[] getElements( Object inputElement )
    {
        return relDirList.toArray();
    }

    public void dispose()
    {
    }

    public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
    {
    }

}
