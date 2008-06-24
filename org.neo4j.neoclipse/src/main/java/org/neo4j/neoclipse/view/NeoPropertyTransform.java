package org.neo4j.neoclipse.view;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings( "serial" )
public class NeoPropertyTransform
{
    protected interface Parser
    {
        Object transform( Object o );
    }
    protected interface Renderer
    {
        Object transform( Object o );
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
            put( Integer.class, new Parser()
            {
                public Object transform( Object o )
                {
                    return Integer.parseInt( (String) o );
                }
            } );
            put( Double.class, new Parser()
            {
                public Object transform( Object o )
                {
                    return Double.parseDouble( (String) o );
                }
            } );
            put( Float.class, new Parser()
            {
                public Object transform( Object o )
                {
                    return Float.parseFloat( (String) o );
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
            put( Byte.class, new Parser()
            {
                public Object transform( Object o )
                {
                    return Byte.parseByte( (String) o );
                }
            } );
            put( Short.class, new Parser()
            {
                public Object transform( Object o )
                {
                    return Short.parseShort( (String) o );
                }
            } );
            put( Long.class, new Parser()
            {
                public Object transform( Object o )
                {
                    return Long.parseLong( (String) o );
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
                    return Arrays.toString( (String[]) o );
                }
            } );
        }
    };
}
