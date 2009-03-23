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
package org.neo4j.neoclipse.perspective;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.neo4j.neoclipse.reltype.RelationshipTypeView;
import org.neo4j.neoclipse.view.NeoGraphViewPart;

/**
 * This class represents a perspective for neo which consists of neo-specific
 * views.
 * @author Peter H&auml;nsgen
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
    private static final String GRAPH_AREA = "graphArea";
    private static final String PROPERTIES_AREA = "propertiesArea";

    /**
     * Creates the perspective.
     */
    public void createInitialLayout( final IPageLayout layout )
    {
        String editorArea = layout.getEditorArea();
        // do not show an editor (for now), take full space for views only
        layout.setEditorAreaVisible( false );
        // neo graph view
        IFolderLayout graph = layout.createFolder( GRAPH_AREA,
            IPageLayout.BOTTOM, 0.95f, editorArea );
        graph.addView( NeoGraphViewPart.ID );
        layout.getViewLayout( NeoGraphViewPart.ID ).setCloseable( false );
        // properties view
        IFolderLayout props = layout.createFolder( PROPERTIES_AREA,
            IPageLayout.BOTTOM, 0.75f, GRAPH_AREA );
        props.addView( IPageLayout.ID_PROP_SHEET );
        // relationship types view
        IFolderLayout types = layout.createFolder( "typesArea",
            IPageLayout.RIGHT, 0.55f, PROPERTIES_AREA );
        types.addView( RelationshipTypeView.ID );
        // view shortcuts
        layout.addShowViewShortcut( NeoGraphViewPart.ID );
        layout.addShowViewShortcut( IPageLayout.ID_PROP_SHEET );
        layout.addShowViewShortcut( RelationshipTypeView.ID );
    }
}
