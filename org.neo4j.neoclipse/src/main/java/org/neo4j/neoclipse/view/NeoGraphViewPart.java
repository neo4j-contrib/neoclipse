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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
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
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.event.NeoclipseEvent;
import org.neo4j.neoclipse.event.NeoclipseEventListener;
import org.neo4j.neoclipse.event.NeoclipseListenerList;
import org.neo4j.neoclipse.help.HelpContextConstants;
import org.neo4j.neoclipse.neo.NeoServiceEvent;
import org.neo4j.neoclipse.neo.NeoServiceEventListener;
import org.neo4j.neoclipse.neo.NeoServiceManager;
import org.neo4j.neoclipse.neo.NeoServiceStatus;
import org.neo4j.neoclipse.preference.NeoPreferences;
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
     * Max number of nodes to try to find a better starting point.
     */
    private static final int MAX_NODE_TRIES = 1000;
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
     * The depth how deep we should traverse into the network.
     */
    private int traversalDepth = 1;
    private final List<Node> currentSelectedNodes = new ArrayList<Node>();
    private final List<Relationship> currentSelectedRels = new ArrayList<Relationship>();
    private RelationshipTypeView relTypeView;
    private final List<InputChangeListener> listeners = new ArrayList<InputChangeListener>();
    private Node previousInputNode = null;

    private final NeoclipseListenerList relColorChange = new NeoclipseListenerList();
    private NeoGraphMenu menu;
    /**
     * Keep track of the current database state.
     */
    private boolean dirty = false;

    /**
     * Creates the view.
     */
    @Override
    public void createPartControl( final Composite parent )
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

        menu = new NeoGraphMenu( this );

        NeoServiceManager sm = Activator.getDefault().getNeoServiceManager();
        sm.addServiceEventListener( new NeoGraphServiceEventListener() );
        getSite().setSelectionProvider( viewer );

        Activator.getDefault().getPluginPreferences()
            .addPropertyChangeListener( new PreferenceChangeHandler() );

        showSomeNode();

        PlatformUI.getWorkbench().getHelpSystem().setHelp( viewer.getControl(),
            HelpContextConstants.NEO_GRAPH_VIEW_PART );
    }

    /**
     * Get current relationship type view.
     * @return
     */
    public RelationshipTypeView getRelTypeView()
    {
        return relTypeView;
    }

    /**
     * Set the current relationship type view.
     * @param relTypeView
     */
    private void setRelTypeView( final RelationshipTypeView relTypeView )
    {
        this.relTypeView = relTypeView;
    }

    /**
     * Add listener for input changes.
     * @param listener
     */
    public void addListener( final InputChangeListener listener )
    {
        listeners.add( listener );
    }

    /**
     * Add listener for changes in the relationship colors setting.
     * @param listener
     */
    public void addRelColorChangeListener( final NeoclipseEventListener listener )
    {
        relColorChange.add( listener );
    }

    /**
     * Notify listeners of new input node.
     * @param node
     */
    private void notifyListeners( final Node node )
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
     * Show or hide relationship colors.
     * @param state
     *            set true to display
     */
    public void setShowRelationshipColors( final boolean state )
    {
        getLabelProvider().getViewSettings().setShowRelationshipColors( state );
        relColorChange.notifyListeners( new NeoclipseEvent( Boolean
            .valueOf( state ) ) );
        refreshPreserveLayout();
    }

    /**
     * Set context menu to the correct state.
     */
    public void updateMenuState()
    {
        int selectedNodeCount = currentSelectedNodes.size();
        int selectedRelationshipCount = currentSelectedRels.size();
        menu.setEnableDeleteAction( selectedNodeCount > 0
            || selectedRelationshipCount > 0 );

        boolean rel = selectedNodeCount == 2;
        boolean outIn = selectedNodeCount > 0;
        menu.setEnabledRelActions( rel, outIn, outIn );
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
        Node refNode = Activator.getDefault().getReferenceNode();
        if ( refNode != null )
        {
            return refNode;
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
     * Updates the content of the status bar.
     */
    public void refreshStatusBar()
    {
        StringBuilder str = new StringBuilder( 64 );
        str.append( "Traversal depth: " ).append( traversalDepth );
        str.append( "   Nodes: " ).append(
            viewer.getGraphControl().getNodes().size() );
        str.append( "   Relationships: " ).append(
            viewer.getGraphControl().getConnections().size() );

        getViewSite().getActionBars().getStatusLineManager().setMessage(
            str.toString() );
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
    @Override
    @SuppressWarnings( "unchecked" )
    public Object getAdapter( final Class key )
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
    @Override
    public void dispose()
    {
        cleanTransactionBeforeShutdown();
        if ( propertySheetPage != null )
        {
            propertySheetPage.dispose();
        }
        super.dispose();
    }

    /**
     * Make sure the transaction is clean before Neo is to be
     * shutdown/restarted.
     */
    private void cleanTransactionBeforeShutdown()
    {
        if ( !dirty )
        {
            return; // no need to do anything
        }
        NeoServiceManager sm = Activator.getDefault().getNeoServiceManager();
        if ( MessageDialog
            .openQuestion(
                null,
                "Database changed",
                "There are changes that are not commited to the database. Do you want to commit (save) them?" ) )
        {
            sm.commit();
        }
        else
        {
            sm.rollback();
        }
    }

    /**
     * Sets the focus.
     */
    @Override
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
        Node node = Activator.getDefault().getReferenceNode();
        setInput( node );
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
            Node node = ns.getReferenceNode();
            if ( !node.hasRelationship() )
            {
                // so, find a more friendly node if possible!
                int tries = 0;
                for ( Node candidateNode : ns.getAllNodes() )
                {
                    if ( node.equals( candidateNode ) )
                    {
                        continue;
                    }
                    if ( candidateNode.hasRelationship() )
                    {
                        node = candidateNode;
                        break;
                    }
                    if ( tries++ > MAX_NODE_TRIES )
                    {
                        break;
                    }
                }
            }
            setInput( node );
        }
    }

    /**
     * Focuses the view on the node with the given id.
     */
    public void showNode( final long nodeId )
    {
        NeoService ns = Activator.getDefault().getNeoServiceSafely();
        if ( ns != null )
        {
            Node node = ns.getNodeById( nodeId );
            setInput( node );
        }
    }

    /**
     * Focuses the view on the given node.
     */
    public void showNode( final Node node )
    {
        setInput( node );
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
        traversalDepth++;
        refreshViewer();
        viewer.applyLayout();
        if ( traversalDepth > 0 )
        {
            menu.setEnabledDecAction( true );
        }
    }

    /**
     * Decrements the traversal depth.
     */
    public void decTraversalDepth()
    {
        if ( traversalDepth > 0 )
        {
            traversalDepth--;
            refreshViewer();
            viewer.applyLayout();

            if ( traversalDepth < 1 )
            {
                menu.setEnabledDecAction( false );
            }
        }
    }

    /**
     * Refreshes the view.
     */
    public void refresh()
    {
        refreshViewer();
        viewer.applyLayout();
    }

    /**
     * Refreshes the view without changing the layout.
     */
    public void refreshPreserveLayout()
    {
        refreshViewer();
    }

    /**
     * Refresh viewer and status bar as well.
     */
    private void refreshViewer()
    {
        refresh( false );
    }

    public void refresh( final boolean updateLabels )
    {
        disableDelete();
        viewer.refresh( updateLabels );
        if ( viewer.getGraphControl().getNodes().size() == 0 )
        {
            // will take care of if the input node
            // gets deleted or disappears in a rollback
            showSomeNode();
        }
        refreshStatusBar();
    }

    /**
     * Refresh the graph view.
     * @param element
     * @param updateLabels
     */
    public void refresh( final Object element, final boolean updateLabels )
    {
        disableDelete();
        viewer.refresh( element, updateLabels );
        refreshStatusBar();
    }

    /**
     * Disable the delete action. (we need this in some places to avoid ending
     * up in an inconsistent state due to bugs in the underlying frameworks)
     */
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
    public void setInput( final Node node )
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
        menu.setEnabledBackAction( getBrowserHistory().hasPrevious() );
        menu.setEnabledForwardAction( getBrowserHistory().hasNext() );
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
        public void serviceChanged( final NeoServiceEvent event )
        {
            if ( event.getStatus() == NeoServiceStatus.STOPPED )
            {
                // when called during shutdown the content provider may already
                // have been disposed
                if ( getViewer().getContentProvider() != null )
                {
                    getViewer().setInput( null );
                }
            }
            else if ( event.getStatus() == NeoServiceStatus.STARTED )
            {
                // throw away old relationship colors
                getLabelProvider().refreshRelationshipColors();
                showSomeNode();
            }
            else if ( event.getStatus() == NeoServiceStatus.ROLLBACK )
            {
                refreshPreserveLayout();
                setDirty( false );
            }
            else if ( event.getStatus() == NeoServiceStatus.COMMIT )
            {
                setDirty( false );
            }
        }
    }

    /**
     * Update UI according to the state of the database.
     * @param dirty
     */
    public void setDirty( final boolean dirty )
    {
        this.dirty = dirty;
        menu.setEnabledCommitAction( dirty );
        menu.setEnabledRollbackAction( dirty );
    }

    /**
     * Handles double clicks on graph figures.
     */
    private class NeoGraphDoubleClickListener implements IDoubleClickListener
    {
        /**
         * Sets the selected node as input for the viewer.
         */
        public void doubleClick( final DoubleClickEvent event )
        {
            StructuredSelection sel = (StructuredSelection) event
                .getSelection();
            Object s = sel.getFirstElement();
            if ( (s != null) && (s instanceof Node) )
            {
                Node node = (Node) s;
                if ( viewer != event.getViewer() )
                {
                    throw new IllegalStateException(
                        "Double click event comes from wrong view." );
                }
                setInput( node );
                refreshStatusBar();
            }
        }
    }

    /**
     * Class to handle changes in selection of this view.
     */
    private class SelectionChangeHandler implements ISelectionListener
    {
        /**
         * Handles selection, making the context menu look right.
         */
        public void selectionChanged( final IWorkbenchPart part,
            final ISelection selection )
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
        public void selectionChanged( final IWorkbenchPart part,
            final ISelection selection )
        {
            if ( part instanceof RelationshipTypeView )
            {
                setRelTypeView( (RelationshipTypeView) part );
            }
        }
    }

    /**
     * Get current selected nodes.
     * @return
     */
    public List<Node> getCurrentSelectedNodes()
    {
        return currentSelectedNodes;
    }

    /**
     * Get current selected relationships.
     * @return
     */
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
        public void handleStateChanged( final ChangeEvent event )
        {
            refresh( event.getSource(), true );
            setDirty( true );
        }
    }

    /**
     * Class that responds to changes in preferences.
     */
    private class PreferenceChangeHandler implements IPropertyChangeListener
    {
        /**
         * Forward event, then refresh view.
         */
        public void propertyChange( final PropertyChangeEvent event )
        {
            String property = event.getProperty();
            if ( NeoPreferences.DATABASE_LOCATION.equals( property ) )
            {
                // handle change in database location
                cleanTransactionBeforeShutdown();
                Activator.getDefault().restartNeo();
            }
            else
            {
                if ( NeoGraphLabelProviderWrapper.getInstance()
                    .propertyChanged( event ) )
                {
                    refresh( true );
                }
            }
        }
    }
}
