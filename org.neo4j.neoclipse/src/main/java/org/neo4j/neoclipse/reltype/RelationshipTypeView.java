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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.neo4j.api.core.Direction;
import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.RelationshipType;
import org.neo4j.api.core.Transaction;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.NeoIcons;
import org.neo4j.neoclipse.view.NeoGraphLabelProvider;
import org.neo4j.neoclipse.view.NeoGraphLabelProviderWrapper;
import org.neo4j.neoclipse.view.NeoGraphViewPart;

public class RelationshipTypeView extends ViewPart implements
    ISelectionListener
{
    public final static String ID = "org.neo4j.neoclipse.reltype.RelationshipTypeView";
    private static final String MARK_RELATIONSHIPS_TOOL_TIP = "Mark all relationships of this type.";
    private static final String NEW_RELTYPE_DIALOG_TEXT = "Please enter the name of the new relationships type";
    private static final String NEW_RELTYPE_DIALOG_TITLE = "New relationship type entry";
    private static final String MARK_RELATIONSHIPS_LABEL = "Mark relationships";
    private static final String ADD_NODE_START_TOOL_TIP = "Add a node with a relationship; "
        + "the new node is the start node of the relationship(s).";
    private static final String ADD_NODE_START_LABEL = "Add node as start node";
    private static final String ADD_NODE_END_TOOL_TIP = "Add a node with a relationship; "
        + "the new node is the end node of the relationship(s).";
    private static final String ADD_NODE_END_LABEL = "Add node as end node";
    private static final String ADD_RELATIONSHIP_LABEL = "Add relationship";
    private static final String CREATE_NEW_TOOL_TIP = "Create new relationship type.";
    private static final String CREATE_NEW_LABEL = "Create new type";
    private static final String CLEAR_HIGHLIGHT = "Remove highlighting";
    private static final String MARK_START_NODES_TOOLTIP = "Mark start nodes for relationships of this type.";
    private static final String MARK_START_NODES_LABEL = "Mark start nodes";
    private static final String MARK_END_NODES_TOOLTIP = "Mark end nodes for relationships of this type.";
    private static final String MARK_END_NODES_LABEL = "Mark end nodes";
    protected static final int OK = 0;
    private TableViewer viewer;
    private Action markIncomingAction;
    private Action markOutgoingAction;
    private Action clearMarkedAction;
    private Action markRelationshipAction;
    private RelationshipTypesProvider provider;
    private NeoGraphViewPart graphView = null;
    private NeoGraphLabelProvider graphLabelProvider = NeoGraphLabelProviderWrapper
        .getInstance();
    private Action newAction;
    private Action addRelationship;
    private Action addOutgoingNode;
    private List<Node> currentSelectedNodes = Collections.emptyList();
    private Action addIncomingNode;

    class NameSorter extends ViewerSorter
    {
        @Override
        public int compare( Viewer viewer, Object e1, Object e2 )
        {
            if ( e1 instanceof RelationshipType
                && e2 instanceof RelationshipType )
            {
                return ((RelationshipType) e1).name().compareTo(
                    ((RelationshipType) e2).name() );
            }
            return super.compare( viewer, e1, e2 );
        }
    }

    /**
     * The constructor.
     */
    public RelationshipTypeView()
    {
    }

    /**
     * This is a callback that will allow us to create the viewer and initialize
     * it.
     */
    public void createPartControl( Composite parent )
    {
        viewer = new TableViewer( parent, SWT.MULTI | SWT.V_SCROLL );
        provider = new RelationshipTypesProvider();
        viewer.setContentProvider( provider );
        viewer.setLabelProvider( NeoGraphLabelProviderWrapper.getInstance() );
        viewer.setSorter( new NameSorter() );
        viewer.setInput( getViewSite() );

        // Create the help context id for the viewer's control
        PlatformUI.getWorkbench().getHelpSystem().setHelp( viewer.getControl(),
            "org.neo4j.neoclipse.reltypesviewer" );
        makeActions();
        hookContextMenu();
        hookDoubleClickAction();
        contributeToActionBars();
        getSite().getPage().addSelectionListener( NeoGraphViewPart.ID, this );
        for ( IViewReference view : getSite().getPage().getViewReferences() )
        {
            if ( NeoGraphViewPart.ID.equals( view.getId() ) )
            {
                graphView = (NeoGraphViewPart) view.getView( false );
            }
        }
        getSite().setSelectionProvider( viewer );
        getSite().getPage().addSelectionListener( ID, this );
    }

    private void hookContextMenu()
    {
        MenuManager menuMgr = new MenuManager( "#PopupMenu" );
        menuMgr.setRemoveAllWhenShown( true );
        menuMgr.addMenuListener( new IMenuListener()
        {
            public void menuAboutToShow( IMenuManager manager )
            {
                RelationshipTypeView.this.fillContextMenu( manager );
            }
        } );
        Menu menu = menuMgr.createContextMenu( viewer.getControl() );
        viewer.getControl().setMenu( menu );
        getSite().registerContextMenu( menuMgr, viewer );
    }

    private void contributeToActionBars()
    {
        IActionBars bars = getViewSite().getActionBars();
        // fillLocalPullDown( bars.getMenuManager() );
        fillLocalToolBar( bars.getToolBarManager() );
    }

    // private void fillLocalPullDown( IMenuManager manager )
    // {
    // manager.add( markIncomingAction );
    // manager.add( markOutgoingAction );
    // manager.add( clearMarkedAction );
    // }

    private void fillContextMenu( IMenuManager manager )
    {
        manager.add( markOutgoingAction );
        manager.add( markIncomingAction );
        manager.add( markRelationshipAction );
        manager.add( addRelationship );
        manager.add( addOutgoingNode );
        manager.add( addIncomingNode );
        // Other plug-ins can contribute there actions here
        manager.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );
    }

    private void fillLocalToolBar( IToolBarManager manager )
    {
        manager.add( markIncomingAction );
        manager.add( markOutgoingAction );
        manager.add( clearMarkedAction );
        manager.add( newAction );
    }

    private void makeActions()
    {
        markIncomingAction = new Action( MARK_END_NODES_LABEL )
        {
            public void run()
            {
                RelationshipType relType = getCurrentRelType();
                if ( relType == null )
                {
                    return;
                }
                highlightNodes( relType, Direction.INCOMING );
                clearMarkedAction.setEnabled( true );
            }
        };
        markIncomingAction.setToolTipText( MARK_END_NODES_TOOLTIP );
        markIncomingAction.setImageDescriptor( NeoIcons.HIGHLIGHT_INCOMING
            .getDescriptor() );
        markIncomingAction.setEnabled( false );

        markOutgoingAction = new Action( MARK_START_NODES_LABEL )
        {
            public void run()
            {
                RelationshipType relType = getCurrentRelType();
                if ( relType == null )
                {
                    return;
                }
                highlightNodes( relType, Direction.OUTGOING );
                clearMarkedAction.setEnabled( true );
            }
        };
        markOutgoingAction.setToolTipText( MARK_START_NODES_TOOLTIP );
        markOutgoingAction.setImageDescriptor( NeoIcons.HIGHLIGHT_OUTGOING
            .getDescriptor() );
        markOutgoingAction.setEnabled( false );

        clearMarkedAction = new Action( CLEAR_HIGHLIGHT )
        {
            public void run()
            {
                graphLabelProvider.clearMarkedNodes();
                graphLabelProvider.clearMarkedRels();
                graphView.getViewer().refresh( true );
                setEnabled( false );
            }
        };
        clearMarkedAction.setImageDescriptor( NeoIcons.CLEAR_ENABLED
            .getDescriptor() );
        clearMarkedAction.setDisabledImageDescriptor( NeoIcons.CLEAR_DISABLED
            .getDescriptor() );
        clearMarkedAction.setEnabled( false );

        newAction = new Action( CREATE_NEW_LABEL )
        {
            public void run()
            {
                InputDialog input = new InputDialog( null,
                    NEW_RELTYPE_DIALOG_TITLE, NEW_RELTYPE_DIALOG_TEXT, null,
                    null );
                if ( input.open() == OK && input.getReturnCode() == OK )
                {
                    RelationshipType relType = createRelationshipType( input
                        .getValue() );
                    provider.addFakeType( relType );
                    viewer.refresh();
                }
            }
        };
        newAction.setToolTipText( CREATE_NEW_TOOL_TIP );
        newAction.setImageDescriptor( NeoIcons.NEW.getDescriptor() );

        addRelationship = new Action( ADD_RELATIONSHIP_LABEL )
        {
            public void run()
            {
                RelationshipType relType = getAddRelPreconditions();
                Node source = currentSelectedNodes.get( 0 );
                Node dest = currentSelectedNodes.get( 1 );
                createRelationship( source, dest, relType );
            }
        };
        addRelationship.setImageDescriptor( NeoIcons.ADD.getDescriptor() );

        addOutgoingNode = new Action( ADD_NODE_END_LABEL )
        {
            public void run()
            {
                RelationshipType relType = getCurrentRelType();
                createRelationship( currentSelectedNodes, null, relType );
            }
        };
        addOutgoingNode.setImageDescriptor( NeoIcons.ADD_OUTGOING
            .getDescriptor() );
        addOutgoingNode.setToolTipText( ADD_NODE_END_TOOL_TIP );

        addIncomingNode = new Action( ADD_NODE_START_LABEL )
        {
            public void run()
            {
                RelationshipType relType = getCurrentRelType();
                createRelationship( null, currentSelectedNodes, relType );
            }
        };
        addIncomingNode.setImageDescriptor( NeoIcons.ADD_INCOMING
            .getDescriptor() );
        addIncomingNode.setToolTipText( ADD_NODE_START_TOOL_TIP );

        markRelationshipAction = new Action( MARK_RELATIONSHIPS_LABEL )
        {
            public void run()
            {
                RelationshipType relType = getCurrentRelType();
                if ( relType == null )
                {
                    return;
                }
                highlightRelationshipType( relType );
                setEnableRestrictedActions( true );
                clearMarkedAction.setEnabled( true );
            }
        };
        markRelationshipAction.setImageDescriptor( NeoIcons.HIGHLIGHT
            .getDescriptor() );
        markRelationshipAction.setToolTipText( MARK_RELATIONSHIPS_TOOL_TIP );
    }

    private void createRelationship( Node source, Node dest,
        RelationshipType relType )
    {
        List<Node> sourceNodes = null;
        if ( source != null )
        {
            sourceNodes = new ArrayList<Node>();
            sourceNodes.add( source );
        }
        List<Node> destNodes = null;
        if ( dest != null )
        {
            destNodes = new ArrayList<Node>();
            destNodes.add( dest );
        }
        createRelationship( sourceNodes, destNodes, relType );
    }

    /**
     * Create relationship between two nodes. One node can be created, but not
     * both
     * @param sourceNodes
     *            source, is created if <code>null</code> is given
     * @param destNodes
     *            destination, is created if <code>null</code> is given
     * @param relType
     */
    private void createRelationship( List<Node> sourceNodes,
        List<Node> destNodes, RelationshipType relType )
    {
        if ( relType == null )
        {
            return;
        }
        NeoService ns = Activator.getDefault().getNeoServiceSafely();
        if ( ns == null )
        {
            return;
        }
        Transaction tx = ns.beginTx();
        try
        {
            if ( destNodes == null )
            {
                destNodes = new ArrayList<Node>();
                destNodes.add( ns.createNode() );
            }
            else if ( sourceNodes == null )
            {
                sourceNodes = new ArrayList<Node>();
                sourceNodes.add( ns.createNode() );
            }
            for ( Node source : sourceNodes )
            {
                for ( Node dest : destNodes )
                {
                    source.createRelationshipTo( dest, relType );
                }
            }
            tx.success();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        finally
        {
            tx.finish();
        }
        graphView.refreshPreserveLayout();
    }

    private RelationshipType getAddRelPreconditions()
    {
        if ( currentSelectedNodes.isEmpty() || currentSelectedNodes.size() != 2 )
        {
            // TODO err msg
            return null;
        }
        RelationshipType relType = getCurrentRelType();
        return relType;
    }

    private void setEnableRestrictedActions( boolean enabled )
    {
        markIncomingAction.setEnabled( enabled );
        markOutgoingAction.setEnabled( enabled );
    }

    private void setEnableAddRelationship( boolean enabled )
    {
        addRelationship.setEnabled( enabled );
    }

    private void setEnableAddNode( boolean enabled )
    {
        addOutgoingNode.setEnabled( enabled );
        addIncomingNode.setEnabled( enabled );
    }

    private RelationshipType getCurrentRelType()
    {
        ISelection selection = viewer.getSelection();
        Object obj = ((IStructuredSelection) selection).getFirstElement();
        if ( obj instanceof RelationshipType )
        {
            return (RelationshipType) obj;
        }
        return null;
    }

    private void highlightRelationshipType( RelationshipType relType )
    {
        if ( graphView == null )
        {
            return;
        }
        List<Relationship> rels = new ArrayList<Relationship>();
        GraphViewer gViewer = graphView.getViewer();
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
        gViewer.refresh( true );
    }

    private void highlightNodes( RelationshipType relType, Direction direction )
    {
        if ( graphView == null )
        {
            return;
        }
        GraphViewer gViewer = graphView.getViewer();
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
        gViewer.refresh( true );
    }

    private void hookDoubleClickAction()
    {
        viewer.addDoubleClickListener( new IDoubleClickListener()
        {
            public void doubleClick( DoubleClickEvent event )
            {
                markRelationshipAction.run();
            }
        } );
    }

    private RelationshipType createRelationshipType( final String name )
    {
        return new RelationshipType()
        {
            public String name()
            {
                return name;
            }
        };
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    public void setFocus()
    {
        viewer.getControl().setFocus();
    }

    /**
     * Keep track of the graph view selections.
     */
    public void selectionChanged( IWorkbenchPart part, ISelection selection )
    {
        if ( !(selection instanceof IStructuredSelection) )
        {
            return;
        }
        IStructuredSelection parSs = (IStructuredSelection) selection;
        Object firstElement = parSs.getFirstElement();
        if ( part instanceof NeoGraphViewPart )
        {
            graphView = (NeoGraphViewPart) part;
            setEnableAddRelationship( false );
            setEnableAddNode( false );
            currentSelectedNodes = Collections.emptyList();
            if ( firstElement instanceof Relationship )
            {
                RelationshipType currentSelection = ((Relationship) firstElement)
                    .getType();
                viewer
                    .setSelection( new StructuredSelection( currentSelection ) );
                setEnableRestrictedActions( true );
            }
            else if ( firstElement instanceof Node )
            {
                boolean onlyNodes = true;
                List<Node> nodes = new ArrayList<Node>();
                for ( Object o : parSs.toList() )
                {
                    if ( o instanceof Node )
                    {
                        nodes.add( (Node) o );
                    }
                    else
                    {
                        onlyNodes = false;
                        break;
                    }
                }
                if ( onlyNodes )
                {
                    currentSelectedNodes = nodes;
                    setEnableAddNode( true );
                    if ( currentSelectedNodes.size() == 2 )
                    {
                        setEnableAddRelationship( true );
                    }
                }
            }
        }
        else if ( this.equals( part ) )
        {
            if ( selection.isEmpty() )
            {
                setEnableRestrictedActions( false );
            }
            else
            {
                setEnableRestrictedActions( true );
            }
        }
    }
}