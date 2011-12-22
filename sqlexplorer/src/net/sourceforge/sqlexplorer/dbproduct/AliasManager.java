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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import net.sourceforge.sqlexplorer.ApplicationFiles;
import net.sourceforge.sqlexplorer.ExplorerException;
import net.sourceforge.sqlexplorer.XMLUtils;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;

import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

/**
 * Maintains the list of Alias objects
 * @author John Spackman
 *
 */
public class AliasManager implements ConnectionListener {
	
	// List of aliases, indexed by alias name
	private TreeMap<String, Alias> aliases = new TreeMap<String, Alias>();
	
	// Connection Listeners
	private LinkedList<ConnectionListener> connectionListeners = new LinkedList<ConnectionListener>();
	
	private String checkNull(String pString)
	{
		return pString == null ? "" : pString;
	}
	/**
	 * Loads Aliases from the users preferences
	 *
	 */
	public void loadAliases() throws ExplorerException {
		aliases.clear();
		
		Element root = XMLUtils.readRoot(new File(ApplicationFiles.USER_ALIAS_FILE_NAME));
		if(root != null ) {
			if (root.getName().equals("Beans"))
				root = convertToV350(root);
			List<Element> list = root.elements(Alias.ALIAS);
			if (list != null)
				for (Element elem : list)
					addAlias(new Alias(elem));
		}
	}

	/**
	 * Saves all the Aliases to the users preferences
	 *
	 */
	public void saveAliases() throws ExplorerException {
		DefaultElement root = new DefaultElement(Alias.ALIASES);
		for (Alias alias : aliases.values())
			root.add(alias.describeAsXml());

		XMLUtils.save(root, new File(ApplicationFiles.USER_ALIAS_FILE_NAME));
	}
	
	/**
	 * Adds an Alias
	 * @param alias
	 */
	public void addAlias(Alias alias) throws ExplorerException {
		aliases.put(alias.getName(), alias);
	}
	
	/**
	 * Removes an Alias with a given name
	 * @param aliasName
	 */
	public void removeAlias(String aliasName) {
		Alias alias = aliases.remove(aliasName);
		if (alias != null) {
			alias.closeAllConnections();
			SQLExplorerPlugin.getDefault().getAliasManager().modelChanged();
		}
	}
	
	/**
	 * Locates an Alias by name
	 * @param aliasName
	 * @return
	 */
	public Alias getAlias(String aliasName) {
		return aliases.get(aliasName);
	}
	
	/**
	 * Provides a list of all Aliases
	 * @return
	 */
	public Collection<Alias> getAliases() {
		return aliases.values();
	}
	
	/**
	 * Returns true if the alias is in our list
	 * @param alias
	 * @return
	 */
	public boolean contains(Alias alias) {
		return aliases.values().contains(alias);
	}

	/**
	 * Closes all connections in all aliases; note that ConnectionListeners
	 * are NOT invoked
	 *
	 */
	public void closeAllConnections() throws ExplorerException {
		for (Alias alias : aliases.values())
			alias.closeAllConnections();
	}
	
	/**
	 * Adds a listener for the connections
	 * @param listener
	 */
	public void addListener(ConnectionListener listener) {
		connectionListeners.add(listener);
	}
	
	/**
	 * Removes a listener
	 * @param listener
	 */
	public void removeListener(ConnectionListener listener) {
		connectionListeners.remove(listener);
	}

	/**
	 * Called to notify that the list of connections has changed; passes this onto 
	 * the listeners
	 */
	public void modelChanged() {
		for (ConnectionListener listener : connectionListeners)
			listener.modelChanged();
	}

	/**
	 * Upgrades a v3 definition (java beans style) to v3.5.0beta2 and onwards
	 * @param beans
	 * @return
	 */
	protected Element convertToV350(Element beans) {
		Element result = new DefaultElement(Alias.ALIASES);
		
		for (Element bean : beans.elements("Bean")) {
			Element alias = result.addElement(Alias.ALIAS);
			alias.addAttribute(Alias.AUTO_LOGON, Boolean.toString(getBoolean(bean.elementText("autoLogon"), false)));
			alias.addAttribute(Alias.CONNECT_AT_STARTUP, Boolean.toString(getBoolean(bean.elementText("connectAtStartup"), false)));
			alias.addAttribute(Alias.DRIVER_ID, checkNull(bean.element("driverIdentifier").elementText("string")));
			alias.addElement(Alias.NAME).setText(checkNull(bean.elementText("name")));
			Element userElem = alias.addElement(Alias.USERS).addElement(User.USER);
			userElem.addElement(User.USER_NAME).setText(checkNull(bean.elementText("userName")));
			userElem.addElement(User.PASSWORD).setText(checkNull(bean.elementText("password")));
			alias.addElement(Alias.URL).setText(checkNull(bean.elementText("url")));
			alias.addElement(Alias.FOLDER_FILTER_EXPRESSION).setText(checkNull(bean.elementText("folderFilterExpression")));
			alias.addElement(Alias.NAME_FILTER_EXPRESSION).setText(checkNull(bean.elementText("nameFilterExpression")));
			alias.addElement(Alias.SCHEMA_FILTER_EXPRESSION).setText(checkNull(bean.elementText("schemaFilterExpression")));
		}
		
		return result;
	}
	
	/**
	 * Helper method to convert a string to a boolean
	 * @param value
	 * @param defaultValue
	 * @return
	 */
	private boolean getBoolean(String value, boolean defaultValue) {
		if (value == null || value.trim().length() == 0)
			return defaultValue;
		return value.equals("true") || value.equals("yes") || value.equals("on");
	}
}
