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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheetSorter;

/**
 * This class is a workaround to sort the property
 * categories in the way we want.
 * 
 * @author Anders Nawroth
 */
public class NeoPropertySheetPage extends PropertySheetPage
{
    private class NeoPropertySheetSorter extends PropertySheetSorter
    {
        private static final String RELATIONSHIP = "Relationship";

        @Override
        public int compareCategories( String categoryA, String categoryB )
        {
            if ( RELATIONSHIP.equals( categoryA )
                || RELATIONSHIP.equals( categoryB ) )
            {
                // reverse the categories in this case
                return super.compareCategories( categoryB, categoryA );
            }
            return super.compareCategories( categoryA, categoryB );
        }
    }

    public NeoPropertySheetPage()
    {
        super();
    }

    @Override
    public void createControl( Composite parent )
    {
        super.createControl( parent );
        setSorter( new NeoPropertySheetSorter() );
    }
}