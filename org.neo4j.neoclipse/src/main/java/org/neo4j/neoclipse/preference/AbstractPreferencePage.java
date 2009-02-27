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
        super( FieldEditorPreferencePage.GRID );
        setPreferenceStore( Activator.getDefault().getPreferenceStore() );
    }

    /**
     * Initializes the page.
     */
    public void init( IWorkbench workbench )
    {
        // this method is needed!
    }

    /**
     * Adds a separator element to this PreferencePage.
     */
    public void addSeparator()
    {
        Label spacer = new Label( getFieldEditorParent(), SWT.SEPARATOR
            | SWT.HORIZONTAL );
        GridData spacerData = new GridData( GridData.FILL_HORIZONTAL );
        spacerData.horizontalSpan = 3;
        spacer.setLayoutData( spacerData );
    }

    public void addNote( final String title, final String message )
    {
        Composite note = createNoteComposite( getFieldEditorParent().getFont(),
            getFieldEditorParent(), title, message );
        GridData spacerData = new GridData( GridData.FILL_HORIZONTAL );
        spacerData.horizontalSpan = 3;
        note.setLayoutData( spacerData );
    }

    @Override
    protected void addField( final FieldEditor editor )
    {
        super.addField( editor );
        addSeparator();
    }

    protected void addField( FieldEditor editor, String note )
    {
        super.addField( editor );
        addNote( "Note:", note );
        addSeparator();
    }
}
