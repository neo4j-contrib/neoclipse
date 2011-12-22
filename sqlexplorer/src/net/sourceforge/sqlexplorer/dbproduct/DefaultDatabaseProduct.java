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
package net.sourceforge.sqlexplorer.dbproduct;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.sqlexplorer.parsers.BasicQueryParser;
import net.sourceforge.sqlexplorer.parsers.Query;
import net.sourceforge.sqlexplorer.parsers.QueryParser;
import net.sourceforge.sqlexplorer.plugin.editors.Message;

public class DefaultDatabaseProduct extends AbstractDatabaseProduct {

	public Collection<Message> getErrorMessages(SQLConnection connection, SQLException e, int lineNoOffset) throws SQLException {
		List<Message> list = new LinkedList<Message>();
		String message = e.getMessage();
		int offset = 1;
		if(message != null)
		{
			int pos = message.indexOf(" at line ");
			if(pos > 0)
			{
				try
				{
					offset = Integer.parseInt(message.substring(pos + " at line ".length()));
				}
				catch(Exception ignored){}
			}
		}
		list.add(new Message(Message.Status.FAILURE, lineNoOffset + offset, 0, e.getMessage()));
		return list;
	}

	public Collection<Message> getServerMessages(SQLConnection connection) throws SQLException {
		return null;
	}

	public QueryParser getQueryParser(String sql, int initialLineNo) {
		return new BasicQueryParser(sql, initialLineNo);
	}

	public Collection<Message> getErrorMessages(SQLConnection connection, Query query) throws SQLException {
		return null;
	}

}
