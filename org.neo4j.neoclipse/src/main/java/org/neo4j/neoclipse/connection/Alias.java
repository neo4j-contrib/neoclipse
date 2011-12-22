package org.neo4j.neoclipse.connection;

import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.neo4j.neoclipse.Activator;
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

    /*package*/static final String ALIASES = "aliases";
    /*package*/static final String ALIAS = "alias";
    /*package*/static final String NAME = "name";
    /*package*/static final String URL = "url";
    /*package*/static final String USER_NAME = "user-name";
    /*package*/static final String PASSWORD = "password";

    private final String name;
    private final String neo4JDbLocation; // TODO , rename to URL or URI
    private final String userName;
    private final String password;
    private long createdTime;
    private long lastUsed;

    public Alias( String aliasName, String dbLocation, String user, String pass, GraphDbServiceMode serviceMode )

    {

        name = aliasName;
        neo4JDbLocation = dbLocation;
        createdTime = lastUsed = System.currentTimeMillis();
        userName = user;
        password = pass;

    }

    /**
     * Constructs an Alias from XML, previously obtained from describeAsXml()
     * 
     * @param root
     */
    public Alias( Element root )
    {
        name = root.elementText( NAME );
        neo4JDbLocation = root.elementText( URL );
        userName = root.elementText( USER_NAME );
        password = root.elementText( PASSWORD );

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

    public void remove()
    {
        Activator.getDefault().getAliasManager().removeAlias( getName() );
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
        result = prime * result + ( ( neo4JDbLocation == null ) ? 0 : neo4JDbLocation.hashCode() );
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
        if ( neo4JDbLocation == null )
        {
            if ( other.neo4JDbLocation != null )
            {
                return false;
            }
        }
        else if ( !neo4JDbLocation.equals( other.neo4JDbLocation ) )
        {
            return false;
        }
        return true;
    }

    public String getUserName()
    {
        return userName;
    }

    public String getPassword()
    {
        return password;
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
        root.addElement( NAME ).setText( checkNull( name ) );
        root.addElement( URL ).setText( checkNull( neo4JDbLocation ) );
        root.addElement( USER_NAME ).setText( checkNull( userName ) );
        root.addElement( PASSWORD ).setText( checkNull( password ) );
        return root;
    }

    private String checkNull( String pString )
    {
        return pString == null ? "" : pString;
    }
}
