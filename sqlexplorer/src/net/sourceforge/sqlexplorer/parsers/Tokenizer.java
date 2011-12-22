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

import net.sourceforge.sqlexplorer.util.BackedCharSequence;

/**
 * Tokenizer is a utility class for reading and tokenising a string; note that this
 * is not restricted to SQL - it is also used for tokenising structured comments.
 * @author John Spackman
 */
public class Tokenizer {

	/*
	 * Types of token which can be returned
	 */
	public enum TokenType {
		WORD, NUMBER, QUOTED, PUNCTUATION, EOL_COMMENT, ML_COMMENT
	};
	
	/*
	 * Tokens
	 */
	public static class Token extends BackedCharSequence {
		private TokenType tokenType;
		private int lineNo;
		private int charNo;
		
		public Token(StringBuffer buffer, TokenType tokenType, int start, int end, int lineNo, int charNo) {
			super(buffer, start, end);
			if (tokenType == null)
				throw new IllegalArgumentException();
			this.tokenType = tokenType;
			this.lineNo = lineNo;
			this.charNo = charNo;
		}
		
		/**
		 * @return the tokenType
		 */
		public TokenType getTokenType() {
			return tokenType;
		}

		/**
		 * @return the lineNo
		 */
		public int getLineNo() {
			return lineNo;
		}

		/**
		 * @return the charNo
		 */
		public int getCharNo() {
			return charNo;
		}

		/**
		 * Returns the value without quotes, if applicable
		 * @return
		 */
		public CharSequence getUnquotedValue() {
			if (tokenType == TokenType.QUOTED)
				return new BackedCharSequence(buffer, start + 1, end - 1);
			return this;
		}
	}
	
	// The SQL being parsed
	private StringBuffer sql;
	
	// Where in sql to look for the next token
	private int nextToken; 
	
	// Initial line number
	private int initialLineNo;
	
	// Current line number
	private int lineNo;
	
	// Current position within the line
	private int charNo;

	/**
	 * Constructor; the tokenizer will work on sql.  If sql is a StringBuffer
	 * it will use it as is, otherwise it will duplicate it into its own
	 * StringBuffer
	 * @param sql
	 */
	public Tokenizer(CharSequence sql) {
		super();
		if (sql instanceof StringBuffer)
			this.sql = (StringBuffer)sql;
		else
			this.sql = new StringBuffer(sql);
		lineNo = initialLineNo = 1;
		charNo = 0;
	}
	
	/**
	 * Resets the tokenizer to the start of the buffer
	 */
	public void reset() {
		nextToken = 0;
		lineNo = initialLineNo;
		charNo = 0;
	}
	
	/**
	 * Gets the remaining, untokenized part of the string
	 * @return
	 */
	public BackedCharSequence getRemainder() {
		return new BackedCharSequence(sql, nextToken, sql.length());
	}

	/**
	 * Generates a token which consists of everything from the current
	 * position up to and including the end of line character
	 * @return
	 */
	public Token skipToEOL() {
		int start = nextToken;
		for (; nextToken < sql.length(); nextToken++) {
			char c = sql.charAt(nextToken);
			if (c == '\n') {
				lineNo++;
				charNo = 1;
				break;
			}
		}
		
		// Return the token
		return new Token(sql, TokenType.WORD, start, nextToken, lineNo, charNo);
	}

