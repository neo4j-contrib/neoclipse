package net.sourceforge.sqlexplorer.plugin;

import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Singleton to get current IPreferenceStore. Use to set a dummy implementation for
 * tests.
 * 
 * @author Heiko
 *
 */
public class PluginPreferences 
{
	private static IPreferenceStore _store = null;
	
	public static IPreferenceStore getCurrent()
	{
		return _store;
	}
	
	public static void setCurrent(IPreferenceStore pStore)
	{
		_store = pStore;
	}

}
