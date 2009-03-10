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

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;
import org.neo4j.neoclipse.property.PropertyTransform.PropertyHandler;

/**
 * Editors used for property values.
 * @author Anders Nawroth
 */
public enum PropertyEditor
{
    NONE
    {
        CellEditor getEditor( Composite parent, PropertyHandler propertyHandler )
        {
            return null;
        }
    },
    TEXT
    {
        CellEditor getEditor( Composite parent, PropertyHandler propertyHandler )
        {
            return new PropertyCellEditor( parent, propertyHandler );
        }
    };
    /**
     * Get actual editor for this property editor type.
     * @param parent
     *            parent object
     * @param propertyHandler
     * @return cell editor for a property object
     */
    abstract CellEditor getEditor( Composite parent,
        PropertyHandler propertyHandler );

    public static class PropertyCellEditor extends TextCellEditor
    {
        private final PropertyHandler propertyHandler;
        private Object untouched = null;

        public PropertyCellEditor( Composite parent,
            PropertyHandler propertyHandler )
        {
            super( parent );
            this.propertyHandler = propertyHandler;
        }

        @Override
        protected Object doGetValue()
        {
            String value = text.getText();
            if ( !propertyHandler.isType( String.class )
                && "".equals( ((String) value).trim() ) )
            {
                return untouched;
            }
            try
            {
                return propertyHandler.parse( value );
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
            return untouched;
        }

        @Override
        protected void doSetValue( Object value )
        {
            untouched = value;
            super.doSetValue( propertyHandler.render( value ) );
        }

        @Override
        protected boolean isCorrect( Object value )
        {
            if ( value instanceof String && "".equals( ((String) value).trim() ) )
            {
                return true;
            }
            return super.isCorrect( value );
        }
    }
}
