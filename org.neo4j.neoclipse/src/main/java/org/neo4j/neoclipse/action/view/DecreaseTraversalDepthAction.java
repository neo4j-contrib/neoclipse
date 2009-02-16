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
import org.neo4j.neoclipse.NeoIcons;
import org.neo4j.neoclipse.view.NeoGraphViewPart;

/**
 * Decreases the traversal depth of the neo graph view.
 * @author Peter H&auml;nsgen
 */
public class DecreaseTraversalDepthAction extends Action
{
    private static final String DECREASE_TRAVERSAL_DEPTH = "Decrease Traversal Depth";
    /**
     * The view.
     */
    protected NeoGraphViewPart view;

    /**
     * The constructor.
     */
    public DecreaseTraversalDepthAction( NeoGraphViewPart view )
    {
        super( DECREASE_TRAVERSAL_DEPTH, Action.AS_PUSH_BUTTON );
        this.view = view;
        setToolTipText( DECREASE_TRAVERSAL_DEPTH );
        setImageDescriptor( NeoIcons.getDescriptor( NeoIcons.MINUS_ENABLED ) );
        setDisabledImageDescriptor( NeoIcons
            .getDescriptor( NeoIcons.MINUS_DISABLED ) );
    }

    /**
     * Executes the action.
     */
    public void run()
    {
        view.decTraversalDepth();
    }
}
