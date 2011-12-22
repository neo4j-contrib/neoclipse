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

import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

import net.sourceforge.sqlexplorer.ExplorerException;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;

/**
 * Represents a configured Alias, maintaining a pool of available 
 * connections
 * 
 * Note that this superceeds the old net.sourceforge.sqlexplorer.AliasModel and 
 * net.sourceforge.sqlexplorer.sessiontree.model.* classes.
 * 
 * This is basically a large rewrite of SQLAlias, which was originally taken from
 * SquirrelSQL; it was based on and used parts of Squirrel which no longer exist
 * (even in the SquirrelSQL CVS on Sourceforge) and are effectively undocumented.
 * Changes needed to fix bugs relating to transactions and multiple logons per alias
 * meant that keeping the old code became unmaintainable, hence the sweeping rewrite.
 * 
 * @author John Spackman
 */
public class Alias {
	
	/*package*/ static final String ALIASES = "aliases";
	/*package*/ static final String ALIAS = "alias";
	/*package*/ static final String AUTO_LOGON = "auto-logon";
	/*package*/ static final String CONNECT_AT_STARTUP = "connect-at-startup";
	/*package*/ static final String DEFAULT_USER = "default-user";
	/*package*/ static final String DRIVER_ID = "driver-id";
	/*package*/ static final String FOLDER_FILTER_EXPRESSION = "folder-filter-expression";
	/*package*/ static final String HAS_NO_USER_NAME = "has-no-user-name";
	/*package*/ static final String NAME = "name";
	/*package*/ static final String NAME_FILTER_EXPRESSION = "name-filter-expression";
	/*package*/ static final String SCHEMA_FILTER_EXPRESSION = "schema-filter-expression";
	/*package*/ static final String URL = "url";
	/*package*/ static final String USERS = "users";


	private static int s_serialNo = 0;

	// Descriptive name of the Alias
    private String name;
    
    // Driver
    private String driverId;

    // Database URL
    private String url;

    // Whether to auto-logon the default user
    private boolean autoLogon;

    // Whether to connect at startup
    private boolean connectAtStartup;

    // Filters
    private String folderFilterExpression = "";
    private String nameFilterExpression = "";
    private String schemaFilterExpression = "";
    
    // Whether username/password are required
    private boolean hasNoUserName;

	// Default user
	private User defaultUser;
	
	// List of all users (including the default user), indexed by user name
	private TreeMap<String, User> users = new TreeMap<String, User>();

	/**
	 * Constructs a new Alias with a given name
	 *
	 */
	public Alias(String name) {
		this.name = name;
	}
	
	/**
	 * Constructs a new Alias with a unique name
	 *
	 */
	public Alias() {
		this("new-alias-" + (++s_serialNo));
	}
	
	/**
	 * Constructs an Alias from XML, previously obtained from describeAsXml()
	 * @param root
	 */
	public Alias(Element root) {
		autoLogon = Boolean.parseBoolean(root.attributeValue(AUTO_LOGON));
		connectAtStartup = Boolean.parseBoolean(root.attributeValue(CONNECT_AT_STARTUP));
		driverId = root.attributeValue(DRIVER_ID);
		String str = root.attributeValue(HAS_NO_USER_NAME);
		if (str != null)
			hasNoUserName = Boolean.parseBoolean(str);
		name = root.elementText(NAME);
		url = root.elementText(URL);
		folderFilterExpression = root.elementText(FOLDER_FILTER_EXPRESSION);
		nameFilterExpression = root.elementText(NAME_FILTER_EXPRESSION);
		schemaFilterExpression = root.elementText(SCHEMA_FILTER_EXPRESSION);

		if (hasNoUserName) {
			User user = new User("anonymous", "");
			addUser(user);
			setDefaultUser(user);
		} else {
			Element usersElem = root.element(USERS);
			if (usersElem != null) {
				List<Element> list = usersElem.elements(User.USER);
				if (list != null)
					for (Element userElem : list) {
						User user = new User(userElem);
						if (user.getUserName() != null && user.getUserName().trim().length() > 0)
							addUser(user);
					}
				String defaultUserName = root.elementText(DEFAULT_USER);
				if (defaultUserName != null) {
					User user = users.get(defaultUserName);
					if (user != null)
						defaultUser = user;
				}
			}
		}
	}

