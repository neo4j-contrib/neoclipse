/*
 * Copyright (C) 2006 SQL Explorer Development Team
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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

import net.sourceforge.sqlexplorer.parsers.ExecutionContext;
import net.sourceforge.sqlexplorer.parsers.ParserException;
import net.sourceforge.sqlexplorer.parsers.QueryParser;
import net.sourceforge.sqlexplorer.parsers.Tokenizer;
import net.sourceforge.sqlexplorer.parsers.Tokenizer.Token;
import net.sourceforge.sqlexplorer.parsers.Tokenizer.TokenType;

/**
 * Implements extensions to the underlying database platform via structured comments.  Each structured 
 * comment begins with ${ as the FIRST two characters in the comment - no space can occur between the 
 * start of the comment and the ${.  From there up to the first } is a command, and the rest of the 
 * comment is data for the command to act on.
 * 
 * The commands supported are:
 * 
 * 	${define macro-name} value
 * 		Defines a macro called macro-name and assigns a value
 * 
 * 	${ifdef [!]macro-name} [data]
 * 		If macro-name has been defined, then the data in the comment (IE the text outside the 
 * 		${...}) is uncommented.  If the optional ! is provided then the macro-name must NOT have 
 * 		been defined.  If data has not been given, then ordinary code between ifdef and the next
 * 		else or endif is commented depending on whether macro-name is defined
 * 
 * 	${else} [data]
 * 		If the last structured comment was an ifdef which evaluated to false, then the data in 
 * 		the comment is uncommented if
 * 
 * 	${endif}
 * 		Only needed to complete the multi-line versions of ifdef/else.
 * 
 * 	${undef macro-name}
 * 		Undefines the given macro name
 * 
 * 	${ref macro-name}
 * 		Causes the value of macro-name to be output instead of the comment; note that no error is
 * 		raised if macro-name has not been defined.
 * 
 * 	${endref}
 * 		Needed to complete multi-line versions of ref
 * 
 * 	${parameter name [("output"|"inout")] [datatype] [arguments]} [value]
 * 		Declares a named parameter, it's datatype (decimal, int, string, or cursor), whether it is
 * 		an output variable, and an optional value.  If the datatype is "cursor" then it is an output 
 * 		variable only; if not specified, it is input only.  Parameters are specified in queries
 * 		using the notation ":name", eg "select * from people where age = :agetofind".  If input
 * 		or output is not specified, then input is assumed (unless it is a cursor).  Some datatypes
 * 		have additional parameters - eg parameters of type "date", where the format can be specified.
 * 
 * Up and coming:
 * 	${question [id=id] [datatype=(char|int|decimal|date)]} data
 * 		Interactively asks a user a question before running; the text of the question is in data,
 * 		the default datatype is char.  ID is used so that subsequent executions can remember the
 * 		last value typed for that ID.
 * 
 * 	${date 'date-in-locale'}
 * 		Replaced with parameter substitution to provide a date; date-in-locale is the date
 * 		specified in the client's default locale.
 * 
 * 	${content-type column=column-name, type='mime-type'}
 * 		Annotates a column named column-name as having the given mime-type, overriding the default
 * 		assumption by SQLExplorer.  Typically intended for marking BLOBs as (for example) images,
 * 		can also be used to determine that varchars or CLOBs might contain XML, etc etc.
 *
 * Examples:
 * NOTE: in all of these examples, -- comments are used; the Java/C style of slash-asterisk to 
 * asterisk-slash also works, but there's no examples here because Java will not understand it.
 * 
 * 		--${define DEBUG} true
 * 		--${ifdef DEBUG} dbms_output.put_line('in debug mode...');
 * 		--${else} dbms_output.put_line('not in debug mode');
 * 		--${endif}
 * 
 * Defines a macro called DEBUG, and outputs one of the dbms_output statements.  If loaded through
 * a standard SQL tool, both statements would remain comments. 
 * 
 * 		--${ifdef DEBUG} dbms_output.put_line('in debug mode');
 * 			dbms_output.put_line('The default is not in debug mode');
 * 		--${endif}
 * 
 * If DEBUG is defined, then the first dbms_output statement would be uncommented, but the
 * second will become commented; however if code was loaded through a standard SQL tool then
 * the second line will run by default.
 * 
 * 		--${define DEFAULT_DATE} to_date('1980-JAN-01')
 * 			...snip...
 * 		insert into mytable (id, when) 
 * 			values (
 * 				1,
 * 				--${ref DEFAULT_DATE}
 * 			);
 * 
 * This defines a macro called DEFAULT_DATE and assigns it the value "to_date('1980-JAN-01')";
 * later on it uses ref to get the value of DEFAULT_DATE to insert into the second column
 * 
 * 		--${define SOME_SQL} select * from blah;
 * 			...snip...
 * 		--${ref SOME_SQL}
 * 		null;
 * 		--${endref}
 * 
 * This defines SOME_SQL as "select * from blah;" and refers to it later.  However, because the
 * code uses <code>endref</code>, then the non-comment between ref and endref is commented out;
 * this allows a standard SQL tool to still be able to compile with a default.
 * 
 * @author John Spackman
 */
