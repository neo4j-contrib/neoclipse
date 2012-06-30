/**
 * Licensed to Neo Technology under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Neo Technology licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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




}
