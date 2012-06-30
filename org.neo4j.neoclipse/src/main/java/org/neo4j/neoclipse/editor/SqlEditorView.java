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
package org.neo4j.neoclipse.editor;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.part.ViewPart;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.Icons;
import org.neo4j.neoclipse.graphdb.GraphDbServiceManager;
import org.neo4j.neoclipse.util.ApplicationUtil;
import org.neo4j.neoclipse.util.DataExportUtils;
import org.neo4j.neoclipse.view.ErrorMessage;
import org.neo4j.neoclipse.view.UiHelper;


public class SqlEditorView extends ViewPart implements Listener
{

    public static final String ID = "org.neo4j.neoclipse.editor.SqlEditorView"; //$NON-NLS-1$
    private Text cypherQueryText;
    private CTabFolder tabFolder;
    private Label messageStatus;
    private ToolItem tltmExecuteCypherSql;
    private ToolItem exportCsv;
    private ToolItem exportJson;
    private ToolItem exportXml;
    private String jsonString;
    private static boolean altKeyPressed = false;
    private static boolean enterKeyPressed = false;

    public SqlEditorView()
    {
    }

    /**
     * Create contents of the view part.
     * 
     * @param parent
     */
    @Override
    public void createPartControl( Composite parent )
    {
        parent.setLayout( new GridLayout( 1, false ) );
        {
            ToolBar toolBar = new ToolBar( parent, SWT.FLAT | SWT.RIGHT );
            {
                tltmExecuteCypherSql = new ToolItem( toolBar, SWT.PUSH );
                tltmExecuteCypherSql.setEnabled( false );
                tltmExecuteCypherSql.setToolTipText( "Execute (ALT+Enter)" );
                tltmExecuteCypherSql.setImage( Icons.EXECUTE_SQL.image() );
                tltmExecuteCypherSql.addListener( SWT.Selection, this );
            }
        }

        cypherQueryText = new Text( parent, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI );

        cypherQueryText.addKeyListener( new KeyListener()
        {

            @Override
            public void keyReleased( KeyEvent keyEvent )
            {
                if ( !validate() )
                {
                    return;
                }

                if ( altKeyPressed && keyEvent.keyCode == SWT.CR )
                {
                    enterKeyPressed = true;
                }
                else
                {
                    altKeyPressed = false;
                }

                if ( altKeyPressed && enterKeyPressed && validate() )
                {
                    executeCypherQuery( cypherQueryText.getText() );
                    altKeyPressed = enterKeyPressed = false;
                }

            }

            @Override
            public void keyPressed( KeyEvent keyEvent )
            {
                if ( !validate() )
                {
                    return;
                }
                if ( keyEvent.keyCode == SWT.ALT )
                {
                    altKeyPressed = true;
                }
            }
        } );
        GridData gd_text = new GridData( SWT.FILL, SWT.CENTER, true, false, 1, 1 );
        gd_text.heightHint = 172;
        cypherQueryText.setLayoutData( gd_text );
        {
            new Label( parent, SWT.NONE );
        }
        {
            ToolBar toolBar = new ToolBar( parent, SWT.FLAT | SWT.RIGHT );
            {
                exportCsv = new ToolItem( toolBar, SWT.PUSH );
                exportCsv.setEnabled( false );
                exportCsv.setToolTipText( "Export to CSV" );
                exportCsv.setImage( Icons.CSV.image() );
                exportCsv.addListener( SWT.Selection, this );

                exportJson = new ToolItem( toolBar, SWT.PUSH );
                exportJson.setEnabled( false );
                exportJson.setToolTipText( "Export as Json" );
                exportJson.setImage( Icons.JSON.image() );
                exportJson.addListener( SWT.Selection, this );

                exportXml = new ToolItem( toolBar, SWT.PUSH );
                exportXml.setEnabled( false );
                exportXml.setToolTipText( "Export as Xml" );
                exportXml.setImage( Icons.XML.image() );
                exportXml.addListener( SWT.Selection, this );
            }
        }
        {
            Label label = new Label( parent, SWT.SEPARATOR | SWT.HORIZONTAL );
            label.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false, 1, 1 ) );
        }
        {
            tabFolder = new CTabFolder( parent, SWT.BORDER );
            tabFolder.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 1, 1 ) );
            tabFolder.setSelectionBackground( Display.getCurrent().getSystemColor(
                    SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT ) );
            CTabItem resultsTabItem = new CTabItem( tabFolder, SWT.NONE );
            resultsTabItem.setText( "Results" );
            tabFolder.setSelection( resultsTabItem );
        }
        {
            messageStatus = new Label( parent, SWT.NONE );
            messageStatus.setTouchEnabled( true );
            messageStatus.setLayoutData( new GridData( SWT.FILL, SWT.FILL, false, false, 1, 1 ) );
        }
    }



    private void enableDisableToolBars( boolean flag )
    {
        exportCsv.setEnabled( flag );
        exportJson.setEnabled( flag );
        exportXml.setEnabled( flag );
    }

    private boolean validate()
    {
        boolean enableDisable = false;

        if ( !cypherQueryText.getText().trim().isEmpty() )
        {
            enableDisable = true;
        }

        tltmExecuteCypherSql.setEnabled( enableDisable );
        return enableDisable;
    }

    @Override
    public void setFocus()
    {
        cypherQueryText.setFocus();
    }

    // This will create the columns for the table
    private void createColumns( TableViewer tableViewer, Collection<String> titles )
    {

        TableViewerColumn col = null;
        int columnCount = 0;
        for ( final String column : titles )
        {
            col = createTableViewerColumn( tableViewer, column, columnCount++ );
            col.setLabelProvider( new ColumnLabelProvider()
            {
                @Override
                public String getText( Object element )
                {
                    Map<String, Object> rs = (Map<String, Object>) element;
                    Object value = rs.get( column );
                    value = ApplicationUtil.getPropertyValue( value );
                    return value.toString();
                }
            } );
        }

    }

    private TableViewerColumn createTableViewerColumn( TableViewer tableViewer, String title, final int colNumber )
    {
        final TableViewerColumn viewerColumn = new TableViewerColumn( tableViewer, SWT.NONE, colNumber );
        final TableColumn column = viewerColumn.getColumn();
        column.setText( title );
        column.setWidth( 150 );
        column.setResizable( true );
        column.setMoveable( true );
        return viewerColumn;

    }

    @Override
    public void handleEvent( Event event )
    {

        if ( event.widget == tltmExecuteCypherSql )
        {
            executeCypherQuery( cypherQueryText.getText() );

        }
        else if ( event.widget == exportCsv )
        {
            try
            {
                File file = DataExportUtils.exportToCsv( jsonString );
                ErrorMessage.showDialog( "CSV Export", "CSV file is created at " + file );
            }
            catch ( Exception e )
            {
                ErrorMessage.showDialog( "CSV exporting problem", e );
            }
        }
        else if ( event.widget == exportJson )
        {
            try
            {
                File file = DataExportUtils.exportToJson( jsonString.toString() );
                ErrorMessage.showDialog( "Json Export", "Json file is created at " + file );
            }
            catch ( Exception e )
            {
                ErrorMessage.showDialog( "Json exporting problem", e );
            }

        }
        else if ( event.widget == exportXml )
        {
            try
            {
                File file = DataExportUtils.exportToXml( jsonString );
                ErrorMessage.showDialog( "XML Export", "XML file is created at " + file );
            }
            catch ( Exception e )
            {
                ErrorMessage.showDialog( "XML exporting problem", e );
            }
        }
    }

    private void executeCypherQuery( final String cypherSql )
    {
        UiHelper.asyncExec( new Runnable()
        {
            @Override
            public void run()
            {
                final GraphDbServiceManager gsm = Activator.getDefault().getGraphDbServiceManager();
                try
                {
                    CypherResultSet cypherResultSet = gsm.executeCypher( cypherSql );
                    displayResultSet( cypherResultSet );
                }
                catch ( Exception e )
                {
                    e.printStackTrace();
                    enableDisableToolBars( false );
                    ErrorMessage.showDialog( "execute cypher query", e );
                }
            }

        } );
    }

    private void displayResultSet( CypherResultSet cypherResultSet )
    {
        List<Map<String, Object>> resultSetList = cypherResultSet.getIterator();
        Collection<String> columns = cypherResultSet.getColumns();

        jsonString = ApplicationUtil.toJson( resultSetList );
        messageStatus.setText( cypherResultSet.getMessage() != null ? cypherResultSet.getMessage() : "" );
        TableViewer tableViewer = new TableViewer( tabFolder, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI
                                                              | SWT.VIRTUAL | SWT.FULL_SELECTION );
        createColumns( tableViewer, columns );
        tableViewer.setContentProvider( new ArrayContentProvider() );
        Table table = tableViewer.getTable();
        table.setHeaderVisible( true );
        table.setLinesVisible( true );
        tableViewer.setInput( resultSetList );
        getSite().setSelectionProvider( tableViewer );
        CTabItem resultsTabItem = tabFolder.getSelection();
        if ( resultsTabItem == null )
        {
            resultsTabItem = new CTabItem( tabFolder, SWT.NONE );
            resultsTabItem.setText( "Results" );
            tabFolder.setSelection( resultsTabItem );
        }
        resultsTabItem.setControl( table );
        enableDisableToolBars( true );
    }

}
