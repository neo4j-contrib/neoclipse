/**
 * 
 */
package net.sourceforge.sqlexplorer.dbstructure.actions;

import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.plugin.actions.CopyTextProvider;
import net.sourceforge.sqlexplorer.plugin.actions.CopyTextToEditorAction;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;

/**
 * @author Heiko
 *
 */
public class InsertTextAction extends AbstractDBTreeContextAction implements CopyTextProvider 
{
	private IAction _textAction = new CopyTextToEditorAction(this);
	
	public String getCopyText() 
	{
		if(_selectedNodes.length == 0)
		{
			return null;
		}
        StringBuilder text = new StringBuilder();
        boolean first = true;

        for (INode current : _selectedNodes) {
        	if(first) {
        		first = false;
        	}
        	else
        	{
                text.append(", ");
        	}
            text.append(current.getName());
        }
        return text.toString();
	}

    /**
     * Custom image for copy action
     * 
     * @see org.eclipse.jface.action.IAction#getImageDescriptor()
     */
    public ImageDescriptor getImageDescriptor() {
        return this._textAction.getImageDescriptor();
    }


    /**
     * Set the text for the menu entry.
     * 
     * @see org.eclipse.jface.action.IAction#getText()
     */
    public String getText() {
        return this._textAction.getText();
    }


    /**
     * Copy the name of the selected node to the clipboard.
     * 
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run() {

    	this._textAction.run();
        Clipboard clipBoard = new Clipboard(Display.getCurrent());
        TextTransfer textTransfer = TextTransfer.getInstance();

        StringBuffer text = new StringBuffer("");
        String sep = "";

        for (int i = 0; i < _selectedNodes.length; i++) {
            text.append(sep);
            text.append(_selectedNodes[i].getQualifiedName());
            sep = ", ";
        }

        clipBoard.setContents(new Object[] {text.toString()}, new Transfer[] {textTransfer});

    }


    /**
     * Action is availble when a node is selected
     * 
     * @see net.sourceforge.sqlexplorer.dbstructure.actions.AbstractDBTreeContextAction#isAvailable()
     */
    public boolean isAvailable() {

        return _selectedNodes.length == 1;
    }

	@Override
	public boolean isDefault() {
		return isAvailable();
	}

	
}
