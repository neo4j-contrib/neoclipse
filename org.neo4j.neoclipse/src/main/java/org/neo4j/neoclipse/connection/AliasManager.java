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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.event.NeoclipseEventListener;
import org.neo4j.neoclipse.event.NeoclipseListenerList;
import org.neo4j.neoclipse.graphdb.GraphDbServiceManager;
import org.neo4j.neoclipse.util.ApplicationUtil;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

/**
 * Maintains the list of Neo4JConnection Alias
 * 
 * @author Radhakrishna Kalyan
 * 
 */
public class AliasManager
{

    private static final String ALIAS_FILE_NAME = "NeoDbAliases.json";
    private final Set<Alias> aliases = new HashSet<Alias>();
    private final NeoclipseListenerList connectionListeners = new NeoclipseListenerList();

    public void loadAliases() throws FileNotFoundException
    {
        aliases.clear();


        File aliasSettings = new File( ApplicationUtil.NEOCLIPSE_SETTINGS_DIR, ALIAS_FILE_NAME );
        if (!aliasSettings.exists()) return;
        Object aliases = new Gson().fromJson( new FileReader(aliasSettings), Object.class );
        if (aliases instanceof Collection) {
            for ( Map alias : (Collection<Map>)aliases )
            {
                addAlias(new Alias(alias));
            }
        }
        if (aliases instanceof Map) {
            addAlias(new Alias((Map<String, Object>) aliases));
        }
    }

    /**
     * Saves all the Aliases to the users preferences
     * @throws IOException 
     * @throws JsonIOException 
     * 
     */
    public void saveAliases() throws JsonIOException, IOException
    {
        List<Map> data = new ArrayList<>();
        for ( Alias alias : aliases )
        {
            data.add( alias.describeAsJson() );
        }
        File aliasSettings = new File( ApplicationUtil.NEOCLIPSE_SETTINGS_DIR, ALIAS_FILE_NAME );
        new Gson().toJson( data, new FileWriter( aliasSettings ));
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