public class StructuredCommentParser {
	
	/*
	 * Type of command; knows how to instantiate a Command 
	 */
	public enum CommandType { 
		DEFINE {
			@Override
			public Command createInstance(StructuredCommentParser parser, Token comment, Tokenizer tokenizer, CharSequence data) throws ParserException {
				return new DefineCommand(parser, comment, tokenizer, data);
			}
		}, 
		UNDEF {
			@Override
			public Command createInstance(StructuredCommentParser parser, Token comment, Tokenizer tokenizer, CharSequence data) throws ParserException {
				return new UndefCommand(parser, comment, tokenizer, data);
			}
		}, 
		IFDEF {
			@Override
			public Command createInstance(StructuredCommentParser parser, Token comment, Tokenizer tokenizer, CharSequence data) throws ParserException {
				return new IfdefCommand(parser, comment, tokenizer, data);
			}
		}, 
		ELSE {
			@Override
			public Command createInstance(StructuredCommentParser parser, Token comment, Tokenizer tokenizer, CharSequence data) throws ParserException {
				return new ElseCommand(parser, comment, tokenizer, data);
			}
		}, 
		ENDIF {
			@Override
			public Command createInstance(StructuredCommentParser parser, Token comment, Tokenizer tokenizer, CharSequence data) throws ParserException {
				return new EndifCommand(parser, comment, tokenizer, data);
			}
		}, 
		REF {
			@Override
			public Command createInstance(StructuredCommentParser parser, Token comment, Tokenizer tokenizer, CharSequence data) throws ParserException {
				return new RefCommand(parser, comment, tokenizer, data);
			}
		}, 
		ENDREF {
			@Override
			public Command createInstance(StructuredCommentParser parser, Token comment, Tokenizer tokenizer, CharSequence data) throws ParserException {
				return new EndrefCommand(parser, comment, tokenizer, data);
			}
		},
		PARAMETER {
			@Override
			public Command createInstance(StructuredCommentParser parser, Token comment, Tokenizer tokenizer, CharSequence data) throws ParserException {
				new ParameterCommand(parser, comment, tokenizer, data);
				return null;
			}
		},		
		SET {
			@Override
			public Command createInstance(StructuredCommentParser parser, Token comment, Tokenizer tokenizer, CharSequence data) throws ParserException {
				return new SetCommand(parser, comment, tokenizer, data);
			}
		};
		
		public abstract Command createInstance(StructuredCommentParser parser, Token comment, Tokenizer tokenizer, CharSequence data) throws ParserException;
	};
	
	// The QueryParser
	protected QueryParser parser;
	
	// Master buffer
	protected StringBuffer buffer;

	// Structured comments
	protected LinkedList<Command> commands = new LinkedList<Command>();
	
	// Macros which have been defined
	HashMap<String, CharSequence> macros = new HashMap<String, CharSequence>();

