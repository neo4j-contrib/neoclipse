/*
 * Licensed to "Neo Technology," Network Engine for Objects in Lund AB
 * (http://neotechnology.com) under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership. Neo Technology licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License
 * at (http://www.apache.org/licenses/LICENSE-2.0). Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
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
    public final String[] extensions = new String[] { "png", "gif", "ico",
        "bmp", "jpg", "jpeg", "tif", "tiff" };
    /**
     * Last modified value of icons directory.
     */
    private long lastModified = 0;
    /**
     * Last icon location.
     */
    private String iconLocation = "";
    /**
     * Contents of the icon directory.
     */
    String[] dirContents = null;
    /**
     * The images.
     */
    protected Map<String,Image> images = new HashMap<String,Image>();

    /**
     * Looks up the user image for the given name, using the given location if
     * needed.
     */
    public Image getImage( String name, String location )
    {
        if ( name == null || name.trim() == "" )
        {
            return null; // don't care for now
        }
        Image img = images.get( name );
        if ( img != null )
        {
            return img;
        }
        if ( !location.equals( iconLocation ) )
        {
            iconLocation = location;
            lastModified = 0;
        }
        File directory = new File( location );
        if ( !directory.exists() || !directory.isDirectory() )
        {
            return null; // this sholdn't happen
        }
        if ( directory.lastModified() != lastModified )
        {
            dirContents = directory.list();
            lastModified = directory.lastModified();
        }
        for ( String fileName : dirContents )
        {
            if ( fileName.charAt( 0 ) != name.charAt( 0 ) )
            {
                continue;
            }
            for ( String imgExt : extensions )
            {
                if ( fileName.equals( name + "." + imgExt )
                    || fileName.equals( name + "." + imgExt.toUpperCase() ) )
                {
                    String imgFileName = location
                        + System.getProperty( "file.separator" ) + fileName;
                    img = new Image( Display.getDefault(), imgFileName );
                    images.put( name, img );
                    return img;
                }
            }
        }
        return img;
    }
}
