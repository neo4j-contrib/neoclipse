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
package org.neo4j.neoclipse.view;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

public class Dialog
{
    public static void openError( final Shell parent, final String title,
            final String message )
    {
        UiHelper.asyncExec( new Runnable()
        {
            @Override
            public void run()
            {
                MessageDialog.openError( parent, title, message );
            }
        } );
    }

    public static void openError( final String title, final String message )
    {
        Dialog.openError( null, title, message );
    }

    public static void openWarning( final Shell parent, final String title,
            final String message )
    {
        UiHelper.asyncExec( new Runnable()
        {
            @Override
            public void run()
            {
                MessageDialog.openWarning( parent, title, message );
            }
        } );
    }

    public static void openWarning( final String title, final String message )
    {
        Dialog.openWarning( null, title, message );
    }
}
