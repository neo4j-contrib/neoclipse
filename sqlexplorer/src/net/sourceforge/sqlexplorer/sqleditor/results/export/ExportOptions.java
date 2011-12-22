package net.sourceforge.sqlexplorer.sqleditor.results.export;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;

public class ExportOptions {

	public static final ExportOptions Current = new ExportOptions();
	static
	{
		Current.columnSeparator = SQLExplorerPlugin.getDefault().getPreferenceStore().getString(IConstants.CLIP_EXPORT_SEPARATOR);
		Current.includeColumnNames = SQLExplorerPlugin.getDefault().getPreferenceStore().getBoolean(IConstants.CLIP_EXPORT_COLUMNS);
		Current.nullValue = Messages.getString("ExportDlg.Null");		
	}
	public String characterSet;
    public String columnSeparator; 
    public boolean includeColumnNames;
    public boolean rtrim;
    public boolean quote;
    public String nullValue;
    
}
