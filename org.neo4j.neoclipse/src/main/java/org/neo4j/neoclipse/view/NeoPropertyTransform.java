package org.neo4j.neoclipse.view;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings( "serial" )
public class NeoPropertyTransform
{
    protected interface Parser
    {
        Object transform( Object o ) throws IOException;
    }
    protected interface Renderer
    {
        Object transform( Object o );
    }

    protected static List<String> arrayToCollection( Object input )
    {
        String in = removeBrackets( input );
        List<String> out = new ArrayList<String>();
        for ( String item : in.split( "," ) )
        {
            item = item.trim();
            if ( item.length() > 0 )
            {
                out.add( item );
            }
        }
        return out;
    }

    protected static List<String> stringArrayToCollection( Object input ) throws IOException
    {
        String in = removeBrackets( input );
        List<String> out = new ArrayList<String>();
            StreamTokenizer tokenizer = new StreamTokenizer( new StringReader( in ) );
            tokenizer.wordChars( '0', '9' );
            while ( tokenizer.nextToken() != StreamTokenizer.TT_EOF )
            {
                if ( tokenizer.sval != null )
                {
                    out.add( tokenizer.sval );
                }
            }
        return out;
    }

    private static String removeBrackets( Object input )
    {
        String in = input.toString().trim();
        if ( in.charAt( 0 ) == '[' )
        {
            in = in.substring( 1 );
        }
        if ( in.charAt( in.length() - 1 ) == ']' )
        {
            in = in.substring( 0, in.length() - 1 );
        }
        return in;
    }

