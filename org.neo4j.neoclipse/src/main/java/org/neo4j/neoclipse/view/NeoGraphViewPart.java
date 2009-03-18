/*
 * Licensed to "Neo Technology," Network Engine for Objects in Lund AB
 * (http://neotechnology.com) under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership. Neo Technology licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License
 * at (http://www.apache.org/licenses/LICENSE-2.0). Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.neo4j.neoclipse.view;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.draw2d.ChangeEvent;
import org.eclipse.draw2d.ChangeListener;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.zest.core.viewers.AbstractZoomableViewer;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.viewers.IZoomableWorkbenchPart;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.SpringLayoutAlgorithm;
import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.NotFoundException;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.Transaction;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.action.Actions;
import org.neo4j.neoclipse.action.PrintGraphAction;
import org.neo4j.neoclipse.action.browse.GoBackAction;
import org.neo4j.neoclipse.action.browse.GoForwardAction;
import org.neo4j.neoclipse.action.browse.RefreshAction;
import org.neo4j.neoclipse.action.browse.ShowReferenceNodeAction;
import org.neo4j.neoclipse.action.context.AddIncomingNodeAction;
import org.neo4j.neoclipse.action.context.AddOutgoingNodeAction;
import org.neo4j.neoclipse.action.context.AddRelationshipAction;
import org.neo4j.neoclipse.action.context.DeleteAction;
import org.neo4j.neoclipse.action.decorate.node.ShowNodeColorsAction;
import org.neo4j.neoclipse.action.decorate.node.ShowNodeIconsAction;
import org.neo4j.neoclipse.action.decorate.node.ShowNodeIdsAction;
import org.neo4j.neoclipse.action.decorate.node.ShowNodeLabelAction;
import org.neo4j.neoclipse.action.decorate.rel.ShowRelationshipColorsAction;
import org.neo4j.neoclipse.action.decorate.rel.ShowRelationshipDirectionsAction;
import org.neo4j.neoclipse.action.decorate.rel.ShowRelationshipIdsAction;
import org.neo4j.neoclipse.action.decorate.rel.ShowRelationshipLabelAction;
import org.neo4j.neoclipse.action.decorate.rel.ShowRelationshipTypesAction;
import org.neo4j.neoclipse.action.layout.ShowGridLayoutAction;
import org.neo4j.neoclipse.action.layout.ShowHorizontalShiftLayoutAction;
import org.neo4j.neoclipse.action.layout.ShowHorizontalTreeLayoutAction;
import org.neo4j.neoclipse.action.layout.ShowRadialLayoutAction;
import org.neo4j.neoclipse.action.layout.ShowSpringLayoutAction;
import org.neo4j.neoclipse.action.layout.ShowTreeLayoutAction;
import org.neo4j.neoclipse.action.view.DecreaseTraversalDepthAction;
import org.neo4j.neoclipse.action.view.IncreaseTraversalDepthAction;
import org.neo4j.neoclipse.action.view.ZoomAction;
import org.neo4j.neoclipse.help.HelpContextConstants;
import org.neo4j.neoclipse.neo.NeoServiceEvent;
import org.neo4j.neoclipse.neo.NeoServiceEventListener;
import org.neo4j.neoclipse.neo.NeoServiceManager;
import org.neo4j.neoclipse.neo.NeoServiceStatus;
import org.neo4j.neoclipse.property.NeoPropertySheetPage;
import org.neo4j.neoclipse.reltype.RelationshipTypeView;

/**
 * This class is a view that shows the contents of a Neo database as a graph of
 * connected objects.
 * @author Peter H&auml;nsgen
 * @author Anders Nawroth
 */
