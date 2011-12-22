/*
 * Copyright (C) SQL Explorer Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sourceforge.sqlexplorer.history;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.sqlexplorer.ApplicationFiles;
import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.XMLUtils;
import net.sourceforge.sqlexplorer.dbproduct.Alias;
import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;

import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * SQL History contains all statements that have been executed and is
 * responsible for making these persistent.
 */
public class SQLHistory {

    private static final String HISTORY = "history";
    
    private List<SQLHistoryElement> _filteredHistory = new ArrayList<SQLHistoryElement>();

    private List<SQLHistoryElement> _history = new ArrayList<SQLHistoryElement>();

    private ListenerList _listeners = new ListenerList();

    private String _qry = null;

    private SQLHistorySorter _sorter = new SQLHistorySorter();

    private static final String EXECUTION_HINT_MARKER = "#EH#";

    private static final String NEWLINE_REPLACEMENT = "#LF#";

    private static final String NEWLINE_SEPARATOR = System.getProperty("line.separator");

    private static final String TAB_REPLACEMENT = "#T#";

    private static final String SESSION_HINT_MARKER = "#SH#";

    private static final String TIME_HINT_MARKER = "#TH#";
    
    private int _autoSaveAfterCount = SQLExplorerPlugin.getIntPref(IConstants.HISTORY_AUTOSAVE_AFTER);
    private int _maxEntries = SQLExplorerPlugin.getIntPref(IConstants.HISTORY_MAX_ENTRIES);
    
    private int _queriesAdded = 0;
    
