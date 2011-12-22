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
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.util.ImageUtil;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * @author Davy Vanherbergen
 * 
 */
public class RollbackAction extends AbstractEditorAction {

    private ImageDescriptor _image = ImageUtil.getDescriptor("Images.EditorRollbackIcon");

	public RollbackAction(SQLEditor editor) {
		super(editor);
	}

	public ImageDescriptor getImageDescriptor() {
        return _image;
    }

    public String getText() {

        return Messages.getString("SQLEditor.Actions.RollBack");
    }


    public String getToolTipText() {

        return Messages.getString("SQLEditor.Actions.RollBack.ToolTip");
    }


    /**
     * Action is available when session doesn't have autocommit
     * 
     */
    public boolean isDisabled() {

        if (_editor.getSession() == null) {
            return true;
        }

        return _editor.getSession().isAutoCommit();
    }


    public void run() {

        try {

            _editor.getSession().rollback();
            _editor.setMessage(Messages.getString("SQLEditor.Actions.RollBack.Success"));
            
        } catch (final Exception e) {

            _editor.getSite().getShell().getDisplay().asyncExec(new Runnable() {

                public void run() {

                    MessageDialog.openError(_editor.getSite().getShell(),
                            Messages.getString("SQLEditor.Actions.RollBack.Error.Title"), e.getMessage());
                }
            });
        }

    }
}
