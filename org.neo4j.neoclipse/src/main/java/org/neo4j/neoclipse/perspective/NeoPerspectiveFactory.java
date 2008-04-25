/*
 * NeoPerspectiveFactory.java
 */
package org.neo4j.neoclipse.perspective;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.neo4j.neoclipse.view.NeoGraphViewPart;

/**
 * This class represents a perspective for neo which consists of neo-specific
 * views.
 * 
 * @author	Peter H&auml;nsgen
 */
public class NeoPerspectiveFactory implements IPerspectiveFactory
{
    /**
     * The ID of the neo perspective.
     */
    public static final String ID = "org.neo4j.neoclipse.NeoPerspective";
    
    /*
     * Some internal constants.
     */
    private static final String GRAPH_AREA      = "graphArea";
    private static final String PROPERTIES_AREA = "propertiesArea";
    
    /**
     * Creates the perspective.
     */
    public void createInitialLayout(IPageLayout layout)
    {
        String editorArea = layout.getEditorArea();
        
        // do not show an editor (for now), take full space for views only
        layout.setEditorAreaVisible(false);

        // neo graph view
        IFolderLayout graph = layout.createFolder(GRAPH_AREA,
                IPageLayout.BOTTOM, (float) 0.95, editorArea);
        graph.addView(NeoGraphViewPart.ID);

        // properties view
        IFolderLayout props = layout.createFolder(PROPERTIES_AREA,
                IPageLayout.BOTTOM, (float) 0.75, GRAPH_AREA);
        props.addView(IPageLayout.ID_PROP_SHEET);
        
        // view shortcuts
        layout.addShowViewShortcut(NeoGraphViewPart.ID);        
        layout.addShowViewShortcut(IPageLayout.ID_PROP_SHEET);        
    }
}