    public static final Map<Class<?>,Parser> parserMap = new HashMap<Class<?>,Parser>()
    {
        {
            put( String.class, new Parser()
            {
                public Object transform( Object o )
                {
                    return o;
                }
            } );
            put( String[].class, new Parser()
            {
                public Object transform( Object o ) throws IOException
                {
                    List<String> items = stringArrayToCollection( o );
                    String[] res = new String[items.size()];
                    for ( int i = 0; i < res.length; i++ )
                    {
                        res[i] = items.get( i );
                    }
                    return res;
                }
            } );
            put( Integer.class, new Parser()
            {
                public Object transform( Object o )
                {
                    return Integer.parseInt( (String) o );
                }
            } );
            put( int[].class, new Parser()
            {
                public Object transform( Object o )
                {
                    List<String> items = arrayToCollection( o );
                    int[] res = new int[items.size()];
                    for ( int i = 0; i < res.length; i++ )
                    {
                        res[i] = Integer.parseInt( items.get( i ) );
                    }
                    return res;
                }
            } );
            put( Double.class, new Parser()
            {
                public Object transform( Object o )
                {
                    return Double.parseDouble( (String) o );
                }
            } );
            put( double[].class, new Parser()
            {
                public Object transform( Object o )
                {
                    List<String> items = arrayToCollection( o );
                    double[] res = new double[items.size()];
                    for ( int i = 0; i < res.length; i++ )
                    {
                        res[i] = Double.parseDouble( items.get( i ) );
                    }
                    return res;
                }
            } );
            put( Float.class, new Parser()
            {
                public Object transform( Object o )
                {
                    return Float.parseFloat( (String) o );
                }
            } );
            put( float[].class, new Parser()
            {
                public Object transform( Object o )
                {
                    List<String> items = arrayToCollection( o );
                    float[] res = new float[items.size()];
                    for ( int i = 0; i < res.length; i++ )
                    {
                        res[i] = Float.parseFloat( items.get( i ) );
                    }
                    return res;
                }
            } );
            put( Boolean.class, new Parser()
            {
                public Object transform( Object o )
                {
                    // has it's dedicated editor, handling transforms
                    return o;
                }
            } );
            put( boolean[].class, new Parser()
            {
                public Object transform( Object o )
                {
                    List<String> items = arrayToCollection( o );
                    boolean[] res = new boolean[items.size()];
                    for ( int i = 0; i < res.length; i++ )
                    {
                        res[i] = Boolean.parseBoolean( items.get( i ) );
                    }
                    return res;
                }
            } );
            put( Byte.class, new Parser()
            {
                public Object transform( Object o )
                {
                    return Byte.parseByte( (String) o );
                }
            } );
            put( byte[].class, new Parser()
            {
                public Object transform( Object o )
                {
                    List<String> items = arrayToCollection( o );
                    byte[] res = new byte[items.size()];
                    for ( int i = 0; i < res.length; i++ )
                    {
                        res[i] = Byte.parseByte( items.get( i ) );
                    }
                    return res;
                }
            } );
            put( Short.class, new Parser()
            {
                public Object transform( Object o )
                {
                    return Short.parseShort( (String) o );
                }
            } );
            put( short[].class, new Parser()
            {
                public Object transform( Object o )
                {
                    List<String> items = arrayToCollection( o );
                    short[] res = new short[items.size()];
                    for ( int i = 0; i < res.length; i++ )
                    {
                        res[i] = Short.parseShort( items.get( i ) );
                    }
                    return res;
                }
            } );
            put( Long.class, new Parser()
            {
                public Object transform( Object o )
                {
                    return Long.parseLong( (String) o );
                }
            } );
            put( long[].class, new Parser()
            {
                public Object transform( Object o )
                {
                    List<String> items = arrayToCollection( o );
                    long[] res = new long[items.size()];
                    for ( int i = 0; i < res.length; i++ )
                    {
                        res[i] = Long.parseLong( items.get( i ) );
                    }
                    return res;
                }
            } );
            put( Character.class, new Parser()
            {
                public Object transform( Object o )
                {
                    String s = (String) o;
                    if ( s.length() > 0 )
                    {
                        return ((String) o).charAt( 0 );
                    }
                    return null;
                }
            } );
            put( char[].class, new Parser()
            {
                public Object transform( Object o )
                {
                    List<String> items = arrayToCollection( o );
                    char[] res = new char[items.size()];
                    for ( int i = 0; i < res.length; i++ )
                    {
                        res[i] = items.get( i ).charAt( 0 );
                    }
                    return res;
                }
            } );
        }
    };
    public static final Map<Class<?>,Renderer> rendererMap = new HashMap<Class<?>,Renderer>()
    {
        {
            put( String.class, new Renderer()
            {
                public Object transform( Object o )
                {
                    return (String) o;
                }
            } );
            put( Integer.class, new Renderer()
            {
                public Object transform( Object o )
                {
                    return ((Integer) o).toString();
                }
            } );
            put( Double.class, new Renderer()
            {
                public Object transform( Object o )
                {
                    return ((Double) o).toString();
                }
            } );
            put( Float.class, new Renderer()
            {
                public Object transform( Object o )
                {
                    return ((Float) o).toString();
                }
            } );
            put( Boolean.class, new Renderer()
            {
                public Object transform( Object o )
                {
                    // has it's dedicated editor, handling transforms
                    return o;
                }
            } );
            put( Byte.class, new Renderer()
            {
                public Object transform( Object o )
                {
                    return ((Byte) o).toString();
                }
            } );
            put( Short.class, new Renderer()
            {
                public Object transform( Object o )
                {
                    return ((Short) o).toString();
                }
            } );
            put( Long.class, new Renderer()
            {
                public Object transform( Object o )
                {
                    return ((Long) o).toString();
                }
            } );
            put( Character.class, new Renderer()
            {
                public Object transform( Object o )
                {
                    return ((Character) o).toString();
                }
            } );
            put( int[].class, new Renderer()
            {
                public Object transform( Object o )
                {
                    return Arrays.toString( (int[]) o );
                }
            } );
            put( boolean[].class, new Renderer()
            {
                public Object transform( Object o )
                {
                    return Arrays.toString( (boolean[]) o );
                }
            } );
            put( byte[].class, new Renderer()
            {
                public Object transform( Object o )
                {
                    return Arrays.toString( (byte[]) o );
                }
            } );
            put( char[].class, new Renderer()
            {
                public Object transform( Object o )
                {
                    return Arrays.toString( (char[]) o );
                }
            } );
            put( double[].class, new Renderer()
            {
                public Object transform( Object o )
                {
                    return Arrays.toString( (double[]) o );
                }
            } );
            put( float[].class, new Renderer()
            {
                public Object transform( Object o )
                {
                    return Arrays.toString( (float[]) o );
                }
            } );
            put( long[].class, new Renderer()
            {
                public Object transform( Object o )
                {
                    return Arrays.toString( (long[]) o );
                }
            } );
            put( short[].class, new Renderer()
            {
                public Object transform( Object o )
                {
                    return Arrays.toString( (short[]) o );
                }
            } );
            put( String[].class, new Renderer()
            {
                public Object transform( Object o )
                {
                    String[] res = new String[((String[]) o).length];
                    for ( int i = 0; i < res.length; i++ )
                    {
                        String s = ((String[]) o)[i];
                        s = s.replaceAll( "\"", "\\\\\"" );
                        System.out.println(s);
                        res[i] = '"' + s + '"';
                    }
                    return Arrays.toString( res );
                }
            } );
        }
    };
}
