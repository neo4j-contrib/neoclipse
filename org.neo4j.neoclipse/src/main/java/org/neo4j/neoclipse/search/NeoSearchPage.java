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
package org.neo4j.neoclipse.search;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.search.ui.ISearchPage;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.neo4j.neoclipse.view.NeoGraphViewPart;

/**
 * This class represents a search page in the search dialog to perform
 * Neo-specific searches. The found nodes will be shown in the search result
 * view.
 * @author Peter H&auml;nsgen
 */
public class NeoSearchPage extends DialogPage implements ISearchPage
{
    public static final String ID = "org.neo4j.neoclipse.search.NeoSearchPage";
    /**
     * The input field for the search expression.
     */
    private Text expressionField;

    /**
     * The container of this page.
     */
    private ISearchPageContainer container;

    /**
     * Initializes the content of the search page.
     */
    public void createControl( final Composite parent )
    {
        initializeDialogUnits( parent );

        Composite comp = new Composite( parent, SWT.NONE );
        GridLayout layout = new GridLayout( 1, false );
        comp.setLayout( layout );

        Label label = new Label( comp, SWT.NONE );
        label.setText( "Search expression:" );
        label.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );

        expressionField = new Text( comp, SWT.SINGLE | SWT.BORDER );
        expressionField.setLayoutData( new GridData( GridData.GRAB_HORIZONTAL
            | GridData.FILL_HORIZONTAL ) );

        // do some validation
        expressionField.addModifyListener( new ModifyListener()
        {
            public void modifyText( final ModifyEvent event )
            {
                try
                {
                    // try to compile in order to validate input
                    String expression = expressionField.getText();
                    Pattern.compile( expression );

                    container.setPerformActionEnabled( true );
                }
                catch ( PatternSyntaxException p )
                {
                    setErrorMessage( "The search expression is not a valid regular expression." );
                    container.setPerformActionEnabled( false );
                }
            }
        } );

        setControl( comp );
    }

    /**
     * Sets the owning search dialog.
     */
    public void setContainer( final ISearchPageContainer container )
    {
        this.container = container;
    }

    /**
     * Performs the search.
     */
    public boolean performAction()
    {
        try
        {
            // determine expression from input fields
            String expression = expressionField.getText();

            Pattern p = Pattern.compile( expression );
            NeoSearchExpression ex = new NeoSearchExpression( p );
            NeoGraphViewPart gv = (NeoGraphViewPart) PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getActivePage().findView(
                    NeoGraphViewPart.ID );

            NewSearchUI.runQueryInBackground( new NeoSearchQuery( ex, gv ) );

            return true;
        }
        catch ( PatternSyntaxException p )
        {
            return false;
        }
    }
}
