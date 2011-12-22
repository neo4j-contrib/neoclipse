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
 * A QueryParser is able to iterate over SQL code and return the individual queries 
 * acceptable to a JDBC connection.
 * 
 * QueryParsers are not re-usable primarily because the structured comment parser
 * can edit the code and tokens which is generated, therefore removing the possibility
 * of useful caching; they are typically low-cost to allocate a new one and new
 * parsers should be defined with that in mind.
 * 
 * @modified John Spackman
 */
public interface QueryParser extends Iterable<Query> {
	
	/**
	 * Initiates the parsing
	 * @throws ParserException
	 */
	public void parse() throws ParserException;
	
	/**
	 * Used to notify the query parser that the text has been edited and any line numbers
	 * returned from the server need to be adjusted by the specified amount 
	 * @param originalLineNo line number in the original code
	 * @param numLines the number of lines added, can be negative
	 */
	public void addLineNoOffset(int originalLineNo, int numLines);
	
	/**
	 * Used to adjust a line number returned from the server back to an original
	 * source code line number
	 * @param lineNo
	 * @return
	 */
	public int adjustLineNo(int lineNo);
	
	/**
	 * Adds a Named Parameter
	 * @param parameter
	 */
	public void addParameter(NamedParameter parameter);
	
	/**
	 * returns the ExceutionContext for this parser
	 * @return 
	 */
	public ExecutionContext getContext();
	
	/**
	 * returns the number of parsed queries
	 * @return
	 */
	public int numberOfQueries();
}
