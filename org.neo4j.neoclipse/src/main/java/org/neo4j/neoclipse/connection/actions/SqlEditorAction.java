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
package org.neo4j.neoclipse.connection.actions;

import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.action.Actions;
import org.neo4j.neoclipse.connection.AbstractConnectionTreeAction;
import org.neo4j.neoclipse.editor.SqlEditorView;
import org.neo4j.neoclipse.graphdb.GraphDbServiceManager;
import org.neo4j.neoclipse.view.ErrorMessage;

/**
 * @author Radhakrishna Kalyan
 * 
 */
public class SqlEditorAction extends AbstractConnectionTreeAction implements IViewActionDelegate
{

    public SqlEditorAction()
    {
        super( Actions.NEW_CYPHER_EDITOR );
        setEnabled( isAvailable() );
    }

    @Override
    public void run()
    {
        try
        {
            final GraphDbServiceManager gsm = Activator.getDefault().getGraphDbServiceManager();
            if ( !gsm.isRunning() )
            {
                throw new IllegalStateException( "There is no active Neo4j service." );
            }
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView( SqlEditorView.ID );
        }
        catch ( Exception e )
        {
            ErrorMessage.showDialog( "Cypher editor problem", e );
        }
    }

    @Override
    public boolean isAvailable()
    {
        if ( getConnectionView() == null )
        {
            return false;
        }
        return getConnectionView().getSelectedAlias() != null;

    }
}
