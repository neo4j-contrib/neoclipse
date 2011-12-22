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

import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.util.ImageUtil;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * Executes the given command
 * 
 * @modified John Spackman
 *
 */
public class CommandAction extends AbstractEditorAction {
	
	private String commandId;

	public CommandAction(String pCommandId)
	{
		this.commandId = pCommandId;
	}
    private ImageDescriptor img = null;
    
    public CommandAction(SQLEditor editor, String pCommandId) {
		super(editor);
		this.commandId = pCommandId;
	}

	public ImageDescriptor getImageDescriptor() {
		if(img == null)
		{
			img = ImageUtil.getDescriptorByKey(SQLExplorerPlugin.getString(commandId+".icon")); //$NON-NLS-1$			
		}
        return img;
    }

    public String getText() {
        return SQLExplorerPlugin.getString(commandId+".text"); //$NON-NLS-1$
    }

    public String getToolTipText() {
        return SQLExplorerPlugin.getString(commandId+".tooltip"); //$NON-NLS-1$
    }
    
    public void run() {
    	runCommand(commandId);
    }

    protected void runCommand(String pComandId)
    {
		IHandlerService handlerService = (IHandlerService) _editor.getSite().getService(IHandlerService.class);
		try {
			handlerService.executeCommand(pComandId, null);
		} catch (Exception ex) {
			throw new RuntimeException(pComandId +" not found");
		}
    	
    }

}
