package net.sourceforge.sqlexplorer.sqleditor.results;

import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;

/**
 * An EditorResultsTab ties a CTabItem below the SQLEditor to a Results instance.
 * 
 * The ResultsTab class is instantiated when a new Tab for result sets is created;
 * it exists only to return multiple pieces to the calling code.
 * 
 * NOTE: The TabItem is created, its title is the 1-based index of the tab, and
 * its data has been set to the AbstractSQLExecution but it has no other configuration - 
 * IE the title is not exactly descriptive, there's no tooltip, etc.  Similarly, the
 * parent composite has been created but is completely empty.
 * 
 * The tab does not need to cater for life cycle events or controls - because we're using
 * CTabFolder, the tabs have their own "X" to close/terminate individual query, and the
 * SQLEditor takes care of notifying the query to shutdown.  IE, the parent composite is
 * *just* for result set display.
 * @author John Spackman
 *
 */
public class EditorResultsTab {

	// The TabItem for the results
	private CTabItem tabItem;
	
	// The Results
	private AbstractResultsTable results;
	
	public EditorResultsTab(CTabItem tabItem, AbstractResultsTable results) {
		super();
		this.tabItem = tabItem;
		this.results = results;
		Composite composite = results.createControls(tabItem.getParent());
		if(tabItem.getControl() != null)
		{
			tabItem.getControl().dispose();
		}
		tabItem.setControl(composite);
	}
	
	public Composite getComposite() {
		return (Composite)tabItem.getControl();
	}

	/**
	 * Sets the title of the Tab
	 * @param title
	 */
	public void setTabTitle(String title) {
		tabItem.setText(title);
	}
	
	/**
	 * Returns the current title of the tab
	 * @return
	 */
	public String getTabTitle() {
		return tabItem.getText();
	}

	/**
	 * Returns the Results
	 * @return
	 */
	public AbstractResultsTable getResults() {
		return results;
	}
}