	//helper function
	private String checkNull(String pString)
	{
		return pString == null ? "" : pString;
	}
	
	/**
	 * Describes this alias in XML; the result can be passed to the Alias(Element) constructor
	 * to refabricate it
	 * @return
	 */
	public Element describeAsXml() {
		DefaultElement root = new DefaultElement(ALIAS);
		root.addAttribute(AUTO_LOGON, Boolean.toString(autoLogon));
		root.addAttribute(CONNECT_AT_STARTUP, Boolean.toString(connectAtStartup));
		root.addAttribute(DRIVER_ID, checkNull(driverId));
		root.addAttribute(HAS_NO_USER_NAME, Boolean.toString(hasNoUserName));
		root.addElement(NAME).setText(checkNull(name));
		root.addElement(URL).setText(checkNull(url));
		root.addElement(FOLDER_FILTER_EXPRESSION).setText(checkNull(folderFilterExpression));
		root.addElement(NAME_FILTER_EXPRESSION).setText(checkNull(nameFilterExpression));
		root.addElement(SCHEMA_FILTER_EXPRESSION).setText(checkNull(schemaFilterExpression));
		Element usersElem = root.addElement(USERS);
		for (User user : users.values())
			usersElem.add(user.describeAsXml());
		if (defaultUser != null)
			root.addElement(DEFAULT_USER).setText(defaultUser.getUserName());
		return root;
	}
	
	/**
	 * Constructs an Alias as a duplicate of another, but with a new name
	 * @param copyFrom
	 */
	public Alias(Alias copyFrom) {
		this("Copy of " + copyFrom.getName());
		setAutoLogon(copyFrom.isAutoLogon());
		setConnectAtStartup(copyFrom.isConnectAtStartup());
		setDriver(copyFrom.getDriver());
		setHasNoUserName(copyFrom.hasNoUserName());
		setUrl(copyFrom.getUrl());
		if (copyFrom.defaultUser != null) 
		{
			defaultUser = copyFrom.defaultUser.createCopy();
			addUser(defaultUser);
		}
	}
	
	/**
	 * Closes all connections
	 * @throws ExplorerException
	 */
	public void closeAllConnections() {
		for (User user : users.values())
			user.closeAllSessions();
	}
	
	/**
	 * Removes this Alias (permanently)
	 * @throws ExplorerException
	 */
	public void remove() {
		closeAllConnections();
		SQLExplorerPlugin.getDefault().getAliasManager().removeAlias(getName());
	}
	
	/**
	 * Returns the name of the alias
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Adds or redefines a User; if the user does not exist (IE there is no User object with the
	 * same user name) then the new user is added, but if a User already exists then the passed 
	 * in User is used to reconfigure the existing User.  In both cases addUser() will return the 
	 * User object which remains in this Alias instance; EG when reconfiguring a User, the reconfigured,
	 * pre-existing User object is returned.   
	 * @param user
	 * @return the User object which is kept in the list of Users
	 */
	public User addUser(User user) {
		if (user.getAlias() != null) {
			if (user.getAlias() != this)
				throw new IllegalArgumentException("User already belongs to a different Alias");
			return user;
		}
		if (user.getUserName() == null || user.getUserName().length() == 0)
			throw new IllegalArgumentException("Illegal user name");
		if (!users.isEmpty() && hasNoUserName)
			throw new IllegalArgumentException("Cannot add users when usernames are not required by the alias");
			
		User existingUser = users.get(user.getUserName());
		if (existingUser != null) {
			existingUser.mergeWith(user);
			return existingUser;
		}
		
		users.put(user.getUserName(), user);
		user.setAlias(this);
		if (defaultUser == null)
			defaultUser = user;
		SQLExplorerPlugin.getDefault().getAliasManager().modelChanged();
		return user;
	}
	
