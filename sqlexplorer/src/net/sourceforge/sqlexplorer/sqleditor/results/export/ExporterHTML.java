package net.sourceforge.sqlexplorer.sqleditor.results.export;

import java.io.File;
import java.io.PrintStream;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.sqleditor.results.CellRangeRow;
import net.sourceforge.sqlexplorer.sqleditor.results.ResultProvider;
import net.sourceforge.sqlexplorer.util.TextUtil;

/**
 * HTML Export
 * @author John Spackman
 *
 */
public class ExporterHTML implements Exporter {

	private static final String[] FILTER = { "*.html", "*.htm" };
	
	public String[] getFileFilter() {
		return FILTER;
	}

	public String getFormatName() {
		return Messages.getString("ExportDlg.HTML");
	}

	public int getFlags() {
		return FMT_CHARSET | FMT_DELIM | FMT_NULL | OPT_HDR;
	}

	public void export(ResultProvider data, ExportOptions options, File file) throws Exception
	{
        PrintStream writer = new PrintStream(file, options.characterSet); 
        
        // get column header and separator preferences
        boolean includeColumnNames = options.includeColumnNames;
        String nullValue = options.nullValue;

        int columnCount = data.getNumberOfColumns();

        writer.println("<html>");
        writer.println("<head>");                    
        writer.print("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=");
        writer.print(options.characterSet);
        writer.println("\">");
        writer.println("<style type=\"text/css\">");
        writer.println("TABLE {border-collapse: collapse;}");
        writer.println("TH {background-color: rgb(240, 244, 245);}");
        writer.println("TH, TD {border: 1px solid #D1D6D4;font-size: 10px;font-family: Verdana, Arial, Helvetica, sans-serif;}");
        writer.println(".right {text-align: right;}");
        writer.println("</style>");
        writer.println("</head>");
        writer.println("<body>");
        writer.println("<table>");
        
        if (includeColumnNames) {
        	writer.print("<tr>");
            for (int i = 0; i < columnCount; i++) 
            {
            	writer.print("<th>");
                writer.print(data.getColumn(i).getCaption());
                writer.print("</th>");
                
            }
        	writer.println("</tr>");
        }
        // export column data
        for (CellRangeRow row : data.getRows()) 
        {
        	writer.print("<tr>");
                                   
            for (int j = 0; j < columnCount; j++) 
            {
            	Object o = row.getCellValue(j);
                String t = o == null ? nullValue : o.toString();
            	writer.print("<td>");
            	writer.print(TextUtil.htmlEscape(t));
                writer.print("</td>");
            }
        	writer.println("</tr>");
        }

        writer.println("</table>");

        writer.close();
		
	}

}
