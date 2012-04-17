package org.neo4j.neoclipse.editor;



public class RelationshipWrapper extends BaseWrapper
{

    private static final long serialVersionUID = 1L;
    private long endNodeId;
    private String relationshipType;

    public RelationshipWrapper()
    {
    }

    public RelationshipWrapper( long id )
    {
        super( id );
    }


    public long getEndNodeId()
    {
        return endNodeId;
    }

    public void setEndNodeId( long endNode )
    {
        this.endNodeId = endNode;
    }

    public String getRelationshipType()
    {
        return relationshipType;
    }

    public void setRelationshipType( String relationshipType )
    {
        this.relationshipType = relationshipType;
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
    // // StringBuilder sb = new StringBuilder( "relation:{" );
    // // sb.append( "endNodeId:" + endNodeId );
    // // sb.append( ",relationship:" + relationshipType );
    // // if ( !getPropertyMap().isEmpty() )
    // // {
    // // sb.append( "," + ApplicationUtil.getPropertyValue( getPropertyMap() )
    // // );
    // // }
    // // return sb.toString() + "}";
    // }

}
