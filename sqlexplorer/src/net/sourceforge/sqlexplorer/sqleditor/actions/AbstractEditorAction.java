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
package net.sourceforge.sqlexplorer.sqleditor.actions;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;


/**
 * Abstract implementation for a sql editor actions.
 * Extend this class to add new actions to the sql editor.
 * 
 * @author Davy Vanherbergen
 *
 */
public abstract class AbstractEditorAction extends Action {
   
    protected SQLEditor _editor;
    
    public AbstractEditorAction(SQLEditor editor) {
		super();
		this._editor = editor;
	}

    public AbstractEditorAction() {
		super();
	}

	public abstract String getText();
    
    public String getToolTipText() {
        return getText();
    }
    
    public abstract void run();
    
    public final void setEditor(SQLEditor editor) {
        _editor = editor;
    }

    public boolean isDisabled() {
    	Session session = _editor.getSession();
        return session == null || session.isConnectionInUse();
    }
    
    protected Session getSession() {
    	Session session = _editor.getSession();
        if (session != null && session.isConnectionInUse()) {
            _editor.getSite().getShell().getDisplay().asyncExec(new Runnable() {
                public void run() {
                    MessageDialog.openError(_editor.getSite().getShell(), Messages.getString("SQLResultsView.Error.InUseTitle"), Messages.getString("SQLResultsView.Error.InUse"));
                }
            });
            return null;
        }
        return session;
    }
}
