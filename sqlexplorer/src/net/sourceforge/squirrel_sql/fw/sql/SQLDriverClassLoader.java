package net.sourceforge.squirrel_sql.fw.sql;
/*
 * Copyright (C) 2001-2002 Colin Bell
 * colbell@users.sourceforge.net
 *
 * This library is free software; you can redistribute it and/or
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
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Driver;
import java.util.List;

import net.sourceforge.sqlexplorer.dbproduct.ManagedDriver;
import net.sourceforge.squirrel_sql.fw.util.MyURLClassLoader;
import net.sourceforge.squirrel_sql.fw.util.log.ILogger;

public class SQLDriverClassLoader extends MyURLClassLoader
{
	public SQLDriverClassLoader(ClassLoader parent, ManagedDriver sqlDriver) throws MalformedURLException
	{
		super(parent, createURLs(sqlDriver.getJars()));
	}

	public SQLDriverClassLoader(ClassLoader parent, URL url)
	{
		super(parent, url);
	}

	public Class<?>[] getDriverClasses(ILogger logger)
	{
		return getAssignableClasses(Driver.class, logger);
	}

	private static URL[] createURLs(List<String> fileNames) throws MalformedURLException {
		URL[] urls;
		if (fileNames == null)
			urls = new URL[0];
		else {
			urls = new URL[fileNames.size()];
			int i = 0;
			for (String fileName : fileNames)
				urls[i++] = new File(fileName).toURI().toURL();
		}
		return urls;
	}
}
