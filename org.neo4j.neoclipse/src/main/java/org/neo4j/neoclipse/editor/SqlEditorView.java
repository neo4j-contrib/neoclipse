package org.neo4j.neoclipse.editor;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import net.javacrumbs.json2xml.JsonXmlReader;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
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
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.cypherdsl.result.JSONSerializer;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.Icons;
import org.neo4j.neoclipse.graphdb.GraphDbServiceManager;
import org.neo4j.neoclipse.view.ErrorMessage;
import org.neo4j.neoclipse.view.UiHelper;
import org.xml.sax.InputSource;

public class SqlEditorView extends ViewPart implements Listener
{

    public static final String ID = "org.neo4j.neoclipse.editor.SqlEditorView"; //$NON-NLS-1$
    private Text cypherQueryText;
    private final LinkedList<ResultSet> resultSetList = new LinkedList<ResultSet>();
    private CTabFolder tabFolder;
    private Label messageStatus;
    private ToolItem tltmExecuteCypherSql;
    private ToolItem tltmExcel;
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
                tltmExcel = new ToolItem( toolBar, SWT.PUSH );
                tltmExcel.setEnabled( false );
                tltmExcel.setToolTipText( "Export to Excel" );
                tltmExcel.setImage( Icons.EXCEL.image() );
                tltmExcel.addListener( SWT.Selection, this );

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
                    // System.out.println( result.toString() );

                    List<String> columns = executionResult.columns();

                    JSONSerializer jsonSerializer = new JSONSerializer();
                    ArrayNode arrayNode = jsonSerializer.toJSON( executionResult );
                    jsonString = arrayNode.toString();
                    Iterator<JsonNode> elements = arrayNode.getElements();
                    while ( elements.hasNext() )
                    {
                        JsonNode jsonNode = elements.next();
                        ResultSet rs = new ResultSet();
                        for ( String column : columns )
                        {
                            JsonNode str = jsonNode.get( column );
                            if ( str != null )
                            {
                                String value = str.toString().replace( "\"", "" ).replace( "{", "" ).replace( "}", "" ).replace(
                                        "\"_", "\"" );
                                rs.addResults( column, value );
                            }
                        }
                        resultSetList.add( rs );
                    }

                    {
                        String message = executionResult.toString().substring(
                                executionResult.toString().lastIndexOf( "+" ) + 1 ).trim();
                        messageStatus.setText( message );
                        {
                            TableViewer tableViewer = new TableViewer( tabFolder, SWT.BORDER | SWT.V_SCROLL
                                                                                  | SWT.H_SCROLL | SWT.MULTI
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
        tltmExcel.setEnabled( flag );
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
                    ResultSet rs = (ResultSet) element;
                    return rs.getListByKey( column );
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

    private class ResultSet
    {

        private final Map<String, String> results = new LinkedHashMap<String, String>();

        public void addResults( String key, String value )
        {
            this.results.put( key, value );
        }

        public String getListByKey( String key )
        {
            if ( results.isEmpty() )
            {
                return "";
            }
            return results.get( key );
        }

    }

    @Override
    public void handleEvent( Event event )
    {

        if ( event.widget == tltmExecuteCypherSql )
        {
            executeCypherQuery( cypherQueryText.getText() );
        }
        else if ( event.widget == tltmExcel )
        {
            String platform = SWT.getPlatform();
            String extention = ".csv";
            if ( platform.equals( "win32" ) || platform.equals( "wpf" ) )
            {
                extention = ".xls";
            }
            ErrorMessage.showDialog( "CSV exporting problem", "Currently CSV export is not supported" );
            // File file = getFile( extention );
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
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                InputSource source = new InputSource( new StringReader( "{\"root\":" + jsonString + "}" ) );
                Result result = new StreamResult( out );
                transformer.transform( new SAXSource( new JsonXmlReader( "neo4j", false ), source ), result );
                String string = new String( out.toByteArray() );
                BufferedWriter bw = new BufferedWriter( new FileWriter( file ) );
                bw.write( string );
                out.close();
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
