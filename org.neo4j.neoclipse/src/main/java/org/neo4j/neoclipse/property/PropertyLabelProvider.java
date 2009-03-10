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
package org.neo4j.neoclipse.property;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.neo4j.neoclipse.property.PropertyTransform.PropertyHandler;

public class PropertyLabelProvider implements ILabelProvider
{
    public Image getImage( Object value )
    {
        return PropertyTransform.getHandler( value ).image();
    }

    public String getText( Object value )
    {
        PropertyHandler propertyHandler = PropertyTransform.getHandler( value );
        if ( propertyHandler != null )
        {
            return propertyHandler.render( value );
        }
        else
        {
            return "(no rendering available)";
        }
    }

    public void addListener( ILabelProviderListener arg0 )
    {
        // TODO Auto-generated method stub
    }

    public void dispose()
    {
        // TODO Auto-generated method stub
    }

    public boolean isLabelProperty( Object element, String property )
    {
        return true;
    }

    public void removeListener( ILabelProviderListener arg0 )
    {
        // TODO Auto-generated method stub
    }
}
