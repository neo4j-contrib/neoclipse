package net.sourceforge.sqlexplorer.filelist;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.connections.ConnectionsView;
import net.sourceforge.sqlexplorer.connections.SessionEstablishedAdapter;
import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.dbproduct.User;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.SwitchableSessionEditor;
import net.sourceforge.sqlexplorer.sqleditor.actions.SQLEditorSessionSwitcher;
import net.sourceforge.sqlexplorer.util.ImageUtil;
import net.sourceforge.sqlexplorer.util.PartAdapter2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

public class FileListEditor extends EditorPart implements SwitchableSessionEditor {
	
    private static final Log _logger = LogFactory.getLog(BatchJob.class);
    
	private TextEditor editor;
	private Session session;
	private Text messagesText;
	
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());

		// Create a wrapper for our stuff
		final Composite myParent = new Composite(parent, SWT.NONE);
		FormLayout layout = new FormLayout();
		myParent.setLayout(layout);
		FormData data;

		// Create the toolbar and attach it to the top of the composite
		ToolBar toolBar = new ToolBar(myParent, SWT.NONE);
		data = new FormData();
		data.top = new FormAttachment(0, 0);
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(100, 0);
		toolBar.setLayoutData(data);
		
		ToolBarManager mgr = new ToolBarManager(toolBar);
		mgr.add(new Action(Messages.getString("FileListEditor.Actions.Execute"), ImageUtil.getDescriptor("Images.ExecSQLIcon")) {
			@Override
			public void run() {
				execute();
			}

			@Override
			public String getToolTipText() {
				return Messages.getString("FileListEditor.Actions.Execute.ToolTip");
			}
		});
		
        SQLEditorSessionSwitcher sessionSwitcher = new SQLEditorSessionSwitcher(this);
        mgr.add(sessionSwitcher);
        
		mgr.update(true);
		
		// Create sash and attach it to 75% of the way down
		final Sash sash = createSash(myParent);
		data = new FormData();
		data.top = new FormAttachment(75, 0);
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(100, 0);
		sash.setLayoutData(data);

		// Attach the tabs to the bottom of the sash and the bottom of the composite
		CTabFolder tabFolder = createResultTabs(myParent);
		data = new FormData();
		data.top = new FormAttachment(sash, 0);
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(100, 0);
		data.bottom = new FormAttachment(100, 0);
		tabFolder.setLayoutData(data);
		
		// Attach the editor to the toolbar and the top of the sash
		Composite editorParent = new Composite(myParent, SWT.NONE);
		editorParent.setLayout(new FillLayout());
		editor.createPartControl(editorParent);
		data = new FormData();
		data.top = new FormAttachment(toolBar, 0);
		data.bottom = new FormAttachment(sash, 0);
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(100, 0);
		editorParent.setLayoutData(data);
	}

	/**
	 * Creates the results tabs in the bottom half
	 * 
	 * @param parent
	 * @return
	 */
	private CTabFolder createResultTabs(Composite parent) {
		CTabFolder tabFolder = new CTabFolder(parent, SWT.TOP | SWT.CLOSE);
		tabFolder.setBorderVisible(true);
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		// Set up a gradient background for the selected tab
		Display display = getSite().getShell().getDisplay();
	    tabFolder.setSelectionBackground(
	    		new Color[] {
				        display.getSystemColor(SWT.COLOR_WHITE),
		                new Color(null, 211, 225, 250),
		                new Color(null, 175, 201, 246),
		                IConstants.TAB_BORDER_COLOR
	    		},
	    		new int[] {25, 50, 75},
	    		true
	    	);

	    
		CTabItem messagesTab = new CTabItem(tabFolder, SWT.NONE);
		messagesTab.setText(Messages
				.getString("FileListEditor.Messages.Caption"));

		messagesText = new Text(tabFolder, SWT.READ_ONLY | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		Font f = JFaceResources.getFontRegistry().get(JFaceResources.TEXT_FONT);
		messagesText.setFont(f);
		messagesTab.setControl(messagesText);
		tabFolder.setSelection(messagesTab);
	    
		return tabFolder;
	}

	/**
	 * Creates the sash (the draggable splitter) between the editor and the
	 * results tab
	 * 
	 * @param parent
	 * @return
	 */
	private Sash createSash(Composite parent) {
		// Create the sash and put it in the middle
		final Sash sash = new Sash(parent, SWT.HORIZONTAL);
		sash.setBackground(IConstants.TAB_BORDER_COLOR);

		sash.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				FormData data = (FormData)sash.getLayoutData();
				Rectangle rect = sash.getParent().getBounds();
				data.top = new FormAttachment(e.y, rect.height, 0);
				sash.getParent().layout();
				sash.getParent().getParent().layout();
			}
		});

		return sash;
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		// Configure the editor
		setSite(site);
		setInput(input);

		// Create the text editor 
		editor = new TextEditor();
		editor.init(site, input);
		
		// Make sure we get notification that our editor is closing because
		//	we may need to stop running queries
		getSite().getPage().addPartListener(new PartAdapter2() {

			/* (non-JavaDoc)
			 * @see net.sourceforge.sqlexplorer.util.PartAdapter2#partClosed(org.eclipse.ui.IWorkbenchPartReference)
			 */
			public void partClosed(IWorkbenchPartReference partRef) {
				if (partRef.getPart(false) == FileListEditor.this) {
					onCloseEditor();
				}
			}
		});

		// If we havn't got a view, then try for the current session in the ConnectionsView
		if (getSession() == null) {
	        ConnectionsView view = SQLExplorerPlugin.getDefault().getConnectionsView();
	        if (view != null) {
        		User user = view.getDefaultUser();
    			if (user != null)
    				user.queueForNewSession(new SessionEstablishedAdapter() {
						@Override
						public void sessionEstablished(Session session) {
							setSession(session);
						}
	        		});
	        }
		}
	}
	
	/**
	 * Adds a message to the message window
	 * @param message
	 */
	public void addMessage(final String msg) {
		if (messagesText.isDisposed())
			return;
		
        editor.getEditorSite().getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				messagesText.append(msg + "\r\n");
			}
        });
	}
	
	/**
	 * Called internally when the user tries to close the editor
	 */
	private void onCloseEditor() {
		editor.getDocumentProvider().disconnect(getEditorInput());
		editor.setInput(null);
	}

	/**
	 * @return the session
	 */
	public Session getSession() {
		// In theory, if our session is somehow closed by something else then we will have already
		//	had our session changed or reset; however, just in case this doesn't happen we can
		//	detect it because the session has it's user set to null when it is detached.  If that
		//	happened, then we reset the session to null
		if (session != null && session.getUser() == null)
			session = null;
		return session;
	}

	/**
	 * Sets the session to use for executing queries
	 * 
	 * @param session The new Session
	 */
	public void setSession(Session session) {
		if (session == this.session)
			return;
		
		// If we already have a session and we're changing to a different one, close the current one
		if (getSession() != null && session != this.session)
			this.session.close();
		this.session = session;
		_logger.fatal("Session set to " + session);
	}
	
	@Override
	public boolean isSaveAsAllowed() {
		return editor.isSaveAsAllowed();
	}

	@Override
	public boolean isDirty() {
		return editor.isDirty();
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		editor.doSave(monitor);
	}

	@Override
	public void doSaveAs() {
		editor.doSaveAs();
	}

	@Override
	public void setFocus() {
		editor.setFocus();
	}
	
	protected void execute() {
		messagesText.setText("");
		IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		File baseDir = null;
		IEditorInput input = editor.getEditorInput();
		if(input instanceof IPathEditorInput)
		{
			baseDir = ((IPathEditorInput) input).getPath().toFile().getParentFile();
		}
		String str = doc.get();
		BufferedReader reader = new BufferedReader(new StringReader(str));
		LinkedList<File> files = new LinkedList<File>();
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.length() < 1)
					continue;
				File file = new File(line);
				if(!file.exists() && !file.isAbsolute() && !file.getPath().startsWith(File.separator))
				{
					// make absolute path from base directory
					file = new File(baseDir, file.getPath());
				}
				if (!file.exists() || !file.canRead())
					addMessage("Cannot locate/read file " + file.getAbsolutePath());
				else
					files.add(file);
			}
		}catch(IOException e) {
			SQLExplorerPlugin.error(e);
		}
		if (files.isEmpty())
			return;
		
        final BatchJob bgJob = new BatchJob(this, getSession().getUser(), files);
        
        editor.getEditorSite().getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
		        IWorkbenchSiteProgressService siteps = (IWorkbenchSiteProgressService) editor.getEditorSite().getAdapter(IWorkbenchSiteProgressService.class);
		        siteps.showInDialog(editor.getEditorSite().getShell(), bgJob);
		        bgJob.schedule();
			}
        });
	}

	public void refreshToolbars() {
	}
	
}
