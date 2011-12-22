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
package net.sourceforge.sqlexplorer.plugin.editors;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.util.TextUtil;

public class Message {
	
	// Max length of the SQL to display in the results table
	private static final int MAX_SQL_DISPLAY_LENGTH = 70;
	
	public static enum Status {
		FAILURE {
			protected String getText() {
				return Messages.getString("SQLEditor.Results.Messages.Failure");
			}
		}, SUCCESS {
			protected String getText() {
				return Messages.getString("SQLEditor.Results.Messages.Success");
			}
		}, STATUS {
			protected String getText() {
				return Messages.getString("SQLEditor.Results.Messages.Status");
			}
		};
		
		protected abstract String getText();
	}
	
	// Whether this is a success or failure message
	private Status status;
	
	// Line number the message relates to
	private int lineNo;
	
	// Column (within the line identified by lineNo) that the message relates to
	private int charNo;
	
	// The SQL
	private String sql;
	
	// The message
	private String message;

	/**
	 * Constructor.
	 * @param status true if the message is about status
	 * @param lineNo first line number in sql that the message relates to
	 * @param charNo first character within the line identified by lineNo that the message relates to
	 * @param sql the SQL or command that was executed
	 * @param message the message
	 */
	public Message(Status status, int lineNo, int charNo, CharSequence sql, String message) {
		super();
		this.status = status;
		this.lineNo = lineNo;
		this.charNo = charNo;
		if (sql != null)
			setSql(sql);
		this.message = message;
	}
	
	/**
	 * Constructor.
	 * @param status true if the message is about success
	 * @param lineNo first line number in sql that the message relates to
	 * @param charNo first character within the line identified by lineNo that the message relates to
	 * @param sql the SQL or command that was executed
	 * @param message the message
	 */
	public Message(Status status, int lineNo, int charNo, String message) {
		this(status, lineNo, charNo, null, message);
	}
	
	/**
	 * Constructor.
	 * @param status true if the message is about success
	 * @param message the message
	 */
	public Message(Status status, String sql, String message) {
		this(status, 0, 0, sql, message);
	}
	
	/**
	 * Constructor.
	 * @param status true if the message is about success
	 * @param message the message
	 */
	public Message(Status status, String message) {
		this(status, 0, 0, message);
	}
	
	/**
	 * Returns an array of strings to be placed in the table's TableItem
	 * @return
	 */
	/*package*/ String[] getTableText() {
		String location = "";
		if (lineNo > 0) {
			location = "line " + lineNo;
			if (charNo > 0)
				location += ", col " + charNo;
		}
		
		String[] result = new String[] {
			status.getText(),
			location,
			(sql == null) ? "" : sql,
			TextUtil.getWrappedText(message)
		};
		return result; 
	}

	/**
	 * returns the status of the message
	 * @return
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * The first line within getSql() to which the message relates; zero
	 * if not available
	 * @return the lineNo
	 */
	public int getLineNo() {
		return lineNo;
	}

	/**
	 * The first character offset within the line identified by getLineNo()
	 * to which the message relates.  Zero if not available
	 * @return the charNo
	 */
	public int getCharNo() {
		return charNo;
	}

	/**
	 * Returns the message text
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @return the sql
	 */
	public String getSql() {
		return sql;
	}

	/**
	 * @param sql the sql to set
	 */
	public void setSql(CharSequence sql) {
		this.sql = TextUtil.compressWhitespace(sql, MAX_SQL_DISPLAY_LENGTH);
	}

	/**
	 * @param lineNo the lineNo to set
	 */
	public void setLineNo(int lineNo) {
		this.lineNo = lineNo;
	}

	/**
	 * @param charNo the charNo to set
	 */
	public void setCharNo(int charNo) {
		this.charNo = charNo;
	}
	
	public String toString() {
		return "[" + lineNo + "," + charNo + "] " + message;
	}
}