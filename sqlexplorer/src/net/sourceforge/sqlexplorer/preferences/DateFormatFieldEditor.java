/**
 * 
 */
package net.sourceforge.sqlexplorer.preferences;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Heiko
 *
 */
public class DateFormatFieldEditor extends StringFieldEditor {

	/**
	 * @param name
	 * @param labelText
	 * @param parent
	 */
	public DateFormatFieldEditor(String name, String labelText, Composite parent) {
		super(name, labelText, 25,VALIDATE_ON_KEY_STROKE,parent);
	}

	@Override
	protected boolean doCheckState() {
		try
		{
			new SimpleDateFormat(getStringValue()).format(new Date());
		}
		catch(Exception e)
		{
			return false;
		}
		return super.doCheckState();
	}


}
