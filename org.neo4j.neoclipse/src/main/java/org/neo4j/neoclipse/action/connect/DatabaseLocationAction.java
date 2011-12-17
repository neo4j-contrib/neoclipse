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
package org.neo4j.neoclipse.action.connect;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.PlatformUI;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.action.AbstractGraphAction;
import org.neo4j.neoclipse.action.Actions;
import org.neo4j.neoclipse.preference.Preferences;
import org.neo4j.neoclipse.view.ErrorMessage;
import org.neo4j.neoclipse.view.NeoGraphViewPart;

/**
 * Action to choose the database location.
 * 
 * @author Radhakrishna Kalyan
 */
public class DatabaseLocationAction extends AbstractGraphAction
{
    public DatabaseLocationAction( final NeoGraphViewPart neoGraphViewPart )
    {
        super( Actions.DATABASE_LOCATION, neoGraphViewPart );
        setEnabled( true );
    }

    @Override
    public void run()
    {
        try
        {
            DirectoryDialog dialog = new DirectoryDialog(
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.OPEN | SWT.SHEET );
            dialog.setText( "Neo4j Database Location" );
            dialog.setMessage( "Please choose the database location." );
            String dbLocation = dialog.open();
            IPreferenceStore pref = Activator.getDefault().getPreferenceStore();

            if ( dbLocation != null && !dbLocation.trim().isEmpty() )
            {
                pref.setValue( Preferences.DATABASE_LOCATION, dbLocation );
                pref.setValue( Preferences.DATABASE_RESOURCE_URI, dbLocation );
            }
        }
        catch ( Exception e )
        {
            ErrorMessage.showDialog( "Database location error", e );
        }
    }
}
