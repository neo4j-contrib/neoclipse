/**
 * 
 */
package net.sourceforge.sqlexplorer.dbproduct;

import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.datalocation.Location;

/**
 * Utility class to provide replacement functions for some locations
 * 
 * @author hhilbert
 *
 */
public class Locations {
	private static final String ECLIPSE_HOME = "${eclipse_home}";
	private static final String WORKSPACE_LOC = "${workspace_loc}";

	static private String getPath(Location pLocation)
	{
		String path = pLocation.getURL().getPath();
		// on win platforms we get /c:/xx and we need c:/xx on linux the path is ok
		int pos = path.indexOf(':');
		if( pos > 0 && pos < 5)
		{
			path = path.substring(1);
		}
		return path;
	}

	/**
	 * expand place holder for workspace location with real path
	 * 
	 * @param pName string to replace place holder with path of known location
	 * @return modified string
	 */
	static public String expandWorkspace(String pName)
	{
		return pName
			.replace(WORKSPACE_LOC, getPath(Platform.getInstanceLocation()))
			;
	}

	/**
	 * expand place holder for known locations with real path
	 * 
	 * @param pName string to replace place holder with path of known location
	 * @return modified string
	 */
	static public String expand(String pName)
	{
		return expandWorkspace(pName)
			.replace(ECLIPSE_HOME, getPath(Platform.getInstallLocation()))
			;
	}

	/**
	 * insert place holder for known locations
	 * 
	 * @param pName string to replace paths of known locations with place holder
	 * @return modified string
	 */
	static public String insert(String pName)
	{
		return pName
			.replace('\\', '/')
			.replace(getPath(Platform.getInstanceLocation()),WORKSPACE_LOC)
			.replace(getPath(Platform.getInstallLocation()),ECLIPSE_HOME)
			;
	}

}
