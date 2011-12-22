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


import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import net.sourceforge.sqlexplorer.parsers.Tokenizer.Token;

/**
 * A named parameter
 * 
 * @author John Spackman
 */
public class NamedParameter implements Comparable<NamedParameter> {

	// Data Type of the parameter
	public enum DataType { 
		INTEGER {
			@Override
			public void configureStatement(NamedParameter param, CallableStatement stmt, int columnIndex) throws SQLException {
				if (param.isOutput())
					stmt.registerOutParameter(columnIndex, Types.INTEGER);
				int intValue = 0;
				if (param.value != null)
					try {
						intValue = Integer.parseInt(param.value.toString());
					} catch(NumberFormatException e) {
					}
				stmt.setInt(columnIndex, intValue);
			}
		},
		
		DECIMAL {
			@Override
			public void configureStatement(NamedParameter param, CallableStatement stmt, int columnIndex) throws SQLException {
				if (param.isOutput())
					stmt.registerOutParameter(columnIndex, Types.DOUBLE);
				double doubleValue = 0;
				if (param.value != null)
					try {
						doubleValue = Double.parseDouble(param.value.toString());
					} catch(NumberFormatException e) {
					}
				stmt.setDouble(columnIndex, doubleValue);
			}
		},
		
		STRING {
			@Override
			public void configureStatement(NamedParameter param, CallableStatement stmt, int columnIndex) throws SQLException {
				if (param.isOutput())
					stmt.registerOutParameter(columnIndex, Types.VARCHAR);
				stmt.setString(columnIndex, param.value == null ? "" : param.value.toString());
			}
		},
		
		DATE {
			@Override
			public void configureStatement(NamedParameter param, CallableStatement stmt, int columnIndex) throws SQLException {
				if (param.isOutput())
					stmt.registerOutParameter(columnIndex, Types.DATE);
				Date date = new Date(System.currentTimeMillis());
				if (param.value != null)
					try {
						DateFormat df;
						String formatName = null;
						if (param.arguments != null && !param.arguments.isEmpty())
							formatName = param.arguments.get(0).getUnquotedValue().toString().trim();
						if (formatName == null || formatName.length() == 0)
							df = DateFormat.getDateInstance();
						else if (formatName.equalsIgnoreCase("short"))
							df = DateFormat.getDateInstance(DateFormat.SHORT);
						else if (formatName.equalsIgnoreCase("medium"))
							df = DateFormat.getDateInstance(DateFormat.MEDIUM);
						else if (formatName.equalsIgnoreCase("long"))
							df = DateFormat.getDateInstance(DateFormat.LONG);
						else 
							df = new SimpleDateFormat(formatName);
						String value = param.value.toString();
						java.util.Date newDt = df.parse(value);
						date = new Date(newDt.getTime());
					} catch(ParseException e) {
						throw new SQLException(e.getMessage());
					}
				stmt.setDate(columnIndex, date);
			}
		},
		
		TIME {
			@Override
			public void configureStatement(NamedParameter param, CallableStatement stmt, int columnIndex) throws SQLException {
				if (param.isOutput())
					stmt.registerOutParameter(columnIndex, Types.TIME);
				Date date = new Date(System.currentTimeMillis());
				if (param.value != null)
					try {
						DateFormat df;
						String formatName = null;
						if (param.arguments != null && !param.arguments.isEmpty())
							formatName = param.arguments.get(0).getUnquotedValue().toString().trim();
						if (formatName == null || formatName.length() == 0)
							df = DateFormat.getTimeInstance();
						else if (formatName.equalsIgnoreCase("short"))
							df = DateFormat.getTimeInstance(DateFormat.SHORT);
						else if (formatName.equalsIgnoreCase("medium"))
							df = DateFormat.getTimeInstance(DateFormat.MEDIUM);
						else if (formatName.equalsIgnoreCase("long"))
							df = DateFormat.getTimeInstance(DateFormat.LONG);
						else 
							df = new SimpleDateFormat(formatName);
						date = new Date(df.parse(param.value.toString()).getTime());
					} catch(ParseException e) {
						throw new SQLException(e.getMessage());
					}
				stmt.setDate(columnIndex, date);
			}
		},
		
		DATETIME {
			@Override
			public void configureStatement(NamedParameter param, CallableStatement stmt, int columnIndex) throws SQLException {
				if (param.isOutput())
					stmt.registerOutParameter(columnIndex, Types.TIMESTAMP);
				Date date = new Date(System.currentTimeMillis());
				if (param.value != null)
					try {
						DateFormat df;
						String formatName = null;
						if (param.arguments != null && !param.arguments.isEmpty())
							formatName = param.arguments.get(0).getUnquotedValue().toString().trim();
						if (formatName == null || formatName.length() == 0)
							df = DateFormat.getDateTimeInstance();
						else if (formatName.equalsIgnoreCase("short"))
							df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
						else if (formatName.equalsIgnoreCase("medium"))
							df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
						else if (formatName.equalsIgnoreCase("long"))
							df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
						else 
							df = new SimpleDateFormat(formatName);
						date = new Date(df.parse(param.value.toString()).getTime());
					} catch(ParseException e) {
						throw new SQLException(e.getMessage());
					}
				stmt.setDate(columnIndex, date);
			}
		},
		
		CURSOR {
			@Override
			public void configureStatement(NamedParameter param, CallableStatement stmt, int columnIndex) throws SQLException {
				throw new IllegalAccessError("Cursors are not supported by this database type");
			}
		};
		
		public abstract void configureStatement(NamedParameter param, CallableStatement stmt, int columnIndex) throws SQLException;
	};
	
	// Directionality
	public enum Direction { INPUT, OUTPUT, INOUT };
	
	// The Token containing the structured comment which defined this
	//	named parameter; used to determine scope
	private Token comment;
	
	// Parameter name
	private String name;
	
	// Data Type of the parameter
	private DataType dataType;
	
	// IN, OUT, or both
	private Direction direction;
	
	// Arguments
	private List<Token> arguments;
	
	// Value of the parameter (for input parameters only)
	private CharSequence value;

	public NamedParameter(Token comment, String name, DataType dataType, Direction direction, List<Token> arguments, CharSequence value) {
		super();
		this.comment = comment;
		this.name = name;
		this.dataType = dataType;
		this.direction = direction;
		this.arguments = arguments;
		this.value = value;
	}

	/**
	 * Configures the statement with this parameter, where the parameter is at a 
	 * given ordinal index
	 * @param stmt
	 * @param param
	 * @param columnIndex
	 */
	public void configureStatement(CallableStatement stmt, int columnIndex) throws SQLException {
		dataType.configureStatement(this, stmt, columnIndex);
	}
	
	/**
	 * Returns true if this parameter is an output (may also be an input)
	 * @return
	 */
	public boolean isOutput() {
		return direction != Direction.INPUT;
	}
	
	/**
	 * Returns true if this parameter is an input (may also be an output)
	 * @return
	 */
	public boolean isInput() {
		return direction != Direction.OUTPUT;
	}
	
	public Token getComment() {
		return comment;
	}

	public DataType getDataType() {
		return dataType;
	}

	public Direction getDirection() {
		return direction;
	}

	public String getName() {
		return name;
	}

	public CharSequence getValue() {
		return value;
	}
	
	public int compareTo(NamedParameter that) {
		return name.compareTo(that.name);
	}

	public String toString() {
		return ":" + name + "[" + direction.toString().toLowerCase() + " " + dataType.toString().toLowerCase() + "] = " + (value == null ? "null" : ("\"" + value.toString() + "\""));
	}
}
