package net.sourceforge.sqlexplorer.history;

import java.text.DateFormat;
import java.util.Date;

import net.sourceforge.sqlexplorer.dbproduct.Alias;
import net.sourceforge.sqlexplorer.dbproduct.User;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.util.TextUtil;

import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

/**
 * SQLHistoryElement represents a single entry in the SQLHistoryView.
 */
public class SQLHistoryElement {

	public static final String ELEMENT = "element";
    private static final String ALIAS = "alias";
    private static final String EXECUTION_COUNT = "execution-count";
    private static final String LAST_EXECUTION_TIME = "last-execution-time";
    private static final String USER_NAME = "user-name";
    
    private int _executionCount = 1;

    private String _formattedTime;

    private String _rawSQLString;

    private String _searchableString;

    // User for the connection, plus the username and alias name; the user is not stored because
    //	the user can be deleted without deleting associated history
    private String userName;
    private String aliasName;

    private String _singleLineText;

    private long _time;

//    private static SimpleDateFormat _dateFormatter = new SimpleDateFormat(
//            SQLExplorerPlugin.getDefault().getPluginPreferences().getString(IConstants.DATASETRESULT_DATE_FORMAT));

    private static DateFormat _dateFormatter = DateFormat.getDateTimeInstance();

    public SQLHistoryElement(String rawSQLString, User user) {
        _rawSQLString = rawSQLString;
        setUser(user);
        _time = System.currentTimeMillis();
        initialize();
    }

    public SQLHistoryElement(String rawSQLString, User user, String time, String executions) {
        _rawSQLString = rawSQLString;
        setUser(user);

        if (time != null && time.length() != 0) {
            _time = Long.parseLong(time);
        } else {
            _time = System.currentTimeMillis();
        }

        if (executions != null && executions.length() != 0) {
            _executionCount = Integer.parseInt(executions);
        } else {
            _executionCount = 1;
        }

        initialize();
    }

    /**
     * Constructor; loads from the specified Element, which was previously generated
     * by a call to describeAsXml()
     * @param root
     */
    public SQLHistoryElement(Element root) {
    	_executionCount = Integer.parseInt(root.attributeValue(EXECUTION_COUNT));
    	_time = Long.parseLong(root.attributeValue(LAST_EXECUTION_TIME));
    	aliasName = root.attributeValue(ALIAS);
    	userName = root.attributeValue(USER_NAME);
    	_rawSQLString = root.getTextTrim();
    	initialize();
    }
    
    /**
     * Returns the User for this entry; can return null if the user is no longer configured
     * @return
     */
    public User getUser() {
    	Alias alias = getAlias();
    	if (alias != null)
    		return alias.getUser(userName);
    	return null;
    }
    
    /**
     * Returns the Alias for this history element; can return null if the alias no longer exists
     * @return
     */
    public Alias getAlias() {
    	return SQLExplorerPlugin.getDefault().getAliasManager().getAlias(aliasName);
    }

    /**
     * Creates an Element which can be used to reconstruct this instance at a later date
     * @return
     */
    public Element describeAsXml() {
    	Element root = new DefaultElement(ELEMENT);
    	root.addAttribute(EXECUTION_COUNT, Integer.toString(_executionCount));
    	root.addAttribute(LAST_EXECUTION_TIME, Long.toString(_time));
    	root.addAttribute(ALIAS, aliasName);
    	root.addAttribute(USER_NAME, userName);
    	root.setText(_rawSQLString);
    	return root;
    }


    /**
     * Check if the current element matches a given sql string
     * 
     * @param rawSQL original sql statement to compare too.
     * @return true rawSQL matches this element
     */
    public boolean equals(String rawSQL) {
    	int i1 = 0, i2 = 0; 
    	while (true) {
       		char c1 = i1 < rawSQL.length() ? rawSQL.charAt(i1) : 0;
    		if (Character.isWhitespace(c1)) {
    			i1++;
    			continue;
    		}
       		char c2 = i2 < _singleLineText.length() ? _singleLineText.charAt(i2) : 0;
    		if (Character.isWhitespace(c2)) {
    			i2++;
    			continue;
    		}
    		if (c1 == 0 && c2 == 0)
    			return true;
    		if (c1 != c2)
    			return false;
    		i1++;
    		i2++;
    	}
    	// This was really, really slow for big histories (or even just for big scripts)
        //return TextUtil.removeLineBreaks(rawSQL).equals(_singleLineText);
    }


    /**
     * @return number of times this statement was executed
     */
    public int getExecutionCount() {

        return _executionCount;
    }


    public String getFormattedTime() {

        return _formattedTime;
    }


    /**
     * @return unformatted sql string
     */
    public String getRawSQLString() {

        return _rawSQLString;
    }


    public String getSearchableString() {

        return _searchableString;
    }

    
    public String getSessionDescription() {
    	return aliasName + '/' + userName;
    }


	/**
     * Return all text without newline separators.
     */
    public String getSingleLineText() {

        return _singleLineText;
    }


    public long getTime() {

        return _time;
    }


    /**
     * increase execution count by 1 and reset the timestamp to the current
     * time.
     */
    public void increaseExecutionCount() {

        _executionCount++;
        _time = System.currentTimeMillis();
        initialize();
    }


    /**
     * initialize our search string immediately, this allows for very fast
     * searching in the history view
     */
    private void initialize() {

        _formattedTime = _dateFormatter.format(new Date(_time));
        _searchableString = (_rawSQLString + " " + aliasName + "/" + userName + " " + _formattedTime).toLowerCase();
        _singleLineText = TextUtil.removeLineBreaks(_rawSQLString);
    }


    public void setUser(User user) {
        this.userName = user.getUserName();
        this.aliasName = user.getAlias().getName();
        initialize();
	}
}
