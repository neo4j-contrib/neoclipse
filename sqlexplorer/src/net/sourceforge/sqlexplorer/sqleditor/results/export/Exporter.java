package net.sourceforge.sqlexplorer.sqleditor.results.export;

import java.io.File;

import net.sourceforge.sqlexplorer.sqleditor.results.ResultProvider;

/**
 * Implements 
 * @author John Spackman
 *
 */
public interface Exporter {

	/**
	 * Flag indicating consumer wants to obtain character set.
	 */
	public static final int FMT_CHARSET = 1 << 0;

	/**
	 * Flag indicating consumer wants to obtain field delimiter.
	 */
	public static final int FMT_DELIM = 1 << 1;

	/**
	 * Flag indicating consumer wants to obtain null value string.
	 */
	public static final int FMT_NULL = 1 << 2;

	/**
	 * Flag indicating consumer wants to know whether to export column headers.
	 */
	public static final int OPT_HDR = 1 << 3;

	/**
	 * Flag indicating consumer wants to know whether to quote string values.
	 */
	public static final int OPT_QUOTE = 1 << 4;

	/**
	 * Flag indicating consumer wants to know whether to right-trim values.
	 */
	public static final int OPT_RTRIM = 1 << 5;


	/**
	 * Get export format title
	 */
	public String getFormatName();

	/**
	 * Get dialog's file filter when choosing input file.
	 * 
	 * @return List of file patterns (already containing '*').
	 */
	public String[] getFileFilter();
	
	/**
	 * get Bit mask for available export options
	 * @return
	 */
	public int getFlags();

	/**
	 * export the result data
	 * @param data
	 * @param options
	 * @param file
	 * @throws Exception
	 */
	public void export(ResultProvider data, ExportOptions options, File file) throws Exception;

}
