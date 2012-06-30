/**
 * Licensed to Neo Technology under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Neo Technology licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.neo4j.neoclipse.util;

/*
 * Copyright (C) 2002-2004 Andrea Mazzolini
 * andreamazzolini@users.sourceforge.net
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

import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.neo4j.neoclipse.Activator;

public class URLUtil
{

    private URLUtil()
    {
    }

    public static URL getResourceURL( String s )
    {
        if ( !initialized )
        {
            init();
        }
        URL url = null;
        try
        {
            url = new URL( baseURL, s );
        }
        catch ( Throwable e )
        {
        }
        return url;
    }

    static private boolean initialized = false;

    static private void init()
    {
        Activator defaultPlugin = Activator.getDefault();

        baseURL = defaultPlugin.getBundle().getEntry( "/" );
        initialized = true;
    }

    private static URL baseURL;

    /**
     * Return a URI to a file located in your plugin fragment
     * 
     * @param yourPluginId e.g net.sourceforge.sqlexplorer.oracle
     * @param filePath path to file within your fragment e.g. icons/test.gif
     * @return URI to the file.
     */
    public static URL getFragmentResourceURL( String yourPluginId, String filePath )
    {

        URL url = null;

        try
        {
            URL baseURL = Platform.getBundle( yourPluginId ).getEntry( "/" );
            url = new URL( baseURL, filePath );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

        return url;
    }

}
