package org.neo4j.neoclipse.connection;

import org.neo4j.neoclipse.graphdb.GraphDbServiceMode;

/**
 * Our Neo4JConnection, which adds the connection to our GraphDatabaseService
 * object
 * 
 * @author Radhakrishna Kalyan
 * 
 */
public class Alias
{

    private final String name;
    private final String neo4JDbLocation;
    private final long createdTime;
    private long lastUsed;

    public Alias( String aliasName, String dbLocation, GraphDbServiceMode serviceMode )

    {

        name = aliasName;
        neo4JDbLocation = dbLocation;
        createdTime = lastUsed = System.currentTimeMillis();

    }

    /**
     * Returns when this connection was created
     * 
     * @return
     */
    public long getCreatedTime()
    {
        return createdTime;
    }

    /**
     * Returns when this connection was last used
     * 
     * @return
     */
    public long getLastUsed()
    {
        return lastUsed;
    }

    /**
     * Updates the timestamp to say when this connection was last used
     */
    public void updateLastUsed()
    {
        lastUsed = System.currentTimeMillis();
    }

    public String getName()
    {
        return name;
    }

    public String getNeo4JDbLocation()
    {
        return neo4JDbLocation;
    }

}
