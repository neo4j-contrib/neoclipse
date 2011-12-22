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
package net.sourceforge.sqlexplorer.dbstructure.actions;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbproduct.Alias;
import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.dialogs.FilterStructureDialog;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.views.DatabaseStructureView;
import net.sourceforge.sqlexplorer.util.ImageUtil;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;

public class FilterStructureAction extends Action {

	public FilterStructureAction()
	{
		super();
		setToolTipText(Messages.getString("DatabaseStructureView.Actions.Filter"));
	}
	
	public ImageDescriptor getImageDescriptor() {
		return ImageUtil.getDescriptor("Images.FilterIcon");
	}

	public void run() {

		try {

			DatabaseStructureView view = SQLExplorerPlugin.getDefault().getDatabaseStructureView();
			FilterStructureDialog dialog = new FilterStructureDialog();

			Session session = view.getSession();
			if (session == null)
				return;
			Alias alias = session.getUser().getAlias();

			if (alias.getSchemaFilterExpression() != null
					&& alias.getSchemaFilterExpression().length() != 0) {
				dialog.setSchemaFilter(alias.getSchemaFilterExpression().split(","));
			}
			if (alias.getFolderFilterExpression() != null
					&& alias.getFolderFilterExpression().length() != 0) {
				dialog.setFolderFilter(alias.getFolderFilterExpression().split(","));
			}
			if (alias.getNameFilterExpression() != null
					&& alias.getNameFilterExpression().length() != 0) {
				dialog.setNameFilter(alias.getNameFilterExpression());
			}

			if (dialog.open() != Window.OK) {
				return;
			}

			String[] schemaFilter = dialog.getSchemaFilter();
			StringBuffer schemaFilterString = new StringBuffer("");
			String sep = "";
			if (schemaFilter != null) {
				for (int i = 0; i < schemaFilter.length; i++) {
					schemaFilterString.append(sep);
					schemaFilterString.append(schemaFilter[i]);
					sep = ",";
				}
			}
			alias.setSchemaFilterExpression(schemaFilterString.toString());

			String[] folderFilter = dialog.getFolderFilter();
			StringBuffer folderFilterString = new StringBuffer("");
			sep = "";
			if (folderFilter != null) {
				for (int i = 0; i < folderFilter.length; i++) {
					folderFilterString.append(sep);
					folderFilterString.append(folderFilter[i]);
					sep = ",";
				}
			}
			alias.setFolderFilterExpression(folderFilterString.toString());

			alias.setNameFilterExpression(dialog.getNameFilter());

			view.refreshSessionTrees(view.getSession());

		} catch (Exception e) {
			SQLExplorerPlugin.error("Error creating dialog", e);
		}
	}

}
