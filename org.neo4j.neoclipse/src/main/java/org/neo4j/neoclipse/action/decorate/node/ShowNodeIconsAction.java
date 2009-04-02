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
package org.neo4j.neoclipse.action.decorate.node;

import org.neo4j.neoclipse.action.AbstractCheckboxAction;
import org.neo4j.neoclipse.action.Actions;
import org.neo4j.neoclipse.view.NeoGraphViewPart;

/**
 * This action handles the node icons setting.
 * @author Anders Nawroth
 */
public class ShowNodeIconsAction extends AbstractCheckboxAction
{
    /**
     * The constructor.
     */
    public ShowNodeIconsAction( final NeoGraphViewPart view,
        final boolean checked )
    {
        super( Actions.NODE_ICONS, checked, view );
    }

    /**
     * Executes the action.
     */
    public void run()
    {
        graphView.getLabelProvider().getViewSettings().setShowNodeIcons(
            isChecked() );
        graphView.refreshPreserveLayout();
    }
}
