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
package net.sourceforge.sqlexplorer.parsers.scp;

import java.util.LinkedList;

import net.sourceforge.sqlexplorer.parsers.NamedParameter;
import net.sourceforge.sqlexplorer.parsers.ParserException;
import net.sourceforge.sqlexplorer.parsers.Tokenizer;
import net.sourceforge.sqlexplorer.parsers.Tokenizer.Token;
import net.sourceforge.sqlexplorer.parsers.Tokenizer.TokenType;
import net.sourceforge.sqlexplorer.parsers.scp.StructuredCommentParser.CommandType;

/*package*/ class ParameterCommand extends Command {

	public ParameterCommand(StructuredCommentParser parser, Token comment, Tokenizer tokenizer, CharSequence data) throws ParserException {
		super(parser, CommandType.PARAMETER, comment, tokenizer, data);
		
		String name;
		NamedParameter.DataType dataType = NamedParameter.DataType.STRING;
		NamedParameter.Direction direction = null;
		
		// 	${parameter name [("output"|"inout")] [datatype]} [value]
		
		if (tokens.size() < 1)
			throw new StructuredCommentException("parameter has no parameter name", comment);
		name = popWord();
		
		// Get the direction; if we do not receognise the word, then assume that it is
		//	input and we actually found the datatype
		String str = popWord();
		if (str != null)
			try {
				direction = NamedParameter.Direction.valueOf(str.toUpperCase());
				str = null;
			} catch(IllegalArgumentException e) {
				direction = NamedParameter.Direction.INPUT;
			}
		
		// If the last parameter was used, get another
		if (str == null)
			str = popWord();
		
		// Check the datatype
		if (str != null)
			try {
				dataType = NamedParameter.DataType.valueOf(str.toUpperCase());
			} catch(IllegalArgumentException e) {
				throw new StructuredCommentException("Unrecognised data type " + str, comment);
			}
			
		LinkedList<Token> arguments = new LinkedList<Token>();
		while (!tokens.isEmpty()) {
			Token token = tokens.removeFirst();
			arguments.add(token);
		}
			
		parser.parser.addParameter(new NamedParameter(comment, name, dataType, direction, arguments, data));
	}
	
	protected String popWord() throws StructuredCommentException {
		if (tokens.isEmpty())
			return null;
		Token token = tokens.removeFirst();
		if (token.getTokenType() != TokenType.WORD)
			throw new StructuredCommentException("Invalid token, expected a word but got " + token.toString(), comment);
		return token.toString();
	}

	@Override
	public String toString() {
		return "parameter";
	}
}