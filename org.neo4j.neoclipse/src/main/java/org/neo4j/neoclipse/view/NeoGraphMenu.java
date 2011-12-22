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
package org.neo4j.neoclipse.view;

import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.action.Actions;
import org.neo4j.neoclipse.action.PrintGraphAction;
import org.neo4j.neoclipse.action.browse.GoBackAction;
import org.neo4j.neoclipse.action.browse.GoForwardAction;
import org.neo4j.neoclipse.action.browse.RefreshAction;
import org.neo4j.neoclipse.action.browse.ShowReferenceNodeAction;
import org.neo4j.neoclipse.action.connect.StartAction;
import org.neo4j.neoclipse.action.connect.StopAction;
import org.neo4j.neoclipse.action.connect.SyncAction;
import org.neo4j.neoclipse.action.context.CommitAction;
import org.neo4j.neoclipse.action.context.DeleteAction;
import org.neo4j.neoclipse.action.context.RollbackAction;
import org.neo4j.neoclipse.action.decorate.node.FilterNodePropertiesAction;
import org.neo4j.neoclipse.action.decorate.node.ShowNodeColorsAction;
import org.neo4j.neoclipse.action.decorate.node.ShowNodeIconsAction;
import org.neo4j.neoclipse.action.decorate.node.ShowNodeIdsAction;
import org.neo4j.neoclipse.action.decorate.node.ShowNodeLabelAction;
import org.neo4j.neoclipse.action.decorate.node.ShowNodePropertyKeysAction;
import org.neo4j.neoclipse.action.decorate.rel.FilterRelationshipPropertiesAction;
import org.neo4j.neoclipse.action.decorate.rel.ShowRelationshipColorsAction;
import org.neo4j.neoclipse.action.decorate.rel.ShowRelationshipDirectionsAction;
import org.neo4j.neoclipse.action.decorate.rel.ShowRelationshipIdsAction;
import org.neo4j.neoclipse.action.decorate.rel.ShowRelationshipLabelAction;
import org.neo4j.neoclipse.action.decorate.rel.ShowRelationshipPropertyKeysAction;
import org.neo4j.neoclipse.action.decorate.rel.ShowRelationshipTypesAction;
import org.neo4j.neoclipse.action.layout.ShowGridLayoutAction;
import org.neo4j.neoclipse.action.layout.ShowHorizontalShiftLayoutAction;
import org.neo4j.neoclipse.action.layout.ShowHorizontalTreeLayoutAction;
import org.neo4j.neoclipse.action.layout.ShowRadialLayoutAction;
import org.neo4j.neoclipse.action.layout.ShowSpringLayoutAction;
import org.neo4j.neoclipse.action.layout.ShowTreeLayoutAction;
import org.neo4j.neoclipse.action.reltype.NewRelationshipTypeAction;
import org.neo4j.neoclipse.action.reltype.NewRelationshipTypeAction.NodeSpaceAction;
import org.neo4j.neoclipse.action.view.DecreaseTraversalDepthAction;
import org.neo4j.neoclipse.action.view.IncreaseTraversalDepthAction;
import org.neo4j.neoclipse.action.view.ZoomAction;
import org.neo4j.neoclipse.decorate.SimpleGraphDecorator.ViewSettings;
import org.neo4j.neoclipse.event.NeoclipseEvent;
import org.neo4j.neoclipse.event.NeoclipseEventListener;
import org.neo4j.neoclipse.graphdb.GraphDbUtil;
import org.neo4j.neoclipse.reltype.RelationshipTypeHashMap;
import org.neo4j.neoclipse.reltype.RelationshipTypeSorter;
import org.neo4j.neoclipse.reltype.RelationshipTypesProvider;
import org.neo4j.neoclipse.reltype.RelationshipTypesProviderWrapper;

/**
 * Handle tool bar, view menu and context menu of the graph view.
 * 
 * @author Anders Nawroth
 */
