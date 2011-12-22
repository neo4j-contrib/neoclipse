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

import java.util.Iterator;
import java.util.LinkedList;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.parsers.Tokenizer.Token;
import net.sourceforge.sqlexplorer.parsers.scp.StructuredCommentException;
import net.sourceforge.sqlexplorer.parsers.scp.StructuredCommentParser;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;

/**
 * This parser is based on scanning the SQL text looking for separators (eg ";", "go", or 
 * "/") that show where to split the text into separate queries; conversely, the new 
 * AbstractSyntaxQueryParser derived style is based on scanning the SQL text for language grammar
 * tokens so that it can split the SQL by natural syntax.
 * 
 * (Note: the previous version was the SE3.0.0 parser, this is a rewrite for SE3.5RC6 
 * 
 * @modified John Spackman
 */
public class BasicQueryParser extends AbstractQueryParser {

	// Quote marks that wrap a string
	private static final String QUOTE_CHARS = "'\"";
	
	// Current line number into sql
	private int lineNo;
	
	// Current position into sql for the parser
	private int charIndex;
	
	// Whether quotes inside strings are escaped - and if so, the quote character
	private char quoteEscapes;
	
	// The start of a single-line comment
	private String slComment;
	
	// The start of a multi-line comment
	private String mlCommentStart;
	
	// The end of a multi-line comment
	private String mlCommentEnd;
	
	// Command separator
	private String cmdSeparator;
	
	// Alternative separator (must occur on a line on its own)
	private String altSeparator;
	
	// Whether structured comments are enabled
	private boolean enableStructuredComments;

	// The SQL
    private CharSequence sql;
    
    // Parsed queries
    private LinkedList<Query> queries;
    
    /**
     * Constructor
     * @param sql
     */
	public BasicQueryParser(CharSequence sql) {
		super();
        if (sql == null)
        	this.sql = "";
        else
        	this.sql = sql;
        lineNo = 1;
        
        cmdSeparator = ";";
	    altSeparator = "GO";
	    slComment = "--";
	    mlCommentStart = "/*";
	    mlCommentEnd = "*/";	    
	}

    /**
     * Constructor
     * @param sql
     */
	public BasicQueryParser(CharSequence sql, int pLineNo) {
		this(sql);
	    setCmdSeparator(SQLExplorerPlugin.getStringPref(IConstants.SQL_QRY_DELIMITER));
	    setAltSeparator(SQLExplorerPlugin.getStringPref(IConstants.SQL_ALT_QRY_DELIMITER));
	    setSlComment(SQLExplorerPlugin.getStringPref(IConstants.SQL_SL_COMMENT));
	    setMlCommentStart(SQLExplorerPlugin.getStringPref(IConstants.SQL_ML_COMMENT_START));
	    setMlCommentEnd(SQLExplorerPlugin.getStringPref(IConstants.SQL_ML_COMMENT_END));
	    setQuoteEscapes(getPref(IConstants.SQL_QUOTE_ESCAPE_CHAR));
	    enableStructuredComments = SQLExplorerPlugin.getBooleanPref(IConstants.ENABLE_STRUCTURED_COMMENTS);
	    
	    String str = getPref(IConstants.SQL_QUOTE_ESCAPE_CHAR);
	    if (str != null) {
	    	str = str.trim();
	    	if (str.length() > 0)
	    		quoteEscapes = str.charAt(0);
	    }
	    lineNo = pLineNo;
	}

    /* (non-JavaDoc)
	 * @see net.sourceforge.sqlexplorer.parsers.QueryParser#parse()
	 */
	public void parse() throws ParserException {
		if (sql == null)
			return;

	    if (enableStructuredComments) {
			StringBuffer buffer = new StringBuffer(sql.toString());
			Tokenizer tokenizer = new Tokenizer(buffer);
			StructuredCommentParser structuredComments = new StructuredCommentParser(this, buffer);
			
			// Otherwise just use a standard tokenizer
			try {
				Token token;
				while ((token = tokenizer.nextToken()) != null) {
					if (token.getTokenType() == Tokenizer.TokenType.EOL_COMMENT ||
							token.getTokenType() == Tokenizer.TokenType.ML_COMMENT) {
						structuredComments.addComment(token);
					}
				}
			}catch(StructuredCommentException e) {
				e.printStackTrace();
			}
			catch(Throwable e)
			{
				e.printStackTrace();				
			}
			
			// Do the structured comments and then reset the tokenizer
			structuredComments.process();
			tokenizer.reset();
			tokenizer = null;
			sql = buffer;
		}
        
        charIndex = 0;
        //BasicQuery query;
        queries = new LinkedList<Query>();
        for (BasicQuery query = getNextQuery(); query != null; query = getNextQuery())
        {
        	queries.add(query);
        }
	}

