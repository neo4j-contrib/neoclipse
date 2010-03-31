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
package org.neo4j.neoclipse.action.connect;

import org.eclipse.jface.dialogs.MessageDialog;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.action.AbstractGraphAction;
import org.neo4j.neoclipse.action.Actions;
import org.neo4j.neoclipse.view.NeoGraphViewPart;

/**
 * Action to start the database.
 * 
 * @author Anders Nawroth
 */
public class StartAction extends AbstractGraphAction
{
    private StopAction stopAction = null;

    public StartAction( final NeoGraphViewPart neoGraphViewPart )
    {
        super( Actions.START, neoGraphViewPart );
        setEnabled( true );
    }

    public void setStopAction( final StopAction stopAction )
    {
        this.stopAction = stopAction;
    }

    @Override
    public void run()
    {
        try
        {
            Activator.getDefault().getGraphDbServiceManager().startGraphDbService();
            setEnabled( false );
            if ( stopAction != null )
            {
                stopAction.setEnabled( true );
            }
            graphView.showSomeNode();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            String message = e.getMessage();
            Throwable throwable = e.getCause();
            int depth = 0;
            while ( throwable != null && depth++ < 10 )
            {
                if ( throwable.getMessage() != null )
                {
                    message += ": " + throwable.getMessage();
                }
                throwable = throwable.getCause();
            }
            MessageDialog.openInformation( null, "Database problem", message );

        }
    }
}
