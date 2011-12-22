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
import java.util.ListIterator;

import net.sourceforge.sqlexplorer.parsers.ParserException;
import net.sourceforge.sqlexplorer.parsers.Tokenizer;
import net.sourceforge.sqlexplorer.parsers.Tokenizer.Token;
import net.sourceforge.sqlexplorer.parsers.scp.StructuredCommentParser.CommandType;


/**
 * Base class for StructuredCommentParser commands
 * @author John Spackman
 *
 */
/*package*/ class Command {
	protected StructuredCommentParser parser;
	protected CommandType commandType;
	protected Token comment;
	protected LinkedList<Token> tokens = new LinkedList<Token>();
	protected CharSequence data;
	
	/**
	 * Constructor
	 * @param comment the original comment
	 * @param tokenizer a tokenizer built to parse the comment; it is expected 
	 * 	that the comment start and leading ${ have already been be skipped over 
	 * @throws StructuredCommentException
	 */
	public Command(StructuredCommentParser parser, CommandType commandType, Token comment, Tokenizer tokenizer, CharSequence data) throws ParserException {
		this.parser = parser;
		this.commandType = commandType;
		this.comment = comment;
		this.data = data;
		Token token;
		while ((token = tokenizer.nextToken()) != null)
			tokens.add(token);
	}
	
	public void process(ListIterator<Command> pIter)
	{
		
	}
}