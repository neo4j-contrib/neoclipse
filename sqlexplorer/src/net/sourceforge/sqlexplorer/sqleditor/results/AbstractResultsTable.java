package net.sourceforge.sqlexplorer.sqleditor.results;

import net.sourceforge.sqlexplorer.sqleditor.results.actions.CopyCellAction;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * The ResultsTable class is instantiated when code needs to display some kind of tabular
 * results, either a Table or a TreeTable (in Eclipse 3.3 this will become just a Table
 * because of more advanced Tree support).
 * 
 */
public abstract class AbstractResultsTable implements KeyListener {
	
	public enum SelectionType {
		CELL, ROW, COLUMN, SELECTION, ENTIRE_TABLE
	}
	
	public class MyTableLabelProvider extends TableLabelProviderAdapter {
    	@Override
        public String getColumnText(Object element, int colIndex) {
            CellRangeRow row = (CellRangeRow) element;
            Object value = row.getCellValue(colIndex);
            return getResultProvider().getColumn(colIndex).getDisplayValue(value);
        }
    };
	
    private static final int CTRL_C = 3;
    private static final int CTRL_F = 6;
    
	// The parent composite to add controls to (the TabItem's one and only control) 
	private Composite parent;
	
	// Where child controls are added to; excludes the statusBar, may be the same as
	//	"parent" (eg if there is no status bar)
	private Composite controlParent;
	
	// Status bar parent and the label for displaying the status message
	private Composite statusBar;
	private Label statusLabel;
	
	// Popup context menu; created on demand
	private MenuManager menuManager;

	// Whether a status bar is present
	private boolean hasStatusBar;
	
	// What the status message is
	private String statusMessage;
	
	// Popup for find (Ctrl-F)
	private Shell findPopup;
	
	// Where we get the result data from
	private ResultProvider resultProvider;

	/**
	 * Constructor
	 * @param resultProvider The ResultProvider for the table contents
	 */
	public AbstractResultsTable(ResultProvider resultProvider) {
		super();
		this.resultProvider = resultProvider;
	}
	
	public MenuManager getMenuManager() {
		if (menuManager == null)
			menuManager = createMenuManager();
		return menuManager;
	}
	
	public abstract MenuManager createMenuManager();

	/**
	 * Creates the control parent, status bar parent, etc
	 * @param parent
	 * @return The Composite which was created 
	 */
	public Composite createControls(Composite container) {
		
		// Create a composite to add all controls to
		parent = new Composite(container, SWT.NONE);

		if (isHasStatusBar()) {
			parent.setLayout(new GridLayout(1, true));
			
			controlParent = new Composite(parent, SWT.NONE);
	        GridData gd = new GridData();
	        gd.grabExcessHorizontalSpace = true;
	        gd.grabExcessVerticalSpace = true;
	        gd.horizontalAlignment = SWT.FILL;
	        gd.verticalAlignment = SWT.FILL;
	        controlParent.setLayoutData(gd);
	        
	        statusBar = createStatusBar(parent);
//			Composite statusBar = new Composite(parent, SWT.NONE);
	        gd = new GridData();
	        gd.grabExcessHorizontalSpace = true;
	        gd.horizontalAlignment = SWT.FILL;
	        statusBar.setLayoutData(gd);
		} else {
			controlParent = parent;
		}
		controlParent.setLayout(new FillLayout());
		createResultsTable(controlParent);
		controlParent.layout();
		controlParent.redraw();
		
		return parent;
	}
	
	protected Composite createStatusBar(Composite parent) {
		Composite statusBar = new Composite(parent, SWT.NONE);
		statusBar.setLayout(new FillLayout());
		
        // add status bar labels
        statusLabel = new Label(statusBar, SWT.NONE);
        if (getStatusMessage() != null)
        	statusLabel.setText(getStatusMessage());
		return statusBar;
	}
	
	protected void createResultsTable(Composite parent) {
		
	}

	public abstract CellRange getSelection(SelectionType selection);
	
	public void copyToClipboard() {
		new CopyCellAction().run();
	}

	public Shell createFindPopup() {
		return null;
	}
	
	public void disposeFindPopup() {
		if (findPopup != null) {
			findPopup.dispose();
			findPopup = null;
		}
	}
	
	public void refreshContent() {
		// Nothing
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
	 */
	public void keyPressed(KeyEvent e) {
        switch (e.character) {
		case CTRL_C:
			copyToClipboard();
			break;

		case CTRL_F:
			if (findPopup == null || findPopup.isDisposed())
				findPopup = createFindPopup();
			if (!findPopup.isVisible())
				findPopup.open();
			findPopup.forceActive();
			break;

		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
	 */
	public void keyReleased(KeyEvent e) {
        switch (e.keyCode) {

		case SWT.F5:
			refreshContent();
			break;

		case SWT.ESC:
			disposeFindPopup();
			break;

		}
	}

	/**
	 * @return the hasStatusBar
	 */
	public boolean isHasStatusBar() {
		return hasStatusBar;
	}

	/**
	 * @param hasStatusBar the hasStatusBar to set
	 */
	public void setHasStatusBar(boolean hasStatusBar) {
		if (controlParent != null)
			throw new IllegalStateException("Cannot enable/disable status bar because controls have already been created");
		this.hasStatusBar = hasStatusBar;
	}

	/**
	 * @return the statusMessage
	 */
	public String getStatusMessage() {
		return statusMessage;
	}

	/**
	 * @param text the statusMessage to set
	 */
	public void setStatusMessage(String text) {
		if (statusLabel != null)
			statusLabel.setText(text);
		this.statusMessage = text;
	}

	/**
	 * @return the statusBar
	 */
	public Composite getStatusBar() {
		return statusBar;
	}

	public ResultProvider getResultProvider() {
		return resultProvider;
	}

	public void setResultProvider(ResultProvider resultProvider) {
		this.resultProvider = resultProvider;
	}
	
}