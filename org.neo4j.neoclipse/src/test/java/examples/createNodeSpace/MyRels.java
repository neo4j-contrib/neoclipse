package examples.createNodeSpace;
import org.neo4j.api.core.RelationshipType;


public enum MyRels implements RelationshipType{
	KNOWS, OWNS, ROOT;
}
