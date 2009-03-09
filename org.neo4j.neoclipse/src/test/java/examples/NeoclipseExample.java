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
package examples;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.neo4j.api.core.EmbeddedNeo;
import org.neo4j.api.core.NeoService;

/**
 * Utility to set up a Neo4j instance for test cases. We use Junit4 to run the
 * examples. Look at the existing examples for guidance. If icons exists, they
 * should be put in an "icons" subfolder and the copyIcons() method should be
 * called in a @BeforeClass method. The basic setup of data should be done in a @BeforeClass
 * as well. This base class will start (and stop) a Neo4j instance for you,
 * found in the "neo" variable. A newline is automatically printed after each
 * test case, to provide a little formatting.
 * @author Anders Nawroth
 */
public abstract class NeoclipseExample
{
    private static final String TARGET_DIR = "target";
    private static final String NEOSTORE_SUBDIR = "neo";
    private static final String ICON_SUBDIR = "icons";
    private static final String STORE_LOCATION_DIR = TARGET_DIR
        + File.separator + NEOSTORE_SUBDIR;
    private static final String ICON_LOCATION_DIR = TARGET_DIR + File.separator
        + ICON_SUBDIR;
    private static final String EXAMPLES_DIR = "src" + File.separator + "test"
        + File.separator + "java" + File.separator + "examples";
    /**
     * Local Neo4j instance.
     */
    protected static NeoService neo;

    @BeforeClass
    public static void startNeo()
    {
        File file = new File( STORE_LOCATION_DIR );
        if ( file.exists() )
        {
            deleteDir( file );
        }
        neo = new EmbeddedNeo( file.getAbsolutePath() );
    }

    /**
     * Method to copy icons.
     * @param exampleDir
     *            subdirectory name of example
     */
    protected static void copyIcons( String exampleDir )
    {
        File dest = new File( ICON_LOCATION_DIR );
        if ( dest.exists() )
        {
            deleteDir( dest );
        }
        copyDir( EXAMPLES_DIR + File.separator + exampleDir + File.separator
            + ICON_SUBDIR, ICON_LOCATION_DIR );
    }

    @AfterClass
    public static void stopNeo()
    {
        try
        {
            neo.shutdown();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    @After
    public void addNewline()
    {
        System.out.println();
    }

    private static boolean deleteDir( File directory )
    {
        if ( directory.isDirectory() )
        {
            String[] contents = directory.list();
            for ( int i = 0; i < contents.length; i++ )
            {
                if ( !deleteDir( new File( directory, contents[i] ) ) )
                {
                    return false;
                }
            }
        }
        return directory.delete();
    }

    private static void copyDir( String source, String dest )
    {
        File destination = new File( dest );
        if ( !destination.exists() )
        {
            if ( !destination.mkdir() )
            {
                System.out.println( "Couldn't create destination directory: "
                    + destination );
            }
        }
        File directory = new File( source );
        if ( !directory.exists() || !directory.isDirectory() )
        {
            return;
        }
        String[] contents = directory.list();
        for ( int i = 0; i < contents.length; i++ )
        {
            File file = new File( source + File.separator + contents[i] );
            if ( !file.isFile() || !file.canRead() )
            {
                continue;
            }
            FileChannel in;
            try
            {
                in = new FileInputStream( file ).getChannel();
            }
            catch ( FileNotFoundException e )
            {
                System.err.println( "File not found: " + file );
                return;
            }
            FileChannel out = null;
            try
            {
                out = new FileOutputStream( dest + File.separator + contents[i] )
                    .getChannel();
            }
            catch ( FileNotFoundException e )
            {
                System.err.println( "File not found: " + dest + File.separator
                    + contents[i] );
                return;
            }
            try
            {
                in.transferTo( 0, in.size(), out );
                in.close();
                out.close();
            }
            catch ( IOException e )
            {
                e.printStackTrace();
                return;
            }
        }
    }
}
