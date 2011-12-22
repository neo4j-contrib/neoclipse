package net.sourceforge.sqlexplorer.plugin.editors;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.EditorActionBarContributor;

public class SqlExplorerEditorActionBarContributor extends
		EditorActionBarContributor {

	private CursorPositionContrib _cursorPosition;

	public SqlExplorerEditorActionBarContributor() {
		super();
		_cursorPosition = new CursorPositionContrib();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorActionBarContributor#contributeToStatusLine(org.eclipse.jface.action.IStatusLineManager)
	 */
	@Override
	public void contributeToStatusLine(IStatusLineManager statusLineManager) {
		super.contributeToStatusLine(statusLineManager);
		
		statusLineManager.add(_cursorPosition);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorActionBarContributor#setActiveEditor(org.eclipse.ui.IEditorPart)
	 */
	@Override
	public void setActiveEditor(IEditorPart targetEditor) {
		super.setActiveEditor(targetEditor);
		if(targetEditor instanceof SQLTextEditor)
		{
			((SQLTextEditor)targetEditor).getEditor().updateCursorPosition();			
		}
		else
		{
			((SQLEditor)targetEditor).updateCursorPosition();
		}
	}
}
