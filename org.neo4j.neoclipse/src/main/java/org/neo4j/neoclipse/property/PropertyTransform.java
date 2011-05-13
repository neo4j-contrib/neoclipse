/**
 * Licensed to Neo Technology under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Neo Technology licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.neo4j.neoclipse.Icons;

/**
 * Transform between property values and representations for editors.
 * 
 * @author Anders Nawroth
 */
public final class PropertyTransform
{
    /**
     * Transform between editor representation and property value.
     */
    public static abstract class PropertyHandler
    {
        private final Class<?> type;
        private final Icons icon;
        private final Object standard;
        private Validator validator = null;
        private final boolean isArray;

        private PropertyHandler( final Class<?> type, final Icons icon,
                final Object standard, final boolean array )
        {
            this.type = type;
            this.icon = icon;
            this.standard = standard;
            this.isArray = array;
        }

        /**
         * Transform from editor representation to property value.
         * 
         * @param o editor representation (mostly a String)
         * @return property value or null
         * @throws IOException
         */
        public Object parse( final Object o ) throws IOException
        {
            if ( isType( o.getClass() ) )
            {
                return o;
            }
            return parser( o );
        }

        /**
         * Compare type to the type of this handler.
         */
        public boolean isType( final Class<?> t )
        {
            return t.equals( type );
        }

        /**
         * Tell if type is array type.
         * 
         * @return true if type is array type
         */
        public boolean isArray()
        {
            return isArray;
        }

        protected abstract Object parser( Object o ) throws IOException;

        /**
         * Transform from property value to editor representation.
         * 
         * @param o property value
         * @return editor representation of the value
         */
        public String render( final Object o )
        {
            return type.cast( o ).toString();
        }

        /**
         * Get the icon image for this type.
         * 
         * @return
         */
        public ImageDescriptor descriptor()
        {
            return icon.descriptor();
        }

        /**
         * Get the icon image for this type.
         * 
         * @return
         */
        public Image image()
        {
            return icon.image();
        }

        /**
         * Get default value.
         * 
         * @return default value
         */
        public Object value()
        {
            return standard;
        }

        /**
         * Get simple name of the class.
         * 
         * @return
         */
        public String name()
        {
            return type.getSimpleName();
        }

        private class Validator implements ICellEditorValidator,
                IInputValidator
        {
            /**
             * cell editor validator
             */
            @Override
            public String isValid( final Object value )
            {
                try
                {
                    parse( value );
                }
                catch ( Exception e )
                {
                    return "Could not parse the input as type " + name() + ".";
                }
                return null;
            }

            /**
             * dialog field validator
             */
            @Override
            public String isValid( final String value )
            {
                return isValid( (Object) value );
            }
        }

        /**
         * get an input field validator
         * 
         * @return
         */
        public IInputValidator getValidator()
        {
            return getInternalValidator();
        }

        /**
         * get a cell editor validator
         * 
         * @return
         */
        private ICellEditorValidator getCellValidator()
        {
            return getInternalValidator();
        }

        /**
         * Get the real internal validator.
         * 
         * @return
         */
        private Validator getInternalValidator()
        {
            if ( validator == null )
            {
                validator = new Validator();
            }
            return validator;
        }

        /**
         * Editor for this property type.
         * 
         * @return editor
         */
        public CellEditor getEditor( final Composite parent )
        {
            CellEditor editor = getEditorType().getEditor( parent, this );
            editor.setValidator( getCellValidator() );
            return editor;
        }

        /**
         * Gets editor type for this property type. Override if another editor
         * than TEXT should be used.
         * 
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
     * 
     * @param input editor data for array
     * @return list containing the separated strings
     */
    protected static List<String> arrayToCollection( final Object input )
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
     * 
     * @param input editor data for String array
     * @return list containing the separated strings
     * @throws IOException
     */
    protected static List<String> stringArrayToCollection( final Object input )
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
     * 
     * @param input user data
     * @return user data minus brackets and leading/trailing whitespace
     */
    private static String removeBrackets( final Object input )
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
     * 
     * @param cls type to handle
     * @return handler for type
     */
    public static PropertyHandler getHandler( final Class<?> cls )
    {
        return HANDLERS.get( cls );
    }

