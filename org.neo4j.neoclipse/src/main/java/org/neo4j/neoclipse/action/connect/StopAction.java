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

import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.action.AbstractGraphAction;
import org.neo4j.neoclipse.action.Actions;
import org.neo4j.neoclipse.graphdb.GraphDbServiceManager;
import org.neo4j.neoclipse.view.ErrorMessage;
import org.neo4j.neoclipse.view.NeoGraphViewPart;

/**
 * Action to stop the database.
 * 
 * @author Anders Nawroth
 * @author Radhakrishna Kalyan
 */
public class StopAction extends AbstractGraphAction
{
    public StopAction( final NeoGraphViewPart neoGraphViewPart )
    {
        super( Actions.STOP, neoGraphViewPart );
        setEnabled( false );
    }

    protected StopAction( Actions actions, NeoGraphViewPart neoGraphViewPart )
    {
        super( actions, neoGraphViewPart );
    }

    @Override
    public void run()
    {
        graphView.cleanTransactionBeforeShutdown();
        GraphDbServiceManager gsm = Activator.getDefault().getGraphDbServiceManager();
        try
        {
            gsm.stopGraphDbService().get();
        }
        catch ( Exception e )
        {
            ErrorMessage.showDialog( "Database problem", e );
        }
        Activator.getDefault().getAliasManager().notifyListners();
    }
}
