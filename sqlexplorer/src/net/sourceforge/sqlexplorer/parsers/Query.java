/*
 * Copyright (C) 2007 SQL Explorer Development Team
 * http://sourceforge.net/projects/eclipsesql
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
package net.sourceforge.sqlexplorer.parsers;

import java.util.Map;



/**
 * The QueryParser produces a series of Query objects; they are expected to be fairly light-weight
 * and to use the CharSequence to point into the original buffer rather than take a copy of the
 * entire string. 
 *  
 * @author John Spackman
 */
public interface Query {
	
	// The possible types of query
	public enum QueryType {
		UNKNOWN, SELECT, DDL, DML, CODE
	}
	
	// Returns the type of the query
	public QueryType getQueryType();

	/**
	 * @return Returns the SQL to be executed
	 */
	public CharSequence getQuerySql();

	/**
	 * @return Returns the line number of the original which the query started on 
	 */
	public int getLineNo();

	/**
	 * Returns a map of NamedParameters index by their name
	 * @return
	 */
	public Map<String, NamedParameter> getNamedParameters();
	
	/**
	 * strip comments from the contained query SQL
	 * @throws ParserException
	 */
	public void stripComments() throws ParserException;
}