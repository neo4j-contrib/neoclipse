package net.sourceforge.sqlexplorer.sqleditor.results.export;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.sqleditor.results.CellRangeRow;
import net.sourceforge.sqlexplorer.sqleditor.results.ResultProvider;
import net.sourceforge.sqlexplorer.util.TextUtil;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * XLS (HTML) Export
 * @author Heiko
 *
 */
public class ExporterXLS implements Exporter {

	private static final String[] FILTER = { "*.xls"};
	
	public String[] getFileFilter() {
		return FILTER;
	}

	public String getFormatName() {
		return Messages.getString("ExportDlg.XLS");
	}

	public int getFlags() {
		return OPT_HDR | OPT_RTRIM;
	}

	public void export(ResultProvider data, ExportOptions options, File file) throws Exception
	{
        // get column header and separator preferences
        boolean includeColumnNames = options.includeColumnNames;
        boolean rtrim = options.rtrim;
                                   
        int columnCount = data.getNumberOfColumns();

		Workbook wb = new HSSFWorkbook();
		CreationHelper createHelper = wb.getCreationHelper();
		CellStyle dateStyle = wb.createCellStyle();
		String pattern = ((SimpleDateFormat)SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.MEDIUM,SimpleDateFormat.MEDIUM)).toPattern();
		dateStyle.setDataFormat(createHelper.createDataFormat().getFormat(pattern));
		
		Sheet sheet = wb.createSheet("sheet1");
		
		int rowNum = 0;

        if (includeColumnNames) {
    		Row row = sheet.createRow(rowNum++);
            for (short i = 0; i < columnCount; i++) 
            {
        		row.createCell(i).setCellValue(data.getColumn(i).getCaption());
            }
        }
        
        // export column data
        for (CellRangeRow row : data.getRows()) 
        {
    		Row nextRow = sheet.createRow(rowNum++);
            for (int j = 0; j < columnCount; j++) 
            {
            	Object o = row.getCellValue(j);
            	if(o != null)
            	{
            		Cell cell = nextRow.createCell(j);
	            	if(o instanceof Date)
	            	{
	            		cell.setCellValue((Date)o);
	            		cell.setCellStyle(dateStyle);
	            	}
	            	else if(o instanceof Number)
	            	{
	            		cell.setCellValue(((Number)o).doubleValue());
	            	}
	            	else
	            	{
	            		cell.setCellValue(rtrim ? TextUtil.rtrim(o.toString()) : o.toString());
	            	}
            	}
            }
        }

        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream(file);
        wb.write(fileOut);
        fileOut.close();
		
	}
	
}
