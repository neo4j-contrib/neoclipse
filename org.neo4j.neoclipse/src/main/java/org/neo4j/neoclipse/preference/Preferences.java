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
package org.neo4j.neoclipse.preference;

/**
 * Defines the preferences of the neo4j plugin.
 * 
 * @author Peter H&auml;nsgen
 */
public final class Preferences
{
    /**
     * Preventing instantiation.
     */
    private Preferences()
    {
        // preventing instantiation
    }

    /**
     * The location of the neo4j database in the file system.
     */
    public static final String DATABASE_LOCATION = "databaseLocation";
    /**
     * Connection mode, e.g. read/write, readonly
     */
    public static final String CONNECTION_MODE = "connectionMode";
    /**
     * Database resource URI using RemoteNeo.
     */
    public static final String DATABASE_RESOURCE_URI = "databaseResourceUri";
    /**
     * Show the help view when the application starts.
     */
    public static final String HELP_ON_START = "helpOnStart";
    /**
     * Maximum number of nodes to show.
     */
    public static final String MAX_NODES = "maxNodes";

    public static final String MAX_TRAVERSAL_DEPTH = "maxTraversalDepth";
}
