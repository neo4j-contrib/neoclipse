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

/**
 * @author Davy Vanherbergen
 * 
 */
public class ColumnNode extends AbstractNode {

    private boolean _isForeignKey = false;

    private boolean _isPrimaryKey = false;
    
    private int _idx;

    private String _labelDecoration = null;

    private TableNode _parentTable;

   
    public ColumnNode(INode parent, String name, MetaDataSession session, TableNode parentTable, boolean showKeyLabels) {
    	this(parent, name, session, parentTable,showKeyLabels, 0);
    }
    public ColumnNode(INode parent, String name, MetaDataSession session, TableNode parentTable, boolean showKeyLabels, int pIdx) {
    	super(parent, name, session, "column");
        _parentTable = parentTable;
        _idx = pIdx;
        setImageKey("Images.ColumnNodeIcon");

        if (showKeyLabels) {
	        if (_parentTable.getPrimaryKeyNames().contains(_name)) {
	            _isPrimaryKey = true;
	            _imageKey = "Images.PrimaryKeyIcon";
	        }
	        // this has been disabled for now.
	        // foreign key determination turns out to be a real performance hog for oracle
	//        if (_parentTable.getForeignKeyNames().contains(_name)) {
	//            _isForeignKey = true;
	//            if (_isPrimaryKey) {
	//                _imageKey = "Images.PKForeignKeyIcon";
	//            } else {
	//                _imageKey = "Images.ForeignKeyIcon";
	//            }
	//        }
        }
    }


    public String getLabelDecoration() {

        return _labelDecoration;
    }


    public String getQualifiedParentTableName() {
        return _parentTable.getQualifiedName();
    }
    
    
    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.sqlexplorer.dbstructure.nodes.INode#getUniqueIdentifier()
     */
    public String getQualifiedName() {

        return _parentTable.getName() + "." + _name;
    }


    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.sqlexplorer.dbstructure.nodes.INode#getUniqueIdentifier()
     */
    public String getUniqueIdentifier() {

        return _parentTable.getQualifiedName() + "." + _name;
    }


    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.sqlexplorer.dbstructure.nodes.INode#isEndNode()
     */
    public boolean isEndNode() {

        return true;
    }


    public boolean isForeignKey() {

        return _isForeignKey;
    }


    public boolean isPrimaryKey() {

        return _isPrimaryKey;
    }


    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractNode#loadChildren()
     */
    public void loadChildren() {

        // noop
    }


    public void setLabelDecoration(String text) {

        _labelDecoration = text;
    }
    
    public int getIdx() {
    	return _idx;
    }
}
