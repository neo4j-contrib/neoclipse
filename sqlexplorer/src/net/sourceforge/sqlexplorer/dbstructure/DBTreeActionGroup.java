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
package net.sourceforge.sqlexplorer.dbstructure;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.sqlexplorer.dbstructure.actions.AbstractDBTreeContextAction;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.util.ImageUtil;
import net.sourceforge.sqlexplorer.util.TextUtil;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.ui.actions.ActionGroup;

/**
 * ActionGroup for Database Structure View. This group controls what context
 * menu actions are being shown for which node.
 * 
 * @author Davy Vanherbergen
 */
public class DBTreeActionGroup extends ActionGroup {

	private TreeViewer _treeViewer;

	/**
	 * Construct a new action group for a given database structure outline.
	 * 
	 * @param treeViewer
	 *            TreeViewer used for this outline.
	 */
	public DBTreeActionGroup(TreeViewer treeViewer) {

		_treeViewer = treeViewer;
		treeViewer.getTree().addMouseListener(new MouseAdapter() {

			public void mouseDoubleClick(MouseEvent e) {
				runDefault();

			}

		});
	}

	private void runDefault() {
		INode[] nodes = getSelectedNodes();
		if (nodes == null) {
			return;
		}
		AbstractDBTreeContextAction[] actions = getContextActions(nodes);

		for (AbstractDBTreeContextAction current : actions) {
			if(current.isDefault())
			{
				current.run();
			}
		}

	}

	/**
	 * Fill the node context menu with all the correct actions.
	 * 
	 * @see org.eclipse.ui.actions.ActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public void fillContextMenu(IMenuManager menu) {

		INode[] nodes = getSelectedNodes();
		if (nodes == null) {
			return;
		}
		AbstractDBTreeContextAction[] actions = getContextActions(nodes);

		for (AbstractDBTreeContextAction current : actions) {
			menu.add(current);
		}

	}

	private INode[] getSelectedNodes() {
		// find our target node..
		IStructuredSelection selection = (IStructuredSelection) _treeViewer
				.getSelection();

		// check if we have a valid selection
		if (selection == null) {
			return null;
		}

		List<INode> selectedNodes = new ArrayList<INode>();
		Iterator<?> it = selection.iterator();

		while (it.hasNext()) {
			Object object = it.next();
			if (object instanceof INode) {
				selectedNodes.add((INode) object);
			}
		}

		if (selectedNodes.size() == 0) {
			return null;
		}

		INode[] nodes = selectedNodes.toArray(new INode[selectedNodes.size()]);
		return nodes;
	}

	/**
	 * Loop through all extensions and add the appropriate actions.
	 * 
	 * Actions are selected by database product name, node type and
	 * availability.
	 * 
	 * @param nodes
	 *            currently selected nodes
	 * @return array of actions
	 */
	private AbstractDBTreeContextAction[] getContextActions(INode[] nodes) {

		String databaseProductName = nodes[0].getSession().getRoot()
				.getDatabaseProductName().toLowerCase().trim();
		String nodeType = nodes[0].getType().toLowerCase().trim();

		List<AbstractDBTreeContextAction> actions = new ArrayList<AbstractDBTreeContextAction>();

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint point = registry.getExtensionPoint(
				"net.sourceforge.sqlexplorer", "nodeContextAction");
		IExtension[] extensions = point.getExtensions();

		for (int i = 0; i < extensions.length; i++) {

			IExtension e = extensions[i];

			IConfigurationElement[] ces = e.getConfigurationElements();

			for (int j = 0; j < ces.length; j++) {
				try {

					boolean isValidProduct = false;
					boolean isValidNodeType = false;

					String id = ces[j].getAttribute("id");
					String[] validProducts = ces[j].getAttribute(
							"database-product-name").split(",");
					String[] validNodeTypes = ces[j].getAttribute("node-type")
							.split(",");
					String imagePath = ces[j].getAttribute("icon");

					// check if action is valid for current database product
					for (int k = 0; k < validProducts.length; k++) {

						String product = validProducts[k].toLowerCase().trim();

						if (product.length() == 0) {
							continue;
						}

						if (product.equals("*")) {
							isValidProduct = true;
							break;
						}

						String regex = TextUtil.replaceChar(product, '*', ".*");
						if (databaseProductName.matches(regex)) {
							isValidProduct = true;
							break;
						}

					}

					if (!isValidProduct) {
						continue;
					}

					// check if action is valid for current node type
					for (int k = 0; k < validNodeTypes.length; k++) {

						String type = validNodeTypes[k].toLowerCase().trim();

						if (type.length() == 0) {
							continue;
						}

						if (type.equals("*")) {
							isValidNodeType = true;
							break;
						}

						String regex = TextUtil.replaceChar(type, '*', ".*");
						if (nodeType.matches(regex)) {
							isValidNodeType = true;
							break;
						}

					}

					if (!isValidNodeType) {
						continue;
					}

					// check if the action thinks it is suitable..
					AbstractDBTreeContextAction action = (AbstractDBTreeContextAction) ces[j]
							.createExecutableExtension("class");
					action.setSelectedNodes(nodes);
					action.setTreeViewer(_treeViewer);

					String fragmentId = id.substring(0, id.indexOf('.', 28));

					if (imagePath != null && imagePath.trim().length() != 0) {
						action.setImageDescriptor(ImageUtil
								.getFragmentDescriptor(fragmentId, imagePath));
					}

					if (action.isAvailable()) {
						actions.add(action);
					}

				} catch (Throwable ex) {
					SQLExplorerPlugin.error("Could not create menu action", ex);
				}
			}
		}

		return actions.toArray(new AbstractDBTreeContextAction[actions.size()]);
	}

}
