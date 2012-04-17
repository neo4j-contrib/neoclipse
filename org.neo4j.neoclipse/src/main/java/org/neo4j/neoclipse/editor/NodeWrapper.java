package org.neo4j.neoclipse.editor;

import java.util.LinkedList;
import java.util.List;

public class NodeWrapper extends BaseWrapper
{

    private static final long serialVersionUID = 1L;


    private List<RelationshipWrapper> relation = new LinkedList<RelationshipWrapper>();

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


    // @Override
    // public String toString()
    // {
    // try
    // {
    // return JSONObject.valueToString( this );
    // }
    // catch ( JSONException e )
    // {
    // throw new RuntimeException( e );
    // }
    // // StringBuilder sb = new StringBuilder( "node:{id:" + getId() );
    // // sb.append( ",relations:{" + ApplicationUtil.getPropertyValue(
    // // relation ) + "}" );
    // // if ( !getPropertyMap().isEmpty() )
    // // {
    // // sb.append( "," + ApplicationUtil.getPropertyValue( getPropertyMap() )
    // // );
    // // }
    // // return sb.toString() + "}";
    // }

}