	/* (non-JavaDoc)
	 * @see net.sourceforge.sqlexplorer.parsers.QueryParser#addLineNoOffset(int, int)
	 */
	public void addLineNoOffset(int originalLineNo, int numLines) {
		// Nothing - not supported
	}

	/* (non-JavaDoc)
	 * @see net.sourceforge.sqlexplorer.parsers.QueryParser#adjustLineNo(int)
	 */
	public int adjustLineNo(int pLineNo) {
		return pLineNo; // Not implemented
	}

	/* (non-JavaDoc)
	 * @see net.sourceforge.sqlexplorer.parsers.QueryParser#iterator()
	 */
	public Iterator<Query> iterator() {
		return queries.iterator();
	}
	
	/**
	 * Gets the next query, or returns null if there are no more
	 * @return
	 */
	private BasicQuery getNextQuery() {
		if (charIndex >= sql.length())
			return null;
		
		int start = charIndex;
		int startOfLine = -1;
		char cQuote = 0;
		boolean inSLComment = false;
		boolean inMLComment = false;
		int startLineNo = -1;
		for (; charIndex < sql.length(); charIndex++) {
			char c = sql.charAt(charIndex);
			char nextC = 0;
			if (charIndex < sql.length() - 1)
				nextC = sql.charAt(charIndex + 1);
			
			// Skip comments
			if(inSLComment)
			{
				if(c == '\n')
				{
					inSLComment = false;
				}
				else
				{
					continue;
				}
			}
			if (inMLComment) 
			{
				if (c == '\n') {
					startOfLine = -1;
					lineNo++;
				}				
				inMLComment =  !nextIs(mlCommentEnd);
				continue;
			}
			// If we're quoting
			if (cQuote != 0) {
				if (c == quoteEscapes && QUOTE_CHARS.indexOf(nextC) > -1)
					charIndex++;
				else if (cQuote == c)
					cQuote = 0;
				continue;
			}

			// Skip leading whitespace
			if (start == charIndex && Character.isWhitespace(c)) {
				start++;
				if (c == '\n') {
					startOfLine = -1;
					lineNo++;
				}
				continue;
			}
			// Calculate the start of line (gets reset to -1 on every \n) 
			if (startOfLine < 0 && !Character.isWhitespace(c))
				startOfLine = charIndex;
			
			if (cmdSeparator != null && nextIs(cmdSeparator)) {
				int oldCharIndex = charIndex;
				charIndex += cmdSeparator.length();
				if (startLineNo < 0 || start + cmdSeparator.length() >= oldCharIndex) {
					start = charIndex;
					startLineNo = -1;
					startOfLine = -1;
					continue;
				}
				return new BasicQuery(sql.subSequence(start, oldCharIndex), startLineNo);
			}
			// Starting a quote?
			if (QUOTE_CHARS.indexOf(c) > -1) {
				cQuote = c;
				continue;
			}
			
			// Newlines - count line numbers, and look for "GO" (or equivelant)
			if (c == '\n') {
				if (rangeIs(startOfLine, charIndex, altSeparator)) {
					if (startLineNo < 0 || start + altSeparator.length() >= startOfLine) {
						start = charIndex + 1;
						startLineNo = -1;
						startOfLine = -1;
						lineNo++;
						continue;
					}
					return new BasicQuery(sql.subSequence(start, startOfLine), startLineNo);
				}
				if(startOfLine >= 0)
				{
					String lineStart = sql.subSequence(startOfLine, Math.min(charIndex, startOfLine + 15)).toString().toLowerCase();
					if (lineStart.startsWith("delimiter")) 
					{					
						String delimiter = lineStart.substring(9).trim();
						if(delimiter.length() == 1)
						{
						    setCmdSeparator(delimiter);
						    setAltSeparator(null);
						}
						else
						{
						    setCmdSeparator(new String(new byte[]{0}));
						    setAltSeparator(delimiter);						
						}
						if (startLineNo < 0 || start + lineStart.length() >= startOfLine) {
							start = charIndex + 1;
							startLineNo = -1;
							startOfLine = -1;
							lineNo++;
							continue;
						}
						return new BasicQuery(sql.subSequence(start, startOfLine), startLineNo);
					}
				}
				startOfLine = -1;
				lineNo++;
				if (inSLComment) {
					inSLComment = false;
					continue;
				}
			}
			
			
			// Starting a single-line comment
			if (nextIs(slComment)) {
				if (rangeIs(startOfLine, charIndex, altSeparator)) {
					if (startLineNo < 0 || start + altSeparator.length() >= startOfLine) {
						start = charIndex;
						startLineNo = -1;
						startOfLine = -1;
						continue;
					}
					return new BasicQuery(sql.subSequence(start, startOfLine), startLineNo);
				}
				inSLComment = true;
				continue;
			}
			
			// Starting a multi-line comment
			if (nextIs(mlCommentStart)) {
				if (rangeIs(startOfLine, charIndex, altSeparator)) {
					if (startLineNo < 0 || start + altSeparator.length() >= startOfLine) {
						start = charIndex;
						startLineNo = -1;
						startOfLine = -1;
						continue;
					}
					return new BasicQuery(sql.subSequence(start, startOfLine), startLineNo);
				}
				inMLComment = true;
				continue;
			}
			
			// Only update the startLineNo when we know when code starts
			if (startLineNo < 0 && !Character.isWhitespace(c))
				startLineNo = lineNo;
		}
		
		// Returns something if there is something to return
		if (start < charIndex) {
			if (rangeIs(startOfLine, charIndex, altSeparator)) {
				charIndex = sql.length();
				if (startLineNo < 0 || start + altSeparator.length() >= startOfLine)
					return null;
				return new BasicQuery(sql.subSequence(start, startOfLine), startLineNo);
			}
			return new BasicQuery(sql.subSequence(start, charIndex), startLineNo);
		}
		return null;
	}

