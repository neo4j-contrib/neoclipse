package org.neo4j.neoclipse.editor;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.neoclipse.util.ApplicationUtil;

public class NodeWrapper extends BaseWrapper
{

    private static final long serialVersionUID = 1L;


    private List<RelationshipWrapper> relation = new ArrayList<RelationshipWrapper>();

    public NodeWrapper()
    {
    }


    public NodeWrapper( long id )
    {
        super( id );
    }

    public List<RelationshipWrapper> getRelation()
    {
        return relation;
    }


    public void setRelation( List<RelationshipWrapper> relation )
    {
        this.relation = relation;
    }

    public void addRelation( RelationshipWrapper relation )
    {
        this.relation.add( relation );
    }


    @Override
    public String toString()
    {
        return ApplicationUtil.toJson( this );
    }

}
