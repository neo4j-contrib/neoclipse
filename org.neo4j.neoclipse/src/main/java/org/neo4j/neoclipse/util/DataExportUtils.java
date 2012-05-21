package org.neo4j.neoclipse.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.datalocation.Location;
import org.json.CDL;
import org.json.JSONArray;
import org.json.XML;

public class DataExportUtils
{

    public static File exportToXml( String jsonString ) throws Exception
    {
        File file = getFile( ".xml" );
        StringBuilder sb = new StringBuilder( "<rootnode>" );
        sb.append( XML.toString( new JSONArray( jsonString ), "node" ) );
        sb.append( "</rootnode>" );
        BufferedWriter bw = new BufferedWriter( new FileWriter( file ) );
        bw.write( sb.toString() );
        bw.close();
        return file;
    }



    public static File exportToJson( String jsonString ) throws IOException
    {
        File file = getFile( ".json" );
        BufferedWriter out = new BufferedWriter( new FileWriter( file ) );
        out.write( jsonString );
        out.close();
        return file;
    }

    public static File exportToCsv( String jsonString ) throws Exception
    {
        File file = getFile( ".csv" );
        String csv = CDL.toString( new JSONArray( jsonString ) );
        BufferedWriter out = new BufferedWriter( new FileWriter( file ) );
        out.write( csv );
        out.close();
        return file;
    }

    private static File getFile( String fileExtention )
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
