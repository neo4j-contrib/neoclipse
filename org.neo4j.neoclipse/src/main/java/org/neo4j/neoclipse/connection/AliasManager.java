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
package org.neo4j.neoclipse.connection;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.event.NeoclipseEventListener;
import org.neo4j.neoclipse.event.NeoclipseListenerList;
import org.neo4j.neoclipse.graphdb.GraphDbServiceManager;
import org.neo4j.neoclipse.util.ApplicationUtil;
import org.neo4j.neoclipse.util.XMLUtils;

/**
 * Maintains the list of Neo4JConnection Alias
 * 
 * @author Radhakrishna Kalyan
 * 
 */
public class AliasManager
{

    private static final String ALIAS_FILE_NAME = "NeoDbAliases.xml";
    private final Set<Alias> aliases = new HashSet<Alias>();
    private final NeoclipseListenerList connectionListeners = new NeoclipseListenerList();

    public void loadAliases()
    {
        aliases.clear();


        File aliasSettings = new File( ApplicationUtil.NEOCLIPSE_SETTINGS_DIR, ALIAS_FILE_NAME );
        Element root = XMLUtils.readRoot( aliasSettings );
        if ( root != null )
        {
            List<Element> elements = root.elements( Alias.ALIAS );
            if ( root.getName().equals( Alias.ALIASES ) )
            {
                for ( Element aliasElement : elements )
                {
                    addAlias( new Alias( aliasElement ) );
                }
            }

        }
    }

    /**
     * Saves all the Aliases to the users preferences
     * 
     */
    public void saveAliases()
    {
        DefaultElement root = new DefaultElement( Alias.ALIASES );
        for ( Alias alias : aliases )
        {
            root.add( alias.describeAsXml() );
        }
        File aliasSettings = new File( ApplicationUtil.NEOCLIPSE_SETTINGS_DIR, ALIAS_FILE_NAME );
        XMLUtils.save( root, aliasSettings );
    }

    /**
     * Add Alias to the set
     * 
     */
    public void addAlias( Alias alias )
    {
        aliases.add( alias );
        notifyListners();
    }

    /**
     * Remove Alias from the set
     * 
     */
    public void removeAlias( Alias alias )
    {
        GraphDbServiceManager graphDbServiceManager = Activator.getDefault().getGraphDbServiceManager();
        if ( graphDbServiceManager.isRunning() && graphDbServiceManager.getCurrentAlias().equals( alias ) )
        {
            throw new IllegalStateException( "Please stop the service before deleting." );
        }

        aliases.remove( alias );
        notifyListners();
    }

    public Collection<Alias> getAliases()
    {
        return aliases;
    }

    public void registerConnetionListener( NeoclipseEventListener listener )
    {
        connectionListeners.add( listener );
    }

    public void notifyListners()
    {
        connectionListeners.notifyListeners();
    }

}
