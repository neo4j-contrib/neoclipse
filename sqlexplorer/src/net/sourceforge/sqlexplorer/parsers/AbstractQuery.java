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

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.sqlexplorer.parsers.Tokenizer.Token;
import net.sourceforge.sqlexplorer.parsers.Tokenizer.TokenType;

public abstract class AbstractQuery implements Query {

	protected CharSequence stripComments(CharSequence pQuery) throws ParserException
	{
		if(pQuery == null)
		{
			return null;
		}
		StringBuffer query = new StringBuffer(pQuery);
		Tokenizer tokenizer = new Tokenizer(query);
		StringBuilder result = new StringBuilder();
		Token token = null;
		int start = 0;
		int end = 0;
		while ((token = tokenizer.nextToken()) != null) {
			if(TokenType.ML_COMMENT == token.getTokenType() || TokenType.EOL_COMMENT == token.getTokenType())
			{
				if(end > start)
				{
					result.append(query.substring(start, end));
				}
				start = token.getEnd();
				continue;
			}
			end = token.getEnd();
		}
		if(end > start)
		{
			result.append(query.substring(start, end));
		}
		return result.toString().trim();
	}
	
	// Named parameters in scope
	private HashMap<String, NamedParameter> parameters;
	private CharSequence querySql;
	private int lineNo;
	
	public AbstractQuery(CharSequence querySql, int lineNo) {
		this.querySql = querySql;
		this.lineNo = lineNo;
	}

	/*package*/ void setParameters(HashMap<String, NamedParameter> parameters) {
		this.parameters = parameters;
	}

	public Map<String, NamedParameter> getNamedParameters() {
		return parameters;
	}

	/* (non-JavaDoc)
	 * @see net.sourceforge.sqlexplorer.tokenizer.IQuery#getLineNo()
	 */
	public int getLineNo() {
		return lineNo;
	}

	/* (non-JavaDoc)
	 * @see net.sourceforge.sqlexplorer.tokenizer.IQuery#getQuerySql()
	 */
	public CharSequence getQuerySql() {
		return querySql;
	}

	public void stripComments() throws ParserException
	{
		this.querySql = stripComments(this.querySql);
	}

	/* (non-JavaDoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return Integer.toString(lineNo) + ": " + querySql.toString();
	}
	
}
