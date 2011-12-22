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
package net.sourceforge.sqlexplorer.preview;

import java.util.HashSet;

import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;

/**
 * Factory class used to create instances of Previewer for a given MIME 
 * content type or reflected object type
 * 
 * @author John Spackman
 */
public final class PreviewerFactory {
	
	/*
	 * Class used to register a PreviewerType with the factory
	 */
	public interface Resolver {
		/**
		 * Called to discover whether a given MIME type can be handled
		 * @param mimeType
		 * @return
		 */
		public boolean canAcceptMimeType(String mimeType);
		
		/**
		 * Called to discover whether a given Object can be handled; this should
		 * not include implicit conversions to String etc
		 * @param data
		 * @return
		 */
		public boolean canAcceptObject(Object data);
		
		/**
		 * Returns the Previewer class
		 * @return
		 */
		public Class<? extends Previewer> getPreviewerClass();
	}
	
	// Singleton instance
	private static PreviewerFactory s_instance;

	// Resolvers
	private HashSet<Resolver> resolvers = new HashSet<Resolver>();

	/**
	 * Registers a Previewer resolver
	 * @param mimeType
	 * @param clazz
	 */
	/*package*/ void registerClass(Resolver resolver) {
		resolvers.add(resolver);
	}
	
	/**
	 * Returns a Previewer-derived instance for a given MIME type
	 * @param mimeType the MIME content type
	 * @param data the data to be displayed
	 * @return the Previewer, or null if one cannot be found
	 */
	public Previewer getInstance(String mimeType, Object data) {
		if (mimeType != null)
			for (Resolver resolver : resolvers)
				if (resolver.canAcceptMimeType(mimeType))
					return newInstance(resolver, data);
		return getInstance(data);
	}
	
	/**
	 * Returns a Previewer-derived instance based solely on the type of
	 * object in <data/>
	 * @param mimeType the MIME content type
	 * @param data the data to be displayed
	 * @return the Previewer, or null if one cannot be found
	 */
	public Previewer getInstance(Object data) {
		for (Resolver resolver : resolvers)
			if (resolver.canAcceptObject(data))
				return newInstance(resolver, data);
		return null;
	}
	
	/**
	 * Called to instantiate a Previewer
	 * @param resolver
	 * @param data
	 * @return
	 */
	private Previewer newInstance(Resolver resolver, Object data) {
		Class<? extends Previewer> clazz = resolver.getPreviewerClass();
		
		Exception exception = null;
		try {
			Previewer previewer = clazz.newInstance();
			return previewer;
		} catch(InstantiationException e) {
			exception = e;
		} catch(IllegalAccessException e) {
			exception = e;
		}
		
		SQLExplorerPlugin.error("Cannot create Previewer", exception);
		return null;
	}
	
	/**
	 * Returns the singleton instance
	 * @return
	 */
	public static PreviewerFactory getInstance() {
		if (s_instance == null) {
			s_instance = new PreviewerFactory();
			XmlPreviewer.register();
		}
		return s_instance;
	}
}
