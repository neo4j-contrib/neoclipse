/*
 * NeoUserIcons.java
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
