package examples.vehicleAssembly;

import org.neo4j.api.core.RelationshipType;

public enum VehicleRels implements RelationshipType
{
    CONTAINS, VEHICLE, ROOT;
}