public class NeoGraphMenu
{
    /**
     * A bundle of relationship-oriented actions.
     */
    private class ActionSet
    {
        private final Action addRel;
        private final Action addOut;
        private final Action addIn;
        private final Action addLoop;

        /**
         * Create normal actions from relationship type.
         * 
         * @param relType relationship type to use
         */
        public ActionSet( final RelationshipType relType )
        {
            final String name = relType.name();
            ImageDescriptor imgDesc;
            if ( showRelationshipColors )
            {
                imgDesc = relTypeImages.get( relType );
                if ( imgDesc == null )
                {
                    Image image = new Image( Display.getCurrent(), RELTYPE_IMG_SIZE, RELTYPE_IMG_SIZE );
                    Color color = graphView.getLabelProvider().getColor( relType );
                    GC gc = new GC( image );
                    gc.setBackground( color );
                    gc.fillRectangle( RELTYPE_POSITION, RELTYPE_POSITION, RELTYPE_DOT_SIZE, RELTYPE_DOT_SIZE );
                    gc.dispose();
                    imgDesc = ImageDescriptor.createFromImage( image );
                    relTypeImages.put( relType, imgDesc );
                }
            }
            else
            {
                imgDesc = RELTYPES_DEFAULT_IMG;
            }
            addRel = new Action( name, imgDesc )
            {
                @Override
                public void run()
                {
                    GraphDbUtil.addRelationshipAction( relType, graphView );
                }
            };
            addRel.setEnabled( true );
            addOut = new Action( name, imgDesc )
            {
                @Override
                public void run()
                {
                    GraphDbUtil.addOutgoingNodeAction( relType, graphView );
                }
            };
            addOut.setEnabled( true );
            addIn = new Action( name, imgDesc )
            {
                @Override
                public void run()
                {
                    GraphDbUtil.addIncomingNodeAction( relType, graphView );
                }
            };
            addIn.setEnabled( true );
            addLoop = new Action( name, imgDesc )
            {
                @Override
                public void run()
                {
                    GraphDbUtil.addLoopNodeAction( relType, graphView );
                }
            };
            addLoop.setEnabled( true );
        }

        /**
         * Create a "create relationship type" action set. The actions will
         * first create a relationship type, then use it in an action.
         * 
         * @param relTypesProvider relationship types provider to use
         */
        public ActionSet( final RelationshipTypesProvider relTypesProvider )
        {
            addRel = new NewRelationshipTypeAction( relTypesProvider, NodeSpaceAction.RELATIONSHIP, graphView );
            addOut = new NewRelationshipTypeAction( relTypesProvider, NodeSpaceAction.OUTGOING_NODE, graphView );
            addIn = new NewRelationshipTypeAction( relTypesProvider, NodeSpaceAction.INCOMING_NODE, graphView );
            addLoop = new NewRelationshipTypeAction( relTypesProvider, NodeSpaceAction.LOOP, graphView );
        }

        /**
         * Add action set as last item in menu managers.
         */
        public void addLast()
        {
            addRelMenuMgr.add( addRel );
            addOutNodeMenuMgr.add( addOut );
            addInNodeMenuMgr.add( addIn );
            addLoopMenuMgr.add( addLoop );
        }

        /**
         * Add action set at the specified position.
         * 
         * @param index position of addition
         */
        public void addAt( final int index )
        {
            addRelMenuMgr.insert( index, new ActionContributionItem( addRel ) );
            addOutNodeMenuMgr.insert( index, new ActionContributionItem( addOut ) );
            addInNodeMenuMgr.insert( index, new ActionContributionItem( addIn ) );
            addLoopMenuMgr.insert( index, new ActionContributionItem( addLoop ) );
        }
    }

