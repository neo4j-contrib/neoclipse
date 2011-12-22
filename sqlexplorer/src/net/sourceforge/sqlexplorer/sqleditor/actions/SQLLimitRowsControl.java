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

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;

import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * Class to add a "Limit Rows?" checkbox for the toolbar of SQLEditor;  if checked,
 * the user is able to key in a number into a text field (default value is 100).
 * @modified John Spackman
 *
 */
public class SQLLimitRowsControl extends ControlContribution {
	
	// The SQLEditor we belong to
    @SuppressWarnings("unused")
	private SQLEditor editor;

	// The checkbox in the status bar which says whether to restrict results
	private Button limitResults;

	// How many rows to restrict the results to
	private Text maxResultField;

	/**
	 * Constructor
	 * @param editor the SQLEditor we're attached to
	 */
	public SQLLimitRowsControl(SQLEditor editor) {
		super(SQLLimitRowsControl.class.getName());
		this.editor = editor;
	}

	/* (non-JavaDoc)
	 * @see org.eclipse.jface.action.ControlContribution#createControl(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createControl(Composite parent) {
		// create bottom status bar
		Composite statusBar = new Composite(parent, SWT.NULL);

		GridLayout statusBarLayout = new GridLayout();
		statusBarLayout.numColumns = 3;
		statusBarLayout.verticalSpacing = 0;
		statusBarLayout.marginHeight = 0;
		statusBarLayout.marginWidth = 0;
		statusBarLayout.marginTop = 0;
		statusBarLayout.marginBottom = 0;
		statusBarLayout.marginRight = 5;
		statusBarLayout.horizontalSpacing = 5;
		statusBarLayout.verticalSpacing = 0;

		statusBar.setLayout(statusBarLayout);

		GridData statusBarGridData = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
		statusBarGridData.verticalIndent = 0;
		statusBarGridData.horizontalIndent = 0;
		statusBar.setLayoutData(statusBarGridData);

		// add status line manager

		StatusLineManager statusMgr = new StatusLineManager();
		statusMgr.createControl(statusBar);

		GridData c1Grid = new GridData();
		c1Grid.horizontalAlignment = SWT.FILL;
		c1Grid.verticalAlignment = SWT.BOTTOM;
		c1Grid.grabExcessHorizontalSpace = true;
		c1Grid.grabExcessVerticalSpace = false;
		statusMgr.getControl().setLayoutData(c1Grid);

		// add checkbox for limiting results

		GridData c2Grid = new GridData();
		c2Grid.horizontalAlignment = SWT.RIGHT;
		c2Grid.verticalAlignment = SWT.CENTER;
		c2Grid.grabExcessHorizontalSpace = false;
		c2Grid.grabExcessVerticalSpace = false;

		limitResults = new Button(statusBar, SWT.CHECK);
		limitResults.setText(Messages.getString("SQLEditor.LimitRows"));
		limitResults.setSelection(true);
		limitResults.setLayoutData(c2Grid);

		// add input field for result limit

		GridData c3Grid = new GridData();
		c3Grid.horizontalAlignment = SWT.RIGHT;
		c3Grid.verticalAlignment = SWT.CENTER;
		c3Grid.grabExcessHorizontalSpace = false;
		c3Grid.grabExcessVerticalSpace = false;
		c3Grid.widthHint = 30;

		maxResultField = new Text(statusBar, SWT.BORDER | SWT.SINGLE);
		maxResultField.setText(SQLExplorerPlugin.getDefault().getPreferenceStore().getString(IConstants.MAX_SQL_ROWS));
		maxResultField.setLayoutData(c3Grid);

		limitResults.addMouseListener(new MouseAdapter() {

			// enable/disable input field when checkbox is clicked
			public void mouseUp(MouseEvent e) {

				maxResultField.setEnabled(limitResults.getSelection());
			}
		});

		statusBar.layout();
		return statusBar;
	}

	/**
	 * Returns whether to limit the results and if so by how much.
	 * 
	 * @return the maximum number of rows to retrieve, 0 for unlimited, or null
	 *         if it cannot be interpretted
	 */
	public Integer getLimitResults() {
		if (!limitResults.getSelection())
			return new Integer(0);
		try {
			return new Integer(Integer.parseInt(maxResultField.getText()));
		} catch (NumberFormatException e) {
			// Nothing
		} catch (NullPointerException e) {
			// Nothing
		}

		return null;
	}

}
