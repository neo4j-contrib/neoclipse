package net.sourceforge.sqlexplorer.dbproduct;

import java.sql.SQLException;

import net.sourceforge.sqlexplorer.ExplorerException;
import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.dbdetail.DetailTabManager;
import net.sourceforge.sqlexplorer.dbstructure.DatabaseModel;
import net.sourceforge.sqlexplorer.dbstructure.nodes.DatabaseNode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.sessiontree.model.utility.Dictionary;
import net.sourceforge.sqlexplorer.sessiontree.model.utility.DictionaryLoader;
import net.sourceforge.squirrel_sql.fw.sql.SQLDatabaseMetaData;

/**
 * Specialisation of Session which adds meta data; every user has at most one
 * of these, loaded for the first time on demand (which is pretty much always
 * because it's used for detailing catalogs in the editor and for navigating
 * the database structure view)
 * 
 * @author John Spackman
 */
public class MetaDataSession extends Session {
	
	private boolean initialised;
	
	private String databaseProductName;

	// Cached set of Catalogs for this connection
	private String[] catalogs; 

    // Whether content assist is enabled
    boolean _assistanceEnabled;

    // The dictionary used for content assist
    private Dictionary dictionary;
    
    // Database Model
    private DatabaseModel dbModel;
    
	public MetaDataSession(User user) throws SQLException {
		super(user);
		//setKeepConnection(true);
	}
	
	/**
	 * Initialises the metadata, but only if the meta data has not already been collected
	 */
	private synchronized void initialise() throws SQLException {
		if (initialised)
			return;
		initialised = true;
		
		
		SQLConnection connection = null;
		try {
			connection = grabConnection();
			SQLDatabaseMetaData metaData = connection.getSQLMetaData();
			if (metaData.supportsCatalogs())
			{
				try
				{
					catalogs = metaData.getCatalogs();
				}
				catch(Throwable ex)
				{
					SQLExplorerPlugin.error("Error reading catalogs", ex);
					String catalog = connection.getCatalog();
					if(catalog != null)
					{
						catalogs = new String[]{catalog};
					}
				}
			}
			databaseProductName = metaData.getDatabaseProductName();
			dbModel = new DatabaseModel(this);
		}finally {
			if (connection != null)
				releaseConnection(connection);
		}
		_assistanceEnabled = SQLExplorerPlugin.getBooleanPref(IConstants.SQL_ASSIST);
        if (_assistanceEnabled) {
            // schedule job to load dictionary for this session
        	dictionary = new Dictionary();
            DictionaryLoader dictionaryLoader = new DictionaryLoader(this);
            dictionaryLoader.schedule(500);
        }
        
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.sqlexplorer.dbproduct.Session#internalSetConnection(net.sourceforge.sqlexplorer.dbproduct.SQLConnection)
	 */
	@Override
	protected void internalSetConnection(SQLConnection newConnection) throws SQLException {
		super.internalSetConnection(newConnection);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.sqlexplorer.dbproduct.Session#close()
	 */
	@Override
	public synchronized void close() {
		super.close();
		
        // store dictionary
		if (dictionary != null)
			dictionary.store();
        
        // clear detail tab cache
        DetailTabManager.clearCacheForSession(this);
	}

    /**
     * Gets (and caches) the meta data for this connection
     * @return
     * @throws ExplorerException
     */
    public synchronized SQLDatabaseMetaData getMetaData() throws SQLException {
    	if (getConnection() != null)
    		return getConnection().getSQLMetaData();
    	initialise();
		SQLConnection connection = null;
		try {
			connection = grabConnection();
			return connection.getSQLMetaData();
		}finally {
			releaseConnection(connection);
		}
    }
    
    /**
     * Returns the catalogs supported by the underlying database, or null
     * if catalogs are not supported
     * @return
     * @throws SQLException
     */
    public String[] getCatalogs() {
    	if (catalogs != null)
    		return catalogs;
    	try {
    		initialise();
    	}catch(SQLException e) {
    		SQLExplorerPlugin.error(e);
    		return null;
    	}
   		return catalogs;
    }

    /**
     * Returns the root DatabaseNode for the DatabaseStructureView
     * @return
     */
    public DatabaseNode getRoot() {
    	try {
    		initialise();
    	}catch(SQLException e) {
    		SQLExplorerPlugin.error(e);
    		return null;
    	}
    	if(dbModel == null)
    	{
    		SQLExplorerPlugin.error("Session not initialized");
    		return null;
    	}
    	return dbModel.getRoot();
    }
    
    /**
     * Returns the MetaData dictionary for type ahead etc
     * @return
     */
    public Dictionary getDictionary() {
    	try {
    		initialise();
    	}catch(SQLException e) {
    		SQLExplorerPlugin.error(e);
    		return null;
    	}
        return dictionary;
    }

	/**
	 * @return the databaseProductName
	 */
	public String getDatabaseProductName() throws SQLException {
		if (databaseProductName != null)
			return databaseProductName;
       	initialise();
		return databaseProductName;
	}
}
