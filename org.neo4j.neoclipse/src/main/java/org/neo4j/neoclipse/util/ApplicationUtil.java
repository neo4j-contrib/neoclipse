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

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.datalocation.Location;

public class ApplicationUtil
{

    /** Name of file that contains connection aliases. */
    public static final String USER_ALIAS_FILE_NAME = dirInWorkspace( "settings" ) + File.separator
                                                      + "NeoDbAliases.xml";

    public static File dirInWorkspace( final String... elements )
    {
        Location workspace = Platform.getUserLocation();
        if ( workspace == null )
        {
            throw new RuntimeException( "Can't find workspace." );
        }
        URL url = workspace.getDefault();
        String path;
        try
        {
            path = url.toURI().getPath();
        }
        catch ( URISyntaxException e )
        {
            // workaround for Windows
            System.out.println( "Using path workaround for path: " + url );
            path = url.getPath();
        }
        for ( String element : elements )
        {
            path += File.separator + element;
        }
        File dir = new File( path );
        if ( !dir.exists() )
        {
            if ( !dir.mkdirs() )
            {
                throw new IllegalArgumentException( "Could not create directory: " + dir );
            }
        }
        return dir;
    }

    public static String returnEmptyIfBlank( String pString )
    {
        return pString == null ? "" : pString;
    }

    public static boolean isBlank( String string )
    {
        return ( string == null || string.trim().isEmpty() );
    }

}
