package examples.vehicleAssembly;

import org.neo4j.api.core.RelationshipType;

/**
 * @author Anders Nawroth
 */
public enum VehicleRels implements RelationshipType
{
    CONTAINS, VEHICLE, ROOT;
}