    /**
     * Default constructor. Initializes history with statements from file.
     */
    public SQLHistory() {

        // load all history from file
        loadFromFile();
        _qry = null;
        SQLExplorerPlugin.getDefault().getPreferenceStore()
		.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty() == IConstants.HISTORY_AUTOSAVE_AFTER) {
					_autoSaveAfterCount = SQLExplorerPlugin.getIntPref(IConstants.HISTORY_AUTOSAVE_AFTER);
				}
				else if (event.getProperty() == IConstants.HISTORY_MAX_ENTRIES) {
				    _maxEntries = SQLExplorerPlugin.getIntPref(IConstants.HISTORY_MAX_ENTRIES);
				    checkMaxEntries();
				    refreshHistoryView();
				}
			}
		});
        
    }


    /**
     * Add a listener to the view, so we can properly refresh it if the sql
     * history has changed.
     * 
     * @param listener
     */
    public void addListener(SQLHistoryChangedListener listener) {

        _listeners.add(listener);
    }


    /**
     * Add a query string to the sql history. New queries are added to the start
     * of the list, so that the most recent entry is always located on the top
     * of the history list
     * 
     * @param newSql sql query string
     */
    public void addSQL(String rawSqlString, Session session) {

        if (rawSqlString == null
            || rawSqlString.equalsIgnoreCase("commit")
            || rawSqlString.trim().length() == 0) {
            return;
        }

        for (int i = 0; i < _history.size(); i++) {
            SQLHistoryElement el = (SQLHistoryElement) _history.get(i);
            if (el.equals(rawSqlString) && el.getUser().equals(session.getUser())) {
                _history.remove(i);
                el.setUser(session.getUser());
                el.increaseExecutionCount();
                _history.add(0, el);
                refreshHistoryView();
                return;
            }
        }
        _history.add(0, new SQLHistoryElement(rawSqlString, session.getUser()));
        
        checkMaxEntries();
        
        refreshHistoryView();
        
        // check if we need to save the history
        _queriesAdded++;
        checkAutoSave();
    }

    
    /**
     * Save the history if a number of statements have been executed.
     */
    private void checkAutoSave() {
                
        if (_autoSaveAfterCount > 0 && _queriesAdded >= _autoSaveAfterCount) {            
            _queriesAdded = 0;
            save();
        }
        
        
    }
    
    /**
     * limit number of entries to configured size
     */
    private void checkMaxEntries() {
                
        if (_maxEntries > 0 ) {
        	while(_history.size() > _maxEntries)
        	{
        		_history.remove(_history.size() - 1);
        	}
        }
        
        
    }

    /**
     * Clear all elements from SQL History
     */
    public void clear() {

        _history.clear();
        refreshHistoryView();
    }


    /**
     * @return number of entries available under current filtering options
     */
    public int getEntryCount() {

        if (_qry == null) {
            return _history.size();
        } else {
            return _filteredHistory.size();
        }
    }

    private void loadFromFile() {
    	if (new File(ApplicationFiles.SQLHISTORY_FILE_NAME_V300).exists())
        	loadFromFileV300();
        else if (new File(ApplicationFiles.SQLHISTORY_FILE_NAME_V350).exists())
        	loadFromFileV350();
    }
    
    private void loadFromFileV350() {

    	Element root = XMLUtils.readRoot(new File(ApplicationFiles.SQLHISTORY_FILE_NAME_V350));
    	if(root != null) {
	        _history.clear();
	        _filteredHistory.clear();
	        for (Element elem : root.elements(SQLHistoryElement.ELEMENT)) {
	        	_history.add(new SQLHistoryElement(elem));
	        	if (_maxEntries > 0 && _history.size() >= _maxEntries)
	        		break;
	        }
    	}
    }

    /**
     * Load the sql history from previous sessions.
     */
    private void loadFromFileV300() {

        try {

            File file = new File(ApplicationFiles.SQLHISTORY_FILE_NAME_V300);

            if (!file.exists()) {
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

            String currentLine = reader.readLine();
            while (currentLine != null) {
                if (currentLine.trim().length() != 0) {

                    String sessionHint = null;
                    String query = null;
                    String time = null;
                    String executions = null;

                    int pos = currentLine.indexOf(SESSION_HINT_MARKER);
                    if (pos != -1) {
                        // split line in session and query

                        sessionHint = currentLine.substring(0, pos);
                        currentLine = currentLine.substring(pos + SESSION_HINT_MARKER.length());

                        int posT = currentLine.indexOf(TIME_HINT_MARKER);
                        if (posT != -1) {

                            // split line in session and query
                            query = currentLine.substring(0, posT);
                            currentLine = currentLine.substring(posT + TIME_HINT_MARKER.length());

                            int posE = currentLine.indexOf(EXECUTION_HINT_MARKER);
                            time = currentLine.substring(0, posE);
                            executions = currentLine.substring(posE + EXECUTION_HINT_MARKER.length());

                        } else {

                            query = currentLine;
                        }

                        // clean up query
                        query = query.replaceAll(NEWLINE_REPLACEMENT, NEWLINE_SEPARATOR);
                        query = query.replaceAll(TAB_REPLACEMENT, " ");
                    }

                    if (query != null && query.trim().length() != 0) {
                    	Alias alias = SQLExplorerPlugin.getDefault().getAliasManager().getAlias(sessionHint);
                        _history.add(new SQLHistoryElement(query, alias.getDefaultUser(), time, executions));
        	        	if (_maxEntries > 0 && _history.size() >= _maxEntries)
        	        		break;
                    }

                }
                currentLine = reader.readLine();
            }

            reader.close();

        } catch (Exception e) {
            SQLExplorerPlugin.error("Couldn't load sql history.", e);
        }

    }


    /**
     * Update the view to show changed history
     */
    public void refreshHistoryView() {

        filter();
        
        Object[] ls = _listeners.getListeners();
        for (int i = 0; i < ls.length; ++i) {
            try {
                ((SQLHistoryChangedListener) ls[i]).changed();
            } catch (Throwable e) {
            }

        }
    }


    /**
     * Remove an entry from the history.
     * 
     * @param element SQLHistoryElement
     */
    public void remove(SQLHistoryElement element) {

        _history.remove(element);
        refreshHistoryView();
    }


    /**
     * Remove the listener to the view.
     * 
     * @param listener
     */
    public void removeListener(SQLHistoryChangedListener listener) {

        _listeners.remove(listener);
    }


    /**
     * Save all the used queries into a file, so that we can reuse them next
     * time.
     */
    public void save() {

    	Element root = new DefaultElement(HISTORY);
    	for (SQLHistoryElement elem : _history)
    		root.add(elem.describeAsXml());
    	XMLUtils.save(root, new File(ApplicationFiles.SQLHISTORY_FILE_NAME_V350));

    	// Get rid of old versions
    	new File(ApplicationFiles.SQLHISTORY_FILE_NAME_V300).delete();

    }


    /**
     * Restrict displayed history by given filter string
     * 
     * @param filter string
     */
    public int setQryString(String qry) {

        _qry = qry.trim().toLowerCase();
        if (_qry != null && _qry.trim().length() == 0) {
            _qry = null;
        }
        
        refreshHistoryView();

        if (_qry == null) {
            return _history.size();
        } else {
            return _filteredHistory.size();
        }
    }

    
    /**
     * Filter based on query string
     */
    private void filter() {

        if (_qry == null || _qry.trim().length() == 0) {
            _qry = null;
            return;
        }
        
        _filteredHistory = new ArrayList<SQLHistoryElement>();
        String[] keyword = _qry.split(" ");

        for(SQLHistoryElement el :  _history) {

            boolean include = true;

            for (int i = 0; i < keyword.length; i++) {

                // search SQL, session and dates
                if ((el.getSearchableString().indexOf(keyword[i]) == -1)) {
                    include = false;
                    break;
                }

            }

            if (include) {
                _filteredHistory.add(el);
            }
        }
    }

    /**
     * Change sorting.
     * 
     * @param column index
     * @param direction SWT.UP, SWT.DOWN, SWT.NONE
     */
    public void sort(int column, int direction) {

        _sorter.setSortColumn(column, direction);

        Collections.sort(_history, _sorter);
        Collections.sort(_filteredHistory, _sorter);

    }


    /**
     * @return sorted array of filtered history elements
     */
    public Object[] toArray() {

        if (_qry == null) {
            return _history.toArray();
        } else {
            return _filteredHistory.toArray();
        }

    }

}
