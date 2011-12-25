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
