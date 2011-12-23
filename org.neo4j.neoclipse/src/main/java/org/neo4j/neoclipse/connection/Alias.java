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
import java.util.HashMap;
import java.util.Map;

import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.neo4j.neoclipse.ApplicationUtils;

/**
 * Our Neo4JConnection, which adds the connection to our GraphDatabaseService
 * object
 * 
 * @author Radhakrishna Kalyan
 * 
 */
public class Alias
{

    /*package*/static final String ALIASES = "aliases";
    /*package*/static final String ALIAS = "alias";
    /*package*/static final String NAME = "name";
    /*package*/static final String URI = "uri";
    /*package*/static final String USER_NAME = "user-name";
    /*package*/static final String PASSWORD = "password";

    private final String name;
    private String uri;
    private String userName;
    private String password;
    private long createdTime;
    private ConnectionMode connectionMode;
    private final Map<String, String> configurationMap = new HashMap<String, String>();

    public Alias( String aliasName, String dbPath, String user, String pass )

    {

        name = aliasName;
        uri = dbPath;
        connectionMode = ConnectionMode.getValue( dbPath );

        if ( connectionMode == ConnectionMode.LOCAL )
        {
            File dir = new File( uri );
            if ( !dir.exists() )
            {
                dir = ApplicationUtils.dirInWorkspace( uri );
                uri = dir.getAbsolutePath();
            }
            if ( !dir.isDirectory() )
            {
                throw new IllegalArgumentException( "The database location is not a directory." );
            }
            if ( !dir.canWrite() )
            {
                throw new IllegalAccessError( "Permission Denied for write to the database location." );
            }
        }

        if ( !ApplicationUtils.isBlank( user ) )
        {
            userName = user;
        }
        if ( !ApplicationUtils.isBlank( user ) )
        {
            password = pass;
        }

        createdTime = System.currentTimeMillis();

    }

    /**
     * Constructs an Alias from XML, previously obtained from describeAsXml()
     * 
     * @param root
     */
    public Alias( Element root )
    {
        name = root.elementText( NAME );
        uri = root.elementText( URI );
        connectionMode = ConnectionMode.getValue( uri );
        String user = root.elementText( USER_NAME );
        String pass = root.elementText( PASSWORD );
        if ( !ApplicationUtils.isBlank( user ) )
        {
            userName = user;
        }
        if ( !ApplicationUtils.isBlank( pass ) )
        {
            password = pass;
        }

        // TODO Need to add Configuration

    }

    public long getCreatedTime()
    {
        return createdTime;
    }

    public String getName()
    {
        return name;
    }

    public String getUri()
    {
        return uri;
    }

    public String getUserName()
    {
        return userName;
    }

    public String getPassword()
    {
        return password;
    }

    public ConnectionMode getConnectionMode()
    {
        return connectionMode;
    }

    /**
     * Describes this alias in XML; the result can be passed to the
     * Alias(Element) constructor to refabricate it
     * 
     * @return
     */
    public Element describeAsXml()
    {
        DefaultElement root = new DefaultElement( ALIAS );
        root.addElement( NAME ).setText( ApplicationUtils.returnEmptyIfBlank( name ) );
        root.addElement( URI ).setText( ApplicationUtils.returnEmptyIfBlank( uri ) );
        root.addElement( USER_NAME ).setText( ApplicationUtils.returnEmptyIfBlank( userName ) );
        root.addElement( PASSWORD ).setText( ApplicationUtils.returnEmptyIfBlank( password ) );
        // TODO add configuration settings
        return root;
    }

    public Map<String, String> getConfigurationMap()
    {
        return configurationMap;
    }

    public void addConfiguration( String key, String value )
    {
        configurationMap.put( key, value );
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
        result = prime * result + ( ( uri == null ) ? 0 : uri.hashCode() );
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj == null )
        {
            return false;
        }
        if ( getClass() != obj.getClass() )
        {
            return false;
        }
        Alias other = (Alias) obj;
        if ( name == null )
        {
            if ( other.name != null )
            {
                return false;
            }
        }
        else if ( !name.equals( other.name ) )
        {
            return false;
        }
        if ( uri == null )
        {
            if ( other.uri != null )
            {
                return false;
            }
        }
        else if ( !uri.equals( other.uri ) )
        {
            return false;
        }
        return true;
    }
}
