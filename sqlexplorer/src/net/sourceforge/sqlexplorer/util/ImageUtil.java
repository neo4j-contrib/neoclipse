/*
 * Copyright (C) 2007 SQL Explorer Development Team
 * http://sourceforge.net/projects/eclipsesql
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
package net.sourceforge.sqlexplorer.util;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

public class ImageUtil {

    private static Map<String,Integer> _imageCount = new HashMap<String, Integer>();

    private static Map<String,Image> _images = new HashMap<String, Image>();


    /**
     * Dispose of an image in cache. Once there are no more open handles to the
     * image it will be disposed of.
     * 
     */
    public static void disposeImage(String propertyName) {

        try {
            Image image = (Image) _images.get(propertyName);

            if (image == null) {
                return;
            }

            Integer handleCount = (Integer) _imageCount.get(propertyName);

            if (handleCount == null || handleCount == 0) {
                image.dispose();
                _images.remove(propertyName);
            	_imageCount.remove(propertyName);
            } else {
                handleCount = new Integer(handleCount.intValue() - 1);
            	_imageCount.put(propertyName, handleCount);
            }

        } catch (Throwable e) {
            SQLExplorerPlugin.error("Error disposing images", e);
        }
    }


    /**
     * Create an image descriptor for the given image property in the
     * text.properties file.
     * 
     * @param propertyName
     * @return
     */
    public static ImageDescriptor getDescriptor(String propertyName) {

        try {

            if (propertyName == null) {
                return null;
            }
            
            // get image path
            String path = Messages.getString(propertyName);

            if (path == null || path.trim().length() == 0) {
                SQLExplorerPlugin.error("Missing image path for " + propertyName, null);
                return null;
            }

            // create image
            URL url = URLUtil.getResourceURL(path);
            return ImageDescriptor.createFromURL(url);

        } catch (Exception e) {
            SQLExplorerPlugin.error("Couldn't create image for " + propertyName, e);
            return null;
        }

    }

    /**
     * Create an image descriptor for the given image property in the
     * text.properties file.
     * 
     * @param propertyName
     * @return
     */
    public static ImageDescriptor getDescriptorByKey(String pKey) {

        try {

            // get image path
            String path = pKey;

            if (path == null || path.trim().length() == 0) {
                SQLExplorerPlugin.error("Missing image path for " + pKey, null);
                return null;
            }

            // create image
            URL url = URLUtil.getResourceURL(path);
            return ImageDescriptor.createFromURL(url);

        } catch (Exception e) {
            SQLExplorerPlugin.error("Couldn't create image for " + pKey, e);
            return null;
        }

    }


    public static ImageDescriptor getFragmentDescriptor(String fragmentId, String path) {
        
        try {

            if (path == null || path.trim().length() == 0) {
                return null;
            }
            
            // create image
            URL url = URLUtil.getFragmentResourceURL(fragmentId, path);
            return ImageDescriptor.createFromURL(url);

        } catch (Exception e) {
            SQLExplorerPlugin.error("Couldn't create image for " + fragmentId + ": " + path, e);
            return null;
        }
        
    }

    /**
     * Get an image object from cache or create one if it doesn't exist yet.
     * Everytime an object is retrieved, it should be disposed of using the
     * ImageUtil.disposeImage method.
     * 
     * @param propertyName
     */
    public static Image getImage(String propertyName) {

        Image image = (Image) _images.get(propertyName);

        if (image == null) {
            image = getDescriptor(propertyName).createImage();

            if (image == null) {
                return null;
            }

            _images.put(propertyName, image);
        }

        // increase image handle count by one

        Integer handleCount = (Integer) _imageCount.get(propertyName);

        if (handleCount == null) {
            handleCount = new Integer(1);
        } else {
            handleCount = new Integer(handleCount.intValue() + 1);
        }
        _imageCount.put(propertyName, handleCount);

        return image;
    }
    
    public static Image getFragmentImage(String fragmentId, String path) {
        
        try {

            if (path == null || path.trim().length() == 0) {
                return null;
            }
            
            // create image
            URL url = URLUtil.getFragmentResourceURL(fragmentId, path);
            ImageDescriptor descriptor = ImageDescriptor.createFromURL(url);
            if (descriptor == null) {
                return null;
            }
            return descriptor.createImage();

        } catch (Exception e) {
            SQLExplorerPlugin.error("Couldn't create image for " + fragmentId + ": " + path, e);
            return null;
        }
    }
}
