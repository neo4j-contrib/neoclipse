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
    private Menu menu;
    private Composite parent;

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
        getControl().setMenu( menu );
    }

    /**
     * @param parent
     */
    private void createMenu( final Composite parent )
    {
        MenuManager menuMgr = new MenuManager( "#PopupMenu" );
        menuMgr.add( new PropertyCopyAction( this ) );
        menuMgr.add( new PropertyDeleteAction( parent, this ) );
        menu = menuMgr.createContextMenu( getControl() );
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
                getControl().setMenu( null );
                return;
            }
            Object firstElement = ss.getFirstElement();
            if ( firstElement == null )
            {
                getControl().setMenu( null );
                return;
            }
            if ( firstElement instanceof PropertySheetEntry )
            {
                PropertySheetEntry entry = (PropertySheetEntry) firstElement;
                if ( entry.getEditor( parent ) == null )
                {
                    getControl().setMenu( null );
                    return;
                }
            }
        }
        getControl().setMenu( menu );
    }
}