	/**
	 * Constructor.  <code>buffer</code> must be a writable buffer against which all
	 * tokens past to addComment have been parsed.  
	 * @param buffer
	 */
	public StructuredCommentParser(QueryParser parser, StringBuffer buffer) {
		super();
		this.parser = parser;
		this.buffer = buffer;
	}
	
	/**
	 * Adds a comment to the list; it will be ignored if it is not a structured comment
	 * @param token
	 * @throws StructuredCommentException - usually if the comment begins ${ but is unparsable
	 */
	public void addComment(Token comment) throws ParserException {
		Command command = createCommand(comment);
		if (command == null)
			return;
		
		/*
		 * Conditional constructs (ie IFDEF, ELSE, and ENDIF) are linked together
		 * so that we can easily identify which bits of code go through to the server
		 */
		
		// If we get an ELSE
		if (command instanceof ElseCommand) {
			ElseCommand elseCmd = (ElseCommand)command;
			Command last = null;
			int nestingDepth = 0;
			
			// Starting at the most recent structured comment, work towards the first
			//	looking for the IFDEF that we're part of
			ListIterator<Command> iter = commands.listIterator(commands.size());
			while (iter.hasPrevious()) {
				last = iter.previous();
				
				// If we see an ENDIF on the way, remember the nesting so that we skip
				//	it's IFDEF
				if (last instanceof EndifCommand)
					nestingDepth++;
				
				// Found an IFDEF?
				else if (last instanceof IfdefCommand) {
					if (nestingDepth == 0)
						break;
					else
						nestingDepth--;
				}
			}
			if (last == null)
				throw new StructuredCommentException("Unexpected 'else' command - no previous ifdef", comment);
			IfdefCommand ifdef = (IfdefCommand)last;
			if (ifdef.next != null)
				throw new StructuredCommentException("Unexpected 'else' command - else already encountered for this ifdef", comment);
			
			// Link it to us
			ifdef.next = elseCmd;
			elseCmd.previous = ifdef;
			
		// ENDIF construct
		} else if (command instanceof EndifCommand) {
			EndifCommand endif = (EndifCommand)command;
			Command last = null;
			int nestingDepth = 0;
			
			// Starting at the most recent structured comment, work towards the first
			//	looking for the IFDEF that we're part of
			ListIterator<Command> iter = commands.listIterator(commands.size());
			while (iter.hasPrevious()) {
				last = iter.previous();
				
				// If we see an ENDIF on the way, remember the nesting so that we skip
				//	it's IFDEF
				if (last instanceof EndifCommand)
					nestingDepth++;
				
				// IFDEF?  Check nesting, and only accept it if we're not on a nested IFDEF
				else if (last instanceof IfdefCommand) {
					if (nestingDepth == 0)
						break;
					else
						nestingDepth--;
					
				// ELSE command - also affected by nesting
				} else if (last instanceof ElseCommand) {
					if (nestingDepth == 0)
						break;
				}
			}
			if (last == null)
				throw new StructuredCommentException("Unexpected 'endif' command - no previous ifdef", comment);
			PeeredCommand cond = (PeeredCommand)last;
			if (cond.next != null)
				throw new StructuredCommentException("Unexpected 'endif' command - endif already encountered for this ifdef/else", comment);
			
			// Store it
			cond.next = endif;
			endif.previous = cond;
			
		// EndRef
		} else if (command instanceof EndrefCommand) {
			EndrefCommand endref = (EndrefCommand)command;
			Command last = commands.size() == 0 ? null : (Command)commands.getLast();
			if (last == null || !(last instanceof RefCommand))
				throw new StructuredCommentException("Unexpected endref - no preceeding ref", comment);
			
			// Store it
			RefCommand ref = (RefCommand)last;
			ref.endref = endref;
			endref.ref = ref;
		}
		
		// Add the new command
		commands.add(command);
	}
	
