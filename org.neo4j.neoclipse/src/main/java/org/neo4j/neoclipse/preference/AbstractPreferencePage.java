/*
 * AbstractPreferencePage.java
 */
package org.neo4j.neoclipse.preference;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.neo4j.neoclipse.Activator;

/**
 * This is the common superclass for all neo preference pages.
 * 
 * @author Peter H&auml;nsgen
 */
public abstract class AbstractPreferencePage extends FieldEditorPreferencePage
        implements IWorkbenchPreferencePage
{
    /**
     * The constructor.
     */
    public AbstractPreferencePage()
    {
        super(FieldEditorPreferencePage.GRID);

        setPreferenceStore(Activator.getDefault().getPreferenceStore());
    }

    /**
     * Initializes the page.
     */
    public void init(IWorkbench workbench)
    {
    }

    /**
     * Adds a separator element to this PreferencePage.
     */
    public void addSeparator()
    {
        Label spacer = new Label(getFieldEditorParent(), SWT.SEPARATOR
                | SWT.HORIZONTAL);

        GridData spacerData = new GridData(GridData.FILL_HORIZONTAL);
        spacerData.horizontalSpan = 3;
        spacer.setLayoutData(spacerData);
    }
    
    public void addNote (String title, String message)
    {
        Composite note = createNoteComposite( getFieldEditorParent().getFont(), getFieldEditorParent(), title, message );
        GridData spacerData = new GridData(GridData.FILL_HORIZONTAL);
        spacerData.horizontalSpan = 3;
        note.setLayoutData(spacerData);
    }

    @Override
    protected void addField( FieldEditor editor)
    {
        super.addField( editor );
        addSeparator();
    }

    protected void addField( FieldEditor editor, String note )
    {
        super.addField( editor );
        addNote("Note:", note);
        addSeparator();
    }

    
}