    /**
     * A map to keep the actions sorted by relationship type name.
     */
    private final SortedMap<String, ActionSet> actionMap = new TreeMap<String, ActionSet>( new RelationshipTypeSorter() );
    /**
     * Size of colored squares for relationship types.
     */
    private static final int RELTYPE_IMG_SIZE = 16;
    /**
     * Size of the dot that represents the relationship type.
     */
    private static final int RELTYPE_DOT_SIZE = 8;
    /**
     * Position of dot.
     */
    private static final int RELTYPE_POSITION = ( RELTYPE_IMG_SIZE - RELTYPE_DOT_SIZE ) / 2;
    private final NeoGraphViewPart graphView;
    private final GraphViewer graphViewer;
    private final ActionSet addNewActionSet;
    /**
     * The go back action.
     */
    private final GoBackAction backAction;
    /**
     * The go forward action.
     */
    private final GoForwardAction forwardAction;
    /**
     * The decrease traversal depth action.
     */
    private final DecreaseTraversalDepthAction decAction;
    /**
     * The increase traversal depth action.
     */
    private final IncreaseTraversalDepthAction incAction;
    private final ShowReferenceNodeAction refNodeAction;
    private final RefreshAction refreshAction;
    private final DeleteAction deleteAction;
    private final CommitAction commitAction;
    private final RollbackAction rollbackAction;
    // menu managers
    private final MenuManager addRelMenuMgr = new MenuManager( Actions.ADD_RELATIONSHIP.label(),
            Actions.ADD_RELATIONSHIP.icon().descriptor(), "addRelSubmenu" );
    private static final MenuManager addRelMenuMgrFake = new MenuManager( Actions.ADD_RELATIONSHIP.label(),
            Actions.ADD_RELATIONSHIP.disabledIcon().descriptor(), "addRelSubmenuFake" );
    private final MenuManager addOutNodeMenuMgr = new MenuManager( Actions.ADD_OUTGOING_NODE.label(),
            Actions.ADD_OUTGOING_NODE.icon().descriptor(), "addOutNodeSubmenu" );
    private static final MenuManager addOutNodeMenuMgrFake = new MenuManager( Actions.ADD_OUTGOING_NODE.label(),
            Actions.ADD_OUTGOING_NODE.disabledIcon().descriptor(), "addOutNodeSubmenuFake" );
    private final MenuManager addInNodeMenuMgr = new MenuManager( Actions.ADD_INCOMING_NODE.label(),
            Actions.ADD_INCOMING_NODE.icon().descriptor(), "addInNodeSubmenu" );
    private static final MenuManager addInNodeMenuMgrFake = new MenuManager( Actions.ADD_INCOMING_NODE.label(),
            Actions.ADD_INCOMING_NODE.disabledIcon().descriptor(), "addInNodeSubmenuFake" );
    private final MenuManager addLoopMenuMgr = new MenuManager( Actions.ADD_LOOP.label(),
            Actions.ADD_LOOP.icon().descriptor(), "addLoopSubmenu" );
    private static final MenuManager addLoopMenuMgrFake = new MenuManager( Actions.ADD_LOOP.label(),
            Actions.ADD_LOOP.disabledIcon().descriptor(), "addLoopSubmenuFake" );
    /**
     * Colored images for the different relationship types.
     */
    private final Map<RelationshipType, ImageDescriptor> relTypeImages = new RelationshipTypeHashMap<ImageDescriptor>();
    /**
     * Default image when color is off.
     */
    private final static ImageDescriptor RELTYPES_DEFAULT_IMG;
    /**
     * Separator in menus.
     */
    private static final Separator SEPARATOR = new Separator();
    /**
     * Keep state of relationship colors.
     */
    private boolean showRelationshipColors;
    private final StartAction startAction;
    private final StopAction stopAction;
    private final SyncAction syncAction;
    static
    {
        // create gray default color
        Image image = new Image( Display.getCurrent(), RELTYPE_IMG_SIZE, RELTYPE_IMG_SIZE );
        Color color = new Color( Display.getCurrent(), 96, 96, 96 );
        GC gc = new GC( image );
        gc.setBackground( color );
        gc.fillRectangle( image.getBounds() );
        gc.dispose();
        RELTYPES_DEFAULT_IMG = ImageDescriptor.createFromImage( image );
        Action dummyAction = new Action( "(disabled)" )
        {
        };
        dummyAction.setEnabled( false );
        addRelMenuMgrFake.add( dummyAction );
        addOutNodeMenuMgrFake.add( dummyAction );
        addInNodeMenuMgrFake.add( dummyAction );
        addLoopMenuMgrFake.add( dummyAction );
    }