	/**
	 * Removes the User from the list of users
	 * @param user
	 */
	public void removeUser(User user) {
		boolean isDefault = user == defaultUser;
		if (user.getAlias() != this)
			throw new IllegalArgumentException("User belongs to a different Alias");
		user.closeAllSessions();
		user.setAlias(null);
		users.remove(user.getUserName());
		if (isDefault) {
			if (!users.isEmpty())
				defaultUser = users.values().iterator().next();
			else
				defaultUser = null;
		}
	}
	
	/**
	 * Returns the user with a given name
	 * @param userName
	 * @return
	 */
	public User getUser(String userName) {
		return users.get(userName);
	}

	/**
	 * Returns a list of all users
	 * @return
	 */
	public Collection<User> getUsers() {
		return users.values();
	}
	
	/**
	 * Returns true if the user belongs to this Alias
	 * @param user
	 * @return
	 */
	public boolean contains(User user) {
		return users.values().contains(user);
	}
	
	/**
	 * Returns the ISQLDriver underlying this alias
	 * @return
	 */
	public ManagedDriver getDriver() {
		return SQLExplorerPlugin.getDefault().getDriverModel().getDriver(driverId);
	}
	
	/**
	 * Sets the underlying driver
	 * @param driver
	 */
	public void setDriver(ManagedDriver driver) {
		driverId = driver.getId();
	}
	
	/**
	 * Returns true if filtering is applied
	 * @return
	 */
	public boolean isFiltered() {
		return (folderFilterExpression != null && folderFilterExpression.trim().length() > 0) ||
			(nameFilterExpression != null && nameFilterExpression.trim().length() > 0) ||
			(schemaFilterExpression != null && schemaFilterExpression.trim().length() > 0);
	}

	public boolean isAutoLogon() {
		return autoLogon;
	}

	public void setAutoLogon(boolean autoLogon) {
		this.autoLogon = autoLogon;
	}

	public boolean isConnectAtStartup() {
		return connectAtStartup;
	}

	public void setConnectAtStartup(boolean connectAtStartup) {
		this.connectAtStartup = connectAtStartup;
	}

	public User getDefaultUser() {
		return defaultUser;
	}
	
	/**
	 * Sets the default user, adding it to the list of users if necessary; 
	 * if the defaultUser is a new user and a user with the same username
	 * already exists then the existing user is updated and returned (see
	 * addUser())
	 * @param defaultUser
	 * @return
	 */
	public User setDefaultUser(User defaultUser) {
		this.defaultUser = addUser(defaultUser);
		return this.defaultUser;
	}

	public String getFolderFilterExpression() {
		return folderFilterExpression;
	}

	public void setFolderFilterExpression(String folderFilterExpression) {
		this.folderFilterExpression = folderFilterExpression;
	}

	public String getNameFilterExpression() {
		return nameFilterExpression;
	}

	public void setNameFilterExpression(String nameFilterExpression) {
		this.nameFilterExpression = nameFilterExpression;
	}

	public String getSchemaFilterExpression() {
		return schemaFilterExpression;
	}

	public void setSchemaFilterExpression(String schemaFilterExpression) {
		this.schemaFilterExpression = schemaFilterExpression;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDriverId() {
		return driverId;
	}

	/**
	 * @return the hasNoUserName
	 */
	public boolean hasNoUserName() {
		return hasNoUserName;
	}

	/**
	 * @param hasNoUserName the hasNoUserName to set
	 */
	public void setHasNoUserName(boolean hasNoUserName) {
		if (this.hasNoUserName == hasNoUserName)
			return;
		this.hasNoUserName = hasNoUserName;
		if (hasNoUserName) {
			for (User user : users.values())
				user.setAlias(null);
			users.clear();
			User user = new User("anonymous", "");
			addUser(user);
			setDefaultUser(user);
		} else {
			for (User user : users.values())
				user.setAlias(null);
			users.clear();
		}
	}

	public DatabaseProduct getDatabaseProduct() {
		return DatabaseProductFactory.getInstance(this);
	}
}