	/**
	 * Applies structured comments onto onto the buffer.
	 */
	public void process() {
		ListIterator<Command> iter = commands.listIterator();
		
		while (iter.hasNext()) {
			Command command = iter.next();
			
			if (command.commandType == CommandType.DEFINE) {
				DefineCommand def = (DefineCommand)command;
				macros.put(def.macroName, def.data == null ? "" : def.data);
				delete(iter, def, def, true);
				
			} else if (command.commandType == CommandType.UNDEF) {
				UndefCommand def = (UndefCommand)command;
				macros.remove(def.macroName);
				delete(iter, def, def, true);
				
			} else if (command.commandType == CommandType.IFDEF) {
				IfdefCommand def = (IfdefCommand)command;

				// A next means its a multi-line ifdef
				if (def.next != null) {
					// If it evaluates to false, then we have to delete the content
					if (!def.evaluate()) {
						delete(iter, def, def.next, false);
					
					// Else delete the comment
					} else
						replace(iter, def, def.data);
					
				// Single-line ifdef
				} else {
					// If it evaluates to true we have to replace the comment with the data
					//	otherwise we just leave it commented out
					if (def.evaluate())
						replace(iter, def, def.data);
					
					// Else delete the comment
					else
						delete(iter, def, def, true);
				}
				
			} else if (command.commandType == CommandType.ELSE) {
				ElseCommand elseCmd = (ElseCommand)command;
				IfdefCommand ifdef = (IfdefCommand)elseCmd.previous;
				
				// A next means that it's a multi-line "else"
				if (elseCmd.next != null) {
					// If the ifdef was true, then delete the content
					if (ifdef.evaluate())
						delete(iter, elseCmd, elseCmd.next, true);
					
					// Else delete the comment
					else
						delete(iter, elseCmd, elseCmd, true);
					
				// Single line "else"
				} else {
					// If the IFDEF failed, replace the ELSE comment with the ELSE data
					if (!ifdef.evaluate())
						replace(iter, elseCmd, elseCmd.data);
					
					// Else delete the comment
					else
						delete(iter, elseCmd, elseCmd, true);
				}
				
			} else if (command.commandType == CommandType.REF) {
				RefCommand ref = (RefCommand)command;
				CharSequence seq = macros.get(ref.macroName);
				
				// An ENDREF means it's multi-line
				if (ref.endref != null) {
					// If the macro does not exist, use the content as a default
					if (seq == null) {
						delete(iter, ref, ref, true);
						iter.next();
						delete(iter, ref.endref, ref.endref, true);
					} else
						replace(iter, ref, ref.endref, seq);
				} else {
					if (seq == null)
						seq = "";
					replace(iter, ref, seq);
				}
				
			} else if (command.commandType == CommandType.ENDIF) {
				delete(iter, command, command, true);
				
			} else if (command.commandType == CommandType.PARAMETER) {
				
			} else {
				command.process(iter);
			}
		}
	}
	
	/**
	 * Attempts to create a AbstractCommand from a comment token
	 * @param comment the comment to parse
	 * @return the new AbstractCommand, or null if it is not a structured comment
	 * @throws StructuredCommentException
	 */
	protected Command createCommand(Token comment) throws ParserException {
		StringBuffer sb = new StringBuffer(comment);
		sb.delete(0, 2);
		if (comment.getTokenType() == TokenType.ML_COMMENT)
			sb.delete(sb.length() - 2, sb.length());
		// support sl comment with space before ${ to be a real sl comment
		if (comment.getTokenType() == TokenType.EOL_COMMENT && sb.length() > 0 && sb.charAt(0) == ' ')
			sb.delete(0, 1);
		
		// Make sure it begins ${, but silently ignore it if not
		int pos = sb.indexOf("}", 2);
		if (sb.length() < 3 || !sb.substring(0, 2).equals("${") || pos < 0)
			return null;
		
		// Extract the command (ie the bit between "${" and "}") and the data (the bit after the "}")
		String data = null;
		if (pos < sb.length()) {
			data = sb.substring(pos + 1).trim();
			if (data.length() == 0)
				data = null;
		}
		sb = new StringBuffer(sb.substring(2, pos));
		
		// ...and has a word as the first token
		Tokenizer tokenizer = new Tokenizer(sb);
		Token token = tokenizer.nextToken();
		if (token == null)
			return null;
		if (token.getTokenType() != TokenType.WORD)
			throw new StructuredCommentException("Unexpected command in structured comment: " + token.toString(), comment);
		
		// Create a new AbstractCommand
		CommandType type;
		try {
			// I've kept the determination of CommandType outside of the constructor in case we want 
			//	to instantiate different classes for the different commands. 
			type = CommandType.valueOf(token.toString().toUpperCase());
		} catch(IllegalArgumentException e) {
			throw new StructuredCommentException("Unrecognised structured comment command \"" + token.toString() + "\"", comment);
		}
		
		return type.createInstance(this, comment, tokenizer, data);
	}
	
