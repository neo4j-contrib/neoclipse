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
package org.neo4j.neoclipse.action.view;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.zest.core.viewers.ZoomContributionViewItem;
import org.neo4j.neoclipse.action.AbstractGraphAction;
import org.neo4j.neoclipse.action.Actions;
import org.neo4j.neoclipse.view.NeoGraphViewPart;

/**
 * This action allows to zoom in / out the graph view.
 * @author Peter H&auml;nsgen
 */
public class ZoomAction extends AbstractGraphAction implements IMenuCreator
{
    /**
     * The zoom menu.
     */
    private Menu menu;

    /**
     * The constructor.
     */
    public ZoomAction( final NeoGraphViewPart view )
    {
        super( Actions.ZOOM, Action.AS_DROP_DOWN_MENU, view );
        setMenuCreator( this );
    }

    /**
     * Executes the default action, which sets zoom level to page.
     */
    public void run()
    {
        // represents 100%, workaround for non-public API of zoom manager
        graphView.getZoomableViewer().zoomTo( 0, 0, 0, 0 );
    }

    /**
     * Returns a menu with the default zoom levels.
     */
    public Menu getMenu( final Control parent )
    {
        if ( menu == null )
        {
            menu = new Menu( parent );
            ZoomContributionViewItem zoom = new ZoomContributionViewItem(
                graphView );
            zoom.fill( menu, 0 );
        }
        return menu;
    }

    /**
     * Returns a menu with the default zoom levels.
     */
    public Menu getMenu( final Menu parent )
    {
        if ( menu == null )
        {
            menu = new Menu( parent );
            ZoomContributionViewItem zoom = new ZoomContributionViewItem(
                graphView );
            zoom.fill( menu, 0 );
        }
        return menu;
    }

    /**
     * Disposes the zoom level menu.
     */
    public void dispose()
    {
        if ( menu != null )
        {
            menu.dispose();
        }
    }
}
