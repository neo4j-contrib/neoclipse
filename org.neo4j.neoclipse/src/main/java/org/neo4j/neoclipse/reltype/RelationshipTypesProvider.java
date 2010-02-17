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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.event.NeoclipseEvent;
import org.neo4j.neoclipse.event.NeoclipseEventListener;
import org.neo4j.neoclipse.event.NeoclipseListenerList;
import org.neo4j.neoclipse.view.NeoGraphLabelProviderWrapper;

/**
 * Provide (filtered) relationship types. Initial clients: Database graph view
 * and Relationship types view.
 * @author anders
 */
public class RelationshipTypesProvider implements IContentProvider,
    IStructuredContentProvider
{
    private class ReltypeCtrlChangeListener implements NeoclipseEventListener
    {
        public void stateChanged( final NeoclipseEvent event )
        {
            notifyFilterListeners( event );
        }
    }

    private boolean viewAll = true;
    private final Set<RelationshipType> fakeTypes = new RelationshipTypeHashSet();
    private Set<RelationshipType> currentRelTypes = Collections.emptySet();
    private final Map<RelationshipType,RelationshipTypeControl> currentRelTypeCtrls = new RelationshipTypeHashMap<RelationshipTypeControl>();
    private final NeoclipseListenerList filterListeners = new NeoclipseListenerList();
    private final NeoclipseListenerList typesListeners = new NeoclipseListenerList();
    private final ReltypeCtrlChangeListener reltypeCtrlChangeListener = new ReltypeCtrlChangeListener();
    private final NeoclipseListenerList refreshListeners = new NeoclipseListenerList();

    /**
     * Factory method that creates relationship type items for the table view.
     * @param relType
     *            the relationship type to wrap
     * @return
     */
    public RelationshipTypeControl createRelationshipTypeControl(
        final RelationshipType relType )
    {
        RelationshipTypeControl relTypeCtrl = new RelationshipTypeControl(
            relType );
        relTypeCtrl.addChangeListener( reltypeCtrlChangeListener );
        return relTypeCtrl;
    }

    /**
     * Get all relationship types. If the viewAll attribute is set to false only
     * the relationship types that was handled in the current database graph
     * view will be returned.
     */
    public Object[] getElements( final Object inputElement )
    {
        if ( viewAll )
        {
            currentRelTypes = getRelationshipTypesFromNeo();
            currentRelTypes.addAll( fakeTypes );
        }
        else
        {
            currentRelTypes = NeoGraphLabelProviderWrapper.getInstance()
                .getRelationshipTypes();
            currentRelTypes.addAll( fakeTypes );
        }
        refreshListeners.notifyListeners( new NeoclipseEvent( this ) );
        for ( RelationshipType relType : currentRelTypes )
        {
            // only add if it's not already there
            if ( !currentRelTypeCtrls.containsKey( relType ) )
            {
                currentRelTypeCtrls.put( relType,
                    createRelationshipTypeControl( relType ) );
            }
        }
        return currentRelTypeCtrls.values().toArray();
    }

    /**
     * Get all relationship types in the database.
     * @return
     */
    public Set<RelationshipType> getRelationshipTypesFromNeo()
    {
        Set<RelationshipType> relationshipTypes;
        relationshipTypes = new HashSet<RelationshipType>();
        GraphDatabaseService ns = Activator.getDefault().getNeoServiceSafely();
        if ( ns == null )
        {
            // todo ?
            return Collections.emptySet();
        }
        for ( RelationshipType relType : ns.getRelationshipTypes() )
        {
            relationshipTypes.add( relType );
        }
        return relationshipTypes;
    }

    /**
     * Get all relationship types in the database and additional "fake" types.
     * @return
     */
    public Set<RelationshipType> getCurrentRelationshipTypes()
    {
        return currentRelTypes;
    }

    /**
     * Add a "fake" relationship type. It will be persisted to the database upon
     * usage (creating a relationship of this type).
     * @param name
     *            the name of the relationship type
     */
    public RelationshipType addFakeType( final String name )
    {
        RelationshipType relType = DynamicRelationshipType.withName( name );
        fakeTypes.add( relType );
        notifyTypesListeners( new NeoclipseEvent( relType ) );
        return relType;
    }

    /**
     * Get the table items corresponding to a collection of relationship types.
     * @param relTypes
     *            the relationship types to select
     * @return the relationship type controls that are used in the table
     */
    public Collection<RelationshipTypeControl> getFilteredControls(
        final Collection<RelationshipType> relTypes )
    {
        Collection<RelationshipTypeControl> relTypeCtrls = new ArrayList<RelationshipTypeControl>();
        for ( RelationshipType relType : relTypes )
        {
            RelationshipTypeControl relTypeCtrl = currentRelTypeCtrls
                .get( relType );
            if ( relTypeCtrl != null )
            {
                relTypeCtrls.add( relTypeCtrl );
            }
        }
        return relTypeCtrls;
    }

    /**
     * Get relationship types and direction from current filterset.
     * @return an array of RelationshipType and Direction (alternating)
     */
    public List<Object> getFilteredRelTypesDirections()
    {
        if ( currentRelTypeCtrls.isEmpty() )
        {
            throw new NotFoundException();
        }
        List<Object> relDirList = new ArrayList<Object>();
        for ( RelationshipTypeControl relTypeCtrl : currentRelTypeCtrls
            .values() )
        {
            if ( relTypeCtrl.hasDirection() )
            {
                relDirList.add( relTypeCtrl.getRelType() );
                relDirList.add( relTypeCtrl.getDirection() );
            }
        }
        return relDirList;
    }

    /**
     * Set all relationship types to the same filtering.
     * @param in
     *            state for incoming relationships
     * @param out
     *            state for outgoing relationships
     */
    public void setAllFilters( final boolean in, final boolean out )
    {
        filterListeners.setInhibit( true );
        for ( RelationshipType relType : currentRelTypeCtrls.keySet() )
        {
            RelationshipTypeControl relTypeCtrl = currentRelTypeCtrls
                .get( relType );
            if ( relTypeCtrl != null )
            {
                relTypeCtrl.setIn( in );
                relTypeCtrl.setOut( out );
            }
        }
        filterListeners.setInhibit( false );
        notifyFilterListeners( new NeoclipseEvent( this ) );
    }

    public void refresh()
    {
        fakeTypes.clear();
        currentRelTypes.clear();
        currentRelTypeCtrls.clear();
    }

    public void dispose()
    {
    }

    public void inputChanged( final Viewer viewer, final Object oldInput,
        final Object newInput )
    {
    }

    /**
     * Set provider to return all existing relationship types.
     */
    public void setViewAll()
    {
        viewAll = true;
    }

    /**
     * Set provider to only return relationship types currently used in the
     * database graph view.
     */
    public void setViewTraversed()
    {
        viewAll = false;
    }

    /**
     * Notify listeners something changed in the relationship type filters.
     * @param event
     */
    private void notifyFilterListeners( final NeoclipseEvent event )
    {
        filterListeners.notifyListeners( event );
    }

    /**
     * Add listener to relationship types filter changes.
     * @param newListener
     */
    public void addFilterStatusListener(
        final NeoclipseEventListener newListener )
    {
        filterListeners.add( newListener );
    }

    /**
     * Add listener to relationship type (e.g. addition) changes.
     * @param newListener
     */
    public void addTypeChangeListener( final NeoclipseEventListener newListener )
    {
        typesListeners.add( newListener );
    }

    /**
     * Notify listeners something changed in the relationship types.
     * @param event
     */
    private void notifyTypesListeners( final NeoclipseEvent event )
    {
        typesListeners.notifyListeners( event );
    }

    /**
     * Add listener to complete refresh events.
     * @param newListener
     */
    public void addTypeRefreshListener( final NeoclipseEventListener newListener )
    {
        refreshListeners.add( newListener );
    }
}
