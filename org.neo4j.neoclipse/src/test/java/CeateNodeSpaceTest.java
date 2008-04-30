import java.io.File;


import org.junit.Test;
import org.neo4j.api.core.EmbeddedNeo;
import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Transaction;

public class CeateNodeSpaceTest{

	private static final String NAME = "NAME";
	
	@Test
	public void testCreateSimpleNodeSpace() {
		NeoService neo = new EmbeddedNeo(new File("target/neo").getAbsolutePath());
		Transaction transaction = Transaction.begin();
		try {
			Node referenceNode = neo.getReferenceNode();
			referenceNode.setProperty(NAME, "referenceNode");
			Node peter = neo.createNode();
			peter.setProperty(NAME, "Peter");
			Node li = neo.createNode();
			li.setProperty(NAME, "Li");

			Node blaff = neo.createNode();
			blaff.setProperty(NAME, "blaff");

			Node woff = neo.createNode();
			woff.setProperty(NAME, "woff");
			
			referenceNode.createRelationshipTo(peter,
					MyRels.ROOT);
			referenceNode.createRelationshipTo(li,
					MyRels.ROOT);
			li.createRelationshipTo(peter, MyRels.KNOWS);
			peter.createRelationshipTo(woff, MyRels.OWNS);
			li.createRelationshipTo(blaff, MyRels.OWNS);
			
			transaction.success();
		} finally {
			transaction.finish();
		}
		neo.shutdown();
	}
}
