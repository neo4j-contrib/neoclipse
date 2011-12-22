package net.sourceforge.sqlexplorer.util;

/*
 * Copyright (C) 2001-2002-2004 Colin Bell
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
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.sourceforge.squirrel_sql.fw.util.Utilities;

public class MyURLClassLoader extends URLClassLoader {

    private Map<String,Class<?>> _classes = new HashMap<String, Class<?>>();


    public MyURLClassLoader(String fileName) throws IOException {
        this(new File(fileName).toURI().toURL());
    }


    public MyURLClassLoader(URL url) {
        this(new URL[] {url});
    }


    public MyURLClassLoader(URL[] urls) {
        super(urls, URLUtil.class.getClassLoader());
    }


    public Class<?>[] getAssignableClasses(Class<?> type) throws Exception {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        URL[] urls = getURLs();
        for (int i = 0; i < urls.length; ++i) {
            URL url = urls[i];
            File file = new File(url.toURI().getPath());
            if (!file.isDirectory() && file.exists() && file.canRead()) {
                ZipFile zipFile = null;
                try {
                    zipFile = new ZipFile(file);
                } catch (IOException ex) {
                }
                for (Enumeration<? extends ZipEntry> it = zipFile.entries(); it.hasMoreElements();) {
                    Class<?> cls = null;
                    String entryName = it.nextElement().getName();
                    String className = Utilities.changeFileNameToClassName(entryName);
                    if (className != null) {

                        try {
                            cls = loadClass(className);
                        } catch (Throwable th) {

                        }
                        if (cls != null) {
                            if (type.isAssignableFrom(cls)) {
                                classes.add(cls);
                            }
                        }
                    }
                }
            }
        }
        return (Class[]) classes.toArray(new Class[classes.size()]);
    }


    protected synchronized Class<?> findClass(String className) throws ClassNotFoundException {
        Class<?> cls = _classes.get(className);
        if (cls == null) {
            cls = super.findClass(className);
            _classes.put(className, cls);
        }
        return cls;
    }


    protected void classHasBeenLoaded(Class<?> cls) {
    }
}
