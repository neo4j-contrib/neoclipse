package net.sourceforge.sqlexplorer.parsers;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for net.sourceforge.sqlexplorer.parsers");
		//$JUnit-BEGIN$
		suite.addTestSuite(ExecutionContextTest.class);
		//$JUnit-END$
		return suite;
	}

}
