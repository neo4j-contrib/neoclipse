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

import java.util.HashMap;

import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.util.TextUtil;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

/**
 * Accessor class for obtaining a platform-specific instance of DatabaseProduct.
 * Uses reflection to identify an instance class and then call a public static
 * member called getProductInstance() to get a singleton to return.
 * 
 * If a platform-specific version cannot be found, the DefaultDatabaseProduct is 
 * used instead
 * 
 * @see DatabaseProduct
 * @author John Spackman
 *
 */
public final class DatabaseProductFactory {
	
	// We guarantee to always be able to provide an instance; this is the one we
	//	provide if none has been implemented for the given database platform
	private static DefaultDatabaseProduct s_defaultProduct = new DefaultDatabaseProduct();
	
	private static HashMap<String, DatabaseProduct> instances = new HashMap<String, DatabaseProduct>();
	
	
	
	/**
	 * Returns an instance of DatabaseProduct for the platform at the connection
	 * held by the connection
	 * @param node the connected node
	 * @return a DatabaseProduct for the platform, never returns null
	 */
	public static DatabaseProduct getInstance(Alias pAlias) {
		DatabaseProduct product = instances.get(pAlias.getUrl());
		if (product != null)
			return product;
        return s_defaultProduct;
	}

	public static DatabaseProduct registerProduct(String pUrl, String pDatabaseProductName) {
		DatabaseProduct result = instances.get(pUrl);
		if(result != null)
		{
			return result;
		}
        // load extension nodes
        String databaseProductName = pDatabaseProductName.toLowerCase().trim();

        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint point = registry.getExtensionPoint("net.sourceforge.sqlexplorer", "databaseProduct");
        IExtension[] extensions = point.getExtensions();

        for (int i = 0; i < extensions.length; i++) {

            IExtension e = extensions[i];
            
            IConfigurationElement[] ces = e.getConfigurationElements();

            for (int j = 0; j < ces.length; j++) {
                try {

                    boolean isValidProduct = false;
                    String[] validProducts = ces[j].getAttribute("database-product-name").split(",");

                    // include only nodes valid for this database
                    for (int k = 0; k < validProducts.length; k++) {

                        String product = validProducts[k].toLowerCase().trim();

                        if (product.length() == 0) {
                            continue;
                        }

                        if (product.equals("*")) {
                            isValidProduct = true;
                            break;
                        }

                        String regex = TextUtil.replaceChar(product, '*', ".*");
                        if (databaseProductName.matches(regex)) {
                            isValidProduct = true;
                            break;
                        }

                    }

                    if (!isValidProduct) {
                        continue;
                    }

                    result = (DatabaseProduct) ces[j].createExecutableExtension("class");

                } catch (Throwable ex) {
                    SQLExplorerPlugin.error("Could not determine database product", ex);
                }
            }
        }
        if(result == null)
        {
        	return s_defaultProduct;
        }
        instances.put(pUrl, result);
        return result;
	}
	
}
