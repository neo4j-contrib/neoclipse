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

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.sessiontree.model.utility.Dictionary;
import net.sourceforge.sqlexplorer.sqleditor.SQLTextViewer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

/**
 * TextEditor specialization; encapsulates functionality specific to editing
 * SQL.
 * 
 * Virtually all of this code came from SQLEditor, which used to be derived
 * directly from TextEditor; SQLEditor now combines the text editor (here,
 * SQLTextEditor) and the result and messages panes in a single editor, hence
 * this was separated out for clarity
 * 
 * Note that MouseClickListener was also moved to a top-level, package-private
 * class for readability
 * 
 * @modified John Spackman
 * 
 */
public class SQLTextEditor extends TextEditor {
	private static final String CONTEXT_ID = "net.sourceforge.sqlexplorer.sqlEditorScope";
	
	private SQLEditor editor;

	private MouseClickListener mcl;

	private IPartListener partListener;

	/* package */SQLTextViewer sqlTextViewer;

	private boolean _enableContentAssist = SQLExplorerPlugin.getBooleanPref(IConstants.SQL_ASSIST);

	private IPreferenceStore store;

	public SQLTextEditor(SQLEditor editor) {
		super();
		this.editor = editor;
		mcl = new MouseClickListener(editor);
		store = SQLExplorerPlugin.getDefault().getPreferenceStore();
	}

	public SQLEditor getEditor()
	{
		return this.editor;
	}

	@Override
	public IProgressMonitor getProgressMonitor() {
		return super.getProgressMonitor();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#createActions()
	 */
	protected void createActions() {

		super.createActions();

		if (!_enableContentAssist) {
			return;
		}

		Action action = new Action("Auto-Completion") {

			public void run() {
				sqlTextViewer.showAssistance();
			}
		};

		// This action definition is associated with the accelerator Ctrl+Space
		action
				.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		setAction("ContentAssistProposal", action);

	}

	@Override
	protected void initializeKeyBindingScopes() {
		setKeyBindingScopes(new String[]{CONTEXT_ID});
	}

	public void createPartControl(Composite parent) {

		super.createPartControl(parent);

		PlatformUI.getWorkbench().getHelpSystem().setHelp(
				getSourceViewer().getTextWidget(),
				SQLExplorerPlugin.PLUGIN_ID + ".SQLEditor");


		Object adapter = getAdapter(org.eclipse.swt.widgets.Control.class);
		if (adapter instanceof StyledText) {
			StyledText text = (StyledText) adapter;
			text.setWordWrap(SQLExplorerPlugin.getBooleanPref(IConstants.WORD_WRAP));
			
	        FontData[] fData = PreferenceConverter.getFontDataArray(store, IConstants.FONT);
	        if (fData.length > 0) {
	            JFaceResources.getFontRegistry().put(fData[0].toString(), fData);
	            text.setFont(JFaceResources.getFontRegistry().get(fData[0].toString()));
	        }

		}
	}

	protected ISourceViewer createSourceViewer(final Composite parent,
			IVerticalRuler ruler, int style) {
		
		parent.setLayout(new FillLayout());
		final Composite myParent = new Composite(parent, SWT.NONE);
		
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = layout.horizontalSpacing = layout.verticalSpacing = 0;
		myParent.setLayout(layout);

		// create divider line

		Composite div1 = new Composite(myParent, SWT.NONE);
		GridData lgid = new GridData();
		lgid.grabExcessHorizontalSpace = true;
		lgid.horizontalAlignment = GridData.FILL;
		lgid.heightHint = 1;
		lgid.verticalIndent = 1;
		div1.setLayoutData(lgid);
		div1.setBackground(editor.getSite().getShell().getDisplay()
				.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));

		// create text viewer

		GridData gid = new GridData();
		gid.grabExcessHorizontalSpace = gid.grabExcessVerticalSpace = true;
		gid.horizontalAlignment = gid.verticalAlignment = GridData.FILL;

		Dictionary dictionary = null;
		if (editor.getSession() != null && _enableContentAssist) {
			dictionary = editor.getSession().getUser().getMetaDataSession().getDictionary();
		}
		sqlTextViewer = new SQLTextViewer(myParent, style, store, dictionary,
				ruler);
		sqlTextViewer.getControl().setLayoutData(gid);

		// create bottom divider line

		Composite div2 = new Composite(myParent, SWT.NONE);
		lgid = new GridData();
		lgid.grabExcessHorizontalSpace = true;
		lgid.horizontalAlignment = GridData.FILL;
		lgid.heightHint = 1;
		lgid.verticalIndent = 0;
		div2.setLayoutData(lgid);
		div2.setBackground(editor.getSite().getShell().getDisplay()
				.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));

		sqlTextViewer.getTextWidget().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				SQLTextEditor.this.editor.getEditorSite().getPage().activate(
						SQLTextEditor.this.editor.getEditorSite().getPart());
			}
		});

		myParent.layout();

		IDocument dc = new Document();
		sqlTextViewer.setDocument(dc);

		mcl.install(sqlTextViewer);

		return sqlTextViewer;
	}

	public void setNewDictionary(final Dictionary dictionary) {
		if (editor.getSite() != null && editor.getSite().getShell() != null && editor.getSite().getShell().getDisplay() != null)
			editor.getSite().getShell().getDisplay().asyncExec(new Runnable() {
	
				public void run() {
	
					if (sqlTextViewer != null) {
						sqlTextViewer.setNewDictionary(dictionary);
//						if (editor.getSession() != null) {
//							sqlTextViewer.refresh();
//						}
					}
	
				}
			});
	}

	public void onEditorSessionChanged(Session session) {
		if (session != null && _enableContentAssist) {
			setNewDictionary(editor.getSession().getUser().getMetaDataSession().getDictionary());
		} else {
			setNewDictionary(null);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		if (partListener != null)
			editor.getEditorSite().getPage().removePartListener(partListener);
		mcl.uninstall();
		super.dispose();
	}

	ISourceViewer getViewer() {
		return getSourceViewer();
	}
}
