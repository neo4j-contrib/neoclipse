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
package org.neo4j.neoclipse.reltype;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.neo4j.api.core.Direction;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.RelationshipType;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.action.Actions;
import org.neo4j.neoclipse.action.reltype.NewRelationshipTypeAction;
import org.neo4j.neoclipse.decorate.UserIcons;
import org.neo4j.neoclipse.event.NeoclipseEvent;
import org.neo4j.neoclipse.event.NeoclipseEventListener;
import org.neo4j.neoclipse.help.HelpContextConstants;
import org.neo4j.neoclipse.neo.NeoServiceEvent;
import org.neo4j.neoclipse.neo.NeoServiceEventListener;
import org.neo4j.neoclipse.neo.NeoServiceStatus;
import org.neo4j.neoclipse.neo.NodeSpaceUtil;
import org.neo4j.neoclipse.preference.NeoDecoratorPreferences;
import org.neo4j.neoclipse.view.NeoGraphLabelProvider;
import org.neo4j.neoclipse.view.NeoGraphLabelProviderWrapper;
import org.neo4j.neoclipse.view.NeoGraphViewPart;

/**
 * View that shows the relationships of the database.
 * @author anders
 */
public class RelationshipTypeView extends ViewPart implements
    ISelectionListener
{
    public final static String ID = "org.neo4j.neoclipse.reltype.RelationshipTypeView";
    private static final Separator SEPARATOR = new Separator();
    private static final String[] EXT_FILTER;
    private static final String[] EXT_FILTER_NAMES;
    private final NeoGraphLabelProvider graphLabelProvider = NeoGraphLabelProviderWrapper
        .getInstance();

    private TableViewer viewer;
    private RelationshipTypesProvider provider;
    private NeoGraphViewPart graphView = null;

    private Action markIncomingAction;
    private Action markOutgoingAction;
    private Action clearMarkedAction;
    private Action markRelationshipAction;
    private Action newAction;
    private Action addRelationship;
    private Action addOutgoingNode;
    private Action addIncomingNode;
    private Action addIncomingIcon;
    private Action addOutgoingIcon;
    private Action filterNone;
    private Action filterAll;
    private Action filterOutgoing;
    private Action filterIncoming;

    private final List<RelationshipType> currentSelectedRelTypes = new ArrayList<RelationshipType>();

    static
    {
        // build filters for file selection dialog.
        StringBuilder str = new StringBuilder( 128 );
        for ( String ext : UserIcons.EXTENSIONS )
        {
            str.append( "*." ).append( ext ).append( ';' );
        }
        EXT_FILTER = new String[] { str.toString() };
        EXT_FILTER_NAMES = new String[] { "Image files" };
    }

    /**
     * The constructor.
     */
    public RelationshipTypeView()
    {
    }

    /**
     * Set the current graph view.
     * @param graphView
     */
    private void setGraphView( final NeoGraphViewPart graphView )
    {
        this.graphView = graphView;
    }

    /**
     * Get the current graph view.
     * @return
     */
    private NeoGraphViewPart getGraphView()
    {
        if ( graphView == null )
        {
            graphView = (NeoGraphViewPart) PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getActivePage().findView(
                    NeoGraphViewPart.ID );
            graphView
                .addRelColorChangeListener( new RelationshipColorChangeHandler() );
        }
        return graphView;
    }

    /**
     * Initialization of the workbench part.
     */
    @Override
    public void createPartControl( final Composite parent )
    {
        viewer = new TableViewer( parent, SWT.MULTI | SWT.V_SCROLL );
        provider = RelationshipTypesProviderWrapper.getInstance();
        viewer.setContentProvider( provider );
        provider.addFilterStatusListener( new ProviderFilterChangeHandler() );
        provider.addTypeChangeListener( new ProviderTypesChangeHandler() );
        NeoGraphLabelProvider labelProvider = NeoGraphLabelProviderWrapper
            .getInstance();
        labelProvider.createTableColumns( viewer );
        viewer.setLabelProvider( labelProvider );
        viewer.setComparator( new ViewerComparator(
            new RelationshipTypeSorter() ) );
        viewer.setInput( getViewSite() );

        Activator.getDefault().getNeoServiceManager().addServiceEventListener(
            new ServiceChangeHandler() );

        PlatformUI.getWorkbench().getHelpSystem().setHelp( viewer.getControl(),
            HelpContextConstants.NEO_RELATIONSHIP_TYPE_VIEW );
        makeActions();
        hookContextMenu();
        hookDoubleClickAction();
        contributeToActionBars();
        getSite().getPage().addSelectionListener( NeoGraphViewPart.ID, this );

        getSite().setSelectionProvider( viewer );
        getSite().getPage().addSelectionListener( ID, this );
    }

    /**
     * Hook the double click listener into the view.
     */
    private void hookDoubleClickAction()
    {
        viewer.addDoubleClickListener( new IDoubleClickListener()
        {
            public void doubleClick( final DoubleClickEvent event )
            {
                markRelationshipAction.run();
            }
        } );
    }

    /**
     * Create and hook up the context menu.
     */
    private void hookContextMenu()
    {
        MenuManager menuMgr = new MenuManager( "#PopupMenu" );
        menuMgr.setRemoveAllWhenShown( true );
        menuMgr.addMenuListener( new IMenuListener()
        {
            public void menuAboutToShow( final IMenuManager manager )
            {
                RelationshipTypeView.this.fillContextMenu( manager );
            }
        } );
        Menu menu = menuMgr.createContextMenu( viewer.getControl() );
        viewer.getControl().setMenu( menu );
        getSite().registerContextMenu( menuMgr, viewer );
    }

    /**
     * Add contributions to the different actions bars.
     */
    private void contributeToActionBars()
    {
        IActionBars bars = getViewSite().getActionBars();
        fillLocalPullDown( bars.getMenuManager() );
        fillLocalToolBar( bars.getToolBarManager() );
    }

    /**
     * Add actions to the local pull down menu.
     * @param manager
     *            the pul down menu manager
     */
    private void fillLocalPullDown( final IMenuManager manager )
    {
        manager.add( filterNone );
        manager.add( filterIncoming );
        manager.add( filterOutgoing );
        manager.add( filterAll );
    }

    /**
     * Add actions to the local tool bar menu.
     * @param manager
     *            the tool bar manager
     */
    private void fillLocalToolBar( final IToolBarManager manager )
    {
        manager.add( markRelationshipAction );
        manager.add( markIncomingAction );
        manager.add( markOutgoingAction );
        manager.add( clearMarkedAction );
        manager.add( SEPARATOR );
        manager.add( addRelationship );
        manager.add( addOutgoingNode );
        manager.add( addIncomingNode );
        manager.add( SEPARATOR );
        manager.add( newAction );
    }

    /**
     * Add actions to the context menu.
     * @param manager
     *            contect menu manager
     */
    private void fillContextMenu( final IMenuManager manager )
    {
        manager.add( addIncomingIcon );
        manager.add( addOutgoingIcon );
        // Other plug-ins can contribute there actions here
        manager.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );
    }

    /**
     * Create all actions.
     */
    private void makeActions()
    {
        makeHighlightingActions();

        makeRelationshipTypeActions();

        makeAddActions();

        makeFilterActions();
    }

    /**
     * Create actions to filter on relationship directions.
     */
    private void makeFilterActions()
    {
        filterNone = new Action()
        {
            @Override
            public void run()
            {
                provider.setAllFilters( false, false );
                viewer.refresh();
            }
        };
        Actions.FILTER_NONE.initialize( filterNone );

        filterAll = new Action()
        {
            @Override
            public void run()
            {
                provider.setAllFilters( true, true );
                viewer.refresh();
                getGraphView().refresh();
            }
        };
        Actions.FILTER_ALL.initialize( filterAll );

        filterOutgoing = new Action()
        {
            @Override
            public void run()
            {
                provider.setAllFilters( false, true );
                viewer.refresh();
                getGraphView().refresh();
            }
        };
        Actions.FILTER_OUTGOING.initialize( filterOutgoing );

        filterIncoming = new Action()
        {
            @Override
            public void run()
            {
                provider.setAllFilters( true, false );
                viewer.refresh();
                getGraphView().refresh();
            }
        };
        Actions.FILTER_INCOMING.initialize( filterIncoming );
    }

    /**
     * Create actions that add something.
     */
    private void makeAddActions()
    {
        addRelationship = new Action()
        {
            @Override
            public void run()
            {
                NodeSpaceUtil.addRelationshipAction(
                    getCurrentSelectedRelTypes(), getGraphView() );
            }
        };
        Actions.ADD_RELATIONSHIP.initialize( addRelationship );

        addOutgoingNode = new Action()
        {
            @Override
            public void run()
            {
                NodeSpaceUtil.addOutgoingNodeAction(
                    getCurrentSelectedRelTypes(), getGraphView() );
            }
        };
        Actions.ADD_OUTGOING_NODE.initialize( addOutgoingNode );

        addIncomingNode = new Action()
        {
            @Override
            public void run()
            {
                NodeSpaceUtil.addIncomingNodeAction(
                    getCurrentSelectedRelTypes(), getGraphView() );
            }
        };
        Actions.ADD_INCOMING_NODE.initialize( addIncomingNode );
    }

    /**
     * Create actions that affect relationship types.
     */
    private void makeRelationshipTypeActions()
    {
        newAction = new NewRelationshipTypeAction( provider );

        addIncomingIcon = new Action()
        {
            @Override
            public void run()
            {
                copyIcon( Direction.INCOMING );
            }
        };
        Actions.ADD_INCOMING_ICON.initialize( addIncomingIcon );

        addOutgoingIcon = new Action()
        {
            @Override
            public void run()
            {
                copyIcon( Direction.OUTGOING );
            }
        };
        Actions.ADD_OUTGOING_ICON.initialize( addOutgoingIcon );
    }

    /**
     * Copy icon file for current selected relationship types.
     * @param direction
     *            direction of relationships
     */
    private void copyIcon( final Direction direction )
    {
        File dest = getIconLocation();
        if ( !dest.exists() || !dest.isDirectory() )
        {
            MessageDialog
                .openInformation( null, "Icon location problem",
                    "Please make sure that the node icon location is correctly set." );
            Activator.getDefault().showDecoratorPreferenceDialog( true );
            dest = getIconLocation();
            if ( !dest.exists() || !dest.isDirectory() )
            {
                MessageDialog.openError( null, "Error message",
                    "The icon location can not be found." );
                return;
            }
        }
        FileDialog fd = new FileDialog( RelationshipTypeView.this.getSite()
            .getShell(), SWT.OPEN );
        fd.setFilterExtensions( EXT_FILTER );
        fd.setFilterNames( EXT_FILTER_NAMES );
        String src = fd.open();
        if ( src == null )
        {
            return; // cancel by user
        }
        int dot = src.lastIndexOf( '.' );
        if ( dot == -1 )
        {
            MessageDialog.openError( null, "Error message",
                "Could not find a file extension on the icon image file." );
            return;
        }
        String ext = src.substring( dot ); // includes dot
        File inFile = new File( src );
        FileChannel in = null;
        try
        {
            in = new FileInputStream( inFile ).getChannel();
        }
        catch ( FileNotFoundException e1 )
        {
            e1.printStackTrace();
            return;
        }
        for ( RelationshipType relType : getCurrentSelectedRelTypes() )
        {
            String destFilename = UserIcons.createFilename( relType, direction )
                + ext;
            FileChannel out = null;
            try
            {
                out = new FileOutputStream( dest.getAbsolutePath()
                    + File.separator + destFilename ).getChannel();
            }
            catch ( FileNotFoundException e )
            {
                e.printStackTrace();
                continue;
            }
            try
            {
                in.transferTo( 0, in.size(), out );
                out.close();
            }
            catch ( IOException e )
            {
                e.printStackTrace();
                return;
            }
        }
        try
        {
            in.close();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        graphLabelProvider.readNodeIconLocation();
        getGraphView().refreshPreserveLayout();
    }

    /**
     * Get icons location from the settings.
     * @return
     */
    private File getIconLocation()
    {
        String location = Activator.getDefault().getPreferenceStore()
            .getString( NeoDecoratorPreferences.NODE_ICON_LOCATION );
        File dest = new File( location );
        return dest;
    }

    /**
     * Create actions working with highlighting.
     */
    private void makeHighlightingActions()
    {
        markRelationshipAction = new Action()
        {
            @Override
            public void run()
            {
                List<RelationshipType> relTypes = getCurrentSelectedRelTypes();
                for ( RelationshipType relType : relTypes )
                {
                    highlightRelationshipType( relType );
                }
                setEnableHighlightingActions( true );
                clearMarkedAction.setEnabled( true );
            }
        };
        Actions.HIGHLIGHT_RELATIONSHIPS.initialize( markRelationshipAction );

        markIncomingAction = new Action()
        {
            @Override
            public void run()
            {
                List<RelationshipType> relTypes = getCurrentSelectedRelTypes();
                for ( RelationshipType relType : relTypes )
                {
                    highlightNodes( relType, Direction.INCOMING );
                }
                clearMarkedAction.setEnabled( true );
            }
        };
        Actions.HIGHLIGHT_INCOMING.initialize( markIncomingAction );
        markIncomingAction.setEnabled( false );

        markOutgoingAction = new Action()
        {
            @Override
            public void run()
            {
                List<RelationshipType> relTypes = getCurrentSelectedRelTypes();
                for ( RelationshipType relType : relTypes )
                {
                    highlightNodes( relType, Direction.OUTGOING );
                }
                clearMarkedAction.setEnabled( true );
            }
        };
        Actions.HIGHLIGHT_OUTGOING.initialize( markOutgoingAction );
        markOutgoingAction.setEnabled( false );

        clearMarkedAction = new Action()
        {
            @Override
            public void run()
            {
                graphLabelProvider.clearMarkedNodes();
                graphLabelProvider.clearMarkedRels();
                getGraphView().refresh( true );
                setEnabled( false );
                setEnableAddActions( false );
            }
        };
        Actions.HIGHLIGHT_CLEAR.initialize( clearMarkedAction );
        clearMarkedAction.setEnabled( false );
    }

    /**
     * Enable or disable highlighting actions.
     * @param enabled
     */
    private void setEnableHighlightingActions( final boolean enabled )
    {
        markIncomingAction.setEnabled( enabled );
        markOutgoingAction.setEnabled( enabled );
        markRelationshipAction.setEnabled( enabled );
    }

    /**
     * Enable or disable addition of a relationship.
     * @param enabled
     */
    private void setEnableAddRelationship( final boolean enabled )
    {
        addRelationship.setEnabled( enabled );
    }

    /**
     * Enable or disable setting of relationship type-dependent icons.
     * @param enabled
     */
    private void setEnableSetIcon( final boolean enabled )
    {
        addIncomingIcon.setEnabled( enabled );
        addOutgoingIcon.setEnabled( enabled );
    }

    /**
     * Enable or disable to add a node.
     * @param enabled
     */
    private void setEnableAddNode( final boolean enabled )
    {
        addOutgoingNode.setEnabled( enabled );
        addIncomingNode.setEnabled( enabled );
    }

    /**
     * Enable or disable all add actions.
     * @param enabled
     */
    private void setEnableAddActions( final boolean enabled )
    {
        setEnableAddNode( enabled );
        setEnableAddRelationship( enabled );
    }

    /**
     * Get the currently first selected relationship type.
     * @return
     */
    public RelationshipType getCurrentSelectedRelType()
    {
        if ( currentSelectedRelTypes.size() < 1 )
        {
            return null;
        }
        return currentSelectedRelTypes.get( 0 );
    }

    /**
     * Get the currently selected relationship types.
     * @return
     */
    public List<RelationshipType> getCurrentSelectedRelTypes()
    {
        return currentSelectedRelTypes;
    }

    /**
     * Highlight a relationship type.
     * @param relType
     */
    private void highlightRelationshipType( final RelationshipType relType )
    {
        if ( getGraphView() == null )
        {
            return;
        }
        List<Relationship> rels = new ArrayList<Relationship>();
        GraphViewer gViewer = getGraphView().getViewer();
        for ( Object o : gViewer.getConnectionElements() )
        {
            if ( o instanceof Relationship )
            {
                Relationship rel = (Relationship) o;
                if ( rel.isType( relType ) )
                {
                    rels.add( rel );
                }
            }
        }
        graphLabelProvider.addMarkedRels( rels );
        getGraphView().refresh( true );
        setEnableAddActions( false );
    }

    /**
     * Highlight nodes that are connected to a relationship type.
     * @param relType
     *            relationship type to use
     * @param direction
     *            direction in which nodes should be highlighted
     */
    private void highlightNodes( final RelationshipType relType,
        final Direction direction )
    {
        if ( getGraphView() == null )
        {
            return;
        }
        GraphViewer gViewer = getGraphView().getViewer();
        Set<Node> nodes = new HashSet<Node>();
        for ( Object o : gViewer.getNodeElements() )
        {
            if ( o instanceof Node )
            {
                Node node = (Node) o;
                if ( node.hasRelationship( relType, direction ) )
                {
                    nodes.add( node );
                }
            }
        }
        graphLabelProvider.addMarkedNodes( nodes );
        getGraphView().refresh( true );
        setEnableAddActions( false );
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    @Override
    public void setFocus()
    {
        viewer.getControl().setFocus();
    }

    /**
     * Keep track of the graph view selections.
     */
    public void selectionChanged( final IWorkbenchPart part,
        final ISelection selection )
    {
        if ( !(selection instanceof IStructuredSelection) )
        {
            return;
        }
        setEnableAddRelationship( false );
        setEnableAddNode( false );
        if ( part instanceof NeoGraphViewPart )
        {
            setGraphView( (NeoGraphViewPart) part );
            List<Relationship> currentSelectedRels = getGraphView()
                .getCurrentSelectedRels();
            Set<RelationshipType> relTypes = new HashSet<RelationshipType>();
            for ( Relationship rel : currentSelectedRels )
            {
                relTypes.add( rel.getType() );
            }
            if ( !relTypes.isEmpty() )
            {
                Collection<RelationshipTypeControl> relTypeCtrls = provider
                    .getFilteredControls( relTypes );
                viewer.setSelection( new StructuredSelection( relTypeCtrls
                    .toArray() ) );
                setEnableHighlightingActions( true );
            }
        }
        else if ( this.equals( part ) )
        {
            if ( selection.isEmpty() )
            {
                setEnableHighlightingActions( false );
            }
            else
            {
                setEnableHighlightingActions( true );
            }
            currentSelectedRelTypes.clear();
            Iterator<?> iter = ((IStructuredSelection) selection).iterator();
            while ( iter.hasNext() )
            {
                Object o = iter.next();
                if ( o instanceof RelationshipTypeControl )
                {
                    currentSelectedRelTypes.add( ((RelationshipTypeControl) o)
                        .getRelType() );
                }
            }
        }

        List<Node> currentSelectedNodes = getGraphView()
            .getCurrentSelectedNodes();
        setEnableAddRelationship( getCurrentSelectedRelTypes().size() == 1
            && currentSelectedNodes.size() == 2 );
        setEnableAddNode( getCurrentSelectedRelTypes().size() == 1
            && !currentSelectedNodes.isEmpty() );
        setEnableSetIcon( !getCurrentSelectedRelTypes().isEmpty() );
        getGraphView().updateMenuState();
    }

    /**
     * Respond to changes in the relationship type provider filtering.
     */
    private class ProviderFilterChangeHandler implements NeoclipseEventListener
    {
        /**
         * Respond to changes in the underlying relationship type provider.
         */
        public void stateChanged( final NeoclipseEvent event )
        {
            if ( getGraphView() != null )
            {
                getGraphView().refreshPreserveLayout();
            }
        }
    }

    /**
     * Respond to changes in the relationship type provider types.
     */
    private class ProviderTypesChangeHandler implements NeoclipseEventListener
    {
        /**
         * Respond to changes in the underlying relationship type provider.
         */
        public void stateChanged( final NeoclipseEvent event )
        {
            viewer.refresh();
        }
    }

    /**
     * Handle change in the Neo service.
     */
    private class ServiceChangeHandler implements NeoServiceEventListener
    {
        public void serviceChanged( final NeoServiceEvent event )
        {
            if ( event.getStatus() == NeoServiceStatus.STOPPED )
            {
                provider.refresh();
            }
            else if ( event.getStatus() == NeoServiceStatus.STARTED )
            {
                viewer.refresh( true );
            }
            else if ( event.getStatus() == NeoServiceStatus.ROLLBACK )
            {
                provider.refresh();
                viewer.refresh( true );
            }
        }
    }

    /**
     * Handle change in the relationship color settings.
     */
    private class RelationshipColorChangeHandler implements
        NeoclipseEventListener
    {
        public void stateChanged( final NeoclipseEvent event )
        {
            viewer.refresh( true );
        }
    }
}