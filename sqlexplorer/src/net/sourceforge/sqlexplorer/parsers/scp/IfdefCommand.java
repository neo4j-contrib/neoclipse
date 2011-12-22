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

import java.util.Iterator;

import net.sourceforge.sqlexplorer.parsers.ParserException;
import net.sourceforge.sqlexplorer.parsers.Tokenizer;
import net.sourceforge.sqlexplorer.parsers.Tokenizer.Token;
import net.sourceforge.sqlexplorer.parsers.Tokenizer.TokenType;
import net.sourceforge.sqlexplorer.parsers.scp.StructuredCommentParser.CommandType;

/*package*/ class IfdefCommand extends PeeredCommand {
	protected String macroName;
	protected boolean negated;

	public IfdefCommand(StructuredCommentParser parser, Token comment, Tokenizer tokenizer, CharSequence data) throws ParserException {
		super(parser, CommandType.IFDEF, comment, tokenizer, data);
		if (tokens.size() == 0)
			throw new StructuredCommentException(commandType + " is missing a macro name", comment);
		Iterator<Token> iter = tokens.iterator();
		Token token = iter.next();
		if (token.getTokenType() == TokenType.PUNCTUATION && token.toString().equals("!")) {
			negated = true;
			if (!iter.hasNext())
				throw new StructuredCommentException(commandType + " is missing a macro name", comment);
			token = iter.next();
		}
		if (token.getTokenType() != TokenType.WORD)
			throw new StructuredCommentException("Macro names must be valid identifiers", comment);
		macroName = token.toString();
		if (iter.hasNext())
			throw new StructuredCommentException("ifdef has extra text after the macro name", comment);
	}
	
	public boolean evaluate() {
		boolean eval = parser.macros.containsKey(macroName);
		if (negated)
			eval = !eval;
		return eval;
	}
	
	public String toString() {
		return "ifdef " + (negated ? "!" : "") + macroName;
	}
}