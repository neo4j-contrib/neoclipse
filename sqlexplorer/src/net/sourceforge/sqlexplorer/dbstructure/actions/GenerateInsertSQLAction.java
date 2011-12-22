/*
 * Copyright (C) 2009 Anthony Lazar
 * a_lazar@users.sourceforge.net
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

package net.sourceforge.sqlexplorer.dbstructure.actions;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.nodes.ColumnNode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.TableNode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditorInput;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;

/**
 * Generate a new insert SQL statement for the selected table / columns.
 * 
 * @author Anthony Lazar
 * 
 */
public class GenerateInsertSQLAction extends AbstractDBTreeContextAction {

    private static final ImageDescriptor _image = null;


    /**
     * @return query string for selected columns insert
     */
    private String createColumnInsert() {

        StringBuffer query1 = new StringBuffer("insert into ");
        StringBuffer query2 = new StringBuffer(" values (");
        String sep = "";
        String table = "";

        for (int i = 0; i < _selectedNodes.length; i++) {

            INode node = _selectedNodes[i];

            if (node instanceof ColumnNode) {

                ColumnNode column = (ColumnNode) node;
                
                if (table.length() == 0) {
                    table = column.getQualifiedParentTableName();
                }
                
                
                if(sep.length() == 0) {
                	query1.append(table);
                	query1.append(" (");
                }

                if (column.getQualifiedParentTableName().equals(table)) {
                    query1.append(sep);
                    query1.append(column.getName());
                    
                    query2.append(sep);
                    query2.append("null");
                    sep = ", ";
                }
            }
        }

        query1.append(")");
        query2.append(")");
        query1.append(query2);

        return query1.toString();

    }


    /**
     * @return query string for full table insert
     */
    private String createTableInsert() {

        TableNode node = (TableNode) _selectedNodes[0];

        StringBuffer query1 = new StringBuffer("insert into ");
        query1.append(node.getQualifiedName());
        query1.append(" (");
        StringBuffer query2 = new StringBuffer(" values (");
        String sep = "";

        for (String column : node.getColumnNames()) {

            query1.append(sep);
            query1.append(column);
            
            query2.append(sep);
            query2.append("null");
            sep = ", ";
        }

        query1.append(")");
        query2.append(")");
        query1.append(query2);

        return query1.toString();
    }


    /**
     * Custom image for generate SQL action
     * 
     * @see org.eclipse.jface.action.IAction#getImageDescriptor()
     */
    public ImageDescriptor getImageDescriptor() {

        return _image;
    }


    /**
     * Set the text for the menu entry.
     * 
     * @see org.eclipse.jface.action.IAction#getText()
     */
    public String getText() {

        return Messages.getString("DatabaseStructureView.Actions.GenerateInsertSQL");
    }


    /**
     * Action is always available.
     * 
     * @see net.sourceforge.sqlexplorer.dbstructure.actions.AbstractDBTreeContextAction#isAvailable()
     */
    public boolean isAvailable() {

        if (_selectedNodes.length == 0) {
            return false;
        }

        if (_selectedNodes[0] instanceof ColumnNode) {
        	return ((TableNode)(_selectedNodes[0].getParent().getParent())).isTable();
        }

        if (_selectedNodes[0] instanceof TableNode) {
            return ((TableNode)_selectedNodes[0]).isTable();
        }

        return false;
    }


    /**
     * Generate select statement
     * 
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run() {

        try {

            String query = null;

            if (_selectedNodes[0] instanceof ColumnNode) {
                query = createColumnInsert();
            }

            if (_selectedNodes[0] instanceof TableNode) {
                query = createTableInsert();
            }

            if (query == null) {
                return;
            }

            SQLEditorInput input = new SQLEditorInput("SQL Editor (" + SQLExplorerPlugin.getDefault().getEditorSerialNo()
                    + ").sql");
            input.setUser(_selectedNodes[0].getSession().getUser());
            IWorkbenchPage page = SQLExplorerPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();

            SQLEditor editorPart = (SQLEditor) page.openEditor((IEditorInput) input, SQLEditor.class.getName());
            editorPart.setText(query);

        } catch (Throwable e) {
            SQLExplorerPlugin.error("Could generate sql.", e);
        }
    }
}

