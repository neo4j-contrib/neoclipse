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
package org.neo4j.neoclipse.property;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.swt.widgets.Composite;

/**
 * Transform between property values and representations for editors.
 * @author Anders Nawroth
 */
public final class PropertyTransform
{
    /**
     * Transform between editor representation and property value.
     */
    public static abstract class PropertyHandler
    {
        /**
         * Transform from editor representation to property value.
         * @param o
         *            editor representation (mostly a String)
         * @return property value or null
         * @throws IOException
         */
        public abstract Object parse( Object o ) throws IOException;

        /**
         * Transform from property value to editor representation.
         * @param o
         *            property value
         * @return editor representation of the value
         */
        public abstract Object render( Object o );

        /**
         * Get type wrapped in this editor.
         * @return the type
         */
        protected abstract Class<?> getType();

        /**
         * Editor for this property type.
         * @return editor
         */
        public CellEditor getEditor( final Composite parent )
        {
            CellEditor editor = PropertyEditor.TEXT.getEditor( parent );
            editor.setValidator( new ICellEditorValidator()
            {
                public String isValid( Object value )
                {
                    try
                    {
                        parse( value );
                    }
                    catch ( Exception e )
                    {
                        return "Could not parse the input as type "
                            + getType().getSimpleName() + ".";
                    }
                    return null;
                }
            } );
            return editor;
        }

        /**
         * Gets editor type for this property type. Override if another editor
         * than TEXT should be used.
         * @return editor type
         */
        protected PropertyEditor getEditorType()
        {
            return PropertyEditor.TEXT;
        }
    }

    /**
     * Prevent instantiation.
     */
    private PropertyTransform()
    {
        // no instances
    }

    /**
     * Transform a String to a List of Strings.
     * @param input
     *            editor data for array
     * @return list containing the separated strings
     */
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

    /**
     * Transform a String to a list of Strings that are used as Strings in the
     * property value. Adds a little more features like being able to have
     * spaces inside a string. Citation signs (") are escaped by a backslash
     * (\).
     * @param input
     *            editor data for String array
     * @return list containing the separated strings
     * @throws IOException
     */
    protected static List<String> stringArrayToCollection( Object input )
        throws IOException
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

    /**
     * Simple utility to remove [brackets] used as markers for an array.
     * @param input
     *            user data
     * @return user data minus brackets and leading/trailing whitespace
     */
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

    /**
     * Get a PropertyHandler for a specific type.
     * @param cls
     *            type to handle
     * @return handler for type
     */
    public static PropertyHandler getPropertyHandler( Class<?> cls )
    {
        return HANDLERS.get( cls );
    }

