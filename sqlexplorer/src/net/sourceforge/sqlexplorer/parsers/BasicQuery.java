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

/**
 * Query encapsulates a command to be executed
 * 
 * @author John Spackman
 *
 */
public class BasicQuery extends AbstractQuery {

	private CharSequence querySql;
	private int lineNo;
	private QueryType queryType;
	
	public BasicQuery(CharSequence querySql, int lineNo) {
		super(querySql, lineNo);
		queryType = QueryType.UNKNOWN;
		String lower = querySql.toString().toLowerCase().trim();
		if (lower.startsWith("select") && !lower.contains("into")) {
			queryType = QueryType.SELECT;
		}
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.sqlexplorer.parsers.Query#getQueryType()
	 */
	public QueryType getQueryType() {
		return queryType;
	}

	/* (non-JavaDoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return Integer.toString(lineNo) + ": " + querySql.toString();
	}
}
