/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package examples;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.neo4j.api.core.EmbeddedNeo;
import org.neo4j.api.core.NeoService;

/**
 * Utility to set up a Neo4j instance for test cases.
 * @author Anders Nawroth
 */
public abstract class NeoclipseExample
{
    private static String STORE_LOCATION_DIR = "target"
        + System.getProperty( "file.separator" ) + "neo";
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
}
