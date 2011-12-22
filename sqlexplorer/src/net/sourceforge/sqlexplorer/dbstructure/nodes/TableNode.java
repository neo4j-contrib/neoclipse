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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.sqlexplorer.dbproduct.MetaDataSession;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.util.ImageUtil;
import net.sourceforge.sqlexplorer.util.TextUtil;
import net.sourceforge.squirrel_sql.fw.sql.ITableInfo;
import net.sourceforge.squirrel_sql.fw.sql.TableColumnInfo;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

public class TableNode extends AbstractNode {

    private List<String> _columnNames;

    private List<String> _foreignKeyNames;

    private List<String> _primaryKeyNames;

    private ITableInfo _tableInfo;

    private List<String> _folderNames = new ArrayList<String>();
    
    private boolean isLoaded = false;
    
    private boolean _displaySchemaInName = false;

    /**
     * Create new database table node.
     * 
     * @param parent node
     * @param name of this node
     * @param sessionNode session for this node
     */
    public TableNode(INode parent, String name, MetaDataSession sessionNode, ITableInfo tableInfo, boolean displaySchemaInName) {
    	super(parent, name, sessionNode, tableInfo.getType());
        _tableInfo = tableInfo;
        _displaySchemaInName = displaySchemaInName;
        setImageKey("Images.TableNodeIcon");
    }

    /**
     * Return the table name to display in the structure node.  
     * @see net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractNode#getLabelText()
     */
    @Override
	public String getLabelText() {
    	if (_displaySchemaInName) {
    		return _tableInfo.getSchemaName()+'.'+super.getLabelText();
    	} else {
    		return super.getLabelText();	
    	}
	}

	private void addExtensionNodes() {

        String databaseProductName = getSession().getRoot().getDatabaseProductName().toLowerCase().trim();

        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint point = registry.getExtensionPoint("net.sourceforge.sqlexplorer", "node");
        IExtension[] extensions = point.getExtensions();

        for (int i = 0; i < extensions.length; i++) {

            IExtension e = extensions[i];

            IConfigurationElement[] ces = e.getConfigurationElements();

            for (int j = 0; j < ces.length; j++) {
                try {

                    // include only nodes that are attachted to the schema
                    // node..
                    String parent = ces[j].getAttribute("parent-node");
                    if (parent.indexOf("table") == -1) {
                        continue;
                    }

                    boolean isValidProduct = false;
                    String[] validProducts = ces[j].getAttribute("database-product-name").split(",");

                    // include only nodes valid for this database
                    for (int k = 0; k < validProducts.length; k++) {

                        String product = validProducts[k].toLowerCase().trim();

                        if (product.length() == 0) {
                            continue;
                        }

                        if (product.equals("*")) {
                            isValidProduct = true;
                            break;
                        }

                        String regex = TextUtil.replaceChar(product, '*', ".*");
                        if (databaseProductName.matches(regex)) {
                            isValidProduct = true;
                            break;
                        }

                    }

                    if (!isValidProduct) {
                        continue;
                    }

                    AbstractNode childNode = (AbstractNode) ces[j].createExecutableExtension("class");
                    
                    String imagePath = ces[j].getAttribute("icon");
                    String id = ces[j].getAttribute("id");
                    String fragmentId = id.substring(0, id.indexOf('.', 28));
                    if (imagePath != null && imagePath.trim().length() != 0) {
                        childNode.setImage(ImageUtil.getFragmentImage(fragmentId, imagePath));
                    }
                    
                    childNode.setParent(this);
                    childNode.setSession(_session);

                    addChildNode(childNode);
                    _folderNames.add(childNode.getName());
                    
                } catch (Throwable ex) {
                    SQLExplorerPlugin.error("Could not create child node", ex);
                }
            }
        }

    }


    /**
     * @return List of column names for this table.
     */
    public List<String> getColumnNames() {

        if (_columnNames == null) {

            _columnNames = new ArrayList<String>();
            try {
            	TableColumnInfo[] columns = _session.getMetaData().getColumnInfo(_tableInfo);
            	for (TableColumnInfo col : columns)
            		_columnNames.add(col.getColumnName());
            } catch (SQLException e) {
                SQLExplorerPlugin.error("Could not load column names", e);
            }

        }

        return _columnNames;
    }


