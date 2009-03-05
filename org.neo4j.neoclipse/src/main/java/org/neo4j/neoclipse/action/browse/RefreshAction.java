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
package org.neo4j.neoclipse.action.browse;

import org.eclipse.jface.action.Action;
import org.neo4j.neoclipse.action.AbstractGraphAction;
import org.neo4j.neoclipse.action.Actions;
import org.neo4j.neoclipse.view.NeoGraphViewPart;

/**
 * This action refreshes the graph view, e.g. it sets the current node again as
 * input source.
 * @author Peter H&auml;nsgen
 */
public class RefreshAction extends AbstractGraphAction
{
    /**
     * The constructor.
     */
    public RefreshAction( NeoGraphViewPart view )
    {
        super( Actions.REFRESH, Action.AS_PUSH_BUTTON, view );
    }

    /**
     * Executes the action.
     */
    public void run()
    {
        graphView.refresh();
    }
}
