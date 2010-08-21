package org.neo4j.neoclipse.reltype;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.RelationshipType;

public interface DirectedRelationship
{

    /**
     * Get the relationship type in this wrapper.
     * 
     * @return
     */
    RelationshipType getRelType();

    /**
     * Get direction filter for this relationship type.
     * 
     * @return
     */
    Direction getDirection();

    /**
     * Tell if a relationship type is active.
     * 
     * @return
     */
    boolean hasDirection();
}
