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
