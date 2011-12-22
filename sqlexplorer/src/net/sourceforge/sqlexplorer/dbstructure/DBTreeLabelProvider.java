/*
 * Copyright (C) 2006 Davy Vanherbergen
 * dvanherbergen@users.sourceforge.net
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
package net.sourceforge.sqlexplorer.dbstructure;

import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.util.ImageUtil;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Label provider for database structure outline.
 * 
 * @author Davy Vanherbergen
 */
public class DBTreeLabelProvider extends LabelProvider {

    private Image _defaultNodeImage = ImageUtil.getImage("Images.DefaultNodeIcon");

    private Image _defaultParentNodeImage = ImageUtil.getImage("Images.DefaultParentNodeIcon");


    public void dispose() {

        super.dispose();
        ImageUtil.disposeImage("Images.DefaultNodeIcon");
        ImageUtil.disposeImage("Images.DefaultParentNodeIcon");

    }


    /**
     * Return the image used for the given INode. If the INode does not have an
     * image, default images are returned.
     * 
     * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
     */
    public Image getImage(Object element) {

        INode node = (INode) element;

        // return expanded image if node is expanded and we have an image
        if (node.isExpanded() && node.getExpandedImage() != null && node.getChildNodes() != null
                && node.getChildNodes().length != 0) {
            return node.getExpandedImage();
        }

        // return custom image
        if (node.getImage() != null) {
            return node.getImage();
        }

        // return one of the default images
        if (node.hasChildNodes()) {
            return _defaultParentNodeImage;
        } else {
            return _defaultNodeImage;
        }

    }


    /**
     * Return the text to display the INode.
     * 
     * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
     */
    public String getText(Object element) {

        INode node = (INode) element;
        String text = node.getLabelText();

        // return default if no label is provided
        if (text == null) {
            text = node.toString();
        }

        if (node.getLabelDecoration() != null) {
            text = text + " [" + node.getLabelDecoration() + "]";
        }

        return text;
    }

}
