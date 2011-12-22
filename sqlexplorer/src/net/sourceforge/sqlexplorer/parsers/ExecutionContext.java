/**
 * 
 */
package net.sourceforge.sqlexplorer.parsers;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.plugin.PluginPreferences;

import org.eclipse.jface.preference.IPreferenceStore;

/**
 * @author Heiko
 * 
 */
public class ExecutionContext {
	public static final String LOG_SUCCESS = "LOG_SUCCESS";
	public static final String LOG_SQL = "LOG_SQL";

	public static final String ON = "on";
	public static final String OFF = "off";

	private Map<String, String> options = new HashMap<String, String>();

	public ExecutionContext() {
		// Set default
		set(LOG_SUCCESS, ON);
		set(LOG_SQL, ON);

		IPreferenceStore store = PluginPreferences.getCurrent();
		if (store != null) {
			set(LOG_SUCCESS,
					store.getBoolean(IConstants.LOG_SUCCESS_MESSAGES) ? ON
							: OFF);
			set(LOG_SQL, store.getBoolean(IConstants.LOG_SQL_HISTORY) ? ON
					: OFF);
		}
	}

	public void set(String pOption, String pValue) {
		this.options.put(pOption, pValue);
	}

	public String get(String pOption) {
		return this.options.get(pOption);
	}

	public boolean isOff(String pOption) {
		return OFF.equalsIgnoreCase(get(pOption));
	}

	public boolean isOn(String pOption) {
		return ON.equalsIgnoreCase(get(pOption));
	}
}
