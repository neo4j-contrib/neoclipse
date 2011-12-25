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
package org.neo4j.neoclipse;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.help.IWorkbenchHelpSystem;
import org.neo4j.neoclipse.perspective.NeoPerspectiveFactory;
import org.neo4j.neoclipse.preference.Preferences;
import org.neo4j.neoclipse.view.NeoGraphViewPart;

public class Application extends WorkbenchAdvisor implements IApplication
{
    @Override
    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor( final IWorkbenchWindowConfigurer configurer )
    {
        return new ApplicationWindowAdvisor( configurer );
    }

    @Override
    public Object start( final IApplicationContext context ) throws Exception
    {
        try
        {
            Location workspaceLocation = Platform.getInstanceLocation();
            if ( !workspaceLocation.isSet() )
            {
                Location installLocation = Platform.getInstallLocation();
                String dataPath = installLocation.getURL().getPath() + "neoclipse-workspace" + File.separator;
                File dir = new File( dataPath );
                if ( !dir.exists() )
                {
                    if ( !dir.mkdirs() )
                    {
                        throw new RuntimeException( "Could not create the directory: " + dir );
                    }
                }
                URL dataLocation = new URL( "file", null, dataPath );
                workspaceLocation.set( dataLocation, false );
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

        Display display = PlatformUI.createDisplay();
        try
        {
            int returnCode = PlatformUI.createAndRunWorkbench( display, this );
            if ( returnCode == PlatformUI.RETURN_RESTART )
            {
                return IApplication.EXIT_RESTART;
            }
            return IApplication.EXIT_OK;
        }
        finally
        {
            display.dispose();
        }
    }

    @Override
    public void stop()
    {
        if ( !PlatformUI.isWorkbenchRunning() )
        {
            return;
        }
        final IWorkbench workbench = PlatformUI.getWorkbench();
        final Display display = workbench.getDisplay();
        display.syncExec( new Runnable()
        {
            @Override
            public void run()
            {
                if ( !display.isDisposed() )
                {
                    workbench.close();
                }
            }
        } );
    }

    @Override
    public String getInitialWindowPerspectiveId()
    {
        return NeoPerspectiveFactory.ID;
    }

    @Override
    public void preWindowOpen( final IWorkbenchWindowConfigurer wwc )
    {
        wwc.setShowMenuBar( false );
        wwc.setShowFastViewBars( false );
        wwc.setShowStatusLine( true );
        wwc.setShowCoolBar( true );
    }

    @Override
    public void postStartup()
    {
        super.postStartup();
        // show help on startup if the user wants it
        boolean showHelp = Activator.getDefault().getPreferenceStore().getBoolean( Preferences.HELP_ON_START );
        if ( showHelp )
        {
            IWorkbenchHelpSystem helpSystem = PlatformUI.getWorkbench().getHelpSystem();
            helpSystem.displayDynamicHelp();

            NeoGraphViewPart graphView = (NeoGraphViewPart) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(
                    NeoGraphViewPart.ID );
            if ( graphView != null )
            {
                graphView.setFocus();
            }
        }
    }
}
