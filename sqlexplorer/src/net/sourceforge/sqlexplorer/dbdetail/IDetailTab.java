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
package net.sourceforge.sqlexplorer.dbdetail;

import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;

import org.eclipse.swt.widgets.Composite;


/**
 * @author Davy Vanherbergen
 *
 */
public interface IDetailTab {

    
    public void setNode(INode node);
    
    /**
     * @return label text for this tab.
     */
    public String getLabelText();
    
    
    /**
     * @return tooltip text for this tab.
     */
    public String getLabelToolTipText();
    
    
    
    /**
     * Fill composite with information..
     * 
     * @param composite Composite
     */
    public void fillComposite(Composite composite);
    
    
    
    /**
     * @return string, usually  shown at bottom of tab
     */
    public String getStatusMessage();
    
    
    /**
     * Refresh tab
     */
    public void refresh();
}
