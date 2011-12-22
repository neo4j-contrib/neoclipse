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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.util.ImageUtil;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

public class OpenFileAction extends AbstractEditorAction {

    private ImageDescriptor img = ImageUtil.getDescriptor("Images.OpenFileIcon");

    public OpenFileAction(SQLEditor editor) {
		super(editor);
	}

	public String getText() {
        return Messages.getString("Open_1"); //$NON-NLS-1$
    }

    public boolean isEnabled() {
        return true;
    }

    public void run() {

        FileDialog dlg = new FileDialog(_editor.getSite().getShell(), SWT.OPEN | SWT.MULTI);

        dlg.setFilterExtensions(new String[] {"*.sql;*.txt"});

        String path = dlg.open();
        if (path != null) {
            String[] files = dlg.getFileNames();
            loadFiles(files, dlg.getFilterPath());
        }

    }


    /**
     * Load one or more files into the editor.
     * 
     * @param files string[] of relative file paths
     * @param filePath path where all files are found
     */
    public void loadFiles(String[] files, String filePath) {

        BufferedReader reader = null;

        try {

            StringBuffer all = new StringBuffer();
            String str = null;
            //String delimiter = _editor.getSqlTextViewer().getTextWidget().getLineDelimiter();
            
            /*
             * Note: I have changed the delimiter to a hardcoded \n because this a) allows the
             * interface to SQLEditor to be cleaner (see SQLEditor for refactoring description)
             * and I can find several other places where text will be passed to the same text 
             * editor and \n is hard coded.  If there is an issue with how the view encodes
             * line delimiters, it is likely to be a global problem and we should handle it in 
             * SQLEditor.setText() instead.
             * 
             */

            for (int i = 0; i < files.length; i++) {

                String path = "";
                if (filePath != null) {
                    path += filePath + File.separator;
                }
                path += files[i];

                reader = new BufferedReader(new FileReader(path));

                while ((str = reader.readLine()) != null) {
                    all.append(str);
                    all.append('\n');
                }

                if (files.length > 1) {
                    all.append('\n');
                }
            }

            _editor.setText(all.toString());

        } catch (Throwable e) {
            SQLExplorerPlugin.error("Error loading document", e);

        } finally {
            try {
                reader.close();
            } catch (java.io.IOException e) {
                // noop
            }
        }

    }
    
    
    public String getToolTipText() {
        return Messages.getString("Open_2"); 
    }


    public ImageDescriptor getHoverImageDescriptor() {
        return img;
    }


    public ImageDescriptor getImageDescriptor() {
        return img;
    };
}
