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
package org.neo4j.neoclipse.graphdb;

import org.neo4j.graphdb.GraphDatabaseService;

/**
 * Manages the life cycle of a {@link GraphDatabaseService} as well as other
 * components. Removes the tedious work of having to think about shutting down
 * components and the {@link GraphDatabaseService} when the JVM exists, in the
 * right order as well.
 */
public class GraphDbLifecycle
{
    /**
     * Field not final since it's nulled in the shutdown process (to be able to
     * support multiple calls to shutdown).
     */
    private GraphDatabaseService graphDb;
    private Thread shutdownHook;

    /**
     * Constructs a new {@link GraphDbLifecycle} instance with {@code graphDb}
     * as the {@link GraphDatabaseService}.
     * 
     * @param graphDb the {@link GraphDatabaseService} instance to manage.
     */
    public GraphDbLifecycle( final GraphDatabaseService graphDb )
    {
        this.graphDb = graphDb;
        this.shutdownHook = new Thread()
        {
            @Override
            public void run()
            {
                runJvmShutdownHook();
            }
        };
        Runtime.getRuntime().addShutdownHook( this.shutdownHook );
    }

    /**
     * Runs the shutdown process manually instead of waiting for it to happen
     * just before the JVM exists, see {@link #runJvmShutdownHook()}. Normally
     * this method isn't necessary to call, but can be good to have for special
     * cases.
     */
    public void manualShutdown()
    {
        runShutdown();
        if ( this.shutdownHook != null )
        {
            try
            {
                Runtime.getRuntime().removeShutdownHook( this.shutdownHook );
                this.shutdownHook = null;
            }
            catch ( IllegalStateException ise )
            {
                // already shutting down, so to late to remove hook
            }
        }
    }

    /**
     * Runs the shutdown process of all started services. Supports multiple
     * calls to it (if such would accidentally be done).
     */
    protected void runShutdown()
    {
        if ( this.graphDb != null )
        {
            this.graphDb.shutdown();
            this.graphDb = null;
        }
    }

    /**
     * Called right before the JVM exists. It's called from a thread registered
     * with {@link Runtime#addShutdownHook(Thread)}.
     */
    protected void runJvmShutdownHook()
    {
        runShutdown();
    }

    /**
     * @return the {@link GraphDatabaseService} instance passed in to the
     *         constructor,
     */
    public GraphDatabaseService graphDb()
    {
        return this.graphDb;
    }
}
