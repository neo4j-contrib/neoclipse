package net.sourceforge.sqlexplorer.sqlpanel.actions;
/*
 * Copyright (C) 2004 Alexandre Telles <atelles@dev.java.net> 
 * and Aadi Deshpande <cilquirm@dev.java.net>
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
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.util.ImageUtil;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TabItem;

public class CloseSQLResultTab extends Action {

	private ImageDescriptor img=ImageUtil.getDescriptor("Images.CloseIcon");
	/**
	 * Holds a reference to the TableItem we should close
	 */
	private TabItem tabItem;
	
	/**
	 * Default Constructor
	 *
	 */
	public CloseSQLResultTab(){
		super();
	}
	
	/**
	 * Constructor
	 * @param _tabItem
	 */
	public CloseSQLResultTab(TabItem _tabItem){
		super();
		tabItem = _tabItem;
	}
	
    
	public void run() {
        
        BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {

            public void run() {
                tabItem.dispose();
            }
        });
    }
	
	public ImageDescriptor getHoverImageDescriptor(){
		return img;
    }

	public ImageDescriptor getImageDescriptor(){
		return img;
    }

	public String getToolTipText(){
		return Messages.getString("sqlresults.close");  //$NON-NLS-1$
	}
	
}
