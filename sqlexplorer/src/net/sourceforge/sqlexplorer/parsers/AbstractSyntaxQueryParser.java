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
import java.util.LinkedList;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.parsers.Tokenizer.Token;
import net.sourceforge.sqlexplorer.parsers.scp.StructuredCommentParser;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.util.BackedCharSequence;


/**
 * Implements the foundations of a query parser; derived implementations are expected to use
 * their platform-specific knowledge of a language to break the SQL text into individual
 * queries.
 * 
 * @author John Spackman
 */
public abstract class AbstractSyntaxQueryParser extends AbstractQueryParser {
	
	// We can hold onto a history of previous tokens in case we need to look back to
	//	check context; this is the maximum depth of history 
	private static final int MAX_PREVIOUS_TOKENS = 5;
	
	/*
	 * Simple class used to hold line number offset information
	 */
	private static class LineNoOffset implements Comparable<LineNoOffset> {
		public int lineNo;
		public int offset;
		
		public LineNoOffset(int lineNo, int offset) {
			super();
			this.lineNo = lineNo;
			this.offset = offset;
		}

		public int compareTo(LineNoOffset other) {
			return lineNo - other.lineNo;
		}

		@Override
		public boolean equals(Object obj) {
			LineNoOffset that = (LineNoOffset)obj;
			return that.lineNo == lineNo;
		}
		
		public String toString() {
			return "lineNo=" + lineNo + ", offset=" + offset;
		}
	}

	
	// Master buffer
	private StringBuffer buffer;
	
	// Tokenizer
	private Tokenizer tokenizer;
	
	// Token number; used to make sure we can't try and go back too far
	private int tokenNumber;
	
	// Previous tokens; not usually more than MAX_PREVIOUS_TOKENS
	private LinkedList<Tokenizer.Token> previousTokens = new LinkedList<Tokenizer.Token>();
	
	// Future tokens - i.e. tokens which have been grabbed from the Tokenizer but "ungot"
	private LinkedList<Tokenizer.Token> futureTokens = new LinkedList<Tokenizer.Token>();
	
	// Current token
	private Tokenizer.Token currentToken;
	
	// List of queries
	private LinkedList<Query> queries = new LinkedList<Query>();
	
	// Structured comment parser
	private boolean enableStructuredComments;
	
	// Line number offsets
	private SortedSet<LineNoOffset> lineNoOffsets = new TreeSet<LineNoOffset>();
	
	/**
	 * Constructor, initialises the parser/tokenizer with <code>sql</code>.
	 * @param sql
	 */
	public AbstractSyntaxQueryParser(CharSequence sql) {
		this(sql, SQLExplorerPlugin.getDefault().getPreferenceStore().getBoolean(IConstants.ENABLE_STRUCTURED_COMMENTS));
	}

	/**
	 * Constructor, initialises the parser/tokenizer with <code>sql</code>.
	 * @param sql
	 * @param enableStructuredComments
	 */
	public AbstractSyntaxQueryParser(CharSequence sql, boolean enableStructuredComments) {
		super();
		this.enableStructuredComments = enableStructuredComments;
		
	   	if (enableStructuredComments) {
	   		// Structured Comments require write access to the buffer that was parsed because
	   		//	certain commands trigger code re-writing 
		   	buffer = new StringBuffer(sql);
			tokenizer = new Tokenizer(buffer);
			
		// Otherwise just use a standard tokenizer
	   	} else {
			tokenizer = new Tokenizer(sql);
	   	}
	}
	
	/* (non-JavaDoc)
	 * @see net.sourceforge.sqlexplorer.parsers.QueryParser#parse()
	 */
	public void parse() throws ParserException {
		if (enableStructuredComments) {
			StructuredCommentParser preprocessor = new StructuredCommentParser(this, buffer);
				
			// Otherwise just use a standard tokenizer
			Token token;
			tokenizer.reset();
			while ((token = tokenizer.nextToken()) != null) {
				if (token.getTokenType() == Tokenizer.TokenType.EOL_COMMENT ||
						token.getTokenType() == Tokenizer.TokenType.ML_COMMENT) {
					preprocessor.addComment(token);
				}
			}
			
			// Do the structured comments and then reset the tokenizer
			preprocessor.process();
			tokenizer.reset();
		}

		// Do the parsing
		parseQueries();
		
		/*
		 * It's important to reset the tokenizer if structured comments are in use because some 
		 * of the commands in structured comments can cause the SQL to be rewritten; when this
		 * happens the start and end locations in any tokens and the starting line numbers in
		 * queries and any tokens must be updated to reflect the edits.  While we *could* update
		 * any state held by the tokenizer, this is unnecessary if the text has already been
		 * fully tokenized - reseting tokenizer to null is just to insist that it cannot be used
		 * again by accident.  
		 */
		tokenizer = null;
	}
	
	/**
	 * Parses the text into a series of queries
	 */
	protected abstract void parseQueries() throws ParserException;

	/* (non-JavaDoc)
	 * @see net.sourceforge.sqlexplorer.parsers.QueryParser#addLineNoOffset(int, int)
	 */
	public void addLineNoOffset(int originalLineNo, int numLines) {
		lineNoOffsets.add(new LineNoOffset(originalLineNo, numLines));
	}

