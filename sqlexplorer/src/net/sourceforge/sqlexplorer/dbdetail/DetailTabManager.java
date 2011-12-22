package net.sourceforge.sqlexplorer.dbdetail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbdetail.tab.ColumnInfoTab;
import net.sourceforge.sqlexplorer.dbdetail.tab.ColumnPriviligesTab;
import net.sourceforge.sqlexplorer.dbdetail.tab.ConnectionInfoTab;
import net.sourceforge.sqlexplorer.dbdetail.tab.ExportedKeysTab;
import net.sourceforge.sqlexplorer.dbdetail.tab.ImportedKeysTab;
import net.sourceforge.sqlexplorer.dbdetail.tab.IndexesTab;
import net.sourceforge.sqlexplorer.dbdetail.tab.PreviewTab;
import net.sourceforge.sqlexplorer.dbdetail.tab.PrimaryKeysTab;
import net.sourceforge.sqlexplorer.dbdetail.tab.PriviligesTab;
import net.sourceforge.sqlexplorer.dbdetail.tab.RowCountTab;
import net.sourceforge.sqlexplorer.dbdetail.tab.RowIdsTab;
import net.sourceforge.sqlexplorer.dbdetail.tab.TableInfoTab;
import net.sourceforge.sqlexplorer.dbdetail.tab.VersionsTab;
import net.sourceforge.sqlexplorer.dbproduct.MetaDataSession;
import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.dbstructure.nodes.DatabaseNode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.TableNode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.util.TextUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * Controls creation of detail tabs for all nodes. All detail tabs are cached.
 * 
 * @author Davy Vanherbergen
 */
public class DetailTabManager {

	private static class NodeCache {
		List<IDetailTab> tabs = null;
		Map<String, NodeCache> children = new HashMap<String, NodeCache>();
		
		public List<IDetailTab> getTabs() {
			return this.tabs;
		}
		
		public void setTabs(List<IDetailTab> tabs) {
			this.tabs = tabs;
		}
		
		public NodeCache getChild(INode pNode) {
			NodeCache child = children.get(pNode.getName());
			if(child == null) {
				child = new NodeCache();
				children.put(pNode.getName(), child);
			}
			return child;
		}
		public void reset() {
			this.tabs = null;
			for(NodeCache current : this.children.values()) {
				current.reset();
			}
			this.children.clear();
		}
	}
    private static String _activeTabName = null;

    private static final Log _logger = LogFactory.getLog(DetailTabManager.class);

    private static final Map<Session, NodeCache> _sessionTabCache = new HashMap<Session, NodeCache>();


    /**
     * Clear the detail tab cache for a given node.
     * 
     * @param node INode to remove from cache.
     */
    public static void clearCacheForNode(INode node) {

        if (_logger.isDebugEnabled()) {
            _logger.debug("Clearing tab cache for: " + node.getUniqueIdentifier());
        }

        NodeCache nodeCache = _sessionTabCache.get(node.getSession());
        if(nodeCache != null)
        {
        	nodeCache = findNodeCache(node, nodeCache);
        	nodeCache.reset();
        }

    }


    /**
     * Clear cache of a given session. This method is called when a session is
     * closed or when the database node is refreshed.
     * 
     * @param session SessionTreeNode
     */
    public static void clearCacheForSession(MetaDataSession session) {

        if (_logger.isDebugEnabled()) {
            _logger.debug("Clearing tab cache for: " + session.toString());
        }

        _sessionTabCache.remove(session);
    }


