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
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.part.ViewPart;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.connection.actions.ForceStartHandler;
import org.neo4j.neoclipse.connection.actions.NewAliasAction;
import org.neo4j.neoclipse.connection.actions.SqlEditorAction;
import org.neo4j.neoclipse.event.NeoclipseEvent;
import org.neo4j.neoclipse.event.NeoclipseEventListener;
import org.neo4j.neoclipse.event.NeoclipseListenerList;
import org.neo4j.neoclipse.graphdb.GraphDbServiceManager;
import org.neo4j.neoclipse.graphdb.GraphDbServiceStatus;
import org.neo4j.neoclipse.view.ErrorMessage;
import org.neo4j.neoclipse.view.NeoGraphViewPart;
import org.neo4j.neoclipse.view.UiHelper;

/**
 * 
 * @author Radhakrishna Kalyan
 */
public class ConnectionsView extends ViewPart implements NeoclipseEventListener
{

    public static final String ID = ConnectionsView.class.getCanonicalName();
    private final NeoclipseListenerList connectionListeners = new NeoclipseListenerList();
    private TreeViewer _treeViewer;

    public ConnectionsView()
    {
        super();
        Activator.getDefault().setConnectionsView( this );
        connectionListeners.add( new ForceStartHandler() );
    }

    @Override
    public void createPartControl( Composite parent )
    {
        Activator.getDefault().getAliasManager().registerConnetionListener( this );

        _treeViewer = new TreeViewer( parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL );
        getSite().setSelectionProvider( _treeViewer );

        IToolBarManager toolBarMgr = getViewSite().getActionBars().getToolBarManager();
        toolBarMgr.add( new NewAliasAction() );
        toolBarMgr.add( new SqlEditorAction() );

        _treeViewer.setUseHashlookup( true );
        _treeViewer.setContentProvider( new ConnectionTreeContentProvider() );
        _treeViewer.setLabelProvider( new ConnectionTreeLabelProvider() );
        _treeViewer.setInput( Activator.getDefault().getAliasManager() );

        // doubleclick on alias opens session
        _treeViewer.addDoubleClickListener( new IDoubleClickListener()
        {
            @Override
            public void doubleClick( final DoubleClickEvent event )
            {
                UiHelper.asyncExec( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                        if ( selection != null )
                        {
                            Object selected = selection.getFirstElement();
                            if ( selected instanceof Alias )
                            {
                                Alias alias = (Alias) selection.getFirstElement();
                                startOrStopConnection( alias );
                            }
                        }
                    }
                } );
            }
        } );

        _treeViewer.addSelectionChangedListener( new ISelectionChangedListener()
        {
            @Override
            public void selectionChanged( final SelectionChangedEvent event )
            {
                UiHelper.asyncExec( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        refreshToolbar();
                        Activator.getDefault().fireServiceChangedEvent( GraphDbServiceStatus.DB_SELECT );
                    }
                } );
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

    public void startOrStopConnection( final Alias alias )
    {
        UiHelper.asyncExec( new Runnable()
        {
            @Override
            public void run()
            {
                GraphDbServiceManager gsm = Activator.getDefault().getGraphDbServiceManager();
                NeoGraphViewPart graphView = Activator.getDefault().getNeoGraphViewPart();
                try
                {
                    if ( gsm.isRunning() )
                    {
                        if ( !gsm.getCurrentAlias().equals( alias ) )
                        {
                            ErrorMessage.showDialog( "Database problem",
                                    "Another Database view is already active. Please close it before starting another one." );
                        }
                        else
                        {
                            graphView.cleanTransactionBeforeShutdown();
                            gsm.stopGraphDbService().get();
                        }
                    }
                    else if ( !gsm.isRunning() )
                    {
                        gsm.startGraphDbService( alias ).get();
                        graphView.showSomeNode();
                    }
                }
                catch ( Exception e )
                {
                    ErrorMessage.showDialog( "Database problem", e );
                }
                Activator.getDefault().getAliasManager().notifyListners();
            }
        } );

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

    public void notifyListners()
    {
        connectionListeners.notifyListeners();
    }
}
