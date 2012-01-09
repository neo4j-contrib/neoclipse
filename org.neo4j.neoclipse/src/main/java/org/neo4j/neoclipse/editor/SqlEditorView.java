package org.neo4j.neoclipse.editor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.osgi.service.datalocation.Location;
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
import org.json.CDL;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.Icons;
import org.neo4j.neoclipse.graphdb.GraphDbServiceManager;
import org.neo4j.neoclipse.view.ErrorMessage;
import org.neo4j.neoclipse.view.UiHelper;


public class SqlEditorView extends ViewPart implements Listener
{

    public static final String ID = "org.neo4j.neoclipse.editor.SqlEditorView"; //$NON-NLS-1$
    private Text cypherQueryText;
    private final LinkedList<Map<String, Object>> resultSetList = new LinkedList<Map<String, Object>>();
    private CTabFolder tabFolder;
    private Label messageStatus;
    private ToolItem tltmExecuteCypherSql;
    private ToolItem tltmCsv;
    private ToolItem tltmJson;
    private ToolItem tltmXml;
    private ExecutionResult executionResult;
    private String jsonString;

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
                tltmExecuteCypherSql.setToolTipText( "Execute" );
                tltmExecuteCypherSql.setImage( Icons.EXECUTE_SQL.image() );
                tltmExecuteCypherSql.addListener( SWT.Selection, this );
            }
        }

        cypherQueryText = new Text( parent, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI );
        cypherQueryText.addKeyListener( new KeyListener()
        {

            @Override
            public void keyReleased( KeyEvent arg0 )
            {
                validate();
            }

            @Override
            public void keyPressed( KeyEvent arg0 )
            {
                validate();
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
                tltmCsv = new ToolItem( toolBar, SWT.PUSH );
                tltmCsv.setEnabled( false );
                tltmCsv.setToolTipText( "Export to CSV" );
                tltmCsv.setImage( Icons.CSV.image() );
                tltmCsv.addListener( SWT.Selection, this );

                tltmJson = new ToolItem( toolBar, SWT.PUSH );
                tltmJson.setEnabled( false );
                tltmJson.setToolTipText( "Export as Json" );
                tltmJson.setImage( Icons.JSON.image() );
                tltmJson.addListener( SWT.Selection, this );

                tltmXml = new ToolItem( toolBar, SWT.PUSH );
                tltmXml.setEnabled( false );
                tltmXml.setToolTipText( "Export as Xml" );
                tltmXml.setImage( Icons.XML.image() );
                tltmXml.addListener( SWT.Selection, this );
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


    private void executeCypherQuery( final String cypherSql )
    {
        resultSetList.clear();
        UiHelper.asyncExec( new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    String cypherQuery = cypherSql.replace( '\"', '\'' ).replace( '\n', ' ' );

                    final GraphDbServiceManager gsm = Activator.getDefault().getGraphDbServiceManager();
                    GraphDatabaseService graphDb = gsm.getGraphDb();

                    ExecutionEngine engine = new ExecutionEngine( graphDb );
                    executionResult = engine.execute( cypherQuery );
                    // System.out.println( executionResult.toString() );
                    while ( executionResult.iterator().hasNext() )
                    {
                        Map<String, Object> resultMap = executionResult.iterator().next();
                        LinkedHashMap<String, Object> newMap = new LinkedHashMap<String, Object>();
                        for ( String key : resultMap.keySet() )
                        {
                            Object objectNode = resultMap.get( key );
                            if ( objectNode == null )
                            {
                                continue;
                            }
                            Object sb = null;
                            if ( objectNode instanceof Node )
                            {
                                Node node = (Node) objectNode;
                                Map<String, Object> oMap = new LinkedHashMap<String, Object>();
                                oMap.put( "id", node.getId() );
                                for ( String propertyName : node.getPropertyKeys() )
                                {
                                    boolean containsKey = oMap.containsKey( propertyName );
                                    if ( containsKey )
                                    {
                                        throw new IllegalArgumentException( "Duplicate propertyName : " + propertyName
                                                                            + " present in " + node.toString() );
                                    }
                                    oMap.put( propertyName, node.getProperty( propertyName ) );
                                }
                                sb = oMap;
                            }
                            else
                            {
                                sb = objectNode;
                            }
                            newMap.put( key, sb );
                        }
                        resultSetList.add( newMap );
                    }

                    JSONArray jsonArray = new JSONArray( resultSetList );
                    jsonString = jsonArray.toString();
                    // System.out.println( jsonString );
                    {
                        String message = executionResult.toString().substring(
                                executionResult.toString().lastIndexOf( "+" ) + 1 ).trim();
                        messageStatus.setText( message );
                        {
                            TableViewer tableViewer = new TableViewer( tabFolder, SWT.BORDER | SWT.V_SCROLL
                                                                                  | SWT.H_SCROLL | SWT.MULTI
                                                                                  | SWT.VIRTUAL | SWT.FULL_SELECTION );
                            createColumns( tableViewer, executionResult.columns() );
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
                        }
                    }
                    enableDisableToolBars( true );
                }
                catch ( Throwable e )
                {
                    enableDisableToolBars( false );
                    ErrorMessage.showDialog( "Cypher Query problem", e );
                }
            }
        } );

    }

    private void enableDisableToolBars( boolean flag )
    {
        tltmCsv.setEnabled( flag );
        tltmJson.setEnabled( flag );
        tltmXml.setEnabled( flag );
    }

    private void validate()
    {
        boolean enableDisable = false;

        if ( !cypherQueryText.getText().trim().isEmpty() )
        {
            enableDisable = true;
        }

        tltmExecuteCypherSql.setEnabled( enableDisable );
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
                    value = getPropertyValue( value );
                    return value.toString().replace( "{", "" ).replace( "}", "" );
                }
            } );
        }

    }

    private Object getPropertyValue( Object value )
    {
        if ( Map.class.isAssignableFrom( value.getClass() ) )
        {
            Map<String, Object> map = (Map<String, Object>) value;
            StringBuilder sb = new StringBuilder();
            int i = 0;
            for ( String key : map.keySet() )
            {
                if ( i++ > 0 )
                {
                    sb.append( "," );
                }
                Object object = map.get( key );
                sb.append( key + ":" + getPropertyValue( object ) );
            }
            return sb.toString();
        }
        else if ( value.getClass().isArray() )
        {
            String stringValue = "null";
            Class eClass = value.getClass();

            if ( eClass == byte[].class )
            {
                stringValue = Arrays.toString( (byte[]) value );
            }
            else if ( eClass == short[].class )
            {
                stringValue = Arrays.toString( (short[]) value );
            }
            else if ( eClass == int[].class )
            {
                stringValue = Arrays.toString( (int[]) value );
            }
            else if ( eClass == long[].class )
            {
                stringValue = Arrays.toString( (long[]) value );
            }
            else if ( eClass == char[].class )
            {
                stringValue = Arrays.toString( (char[]) value );
            }
            else if ( eClass == float[].class )
            {
                stringValue = Arrays.toString( (float[]) value );
            }
            else if ( eClass == double[].class )
            {
                stringValue = Arrays.toString( (double[]) value );
            }
            else if ( eClass == boolean[].class )
            {
                stringValue = Arrays.toString( (boolean[]) value );
            }
            return stringValue;
        }
        return value.toString();
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
        else if ( event.widget == tltmCsv )
        {
            try
            {
                String extention = ".csv";
                File file = getFile( extention );
                JSONArray array = new JSONArray( jsonString );
                String csv = CDL.toString( array );
                BufferedWriter out = new BufferedWriter( new FileWriter( file ) );
                out.write( csv );
                out.close();
                ErrorMessage.showDialog( "CSV Export", "CSV file is created at " + file );
            }
            catch ( Exception e )
            {
                ErrorMessage.showDialog( "CSV exporting problem", e );
            }

        }
        else if ( event.widget == tltmJson )
        {
            if ( executionResult != null )
            {
                try
                {
                    File file = getFile( ".json" );
                    if ( file == null )
                    {
                        return;
                    }
                    BufferedWriter out = new BufferedWriter( new FileWriter( file ) );
                    out.write( jsonString );
                    out.close();
                    ErrorMessage.showDialog( "Json Export", "Json file is created at " + file );
                }
                catch ( Exception e )
                {
                    ErrorMessage.showDialog( "Json exporting problem", e );
                }
            }

        }
        else if ( event.widget == tltmXml )
        {
            try
            {
                File file = getFile( ".xml" );
                JSONObject array = new JSONObject( "{\"node\":" + jsonString + "}" );
                String xml = XML.toString( array, "neo4j" );
                // System.out.println( xml );
                BufferedWriter bw = new BufferedWriter( new FileWriter( file ) );
                bw.write( xml );
                bw.close();
                ErrorMessage.showDialog( "XML Export", "XML file is created at " + file );
            }
            catch ( Exception e )
            {
                ErrorMessage.showDialog( "XML exporting problem", e );
            }
        }
    }

    private File getFile( String fileExtention )
    {

        Location installLocation = Platform.getInstallLocation();
        String startingDirectory = installLocation.getURL().getPath() + "neoclipse-workspace/data" + File.separator;
        File dir = new File( startingDirectory );
        if ( !dir.exists() )
        {
            if ( !dir.mkdirs() )
            {
                throw new RuntimeException( "Could not create the directory: " + dir );
            }
        }

        return new File( startingDirectory, System.currentTimeMillis() + fileExtention );
    }
}