public class NeoGraphViewPart extends ViewPart implements
    IZoomableWorkbenchPart
{
    /**
     * The Eclipse view ID.
     */
    public static final String ID = "org.neo4j.neoclipse.view.NeoGraphViewPart";
    /**
     * Max number of guesses to find a better starting point.
     */
    private static final int MAX_ID_GUESSES = 1000;
    /**
     * The property sheet page.
     */
    protected NeoPropertySheetPage propertySheetPage;
    /**
     * The graph.
     */
    protected GraphViewer viewer;
    /**
     * Keep track of visited nodes.
     */
    private BrowserHistory browserHistory = null;
    /**
     * The go back action.
     */
    GoBackAction backAction = new GoBackAction( this );
    /**
     * The go forward action.
     */
    GoForwardAction forwardAction = new GoForwardAction( this );
    /**
     * The decrease traversal depth action.
     */
    protected DecreaseTraversalDepthAction decAction;
    /**
     * The depth how deep we should traverse into the network.
     */
    private int traversalDepth = 1;
    private DeleteAction deleteAction;
    private AddRelationshipAction addRelationshipAction;
    private List<Node> currentSelectedNodes = new ArrayList<Node>();
    private List<Relationship> currentSelectedRels = new ArrayList<Relationship>();
    private RelationshipTypeView relTypeView;
    private AddOutgoingNodeAction addOutgoingAction;
    private AddIncomingNodeAction addIncomingAction;
    private List<InputChangeListener> listeners = new ArrayList<InputChangeListener>();
    private Node previousInputNode = null;

    /**
     * Creates the view.
     */
    public void createPartControl( Composite parent )
    {
        viewer = new GraphViewer( parent, SWT.NONE );
        viewer.setUseHashlookup( true );
        viewer.setContentProvider( new NeoGraphContentProvider( this ) );
        viewer.addDoubleClickListener( new NeoGraphDoubleClickListener() );
        viewer.setLayoutAlgorithm( new SpringLayoutAlgorithm(
            LayoutStyles.NO_LAYOUT_NODE_RESIZING ) );
        NeoGraphLabelProvider labelProvider = NeoGraphLabelProviderWrapper
            .getInstance();
        viewer.setLabelProvider( labelProvider );
        addListener( labelProvider );

        getSite().getPage().addSelectionListener( ID,
            new SelectionChangeHandler() );
        getSite().getPage().addSelectionListener( RelationshipTypeView.ID,
            new RelTypeSelectionChangeHandler() );

        makeContributions();
        NeoServiceManager sm = Activator.getDefault().getNeoServiceManager();
        sm.addServiceEventListener( new NeoGraphServiceEventListener() );
        getSite().setSelectionProvider( viewer );
        Activator.getDefault().getPluginPreferences()
            .addPropertyChangeListener( new PreferenceChangeHandler() );
        showSomeNode();
        PlatformUI.getWorkbench().getHelpSystem().setHelp( viewer.getControl(),
            HelpContextConstants.NEO_GRAPH_VIEW_PART );
        for ( IViewReference view : getSite().getPage().getViewReferences() )
        {
            if ( RelationshipTypeView.ID.equals( view.getId() ) )
            {
                relTypeView = (RelationshipTypeView) view.getView( false );
            }
        }
    }

    /**
     * Add listener for input changes.
     * @param listener
     */
    public void addListener( InputChangeListener listener )
    {
        listeners.add( listener );
    }

    /**
     * Notify listeners of new input node.
     * @param node
     */
    private void notifyListeners( Node node )
    {
        for ( InputChangeListener listener : listeners )
        {
            listener.inputChange( node );
        }
        // make sure to update only these label colors
        if ( previousInputNode != null )
        {
            refresh( previousInputNode, true );
        }
        refresh( node, true );
        previousInputNode = node;
    }

    /**
     * Create a context menu.
     */
    private void createContextMenu()
    {
        MenuManager menuMgr = new MenuManager();
        deleteAction = new DeleteAction( this );
        menuMgr.add( deleteAction );
        addRelationshipAction = new AddRelationshipAction( this );
        menuMgr.add( addRelationshipAction );
        addOutgoingAction = new AddOutgoingNodeAction( this );
        menuMgr.add( addOutgoingAction );
        addIncomingAction = new AddIncomingNodeAction( this );
        menuMgr.add( addIncomingAction );
        Menu menu = menuMgr.createContextMenu( viewer.getControl() );
        viewer.getControl().setMenu( menu );
    }

    /**
     * Set context menu to the correct state.
     */
    public void updateMenuState()
    {
        int selectedNodeCount = currentSelectedNodes.size();
        int selectedRelationshipCount = currentSelectedRels.size();
        if ( deleteAction != null )
        {
            deleteAction.setEnabled( selectedNodeCount > 0
                || selectedRelationshipCount > 0 );
        }

        int selectedRelTypeCount = -1;
        if ( relTypeView != null )
        {
            selectedRelTypeCount = relTypeView.getCurrentSelectedRelTypes()
                .size();
        }
        if ( addRelationshipAction != null )
        {
            addRelationshipAction.setEnabled( selectedNodeCount == 2
                && selectedRelTypeCount == 1 );
        }
        if ( addOutgoingAction != null && addIncomingAction != null )
        {
            boolean enabled = selectedNodeCount > 0
                && selectedRelTypeCount == 1;
            addOutgoingAction.setEnabled( enabled );
            addIncomingAction.setEnabled( enabled );
        }
    }

    /**
     * Gets the current node.
     * @return current node
     */
    public Node getCurrentNode()
    {
        Object node = viewer.getInput();
        if ( node instanceof Node )
        {
            return (Node) node;
        }
        NeoService ns = Activator.getDefault().getNeoServiceSafely();
        if ( ns != null )
        {
            return ns.getReferenceNode();
        }
        throw new NotFoundException( "No current node could be found." );
    }

    /*
     * Add the current node to the view. Used when traversal find nothing to
     * show.
     * @SuppressWarnings( "restriction" ) public void addCurrentNode() {
     * viewer.addNode( getCurrentNode() ); }
     */

    /**
     * Initializes menus, tool bars etc.
     */
    protected void makeContributions()
    {
        // initialize actions
        IToolBarManager tm = getViewSite().getActionBars().getToolBarManager();
        IMenuManager mm = getViewSite().getActionBars().getMenuManager();

        createContextMenu();

        // platform actions
        tm.add( deleteAction );

        // separator
        {
            tm.add( new Separator() );
        }

        // navigation actions
        contributeNavigationActions( tm );
        // recursion level actions
        contributeRecursionLevelActions( tm );
        // zoom actions
        contributeZoomActions( tm );
        // layout actions
        contributeLayoutActions( tm, mm );
        // separator
        {
            mm.add( new Separator() );
        }
        // label settings actions
        contributeLabelActions( mm );
        // separator
        {
            mm.add( new Separator() );
        }
        // platform actions
        contributePlatformActions( mm );
        // printing
        getViewSite().getActionBars().setGlobalActionHandler(
            ActionFactory.PRINT.getId(), new PrintGraphAction( this ) );
    }

    /**
     * Add platform actions like showing the preference page.
     * @param mm
     *            current menu manager
     */
    private void contributePlatformActions( IMenuManager mm )
    {
        Action preferenesAction = new Action()
        {
            @Override
            public void run()
            {
                Activator.getDefault().showPreferenceDialog();
            }
        };
        Actions.PREFERENCES.initialize( preferenesAction );
        mm.add( preferenesAction );
    }

    /**
     * Add label actions to menu.
     * @param mm
     *            current menu manager
     */
    private void contributeLabelActions( IMenuManager mm )
    {
        {
            String labelsGroupName = "labels";
            GroupMarker labelsGroup = new GroupMarker( labelsGroupName );
            mm.add( labelsGroup );
            // relationship types actions
            mm.appendToGroup( labelsGroupName, new ShowRelationshipTypesAction(
                this ) );
            // relationship types actions
            mm.appendToGroup( labelsGroupName, new ShowRelationshipLabelAction(
                this ) );
            // relationship id's actions
            mm.appendToGroup( labelsGroupName, new ShowRelationshipIdsAction(
                this ) );
            // relationship types actions
            mm.appendToGroup( labelsGroupName,
                new ShowRelationshipColorsAction( this ) );
            // relationship directions actions
            mm.appendToGroup( labelsGroupName,
                new ShowRelationshipDirectionsAction( this ) );
            // separator
            {
                mm.add( new Separator() );
            }
            // names actions
            mm.appendToGroup( labelsGroupName, new ShowNodeLabelAction( this ) );
            // relationship id's actions
            mm.appendToGroup( labelsGroupName, new ShowNodeIdsAction( this ) );
            // node colors actions
            mm
                .appendToGroup( labelsGroupName,
                    new ShowNodeColorsAction( this ) );
            // node icons actions
            mm.appendToGroup( labelsGroupName, new ShowNodeIconsAction( this ) );
        }
    }

    /**
     * Add layout actions to the menu and toolbar.
     * @param tm
     *            current tool bar manager
     * @param mm
     *            current menu manager
     */
    private void contributeLayoutActions( IToolBarManager tm, IMenuManager mm )
    {
        {
            String groupName = "layout";
            GroupMarker layoutGroup = new GroupMarker( groupName );
            tm.add( layoutGroup );
            mm.add( layoutGroup );
            // spring layout
            ShowSpringLayoutAction springLayoutAction = new ShowSpringLayoutAction(
                this );
            tm.appendToGroup( groupName, springLayoutAction );
            mm.appendToGroup( groupName, springLayoutAction );
            // tree layout
            ShowTreeLayoutAction treeLayoutAction = new ShowTreeLayoutAction(
                this );
            tm.appendToGroup( groupName, treeLayoutAction );
            mm.appendToGroup( groupName, treeLayoutAction );
            // radial layout
            ShowRadialLayoutAction radialLayoutAction = new ShowRadialLayoutAction(
                this );
            tm.appendToGroup( groupName, radialLayoutAction );
            mm.appendToGroup( groupName, radialLayoutAction );
            // grid layout
            ShowGridLayoutAction gridLayoutAction = new ShowGridLayoutAction(
                this );
            tm.appendToGroup( groupName, gridLayoutAction );
            mm.appendToGroup( groupName, gridLayoutAction );
            // horizontal tree layout
            ShowHorizontalTreeLayoutAction horizontalTreeLayoutAction = new ShowHorizontalTreeLayoutAction(
                this );
            mm.appendToGroup( groupName, horizontalTreeLayoutAction );
            // horizontal shift layout
            ShowHorizontalShiftLayoutAction horizontalShiftLayoutAction = new ShowHorizontalShiftLayoutAction(
                this );
            mm.appendToGroup( groupName, horizontalShiftLayoutAction );
        }
    }

    /**
     * Add zoom actions to the tool bar.
     * @param tm
     *            current tool bar manager
     */
    private void contributeZoomActions( IToolBarManager tm )
    {
        {
            ZoomAction zoomAction = new ZoomAction( this );
            tm.add( zoomAction );
            tm.add( new Separator() );
        }
    }

    /**
     * Add traversal depth actions to the tool bar.
     * @param tm
     *            current tool bar manager
     */
    private void contributeRecursionLevelActions( IToolBarManager tm )
    {
        {
            IncreaseTraversalDepthAction incAction = new IncreaseTraversalDepthAction(
                this );
            tm.add( incAction );

            decAction = new DecreaseTraversalDepthAction( this );
            tm.add( decAction );

            tm.add( new Separator() );
        }
    }

    /**
     * Add standard actions to the tool bar. (home , refresh)
     * @param tm
     *            current tool bar manager
     */
    private void contributeNavigationActions( IToolBarManager tm )
    {
        {
            tm.add( backAction );
            tm.add( forwardAction );

            ShowReferenceNodeAction refNodeAction = new ShowReferenceNodeAction(
                this );
            tm.add( refNodeAction );

            RefreshAction refreshAction = new RefreshAction( this );
            tm.add( refreshAction );

            tm.add( new Separator() );
        }
    }

    /**
     * Updates the content of the status bar.
     */
    public void refreshStatusBar()
    {
        getViewSite().getActionBars().getStatusLineManager().setMessage(
            "Traversal depth: " + String.valueOf( traversalDepth )
                + "   Nodes: "
                + String.valueOf( viewer.getGraphControl().getNodes().size() )
                + "   Relationships: "
                + viewer.getGraphControl().getConnections().size() );
    }

    /**
     * Returns the viewer that contains the graph.
     */
    public GraphViewer getViewer()
    {
        return viewer;
    }

    /**
     * Returns the graph viewer for zooming.
     */
    public AbstractZoomableViewer getZoomableViewer()
    {
        return viewer;
    }

    /**
     * This is how the framework determines which interfaces we implement.
     */
    @SuppressWarnings( "unchecked" )
    public Object getAdapter( Class key )
    {
        if ( key.equals( IPropertySheetPage.class ) )
        {
            return getPropertySheetPage();
        }
        else
        {
            return super.getAdapter( key );
        }
    }

    /**
     * This accesses a cached version of the property sheet.
     */
    public IPropertySheetPage getPropertySheetPage()
    {
        if ( propertySheetPage == null )
        {
            propertySheetPage = new NeoPropertySheetPage();
            propertySheetPage.addChangeListener( new PropertyChangeHandler() );
        }
        return propertySheetPage;
    }

    /**
     * Cleans up.
     */
    public void dispose()
    {
        if ( propertySheetPage != null )
        {
            propertySheetPage.dispose();
        }
        super.dispose();
    }

    /**
     * Sets the focus.
     */
    public void setFocus()
    {
        viewer.getControl().setFocus();
    }

    /**
     * Go back to previous node.
     */
    public void goBack()
    {
        Node node = getBrowserHistory().getPrevious();
        if ( node != null )
        {
            showNode( node );
        }
        updateNavStatus();
    }

    /**
     * Go forward to next node.
     */
    public void goForward()
    {
        Node node = getBrowserHistory().getNext();
        if ( node != null )
        {
            showNode( node );
        }
        updateNavStatus();
    }

    /**
     * Focuses the view on the reference node.
     */
    public void showReferenceNode()
    {
        NeoService ns = Activator.getDefault().getNeoServiceSafely();
        if ( ns != null )
        {
            Transaction txn = ns.beginTx();
            try
            {
                Node node = ns.getReferenceNode();
                setInput( node );
            }
            finally
            {
                txn.finish();
            }
        }
    }

    /**
     * Focuses the view on the reference node or some other node. If the
     * reference node has no relationships, it will try to find a node that has
     * relationships.
     */
    public void showSomeNode()
    {
        NeoService ns = Activator.getDefault().getNeoServiceSafely();
        if ( ns != null )
        {
            Transaction txn = ns.beginTx();
            try
            {
                Node node = ns.getReferenceNode();
                if ( !node.hasRelationship() )
                {
                    // so, find a more friendly node if possible!
                    Node betterNode;
                    for ( long id = 0; id < MAX_ID_GUESSES; id++ )
                    {
                        try
                        {
                            betterNode = ns.getNodeById( id );
                            if ( node.equals( betterNode ) )
                            {
                                continue;
                            }
                            if ( betterNode.hasRelationship() )
                            {
                                node = betterNode;
                                break;
                            }
                        }
                        catch ( NotFoundException e )
                        {
                            // really nothing to do in here
                        }
                    }
                }
                setInput( node );
            }
            finally
            {
                txn.finish();
            }
        }
    }

    /**
     * Focuses the view on the node with the given id.
     */
    public void showNode( long nodeId )
    {
        NeoService ns = Activator.getDefault().getNeoServiceSafely();
        if ( ns != null )
        {
            Transaction txn = ns.beginTx();
            try
            {
                Node node = ns.getNodeById( nodeId );
                viewer.setInput( node );
            }
            finally
            {
                txn.finish();
            }
        }
    }

    /**
     * Focuses the view on the given node.
     */
    public void showNode( Node node )
    {
        NeoService ns = Activator.getDefault().getNeoServiceSafely();
        if ( ns != null )
        {
            Transaction txn = ns.beginTx();
            try
            {
                viewer.setInput( node );
            }
            finally
            {
                txn.finish();
            }
        }
    }

    /**
     * Returns the current traversal depth.
     */
    public int getTraversalDepth()
    {
        return traversalDepth;
    }

    /**
     * Increments the traversal depth.
     */
    public void incTraversalDepth()
    {
        NeoService ns = Activator.getDefault().getNeoServiceSafely();
        if ( ns != null )
        {
            Transaction txn = ns.beginTx();
            try
            {
                traversalDepth++;
                refreshViewer();
                viewer.applyLayout();
            }
            finally
            {
                txn.finish();
            }
        }
        if ( traversalDepth > 0 )
        {
            decAction.setEnabled( true );
        }
    }

    /**
     * Decrements the traversal depth.
     */
    public void decTraversalDepth()
    {
        if ( traversalDepth > 0 )
        {
            NeoService ns = Activator.getDefault().getNeoServiceSafely();
            if ( ns != null )
            {
                Transaction txn = ns.beginTx();
                try
                {
                    traversalDepth--;
                    refreshViewer();
                    viewer.applyLayout();
                }
                finally
                {
                    txn.finish();
                }
            }
            if ( traversalDepth < 1 )
            {
                decAction.setEnabled( false );
            }
        }
    }

    /**
     * Refreshes the view.
     */
    public void refresh()
    {
        NeoService ns = Activator.getDefault().getNeoServiceSafely();
        if ( ns != null )
        {
            Transaction txn = ns.beginTx();
            try
            {
                refreshViewer();
                viewer.applyLayout();
            }
            finally
            {
                txn.finish();
            }
        }
    }

    /**
     * Refreshes the view without changing the layout.
     */
    public void refreshPreserveLayout()
    {
        NeoService ns = Activator.getDefault().getNeoServiceSafely();
        if ( ns != null )
        {
            Transaction tn = ns.beginTx();
            try
            {
                refreshViewer();
            }
            finally
            {
                tn.finish();
            }
        }
    }

    /**
     * Refresh viewer and status bar as well.
     */
    private void refreshViewer()
    {
        disableDelete();
        viewer.refresh();
        refreshStatusBar();
    }

    public void refresh( boolean updateLabels )
    {
        disableDelete();
        viewer.refresh( updateLabels );
        refreshStatusBar();
    }

    public void refresh( Object element, boolean updateLabels )
    {
        disableDelete();
        viewer.refresh( element, updateLabels );
        refreshStatusBar();
    }

    private void disableDelete()
    {
        currentSelectedNodes.clear();
        currentSelectedRels.clear();
        updateMenuState();
    }

    /**
     * Get label provider.
     * @return current label provider
     */
    public NeoGraphLabelProvider getLabelProvider()
    {
        return (NeoGraphLabelProvider) viewer.getLabelProvider();
    }

    /**
     * Get browser history.
     * @return
     */
    public BrowserHistory getBrowserHistory()
    {
        if ( browserHistory == null )
        {
            browserHistory = new BrowserHistory();
        }
        return browserHistory;
    }

    /**
     * Set new input for the view.
     * @param node
     *            the node to use as input/start
     */
    public void setInput( Node node )
    {
        viewer.setInput( node );
        if ( node != null )
        {
            notifyListeners( node );
            getBrowserHistory().add( node );
        }
        updateNavStatus();
    }

    /**
     * Update navigation buttons according to current status. Must be called
     * after all browser history-related operations.
     */
    private void updateNavStatus()
    {
        backAction.setEnabled( getBrowserHistory().hasPrevious() );
        forwardAction.setEnabled( getBrowserHistory().hasNext() );
    }

    /**
     * Updates the view according to service changes.
     */
    private class NeoGraphServiceEventListener implements
        NeoServiceEventListener
    {
        /**
         * Refreshes the input source of the view.
         */
        public void serviceChanged( NeoServiceEvent event )
        {
            if ( event.getStatus() == NeoServiceStatus.STOPPED )
            {
                // throw away old relationship colors
                NeoGraphLabelProviderWrapper.getInstance()
                    .refreshRelationshipColors();
                // when called during shutdown the content provider may already
                // have been disposed
                if ( getViewer().getContentProvider() != null )
                {
                    getViewer().setInput( null );
                }
            }
            else if ( event.getStatus() == NeoServiceStatus.STARTED )
            {
                showSomeNode();
            }
        }
    }

    /**
     * Handles double clicks on graph figures.
     */
    private class NeoGraphDoubleClickListener implements IDoubleClickListener
    {
        /**
         * Sets the selected node as input for the viewer.
         */
        public void doubleClick( DoubleClickEvent event )
        {
            StructuredSelection sel = (StructuredSelection) event
                .getSelection();
            Object s = sel.getFirstElement();
            if ( (s != null) && (s instanceof Node) )
            {
                Node node = (Node) s;
                Transaction txn = Activator.getDefault().beginNeoTxSafely();
                if ( txn == null )
                {
                    return;
                }
                try
                {
                    if ( viewer != event.getViewer() )
                    {
                        throw new IllegalStateException(
                            "Double click event comes from wrong view." );
                    }
                    setInput( node );
                    refreshStatusBar();
                }
                finally
                {
                    txn.finish();
                }
            }
        }
    }

    /**
     * Class to handle changes in selection of this view.
     * @author Anders Nawroth
     */
    private class SelectionChangeHandler implements ISelectionListener
    {
        /**
         * Handles selection, making the context menu look right.
         */
        public void selectionChanged( IWorkbenchPart part, ISelection selection )
        {
            currentSelectedNodes.clear();
            currentSelectedRels.clear();
            if ( !(selection instanceof IStructuredSelection) )
            {
                updateMenuState();
                return;
            }
            IStructuredSelection structSel = (IStructuredSelection) selection;
            Iterator<?> iter = structSel.iterator();
            while ( iter.hasNext() )
            {
                Object o = iter.next();
                if ( o instanceof Node )
                {
                    currentSelectedNodes.add( (Node) o );
                }
                else if ( o instanceof Relationship )
                {
                    currentSelectedRels.add( (Relationship) o );
                }
            }
            updateMenuState();
        }
    }

    /**
     * Class that handles changes in the relationship properties view.
     */
    private class RelTypeSelectionChangeHandler implements ISelectionListener
    {
        /**
         * Handles selection, just updating the relTypeView reference.
         */
        public void selectionChanged( IWorkbenchPart part, ISelection selection )
        {
            if ( part instanceof RelationshipTypeView )
            {
                relTypeView = (RelationshipTypeView) part;
            }
        }
    }

    public List<Node> getCurrentSelectedNodes()
    {
        return currentSelectedNodes;
    }

    public List<Relationship> getCurrentSelectedRels()
    {
        return currentSelectedRels;
    }

    /**
     * Class that responds to changes in properties.
     */
    private class PropertyChangeHandler implements ChangeListener
    {
        /**
         * Handle change in properties.
         */
        public void handleStateChanged( ChangeEvent event )
        {
            refresh( event.getSource(), true );
        }
    }

    public RelationshipTypeView getRelTypeView()
    {
        return relTypeView;
    }

    /**
     * Class that responds to changes in preferences.
     */
    private class PreferenceChangeHandler implements IPropertyChangeListener
    {
        /**
         * Forward event, then refresh view.
         */
        public void propertyChange( PropertyChangeEvent event )
        {
            if ( NeoGraphLabelProviderWrapper.getInstance().propertyChanged(
                event ) )
            {
                refresh( true );
            }
        }
    }
}
