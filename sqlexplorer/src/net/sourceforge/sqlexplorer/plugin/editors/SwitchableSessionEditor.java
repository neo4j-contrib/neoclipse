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

import org.eclipse.ui.IWorkbenchSite;

import net.sourceforge.sqlexplorer.dbproduct.Session;

/**
 * Interface implemented by editors which want to support SQLEditorSessionSwitcher
 * @author John Spackman
 *
 */
public interface SwitchableSessionEditor {

	
	/**
	 * Returns the current session
	 * @return
	 */
	public Session getSession();

	/**
	 * Called when the session changes (including if the session failed).  This is always
	 * called in the main Workspace thread.
	 * @param session the new session; null if the session could not be established
	 */
	public void setSession(Session session);
	
	public IWorkbenchSite getSite();
	
	public void refreshToolbars();
}
