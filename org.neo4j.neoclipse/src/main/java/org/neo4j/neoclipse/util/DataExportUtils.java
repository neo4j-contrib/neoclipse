package org.neo4j.neoclipse.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.datalocation.Location;
import org.json.CDL;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

public class DataExportUtils
{

    public static File exportToXml( String jsonString ) throws Exception
    {
        File file = getFile( ".xml" );
        JSONObject array = new JSONObject( "{\"node\":" + jsonString + "}" );
        String xml = XML.toString( array, "neo4j" );
        // System.out.println( xml );
        BufferedWriter bw = new BufferedWriter( new FileWriter( file ) );
        bw.write( xml );
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
        JSONArray array = new JSONArray( jsonString );
        String csv = CDL.toString( array );
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
