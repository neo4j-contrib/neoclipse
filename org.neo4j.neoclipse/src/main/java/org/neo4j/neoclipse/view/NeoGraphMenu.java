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

import java.util.HashMap;
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
import org.neo4j.api.core.RelationshipType;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.action.Actions;
import org.neo4j.neoclipse.action.PrintGraphAction;
import org.neo4j.neoclipse.action.browse.GoBackAction;
import org.neo4j.neoclipse.action.browse.GoForwardAction;
import org.neo4j.neoclipse.action.browse.RefreshAction;
import org.neo4j.neoclipse.action.browse.ShowReferenceNodeAction;
import org.neo4j.neoclipse.action.context.CommitAction;
import org.neo4j.neoclipse.action.context.DeleteAction;
import org.neo4j.neoclipse.action.context.RollbackAction;
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
import org.neo4j.neoclipse.action.reltype.NewRelationshipTypeAction;
import org.neo4j.neoclipse.action.view.DecreaseTraversalDepthAction;
import org.neo4j.neoclipse.action.view.IncreaseTraversalDepthAction;
import org.neo4j.neoclipse.action.view.ZoomAction;
import org.neo4j.neoclipse.event.NeoclipseEvent;
import org.neo4j.neoclipse.event.NeoclipseEventListener;
import org.neo4j.neoclipse.neo.NodeSpaceUtil;
import org.neo4j.neoclipse.reltype.RelationshipTypesProviderWrapper;

