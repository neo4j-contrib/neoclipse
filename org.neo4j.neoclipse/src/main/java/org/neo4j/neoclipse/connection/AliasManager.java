package org.neo4j.neoclipse.connection;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.neo4j.neoclipse.ApplicationFiles;
import org.neo4j.neoclipse.XMLUtils;

/**
 * Maintains the list of Neo4JConnection Alias
 * 
 * @author Radhakrishna Kalyan
 * 
 */
public class AliasManager implements ConnectionListener
{

    // List of Neo4JConnectiones, indexed by Neo4JConnection name
    private TreeMap<String, Alias> aliases = new TreeMap<String, Alias>();

    // Connection Listeners
    private LinkedList<ConnectionListener> connectionListeners = new LinkedList<ConnectionListener>();

    public void loadAliases()
    {
        aliases.clear();

        Element root = XMLUtils.readRoot( new File( ApplicationFiles.USER_ALIAS_FILE_NAME ) );
        if ( root != null )
        {
            if ( root.getName().equals( "aliases" ) )
            {
                root = convertToV350( root );
            }
            List<Element> list = root.elements( Alias.ALIAS );
            if ( list != null )
            {
                for ( Element elem : list )
                {
                    addAlias( new Alias( elem ) );
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
        for ( Alias alias : aliases.values() )
        {
            root.add( alias.describeAsXml() );
        }

        XMLUtils.save( root, new File( ApplicationFiles.USER_ALIAS_FILE_NAME ) );
    }

    /**
     * Upgrades a v3 definition (java beans style) to v3.5.0beta2 and onwards
     * 
     * @param beans
     * @return
     */
    protected Element convertToV350( Element beans )
    {
        Element result = new DefaultElement( Alias.ALIASES );

        for ( Element bean : beans.elements( "alias" ) )
        {
            Element alias = result.addElement( Alias.ALIAS );
            alias.addElement( Alias.NAME ).setText( checkNull( bean.elementText( "name" ) ) );
            alias.addElement( Alias.URL ).setText( checkNull( bean.elementText( "url" ) ) );
            alias.addElement( Alias.USER_NAME ).setText( checkNull( bean.elementText( "userName" ) ) );
            alias.addElement( Alias.PASSWORD ).setText( checkNull( bean.elementText( "password" ) ) );
        }

        return result;
    }

    private String checkNull( String pString )
    {
        return pString == null ? "" : pString;
    }

    /**
     * Adds an Neo4JConnection
     * 
     * @param Alias
     */
    public void addAlias( Alias connection )
    {
        aliases.put( connection.getName(), connection );
    }

    /**
     * Removes an Neo4JConnection with a given name
     * 
     * @param aliasName
     */
    public void removeAlias( String aliasName )
    {
        Alias connection = aliases.remove( aliasName );
        if ( connection != null )
        {
            // connection.closeConnection();
        }
    }

    /**
     * Locates an Neo4JConnection by name
     * 
     * @param aliasName
     * @return
     */
    public Alias getAlias( String aliasName )
    {
        return aliases.get( aliasName );
    }

    /**
     * Provides a list of all Neo4JConnectiones
     * 
     * @return
     */
    public Collection<Alias> getAliases()
    {
        return aliases.values();
    }

    /**
     * Returns true if the Neo4JConnection is in our list
     * 
     * @param alias
     * @return
     */
    public boolean contains( Alias alias )
    {
        return aliases.values().contains( alias );
    }

    /**
     * Closes all connections in all Neo4JConnectiones; note that
     * ConnectionListeners are NOT invoked
     * 
     */
    public void closeAllConnections() throws Exception
    {
        for ( Alias connection : aliases.values() )
        {
            // connection.closeConnection();
        }
    }

    /**
     * Adds a listener for the connections
     * 
     * @param listener
     */
    public void addListener( ConnectionListener listener )
    {
        connectionListeners.add( listener );
    }

    /**
     * Removes a listener
     * 
     * @param listener
     */
    public void removeListener( ConnectionListener listener )
    {
        connectionListeners.remove( listener );
    }

    /**
     * Called to notify that the list of connections has changed; passes this
     * onto the listeners
     */
    @Override
    public void modelChanged()
    {
        for ( ConnectionListener listener : connectionListeners )
        {
            listener.modelChanged();
        }
    }

    public void addConnection( Alias connection )
    {
        aliases.put( connection.getName(), connection );
    }
}
