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
package org.neo4j.neoclipse.property;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ChangeEvent;
import org.eclipse.draw2d.ChangeListener;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.IPropertySourceProvider;
import org.eclipse.ui.views.properties.PropertySheetEntry;
import org.eclipse.ui.views.properties.PropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheetSorter;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.neoclipse.Icons;
import org.neo4j.neoclipse.help.HelpContextConstants;
import org.neo4j.neoclipse.property.action.AddNodeLabelAction;
import org.neo4j.neoclipse.property.action.CopyAction;
import org.neo4j.neoclipse.property.action.DeleteAction;
import org.neo4j.neoclipse.property.action.NewAction;
import org.neo4j.neoclipse.property.action.PasteAction;
import org.neo4j.neoclipse.property.action.RenameAction;
import org.neo4j.neoclipse.view.NeoGraphViewPart;
import org.neo4j.neoclipse.view.UiHelper;

/**
 * This class is a workaround to sort the property categories in the way we
 * want.
 * 
 * @author Anders Nawroth
 */
public class NeoPropertySheetPage extends PropertySheetPage implements
        ISelectionListener, INullSelectionListener
{
    private static class NeoPropertySheetSorter extends PropertySheetSorter
    {
        private static final String RELATIONSHIP = "Relationship";

        @Override
        public int compareCategories( final String categoryA,
                final String categoryB )
        {
            if ( RELATIONSHIP.equals( categoryA )
                 || RELATIONSHIP.equals( categoryB ) )
            {
                // reverse the categories in this case
                return super.compareCategories( categoryB, categoryA );
            }
            return super.compareCategories( categoryA, categoryB );
        }
    }

    /**
     * The Eclipse view ID.
     */
    public static final String ID = "org.neo4j.neoclipse.property.NeoPropertySheetPage";
    private ISelection selection;
    private Menu menu;
    private Composite parent;
    private DeleteAction deleteAction;
    private CopyAction copyAction;
    private RenameAction renameAction;
    private PasteAction pasteAction;
    private PropertyContainer containerSelection;
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    private AddNodeLabelAction addNodeLabelAction;

    public NeoPropertySheetPage()
    {
        super();
        setPropertySourceProvider( new PropertySourceProvider( this ) );
    }

    public void addChangeListener( final ChangeListener listener )
    {
        listeners.add( listener );
    }

    public void fireChangeEvent( final Object element, final String key,
            final boolean refresh )
    {
        // TODO make sure this key gets selected in the view
        ChangeEvent ce = new ChangeEvent( element, key );
        for ( ChangeListener listener : listeners )
        {
            listener.handleStateChanged( ce );
        }
        if ( refresh )
        {
            refreshSafely();
        }
    }

    public void refreshSafely()
    {
        UiHelper.asyncExec( new Runnable()
        {
            @Override
            public void run()
            {
                refresh();
            }
        } );
    }

    public PropertyContainer getPropertyContainer()
    {
        return containerSelection;
    }

    /**
     * Get the current selection.
     * 
     * @return current selection
     */
    public ISelection getSelection()
    {
        return selection;
    }

    @Override
    public void createControl( final Composite parent )
    {
        super.createControl( parent );
        this.parent = parent;
        setSorter( new NeoPropertySheetSorter() );
        createMenu( parent );
        getControl().setMenu( menu );
        getSite().getPage().addSelectionListener( NeoGraphViewPart.ID, this );
        PlatformUI.getWorkbench().getHelpSystem().setHelp( parent,
                HelpContextConstants.NEO_PROPERTY_SHEET_PAGE );
    }

    /**
     * Create the context menu for this property sheet.
     * 
     * @param parent
     */
    private void createMenu( final Composite parent )
    {
        MenuManager menuManager = createMainMenu( parent );
        menu = menuManager.createContextMenu( getControl() );
        MenuManager addMenuManager = createNewSubmenu( parent );
        addMenuManager.setParent( menuManager );
        menuManager.add( addMenuManager );
        MenuManager addArrayMenuManager = createNewArraySubmenu( parent );
        addArrayMenuManager.setParent( menuManager );
        menuManager.add( addArrayMenuManager );
    }

    /**
     * Create the main structure of the menu.
     */
    private MenuManager createMainMenu( final Composite parent )
    {
        MenuManager menuMgr = new MenuManager();
        copyAction = new CopyAction( parent, this );
        menuMgr.add( copyAction );
        pasteAction = new PasteAction( parent, this );
        pasteAction.setEnabled( false );
        menuMgr.add( pasteAction );
        deleteAction = new DeleteAction( parent, this );
        menuMgr.add( deleteAction );
        renameAction = new RenameAction( parent, this );
        menuMgr.add( renameAction );
        addNodeLabelAction = new AddNodeLabelAction( parent, this );
        menuMgr.add( addNodeLabelAction );
        return menuMgr;
    }

    /**
     * Enable paste action.
     */
    public void enablePaste()
    {
        pasteAction.setEnabled( true );
    }

    /**
     * Enable/disable actions that operate on an existing property.
     * 
     * @param enabled
     */
    private void setRestrictedEnabled( final boolean enabled )
    {
        if ( deleteAction != null )
        {
            deleteAction.setEnabled( enabled );
        }
        if ( copyAction != null )
        {
            copyAction.setEnabled( enabled );
        }
        if ( renameAction != null )
        {
            renameAction.setEnabled( enabled );
        }
        if ( addNodeLabelAction != null )
        {
            addNodeLabelAction.setEnabled( enabled );
        }
    }

    /**
     * Create submenu for adding new properties.
     */
    private MenuManager createNewSubmenu( final Composite parent )
    {
        MenuManager addMenuMgr = new MenuManager( "New",
                Icons.NEW_ENABLED.descriptor(), "propertiesAddSubmenu" );
        addMenuMgr.add( new NewAction( parent, this,
                PropertyTransform.getHandler( String.class ) ) );
        addMenuMgr.add( new NewAction( parent, this,
                PropertyTransform.getHandler( Character.class ) ) );
        addMenuMgr.add( new NewAction( parent, this,
                PropertyTransform.getHandler( Long.class ) ) );
        addMenuMgr.add( new NewAction( parent, this,
                PropertyTransform.getHandler( Integer.class ) ) );
        addMenuMgr.add( new NewAction( parent, this,
                PropertyTransform.getHandler( Short.class ) ) );
        addMenuMgr.add( new NewAction( parent, this,
                PropertyTransform.getHandler( Byte.class ) ) );
        addMenuMgr.add( new NewAction( parent, this,
                PropertyTransform.getHandler( Double.class ) ) );
        addMenuMgr.add( new NewAction( parent, this,
                PropertyTransform.getHandler( Float.class ) ) );
        addMenuMgr.add( new NewAction( parent, this,
                PropertyTransform.getHandler( Boolean.class ) ) );
        return addMenuMgr;
    }

    /**
     * Create submenu for adding new properties of array type.
     */
    private MenuManager createNewArraySubmenu( final Composite parent )
    {
        MenuManager addMenuMgr = new MenuManager( "New[]",
                Icons.NEW_ENABLED.descriptor(), "propertiesArrayAddSubmenu" );
        addMenuMgr.add( new NewAction( parent, this,
                PropertyTransform.getHandler( String[].class ) ) );
        addMenuMgr.add( new NewAction( parent, this,
                PropertyTransform.getHandler( char[].class ) ) );
        addMenuMgr.add( new NewAction( parent, this,
                PropertyTransform.getHandler( long[].class ) ) );
        addMenuMgr.add( new NewAction( parent, this,
                PropertyTransform.getHandler( int[].class ) ) );
        addMenuMgr.add( new NewAction( parent, this,
                PropertyTransform.getHandler( short[].class ) ) );
        addMenuMgr.add( new NewAction( parent, this,
                PropertyTransform.getHandler( byte[].class ) ) );
        addMenuMgr.add( new NewAction( parent, this,
                PropertyTransform.getHandler( double[].class ) ) );
        addMenuMgr.add( new NewAction( parent, this,
                PropertyTransform.getHandler( float[].class ) ) );
        addMenuMgr.add( new NewAction( parent, this,
                PropertyTransform.getHandler( boolean[].class ) ) );
        return addMenuMgr;
    }

    @Override
    public void handleEntrySelection( final ISelection selection )
    {
        super.handleEntrySelection( selection );
        this.selection = selection;
        if ( selection instanceof IStructuredSelection )
        {
            IStructuredSelection ss = (IStructuredSelection) selection;
            if ( ss.size() > 1 )
            {
                setRestrictedEnabled( false );
                return;
            }
            Object firstElement = ss.getFirstElement();
            if ( firstElement == null )
            {
                setRestrictedEnabled( false );
                return;
            }
            if ( firstElement instanceof PropertySheetEntry )
            {
                PropertySheetEntry entry = (PropertySheetEntry) firstElement;
                if ( entry.getEditor( parent ) == null )
                {
                    setRestrictedEnabled( false );
                    return;
                }
            }
        }
        setRestrictedEnabled( true );
    }

    @Override
    public void selectionChanged( final IWorkbenchPart part,
            final ISelection selection )
    {
        super.selectionChanged( part, selection );
        if ( !( selection instanceof IStructuredSelection ) )
        {
            containerSelection = null;
            return;
        }
        IStructuredSelection parSs = (IStructuredSelection) selection;
        if ( parSs.isEmpty() )
        {
            containerSelection = null;
            refresh();
            return;
        }
        Object parFirstElement = parSs.getFirstElement();
        if ( !( parFirstElement instanceof PropertyContainer ) )
        {
            containerSelection = null;
            return;
        }
        containerSelection = (PropertyContainer) parFirstElement;
    }

    @Override
    public void setPropertySourceProvider(
            final IPropertySourceProvider newProvider )
    {
        super.setPropertySourceProvider( newProvider );
    }
}