    /**
     * A Map from property type classes to property handlers. Use get() on the
     * map to retrieve the correct property handler with parse(), render() and
     * getEditorType() methods.
     */
    private static final Map<Class<?>,PropertyHandler> HANDLERS = new HashMap<Class<?>,PropertyHandler>()
    {
        private static final long serialVersionUID = 1L;

        {
            put( String.class, new PropertyHandler()
            {
                public Object parse( Object o )
                {
                    return o;
                }

                public Object render( Object o )
                {
                    return (String) o;
                }

                protected Class<?> getType()
                {
                    return String.class;
                }
            } );
            put( String[].class, new PropertyHandler()
            {
                public Object parse( Object o ) throws IOException
                {
                    List<String> items = stringArrayToCollection( o );
                    String[] res = new String[items.size()];
                    for ( int i = 0; i < res.length; i++ )
                    {
                        res[i] = items.get( i );
                    }
                    return res;
                }

                public Object render( Object o )
                {
                    String[] res = new String[((String[]) o).length];
                    for ( int i = 0; i < res.length; i++ )
                    {
                        String s = ((String[]) o)[i];
                        s = s.replaceAll( "\"", "\\\\\"" );
                        res[i] = '"' + s + '"';
                    }
                    return Arrays.toString( res );
                }

                protected Class<?> getType()
                {
                    return String[].class;
                }
            } );
            put( Integer.class, new PropertyHandler()
            {
                public Object parse( Object o )
                {
                    return Integer.parseInt( (String) o );
                }

                public Object render( Object o )
                {
                    return ((Integer) o).toString();
                }

                protected Class<?> getType()
                {
                    return Integer.class;
                }
            } );
            put( int[].class, new PropertyHandler()
            {
                public Object parse( Object o )
                {
                    List<String> items = arrayToCollection( o );
                    int[] res = new int[items.size()];
                    for ( int i = 0; i < res.length; i++ )
                    {
                        res[i] = Integer.parseInt( items.get( i ) );
                    }
                    return res;
                }

                public Object render( Object o )
                {
                    return Arrays.toString( (int[]) o );
                }

                protected Class<?> getType()
                {
                    return int[].class;
                }
            } );
            put( Double.class, new PropertyHandler()
            {
                public Object parse( Object o )
                {
                    return Double.parseDouble( (String) o );
                }

                public Object render( Object o )
                {
                    return ((Double) o).toString();
                }

                protected Class<?> getType()
                {
                    return Double.class;
                }
            } );
            put( double[].class, new PropertyHandler()
            {
                public Object parse( Object o )
                {
                    List<String> items = arrayToCollection( o );
                    double[] res = new double[items.size()];
                    for ( int i = 0; i < res.length; i++ )
                    {
                        res[i] = Double.parseDouble( items.get( i ) );
                    }
                    return res;
                }

                public Object render( Object o )
                {
                    return Arrays.toString( (double[]) o );
                }

                protected Class<?> getType()
                {
                    return double.class;
                }
            } );
            put( Float.class, new PropertyHandler()
            {
                public Object parse( Object o )
                {
                    return Float.parseFloat( (String) o );
                }

                public Object render( Object o )
                {
                    return ((Float) o).toString();
                }

                protected Class<?> getType()
                {
                    return Float.class;
                }
            } );
            put( float[].class, new PropertyHandler()
            {
                public Object parse( Object o )
                {
                    List<String> items = arrayToCollection( o );
                    float[] res = new float[items.size()];
                    for ( int i = 0; i < res.length; i++ )
                    {
                        res[i] = Float.parseFloat( items.get( i ) );
                    }
                    return res;
                }

                public Object render( Object o )
                {
                    return Arrays.toString( (float[]) o );
                }

                protected Class<?> getType()
                {
                    return float[].class;
                }
            } );
            put( Boolean.class, new PropertyHandler()
            {
                // has it's dedicated editor, handling transforms,
                // so we just pass things through here
                public Object parse( Object o )
                {
                    return Boolean.parseBoolean( (String) o );
                }

                public Object render( Object o )
                {
                    return ((Boolean) o).toString();
                }

                protected Class<?> getType()
                {
                    return Boolean.class;
                }
            } );
            put( boolean[].class, new PropertyHandler()
            {
                public Object parse( Object o )
                {
                    List<String> items = arrayToCollection( o );
                    boolean[] res = new boolean[items.size()];
                    for ( int i = 0; i < res.length; i++ )
                    {
                        res[i] = Boolean.parseBoolean( items.get( i ) );
                    }
                    return res;
                }

                public Object render( Object o )
                {
                    return Arrays.toString( (boolean[]) o );
                }

                protected Class<?> getType()
                {
                    return boolean[].class;
                }
            } );
            put( Byte.class, new PropertyHandler()
            {
                public Object parse( Object o )
                {
                    return Byte.parseByte( (String) o );
                }

                public Object render( Object o )
                {
                    return ((Byte) o).toString();
                }

                protected Class<?> getType()
                {
                    return Byte.class;
                }
            } );
            put( byte[].class, new PropertyHandler()
            {
                public Object parse( Object o )
                {
                    List<String> items = arrayToCollection( o );
                    byte[] res = new byte[items.size()];
                    for ( int i = 0; i < res.length; i++ )
                    {
                        res[i] = Byte.parseByte( items.get( i ) );
                    }
                    return res;
                }

                public Object render( Object o )
                {
                    return Arrays.toString( (byte[]) o );
                }

                protected Class<?> getType()
                {
                    return byte[].class;
                }
            } );
            put( Short.class, new PropertyHandler()
            {
                public Object parse( Object o )
                {
                    return Short.parseShort( (String) o );
                }

                public Object render( Object o )
                {
                    return ((Short) o).toString();
                }

                protected Class<?> getType()
                {
                    return Short.class;
                }
            } );
            put( short[].class, new PropertyHandler()
            {
                public Object parse( Object o )
                {
                    List<String> items = arrayToCollection( o );
                    short[] res = new short[items.size()];
                    for ( int i = 0; i < res.length; i++ )
                    {
                        res[i] = Short.parseShort( items.get( i ) );
                    }
                    return res;
                }

                public Object render( Object o )
                {
                    return Arrays.toString( (short[]) o );
                }

                protected Class<?> getType()
                {
                    return short[].class;
                }
            } );
            put( Long.class, new PropertyHandler()
            {
                public Object parse( Object o )
                {
                    return Long.parseLong( (String) o );
                }

                public Object render( Object o )
                {
                    return ((Long) o).toString();
                }

                protected Class<?> getType()
                {
                    return Long.class;
                }
            } );
            put( long[].class, new PropertyHandler()
            {
                public Object parse( Object o )
                {
                    List<String> items = arrayToCollection( o );
                    long[] res = new long[items.size()];
                    for ( int i = 0; i < res.length; i++ )
                    {
                        res[i] = Long.parseLong( items.get( i ) );
                    }
                    return res;
                }

                public Object render( Object o )
                {
                    return Arrays.toString( (long[]) o );
                }

                protected Class<?> getType()
                {
                    return long[].class;
                }
            } );
            put( Character.class, new PropertyHandler()
            {
                public Object parse( Object o )
                {
                    String s = (String) o;
                    if ( s.length() > 0 )
                    {
                        return ((String) o).charAt( 0 );
                    }
                    return null;
                }

                public Object render( Object o )
                {
                    return ((Character) o).toString();
                }

                protected Class<?> getType()
                {
                    return Character.class;
                }
            } );
            put( char[].class, new PropertyHandler()
            {
                public Object parse( Object o )
                {
                    List<String> items = arrayToCollection( o );
                    char[] res = new char[items.size()];
                    for ( int i = 0; i < res.length; i++ )
                    {
                        res[i] = items.get( i ).charAt( 0 );
                    }
                    return res;
                }

                public Object render( Object o )
                {
                    return Arrays.toString( (char[]) o );
                }

                protected Class<?> getType()
                {
                    return char[].class;
                }
            } );
        }
    };
}
