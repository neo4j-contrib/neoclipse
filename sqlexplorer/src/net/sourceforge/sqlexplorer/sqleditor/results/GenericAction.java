package net.sourceforge.sqlexplorer.sqleditor.results;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;

public class GenericAction extends Action {
    
    public GenericAction() {
		super();
	}

	public GenericAction(String text, ImageDescriptor image) {
		super(text, image);
	}

	public GenericAction(String text, int style) {
		super(text, style);
	}

	public GenericAction(String text) {
		super(text);
	}
	
	/** 
	 * Called immediately after construction
	 * @param shell
	 */
	public void initialise(Shell shell) {
		// Nothing
	}

	/**
     * Implement this method to return true when your action is available
     * for the active table.  When true, the action will be included in the
     * context menu, when false it will be ignored.
     * 
     * @return true if the action should be included in the context menu
     */
    public boolean isAvailable() {
        return true;
    }
    
}