    /**
     * Get a PropertyHandler for an object.
     * 
     * @param o object to handle
     * @return handler for type
     */
    public static PropertyHandler getHandler( final Object o )
    {
        return HANDLERS.get( o.getClass() );
    }

    /**
     * Render a property value.
     * 
     * @param o the value
     * @return rendition of value
     */
    public static String render( final Object o )
    {
        return HANDLERS.get( o.getClass() ).render( o );
    }

    /**
     * A Map from property type classes to property handlers. Use get() on the
     * map to retrieve the correct property handler with parse(), render() and
     * getEditorType() methods.
     */
    private static final Map<Class<?>, PropertyHandler> HANDLERS = new HashMap<Class<?>, PropertyHandler>()
    {
        private static final long serialVersionUID = 1L;
        {
            put( String.class, new PropertyHandler( String.class,
                    Icons.TYPE_STRING, "", false )
            {
                @Override
                protected Object parser( final Object o )
                {
                    return o;
                }
            } );
            put( String[].class, new PropertyHandler( String[].class,
                    Icons.TYPE_STRINGS, new String[0], true )
            {
                @Override
                protected Object parser( final Object o ) throws IOException
                {
                    List<String> items = stringArrayToCollection( o );
                    String[] res = new String[items.size()];
                    for ( int i = 0; i < res.length; i++ )
                    {
                        res[i] = items.get( i );
                    }
                    return res;
                }

                @Override
                public String render( final Object o )
                {
                    String[] res = new String[( (String[]) o ).length];
                    for ( int i = 0; i < res.length; i++ )
                    {
                        String s = ( (String[]) o )[i];
                        s = s.replaceAll( "\"", "\\\\\"" );
                        res[i] = '"' + s + '"';
                    }
                    return Arrays.toString( res );
                }
            } );
            put( Integer.class, new PropertyHandler( Integer.class,
                    Icons.TYPE_INT, 0, false )
            {
                @Override
                protected Object parser( final Object o )
                {
                    return Integer.parseInt( (String) o );
                }
            } );
            put( int[].class, new PropertyHandler( int[].class,
                    Icons.TYPE_INTS, new int[0], true )
            {
                @Override
                protected Object parser( final Object o )
                {
                    List<String> items = arrayToCollection( o );
                    int[] res = new int[items.size()];
                    for ( int i = 0; i < res.length; i++ )
                    {
                        res[i] = Integer.parseInt( items.get( i ) );
                    }
                    return res;
                }

                @Override
                public String render( final Object o )
                {
                    return Arrays.toString( (int[]) o );
                }
            } );
            put( Double.class, new PropertyHandler( Double.class,
                    Icons.TYPE_DOUBLE, 0d, false )
            {
                @Override
                protected Object parser( final Object o )
                {
                    return Double.parseDouble( (String) o );
                }
            } );
            put( double[].class, new PropertyHandler( double[].class,
                    Icons.TYPE_DOUBLES, new double[0], true )
            {
                @Override
                protected Object parser( final Object o )
                {
                    List<String> items = arrayToCollection( o );
                    double[] res = new double[items.size()];
                    for ( int i = 0; i < res.length; i++ )
                    {
                        res[i] = Double.parseDouble( items.get( i ) );
                    }
                    return res;
                }

                @Override
                public String render( final Object o )
                {
                    return Arrays.toString( (double[]) o );
                }
            } );
            put( Float.class, new PropertyHandler( Float.class,
                    Icons.TYPE_FLOAT, 0f, false )
            {
                @Override
                protected Object parser( final Object o )
                {
                    return Float.parseFloat( (String) o );
                }
            } );
            put( float[].class, new PropertyHandler( float[].class,
                    Icons.TYPE_FLOATS, new float[0], true )
            {
                @Override
                protected Object parser( final Object o )
                {
                    List<String> items = arrayToCollection( o );
                    float[] res = new float[items.size()];
                    for ( int i = 0; i < res.length; i++ )
                    {
                        res[i] = Float.parseFloat( items.get( i ) );
                    }
                    return res;
                }

                @Override
                public String render( final Object o )
                {
                    return Arrays.toString( (float[]) o );
                }
            } );
            put( Boolean.class, new PropertyHandler( Boolean.class,
                    Icons.TYPE_BOOLEAN, false, false )
            {
                // has it's dedicated editor, handling transforms,
                // so we just pass things through here
                @Override
                protected Object parser( final Object o )
                {
                    return Boolean.parseBoolean( (String) o );
                }
            } );
            put( boolean[].class, new PropertyHandler( boolean[].class,
                    Icons.TYPE_BOOLEANS, new boolean[0], true )
            {
                @Override
                protected Object parser( final Object o )
                {
                    List<String> items = arrayToCollection( o );
                    boolean[] res = new boolean[items.size()];
                    for ( int i = 0; i < res.length; i++ )
                    {
                        res[i] = Boolean.parseBoolean( items.get( i ) );
                    }
                    return res;
                }

                @Override
                public String render( final Object o )
                {
                    return Arrays.toString( (boolean[]) o );
                }
            } );
            put( Byte.class, new PropertyHandler( Byte.class, Icons.TYPE_BYTE,
                    (byte) 0, false )
            {
                @Override
                protected Object parser( final Object o )
                {
                    return Byte.parseByte( (String) o );
                }
            } );
            put( byte[].class, new PropertyHandler( byte[].class,
                    Icons.TYPE_BYTES, new byte[0], true )
            {
                @Override
                protected Object parser( final Object o )
                {
                    List<String> items = arrayToCollection( o );
                    byte[] res = new byte[items.size()];
                    for ( int i = 0; i < res.length; i++ )
                    {
                        res[i] = Byte.parseByte( items.get( i ) );
                    }
                    return res;
                }

                @Override
                public String render( final Object o )
                {
                    return Arrays.toString( (byte[]) o );
                }
            } );
            put( Short.class, new PropertyHandler( Short.class,
                    Icons.TYPE_SHORT, (short) 0, false )
            {
                @Override
                protected Object parser( final Object o )
                {
                    return Short.parseShort( (String) o );
                }
            } );
            put( short[].class, new PropertyHandler( short[].class,
                    Icons.TYPE_SHORTS, new short[0], true )
            {
                @Override
                protected Object parser( final Object o )
                {
                    List<String> items = arrayToCollection( o );
                    short[] res = new short[items.size()];
                    for ( int i = 0; i < res.length; i++ )
                    {
                        res[i] = Short.parseShort( items.get( i ) );
                    }
                    return res;
                }

                @Override
                public String render( final Object o )
                {
                    return Arrays.toString( (short[]) o );
                }
            } );
            put( Long.class, new PropertyHandler( Long.class, Icons.TYPE_LONG,
                    0L, false )
            {
                @Override
                protected Object parser( final Object o )
                {
                    return Long.parseLong( (String) o );
                }
            } );
            put( long[].class, new PropertyHandler( long[].class,
                    Icons.TYPE_LONGS, new long[0], true )
            {
                @Override
                protected Object parser( final Object o )
                {
                    List<String> items = arrayToCollection( o );
                    long[] res = new long[items.size()];
                    for ( int i = 0; i < res.length; i++ )
                    {
                        res[i] = Long.parseLong( items.get( i ) );
                    }
                    return res;
                }

                @Override
                public String render( final Object o )
                {
                    return Arrays.toString( (long[]) o );
                }
            } );
            put( Character.class, new PropertyHandler( Character.class,
                    Icons.TYPE_CHAR, (char) 0, false )
            {
                @Override
                protected Object parser( final Object o )
                {
                    String s = (String) o;
                    if ( s.length() > 0 )
                    {
                        return ( (String) o ).charAt( 0 );
                    }
                    return null;
                }
            } );
            put( char[].class, new PropertyHandler( char[].class,
                    Icons.TYPE_CHARS, new char[0], true )
            {
                @Override
                protected Object parser( final Object o )
                {
                    List<String> items = arrayToCollection( o );
                    char[] res = new char[items.size()];
                    for ( int i = 0; i < res.length; i++ )
                    {
                        res[i] = items.get( i ).charAt( 0 );
                    }
                    return res;
                }

                @Override
                public String render( final Object o )
                {
                    return Arrays.toString( (char[]) o );
                }
            } );
        }
    };
}
