/*
 * Copyright (C) 2008 SQL Explorer Development Team
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

import java.util.ListIterator;

import net.sourceforge.sqlexplorer.parsers.ParserException;
import net.sourceforge.sqlexplorer.parsers.Tokenizer;
import net.sourceforge.sqlexplorer.parsers.Tokenizer.Token;
import net.sourceforge.sqlexplorer.parsers.Tokenizer.TokenType;
import net.sourceforge.sqlexplorer.parsers.scp.StructuredCommentParser.CommandType;

/*package*/ class SetCommand extends Command {
	
	private String optionName; 

	public SetCommand(StructuredCommentParser parser, Token comment, Tokenizer tokenizer, CharSequence data) throws ParserException {
		super(parser, CommandType.SET, comment, tokenizer, data);
		if (tokens.size() == 0)
			throw new StructuredCommentException(commandType + " is missing a option name", comment);
		Token token = tokens.getFirst();
		if (token.getTokenType() != TokenType.WORD)
			throw new StructuredCommentException("Option names must be valid identifiers", comment);
		if (tokens.size() != 1)
			throw new StructuredCommentException(commandType + " has extra text after option name", comment);
		optionName = token.toString();
	}
	
	public String toString() {
		return "set";
	}
	
	public void process(ListIterator<Command> pIter)
	{
		this.parser.getContext().set(this.optionName, this.data.toString());
		this.parser.delete(pIter, this, this, true);
	}
}