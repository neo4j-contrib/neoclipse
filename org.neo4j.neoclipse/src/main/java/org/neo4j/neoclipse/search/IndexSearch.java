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
package org.neo4j.neoclipse.search;

/**
 * Encapsulates an index lookup or search.
 * 
 * @author Anders Nawroth
 */
public class IndexSearch
{
    enum Mode
    {
        EXACT_MATCH;
    }
    private final Mode mode;

    private final String propertyName;
    private final String searchString;
    private final Iterable<String> nodeIndexNames;
    private final Iterable<String> relationshipIndexNames;

    private IndexSearch( final Mode mode, final String propertyName,
            final String searchString,
            final Iterable<String> nodeIndexNames,
            final Iterable<String> relationshipIndexNames )
    {
        this.mode = mode;
        this.propertyName = propertyName;
        this.searchString = searchString;
        this.nodeIndexNames = nodeIndexNames;
        this.relationshipIndexNames = relationshipIndexNames;
    }

    public static IndexSearch exact( final String propertyName,
            final String searchString,
            final Iterable<String> nodeIndexNames,
            final Iterable<String> relationshipIndexNames )
    {
        return new IndexSearch( Mode.EXACT_MATCH, propertyName, searchString,
                nodeIndexNames,
                relationshipIndexNames );
    }

    public Mode getMode()
    {
        return mode;
    }

    public String getPropertyName()
    {
        return propertyName;
    }

    public String getSearchString()
    {
        return searchString;
    }

    public Iterable<String> getNodeIndexNames()
    {
        return nodeIndexNames;
    }

    public Iterable<String> getRelationshipIndexNames()
    {
        return relationshipIndexNames;
    }
}
