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
package org.neo4j.neoclipse;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;

public class Application extends WorkbenchAdvisor implements IApplication
{
    public Object start( IApplicationContext context ) throws Exception
    {
//        System.out.println( "Hello world!" );
        Display display = PlatformUI.createDisplay();
        int returnCode = PlatformUI.createAndRunWorkbench( display, this );
        if ( returnCode == PlatformUI.RETURN_RESTART )
        {
            return IApplication.EXIT_RESTART;
        }
        else
        {
            return IApplication.EXIT_OK;
        }
    }

    public void stop()
    {
        // TODO Auto-generated method stub
    }

    @Override
    public String getInitialWindowPerspectiveId()
    {
        return "org.neo4j.neoclipse.NeoPerspective";
    }

    public void preWindowOpen( IWorkbenchWindowConfigurer wwc )
    {
        wwc.setShowMenuBar( false );
        wwc.setShowFastViewBars( false );
        wwc.setShowStatusLine( true );
        wwc.setShowCoolBar( false );
    }
}
