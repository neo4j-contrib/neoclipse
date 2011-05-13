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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.search.ui.ISearchPage;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.graphdb.GraphCallable;
import org.neo4j.neoclipse.graphdb.GraphDbServiceEvent;
import org.neo4j.neoclipse.graphdb.GraphDbServiceEventListener;
import org.neo4j.neoclipse.graphdb.GraphDbServiceManager;

/**
 * This class represents a search page in the search dialog to perform
 * Neo4j-specific searches. The found nodes will be shown in the search result
 * view.
 * 
 * @author Peter H&auml;nsgen
 * @author Anders Nawroth
 */
public class NeoSearchPage extends DialogPage implements ISearchPage
{
    private static final int QUERY_MODE = 1;
    private static final int EXACT_MODE = 0;
    public static final String ID = "org.neo4j.neoclipse.search.NeoSearchPage";
    /**
     * Choose the index to search.
     */
    private Tree indexTree;
    /**
     * The input field for the search expression.
     */
    private Text valueOrQueryField;

    /**
     * The container of this page.
     */
    protected ISearchPageContainer container;
    private Text keyField;

    private TreeItem nodeRoot;
    private TreeItem relRoot;
    private Combo modeCombo;
    private GraphDbServiceEventListener listener;
    private GraphDbServiceManager gsm;

    /**
     * Initializes the content of the search page.
     */
    @Override
    public void createControl( final Composite parent )
    {
        initializeDialogUnits( parent );

        final Composite comp = new Composite( parent, SWT.NONE );
        GridLayout layout = new GridLayout( 1, false );
        comp.setLayout( layout );

        Label indexLabel = new Label( comp, SWT.NONE );
        indexLabel.setText( "Index to search:" );
        indexLabel.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );

        indexTree = new Tree( comp, SWT.BORDER | SWT.CHECK );
        GridData treeLayout = new GridData( GridData.FILL_BOTH );
        treeLayout.heightHint = 300;
        indexTree.setLayoutData( treeLayout );
        indexTree.addListener( SWT.Selection, new Listener()
        {
            @Override
            public void handleEvent( final Event event )
            {
                if ( event.detail == SWT.CHECK )
                {
                    TreeItem item = (TreeItem) event.item;
                    boolean checked = item.getChecked();
                    checkItems( item, checked );
                    checkPath( item.getParentItem(), checked, false );
                }
            }
        } );