/**
 * Handle tool bar, view menu and context menu of the graph view.
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

        public ActionSet( final RelationshipType relType )
        {
            final String name = relType.name();
            ImageDescriptor imgDesc;
            if ( showRelationshipColors )
            {
                imgDesc = relTypeImages.get( relType );
                if ( imgDesc == null )
                {
                    Image image = new Image( Display.getCurrent(),
                        RELTYPE_IMG_SIZE, RELTYPE_IMG_SIZE );
                    Color color = graphView.getLabelProvider().getColor(
                        relType );
                    GC gc = new GC( image );
                    gc.setBackground( color );
                    gc.fillRectangle( image.getBounds() );
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
                    NodeSpaceUtil.addRelationshipAction( relType, graphView );
                }
            };
            addRel.setEnabled( false );
            addOut = new Action( name, imgDesc )
            {
                @Override
                public void run()
                {
                    NodeSpaceUtil.addOutgoingNodeAction( relType, graphView );
                }
            };
            addOut.setEnabled( false );
            addIn = new Action( name, imgDesc )
            {
                @Override
                public void run()
                {
                    NodeSpaceUtil.addIncomingNodeAction( relType, graphView );
                }
            };
            addIn.setEnabled( false );
        }

        /**
         * Add action set as last item in menu managers.
         */
        public void addLast()
        {
            addRelMenuMgr.add( addRel );
            addOutNodeMenuMgr.add( addOut );
            addInNodeMenuMgr.add( addIn );
        }

        public void addAt( int index )
        {
            addRelMenuMgr.insert( index, new ActionContributionItem( addRel ) );
            addOutNodeMenuMgr.insert( index,
                new ActionContributionItem( addOut ) );
            addInNodeMenuMgr
                .insert( index, new ActionContributionItem( addIn ) );
        }

        public void setEnabled( boolean rel, boolean out, boolean in )
        {
            addRel.setEnabled( rel );
            addOut.setEnabled( out );
            addIn.setEnabled( in );
        }
    }

    private final SortedMap<String,ActionSet> actionMap = new TreeMap<String,ActionSet>();

    /**
     * Size of colored squares for reltypes.
     */
    private static final int RELTYPE_IMG_SIZE = 6;

    private final NeoGraphViewPart graphView;
    private final GraphViewer graphViewer;

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
    private final IncreaseTraversalDepthAction incAction;
    private final DeleteAction deleteAction;
    private final CommitAction commitAction;

    private final RollbackAction rollbackAction;
    private final MenuManager addRelMenuMgr = new MenuManager(
        Actions.ADD_RELATIONSHIP.label(), Actions.ADD_RELATIONSHIP.icon()
            .descriptor(), "addRelSubmenu" );
    private final MenuManager addOutNodeMenuMgr = new MenuManager(
        Actions.ADD_OUTGOING_NODE.label(), Actions.ADD_OUTGOING_NODE.icon()
            .descriptor(), "addOutNodeSubmenu" );
    private final MenuManager addInNodeMenuMgr = new MenuManager(
        Actions.ADD_INCOMING_NODE.label(), Actions.ADD_INCOMING_NODE.icon()
            .descriptor(), "addInNodeSubmenu" );

    private final Map<RelationshipType,ImageDescriptor> relTypeImages = new HashMap<RelationshipType,ImageDescriptor>();
    private final static ImageDescriptor RELTYPES_DEFAULT_IMG;
    private final ShowReferenceNodeAction refNodeAction;
    private final RefreshAction refreshAction;
    private final NewRelationshipTypeAction newRelTypeAction;

    private boolean showRelationshipColors = ShowRelationshipColorsAction.DEFAULT_STATE;
    private boolean addState = false;
    private boolean outState = false;
    private boolean inState = false;

    static
    {
        // create gray default color
        Image image = new Image( Display.getCurrent(), RELTYPE_IMG_SIZE,
            RELTYPE_IMG_SIZE );
        Color color = new Color( Display.getCurrent(), 96, 96, 96 );
        GC gc = new GC( image );
        gc.setBackground( color );
        gc.fillRectangle( image.getBounds() );
        gc.dispose();
        RELTYPES_DEFAULT_IMG = ImageDescriptor.createFromImage( image );
    }

    public NeoGraphMenu( final NeoGraphViewPart graphView )
    {
        this.graphView = graphView;
        this.graphViewer = graphView.getViewer();
        deleteAction = new DeleteAction( graphView );
        backAction = new GoBackAction( graphView );
        forwardAction = new GoForwardAction( graphView );
        decAction = new DecreaseTraversalDepthAction( graphView );
        incAction = new IncreaseTraversalDepthAction( graphView );
        commitAction = new CommitAction( graphView );
        rollbackAction = new RollbackAction( graphView );
        refNodeAction = new ShowReferenceNodeAction( graphView );
        refreshAction = new RefreshAction( graphView );
        newRelTypeAction = new NewRelationshipTypeAction(
            RelationshipTypesProviderWrapper.getInstance() );
        makeContributions();
        registerChangeHandlers();
    }

    public void setEnableDeleteAction( boolean enabled )
    {
        deleteAction.setEnabled( enabled );
    }

    public void setEnabledRelActions( boolean add, boolean out, boolean in )
    {
        this.addState = add;
        this.outState = out;
        this.inState = in;
        for ( ActionSet actionSet : actionMap.values() )
        {
            actionSet.setEnabled( add, out, in );
        }
    }

    public void setEnabledDecAction( boolean enabled )
    {
        decAction.setEnabled( enabled );
    }

    public void setEnabledBackAction( boolean enabled )
    {
        backAction.setEnabled( enabled );
    }

    public void setEnabledForwardAction( boolean enabled )
    {
        forwardAction.setEnabled( enabled );
    }

    public void setEnabledCommitAction( boolean enabled )
    {
        commitAction.setEnabled( enabled );
    }

    public void setEnabledRollbackAction( boolean enabled )
    {
        rollbackAction.setEnabled( enabled );
    }

    private void registerChangeHandlers()
    {
        RelationshipTypesProviderWrapper.getInstance().addTypeChangeListener(
            new RelTypesChangeHandler() );
        graphView.addRelColorChangeListener( new RelTypesColorChangeHandler() );
    }

    public void clearImages()
    {
        relTypeImages.clear();
    }

    /**
     * Initializes menus, tool bars etc.
     */
    private void makeContributions()
    {
        // initialize actions
        IToolBarManager tm = graphView.getViewSite().getActionBars()
            .getToolBarManager();
        IMenuManager mm = graphView.getViewSite().getActionBars()
            .getMenuManager();

        createContextMenu();

        contributeTransactionActions( tm );

        nodeSpaceActions( tm );

        mm.add( new Separator() );

        // navigation actions
        contributeNavigationActions( tm );
        // recursion level actions
        contributeRecursionLevelActions( tm );
        // zoom actions
        contributeZoomActions( tm );
        // layout actions
        contributeLayoutActions( tm, mm );
        // separator
        mm.add( new Separator() );
        // label settings actions
        contributeLabelActions( mm );
        // separator
        mm.add( new Separator() );
        // platform actions
        contributePlatformActions( mm );
        // printing
        graphView.getViewSite().getActionBars().setGlobalActionHandler(
            ActionFactory.PRINT.getId(), new PrintGraphAction( graphView ) );
    }

    private void nodeSpaceActions( IToolBarManager tm )
    {
        tm.add( deleteAction );
        tm.add( new Separator() );
    }

    /**
     * Create a context menu.
     */
    private void createContextMenu()
    {
        MenuManager menuMgr = new MenuManager();
        menuMgr.add( addRelMenuMgr );
        menuMgr.add( addOutNodeMenuMgr );
        menuMgr.add( addInNodeMenuMgr );
        menuMgr.add( newRelTypeAction );
        menuMgr.add( deleteAction );
        Menu menu = menuMgr.createContextMenu( graphViewer.getControl() );
        graphViewer.getControl().setMenu( menu );
    }

    /**
     * Add commit and rollback actions.
     * @param tm
     */
    private void contributeTransactionActions( IToolBarManager tm )
    {
        tm.add( commitAction );
        tm.add( rollbackAction );
        tm.add( new Separator() );
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

            tm.add( refNodeAction );

            tm.add( refreshAction );

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
            tm.add( incAction );

            tm.add( decAction );

            tm.add( new Separator() );
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
            ZoomAction zoomAction = new ZoomAction( graphView );
            tm.add( zoomAction );
            tm.add( new Separator() );
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
                graphView );
            tm.appendToGroup( groupName, springLayoutAction );
            mm.appendToGroup( groupName, springLayoutAction );
            // tree layout
            ShowTreeLayoutAction treeLayoutAction = new ShowTreeLayoutAction(
                graphView );
            tm.appendToGroup( groupName, treeLayoutAction );
            mm.appendToGroup( groupName, treeLayoutAction );
            // radial layout
            ShowRadialLayoutAction radialLayoutAction = new ShowRadialLayoutAction(
                graphView );
            tm.appendToGroup( groupName, radialLayoutAction );
            mm.appendToGroup( groupName, radialLayoutAction );
            // grid layout
            ShowGridLayoutAction gridLayoutAction = new ShowGridLayoutAction(
                graphView );
            tm.appendToGroup( groupName, gridLayoutAction );
            mm.appendToGroup( groupName, gridLayoutAction );
            // horizontal tree layout
            ShowHorizontalTreeLayoutAction horizontalTreeLayoutAction = new ShowHorizontalTreeLayoutAction(
                graphView );
            mm.appendToGroup( groupName, horizontalTreeLayoutAction );
            // horizontal shift layout
            ShowHorizontalShiftLayoutAction horizontalShiftLayoutAction = new ShowHorizontalShiftLayoutAction(
                graphView );
            mm.appendToGroup( groupName, horizontalShiftLayoutAction );
        }
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
                graphView ) );
            // relationship types actions
            mm.appendToGroup( labelsGroupName, new ShowRelationshipLabelAction(
                graphView ) );
            // relationship id's actions
            mm.appendToGroup( labelsGroupName, new ShowRelationshipIdsAction(
                graphView ) );
            // relationship types actions
            mm.appendToGroup( labelsGroupName,
                new ShowRelationshipColorsAction( graphView ) );
            // relationship directions actions
            mm.appendToGroup( labelsGroupName,
                new ShowRelationshipDirectionsAction( graphView ) );
            // separator
            {
                mm.add( new Separator() );
            }
            // names actions
            mm.appendToGroup( labelsGroupName, new ShowNodeLabelAction(
                graphView ) );
            // relationship id's actions
            mm.appendToGroup( labelsGroupName,
                new ShowNodeIdsAction( graphView ) );
            // node colors actions
            mm.appendToGroup( labelsGroupName, new ShowNodeColorsAction(
                graphView ) );
            // node icons actions
            mm.appendToGroup( labelsGroupName, new ShowNodeIconsAction(
                graphView ) );
        }
    }

    /**
     * Add platform actions like showing the preference page.
     * @param mm
     *            current menu manager
     */
    private void contributePlatformActions( IMenuManager mm )
    {
        Action preferencesAction = new Action()
        {
            @Override
            public void run()
            {
                Activator.getDefault().showPreferenceDialog( true );
            }
        };
        Actions.PREFERENCES.initialize( preferencesAction );
        mm.add( preferencesAction );
    }

    /**
     * Refresh all relationship types in the menus.
     */
    public void loadDynamicMenus()
    {
        addRelMenuMgr.removeAll();
        addOutNodeMenuMgr.removeAll();
        addInNodeMenuMgr.removeAll();
        Set<RelationshipType> relTypes = RelationshipTypesProviderWrapper
            .getInstance().getCurrentRelationshipTypes();
        for ( RelationshipType relType : relTypes )
        {
            actionMap.put( relType.name(), new ActionSet( relType ) );
        }
        for ( ActionSet actionSet : actionMap.values() )
        {
            actionSet.addLast();
        }
    }

    private void addRelType( RelationshipType relType )
    {
        final ActionSet actionSet = new ActionSet( relType );
        actionSet.setEnabled( addState, outState, inState );
        final String name = relType.name();
        actionMap.put( name, actionSet );
        actionSet.addAt( actionMap.headMap( name ).size() );
    }

    private class RelTypesChangeHandler implements NeoclipseEventListener
    {
        public void stateChanged( NeoclipseEvent event )
        {
            if ( event.getSource() instanceof RelationshipType )
            {
                addRelType( (RelationshipType) event.getSource() );
            }
        }
    }

    private class RelTypesColorChangeHandler implements NeoclipseEventListener
    {
        public void stateChanged( NeoclipseEvent event )
        {
            if ( event.getSource() instanceof Boolean )
            {
                showRelationshipColors = Boolean.TRUE
                    .equals( event.getSource() );
                loadDynamicMenus();
            }
        }
    }
}
