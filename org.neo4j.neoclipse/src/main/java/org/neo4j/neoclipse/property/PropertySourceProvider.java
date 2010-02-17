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

import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.IPropertySourceProvider;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * Resolves the properties for Neo nodes and relationships.
 * @author Peter H&auml;nsgen
 */
public class PropertySourceProvider implements IPropertySourceProvider
{
    private final NeoPropertySheetPage propertySheet;

    public PropertySourceProvider(
        final NeoPropertySheetPage neoPropertySheetPage )
    {
        propertySheet = neoPropertySheetPage;
    }

    public IPropertySource getPropertySource( final Object source )
    {
        if ( source instanceof Node )
        {
            return new NodePropertySource( (Node) source, propertySheet );
        }
        else if ( source instanceof Relationship )
        {
            return new RelationshipPropertySource( (Relationship) source,
                propertySheet );
        }
        else
        {
            return null;
        }
    }
}
