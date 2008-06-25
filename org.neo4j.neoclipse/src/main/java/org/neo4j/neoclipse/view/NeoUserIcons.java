/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.neoclipse.view;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * This class manages user icons.
 * @author Anders Nawroth
 */
public class NeoUserIcons
{
    /**
     * Prefix to separate user icons.
     */
    public final String PREFIX = "user.";
    /**
     * Image file extensions to look for.
     */
    public final String[] extensions = new String[] { "png", "gif", "ico", "bmp" };
    /**
     * The images.
     */
    protected Map<String,Image> images = new HashMap<String,Image>();

    /**
     * Looks up the user image for the given name, using the given location if needed.
     */
    public Image getImage( String name, String location )
    {
        Image img = images.get( name );
        if ( img == null )
        {
            String imgFileName = location
                + System.getProperty( "file.separator" ) + name + ".";
            for ( String imgExt : extensions )
            {
                String fullFileName = imgFileName + imgExt;
                File file = new File( fullFileName );
                if ( file.exists() )
                {
                    img = new Image( Display.getDefault(), fullFileName );
                    images.put( name, img );
                    break;
                }
            }
        }
        return img;
    }
}
