/*
 * Copyright (C) 2007 SQL Explorer Development Team
 * http://sourceforge.net/projects/eclipsesql
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sourceforge.sqlexplorer.dbproduct;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;

import net.sourceforge.sqlexplorer.ApplicationFiles;
import net.sourceforge.sqlexplorer.ExplorerException;
import net.sourceforge.sqlexplorer.XMLUtils;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.util.URLUtil;

import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

/**
 * Provides access to the list of drivers, persisting their configuration in the
 * Eclipse workspace; restoring to defaults is also supported.
 * 
 * This is part of the rewrite of SQLAlias, which was originally taken from
 * SquirrelSQL; the old DriverModel (via DataCache) used parts of Squirrel which no 
 * longer exist (even in the SquirrelSQL CVS on Sourceforge) and are effectively 
 * undocumented.  Changes needed to fix bugs relating to transactions and multiple 
 * logons per alias meant that keeping the old code became unmaintainable, hence the 
 * sweeping rewrite.
 * 
 * @author John Spackman
 */
public class DriverManager {
	
	public static final String DRIVER = "driver";
	public static final String DRIVER_CLASS = "driver-class";
	public static final String DRIVERS = "drivers";
	public static final String ID = "id";
	public static final String JARS = "jars";
	public static final String JAR = "jar";
	public static final String NAME = "name";
	public static final String URL = "url";
	
	// List of drivers, indexed by ID
	private HashMap<String, ManagedDriver> drivers = new HashMap<String, ManagedDriver>();
	
	// Highest ID, used when creating a new unique ID
	private int highestId;
	
	/**
	 * Restores drivers to their default location
	 * @throws ExplorerException
	 */
	public void restoreDrivers() throws ExplorerException {
		try {
			drivers.clear();
			highestId = 0;
	        URL url = URLUtil.getResourceURL("default_drivers.xml");
			loadDrivers(url.openStream());
		}catch(IOException e) {
			throw new ExplorerException(e);
		}
	}
	
	/**
	 * Loads drivers from the users preferences
	 * @throws ExplorerException
	 */
	public void loadDrivers() throws ExplorerException {
		try {
			File file = new File(ApplicationFiles.USER_DRIVER_FILE_NAME);
			if (!file.exists()) {
				restoreDrivers();
				saveDrivers();
				return;
			}
			loadDrivers(new FileInputStream(file));
		}catch(IOException e) {
			throw new ExplorerException("Cannot load user drivers: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Loads driver definition from a given location
	 * @param input
	 * @throws ExplorerException
	 */
	protected void loadDrivers(InputStream input) throws ExplorerException {
		try {
			Element root = XMLUtils.readRoot(input);
			if(root == null)
			{
				throw new ExplorerException("Unable to read driver definitions");
			}
			if (root.getName().equals("Beans"))
				root = convertFromV3(root);
			
			for (Element driverElem : root.elements(DRIVER)) {
				ManagedDriver driver = new ManagedDriver(driverElem);
				addDriver(driver);
			}
		}catch(Exception e) {
			throw new ExplorerException(e);
		}
	}
	
	/**
	 * Saves the drivers back to disk
	 * @throws ExplorerException
	 */
	public void saveDrivers() throws ExplorerException {
		Element root = new DefaultElement(DRIVERS);
		for (ManagedDriver driver : drivers.values())
			root.add(driver.describeAsXml());

		XMLUtils.save(root, new File(ApplicationFiles.USER_DRIVER_FILE_NAME));
	}
	
	/**
	 * Adds a new Driver
	 * @param driver
	 */
	public void addDriver(ManagedDriver driver) {
		if (driver.getId() == null || driver.getId().trim().length() == 0)
			throw new IllegalArgumentException("Driver has an invalid ID");
		if (drivers.get(driver.getId()) != null)
			throw new IllegalArgumentException("Driver with id of " + driver.getId() + " already exists");
		drivers.put(driver.getId(), driver);
		
		// Try and update our highest ID; if it's not a valid number then we
		//	just ignore it
		try {
			int id = Integer.parseInt(driver.getId());
			if (id > 0 && id > highestId)
				highestId = id;
		} catch(NumberFormatException e) {
			// Nothing
		}
	}
	
	/**
	 * Removes a driver
	 * @param driver
	 */
	public void removeDriver(ManagedDriver driver) {
		drivers.remove(driver.getId());
	}
	
	/**
	 * Returns a driver with a given ID
	 * @param id
	 * @return
	 */
	public ManagedDriver getDriver(String id) {
		return drivers.get(id);
	}
	
	/**
	 * Returns all the drivers 
	 * @return
	 */
	public Collection<ManagedDriver> getDrivers() {
		return drivers.values();
	}
	
	/**
	 * Allocates a new Unique ID for creating drivers with
	 * @return
	 */
	public String createUniqueId() {
		return Integer.toString(++highestId);
	}
	
	/**
	 * Converts from the old v3 format (which is a JavaBean encoding)
	 * @param root
	 * @return
	 */
	protected Element convertFromV3(Element root) {
		Element result = new DefaultElement(DRIVERS);
		for (Element elem : root.elements("Bean")) {
			String str;
			Element driver = result.addElement(DRIVER);
			
			try {			
				str = elem.element("identifier").elementText("string");
				driver.addAttribute(ID, str);
				
				str = elem.elementText("driverClass");
				if (str != null)
					driver.addElement(DRIVER_CLASS).setText(str);
				
				str = elem.elementText("name");
				driver.addElement(NAME).setText(str);
				
				str = elem.elementText("url");
				driver.addElement(URL).setText(str);
				
				Element jars = driver.addElement(JARS);
				Element jarFileNames = elem.element("jarFileNames");
				for (Element jarBeanElem : jarFileNames.elements("Bean")) {
					str = jarBeanElem.elementText("string");
					if (str != null && str.trim().length() > 0)
						jars.addElement(JAR).setText(str);
				}
			}catch(IllegalArgumentException e) {
				SQLExplorerPlugin.error("Error loading v3 driver " + driver.attributeValue(ID), e);
				throw e;
			}
		}
		
		return result;
	}
}