    /**
     * Create a menu for the given Neo4j graph view.
     * 
     * @param graphView graph view to create menu parts for
     */
    public NeoGraphMenu( final NeoGraphViewPart graphView )
    {
        this.graphView = graphView;
        graphViewer = graphView.getViewer();
        deleteAction = new DeleteAction( graphView );
        backAction = new GoBackAction( graphView );
        forwardAction = new GoForwardAction( graphView );
        decAction = new DecreaseTraversalDepthAction( graphView );
        incAction = new IncreaseTraversalDepthAction( graphView );
        startAction = new StartAction( graphView );
        stopAction = new StopAction( graphView );
        commitAction = new CommitAction( graphView );
        rollbackAction = new RollbackAction( graphView );
        syncAction = new SyncAction( graphView );
        refNodeAction = new ShowReferenceNodeAction( graphView );
        refreshAction = new RefreshAction( graphView );
        RelationshipTypesProvider relTypesProvider = RelationshipTypesProviderWrapper.getInstance();
        addNewActionSet = new ActionSet( relTypesProvider );
        makeContributions();
        registerChangeHandlers();
        showRelationshipColors = graphView.getLabelProvider().getViewSettings().isShowRelationshipColors();
    }

    /**
     * Enable delete actions.
     * 
     * @param enabled
     */
    public void setEnableDeleteAction( final boolean enabled )
    {
        deleteAction.setEnabled( enabled );
    }

    /**
     * Enable relationship actions.
     * 
     * @param add enable add relationship
     * @param out enable add outgoing relationships
     * @param in enable add incoming relationships
     */
    public void setEnabledRelActions( final boolean add, final boolean out, final boolean in, final boolean self )
    {
        addRelMenuMgr.setVisible( add );
        addRelMenuMgrFake.setVisible( !add );
        addOutNodeMenuMgr.setVisible( out );
        addOutNodeMenuMgrFake.setVisible( !out );
        addInNodeMenuMgr.setVisible( in );
        addInNodeMenuMgrFake.setVisible( !in );
        addLoopMenuMgr.setVisible( self );
        addLoopMenuMgrFake.setVisible( !self );
        // update
        addInNodeMenuMgr.getParent().update( true );
    }

    /**
     * Enable decrement traversal depth action.
     * 
     * @param enabled
     */
    public void setEnabledDecAction( final boolean enabled )
    {
        decAction.setEnabled( enabled );
    }

    /**
     * Enable go back action.
     * 
     * @param enabled
     */
    public void setEnabledBackAction( final boolean enabled )
    {
        backAction.setEnabled( enabled );
    }

    /**
     * Enable go forward action.
     * 
     * @param enabled
     */
    public void setEnabledForwardAction( final boolean enabled )
    {
        forwardAction.setEnabled( enabled );
    }

    /**
     * Enable show reference node action.
     * 
     * @param enabled
     */
    public void setEnabledShowRefNodeAction( final boolean enabled )
    {
        refNodeAction.setEnabled( enabled );
    }

    /**
     * Enable refresh action.
     * 
     * @param enabled
     */
    public void setEnabledRefreshAction( final boolean enabled )
    {
        refreshAction.setEnabled( enabled );
    }

    /**
     * Enable commit action.
     * 
     * @param enabled
     */
    public void setEnabledCommitAction( final boolean enabled )
    {
        commitAction.setEnabled( enabled );
    }

    /**
     * Enable roll back action.
     * 
     * @param enabled
     */
    public void setEnabledRollbackAction( final boolean enabled )
    {
        rollbackAction.setEnabled( enabled );
    }

    /**
     * Enable sync action.
     * 
     * @param enabled
     */
    public void setEnabledSyncAction( final boolean enabled )
    {
        syncAction.setEnabled( enabled );
    }

