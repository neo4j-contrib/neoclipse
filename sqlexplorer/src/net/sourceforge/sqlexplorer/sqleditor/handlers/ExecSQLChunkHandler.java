package net.sourceforge.sqlexplorer.sqleditor.handlers;

import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.parsers.QueryParser;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;

public class ExecSQLChunkHandler extends ExecSQLHandler {
	public static final String COMMAND_ID = "net.sourceforge.sqlexplorer.executeSQLChunk";

	@Override
    protected QueryParser getQueryParser(Session session, SQLEditor pEditor)
    {
    	return  session.getDatabaseProduct().getQueryParser(pEditor.getSQLToBeExecuted(true), pEditor.getSQLLineNumber(true));
    }
}
