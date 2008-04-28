/*
 * ZoomAction.java
 */
package org.neo4j.neoclipse.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.zest.core.viewers.ZoomContributionViewItem;
import org.neo4j.neoclipse.view.NeoGraphViewPart;

/**
 * This action allows to zoom in / out the graph view.
 * 
 * @author	Peter H&auml;nsgen
 */
public class ZoomAction extends Action implements IMenuCreator
{
    /**
     * The view.
     */
    private NeoGraphViewPart view;
    
    /**
     * The zoom menu.
     */
    private Menu menu;
    
    /**
     * The constructor.
     */
    public ZoomAction(NeoGraphViewPart view)
    {
        super("ZoomAction", Action.AS_DROP_DOWN_MENU);

        this.view = view;
        
        setMenuCreator(this);
    }

    /**
     * Executes the default action, which sets zoom level to page.
     */
    public void run()
    {
        // represents 100%, workaround for non-public API of zoom manager
        view.getZoomableViewer().zoomTo(0, 0, 0, 0);
    }

    /**
     * Returns a menu with the default zoom levels.
     */
    public Menu getMenu(Control parent)
    {
        if (menu == null)
        {
            menu = new Menu(parent);
            
            ZoomContributionViewItem zoom = new ZoomContributionViewItem(view);            
            zoom.fill(menu, 0);
        }

        return menu;
    }

    /**
     * Returns a menu with the default zoom levels.
     */
    public Menu getMenu(Menu parent)
    {
        if (menu == null)
        {
            menu = new Menu(parent);
            
            ZoomContributionViewItem zoom = new ZoomContributionViewItem(view);            
            zoom.fill(menu, 0);
        }

        return menu;
    }

    /**
     * Disposes the zoom level menu.
     */
    public void dispose()
    {
        if (menu != null)
        {
            menu.dispose();
        }
    }
}
