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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.SQLCannotConnectException;
import net.sourceforge.sqlexplorer.dbproduct.MetaDataSession;
import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.dbproduct.User;
import net.sourceforge.sqlexplorer.dbstructure.DBTreeActionGroup;
import net.sourceforge.sqlexplorer.dbstructure.DBTreeContentProvider;
import net.sourceforge.sqlexplorer.dbstructure.DBTreeLabelProvider;
import net.sourceforge.sqlexplorer.dbstructure.actions.FilterStructureAction;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractNode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.ColumnNode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.DatabaseNode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.TableNode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 * Database Structure View. Shows the database outline. Selections made in this
 * view are shown in the DatabaseDetailView.
 * 
 * @author Davy Vanherbergen
 */
public class DatabaseStructureView extends ViewPart {
	
	private static class RootNode extends AbstractNode
	{
		private DatabaseNode node;
		public RootNode(DatabaseNode pNode)
		{
			super(pNode.getName());
			this.node = pNode;
		}
		@Override
		public void loadChildren() {
			_children = new ArrayList<INode>();
			_children.add(this.node);
			
		}
		
	}
	/*
	 * Contains state data for each tab
	 */
	private static class TabData {
		private TreeViewer treeViewer;
		private MetaDataSession session;
	}

    private FilterStructureAction _filterAction;

    private Composite _parent;

    /** We use one tab for every session */
    private CTabFolder _tabFolder;

    private List<MetaDataSession> _allSessions = new ArrayList<MetaDataSession>();

    public DatabaseStructureView() {
		super();
		SQLExplorerPlugin.getDefault().setDatabaseStructureView(this);
	}

	/**
     * Adds a new user
     * @param user
     */
    public void addUser(final User user) throws SQLCannotConnectException {
    	// Make sure we list each user only once
    	for (Session session : _allSessions)
    		if (session.getUser() == user)
    			return;
    	
		MetaDataSession session = user.getMetaDataSession();
		if (session != null)
			addSession(user.getMetaDataSession());
    }
    	
    /**
     * Add a new session to the database structure view. This will create a new
     * tab for the session.
     * 
     * @param session
     */
    private void addSession(final MetaDataSession session) throws SQLCannotConnectException {
        if (_allSessions.contains(session))
            return;
        try {
        	session.getMetaData();
        	session.setAutoCommit(true);
        } catch(SQLCannotConnectException e) {
        	SQLExplorerPlugin.error(e);
        	throw e;
        }catch(SQLException e) {
        	SQLExplorerPlugin.error(e);
        	MessageDialog.openError(getSite().getShell(), "Cannot connect", e.getMessage());
        }
        DatabaseNode rootNode = session.getRoot();
        if (rootNode == null)
        	return;
        _allSessions.add(session);
        
        if (_filterAction != null) {
            _filterAction.setEnabled(true);
        }

        if (_tabFolder == null || _tabFolder.isDisposed()) {

            clearParent();

            // create tab folder for different sessions
            _tabFolder = new CTabFolder(_parent, SWT.TOP | SWT.CLOSE);

            // add listener to keep both views on the same active tab
            _tabFolder.addSelectionListener(new SelectionAdapter() {

                public void widgetSelected(SelectionEvent e) {

                    // set the selected node in the detail view.
                    DatabaseDetailView detailView = (DatabaseDetailView) getSite().getPage().findView(
                            SqlexplorerViewConstants.SQLEXPLORER_DBDETAIL);
                    synchronizeDetailView(detailView);
                }

            });

    		// Set up a gradient background for the selected tab
    		Display display = getSite().getShell().getDisplay();
    	    _tabFolder.setSelectionBackground(
    	    		new Color[] {
    				        display.getSystemColor(SWT.COLOR_WHITE),
    		                new Color(null, 211, 225, 250),
    		                new Color(null, 175, 201, 246),
    		                IConstants.TAB_BORDER_COLOR
    	    		},
    	    		new int[] {25, 50, 75},
    	    		true
    	    	);
    		
    	    // Add a listener to handle the close button on each tab
    	    _tabFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {
    			public void close(CTabFolderEvent event) {
    				CTabItem tabItem = (CTabItem)event.item;
    				TabData tabData = (TabData)tabItem.getData();
    				_allSessions.remove(tabData.session);
    				event.doit = true;
    			}
    	    });
    	    
            _parent.layout();
            _parent.redraw();

        }

        // create tab
        final CTabItem tabItem = new CTabItem(_tabFolder, SWT.NULL);
        TabData tabData = new TabData();
        tabItem.setData(tabData);
        tabData.session = session;

