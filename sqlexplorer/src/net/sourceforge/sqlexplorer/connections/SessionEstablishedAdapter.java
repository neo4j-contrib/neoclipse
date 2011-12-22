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
package net.sourceforge.sqlexplorer.connections;

import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.dbproduct.User;

/**
 * Stubbed out implementation of SessionEstablishedListener
 * @author John Spackman
 */
public class SessionEstablishedAdapter implements SessionEstablishedListener {

	/* (non-Javadoc)
	 * @see net.sourceforge.sqlexplorer.connections.SessionEstablishedListener#cannotEstablishSession(net.sourceforge.sqlexplorer.dbproduct.User)
	 */
	public void cannotEstablishSession(User user) {
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.sqlexplorer.connections.SessionEstablishedListener#sessionEstablished(net.sourceforge.sqlexplorer.dbproduct.Session)
	 */
	public void sessionEstablished(Session session) {
	}
}