	/**
	 * Scans looking for the next token, returning null if there are no more.
	 * Whitespace is not a token, but comments are.
	 * @return
	 */
	public Token nextToken() throws ParserException {
		TokenType tokenType = null;
		char currentQuote = 0;
		
		int startCharNo = charNo + 1;
		int startLineNo = lineNo;
		int start = nextToken;
		char c = 0;
		char nextC = 0;
		for (; nextToken < sql.length(); nextToken++) {
			c = sql.charAt(nextToken);
			
			if (c == '\n') {
				lineNo++;
				charNo = 0;
				
				// No token type?  then we're skipping whitespace so adjust
				if (tokenType == null) {
					startCharNo = charNo + 1;
					startLineNo = lineNo;
				}
			}
			charNo++;
			
			TokenType nextType = null;
			
			// Quotes
			if (c == '\'' || c == '\"') {
				// Unless it's in a comment
				if (tokenType == TokenType.EOL_COMMENT || tokenType == TokenType.ML_COMMENT)
					continue;
				
				// Not in between quotes but we are already on a token?  Then stop
				if (tokenType != TokenType.QUOTED && tokenType != null)
					break;
				
				// First token?  Then start eating characters
				if (tokenType == null) {
					tokenType = TokenType.QUOTED;
					currentQuote = c;
					continue;
					
				// Else if we're currently eating a quoted string
				} else if (tokenType == TokenType.QUOTED) {
					// It's got to be the same as the one that started it
					if (c != currentQuote)
						continue;
					
					// If the next char is the same quote, then it's an escapement
					//	EG:		'that''s mine'
					if (nextToken < sql.length() -1 && sql.charAt(nextToken + 1) == currentQuote) {
						// Skip
						nextToken++;
						charNo++;
						continue;
					}

					// End of quote; move to the character following and stop
					nextToken++;
					charNo++;
					currentQuote = 0;
					break;
				}
			}
			
			// In a quote? Carry on eating
			if (tokenType == TokenType.QUOTED)
				continue;
			
			// Check for comments up to the end of the line
			if (tokenType == TokenType.EOL_COMMENT) {
				// EOL?  Then we're done
				if (c == '\n')
					break;
				continue;
			}
			
			// If there's at least 2 characters left to check
			if (nextToken < sql.length() - 1) {
				nextC = sql.charAt(nextToken + 1);
				
				// If we're in a multi-line comment, check for the end of the comment
				if (tokenType == TokenType.ML_COMMENT) {
					// If we're at the end, skip to the character after the comment and stop
					if (c == '*' && nextC == '/') {
						nextToken += 2;
						charNo += 2;
						break;
					}
					
					// Still eating the comment
					continue;
				}
				
				// Single line comment
				if ((c == '-' && nextC == '-') || (c == '/' && nextC == '/'))
					nextType = TokenType.EOL_COMMENT;
				
				// Multi-line comment
				else if (c == '/' && nextC == '*')
					nextType = TokenType.ML_COMMENT;
				
				// Found something?
				if (nextType != null) {
					// If we're yet in a token, then start the token and start eating
					if (tokenType == null) {
						tokenType = nextType;
						continue;
					}
					
					// Else we're in a token already so exit to mark the end of that token
					break;
				}
			} else
				nextC = 0;

			// Continuing a word
			if (tokenType == TokenType.WORD) {
				if (!isIdentifier(c))
					break;
				continue;
			}
			
			// Starting a word
			if (tokenType == null && isFirstIdentifier(c)) {
				tokenType = TokenType.WORD;
				continue;
			}

			// A Number
			if (Character.isDigit(c)) {
				// If we're not in a token yet, OR: if we'd started punctuation but it's just a dot, then convert to a decimal (missing zero prefix)
				if (tokenType == null || (tokenType == TokenType.PUNCTUATION && sql.substring(start, nextToken).equals("."))) {
					tokenType = TokenType.NUMBER;
					continue;
					
				// Already on a number?  Then carry on eating
				} else if (tokenType == TokenType.NUMBER)
					continue;
				
				break;
			} else if (c == '.' && tokenType == TokenType.NUMBER) {
				continue;
			}
			
			// If it's an identifier but we're not on a word, then stop
			if (isIdentifier(c) && tokenType != TokenType.WORD)
				break;
			
			// Whitespace
			if (Character.isWhitespace(c)) {
				// If we're in a token, then stop
				if (tokenType != null)
					break;
				
				// Skip the whitespace
				start++;
				continue;
			}

			// If we're already on punctuation, stop there (punctuation is always only one character)
			if (tokenType == TokenType.PUNCTUATION)
				break;
			
			// Anything else is considered punctuation
			if (tokenType != null && tokenType != TokenType.PUNCTUATION)
				break;
			if (tokenType == null)
				tokenType = TokenType.PUNCTUATION;
		}
		
		// Check for unterminated strings
		if (currentQuote != 0)
			throw new ParserException("Unterminated string literal", startLineNo, startCharNo);
		if (tokenType == TokenType.ML_COMMENT && (c != '*' | nextC != '/'))
			throw new ParserException("Unterminated multi-line comment", startLineNo, startCharNo);
		
		// Nothing found?
		if (tokenType == null) {
			if (nextToken < sql.length())
				throw new RuntimeException("Internal error: could not find a token but buffer is not exhausted");
			return null;
			
		// If we found a token but the last character found was a CR, then we have to reduce
		//	the line count because the CR will be the first one read next time around.
		} else if (c == '\n') {
			lineNo--;
		}
		
		// Return the token
		return new Token(sql, tokenType, start, nextToken, startLineNo, startCharNo);
	}

	/**
	 * Returns true if c is suitable as the first character of an identifier; it
	 * must be a character or an underscore
	 * @param c
	 * @return true if its suitable as an identifier
	 */
	private boolean isFirstIdentifier(char c) {
		return Character.isLetter(c) || c == '_';
	}

	/**
	 * Returns true if c is suitable a subsequent character of an identifier; it
	 * must be a character or an underscore or digits
	 * @param c
	 * @return
	 */
	private boolean isIdentifier(char c) {
		return Character.isDigit(c) || isFirstIdentifier(c);
	}

	/**
	 * Sets the inital line number - the line number that the first line of
	 * text is percieved to be on
	 * @param initialLineNo
	 */
	public void setInitialLineNo(int initialLineNo) {
		this.initialLineNo = initialLineNo;
		this.lineNo = initialLineNo;
	}
	
}
