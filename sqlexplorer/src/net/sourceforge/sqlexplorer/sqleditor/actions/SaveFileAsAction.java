package net.sourceforge.sqlexplorer.sqleditor.actions;

/*
 * Copyright (C) 2002-2004 Andrea Mazzolini
 * andreamazzolini@users.sourceforge.net
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

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.util.ImageUtil;

import org.eclipse.jface.resource.ImageDescriptor;

public class SaveFileAsAction extends AbstractEditorAction {

    private ImageDescriptor _image = ImageUtil.getDescriptor("Images.SaveFileAsIcon");

    public SaveFileAsAction(SQLEditor editor) {
		super(editor);
	}

	public ImageDescriptor getImageDescriptor() {
        return _image;
    }

    public String getText() {
        return Messages.getString("SQLEditor.Actions.Save");
    }

    public boolean isEnabled() {
        return true;
    }

    public String getToolTipText() {
        return Messages.getString("SQLEditor.Actions.SaveToolTip"); //$NON-NLS-1$
    }

    public void run() {
        _editor.doSave(false, null);
    };
}
