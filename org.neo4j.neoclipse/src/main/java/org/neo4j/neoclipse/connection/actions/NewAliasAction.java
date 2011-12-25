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

import org.eclipse.swt.widgets.Display;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.action.Actions;
import org.neo4j.neoclipse.connection.AbstractConnectionTreeAction;
import org.neo4j.neoclipse.connection.dialogs.CreateAliasDialog;

/**
 * @author Radhakrishna Kalyan
 * 
 */
public class NewAliasAction extends AbstractConnectionTreeAction
{

    public NewAliasAction()
    {
        super( Actions.NEW_CONNECTION );
    }

    @Override
    public void run()
    {
        CreateAliasDialog dlg = new CreateAliasDialog( Display.getCurrent().getActiveShell(),
                CreateAliasDialog.Type.CREATE );
        dlg.open();
        Activator.getDefault().getAliasManager().notifyListners();
    }
}
