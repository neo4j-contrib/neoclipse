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
 * Base class for Parser-related exceptions
 * 
 * @author John Spackman
 */
public class ParserException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int lineNo;
	private int charNo;

	public ParserException(String msg, int lineNo, int charNo, Throwable t) {
		super(msg + describePosition(lineNo, charNo), t);
		this.lineNo = lineNo;
		this.charNo = charNo;
	}

	public ParserException(String msg, int lineNo, int charNo) {
		super(msg + describePosition(lineNo, charNo));
		this.lineNo = lineNo;
		this.charNo = charNo;
	}

	public ParserException(Throwable t, int lineNo, int charNo) {
		super(t.getMessage() + describePosition(lineNo, charNo), t);
		this.lineNo = lineNo;
		this.charNo = charNo;
	}

	public int getCharNo() {
		return charNo;
	}

	public int getLineNo() {
		return lineNo;
	}
	
	private static String describePosition(int lineNo, int charNo) {
		if (lineNo < 1 && charNo < 1)
			return "";
		return " at line " + lineNo + ", position " + charNo;
	}

}