    /**
     * @return List of column names for this table.
     */
    public List<String> getForeignKeyNames() {

        if (_foreignKeyNames == null) {

            _foreignKeyNames = new ArrayList<String>();
            try {
                ResultSet resultSet = _session.getMetaData().getImportedKeys(_tableInfo);
                while (resultSet.next()) {
                    _foreignKeyNames.add(resultSet.getString(4));
                }

            } catch (Exception e) {
                SQLExplorerPlugin.error("Could not load foreign key names", e);
            }

        }

        return _foreignKeyNames;
    }


    /**
     * @return List of column names for this table.
     */
    public List<String> getPrimaryKeyNames() {

        if (_primaryKeyNames == null) {

            _primaryKeyNames = new ArrayList<String>();
            try {
                ResultSet resultSet = _session.getMetaData().getPrimaryKeys(_tableInfo);
                while (resultSet.next()) {
                    _primaryKeyNames.add(resultSet.getString(4));
                }

            } catch (Exception e) {
                SQLExplorerPlugin.error("Could not load primary key names", e);
            }

        }

        return _primaryKeyNames;
    }


    /**
     * @return Qualified table name
     */
    public String getQualifiedName() {

        return _tableInfo.getQualifiedName();
    }


    /**
     * @return // TODO fix this for sql completion?
     */
    public String getTableDesc() {

        return getTableInfo().getQualifiedName();
    }


    /**
     * @return TableInfo for this node
     */
    public ITableInfo getTableInfo() {

        return _tableInfo;
    }


    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.sqlexplorer.dbstructure.nodes.INode#getUniqueIdentifier()
     */
    public String getUniqueIdentifier() {

        return getQualifiedName();
    }


    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.sqlexplorer.dbstructure.nodes.INode#isEndNode()
     */
    public boolean isEndNode() {

        return false;
    }


    /**
     * @return true if this node is a synonym
     */
    public boolean isSynonym() {

        return _tableInfo.getType().equalsIgnoreCase("SYNONYM");
    }


    /**
     * @return true if this node is a table
     */
    public boolean isTable() {

        return _tableInfo.getType().equalsIgnoreCase("TABLE");
    }


    /**
     * @return true if this node is a view
     */
    public boolean isView() {

        return _tableInfo.getType().equalsIgnoreCase("VIEW");
    }

    private void resetInfo()
    {
        try {
			ITableInfo[] tables = getSession().getMetaData().getTables(_tableInfo.getCatalogName(), _tableInfo.getSchemaName(), "%"+_tableInfo.getSimpleName()+"%", new String[] {_tableInfo.getType()}, null);
			for(ITableInfo info : tables)
			{
				if(_tableInfo.getQualifiedName().equals(info.getQualifiedName()))
				{
					_tableInfo = info;
					break;
				}
			}
			_folderNames.clear();
			_columnNames = null;
			_foreignKeyNames = null;
			_primaryKeyNames = null;
		} catch (SQLException e) {
            SQLExplorerPlugin.error("Could not load child nodes for " + getQualifiedName(), e);
		}
    	
    }

    /**
     * 
     * 
     * @see net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractNode#loadChildren()
     */
    public void loadChildren() {

    	if(this.isLoaded) {
    		resetInfo();
    		this.isLoaded = false;
    	}
    	this.isLoaded = true;
        try {            
            addExtensionNodes();
            
            // add column and index nodes if they don't exist yet. 
            
            ColumnFolderNode colNode = new ColumnFolderNode(this, _tableInfo);            
            if (!_folderNames.contains(colNode.getName())) {
                addChildNode(colNode);
            }
            
            IndexFolderNode indexNode = new IndexFolderNode(this, _tableInfo);
            if (!_folderNames.contains(indexNode.getName())) {
                addChildNode(indexNode);
            }
            
            
        } catch (Exception e) {
            SQLExplorerPlugin.error("Could not create child nodes for " + getName(), e);
        }

    }

}
