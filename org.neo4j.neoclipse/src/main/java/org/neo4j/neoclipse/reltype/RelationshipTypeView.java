package org.neo4j.neoclipse.reltype;

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
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.RelationshipType;
import org.neo4j.neoclipse.view.NeoGraphViewPart;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public class RelationshipTypeView extends ViewPart implements
    ISelectionListener
{
    public final static String ID = "org.neo4j.neoclipse.reltype.RelationshipTypeView";
    private TableViewer viewer;
    private Action action1;
    private Action action2;
    private Action doubleClickAction;
    private RelationshipType currentSelection;

    class ViewLabelProvider extends LabelProvider implements
        ITableLabelProvider, ITableColorProvider
    {
        public String getColumnText( Object obj, int index )
        {
            return getText( obj );
        }

        public Image getColumnImage( Object obj, int index )
        {
            return getImage( obj );
        }

        public Image getImage( Object obj )
        {
            return PlatformUI.getWorkbench().getSharedImages().getImage(
                ISharedImages.IMG_OBJ_ELEMENT );
        }

        public Color getBackground( Object element, int columnIndex )
        {
            return null;
        }

        public Color getForeground( Object element, int columnIndex )
        {
            return null;
        }
    }

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
        viewer.setContentProvider( new RelationshipTypesProvider() );
        viewer.setLabelProvider( new ViewLabelProvider() );
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
        fillLocalPullDown( bars.getMenuManager() );
        fillLocalToolBar( bars.getToolBarManager() );
    }

    private void fillLocalPullDown( IMenuManager manager )
    {
        manager.add( action1 );
        manager.add( new Separator() );
        manager.add( action2 );
    }

    private void fillContextMenu( IMenuManager manager )
    {
        manager.add( action1 );
        manager.add( action2 );
        // Other plug-ins can contribute there actions here
        manager.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );
    }

    private void fillLocalToolBar( IToolBarManager manager )
    {
        manager.add( action1 );
        manager.add( action2 );
    }

    private void makeActions()
    {
        action1 = new Action()
        {
            public void run()
            {
                showMessage( "Action 1 executed" );
            }
        };
        action1.setText( "Action 1" );
        action1.setToolTipText( "Action 1 tooltip" );
        action1.setImageDescriptor( PlatformUI.getWorkbench().getSharedImages()
            .getImageDescriptor( ISharedImages.IMG_OBJS_INFO_TSK ) );

        action2 = new Action()
        {
            public void run()
            {
                showMessage( "Action 2 executed" );
            }
        };
        action2.setText( "Action 2" );
        action2.setToolTipText( "Action 2 tooltip" );
        action2.setImageDescriptor( PlatformUI.getWorkbench().getSharedImages()
            .getImageDescriptor( ISharedImages.IMG_OBJS_INFO_TSK ) );
        doubleClickAction = new Action()
        {
            public void run()
            {
                ISelection selection = viewer.getSelection();
                Object obj = ((IStructuredSelection) selection)
                    .getFirstElement();
                showMessage( "Double-click detected on " + obj.toString() );
            }
        };
    }

    private void hookDoubleClickAction()
    {
        viewer.addDoubleClickListener( new IDoubleClickListener()
        {
            public void doubleClick( DoubleClickEvent event )
            {
                doubleClickAction.run();
            }
        } );
    }

    private void showMessage( String message )
    {
        MessageDialog.openInformation( viewer.getControl().getShell(),
            "Relationship Types", message );
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
            if ( !(firstElement instanceof Relationship) )
            {
                return;
            }
            currentSelection = ((Relationship) firstElement).getType();
            viewer.setSelection( new StructuredSelection( currentSelection ) );
        }
        else if ( this.equals( part ) )
        {
            if ( !(firstElement instanceof RelationshipType) )
            {
                return;
            }
            currentSelection = (RelationshipType) firstElement;
        }
    }
}