    /**
     * Enable start action.
     * 
     * @param enabled
     */
    public void setEnabledStartAction( final boolean enabled )
    {
        startAction.setEnabled( enabled );
    }

    /**
     * Enable stop action.
     * 
     * @param enabled
     */
    public void setEnabledStopAction( final boolean enabled )
    {
        stopAction.setEnabled( enabled );
    }

    /**
     * Handle changes in the type list.
     */
    private class RelTypeRefreshHandler implements NeoclipseEventListener
    {
        @Override
        public void stateChanged( final NeoclipseEvent event )
        {
            actionMap.clear();
            relTypeImages.clear();
            loadDynamicMenus();
        }
    }

    /**
     * Register listeners.
     */
    private void registerChangeHandlers()
    {
        final RelationshipTypesProvider typeProvider = RelationshipTypesProviderWrapper.getInstance();
        typeProvider.addTypeChangeListener( new RelTypesChangeHandler() );
        typeProvider.addTypeRefreshListener( new RelTypeRefreshHandler() );
        graphView.addRelColorChangeListener( new RelTypesColorChangeHandler() );
    }

    /**
     * Initializes menus, tool bars etc.
     */
    private void makeContributions()
    {
        // initialize actions
        IToolBarManager tm = graphView.getViewSite().getActionBars().getToolBarManager();
        IMenuManager mm = graphView.getViewSite().getActionBars().getMenuManager();
        MenuManager cm = new MenuManager();
        Menu menu = cm.createContextMenu( graphViewer.getControl() );
        graphViewer.getControl().setMenu( menu );
        contributeContextActions( cm );
        contributeConnectionActions( tm );
        contributeTransactionActions( tm );
        nodeSpaceActions( tm );
        mm.add( SEPARATOR );
        // navigation actions
        contributeNavigationActions( tm );
        // recursion level actions
        contributeRecursionLevelActions( tm );
        // zoom actions
        contributeZoomActions( tm );
        // layout actions
        contributeLayoutActions( tm, mm );
        // separator
        mm.add( SEPARATOR );
        // label settings actions
        contributeLabelActions( mm );
        // separator
        mm.add( SEPARATOR );
        // platform actions
        contributePlatformActions( mm );
        // printing
        graphView.getViewSite().getActionBars().setGlobalActionHandler( ActionFactory.PRINT.getId(),
                new PrintGraphAction( graphView ) );
    }

    /**
     * Add node space actions to the tool bar.
     * 
     * @param tm current tool bar manager
     */
    private void nodeSpaceActions( final IToolBarManager tm )
    {
        tm.add( deleteAction );
        tm.add( SEPARATOR );
    }

    /**
     * Create a context menu.
     * 
     * @param cm context menu
     */
    private void contributeContextActions( final MenuManager cm )
    {
        cm.add( addRelMenuMgr );
        cm.add( addRelMenuMgrFake );
        cm.add( addOutNodeMenuMgr );
        cm.add( addOutNodeMenuMgrFake );
        cm.add( addInNodeMenuMgr );
        cm.add( addInNodeMenuMgrFake );
        cm.add( addLoopMenuMgr );
        cm.add( addLoopMenuMgrFake );
        cm.add( SEPARATOR );
        cm.add( deleteAction );
    }

    /**
     * Add commit and rollback actions.
     * 
     * @param tm
     */
    private void contributeTransactionActions( final IToolBarManager tm )
    {
        tm.add( commitAction );
        tm.add( rollbackAction );
        tm.add( syncAction );
        tm.add( SEPARATOR );
    }

    /**
     * Add start and stop actions.
     * 
     * @param tm
     */
    private void contributeConnectionActions( final IToolBarManager tm )
    {
        tm.add( startAction );
        tm.add( stopAction );
        tm.add( SEPARATOR );
    }

    /**
     * Add standard actions to the tool bar. (home , refresh)
     * 
     * @param tm current tool bar manager
     */
    private void contributeNavigationActions( final IToolBarManager tm )
    {
        {
            tm.add( backAction );
            tm.add( forwardAction );
            tm.add( refNodeAction );
            tm.add( refreshAction );
            tm.add( SEPARATOR );
        }
    }

