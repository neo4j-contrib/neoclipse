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
package org.neo4j.neoclipse.decorate;

import java.util.Map;
import java.util.Set;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.neoclipse.reltype.RelationshipTypeHashMap;

/**
 * Map relationship types to colors.
 * 
 * @author Anders Nawroth
 */
public class RelationshipTypeColorMapper
{
    private static class Colors
    {
        private final Color[] colors;

        public Colors( final float hue, final ColorSetting[] categories )
        {
            colors = new Color[categories.length];
            for ( ColorSetting category : categories )
            {
                colors[category.ordinal()] = new Color( Display.getDefault(),
                        new RGB( hue, category.getSaturation(),
                                category.getBrightness() ) );
            }
        }

        public Color getColor( final ColorSetting category )
        {
            return colors[category.ordinal()];
        }
    }

    /**
     * Map Objects to Colors for the graph.
     */
    private final Map<RelationshipType, Colors> colorMap = new RelationshipTypeHashMap<Colors>();
    /**
     * Create colors.
     */
    private final SimpleHueGenerator hueGenerator = new SimpleHueGenerator();
    private final ColorCategory[] colorCategories;

    public RelationshipTypeColorMapper( final ColorCategory[] values )
    {
        this.colorCategories = values;
    }

    public Color getColor( final RelationshipType type,
            final ColorSetting colorSetting )
    {
        Colors colors = colorMap.get( type );
        if ( colors == null )
        {
            colors = new Colors( hueGenerator.nextHue(), colorCategories );
            colorMap.put( type, colors );
        }
        return colors.getColor( colorSetting );
    }

    public boolean colorExists( final RelationshipType type )
    {
        return colorMap.containsKey( type );
    }

    public Set<RelationshipType> getKeys()
    {
        return colorMap.keySet();
    }
}
