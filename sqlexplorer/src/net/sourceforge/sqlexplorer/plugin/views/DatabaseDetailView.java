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
package net.sourceforge.sqlexplorer.plugin.views;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbdetail.DetailTabManager;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 * Database Detail View. Provides a detailed view on the selection in the
 * database structure view.
 * 
 * @author Davy Vanherbergen
 */
public class DatabaseDetailView extends ViewPart {

    private Composite _composite;


    /**
     * Create our detail view. If the detail view is recreated after the
     * structure view was already available, the currently selected node in the
     * structure view will be displayed.
     * 
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent) {

        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, SQLExplorerPlugin.PLUGIN_ID + ".DatabaseDetailView");
        
        // create new composite to display information
        _composite = new Composite(parent, SWT.NULL);
        _composite.setLayout(new FillLayout());

        // initialize default message
        setSelectedNode(null);
        
        // synchronize with structure view that may already exist
        DatabaseStructureView structureView = SQLExplorerPlugin.getDefault().getDatabaseStructureView();

        if (structureView != null) {
            structureView.synchronizeDetailView(this);
        }

    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPart#setFocus()
     */
    public void setFocus() {
        // noop
    }


    /**
     * Show the details for the given node. If node is null, a default message
     * will be displayed.
     * 
     * @param node INode.
     */
    public void setSelectedNode(INode node) {

        // clean first..
        Composite parent = _composite.getParent();
        _composite.dispose();
        _composite = new Composite(parent, SWT.NULL);
        _composite.setLayout(new FillLayout());

        if (node == null) {

            // add default message
            String message = Messages.getString("DatabaseDetailView.NoSelection");

            Label label = new Label(_composite, SWT.FILL);
            label.setText(message);
            label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        } else {

            DetailTabManager.createTabs(_composite, node);
        }

        _composite.layout();
        _composite.getParent().layout();
        _composite.redraw();
    }

}
