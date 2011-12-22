/**
 * 
 */
package net.sourceforge.sqlexplorer.plugin.actions;

/**
 * Used to provide text from different sources for the CopyTextToEditorAction
 * 
 * @author Heiko
 *
 */
public interface CopyTextProvider 
{
	/**
	 * returns the available text to copy to the editor or null
	 * if no text is available
	 * 
	 * @return text to copy or null
	 */
	public String getCopyText();
}
