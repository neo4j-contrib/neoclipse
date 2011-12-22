package net.sourceforge.sqlexplorer.dbproduct;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import net.sourceforge.sqlexplorer.ExplorerException;

import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

/**
 * Manages a JDBC Driver
 * 
 * @author John Spackman
 */
public class ManagedDriver implements Comparable<ManagedDriver>
{

    private String id;
    private String name;
    private String driverClassName;
    private String url;
    private LinkedList<String> jars = new LinkedList<String>();

    // private Driver jdbcDriver;

    public ManagedDriver( String id )
    {
        this.id = id;
    }

    /**
     * Constructs a new ManagedDriver from a previously serialised version
     * 
     * @param root result of previous call to describeAsXml()
     */
    public ManagedDriver( Element root )
    {
        super();
        id = root.attributeValue( DriverManager.ID );
        name = root.elementText( DriverManager.NAME );
        driverClassName = root.elementText( DriverManager.DRIVER_CLASS );
        url = root.elementText( DriverManager.URL );
        Element jarsElem = root.element( DriverManager.JARS );
        List<Element> list = jarsElem.elements();
        if ( list != null )
        {
            for ( Element jarElem : list )
            {
                String jar = jarElem.getTextTrim();
                if ( jar != null )
                {
                    jars.add( Locations.expand( jar ) );
                }
            }
        }
    }

    /**
     * Describes this driver in XML; the result can be passed to the constructor
     * to refabricate it late
     * 
     * @return
     */
    public Element describeAsXml()
    {
        Element root = new DefaultElement( DriverManager.DRIVER );
        root.addAttribute( DriverManager.ID, id );
        root.addElement( DriverManager.NAME ).setText( name );
        if ( driverClassName != null )
        {
            root.addElement( DriverManager.DRIVER_CLASS ).setText( driverClassName );
        }
        root.addElement( DriverManager.URL ).setText( url );
        Element jarsElem = root.addElement( DriverManager.JARS );
        for ( String jar : jars )
        {
            jarsElem.addElement( DriverManager.JAR ).setText( Locations.insert( jar ) );
        }
        return root;
    }

    // /**
    // * Loads the Driver class
    // * @throws ExplorerException
    // * @throws SQLException
    // */
    // public synchronized void registerSQLDriver() throws
    // ClassNotFoundException {
    // if (driverClassName == null || driverClassName.length() == 0)
    // return;
    // unregisterSQLDriver();
    // try {
    // ClassLoader loader = new
    // SQLDriverClassLoader(getClass().getClassLoader(), this);
    // Class<?> driverCls = loader.loadClass(getDriverClassName());
    // // jdbcDriver = (Driver)driverCls.newInstance();
    // } catch(UnsupportedClassVersionError e) {
    // throw new ClassNotFoundException(e.getMessage(), e);
    // } catch(MalformedURLException e) {
    // throw new ClassNotFoundException(e.getMessage(), e);
    // }
    // }

    // /**
    // * Unloads the class
    // *
    // */
    // public synchronized void unregisterSQLDriver() {
    // jdbcDriver = null;
    // }

    /**
     * Establishes a JDBC connection
     * 
     * @param user
     * @return
     * @throws ExplorerException
     * @throws SQLException
     */
    public SQLConnection getConnection( User user ) throws SQLException
    {
        Properties props = new Properties();
        if ( user.hasCredentials() )
        {
            if ( user.getUserName() != null )
            {
                props.put( "user", user.getUserName() );
            }
            if ( user.getPassword() != null )
            {
                props.put( "password", user.getPassword() );
            }
        }

        // TODO return the instance of NEO4jConnection
        return null;
    }

    // public boolean isDriverClassLoaded()
    // {
    // return jdbcDriver != null;
    // }

    public String getDriverClassName()
    {
        return driverClassName;
    }

    public String getId()
    {
        return id;
    }

    public LinkedList<String> getJars()
    {
        return jars;
    }

    // public Driver getJdbcDriver()
    // {
    // return jdbcDriver;
    // }

    public String getName()
    {
        return name;
    }

    public String getUrl()
    {
        return url;
    }

    public void setJars( LinkedList<String> jars )
    {
        this.jars = jars;
    }

    public void setJars( String[] jars )
    {
        this.jars.clear();
        for ( String jar : jars )
        {
            this.jars.add( jar );
        }
    }

    // public void setJdbcDriver( Driver jdbcDriver )
    // {
    // this.jdbcDriver = jdbcDriver;
    // }

    public void setName( String name )
    {
        this.name = name;
    }

    public void setUrl( String url )
    {
        this.url = url;
    }

    public void setDriverClassName( String driverClassName )
    {
        this.driverClassName = driverClassName;
    }

    /*
    	public DatabaseProduct getDatabaseProduct() {
    		return DatabaseProductFactory.getInstance(this);
    	}
    */
    public int compareTo( ManagedDriver that )
    {
        return name.compareTo( that.name );
    }

}
