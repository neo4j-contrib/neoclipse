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
package org.neo4j.neoclipse.reltype;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.swt.SWT;

public class RelationshipTypeEditingSupport extends EditingSupport
{
    public enum ColumnType
    {
        HEADING, IN, OUT
    }

    private final ColumnType column;
    private final CellEditor editor;

    public RelationshipTypeEditingSupport( ColumnViewer viewer,
        ColumnType column )
    {
        super( viewer );
        this.column = column;
        switch ( column )
        {
            case IN:
            case OUT:
                editor = new CheckboxCellEditor( null, SWT.CHECK );
                break;
            default:
                editor = null;
        }
    }

    @Override
    protected boolean canEdit( Object element )
    {
        return editor != null;
    }

    @Override
    protected CellEditor getCellEditor( Object element )
    {
        return editor;
    }

    @Override
    protected Object getValue( Object element )
    {
        if ( element instanceof RelationshipTypeControl )
        {
            RelationshipTypeControl control = (RelationshipTypeControl) element;
            switch ( column )
            {
                case HEADING:
                    return control.getRelType();
                case IN:
                    return control.isIn();
                case OUT:
                    return control.isOut();
            }
        }
        return null;
    }

    @Override
    protected void setValue( Object element, Object value )
    {
        if ( element instanceof RelationshipTypeControl
            && value instanceof Boolean )
        {
            RelationshipTypeControl control = (RelationshipTypeControl) element;
            Boolean status = (Boolean) value;
            switch ( column )
            {
                case IN:
                    control.setIn( status );
                    break;
                case OUT:
                    control.setOut( status );
                    break;
            }
            getViewer().update( element, null );
        }
    }
}
