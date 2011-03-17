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
package org.neo4j.neoclipse.search;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.ISearchResultListener;
import org.eclipse.search.ui.ISearchResultPage;
import org.eclipse.search.ui.ISearchResultViewPart;
import org.eclipse.search.ui.SearchResultEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.Page;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.graphdb.GraphDbServiceEvent;
import org.neo4j.neoclipse.graphdb.GraphDbServiceEventListener;
import org.neo4j.neoclipse.graphdb.GraphDbServiceManager;
import org.neo4j.neoclipse.view.NeoGraphLabelProviderWrapper;
import org.neo4j.neoclipse.view.NeoGraphViewPart;

/**
 * This class represents a page in the search view that displays Neo search
 * results, e.g. lists of Neo nodes matching a search criteria.
 * 
 * @author Peter H&auml;nsgen
 */
public class NeoSearchResultPage extends Page implements ISearchResultPage
{
    /**
     * The id of this page.
     */
    private String id;
    /**
     * The list of found nodes.
     */
    private TreeViewer viewer;
    private GraphDbServiceManager gsm;
    private GraphDbServiceEventListener listener;

    /**
     * Creates the control
     */
    @Override
    public void createControl( final Composite parent )
    {
        viewer = new TreeViewer( parent, SWT.NONE );
        viewer.setContentProvider( new NeoSearchResultContentProvider() );
        viewer.setLabelProvider( NeoGraphLabelProviderWrapper.getInstance() );
        viewer.addDoubleClickListener( new NeoSearchResultDoubleClickListener() );

        gsm = Activator.getDefault().getGraphDbServiceManager();

        listener = new GraphDbServiceEventListener()
        {
            @Override
            public void serviceChanged( final GraphDbServiceEvent event )
            {
                switch ( event.getStatus() )
                {
                case STOPPED:
                    clearResult();
                    break;
                }
            }
        };
        gsm.addServiceEventListener( listener );
    }

    private void clearResult()
    {
        // TODO this doesn't work
        viewer.setInput( null );
    }

    /**
     * Returns the control.
     */
    @Override
    public Control getControl()
    {
        return viewer.getControl();
    }

    /**
     * Sets the focus.
     */
    @Override
    public void setFocus()
    {
        if ( viewer != null )
        {
            viewer.getControl().setFocus();
        }
    }

    /**
     * Returns the id.
     */
    @Override
    public String getID()
    {
        return id;
    }

    /**
     * Sets the id.
     */
    @Override
    public void setID( final String id )
    {
        this.id = id;
    }

    /**
     * Returns the label of the page.
     */
    @Override
    public String getLabel()
    {
        NeoSearchResult result = (NeoSearchResult) viewer.getInput();
        if ( result != null )
        {
            return "Neo4j - Matches for '"
            + ( (NeoSearchQuery) result.getQuery() ).getExpression()
            + "'";
        }
        else
        {
            return "Neo4j";
        }
    }

    /**
     * Sets the search result for a Neo search.
     */
    @Override
    public void setInput( final ISearchResult result, final Object uiState )
    {
        setInput( result );
        if ( result != null )
        {
            // observe changes in the result and update the view accordingly
            result.addListener( new ISearchResultListener()
            {
                @Override
                public void searchResultChanged( final SearchResultEvent e )
                {
                    PlatformUI.getWorkbench().getDisplay().syncExec(
                            new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    setInput( e.getSearchResult() );
                                }
                            } );
                }
            } );
        }
    }

    /**
     * Sets the input of the viewer.
     */
    protected void setInput( final ISearchResult result )
    {
        viewer.setInput( result );
    }

    /**
     * Sets the view.
     */
    @Override
    public void setViewPart( final ISearchResultViewPart part )
    {
        // does nothing
    }

    /**
     * Not supported.
     */
    @Override
    public Object getUIState()
    {
        return null;
    }

    /**
     * Not supported.
     */
    @Override
    public void restoreState( final IMemento memento )
    {
        // not supported
    }

    /**
     * Not supported.
     */
    @Override
    public void saveState( final IMemento memento )
    {
        // not supported
    }

    /**
     * The handler for double clicks on search result list entries.
     */
    static class NeoSearchResultDoubleClickListener implements
    IDoubleClickListener
    {
        /**
         * Sets the selected node as input for the graph viewer.
         */
        @Override
        public void doubleClick( final DoubleClickEvent event )
        {
            StructuredSelection sel = (StructuredSelection) event.getSelection();
            Object s = sel.getFirstElement();
            // get the graph viewer
            NeoGraphViewPart gv = (NeoGraphViewPart) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(
                    NeoGraphViewPart.ID );
            if ( gv == null )
            {
                // TODO if it does not exist yet - create one? how?
                return;
            }
            if ( s instanceof Node )
            {
                gv.showNode( (Node) s );
            }
            else if ( s instanceof Relationship )
            {
                gv.showNode( ( (Relationship) s ).getStartNode() );
            }
        }
    }
}
