/**
 * Licensed to Neo Technology under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Neo Technology licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.neo4j.neoclipse.connection;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.part.ViewPart;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.connection.actions.NewAliasAction;
import org.neo4j.neoclipse.connection.actions.NewEditorAction;
import org.neo4j.neoclipse.event.NeoclipseEvent;
import org.neo4j.neoclipse.event.NeoclipseEventListener;
import org.neo4j.neoclipse.graphdb.GraphDbServiceStatus;
import org.neo4j.neoclipse.view.UiHelper;

/**
 * 
 * @author Radhakrishna Kalyan
 */
public class ConnectionsView extends ViewPart implements NeoclipseEventListener
{

    public static final String ID = ConnectionsView.class.getCanonicalName();

    private TreeViewer _treeViewer;

    public ConnectionsView()
    {
        super();
        Activator.getDefault().setConnectionsView( this );
    }

    @Override
    public void createPartControl( Composite parent )
    {
        Activator.getDefault().getAliasManager().registerConnetionListener( this );

        _treeViewer = new TreeViewer( parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL );
        getSite().setSelectionProvider( _treeViewer );

        IToolBarManager toolBarMgr = getViewSite().getActionBars().getToolBarManager();
        toolBarMgr.add( new NewAliasAction() );
        toolBarMgr.add( new NewEditorAction() );

        _treeViewer.setUseHashlookup( true );
        _treeViewer.setContentProvider( new ConnectionTreeContentProvider() );
        _treeViewer.setLabelProvider( new ConnectionTreeLabelProvider() );
        _treeViewer.setInput( Activator.getDefault().getAliasManager() );

        _treeViewer.addSelectionChangedListener( new ISelectionChangedListener()
        {
            @Override
            public void selectionChanged( SelectionChangedEvent event )
            {
                refreshToolbar();
                Activator.getDefault().fireServiceChangedEvent( GraphDbServiceStatus.DB_SELECT );

            }
        } );

        // add context menu
        final ConnectionTreeActionGroup actionGroup = new ConnectionTreeActionGroup();
        MenuManager menuManager = new MenuManager( "ConnectionTreeContextMenu" );
        menuManager.setRemoveAllWhenShown( true );
        Menu contextMenu = menuManager.createContextMenu( _treeViewer.getTree() );
        _treeViewer.getTree().setMenu( contextMenu );

        menuManager.addMenuListener( new IMenuListener()
        {
            @Override
            public void menuAboutToShow( IMenuManager manager )
            {
                actionGroup.fillContextMenu( manager );
            }
        } );
        _treeViewer.setAutoExpandLevel( 2 );

        parent.layout();

    }

    public TreeViewer getTreeViewer()
    {
        return _treeViewer;
    }

    public void refresh()
    {
        Display.getDefault().asyncExec( new Runnable()
        {
            @Override
            public void run()
            {
                if ( !_treeViewer.getTree().isDisposed() )
                {
                    _treeViewer.refresh();
                }
            }
        } );
    }

    private void refreshToolbar()
    {
        IToolBarManager toolbar = getViewSite().getActionBars().getToolBarManager();
        IContributionItem[] items = toolbar.getItems();
        for ( IContributionItem item : items )
        {
            if ( item instanceof ActionContributionItem )
            {
                ActionContributionItem contrib = (ActionContributionItem) item;
                IAction contribAction = contrib.getAction();
                if ( contribAction instanceof AbstractConnectionTreeAction )
                {
                    AbstractConnectionTreeAction action = (AbstractConnectionTreeAction) contribAction;
                    action.setEnabled( action.isAvailable() );
                }
            }
        }

    }

    public void openNewEditor()
    {
        // TODO new Cypher Editor
    }

    public Alias getSelectedAlias()
    {
        IStructuredSelection selection = (IStructuredSelection) _treeViewer.getSelection();
        return (Alias) selection.getFirstElement();

    }

    @Override
    public void setFocus()
    {
        _treeViewer.getControl().setFocus();
    }

    @Override
    public void stateChanged( NeoclipseEvent event )
    {
        UiHelper.asyncExec( new Runnable()
        {
            @Override
            public void run()
            {
                if ( !_treeViewer.getTree().isDisposed() )
                {
                    _treeViewer.refresh();
                    refreshToolbar();
                }
            }
        } );

    }

}
