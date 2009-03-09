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
package org.neo4j.neoclipse.property;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.draw2d.ChangeEvent;
import org.eclipse.draw2d.ChangeListener;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.IPropertySourceProvider;
import org.eclipse.ui.views.properties.PropertySheetEntry;
import org.eclipse.ui.views.properties.PropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheetSorter;
import org.neo4j.api.core.PropertyContainer;
import org.neo4j.neoclipse.NeoIcons;
import org.neo4j.neoclipse.help.HelpContextConstants;
import org.neo4j.neoclipse.property.action.CopyAction;
import org.neo4j.neoclipse.property.action.DeleteAction;
import org.neo4j.neoclipse.property.action.NewAction;
import org.neo4j.neoclipse.property.action.PasteAction;
import org.neo4j.neoclipse.property.action.RenameAction;
import org.neo4j.neoclipse.view.NeoGraphViewPart;

/**
 * This class is a workaround to sort the property categories in the way we
 * want.
 * @author Anders Nawroth
 */
public class NeoPropertySheetPage extends PropertySheetPage implements
    ISelectionListener
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
    private Set<ChangeListener> listeners = new HashSet<ChangeListener>();

    public NeoPropertySheetPage()
    {
        super();
        setPropertySourceProvider( new PropertySourceProvider( this ) );
    }

    public void addChangeListener( ChangeListener listener )
    {
        listeners.add( listener );
    }

    public void fireChangeEvent( Object element, String key )
    {
        ChangeEvent ce = new ChangeEvent( element, key );
        for ( ChangeListener listener : listeners )
        {
            listener.handleStateChanged( ce );
        }
    }

    public PropertyContainer getPropertyContainer()
    {
        return containerSelection;
    }

    /**
     * Get the current selection.
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
    }

    /**
     * Create submenu for adding new properties.
     */
    private MenuManager createNewSubmenu( final Composite parent )
    {
        MenuManager addMenuMgr = new MenuManager( "New", NeoIcons.NEW
            .descriptor(), "propertiesAddSubmenu" );
        addMenuMgr
            .add( new NewAction( parent, this, "", NeoIcons.TYPE_STRING ) );
        addMenuMgr.add( new NewAction( parent, this, (char) 0,
            NeoIcons.TYPE_CHAR ) );
        addMenuMgr.add( new NewAction( parent, this, 0L, NeoIcons.TYPE_LONG ) );
        addMenuMgr.add( new NewAction( parent, this, 0, NeoIcons.TYPE_INT ) );
        addMenuMgr.add( new NewAction( parent, this, (short) 0,
            NeoIcons.TYPE_SHORT ) );
        addMenuMgr.add( new NewAction( parent, this, (byte) 0,
            NeoIcons.TYPE_BYTE ) );
        addMenuMgr
            .add( new NewAction( parent, this, 0d, NeoIcons.TYPE_DOUBLE ) );
        addMenuMgr.add( new NewAction( parent, this, 0f, NeoIcons.TYPE_FLOAT ) );
        addMenuMgr.add( new NewAction( parent, this, false,
            NeoIcons.TYPE_BOOLEAN ) );
        return addMenuMgr;
    }

    /**
     * Create submenu for adding new properties of array type.
     */
    private MenuManager createNewArraySubmenu( final Composite parent )
    {
        MenuManager addMenuMgr = new MenuManager( "New[]", NeoIcons.NEW
            .descriptor(), "propertiesArrayAddSubmenu" );
        addMenuMgr.add( new NewAction( parent, this, new String[0],
            NeoIcons.TYPE_STRING ) );
        addMenuMgr.add( new NewAction( parent, this, new char[0],
            NeoIcons.TYPE_CHAR ) );
        addMenuMgr.add( new NewAction( parent, this, new long[0],
            NeoIcons.TYPE_LONG ) );
        addMenuMgr.add( new NewAction( parent, this, new int[0],
            NeoIcons.TYPE_INT ) );
        addMenuMgr.add( new NewAction( parent, this, new short[0],
            NeoIcons.TYPE_SHORT ) );
        addMenuMgr.add( new NewAction( parent, this, new byte[0],
            NeoIcons.TYPE_BYTE ) );
        addMenuMgr.add( new NewAction( parent, this, new double[0],
            NeoIcons.TYPE_DOUBLE ) );
        addMenuMgr.add( new NewAction( parent, this, new float[0],
            NeoIcons.TYPE_FLOAT ) );
        addMenuMgr.add( new NewAction( parent, this, new boolean[0],
            NeoIcons.TYPE_BOOLEAN ) );
        return addMenuMgr;
    }

    @Override
    public void handleEntrySelection( ISelection selection )
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
    public void selectionChanged( IWorkbenchPart part, ISelection selection )
    {
        super.selectionChanged( part, selection );
        if ( part instanceof NeoGraphViewPart )
        {
            if ( !(selection instanceof IStructuredSelection) )
            {
                containerSelection = null;
                return;
            }
            IStructuredSelection parSs = (IStructuredSelection) selection;
            Object parFirstElement = parSs.getFirstElement();
            if ( !(parFirstElement instanceof PropertyContainer) )
            {
                containerSelection = null;
                return;
            }
            containerSelection = (PropertyContainer) parFirstElement;
        }
    }

    @Override
    public void setPropertySourceProvider( IPropertySourceProvider newProvider )
    {
        super.setPropertySourceProvider( newProvider );
    }
}