        // set tab text
        String labelText = session.getUser().getDescription();
        tabItem.setText(labelText);

        // create composite for our outline
        Composite composite = new Composite(_tabFolder, SWT.NULL);
        composite.setLayout(new FillLayout());
        tabItem.setControl(composite);

        // create outline
        final TreeViewer treeViewer = new TreeViewer(composite, SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.BORDER);
        tabData.treeViewer = treeViewer;

        // add drag support
        // TODO improve drag support options
        Transfer[] transfers = new Transfer[] {TableNodeTransfer.getInstance(), TextTransfer.getInstance()};
        treeViewer.addDragSupport(DND.DROP_COPY, transfers, new DragSourceListener() {

            public void dragFinished(DragSourceEvent event) {

                System.out.println("$drag finished");
                TableNodeTransfer.getInstance().setSelection(null);
            }


            public void dragSetData(DragSourceEvent event) {
            	if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
            		event.data = ((IStructuredSelection) treeViewer.getSelection()).getFirstElement().toString();
            	} else {
            		Object sel = ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();
            		event.data = sel;
            	}
            }


            public void dragStart(DragSourceEvent event) {

                event.doit = !treeViewer.getSelection().isEmpty();
                if (event.doit) {
                    Object sel = ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();
                    if (!(sel instanceof TableNode)) {
                        event.doit = false;
                    } else {
                        TableNode tn = (TableNode) sel;
                        TableNodeTransfer.getInstance().setSelection(tn);
                        if (!tn.isTable())
                            event.doit = false;
                    }
                }
            }
        });

        // use hash lookup to improve performance
        treeViewer.setUseHashlookup(true);

        // add content and label provider
        treeViewer.setContentProvider(new DBTreeContentProvider());
        treeViewer.setLabelProvider(new DBTreeLabelProvider());
        treeViewer.setAutoExpandLevel(2);
        
        
        // set input session
        treeViewer.setInput(new RootNode(rootNode));

        // add selection change listener, so we can update detail view as
        // required.
        treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent ev) {

                // set the selected node in the detail view.
                DatabaseDetailView detailView = (DatabaseDetailView) getSite().getPage().findView(
                        SqlexplorerViewConstants.SQLEXPLORER_DBDETAIL);
                synchronizeDetailView(detailView);
            }
        });

        // bring detail to front on doubleclick of node
        treeViewer.addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {

                try {
                    // find view
                    DatabaseDetailView detailView = (DatabaseDetailView) getSite().getPage().findView(
                            SqlexplorerViewConstants.SQLEXPLORER_DBDETAIL);
                    if (detailView == null) {
                        getSite().getPage().showView(SqlexplorerViewConstants.SQLEXPLORER_DBDETAIL);
                    }
                    getSite().getPage().bringToTop(detailView);
                    synchronizeDetailView(detailView);
                } catch (Exception e) {
                    // fail silent
                }
            }

        });

        // add expand/collapse listener
        treeViewer.addTreeListener(new ITreeViewerListener() {

            public void treeCollapsed(TreeExpansionEvent event) {

                // refresh the node to change image
                INode node = (INode) event.getElement();
                node.setExpanded(false);
                TreeViewer viewer = (TreeViewer) event.getSource();
                viewer.update(node, null);
            }


            public void treeExpanded(TreeExpansionEvent event) {

                // refresh the node to change image
                INode node = (INode) event.getElement();
                node.setExpanded(true);
                TreeViewer viewer = (TreeViewer) event.getSource();
                viewer.update(node, null);
            }

        });

        // set new tab as the active one
        _tabFolder.setSelection(_tabFolder.getItemCount() - 1);

        // update detail view
        DatabaseDetailView detailView = (DatabaseDetailView) getSite().getPage().findView(
                SqlexplorerViewConstants.SQLEXPLORER_DBDETAIL);

        if (detailView != null) {

            // synchronze detail view with new session
            synchronizeDetailView(detailView);

            // bring detail to top of the view stack
            getSite().getPage().bringToTop(detailView);
        }

        // refresh view
        composite.layout();
        _tabFolder.layout();
        _tabFolder.redraw();

        // bring this view to top of the view stack, above detail if needed..
        getSite().getPage().bringToTop(this);

        // add context menu
        final DBTreeActionGroup actionGroup = new DBTreeActionGroup(treeViewer);
        MenuManager menuManager = new MenuManager("DBTreeContextMenu");
        menuManager.setRemoveAllWhenShown(true);
        Menu contextMenu = menuManager.createContextMenu(treeViewer.getTree());
        treeViewer.getTree().setMenu(contextMenu);

        menuManager.addMenuListener(new IMenuListener() {

            public void menuAboutToShow(IMenuManager manager) {

                actionGroup.fillContextMenu(manager);
            }
        });
    }


    /**
     * Remove all items from parent
     */
    private void clearParent() {

        Control[] children = _parent.getChildren();
        if (children != null) {
            for (int i = 0; i < children.length; i++) {
                children[i].dispose();
            }
        }
    }


    /**
     * Initializes the view and creates the root tabfolder that holds all the
     * sessions.
     * 
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent) {

        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
                SQLExplorerPlugin.PLUGIN_ID + ".DatabaseStructureView");

        _parent = parent;

        // load all open sessions
        /*
        for (Alias alias : SQLExplorerPlugin.getDefault().getAliasManager().getAliases())
        	for (User user: alias.getUsers()) {
        		MetaDataSession session = user.getMetaDataSession();
        		if (session != null)
        			addSession(session);
        	}
        	*/

        // set default message
        if (_allSessions.isEmpty()) {
            setDefaultMessage();
        }

        _filterAction = new FilterStructureAction();
        _filterAction.setEnabled(!_allSessions.isEmpty());
        IToolBarManager toolBarMgr = getViewSite().getActionBars().getToolBarManager();
        toolBarMgr.add(_filterAction);
    }


    /**
     * Cleanup and reset detail view.
     * 
     * @see org.eclipse.ui.IWorkbenchPart#dispose()
     */
    public void dispose() {

        // refresh detail view
        DatabaseDetailView detailView = (DatabaseDetailView) getSite().getPage().findView(
                SqlexplorerViewConstants.SQLEXPLORER_DBDETAIL);

        if (detailView != null) {
            detailView.setSelectedNode(null);
        }
    }

    public MetaDataSession getSession() {
        if (_tabFolder == null || _tabFolder.getSelectionIndex() < 0)
            return null;

        CTabItem item = _tabFolder.getItem(_tabFolder.getSelectionIndex());
        TabData tabData = (TabData)item.getData();
        return tabData.session;
    }

    /**
     * Loop through all tabs and refresh trees for sessions with session
     */
    public void refreshSessionTrees(Session session) {
        if (_tabFolder == null || _tabFolder.getSelectionIndex() < 0)
            return;

        CTabItem[] items = _tabFolder.getItems();
        if (items != null) {
        	for (CTabItem item : items) {
        		TabData tabData = (TabData)item.getData();
        		if (tabData.session.getUser() == session.getUser()) {
        			tabData.session.getRoot().refresh();
        			tabData.treeViewer.refresh();
        		}
        	}
        }
    }


    /**
     * Set a default message, this method is called when no sessions are
     * available for viewing.
     */
    private void setDefaultMessage() {

        clearParent();

        // add message
        String message = Messages.getString("DatabaseStructureView.NoSession");
        Label label = new Label(_parent, SWT.FILL);
        label.setText(message);
        label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        _parent.layout();
        _parent.redraw();
    }


    /**
     * Set focus on our database structure view..
     * 
     * @see org.eclipse.ui.IWorkbenchPart#setFocus()
     */
    public void setFocus() {

        // we don't need to do anything here..
    }


    /**
     * Update the detail view with the selection in the active treeviewer.
     */
    public void synchronizeDetailView(final DatabaseDetailView detailView) {

        BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {

            public void run() {

                if (detailView == null) {
                    return;
                }

                if (_tabFolder == null || _tabFolder.getItemCount() == 0 || _tabFolder.getSelectionIndex() < 0) {
                    return;
                }

                TabData tabData = (TabData)_tabFolder.getItem(_tabFolder.getSelectionIndex()).getData();
                INode selectedNode = null;

                if (tabData.treeViewer != null) {

                    // find our target node..
                    IStructuredSelection selection = (IStructuredSelection) tabData.treeViewer.getSelection();

                    // check if we have a valid selection
                    if (selection != null && (selection.getFirstElement() instanceof INode)) {

                        selectedNode = (INode) selection.getFirstElement();

                        // if the selected node is a column node, we want to
                        // show it's parent instead
                        // in the detail view.

                        if (selectedNode instanceof ColumnNode) {
                            selectedNode = selectedNode.getParent();
                        }
                    }

                }

                detailView.setSelectedNode(selectedNode);
            }
        });

    }
    
    public boolean isConnectedToUser(User user) {
    	for (Session session : _allSessions)
    		if (session.getUser().compareTo(user) == 0)
    			return true;
    	return false;
    }
}
