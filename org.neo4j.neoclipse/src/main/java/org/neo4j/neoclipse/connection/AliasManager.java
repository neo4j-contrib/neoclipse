package org.neo4j.neoclipse.connection;

import java.util.Collection;
import java.util.LinkedList;
import java.util.TreeMap;

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

    /**
     * Adds an Neo4JConnection
     * 
     * @param Alias
     */
    public void addAlias( Alias connection ) throws Exception
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