    /**
     * Add traversal depth actions to the tool bar.
     * 
     * @param tm current tool bar manager
     */
    private void contributeRecursionLevelActions( final IToolBarManager tm )
    {
        {
            tm.add( incAction );
            tm.add( decAction );
            tm.add( SEPARATOR );
        }
    }

    /**
     * Add zoom actions to the tool bar.
     * 
     * @param tm current tool bar manager
     */
    private void contributeZoomActions( final IToolBarManager tm )
    {
        {
            ZoomAction zoomAction = new ZoomAction( graphView );
            tm.add( zoomAction );
            tm.add( SEPARATOR );
        }
    }

    /**
     * Add layout actions to the menu and toolbar.
     * 
     * @param tm current tool bar manager
     * @param mm current menu manager
     */
    private void contributeLayoutActions( final IToolBarManager tm, final IMenuManager mm )
    {
        {
            String groupName = "layout";
            GroupMarker layoutGroup = new GroupMarker( groupName );
            tm.add( layoutGroup );
            mm.add( layoutGroup );
            // spring layout
            ShowSpringLayoutAction springLayoutAction = new ShowSpringLayoutAction( graphView );
            tm.appendToGroup( groupName, springLayoutAction );
            mm.appendToGroup( groupName, springLayoutAction );
            // tree layout
            ShowTreeLayoutAction treeLayoutAction = new ShowTreeLayoutAction( graphView );
            tm.appendToGroup( groupName, treeLayoutAction );
            mm.appendToGroup( groupName, treeLayoutAction );
            // radial layout
            ShowRadialLayoutAction radialLayoutAction = new ShowRadialLayoutAction( graphView );
            tm.appendToGroup( groupName, radialLayoutAction );
            mm.appendToGroup( groupName, radialLayoutAction );
            // grid layout
            ShowGridLayoutAction gridLayoutAction = new ShowGridLayoutAction( graphView );
            tm.appendToGroup( groupName, gridLayoutAction );
            mm.appendToGroup( groupName, gridLayoutAction );
            // horizontal tree layout
            ShowHorizontalTreeLayoutAction horizontalTreeLayoutAction = new ShowHorizontalTreeLayoutAction( graphView );
            mm.appendToGroup( groupName, horizontalTreeLayoutAction );
            // horizontal shift layout
            ShowHorizontalShiftLayoutAction horizontalShiftLayoutAction = new ShowHorizontalShiftLayoutAction(
                    graphView );
            mm.appendToGroup( groupName, horizontalShiftLayoutAction );
        }
    }

