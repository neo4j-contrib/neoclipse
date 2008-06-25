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
package org.neo4j.neoclipse.view;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;

/**
 * Editors used for property values.
 * @author anders
 */
public enum NeoPropertyEditor
{
    NONE
    {
        CellEditor getEditor( Composite parent )
        {
            return null;
        }
    },
    TEXT
    {
        CellEditor getEditor( Composite parent )
        {
            return new TextCellEditor( parent );
        }
    },
    BOOLEAN
    {
        CellEditor getEditor( Composite parent )
        {
            return new CheckboxCellEditor( parent );
        }
    };
    /**
     * Get actual editor for this property editor type.
     * @param parent parent object
     * @return cell editor for a property object
     */
    abstract CellEditor getEditor( Composite parent );
}
