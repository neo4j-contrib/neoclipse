package examples.roles;

import org.neo4j.api.core.RelationshipType;

/**
 * Role relationship types.
 * @author Anders Nawroth
 */
public enum RoleRels implements RelationshipType
{
    CONTAINS, ROOT;
}
