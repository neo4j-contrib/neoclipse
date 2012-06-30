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

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

public class ImageUtil
{

    public static ImageDescriptor getDescriptor( String path )
    {
        try
        {
            if ( path == null || path.trim().length() == 0 )
            {
                throw new IllegalArgumentException();
            }

            // create image
            URL url = URLUtil.getResourceURL( path );
            return ImageDescriptor.createFromURL( url );

        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }

    }

    public static Image getImage( String propertyName )
    {
        return getDescriptor( propertyName ).createImage();
    }

}
