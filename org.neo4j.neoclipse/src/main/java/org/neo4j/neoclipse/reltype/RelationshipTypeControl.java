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

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.neoclipse.event.NeoclipseEvent;
import org.neo4j.neoclipse.event.NeoclipseEventListener;
import org.neo4j.neoclipse.event.NeoclipseListenerList;

/**
 * This is a single item in the relationship type view, wrapping a relationship
 * type.
 * @author anders
 */
public class RelationshipTypeControl
{
    private final NeoclipseListenerList listeners = new NeoclipseListenerList();
    private final RelationshipType relType;
    private boolean in = true;
    private boolean out = true;

    /**
     * Wrap a relationship type for display in the table viewer.
     * @param relType
     */
    RelationshipTypeControl( final RelationshipType relType )
    {
        this.relType = relType;
    }

    /**
     * Is incoming realtionships selected?
     * @return
     */
    public boolean isIn()
    {
        return in;
    }

    /**
     * Set if incoming relationships are selected.
     * @param in
     */
    public void setIn( final boolean in )
    {
        if ( this.in != in )
        {
            this.in = in;
            notifyListeners();
        }
    }

    /**
     * Is outgoing realtionships selected?
     * @return
     */
    public boolean isOut()
    {
        return out;
    }

    /**
     * Set if outgoing relationships are selected.
     * @param in
     */
    public void setOut( final boolean out )
    {
        if ( this.out != out )
        {
            this.out = out;
            notifyListeners();
        }
    }

    /**
     * Get the relationship type in this wrapper.
     * @return
     */
    public RelationshipType getRelType()
    {
        return relType;
    }

    /**
     * True if either incoming or outgoing or both exists.
     * @return
     */
    public boolean hasDirection()
    {
        return in || out;
    }

    /**
     * Get direction filter for this relationship type.
     * @return
     */
    public Direction getDirection()
    {
        if ( in && out )
        {
            return Direction.BOTH;
        }
        if ( in )
        {
            return Direction.INCOMING;
        }
        if ( out )
        {
            return Direction.OUTGOING;
        }
        throw new RuntimeException(
            "There is no direction set for RelationshipType: " + relType.name() );
    }

    /**
     * Notify listeners in/out attributes changed.
     */
    private void notifyListeners()
    {
        NeoclipseEvent event = new NeoclipseEvent( this );
        listeners.notifyListeners( event );
    }

    /**
     * Add a new listener to changes.
     * @param newListener
     */
    public void addChangeListener( final NeoclipseEventListener newListener )
    {
        listeners.add( newListener );
    }

    @Override
    public String toString()
    {
        return relType.name() + in + out;
    }

    @Override
    public boolean equals( final Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj instanceof RelationshipTypeControl )
        {
            return this.getRelType().name().equals(
                ((RelationshipTypeControl) obj).getRelType().name() );
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return this.getRelType().name().hashCode();
    }
}