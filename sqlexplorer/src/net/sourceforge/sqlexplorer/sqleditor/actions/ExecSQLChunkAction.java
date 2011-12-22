/**
 * 
 */
package net.sourceforge.sqlexplorer.sqleditor.actions;

import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.sqleditor.handlers.ExecSQLChunkHandler;

/**
 * @author Heiko Hilbert
 *
 */
public class ExecSQLChunkAction extends CommandAction {
    
	/**
	 * @param editor
	 */
	public ExecSQLChunkAction(SQLEditor editor) {
		super(editor, ExecSQLChunkHandler.COMMAND_ID);
	}
}
