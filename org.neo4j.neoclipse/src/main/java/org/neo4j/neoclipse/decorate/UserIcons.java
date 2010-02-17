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
package org.neo4j.neoclipse.decorate;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.RelationshipType;

/**
 * This class manages user icons.
 * @author Anders Nawroth
 */
public class UserIcons
{
    /**
     * Separator to use in file paths.
     */
    private static final String FILE_SEPARATOR = System
        .getProperty( "file.separator" );
    /**
     * Image file EXTENSIONS to look for.
     */
    public static final String[] EXTENSIONS = new String[] { "png", "PNG",
        "gif", "GIF", "ico", "ICO", "bmp", "BMP", "jpg", "JPG", "jpeg", "JPEG",
        "tif", "TIF", "tiff", "TIFF" };
    /**
     * Last modified value of icons directory.
     */
    private long lastModified = 0;
    /**
     * The icon location.
     */
    private final String iconLocation;
    /**
     * Contents of the icon directory.
     */
    String[] dirContents = null;
    /**
     * The images.
     */
    private final Map<String,Image> images = new HashMap<String,Image>();
    /**
     * Save the names of non-existing images.
     */
    private final Set<String> misses = new HashSet<String>();

    /**
     * @param nodeIconLocation
     *            where to look for icons
     */
    public UserIcons( final String nodeIconLocation )
    {
        this.iconLocation = nodeIconLocation;
    }

    /**
     * Looks up the user image for the given name.
     */
    public Image getImage( final String name )
    {
        // TODO reduce complexity here?
        // CC = 11, NPath complx = 252
        if ( name == null )
        {
            return null; // don't care for now
        }
        Image img = images.get( name );
        if ( img != null || misses.contains( name ) )
        {
            return img;
        }
        File directory = new File( iconLocation );
        if ( !directory.exists() || !directory.isDirectory() )
        {
            return null; // this sholdn't happen
        }
        if ( directory.lastModified() != lastModified )
        {
            lastModified = directory.lastModified();
            dirContents = directory.list();
            misses.clear();
        }
        for ( String fileName : dirContents )
        {
            if ( fileName.charAt( 0 ) != name.charAt( 0 ) )
            {
                continue;
            }
            for ( String imgExt : EXTENSIONS )
            {
                if ( fileName.equals( name + "." + imgExt ) )
                {
                    String imgFileName = iconLocation + FILE_SEPARATOR
                        + fileName;
                    img = new Image( Display.getDefault(), imgFileName );
                    images.put( name, img );
                    return img;
                }
            }
        }
        misses.add( name );
        return null;
    }

    /**
     * Lookup user icon from relationship type and direction.
     */
    public Image getImage( final RelationshipType relType,
        final Direction direction )
    {
        return getImage( createFilename( relType, direction ) );
    }

    /**
     * Get filename (without extension) from relationship type and direction.
     * The direction has to be incoming or outgoing.
     */
    public static String createFilename( final RelationshipType relType,
        final Direction direction )
    {
        if ( direction == Direction.BOTH )
        {
            throw new IllegalArgumentException(
                "Icons can not be set for BOTH direction." );
        }
        return relType.name() + "." + direction.name();
    }
}
