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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.sqlexplorer.dbproduct.MetaDataSession;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.util.ImageUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

/**
 * Abstract implementation of INode. Extend this class to create your own node
 * types.
 * 
 * @author Davy Vanherbergen
 */
public abstract class AbstractNode implements INode {

    private static final Log _logger = LogFactory.getLog(AbstractNode.class);

    protected List<INode> _children = new ArrayList<INode>();

    protected boolean _childrenLoaded = false;

    protected Image _image;

    protected String _imageKey = "Images.DefaultNodeImage";

    protected String _expandedImageKey = null;

    protected boolean _isExpanded = false;

    protected String _name;

    protected INode _parent;

    protected MetaDataSession _session;

    protected String _type;

    public AbstractNode(String name) {
    	this._name = name;
    }

    public AbstractNode(String name, MetaDataSession session) {
    	this._name = name;
    	this._session = session;
    }

    public AbstractNode(INode parent, String name, MetaDataSession session, String type) {
    	this._parent = parent;
    	this._name = name;
    	this._session = session;
    	this._type = type;
    }

    /**
     * Adds a new child node to this node
     * 
     * @param child node
     */
    public final void addChildNode(INode childNode) {

        _children.add(childNode);
    }


    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.sqlexplorer.dbstructure.nodes.INode#fillDetailComposite(org.eclipse.swt.widgets.Composite)
     */
    public void fillDetailComposite(Composite composite) {

        // noop
    }


    /**
     * Get an iterator to all child nodes. If child nodes haven't been loaded
     * yet, loading is triggered.
     * 
     * @return Iterator of child elements
     */
    public final Iterator<INode> getChildIterator() {

        load();
        return _children.iterator();
    }


    /**
     * Get all the children of this node. If child nodes haven't been loaded
     * yet, loading is triggered.
     * 
     * @return All child nodes of this node.
     * @see net.sourceforge.sqlexplorer.db.INode#getChildren()
     */
    public final INode[] getChildNodes() {
        load();
        return (INode[]) _children.toArray(new INode[_children.size()]);
    }


    /**
     * Override this method to implement custom sorting of child nodes.
     */
    public Comparator<INode> getComparator() {

        return new Comparator<INode>() {

            public int compare(INode arg0, INode arg1) {

                if (arg0 == null || arg1 == null) {
                    return 0;
                }
                String name0 = arg0.getLabelText();
                String name1 = arg1.getLabelText();

                if (name0 == null || name1 == null) {
                    return 0;
                }

                return name0.compareTo(name1);
            }

        };
    }


    /**
     * Override this method to change the image that is displayed for this node
     * in the database structure outline.
     */
    public final Image getExpandedImage() {

        if (_expandedImageKey == null) {
            return null;
        }
        return ImageUtil.getImage(_expandedImageKey);
    }


    /**
     * Override this method to change the image that is displayed for this node
     * in the database structure outline.
     */
    public Image getImage() {

        if (_image != null) {
            return _image;
        }
        if (_imageKey == null) {
            return _image;
        }
        return ImageUtil.getImage(_imageKey);
    }


    public String getLabelDecoration() {

        return null;
    }


    /**
     * Override this method to change the text that is displayed in the database
     * structure outline for this node.
     */
    public String getLabelText() {

        return getName();
    }


    /**
     * @return simple name for this node.
     */
    public String getName() {
        if (_name == null) {
            return "<null>";
        }
        return _name;
    }


    /**
     * Get the parent of this node.
     * 
     * @return Parent node of this node.
     * @see net.sourceforge.sqlexplorer.db.INode#getControlParent()
     */
    public final INode getParent() {

        return _parent;
    }


    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.sqlexplorer.dbstructure.nodes.INode#getUniqueIdentifier()
     */
    public String getQualifiedName() {

        return getName();
    }


    public String getSchemaOrCatalogName() {

        INode node = this;
        while (!(node.getType().equalsIgnoreCase("schema") || node.getType().equalsIgnoreCase("catalog"))) {
            node = node.getParent();
            if (node == null) {
                return null;
            }
        }
        return node.getName();
    }


    /**
     * @return SessionTreeNode for this node.
     */
    public MetaDataSession getSession() {
        return _session;
    }


    public String getType() {

        return _type;
    }


    /**
     * Implement this method to return a unique identifier for this node. It is
     * used to identify the node in the detail cache.
     * 
     * @see net.sourceforge.sqlexplorer.dbstructure.nodes.INode#getUniqueIdentifier()
     */
    public String getUniqueIdentifier() {

        return getParent().getQualifiedName() + "." + getQualifiedName();
    }


    /**
     * Checks if this node has children. If child nodes haven't been loaded yet,
     * this method always returns true. This defers the loading of metadata used
     * in the database structure outline until it is actually required.
     * 
     * @return true if this node has children.
     */
    public final boolean hasChildNodes() {

        if (!_childrenLoaded && !isEndNode()) {
            return true;
        }

        if (_children == null || _children.size() == 0) {
            return false;
        }

        return true;
    }


    /**
     * Returns true. Override this method to return false if your node cannot
     * have any children. This will avoid the twistie being displayed in the
     * database structure outline for nodes that cannot have children.
     * 
     * @see net.sourceforge.sqlexplorer.dbstructure.nodes.INode#isEndNode()
     */
    public boolean isEndNode() {

        return false;
    }


    public boolean isExpanded() {

        return _isExpanded;
    }


    /**
     * Loads all the children for this node if they haven't been loaded yet.
     */
    public final void load() {

    	synchronized(this)
    	{
	        if (!_childrenLoaded) {
	
	            try {
	
	                if (_logger.isDebugEnabled()) {
	                    _logger.debug("Loading child nodes for " + _name);
	                }
	
	                loadChildren();
	                Comparator<INode> comp = getComparator();
	                if (comp != null) {
	                    Collections.sort(_children, getComparator());
	                }
	                _childrenLoaded = true;
	
	            } catch (AbstractMethodError e) {
	
	                SQLExplorerPlugin.error("Could not load child nodes for " + _name, e);
	
	            } catch (Throwable e) {
	
	                SQLExplorerPlugin.error("Could not load child nodes for " + _name, e);
	
	            }
	        }
    	}
    }


    /**
     * Load all the children of this node here. Do not call this method, but use
     * load() instead.
     */
    public abstract void loadChildren();


    /**
     * Refresh. This will clear the nodes' children and reload them. It will
     * also update the dictionary for this node & descendants
     */
    public final void refresh() {

    	for(INode child : _children) {
    		child.refresh();
    	}
        _children.clear();
        _childrenLoaded = false;
        load();

    }


    public final void setExpanded(boolean expanded) {
        _isExpanded = expanded;
    }

    public void setImage(Image image) {
        this._image = image;
    }
    
    protected void setImageKey(String imageKey) {
    	this._imageKey = imageKey;
    }
    
    public String getExpandedImageKey() {
		return _expandedImageKey;
	}

	public String get_imageKey() {
		return _imageKey;
	}

	protected void setExpandedImageKey(String expandedImageKey) {
    	this._expandedImageKey = expandedImageKey;
    }


    /**
     * Set parent node for this node.
     * 
     * @param parent
     */
    public final void setParent(INode parent) {

        this._parent = parent;
    }


    /**
     * Set sessiontreenode for this node
     * 
     * @param session
     */
    public final void setSession(MetaDataSession session) {

        this._session = session;
    }


    public void setType(String type) {

        this._type = type;
    }


    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {

        return getName();
    }
}
