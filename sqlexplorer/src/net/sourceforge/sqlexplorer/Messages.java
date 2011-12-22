package net.sourceforge.sqlexplorer;

/*
 * Copyright (C) 2002-2004 Andrea Mazzolini <andreamazzolini@users.sourceforge.net>
 * Copyright (C) 2007 Rocco Rutte <pdmef@gmx.net>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.squirrel_sql.fw.util.log.ILogger;
import net.sourceforge.squirrel_sql.fw.util.log.LoggerController;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

/**
 * This class manages the string bundle. It is to be used to externalize strings
 * to ease multi-lingual versions based on translation property files.
 * 
 * @author Andrea Mazzolini
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 */
public class Messages {

	private static final String BUNDLE_NAME = ".text";

	private static ResourceBundle[] resources = null;

	private static final ILogger logger = LoggerController
			.createLogger(Messages.class);

	private Messages() {
	}

	/**
	 * Get a translated string given its key. This loads the property file for
	 * sqlexplorer as well as all known extensions prior to first lookup.
	 * 
	 * @param key
	 *            Message's key.
	 * @return The translated message or !key! if not found.
	 */
	public static String getString(String key) {
		init();
		for (int i = 0; i < resources.length; i++) {

			try {
				if (resources[i] != null)
					return resources[i].getString(key);
			} catch (MissingResourceException e) {
				// noop
			}
		}

		return '!' + key + '!';
	}

	/**
	 * Get the translated string given its key and required parameter. This is
	 * wrapper around the java {@link MessageFormat} API.
	 * 
	 * @param key
	 *            Message's key.
	 * @param param
	 *            The message's only parameter.
	 * @return The translated message or !key! if not found.
	 * @see MessageFormat#format(String, Object[])
	 */
	public static String getString(String key, Object param) {
		return getString(key, new Object[] { param });
	}

	/**
	 * Get the translated string given its key and required parameters. This is
	 * wrapper around the java {@link MessageFormat} API.
	 * 
	 * @param key
	 *            Message's key.
	 * @param params
	 *            The message's parameters.
	 * @return The translated message or !key! if not found.
	 * @see MessageFormat#format(String, Object[])
	 */
	public static String getString(String key, Object[] params) {
		String pattern = getString(key);
		return MessageFormat.format(pattern, params);
	}

	/**
	 * Parse some input string and translate all special tokens. This is similar
	 * to a string template engine: it extracts <em>key</em> from
	 * <tt>${key}</tt> sequences in the input string and uses
	 * {@link #getString(String)} to translate it. Contents in between
	 * <tt>${}</tt> sequences are copied as-is.
	 * 
	 * @param input
	 *            The input string.
	 * @return String with all special tokens replaced.
	 */
	public static String processTemplate(String input) {
		if (input == null || input.trim().length() == 0)
			return input;
		StringBuilder sb = new StringBuilder();
		int last = 0;
		for (int i = 0; i < input.length(); i++) {
			if (i <= input.length() - 3 && input.charAt(i) == '$'
					&& input.charAt(i + 1) == '{') {
				int j;
				for (j = i + 2; j < input.length() && input.charAt(j) != '}'; j++)
					;
				if (i > 0)
					sb.append(input.substring(last, i));
				String key = input.substring(i + 2, j);
				String text = null;
				try {
					text = getString(key);
				} catch (Exception e) {
				}
				if (text == null || text.contains(key)) {
					logger.error("Failed to lookup key [" + key + "]");
					sb.append("${" + key + "}");
				}
				else
					sb.append(text);
				last = j + 1;
			}
		}
		sb.append(input.substring(last));
		logger.debug("Template string [" + input + "] turns out as [" + sb
				+ "]");
		return sb.toString();
	}

	private static void init() {
		if (resources == null) {

			// initialize resources

			Bundle mainPlugin = SQLExplorerPlugin.getDefault().getBundle();
			Bundle[] fragments = Platform.getFragments(mainPlugin);

			if (fragments == null) {
				fragments = new Bundle[0];
			}

			resources = new ResourceBundle[fragments.length + 1];

			resources[0] = ResourceBundle.getBundle(mainPlugin
					.getSymbolicName()
					+ BUNDLE_NAME);

			for (int i = 0; i < fragments.length; i++) {
				try {
					resources[i + 1] = ResourceBundle.getBundle(fragments[i]
							.getSymbolicName()
							+ BUNDLE_NAME);
				} catch (Exception ignored) {
					// ignore it
				}
			}
		}
	}

}
