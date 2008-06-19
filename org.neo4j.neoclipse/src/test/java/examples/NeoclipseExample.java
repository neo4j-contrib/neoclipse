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
        neo.shutdown();
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
