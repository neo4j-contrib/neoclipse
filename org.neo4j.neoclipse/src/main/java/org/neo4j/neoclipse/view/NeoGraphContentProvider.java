/**
 * Licensed to Neo Technology under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Neo Technology licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.neo4j.neoclipse.view;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.zest.core.viewers.IGraphEntityRelationshipContentProvider;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.graphdb.DefaultTraverser;
import org.neo4j.neoclipse.graphdb.GraphCallable;
import org.neo4j.neoclipse.graphdb.GraphDbServiceManager;
import org.neo4j.neoclipse.graphdb.TraversalStrategy;
import org.neo4j.neoclipse.preference.Preferences;
import org.neo4j.neoclipse.reltype.DirectedRelationship;
import org.neo4j.neoclipse.reltype.RelationshipTypeHashSet;
import org.neo4j.neoclipse.reltype.RelationshipTypesProvider;
import org.neo4j.neoclipse.reltype.RelationshipTypesProviderWrapper;

/**
 * Provides the elements that must be displayed in the graph.
 * 
 * @author Peter H&auml;nsgen
 */
public class NeoGraphContentProvider implements
        IGraphEntityRelationshipContentProvider
{
    /**
     * Limit the number of nodes returned.
     */
    private final RelationshipTypesProvider relTypesProvider = RelationshipTypesProviderWrapper.getInstance();
    /**
     * The view.
     */
    protected NeoGraphViewPart view;
    private final TraversalStrategy traverser = new DefaultTraverser();
    // private final TraversalStrategy traverser = new DefaultTraverser();
    private final Set<RelationshipType> relTypes = new RelationshipTypeHashSet();
    private final IPreferenceStore preferenceStore;

    /**
     * The constructor.
     */
    public NeoGraphContentProvider( final NeoGraphViewPart view )
    {
        this.view = view;
        preferenceStore = Activator.getDefault().getPreferenceStore();
    }

    /**
     * Returns the relationships between the given nodes.
     */
    @Override
    public Object[] getRelationships( final Object source, final Object dest )
    {
        if ( source == null || dest == null )
        {
            return new Object[] {};
        }
        final Node start = (Node) source;
        final Node end = (Node) dest;
        try
        {
            return Activator.getDefault().getGraphDbServiceManager().submitTask(
                    new Callable<Object[]>()
                    {
                        @Override
                        public Object[] call() throws Exception
                        {
                            return traverser.getRelationships( start, end ).toArray();
                        }
                    }, "find rels" ).get();
        }
        catch ( InterruptedException e )
        {
            e.printStackTrace();
        }
        catch ( ExecutionException e )
        {
            e.printStackTrace();
        }
        return new Object[] {};
    }

    /**
     * Returns all nodes the given node is connected with.
     */
    @Override
    public Object[] getElements( final Object inputElement )
    {
        if ( inputElement == null )
        {
            return new Node[] {};
        }
        final Node node = (Node) inputElement;
        GraphDbServiceManager gsm = Activator.getDefault().getGraphDbServiceManager();
        if ( gsm == null || !gsm.isRunning() )
        {
            return new Node[] { node };
        }
        try
        {
            return gsm.submitTask( new GraphCallable<Object[]>()
            {
                @Override
                public Object[] call( final GraphDatabaseService graphDb )
                {
                    return getTheElements( node, graphDb );
                }
            }, "get elements" ).get();
        }
        catch ( InterruptedException e )
        {
            e.printStackTrace();
        }
        catch ( ExecutionException e )
        {
            e.printStackTrace();
        }
        return new Node[] { node };
    }

    private Object[] getTheElements( final Node node,
            final GraphDatabaseService graphDb )
    {
        GraphDbServiceManager nsm = Activator.getDefault().getGraphDbServiceManager();
        if ( nsm == null || !nsm.isRunning() )
        {
            return new Node[] { node };
        }
        Collection<? extends DirectedRelationship> relDirList;
        relDirList = relTypesProvider.getFilteredDirectedRelationships();
        if ( relDirList.isEmpty() )
        {
            // if there are no relationship types,
            // there can't be any relationships ...
            return new Node[] { node };
        }
        Object[] relDirListArray = relDirList.toArray();
        relTypes.clear();
        for ( Object o : relDirListArray )
        {
            if ( o instanceof RelationshipType )
            {
                relTypes.add( (RelationshipType) o );
            }
        }
        final int depth = view.getTraversalDepth();
        int max = preferenceStore.getInt( Preferences.MAX_NODES );
        return traverser.getNodes( node, relDirList, depth, max, nsm ).toArray();
    }

    @Override
    public void dispose()
    {
    }

    @Override
    public void inputChanged( final Viewer viewer, final Object oldInput,
            final Object newInput )
    {
    }
}
