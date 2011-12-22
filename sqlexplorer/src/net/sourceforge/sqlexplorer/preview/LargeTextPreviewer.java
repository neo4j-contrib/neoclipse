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
package net.sourceforge.sqlexplorer.preview;

import java.io.Reader;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dataset.LargeTextDataType;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * Implements a previewer for large text (CLOB).  The reason that this is not just
 * implemented as a String is because that would require every CLOB to be loaded
 * into the JVM when the query is retrieved, whereas this allows us to only load
 * one at a time.
 * @author John Spackman
 *
 */
public class LargeTextPreviewer implements Previewer {
	
	// Largest amount of text that we're prepared to read
	private static final int MAX_TEXT_SIZE = 64 * 1024;

	/* (non-JavaDoc)
	 * @see net.sourceforge.sqlexplorer.preview.Previewer#createControls(org.eclipse.swt.widgets.Composite, java.lang.Object)
	 */
	public void createControls(final Composite parent, Object obj) {
		if (obj == null)
			return;
		String content = null;
		
		if (obj instanceof LargeTextDataType) {
			LargeTextDataType data = (LargeTextDataType)obj;
			try {
				Reader reader = data.getTextData();
				char[] buffer = new char[4096];
				StringBuffer result = new StringBuffer();
				int pos;
				int length = 0;
				while ((pos = reader.read(buffer)) > -1) {
					result.append(buffer);
					length += pos;
					if (length > MAX_TEXT_SIZE) {
						parent.getDisplay().asyncExec(new Runnable() {
							public void run() {
								MessageDialog.openWarning(parent.getShell(), 
										Messages.getString("LargeTextPreviewer.TooMuchData.Title"), 
										Messages.getString("LargeTextPreviewer.TooMuchData.Message"));
							}
						});
						break;
					}
				}
				content = result.toString();
			}catch(Exception e) {
				SQLExplorerPlugin.error("Cannot read CLOB", e);
				return;
			}
		} else
			content = obj.toString();
		
		if (content == null)
			return;
		
		Text text = new Text(parent, SWT.NONE);
		text.setEditable(false);
		text.setText(content);
	}

	public void dispose() {
	}
}
