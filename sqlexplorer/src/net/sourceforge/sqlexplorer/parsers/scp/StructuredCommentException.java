/*
 * http://sourceforge.net/projects/eclipsesql
 * Copyright (C) 2007 SQL Explorer Development Team
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

import net.sourceforge.sqlexplorer.parsers.ParserException;
import net.sourceforge.sqlexplorer.parsers.Tokenizer.Token;


/**
 * Thrown by the structured comment parser
 * 
 * @author John Spackman
 */
public class StructuredCommentException extends ParserException {

	private static final long serialVersionUID = 1L;

	public StructuredCommentException(String msg, int lineNo, int charNo, Throwable t) {
		super(msg, lineNo, charNo, t);
	}

	public StructuredCommentException(String msg, int lineNo, int charNo) {
		super(msg, lineNo, charNo);
	}

	public StructuredCommentException(Throwable t, int lineNo, int charNo) {
		super(t, lineNo, charNo);
	}
	
	public StructuredCommentException(String msg, Token token, Throwable t) {
		this(msg, token.getLineNo(), token.getCharNo(), t);
	}

	public StructuredCommentException(String msg, Token token) {
		this(msg, token.getLineNo(), token.getCharNo());
	}

	public StructuredCommentException(Throwable t, Token token) {
		this(t, token.getLineNo(), token.getCharNo());
	}
}
