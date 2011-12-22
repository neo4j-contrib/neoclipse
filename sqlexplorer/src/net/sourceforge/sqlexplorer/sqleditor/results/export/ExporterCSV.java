package net.sourceforge.sqlexplorer.sqleditor.results.export;

import java.io.File;
import java.io.PrintStream;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.sqleditor.results.CellRangeRow;
import net.sourceforge.sqlexplorer.sqleditor.results.ResultProvider;
import net.sourceforge.sqlexplorer.util.TextUtil;

/**
 * Handles export to CSV
 * @author John Spackman
 *
 */
public class ExporterCSV implements Exporter {

	private static final String[] FILTER = { "*.csv", "*.txt" };
	
	public String[] getFileFilter() {
		return FILTER;
	}

	public String getFormatName() {
		return Messages.getString("ExportDlg.CSV");
	}

	public int getFlags() {
		return FMT_CHARSET | FMT_DELIM | FMT_NULL | OPT_HDR | OPT_QUOTE | OPT_RTRIM;
	}

	public void export(ResultProvider data, ExportOptions options, File file) throws Exception
	{
        PrintStream writer = new PrintStream(file, options.characterSet); 
        
        // get column header and separator preferences
        String columnSeparator = options.columnSeparator; 
        boolean includeColumnNames = options.includeColumnNames;
        boolean rtrim = options.rtrim;
        boolean quote = options.quote;
        String nullValue = options.nullValue;
                                   
        int columnCount = data.getNumberOfColumns();
        // export column names if we need to 
        if (includeColumnNames) 
        {
            
            for (int i = 0; i < columnCount; i++) 
            {
                if (i != 0)
                {
                	writer.print(columnSeparator);
                }
                writer.print(data.getColumn(i).getCaption());
            }
            writer.println();
        }

        // export column data
        for (CellRangeRow row : data.getRows()) 
        {
                               
            for (int j = 0; j < columnCount; j++) 
            {
            	Object o = row.getCellValue(j);
            	String t = o == null ? nullValue : o.toString();
            	if (rtrim)
            	{
            		t = TextUtil.rtrim(t);
            	}
            	if (quote && o instanceof String) 
            	{
            		writer.print(TextUtil.quote(t));
            	} 
            	else
            	{
            		writer.print(t);
            	}
            	/* don't append separator _after_ last column */
            	if (j < columnCount - 1)
            	{
            		writer.print(columnSeparator);
            	}
            }
            writer.println();
        }

        writer.close();
		
	}
}
