/*
 * Copyright (C) 2007 SQL Explorer Development Team
 * http://sourceforge.net/projects/eclipsesql
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
package net.sourceforge.sqlexplorer.connections;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import net.sourceforge.sqlexplorer.connections.actions.AbstractConnectionTreeAction;
import net.sourceforge.sqlexplorer.connections.actions.CloseAllConnectionsAction;
import net.sourceforge.sqlexplorer.connections.actions.CloseConnectionAction;
import net.sourceforge.sqlexplorer.connections.actions.NewAliasAction;
import net.sourceforge.sqlexplorer.connections.actions.NewDatabaseStructureViewAction;
import net.sourceforge.sqlexplorer.connections.actions.NewEditorAction;
import net.sourceforge.sqlexplorer.dbproduct.Alias;
import net.sourceforge.sqlexplorer.dbproduct.ConnectionListener;
import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;
import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.dbproduct.User;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.actions.OpenPasswordConnectDialogAction;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class ConnectionsView extends ViewPart implements ConnectionListener {
	
	private static final HashSet<SQLConnection> EMPTY_CONNECTIONS = new HashSet<SQLConnection>();
	private static final HashSet<Alias> EMPTY_ALIASES = new HashSet<Alias>();
	private static final HashSet<User> EMPTY_USERS = new HashSet<User>();

	// Tree viewer for connections
    private TreeViewer _treeViewer;
    
    // Last User that was selected
    private User defaultUser;
    
    private Clipboard clipboard;

	public ConnectionsView() {
		super();
		SQLExplorerPlugin.getDefault().setConnectionsView(this);
	}

	/**
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent) {


        SQLExplorerPlugin.getDefault().getAliasManager().addListener(this);

        // create outline
        _treeViewer = new TreeViewer(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI);
        getSite().setSelectionProvider(_treeViewer);

        // create action bar
        IToolBarManager toolBarMgr = getViewSite().getActionBars().getToolBarManager();

        toolBarMgr.add(new NewAliasAction());
        toolBarMgr.add(new NewEditorAction());
        toolBarMgr.add(new NewDatabaseStructureViewAction());
        toolBarMgr.add(new CloseAllConnectionsAction());
        toolBarMgr.add(new CloseConnectionAction());

        // use hash lookup to improve performance
        _treeViewer.setUseHashlookup(true);

        // add content and label provider
        _treeViewer.setContentProvider(new ConnectionTreeContentProvider());
        _treeViewer.setLabelProvider(new ConnectionTreeLabelProvider());

        // set input session
        _treeViewer.setInput(SQLExplorerPlugin.getDefault().getAliasManager());

        // doubleclick on alias opens session
        _treeViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                if (selection != null) {
                    User user = null;
                    Object selected = selection.getFirstElement();
                    if (selected instanceof Alias) {
                        Alias alias = (Alias) selection.getFirstElement();
                        user = alias.getDefaultUser();
                    } else if (selected instanceof User)
                    	user = (User)selected;
                    else if (selected instanceof SQLConnection)
                    	user = ((SQLConnection)selected).getUser();
                    if (user != null)
                    	openNewEditor(user);
                }
            }
        });
        
        _treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				refreshToolbar();
			}
        });

        // add context menu
        final ConnectionTreeActionGroup actionGroup = new ConnectionTreeActionGroup();
        MenuManager menuManager = new MenuManager("ConnectionTreeContextMenu");
        menuManager.setRemoveAllWhenShown(true);
        Menu contextMenu = menuManager.createContextMenu(_treeViewer.getTree());
        _treeViewer.getTree().setMenu(contextMenu);

        menuManager.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                actionGroup.fillContextMenu(manager);
            }
        });
        _treeViewer.expandToLevel(2);

        PlatformUI.getWorkbench().getHelpSystem().setHelp(_treeViewer.getControl(), SQLExplorerPlugin.HELP_PLUGIN_ID + ".connection_view");
        
        parent.layout();

        SQLExplorerPlugin.getDefault().startDefaultConnections(this);
    }
    
    public void openNewEditor(User user) {
		new OpenPasswordConnectDialogAction(user.getAlias(), user).run(true);
/*		
        try {
        	// First time we connect, get the database structure view up too
        	if (!user.hasAuthenticated()) {
        		DatabaseStructureView dsView = SQLExplorerPlugin.getDefault().getDatabaseStructureView();
        		dsView.addUser(user);
        	}
            SQLEditorInput input = new SQLEditorInput("SQL Editor (" + SQLExplorerPlugin.getDefault().getEditorSerialNo() + ").sql");
            input.setUser(user);
            IWorkbenchPage page = SQLExplorerPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
            page.openEditor(input, SQLEditor.class.getName());
        }catch(SQLCannotConnectException e) {
        	MessageDialog.openError(Display.getDefault().getActiveShell(), "Cannot connect", e.getMessage());
        } catch (Throwable e) {
            SQLExplorerPlugin.error("Error creating sql editor", e);
        }
*/        
    }

	public void connectionClosed(Session session) {
    	modelChanged();
	}

	public void connectionOpened(Session session) {
    	modelChanged();
	}

    public void modelChanged() {
        getSite().getShell().getDisplay().asyncExec(new Runnable() {
            public void run() {
            	if (!_treeViewer.getTree().isDisposed()) {
            	//	_treeViewer.refresh();
            	//	refreshToolbar();
            	}
            }
        });
    }

	public void dispose() {
		if (clipboard != null) {
			clipboard.dispose();
			clipboard = null;
		}
        SQLExplorerPlugin.getDefault().getAliasManager().removeListener(this);
        super.dispose();
    }

    public TreeViewer getTreeViewer() {
        return _treeViewer;
    }

	public void refresh() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (!_treeViewer.getTree().isDisposed())
					_treeViewer.refresh();
			}
		});
	}
	
	private void refreshToolbar() {
        IToolBarManager toolbar = getViewSite().getActionBars().getToolBarManager();
        IContributionItem[] items = toolbar.getItems();
        for (IContributionItem item : items) {
        	if (item instanceof ActionContributionItem) {
        		ActionContributionItem contrib = (ActionContributionItem)item;
        		IAction contribAction = contrib.getAction();
        		if(contribAction instanceof AbstractConnectionTreeAction)
        		{
	        		AbstractConnectionTreeAction action = (AbstractConnectionTreeAction)contribAction;
	        		action.setEnabled(action.isAvailable());
        		}
        	}
        }
	}
	
	/**
	 * Returns the objects which are currently selected.  NOTE this is package
	 * private and should remain that way - the implementation of the ConnectionsView
	 * is now hidden from the rest of the application (see the getSelectedXxxx() methods
	 * below for a structured API)
	 * @return
	 */
	/*package*/ Object[] getSelected() {
    	IStructuredSelection selection = (IStructuredSelection)_treeViewer.getSelection();
    	if (selection == null)
    		return null;
    	Object[] result = selection.toArray();
    	if (result.length == 0)
    		return null;
    	return result;
	}
	
	/**
	 * Returns a list of the selected Aliases.  If recurse is true then the result will
	 * include any aliases associated with other objects; eg, if a connection is selected
	 * and recurse is true, then the connection's alias will also be returned
	 * @param recurse
	 * @return Set of Aliases, never returns null
	 */
    public Set<Alias> getSelectedAliases(boolean recurse) {
    	IStructuredSelection selection = (IStructuredSelection)_treeViewer.getSelection();
    	if (selection == null)
    		return EMPTY_ALIASES;
    	
    	LinkedHashSet<Alias> result = new LinkedHashSet<Alias>();
    	Iterator<?> iter = selection.iterator();
    	while (iter.hasNext()) {
    		Object obj = iter.next();
    		if (obj instanceof Alias)
    			result.add((Alias)obj);
    		else if (recurse) {
    			if (obj instanceof User) {
    				User user = (User)obj;
    				result.add(user.getAlias());
    			} else if (obj instanceof SQLConnection) {
    				SQLConnection connection = (SQLConnection)obj;
    				result.add(connection.getUser().getAlias());
    			}
    		}
    	}
    	
    	return result;
    }
    
    /**
     * Returns the first available selected alias; if recurse is true, then 
     * indirectly selected aliases are included (eg a selected connection's alias)
     * @param recurse
     * @return
     */
    public Alias getSelectedAlias(boolean recurse) {
    	return (Alias)getFirstOf(getSelectedAliases(recurse));
    }
    
    /**
     * Returns a list of selected Users; if recurse is true, indirectly selected users
     * are included also (eg a session's user)
     * @param recurse
     * @return Set of Users, never returns null
     */
    public Set<User> getSelectedUsers(boolean recurse) {
    	IStructuredSelection selection = (IStructuredSelection)_treeViewer.getSelection();
    	if (selection == null)
    		return EMPTY_USERS;
    	
    	LinkedHashSet<User> result = new LinkedHashSet<User>();
    	Iterator<?> iter = selection.iterator();
    	while (iter.hasNext()) {
    		Object obj = iter.next();
    		if (obj instanceof User)
    			result.add((User)obj);
    		else if (recurse) {
    			if (obj instanceof Alias) {
    				Alias alias = (Alias)obj;
    				result.addAll(alias.getUsers());
    			} else if (obj instanceof SQLConnection) {
    				SQLConnection connection = (SQLConnection)obj;
    				result.add(connection.getUser());
    			}
    		}
    	}
    	
    	return result;
    }
    
    /**
     * Returns the first selected user; if recurse is true, this includes indirectly
     * selected users (eg an Alias' user)
     * @param recurse
     * @return
     */
    public User getSelectedUser(boolean recurse) {
    	return (User)getFirstOf(getSelectedUsers(recurse));
    }
    
    /**
     * Returns a list of selected sessions; if recurse is true, then it includes indirectly
     * selected sessions (eg a selected user's sessions) 
     * @param recurse
     * @return Set of Sessions, never returns null
     */
    public Set<SQLConnection> getSelectedConnections(boolean recurse) {
    	IStructuredSelection selection = (IStructuredSelection)_treeViewer.getSelection();
    	if (selection == null)
    		return EMPTY_CONNECTIONS;
    	
    	LinkedHashSet<SQLConnection> result = new LinkedHashSet<SQLConnection>();
    	Iterator<?> iter = selection.iterator();
    	while (iter.hasNext()) {
    		Object obj = iter.next();
    		if (obj instanceof SQLConnection)
    			result.add((SQLConnection)obj);
    		else if (recurse) {
    			if (obj instanceof Alias) {
    				Alias alias = (Alias)obj;
    				for (User user : alias.getUsers())
    					result.addAll(user.getConnections());
    			} else if (obj instanceof User) {
    				User user = (User)obj;
					result.addAll(user.getConnections());
    			}
    		}
    	}
    	
    	return result;
    }

    /**
     * Returns the first selected connection; if recurse is true, then includes indirectly
     * selected sessions
     * @param recurse
     * @return
     */
    public SQLConnection getSelectedConnection(boolean recurse) {
    	return (SQLConnection)getFirstOf(getSelectedConnections(recurse));
    }

    /**
     * @see org.eclipse.ui.IWorkbenchPart#setFocus()
     */
    public void setFocus() {

    }

    /**
     * Helper method which returns the first element of a set, or null if the set is
     * empty (or if the set is null)
     * @param set the set to look into (may be null)
     * @return
     */
    private <T> T getFirstOf(Set<T> set) {
    	if (set == null)
    		return null;
    	Iterator<T> iter = set.iterator();
    	if (iter.hasNext())
    		return iter.next();
    	return null;
    }

    /**
	 * @return the defaultUser
	 */
	public User getDefaultUser() {
		if (defaultUser == null) {
			Alias alias = getDefaultAlias();
			if (alias != null)
				return alias.getDefaultUser();
		}
		return defaultUser;
	}

	/**
	 * @param defaultUser the defaultUser to set
	 */
	public void setDefaultUser(User lastSelectedUser) {
		this.defaultUser = lastSelectedUser;
	}

    private Alias getDefaultAlias() {
    	IStructuredSelection selection = (IStructuredSelection)_treeViewer.getSelection();
    	if (selection == null)
    		return null;
    	
    	Object element = selection.getFirstElement();
    	
    	if (element instanceof Alias)
    		return (Alias)element;
    	else if (element instanceof Session) {
    		ITreeContentProvider provider = (ITreeContentProvider)_treeViewer.getContentProvider();
    		return (Alias)provider.getParent(element);
    	}
    	
    	return null;
    }

	/**
	 * @return the clipboard
	 */
	public Clipboard getClipboard() {
		if (clipboard == null)
			clipboard = new Clipboard(getSite().getShell().getDisplay());
		return clipboard;
	}

	/**
	 * @param clipboard the clipboard to set
	 */
	public void setClipboard(Clipboard clipboard) {
		this.clipboard = clipboard;
	}
}
