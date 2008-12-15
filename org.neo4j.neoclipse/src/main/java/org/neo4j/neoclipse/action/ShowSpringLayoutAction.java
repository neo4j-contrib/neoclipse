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
package org.neo4j.neoclipse.action;

import org.eclipse.jface.action.Action;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.SpringLayoutAlgorithm;
import org.neo4j.neoclipse.NeoIcons;
import org.neo4j.neoclipse.view.NeoGraphViewPart;

/**
 * This action sets the layout of the graph viewer to spring layout.
 * @author Peter H&auml;nsgen
 */
public class ShowSpringLayoutAction extends Action
{
    /**
     * The view.
     */
    protected NeoGraphViewPart view;

    /**
     * The constructor.
     */
    public ShowSpringLayoutAction( NeoGraphViewPart view )
    {
        super( "Spring Layout", Action.AS_RADIO_BUTTON );
        this.view = view;
        setText( "Spring Layout" );
        setToolTipText( "Spring Layout" );
        setImageDescriptor( NeoIcons.getDescriptor( NeoIcons.SPRING ) );
        setChecked( true );
    }

    /**
     * Executes the action.
     */
    public void run()
    {
        if ( isChecked() )
        {
            view.getViewer()
                .setLayoutAlgorithm(
                    new SpringLayoutAlgorithm(
                        LayoutStyles.NO_LAYOUT_NODE_RESIZING ), true );
        }
    }
}