        Label modeLabel = new Label( comp, SWT.NONE );
        modeLabel.setText( "Search mode:" );
        modeLabel.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );

        modeCombo = new Combo( comp, SWT.READ_ONLY );
        modeCombo.add( "Exact matches", EXACT_MODE );
        modeCombo.add( "Query", QUERY_MODE );
        modeCombo.select( EXACT_MODE );

        Label propertyLabel = new Label( comp, SWT.NONE );
        propertyLabel.setText( "Key: (leave empty for Compound Queries)" );
        propertyLabel.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );

        keyField = new Text( comp, SWT.SINGLE | SWT.BORDER );
        keyField.setLayoutData( new GridData( GridData.HORIZONTAL_ALIGN_FILL
                                              | GridData.VERTICAL_ALIGN_END ) );

        Label expressionLabel = new Label( comp, SWT.NONE );
        expressionLabel.setText( "Value/Query:" );
        expressionLabel.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );

        valueOrQueryField = new Text( comp, SWT.SINGLE | SWT.BORDER );
        valueOrQueryField.setLayoutData( new GridData(
                GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_END ) );

        gsm = Activator.getDefault().getGraphDbServiceManager();

        listener = new GraphDbServiceEventListener()
        {
            @Override
            public void serviceChanged( final GraphDbServiceEvent event )
            {
                switch ( event.getStatus() )
                {
                case STARTED:
                    loadIndices();
                    break;
                case STOPPED:
                    clearIndices();
                    break;
                case SHUTTING_DOWN:
                    break;
                }
            }
        };
        gsm.addServiceEventListener( listener );

        comp.getShell().addListener( SWT.Show, new Listener()
        {
            @Override
            public void handleEvent( final Event event )
            {
                if ( nodeRoot == null )
                {
                    loadIndices();
                }
            }
        } );

        setControl( comp );
    }

    /**
     * Sets the owning search dialog.
     */
    @Override
    public void setContainer( final ISearchPageContainer container )
    {
        this.container = container;
    }

    /**
     * Performs the search.
     */
    @Override
    public boolean performAction()
    {
        String searchString = valueOrQueryField.getText();
        String propertyName = keyField.getText();
        if ( nodeRoot != null )
        {
            IndexSearch search;
            if ( modeCombo.getSelectionIndex() == QUERY_MODE )
            {
                search = IndexSearch.query( propertyName, searchString,
                        namesFromRoot( nodeRoot ), namesFromRoot( relRoot ) );

            }
            else
            {
                search = IndexSearch.exact( propertyName, searchString,
                        namesFromRoot( nodeRoot ), namesFromRoot( relRoot ) );
            }
            NewSearchUI.runQueryInBackground( new NeoSearchQuery( search ) );
            return true;
        }
        return false;
    }

    private Iterable<String> namesFromRoot( final TreeItem treeItem )
    {
        if ( treeItem == null )
        {
            return Collections.emptyList();
        }
        List<String> names = new ArrayList<String>();
        for ( TreeItem item : treeItem.getItems() )
        {
            if ( item.getChecked() )
            {
                names.add( item.getText() );
            }
        }
        return names;
    }

    private void clearIndices()
    {
        if ( getControl().isDisposed() || indexTree.isDisposed() )
        {
            return;
        }
        indexTree.removeAll();
    }

    private void loadIndices()
    {
        // load indices
        if ( !gsm.isRunning() )
        {
            return;
        }
        final String[] nodeIndexNames = gsm.executeTask(
                new GraphCallable<String[]>()
                {
                    @Override
                    public String[] call( final GraphDatabaseService graphDb )
                    {
                        return graphDb.index().nodeIndexNames();
                    }
                }, "Get node index names." );
        Arrays.sort( nodeIndexNames );
        final String[] relIndexNames = gsm.executeTask(
                new GraphCallable<String[]>()
                {
                    @Override
                    public String[] call( final GraphDatabaseService graphDb )
                    {
                        return graphDb.index().relationshipIndexNames();
                    }
                }, "Get relationship index names." );
        Arrays.sort( relIndexNames );
        gsm.submitDisplayTask( new Runnable()
        {
            @Override
            public void run()
            {
                if ( getControl().isDisposed() || !getControl().isVisible()
                     || indexTree.isDisposed() )
                {
                    return;
                }
                indexTree.removeAll();
                if ( nodeIndexNames.length > 0 )
                {
                    nodeRoot = new TreeItem( indexTree, SWT.None );
                    nodeRoot.setText( "Nodes" );
                    for ( String name : nodeIndexNames )
                    {
                        TreeItem item = new TreeItem( nodeRoot, SWT.None );
                        item.setText( name );
                    }
                }
                if ( relIndexNames.length > 0 )
                {
                    relRoot = new TreeItem( indexTree, SWT.None );
                    relRoot.setText( "Relationships" );
                    for ( String name : relIndexNames )
                    {
                        TreeItem item = new TreeItem( relRoot, SWT.None );
                        item.setText( name );
                    }
                }
            }
        }, "Add index names to the UI." );
    }

    private static void checkPath( final TreeItem item, boolean checked,
            boolean grayed )
    {
        if ( item == null )
        {
            return;
        }
        if ( grayed )
        {
            checked = true;
        }
        else
        {
            int index = 0;
            TreeItem[] items = item.getItems();
            while ( index < items.length )
            {
                TreeItem child = items[index];
                if ( child.getGrayed() || checked != child.getChecked() )
                {
                    checked = grayed = true;
                    break;
                }
                index++;
            }
        }
        item.setChecked( checked );
        item.setGrayed( grayed );
        checkPath( item.getParentItem(), checked, grayed );
    }

    private static void checkItems( final TreeItem item, final boolean checked )
    {
        item.setGrayed( false );
        item.setChecked( checked );
        TreeItem[] items = item.getItems();
        for ( TreeItem item2 : items )
        {
            checkItems( item2, checked );
        }
    }

}
