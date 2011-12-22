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
package net.sourceforge.sqlexplorer.util;

import java.util.Iterator;

/**
 * Implements CharSequence over a section of an existing StringBuffer;
 * note that it does not duplicate the buffer, just provides a view
 * onto a subsection of the StringBuffer.
 * 
 * The idea is that as we tokenize and parse queries, we can use instances
 * of BackedCharSequence to record where the runs of characters for that
 * particular token or query are, without having to duplicate the underlying
 * textual data.  This is not solely for memory conservation - because tokens
 * overlap queries, we are able to manipulate the tokens and queries after
 * tokenizing/parsing is complete (EG structured comments).
 * 
 * @author John Spackman
 */
public class BackedCharSequence implements CharSequence, Iterable<Character> {

	protected StringBuffer buffer;
	protected int start;
	protected int end;

	public BackedCharSequence(StringBuffer buffer, int start, int end) {
		super();
		if (start > end || start < 0)
			throw new StringIndexOutOfBoundsException();
		this.buffer = buffer;
		this.start = start;
		this.end = end;
	}
	
	public Iterator<Character> iterator() {
		return new Iterator<Character>() {

			private int pos = start;
			
			public boolean hasNext() {
				return pos < end;
			}

			public Character next() {
				return buffer.charAt(pos++);
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * Returns a String of the section we represent
	 */
	public String toString() {
		return buffer.substring(start, end);
	}
	
	/**
	 * Creates a sequence starting with this and ending with the sequence
	 * in end; note that both this and end MUST have the same buffer instance 
	 * @param end
	 * @return
	 */
	public BackedCharSequence outerSequence(BackedCharSequence end) {
		if (end.buffer != buffer)
			throw new IllegalArgumentException("Cannot form outer sequence from different buffers");
		return new BackedCharSequence(buffer, this.start, end.end);
	}
	
	/**
	 * Creates a sequence on the buffer with the given points; can be larger or smaller, but
	 * must be within the buffer
	 * @param start
	 * @param end
	 * @return
	 */
	public BackedCharSequence superSequence(int start, int end) {
		if (start < 0 || end > buffer.length())
			throw new IllegalArgumentException("Cannot super-size to larger than the buffer");
		return new BackedCharSequence(buffer, start, end);
	}

	/**
	 * Creates a sequence on the buffer starting at start
	 * @param start
	 * @return
	 */
	public BackedCharSequence superSequence(int start) {
		if (start < 0)
			throw new IllegalArgumentException("Cannot super-size to larger than the buffer");
		return new BackedCharSequence(buffer, start, end);
	}

	/**
	 * Determines whether <code>that</code> is completely contained in our buffer
	 * @param that
	 * @return
	 */
	public boolean contains(BackedCharSequence that) {
		if (buffer != that.buffer)
			return false;
		return start <= that.start && end >= that.end;
	}
	
	/**
	 * Adjusts the start and end positions by adding an offset
	 * @param offset amount to adjust by, can be negative
	 */
	public void applyOffset(int offset) {
		if (end + offset > buffer.length())
			throw new IllegalArgumentException("Cannot offset beyond the size of the buffer");
		if (start + offset < 0)
			throw new IllegalArgumentException("Cannot offset beyond the start of the buffer");
		start += offset;
		end += offset;
	}

	/**
	 * @return the start
	 */
	public int getStart() {
		return start;
	}

	/**
	 * @return the end
	 */
	public int getEnd() {
		return end;
	}

	/* (non-JavaDoc)
	 * @see java.lang.CharSequence#charAt(int)
	 */
	public char charAt(int index) {
		if (index < 0 || end <= start + index)
			throw new StringIndexOutOfBoundsException();
		return buffer.charAt(start + index);
	}

	/* (non-JavaDoc)
	 * @see java.lang.CharSequence#length()
	 */
	public int length() {
		return end - start;
	}

	/* (non-JavaDoc)
	 * @see java.lang.CharSequence#subSequence(int,int)
	 */
	public CharSequence subSequence(int start, int end) {
		if (this.end <= this.start + end || end < start || start < 0)
			throw new StringIndexOutOfBoundsException();
		return new BackedCharSequence(buffer, this.start + start, this.start + end);
	}

}
