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

import org.eclipse.jface.resource.ImageDescriptor;
import org.neo4j.neoclipse.view.NeoGraphViewPart;

abstract public class AbstractGraphAction extends AbstractBaseAction
{
    protected NeoGraphViewPart graphView;

    public AbstractGraphAction( final String name,
        final ImageDescriptor image, final NeoGraphViewPart neoGraphViewPart )
    {
        super( name, image );
        this.graphView = neoGraphViewPart;
    }

    public AbstractGraphAction( final Actions action,
        final NeoGraphViewPart neoGraphViewPart )
    {
        super( action );
        this.graphView = neoGraphViewPart;
    }

    public AbstractGraphAction( final Actions action, final int style,
        final NeoGraphViewPart neoGraphViewPart )
    {
        super( action, style );
        this.graphView = neoGraphViewPart;
    }
}