	/**
	 * Determines whether the next part of the SQL is a given string
	 * @param str
	 * @return
	 */
	private boolean nextIs(String str) {
		if (str == null)
			return false;
		if (str.length() > sql.length() - charIndex)
			return false;
		CharSequence sub = sql.subSequence(charIndex, charIndex + str.length());
		return str.equals(sub.toString());
	}
	
	/**
	 * Determines whether a string is exactly in a given range
	 * @param start
	 * @param end
	 * @param str
	 * @return
	 */
	private boolean rangeIs(int start, int end, String str) {
		if (str == null || end - start != str.length())
			return false;
		String sub = sql.subSequence(start, end).toString();
		return str.equalsIgnoreCase(sub);
	}
	
	/**
	 * Gets a string from preferences, or null if the string is empty
	 * @param prefs
	 * @param id
	 * @return
	 */
	private String getPref(String id) {
		String str = SQLExplorerPlugin.getStringPref(id);
		if (str == null)
			return null;
		str = str.trim();
		if (str.length() == 0)
			return null;
		return str;
	}

	private String getValue(String value) {
		if (value == null)
			return null;
		value = value.trim();
		if (value.length() == 0)
			return null;
		return value;
	}
	public String getAltSeparator() {
		return altSeparator;
	}

	public void setAltSeparator(String altSeparator) {
		this.altSeparator = getValue(altSeparator);
	}

	public String getCmdSeparator() {
		return cmdSeparator;
	}

	public void setCmdSeparator(String cmdSeparator) {
		this.cmdSeparator = getValue(cmdSeparator);
	}

	public String getMlCommentEnd() {
		return mlCommentEnd;
	}

	public void setMlCommentEnd(String mlCommentEnd) {
		this.mlCommentEnd = getValue(mlCommentEnd);
	}

	public String getMlCommentStart() {
		return mlCommentStart;
	}

	public void setMlCommentStart(String mlCommentStart) {
		this.mlCommentStart = getValue(mlCommentStart);
	}

	public char getQuoteEscapes() {
		return quoteEscapes;
	}

	public void setQuoteEscapes(char quoteEscapes) {
		this.quoteEscapes = quoteEscapes;
	}

	public void setQuoteEscapes(String quoteEscapes) {
		quoteEscapes = getValue(quoteEscapes);
		this.quoteEscapes = quoteEscapes == null ? 0 : quoteEscapes.charAt(0);
	}

	public String getSlComment() {
		return slComment;
	}

	public void setSlComment(String slComment) {
		this.slComment = getValue(slComment);
	}

	public int numberOfQueries() {
		return this.queries == null ? -1 : this.queries.size();
	}
}