    /**
     * Creates all the tabs in the detail pane to display the information for a
     * given node.
     * 
     * @param composite
     * @param node
     */
    public static void createTabs(Composite composite, INode node) {

        List<IDetailTab> tabs = getTabs(node);

        if (tabs == null || tabs.size() == 0) {
            // no detail found..

            Label label = new Label(composite, SWT.FILL);
            label.setText(Messages.getString("DatabaseDetailView.Tab.Unavailable") + " " + node.getLabelText());
            label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

            return;
        }

        // create tabs
        TabFolder tabFolder = new TabFolder(composite, SWT.NULL);

        // only init tabs when the tab becomes active
        tabFolder.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {

                // noop
            }


            public void widgetSelected(SelectionEvent e) {

                TabItem tabItem = (TabItem) e.item;
                IDetailTab tab = (IDetailTab) tabItem.getData();
                if (tab != null) {

                    // create composite on tab and fill it..
                    Composite detailComposite = new Composite(tabItem.getParent(), SWT.FILL);
                    tabItem.setControl(detailComposite);
                    detailComposite.setLayout(new FillLayout());
                    tab.fillComposite(detailComposite);
                    detailComposite.layout();

                    // store tab name, so we can reselect when other node is
                    // chosen
                    DetailTabManager.setActiveTabName(tabItem.getText());
                }
            }

        });

        // add tabs to folder
        int tabIndex = 0;
        for(IDetailTab detailTab : tabs) {

            // create tab
            TabItem tabItem = new TabItem(tabFolder, SWT.NULL);
            tabItem.setText(detailTab.getLabelText());
            tabItem.setToolTipText(detailTab.getLabelToolTipText());

            // store tab so we can fill later
            tabItem.setData(detailTab);

            // reselect same tab as was previous selected
            if (tabItem.getText() != null && _activeTabName != null) {
                if (tabItem.getText().equals(_activeTabName)) {
                    tabFolder.setSelection(tabIndex);
                }
            }

            tabIndex++;
        }

        // load data for active tab, default to first one if none is selected
        tabIndex = tabFolder.getSelectionIndex();
        if (tabIndex == -1) {
            tabIndex = 0;
        }

        TabItem tabItem = tabFolder.getItem(tabIndex);
        if (tabItem != null) {
            Composite detailComposite = new Composite(tabItem.getParent(), SWT.FILL);
            tabItem.setControl(detailComposite);
            detailComposite.setLayout(new FillLayout());
            IDetailTab tab = (IDetailTab) tabItem.getData();
            tab.fillComposite(detailComposite);
            detailComposite.layout();
        }

        tabFolder.layout();

    }


    /**
     * Returns a list of all available tabs for a given node. These tabs can be
     * standard or plugin tabs.
     * 
     * @param node for which to find tabs.
     * @return List of tabs
     */
    private static List<IDetailTab> createTabs(INode node) {

        if (_logger.isDebugEnabled()) {
            _logger.debug("Creating tabs for: " + node.getUniqueIdentifier());
        }

        ArrayList<IDetailTab> tabList = new ArrayList<IDetailTab>();

        // create connection info tab if needed
        if (node instanceof DatabaseNode) {

            IDetailTab dbTab = new ConnectionInfoTab();
            dbTab.setNode(node);
            tabList.add(dbTab);

        }

        // create our basic table tabs
        if (node instanceof TableNode) {

            IDetailTab tab1 = new ColumnInfoTab();
            IDetailTab tab2 = new TableInfoTab();
            IDetailTab tab3 = new PreviewTab();
            IDetailTab tab4 = new RowCountTab();
            IDetailTab tab5 = new PrimaryKeysTab();
            IDetailTab tab6 = new ExportedKeysTab();
            IDetailTab tab7 = new ImportedKeysTab();
            IDetailTab tab8 = new IndexesTab();
            IDetailTab tab9 = new PriviligesTab();
            IDetailTab tab10 = new ColumnPriviligesTab();
            IDetailTab tab11 = new RowIdsTab();
            IDetailTab tab12 = new VersionsTab();

            tab1.setNode(node);
            tab2.setNode(node);
            tab3.setNode(node);
            tab4.setNode(node);
            tab5.setNode(node);
            tab6.setNode(node);
            tab7.setNode(node);
            tab8.setNode(node);
            tab9.setNode(node);
            tab10.setNode(node);
            tab11.setNode(node);
            tab12.setNode(node);

            tabList.add(tab1);
            tabList.add(tab2);
            tabList.add(tab3);
            tabList.add(tab4);
            tabList.add(tab5);
            tabList.add(tab6);
            tabList.add(tab7);
            tabList.add(tab8);
            tabList.add(tab9);
            tabList.add(tab10);
            tabList.add(tab11);
            tabList.add(tab12);

        }

        // create extension point tabs
        String databaseProductName = node.getSession().getRoot().getDatabaseProductName().toLowerCase().trim();
        String nodeType = node.getType().toLowerCase().trim();

        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint point = registry.getExtensionPoint("net.sourceforge.sqlexplorer", "nodeDetailTab");
        IExtension[] extensions = point.getExtensions();

        for (int i = 0; i < extensions.length; i++) {

            IExtension e = extensions[i];

            IConfigurationElement[] ces = e.getConfigurationElements();

            for (int j = 0; j < ces.length; j++) {
                try {

                    boolean isValidProduct = false;
                    boolean isValidNodeType = false;

                    String[] validProducts = ces[j].getAttribute("database-product-name").split(",");
                    String[] validNodeTypes = ces[j].getAttribute("node-type").split(",");

                    // check if tab is valid for current database product
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

                    // check if tab is valid for current node type
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

                    // add tab to list
                    IDetailTab tab = (IDetailTab) ces[j].createExecutableExtension("class");
                    tab.setNode(node);

                    tabList.add(tab);

                } catch (Throwable ex) {
                    SQLExplorerPlugin.error("Could not create menu action", ex);
                }
            }
        }

        return tabList;
    }


    /**
     * This method returns the tabs for a given node from the cache. Tabs are
     * cached per sessionTreeNode. If the tabs don't exist in the cache, they
     * are created.
     * 
     * @param node INode for which to retrieve tabs.
     * @return List of tabs.
     */
    private static List<IDetailTab> getTabs(INode node) {

        if (_logger.isDebugEnabled()) {
            _logger.debug("Loading tabs for: " + node.getUniqueIdentifier());
        }

        NodeCache nodeCache = _sessionTabCache.get(node.getSession());

        if (nodeCache == null) {
            // create cache
        	nodeCache = new NodeCache();
            _sessionTabCache.put(node.getSession(), nodeCache);
        }
        
        nodeCache = findNodeCache(node, nodeCache);
        
        List<IDetailTab> tabs = nodeCache.getTabs();

        if (tabs == null) {
            // create tabs & store for later
            tabs = createTabs(node);
            nodeCache.setTabs(tabs);
        }

        // display parent details if we have nothing for this node..
        if ((tabs == null || tabs.size() == 0) && node.getParent() != null) {
            return getTabs(node.getParent());
        }

        return tabs;
    }


    private static NodeCache findNodeCache(INode pNode,	NodeCache pNodeCache) {
    	NodeCache found = 
    		pNode.getParent() == null ? 
    			pNodeCache : 
    			findNodeCache(pNode.getParent(), pNodeCache);
    	
    	return found.getChild(pNode);
	}


	/**
     * Store the name of the active tab, so that we can reselect it when a
     * different node is selected.
     * 
     * @param name tab label
     */
    public static void setActiveTabName(String name) {

        _activeTabName = name;
    }
}
