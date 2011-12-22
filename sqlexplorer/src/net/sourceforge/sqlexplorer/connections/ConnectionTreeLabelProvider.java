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

import java.text.SimpleDateFormat;
import java.util.Date;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbproduct.Alias;
import net.sourceforge.sqlexplorer.dbproduct.User;
import net.sourceforge.sqlexplorer.util.ImageUtil;
import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Label provider for database structure outline.
 * 
 * @author Davy Vanherbergen
 */
public class ConnectionTreeLabelProvider extends LabelProvider {

    private Image _inactiveAliasImage = ImageUtil.getImage("Images.AliasIcon");

    private Image _activeAliasImage = ImageUtil.getImage("Images.ConnectedAliasIcon");

    private Image _sessionImage = ImageUtil.getImage("Images.ConnectionIcon");

    public void dispose() {
        super.dispose();
        ImageUtil.disposeImage("Images.AliasIcon");
        ImageUtil.disposeImage("Images.ConnectedAliasIcon");
        ImageUtil.disposeImage("Images.ConnectionIcon");
    }

    /**
     * Return the image used for the given node.
     * 
     * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
     */
    public Image getImage(Object element) {
        if (element instanceof Alias) {
        	Alias alias = (Alias) element;

        	for (User user : alias.getUsers())
        		if (!user.getSessions().isEmpty())
        			return _activeAliasImage;
        	return _inactiveAliasImage;
        }
        return _sessionImage;
    }

    /**
     * Return the text to display
     * 
     * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
     */
    public String getText(Object element) {
        if (element instanceof Alias) {
        	Alias alias = (Alias) element;

            String label = alias.getName();
            int numSessions = 0;

        	for (User user : alias.getUsers())
        		numSessions += user.getConnections().size();
        	
            if (numSessions == 1)
                return label + " (" + numSessions + " " + Messages.getString("ConnectionsView.ConnectedAlias.single.Postfix") + ")";

            if (numSessions > 1)
                return label + " (" + numSessions + " " + Messages.getString("ConnectionsView.ConnectedAlias.multiple.Postfix") + ")";

            return label;

        } else if (element instanceof User) {
        	User user = (User)element;
        	return user.getUserName();
        	
        } else if (element instanceof SQLConnection) {
        	SQLConnection connection = (SQLConnection)element;
            String label;
            
            if (connection.getDescription() == null) {
	            SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");
            	label = Messages.getString("ConnectionsView.ConnectedAlias.ConnectedSince") + ' ' + fmt.format(new Date(connection.getCreatedTime()));
            } else
            	label = Messages.getString("ConnectionsView.ConnectedAlias.Connection") + ' ' + connection.getDescription();

            if (connection.isPooled())
            	label += ' ' + Messages.getString("ConnectionsView.ConnectedAlias.Pooled");
            return label;
        }
        
        return null;
    }
}
