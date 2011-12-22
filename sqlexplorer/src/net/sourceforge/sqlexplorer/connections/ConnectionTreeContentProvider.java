/*
 * Copyright (C) 2006 SQL Explorer Development Team
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
package net.sourceforge.sqlexplorer.connections;

import net.sourceforge.sqlexplorer.dbproduct.Alias;
import net.sourceforge.sqlexplorer.dbproduct.AliasManager;
import net.sourceforge.sqlexplorer.dbproduct.User;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Content provider for database structure outline.
 * 
 * @author Davy Vanherbergen
 */
public class ConnectionTreeContentProvider implements ITreeContentProvider {
	
    /**
     * Cleanup. We don't do anything here.
     * 
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose() {
        // noop
    }


    /**
     * Return all the children
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object parentElement) {

        if (parentElement instanceof AliasManager) {
        	AliasManager aliases = (AliasManager)parentElement;
        
            Object[] children = aliases.getAliases().toArray();
            return children;
            
        } else if (parentElement instanceof Alias){
        	Alias alias = (Alias)parentElement;
            
        	Object[] children = alias.getUsers().toArray();
        	return children;
        	
        } else if (parentElement instanceof User) {
        	User user = (User)parentElement;
        	
        	return user.getConnections().toArray();
        }
        
        return null;
    }


    /**
     * Return all the children of an INode element.
     * 
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements(Object inputElement) {
        return getChildren(inputElement);
    }


    /**
     * Return the parent of an element.
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
     */
    public Object getParent(Object element) {

        // this is root node
        if (element instanceof AliasManager)
            return null;
            
        // return alias
        else if (element instanceof Alias)
            return SQLExplorerPlugin.getDefault().getAliasManager();
        
        else if (element instanceof User)
        	return ((User)element).getAlias();

        else if (element instanceof SQLConnection)
        	return ((SQLConnection)element).getUser();
        
        return null;
    }

    /**
     * Returns true if the INode has children.
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
     */
    public boolean hasChildren(Object element) {
        Object[] tmp = getChildren(element);
        
        return tmp != null && tmp.length != 0;    
    }

    /**
     * We don't do anything here..
     * 
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
     *      java.lang.Object, java.lang.Object)
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // noop
    }

}
