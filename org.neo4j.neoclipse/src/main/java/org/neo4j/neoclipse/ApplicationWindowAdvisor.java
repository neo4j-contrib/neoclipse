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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.help.IWorkbenchHelpSystem;
import org.neo4j.neoclipse.action.Actions;
import org.neo4j.neoclipse.reltype.RelationshipTypeView;
import org.neo4j.neoclipse.search.NeoSearchPage;

/**
 * Configure the workbench window.
 * @author Anders Nawroth
 */
public class ApplicationWindowAdvisor extends WorkbenchWindowAdvisor
{
    private ApplicationActionBarAdvisor actionBarAdvisor;

    public ApplicationWindowAdvisor( final IWorkbenchWindowConfigurer configurer )
    {
        super( configurer );
    }

    @Override
    public ActionBarAdvisor createActionBarAdvisor(
        final IActionBarConfigurer configurer )
    {
        actionBarAdvisor = new ApplicationActionBarAdvisor( configurer );
        return actionBarAdvisor;
    }

    @Override
    public void preWindowOpen()
    {
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        configurer.setShowCoolBar( true );
        configurer.setShowMenuBar( false );
        configurer.setShowStatusLine( true );
    }

    @Override
    public void postWindowOpen()
    {
        super.postWindowOpen();
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        ICoolBarManager coolBar = configurer.getActionBarConfigurer()
            .getCoolBarManager();
        coolBar.removeAll();
        actionBarAdvisor.fillCoolBar( coolBar );
        coolBar.update( true );
    }

    private static class ApplicationActionBarAdvisor extends ActionBarAdvisor
    {
        private Action preferencesAction;
        private Action propertiesAction;
        private Action reltypesAction;
        private Action helpViewAction;
        private Action helpWindowAction;
        private Action searchAction;

        public ApplicationActionBarAdvisor( IActionBarConfigurer configurer )
        {
            super( configurer );
        }

        @Override
        protected void makeActions( final IWorkbenchWindow window )
        {
            preferencesAction = new Action()
            {
                @Override
                public void run()
                {
                    Activator.getDefault().showPreferenceDialog( false );
                }
            };
            Actions.PREFERENCES.initialize( preferencesAction );

            propertiesAction = new Action()
            {
                @Override
                public void run()
                {
                    try
                    {
                        PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                            .getActivePage().showView(
                                "org.eclipse.ui.views.PropertySheet" );
                    }
                    catch ( PartInitException e )
                    {
                        e.printStackTrace();
                    }
                }
            };
            Actions.PROPERTIES.initialize( propertiesAction );

            reltypesAction = new Action()
            {
                @Override
                public void run()
                {
                    try
                    {
                        PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                            .getActivePage().showView( RelationshipTypeView.ID );
                    }
                    catch ( PartInitException e )
                    {
                        e.printStackTrace();
                    }
                }
            };
            Actions.RELTYPES_VIEW.initialize( reltypesAction );

            searchAction = new Action()
            {
                @Override
                public void run()
                {
                    NewSearchUI.openSearchDialog( window, NeoSearchPage.ID );
                }
            };
            Actions.SEARCH.initialize( searchAction );

            helpViewAction = new Action()
            {
                @Override
                public void run()
                {
                    try
                    {
                        final IWorkbenchHelpSystem helpSystem = PlatformUI
                            .getWorkbench().getHelpSystem();
                        helpSystem.displayDynamicHelp();
                    }
                    catch ( Throwable e )
                    {
                        e.printStackTrace();
                    }
                }
            };
            Actions.HELP_VIEW.initialize( helpViewAction );

            helpWindowAction = new Action()
            {
                @Override
                public void run()
                {
                    final IWorkbenchHelpSystem helpSystem = PlatformUI
                        .getWorkbench().getHelpSystem();
                    helpSystem.displayHelp();
                }
            };
            Actions.HELP_WINDOW.initialize( helpWindowAction );
        }

        @Override
        protected void fillCoolBar( final ICoolBarManager coolBar )
        {
            IToolBarManager main = new ToolBarManager( SWT.FLAT | SWT.RIGHT );
            main.add( preferencesAction );
            coolBar.add( new ToolBarContributionItem( main, "main" ) );
            IToolBarManager views = new ToolBarManager( SWT.FLAT | SWT.RIGHT );
            views.add( propertiesAction );
            views.add( reltypesAction );
            views.add( searchAction );
            views.add( helpViewAction );
            views.add( helpWindowAction );
            coolBar.add( new ToolBarContributionItem( views, "views" ) );
        }
    }
}
