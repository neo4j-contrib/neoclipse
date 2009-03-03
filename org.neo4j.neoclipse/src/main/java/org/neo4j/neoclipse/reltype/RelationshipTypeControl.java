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
package org.neo4j.neoclipse.reltype;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.neo4j.api.core.RelationshipType;

public class RelationshipTypeControl
{
    private List<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();

    private final RelationshipType relType;
    private boolean in = false;
    private boolean out = true;

    public RelationshipTypeControl( RelationshipType relType )
    {
        this.relType = relType;
        notifyListeners();
    }

    public boolean isIn()
    {
        return in;
    }

    public void setIn( boolean in )
    {
        this.in = in;
        notifyListeners();
    }

    public boolean isOut()
    {
        return out;
    }

    public void setOut( boolean out )
    {
        this.out = out;
        notifyListeners();
    }

    public RelationshipType getRelType()
    {
        return relType;
    }

    private void notifyListeners()
    {
        for ( PropertyChangeListener listener : listeners )
        {
            listener.propertyChange( null );
        }
    }

    public void addChangeListener( PropertyChangeListener newListener )
    {
        listeners.add( newListener );
    }

    @Override
    public String toString()
    {
        return relType.name() + in + out;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj instanceof RelationshipTypeControl )
        {
            return this.getRelType().equals(
                ((RelationshipTypeControl) obj).getRelType() );
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return this.getRelType().hashCode();
    }
}