	/* (non-JavaDoc)
	 * @see net.sourceforge.sqlexplorer.parsers.QueryParser#adjustLineNo(int)
	 */
	public int adjustLineNo(int lineNo) {
		/*
		 * Adjust the lineNo; we have a list of adjustments and at what point in the source
		 * the adjustment was made - the offset adjustment is always recorded at the original 
		 * lineNo.
		 * 
		 * We go through each adjustment and refactor the lineNo inversely to what was done;
		 * e.g. if we added 6 lines, then reduce lineNo by 6.
		 * 
		 * We will either run out of adjustments altogether, or we will find the lineNo hits
		 * in the middle of an insert ... in which case we return the closest original
		 */
		
		for (LineNoOffset offset : lineNoOffsets) {
			// If this offset is for a higher line number than we're interested in, we're done. 
			if (lineNo <= offset.lineNo)
				return lineNo;
			
			// If we've added lines
			if (offset.offset > 0) {
				// If the lineNo we're looking to (re-)adjust is in the middle of the new lines,
				//	return the lineNo as it is (ie closest line)
				if (lineNo >= offset.lineNo && lineNo < offset.lineNo + offset.offset)
					return offset.lineNo;
			}
			
			// Adjust the lineNo to compensate for this insert or delete
			lineNo -= offset.offset;
		}
		
		// Done
		return lineNo;
	}

	/**
	 * Skips to the first token on the next line
	 */
	protected void skipToEndOfLine() throws ParserException {
		int lineNo = currentToken.getLineNo();
		while (nextToken() != null && currentToken.getLineNo() == lineNo)
			; // Just loop around
	}
	
	/**
	 * Adds a query, taking the text between the two tokens inclusively,
	 * IE the query starts with the first character of start and end with
	 * the last character of end.
	 * @param start
	 * @param end
	 */
	protected void addQuery(Tokenizer.Token start, Tokenizer.Token end) {
		AnnotatedQuery query;
		if (end != null)
			query = newQueryInstance(start.outerSequence(end), start.getLineNo());
		else
			query = newQueryInstance(start.superSequence(start.getStart()), start.getLineNo());
		
		HashMap<String, NamedParameter> map = new HashMap<String, NamedParameter>();
		for (NamedParameter param : getParameters())
			if (param.getComment().getStart() <= query.getQuerySql().getStart())
				map.put(param.getName(), param);
		if (!map.isEmpty())
			query.setParameters(map);
		queries.add(query);
	}
	
	/**
	 * Instantiates a new Query object
	 * @param buffer
	 * @param lineNo
	 * @return
	 */
	protected AnnotatedQuery newQueryInstance(BackedCharSequence buffer, int lineNo) {
		return new AnnotatedQuery(buffer, lineNo);
	}
	
	/**
	 * Returns the next token
	 * @return
	 */
	protected Tokenizer.Token nextToken() throws ParserException {
		return nextToken(true);
	}
	
	/**
	 * Returns the next token but only trims the history if trimPrevious is true; 
	 * this is so that lookAhead() can work easily without loosing the current value
	 * @param trimPrevious
	 * @return
	 */
	private Tokenizer.Token nextToken(boolean trimPrevious) throws ParserException {
		// Move current into history
		if (currentToken != null) {
			previousTokens.add(currentToken);
			if (trimPrevious && previousTokens.size() > MAX_PREVIOUS_TOKENS)
				previousTokens.removeFirst();
		}
		
		// Use a stored "future" one if there is one, otherwise get another from the tokenizer
		if (!futureTokens.isEmpty())
			currentToken = futureTokens.removeFirst();
		else
			currentToken = tokenizer.nextToken();
		
		// Keep a track of how many we've had
		tokenNumber++;
		return currentToken;
	}
	
	/**
	 * Un-gets the token, and changes the current token to be the previous
	 */
	protected void ungetToken() {
		// Check we can unget (there must be a previous value)
		if (tokenNumber > 0 && previousTokens.isEmpty())
			throw new IllegalStateException("Cannot unget because there are not enough previous tokens");
		tokenNumber--;
		
		// We must have something to unget
		if (currentToken == null)
			throw new IllegalStateException("No token to unget");
		
		// Store it for the future and switch to the previous
		futureTokens.add(currentToken);
		if (previousTokens.size() > 0)
			currentToken = previousTokens.removeLast();
	}
	
	/**
	 * Looks ahead a given number of places; returns null if there are not enough tokens.
	 * A distance of 1 is the next token, but leaves the current token as it is
	 * @param distance
	 * @return
	 */
	protected Tokenizer.Token lookAhead(int distance) throws ParserException {
		// If we already have the future token cached, use it
		if (futureTokens.size() <= distance)
			return futureTokens.get(distance - 1);
		
		// Wind forward until we've got our token....
		Tokenizer.Token futureToken = null;
		for (int i = 0; i < distance; i++) {
			futureToken = nextToken(false);
			if (futureToken == null) {
				distance = i;
				break;
			}
		}
		
		// ...and then wind back
		while (distance > 0) {
			ungetToken();
			distance--;
		}
		
		return futureToken;
	}
	
	/**
	 * Returns the last token we saw
	 * @return
	 */
	protected Tokenizer.Token lastToken() {
		return lastToken(1);
	}
	
	/**
	 * Returns the token distance times back (distance of 1 is the last token)
	 * @param distance
	 * @return
	 */
	protected Tokenizer.Token lastToken(int distance) {
		if (previousTokens.size() < distance)
			throw new IllegalArgumentException();
		return previousTokens.get(previousTokens.size() - distance);
	}

	/**
	 * @return the currentToken
	 */
	public Tokenizer.Token getCurrentToken() {
		return currentToken;
	}

	/* (non-JavaDoc)
	 * @see net.sourceforge.sqlexplorer.parsers.QueryParser#iterator()
	 */
	public Iterator<Query> iterator() {
		return queries.iterator();
	}

	/**
	 * Sets the line number of the first line in the query
	 * @param initialLineNo
	 */
	public void setInitialLineNo(int initialLineNo) {
		if (tokenizer != null)
			tokenizer.setInitialLineNo(initialLineNo);
	}
	
	public int numberOfQueries() {
		return this.queries == null ? -1 : this.queries.size();
	}
	
}
