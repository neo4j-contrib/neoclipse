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

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.views.properties.PropertySheetEntry;
import org.eclipse.ui.views.properties.PropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheetSorter;
import org.neo4j.neoclipse.NeoIcons;
import org.neo4j.neoclipse.property.action.CopyAction;
import org.neo4j.neoclipse.property.action.DeleteAction;
import org.neo4j.neoclipse.property.action.NewAction;
import org.neo4j.neoclipse.view.NeoGraphViewPart;

/**
 * This class is a workaround to sort the property categories in the way we
 * want.
 * @author Anders Nawroth
 */
public class NeoPropertySheetPage extends PropertySheetPage
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

    private ISelection selection;
    private NeoGraphViewPart neoView;
    private Menu standardMenu;
    private Composite parent;
    private DeleteAction deleteAction;
    private CopyAction copyAction;

    public NeoPropertySheetPage( NeoGraphViewPart neoGraphViewPart )
    {
        super();
        this.neoView = neoGraphViewPart;
    }

    public NeoGraphViewPart getNeoGraphViewPart()
    {
        return neoView;
    }

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
        getControl().setMenu( standardMenu );
    }

    /**
     * @param parent
     */
    private void createMenu( final Composite parent )
    {
        MenuManager menuManager = createMainMenu( parent );
        standardMenu = menuManager.createContextMenu( getControl() );
        MenuManager addMenuManager = createNewSubmenu();
        addMenuManager.setParent( menuManager );
        menuManager.add( addMenuManager );
        MenuManager addArrayMenuManager = createNewArraySubmenu();
        addArrayMenuManager.setParent( menuManager );
        menuManager.add( addArrayMenuManager );
    }

    /**
     * @param parent
     * @return
     */
    private MenuManager createMainMenu( final Composite parent )
    {
        // TODO add rename action
        MenuManager menuMgr = new MenuManager();
        copyAction = new CopyAction( this );
        menuMgr.add( copyAction );
        deleteAction = new DeleteAction( parent, this );
        menuMgr.add( deleteAction );
        return menuMgr;
    }

    private void setRestrictedEnabled( Boolean enabled )
    {
        if ( deleteAction == null || copyAction == null )
        {
            return;
        }
        deleteAction.setEnabled( enabled );
        copyAction.setEnabled( enabled );
    }

    private MenuManager createNewSubmenu()
    {
        MenuManager addMenuMgr = new MenuManager( "New", NeoIcons
            .getDescriptor( NeoIcons.NEW ), "propertiesAddSubmenu" );
        addMenuMgr.add( new NewAction( this, "" ) );
        addMenuMgr.add( new NewAction( this, (char) 0 ) );
        addMenuMgr.add( new NewAction( this, 0L ) );
        addMenuMgr.add( new NewAction( this, 0 ) );
        addMenuMgr.add( new NewAction( this, (short) 0 ) );
        addMenuMgr.add( new NewAction( this, (byte) 0 ) );
        addMenuMgr.add( new NewAction( this, 0d ) );
        addMenuMgr.add( new NewAction( this, 0f ) );
        addMenuMgr.add( new NewAction( this, false ) );
        return addMenuMgr;
    }

    private MenuManager createNewArraySubmenu()
    {
        MenuManager addMenuMgr = new MenuManager( "New[]", NeoIcons
            .getDescriptor( NeoIcons.NEW ), "propertiesArrayAddSubmenu" );
        addMenuMgr.add( new NewAction( this, new String[0] ) );
        addMenuMgr.add( new NewAction( this, new char[0] ) );
        addMenuMgr.add( new NewAction( this, new long[0] ) );
        addMenuMgr.add( new NewAction( this, new int[0] ) );
        addMenuMgr.add( new NewAction( this, new short[0] ) );
        addMenuMgr.add( new NewAction( this, new byte[0] ) );
        addMenuMgr.add( new NewAction( this, new double[0] ) );
        addMenuMgr.add( new NewAction( this, new float[0] ) );
        addMenuMgr.add( new NewAction( this, new boolean[0] ) );
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
}