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
package net.sourceforge.sqlexplorer.dbdetail.tab;

import net.sourceforge.sqlexplorer.dbdetail.IDetailTab;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;

import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;


/**
 * @author Davy Vanherbergen
 *
 */
public abstract class AbstractTab implements IDetailTab {

    private INode _node;
    
    public final void setNode(INode node) {
        _node = node;
    }
   
    public final INode getNode() {
        return _node;
    }
    
    public abstract void fillDetailComposite(Composite composite);
    
    public final void fillComposite(final Composite composite) {
        
        BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {

            public void run() {                
                fillDetailComposite(composite);                
            }
        });
        
    }

    public abstract String getLabelText();

    
    /* (non-Javadoc)
     * @see net.sourceforge.sqlexplorer.dbdetail.IDetailTab#getLabelToolTipText()
     */
    public String getLabelToolTipText() {        
        return getLabelText();
    }

    
    
}