    /**
     * Add label actions to menu.
     * 
     * @param mm current menu manager
     */
    private void contributeLabelActions( final IMenuManager mm )
    {
        {
            String relationshipGroupName = "relationship-labels";
            GroupMarker relationshipGroup = new GroupMarker( relationshipGroupName );
            mm.add( relationshipGroup );
            ViewSettings viewSettings = graphView.getLabelProvider().getViewSettings();
            // relationship types actions
            mm.appendToGroup( relationshipGroupName,
                    new ShowRelationshipTypesAction( graphView, viewSettings.isShowRelationshipTypes() ) );
            // relationship labels actions
            mm.appendToGroup( relationshipGroupName,
                    new ShowRelationshipLabelAction( graphView, viewSettings.isShowRelationshipNames() ) );
            mm.appendToGroup( relationshipGroupName,
                    new FilterRelationshipPropertiesAction( graphView, viewSettings.isFilterRelationshipProperties() ) );
            // relationship properties actions
            mm.appendToGroup( relationshipGroupName,
                    new ShowRelationshipPropertyKeysAction( graphView, viewSettings.isShowRelationshipPropertyKeys() ) );
            // relationship id's actions
            mm.appendToGroup( relationshipGroupName,
                    new ShowRelationshipIdsAction( graphView, viewSettings.isShowRelationshipIds() ) );
            // separator
            {
                mm.add( SEPARATOR );
            }
            // relationship directions actions
            mm.appendToGroup( relationshipGroupName,
                    new ShowRelationshipDirectionsAction( graphView, viewSettings.isShowArrows() ) );
            // relationship colors actions
            mm.appendToGroup( relationshipGroupName,
                    new ShowRelationshipColorsAction( graphView, viewSettings.isShowRelationshipColors() ) );
            // separator
            {
                mm.add( SEPARATOR );
            }
            String nodeGroupName = "node-labels";
            GroupMarker nodeGroup = new GroupMarker( nodeGroupName );
            mm.add( nodeGroup );
            // properties action
            mm.appendToGroup( nodeGroupName, new ShowNodeLabelAction( graphView, viewSettings.isShowNodeNames() ) );
            mm.appendToGroup( nodeGroupName,
                    new FilterNodePropertiesAction( graphView, viewSettings.isFilterNodeProperties() ) );
            mm.appendToGroup( nodeGroupName,
                    new ShowNodePropertyKeysAction( graphView, viewSettings.isShowNodePropertyKeys() ) );
            mm.appendToGroup( nodeGroupName, new ShowNodeIdsAction( graphView, viewSettings.isShowNodeIds() ) );
            // separator
            {
                mm.add( SEPARATOR );
            }
            // node colors actions
            mm.appendToGroup( nodeGroupName, new ShowNodeColorsAction( graphView, viewSettings.isShowNodeColors() ) );
            // node icons actions
            mm.appendToGroup( nodeGroupName, new ShowNodeIconsAction( graphView, viewSettings.isShowNodeIcons() ) );
        }
    }

    /**
     * Add platform actions like showing the preference page.
     * 
     * @param mm current menu manager
     */
    private void contributePlatformActions( final IMenuManager mm )
    {
        Action preferencesAction = new Action()
        {
            @Override
            public void run()
            {
                Activator.getDefault().showPreferenceDialog( false );
            }
        };
        Actions.PREFERENCES.initialize( preferencesAction );
        mm.add( preferencesAction );
    }

    /**
     * Refresh all relationship types in the menus.
     */
    private void loadDynamicMenus()
    {
        addRelMenuMgr.removeAll();
        addOutNodeMenuMgr.removeAll();
        addInNodeMenuMgr.removeAll();
        addLoopMenuMgr.removeAll();
        Set<RelationshipType> relTypes = RelationshipTypesProviderWrapper.getInstance().getCurrentRelationshipTypes();
        for ( RelationshipType relType : relTypes )
        {
            actionMap.put( relType.name(), new ActionSet( relType ) );
        }
        addNewActionSet.addLast();
        for ( ActionSet actionSet : actionMap.values() )
        {
            actionSet.addLast();
        }
    }

    /**
     * Add a new relationship type to the menus.
     * 
     * @param relType
     */
    private void addRelType( final RelationshipType relType )
    {
        final ActionSet actionSet = new ActionSet( relType );
        final String name = relType.name();
        actionMap.put( name, actionSet );
        actionSet.addAt( actionMap.headMap( name ).size() + 1 );
    }

    /**
     * Handle new relationship type created.
     */
    private class RelTypesChangeHandler implements NeoclipseEventListener
    {
        @Override
        public void stateChanged( final NeoclipseEvent event )
        {
            if ( event.getSource() instanceof RelationshipType )
            {
                addRelType( (RelationshipType) event.getSource() );
            }
        }
    }

    /**
     * Handle change in color setting.
     */
    private class RelTypesColorChangeHandler implements NeoclipseEventListener
    {
        @Override
        public void stateChanged( final NeoclipseEvent event )
        {
            if ( event.getSource() instanceof Boolean )
            {
                boolean newSetting = Boolean.TRUE.equals( event.getSource() );
                if ( newSetting != showRelationshipColors )
                {
                    showRelationshipColors = newSetting;
                    loadDynamicMenus();
                }
            }
        }
    }

}
