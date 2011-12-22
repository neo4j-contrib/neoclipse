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
package net.sourceforge.sqlexplorer.dbstructure.nodes;

import net.sourceforge.sqlexplorer.dbproduct.MetaDataSession;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

/**
 * All nodes displayed in the database structure outline should implement this
 * interface.
 * 
 * @author Davy Vanherbergen
 */
public interface INode {

    public void fillDetailComposite(Composite composite);


    /**
     * @return All child nodes of this node.
     */
    public INode[] getChildNodes();


    /**
     * The returned image is displayed in the database structure outline for
     * this node when the node is expanded.
     * 
     * @return Image to be used for this node.
     */
    public Image getExpandedImage();


    /**
     * The returned image is displayed in the database structure outline for
     * this node.
     * 
     * @return Image to be used for this node.
     */
    public Image getImage();


    /**
     * @return text to append after node label.
     */

    public String getLabelDecoration();


    /**
     * @return Text that is displayed for this node in the treeviewer.
     */
    public String getLabelText();


    /**
     * @return Simple name for this node.
     */
    public String getName();


    /**
     * @return Parent node of this node.
     */
    public INode getParent();


    /**
     * @return Qualified name for this node.
     */
    public String getQualifiedName();


    public String getSchemaOrCatalogName();


    /**
     * @return SessionTreeNode for this node.
     */
    public MetaDataSession getSession();


    /**
     * @return type of this node, e.g. Database, schema, catalog, table, view,
     *         ...
     */
    public String getType();


    /**
     * @return Qualified path for this node.
     */
    public String getUniqueIdentifier();


    /**
     * @return true if the node has children.
     */
    public boolean hasChildNodes();


    /**
     * @return true if this node cannot have children..
     */
    public boolean isEndNode();


    /**
     * @return true if node is expanded.
     */
    public boolean isExpanded();


    /**
     * Refresh. This will clear the nodes' children and reload them.
     */
    public void refresh();


    /**
     * Set expanded state of element
     */
    public void setExpanded(boolean expanded);
}