	/**
	 * Deletes a section from the buffer and adjusts the offsets in the upcoming tokens accordingly; the section
	 * to be deleted starts at the first character of startCmd and continues up to either the first character of 
	 * endCmd if endInclusive is false, or the last character of endCmd is endInclusive is true
	 * @param iter iterator which will return the future tokens
	 * @param startCmd
	 * @param endCmd
	 * @param endInclusive
	 */
	protected void delete(ListIterator<Command> iter, Command startCmd, Command endCmd, boolean endInclusive) {
		int numLines = endCmd.comment.getLineNo() - startCmd.comment.getLineNo();
		for (char c : endCmd.comment)
			if (c == '\n')
				numLines++;
		if (numLines != 0)
			parser.addLineNoOffset(startCmd.comment.getLineNo(), -numLines);
		
		int start = startCmd.comment.getStart();
		int end = endInclusive ? endCmd.comment.getEnd() : endCmd.comment.getStart();
		buffer.delete(start, end);
		int offset = -(end - start);
		
		if (startCmd != endCmd) {
			while (iter.next() != endCmd)
				;
			if (!endInclusive)
				iter.previous();
		}
		
		iter = commands.listIterator(iter.nextIndex());
		while (iter.hasNext()) {
			Command command = iter.next();
			command.comment.applyOffset(offset);
		}
	}

	/**
	 * Replaces a section of the buffer, starting with the first character of startCmd and ending with the last 
	 * character of endCmd; after replacement it adjusts the offsets in the upcoming tokens
	 * @param iter iterator which will return the future tokens
	 * @param startCmd
	 * @param endCmd
	 * @param replacement the replacement text
	 */
	protected void replace(ListIterator<Command> iter, Command startCmd, Command endCmd, CharSequence replacement) {
		if (replacement == null)
			replacement = "";
		int numLines = endCmd.comment.getLineNo() - startCmd.comment.getLineNo();
		for (char c : endCmd.comment)
			if (c == '\n')
				numLines++;
		for (int i = 0; i < replacement.length(); i++)
			if (replacement.charAt(i) == '\n')
				numLines--;
		parser.addLineNoOffset(startCmd.comment.getLineNo(), -numLines);
		
		int start = startCmd.comment.getStart();
		int end = endCmd.comment.getEnd();
		buffer.delete(start, end);
		buffer.insert(start, replacement);
		int offset = -(end - start) + replacement.length();
		
		if (startCmd != endCmd) {
			while (iter.next() != endCmd)
				;
		}
		
		iter = commands.listIterator(iter.nextIndex());
		while (iter.hasNext()) {
			Command command = iter.next();
			command.comment.applyOffset(offset);
		}
	}
	
	/**
	 * Replaces a section of the buffer, starting with the first character of startCmd and ending with the last 
	 * character of startCmd; after replacement it adjusts the offsets in the upcoming tokens
	 * @param iter iterator which will return the future tokens
	 * @param startCmd
	 * @param replacement the replacement text
	 */
	protected void replace(ListIterator<Command> iter, Command token, CharSequence replacement) {
		replace(iter, token, token, replacement);
	}

	public ExecutionContext getContext()
	{
		return this.parser.getContext();
	}
}
