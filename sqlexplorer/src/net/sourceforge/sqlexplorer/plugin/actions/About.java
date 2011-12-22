/*
 * Copyright (C) 2002-2004 Andrea Mazzolini
 * andreamazzolini@users.sourceforge.net
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
package net.sourceforge.sqlexplorer.plugin.actions;

import net.sourceforge.sqlexplorer.dialogs.AboutDlg;
import net.sourceforge.sqlexplorer.util.ImageUtil;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * @author Davy Vanherbergen
 *
 */
public class About extends Action implements IWorkbenchWindowActionDelegate {

    private ImageDescriptor _image = ImageUtil.getDescriptor("Images.AboutDialog");
    
    private Shell _shell = null;
    
    public void dispose() {
        _shell = null;        
    }

    public ImageDescriptor getImageDescriptor() {
        return _image;
    }

    public void run(IAction action) {

        Dialog dialog = new AboutDlg(_shell);
        dialog.open();
    }

    public void selectionChanged(IAction action, ISelection selection) {
        // noop        
    }


    public void init(IWorkbenchWindow window) {
        _shell = window.getShell();
    }

}
