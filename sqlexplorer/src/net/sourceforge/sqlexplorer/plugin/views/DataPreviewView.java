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
package net.sourceforge.sqlexplorer.plugin.views;

import net.sourceforge.sqlexplorer.ExplorerException;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.preview.Previewer;
import net.sourceforge.sqlexplorer.preview.PreviewerFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

/**
 * The DataPreview provides a read-only view of data which would not otherwise
 * fit comfortably in the results tabs, e.g. an XML tree or an image.
 * 
 * It has fairly limited functionality, it's task is to manipulate instances of
 * classes which implement net.sourceforge.sqlexplorer.preview.Previewer.
 * 
 * @see net.sourceforge.sqlexplorer.preview.Previewer
 * @see net.sourceforge.sqlexplorer.preview.PreviewerFactory
 * @author John Spackman
 */
public class DataPreviewView extends ViewPart {
	
	private Composite parent;
	private Previewer previewer;
	private Object object;
	
	public void previewData(String mimeType, Object object) throws ExplorerException {
		if (previewer != null)
			previewer.dispose();
		previewer = PreviewerFactory.getInstance().getInstance(mimeType, object);
		this.object = object;
		displayPreviewer();
	}
	
	private void displayPreviewer() throws ExplorerException {
		Control[] children = parent.getChildren();
		for (Control control : children)
			control.dispose();
		if (previewer != null && object != null)
			previewer.createControls(parent, object);
		else {
			parent.setLayout(new FillLayout());
			Text text = new Text(parent, SWT.NONE);
			text.setEditable(false);
			if (object != null)
				text.setText(object.toString());
			else
				text.setText(Messages.getString("DataPreviewView.NothingToDisplay"));
			parent.layout();
		}
	}

	/* (non-JavaDoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		if (previewer != null) {
			previewer.dispose();
			previewer = null;
		}
		super.dispose();
	}

	/* (non-JavaDoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
//		this.parent = new Composite(parent, SWT.NULL);
		this.parent = parent;
		this.parent.setLayout(new FillLayout());
		try {
			displayPreviewer();
		}catch(ExplorerException e) {
    		SQLExplorerPlugin.error(e.getMessage(), e);
//			Control[] children = parent.getChildren();
//			for (Control control : children)
//				control.dispose();
//			parent.setLayout(new FillLayout());
//			Text text = new Text(parent, SWT.NONE);
//			text.setEditable(false);
//			text.setText(e.getMessage());
//			parent.layout();
		}
	}

	/* (non-JavaDoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
	}

}
