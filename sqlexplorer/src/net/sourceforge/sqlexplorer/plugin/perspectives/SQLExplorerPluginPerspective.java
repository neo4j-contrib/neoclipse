package net.sourceforge.sqlexplorer.plugin.perspectives;

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

import java.util.Iterator;
import java.util.List;

import net.sourceforge.sqlexplorer.plugin.views.SqlexplorerViewConstants;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * Provides an Eclipse perspective for this plugin.
 * 
 * @author Macon Pegram
 * @modified John Spackman
 */
public class SQLExplorerPluginPerspective implements IPerspectiveFactory {

    /**
     * Creates the default initial layout for this plugin. This method fufills
     * the contract for the IPerspectiveFactory interface
     * 
     * @param IPageLayout
     */
    public void createInitialLayout(IPageLayout layout) {
        defineActions(layout);
        defineLayout(layout);
    }


    /**
     * Define the actions and views you want to make available from the menus.
     * 
     * @param IPageLayout
     */
    private void defineActions(IPageLayout layout) {
        // You can add "new" wizards" here if you want, but none seem applicable
        // in the case of this plugin

        // Grab the list of all available views defined in the constants class
        List<String> views = SqlexplorerViewConstants.getInstance().getFullViewList();
        Iterator<String> iterator = views.iterator();

        // Iterate through those views and add them to the Show Views menu.
        while (iterator.hasNext())
            layout.addShowViewShortcut((String) iterator.next());
    }


    /**
     * Controls the physical default layout of the perspective
     * 
     * @param IPageLayout
     */
    private void defineLayout(IPageLayout layout) {
        
        layout.setEditorAreaVisible(true);
        String editorArea = layout.getEditorArea();       

        IFolderLayout topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, 0.3f, editorArea);
        topLeft.addView(SqlexplorerViewConstants.SQLEXPLORER_CONNECTIONS);
        topLeft.addView("org.eclipse.ui.navigator.ProjectExplorer");

        IFolderLayout bottomLeft = layout.createFolder("bottomLeft", IPageLayout.BOTTOM, 0.3f, "topLeft");
        bottomLeft.addView(SqlexplorerViewConstants.SQLEXPLORER_SQLHISTORY);
        
        IFolderLayout main = layout.createFolder("right", IPageLayout.RIGHT, 0.70f, editorArea);
        main.addView(SqlexplorerViewConstants.SQLEXPLORER_DBSTRUCTURE);
        
        IFolderLayout bottom = layout.createFolder("bottom", IPageLayout.BOTTOM, 0.85f, editorArea);
        bottom.addView(SqlexplorerViewConstants.SQLEXPLORER_DBDETAIL);
        
        IFolderLayout bottomRight = layout.createFolder("bottomRight", IPageLayout.BOTTOM, 0.90f, "right");
        bottomRight.addView(IPageLayout.ID_PROGRESS_VIEW);
    }

}
