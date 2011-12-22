package net.sourceforge.sqlexplorer.parsers;

import junit.framework.TestCase;
import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.plugin.PluginPreferences;
import net.sourceforge.sqlexplorer.plugin.PreferenceDummy;

import org.eclipse.jface.preference.IPreferenceStore;

public class ExecutionContextTest extends TestCase {

	public void testExecutionContextNoPreferences() {
		
		ExecutionContext out = new ExecutionContext();
		
		// default is all on
		assertTrue(out.isOn(ExecutionContext.LOG_SUCCESS));
		assertTrue(out.isOn(ExecutionContext.LOG_SQL));
		assertFalse(out.isOff(ExecutionContext.LOG_SUCCESS));
		assertFalse(out.isOff(ExecutionContext.LOG_SQL));

	}

	public void testExecutionContextPreferences() {
		
		IPreferenceStore testPreferences = new PreferenceDummy();
		
		// setup PluginPrefernces
		testPreferences.setValue(IConstants.LOG_SUCCESS_MESSAGES, false);
		testPreferences.setValue(IConstants.LOG_SQL_HISTORY, false);
		PluginPreferences.setCurrent(testPreferences);
		
		// test it
		ExecutionContext out = new ExecutionContext();
		assertFalse(out.isOn(ExecutionContext.LOG_SUCCESS));
		assertFalse(out.isOn(ExecutionContext.LOG_SQL));
		assertTrue(out.isOff(ExecutionContext.LOG_SUCCESS));
		assertTrue(out.isOff(ExecutionContext.LOG_SQL));

	}

	public void testSetGet() 
	{
		ExecutionContext out = new ExecutionContext();
		
		assertNull(out.get("test"));
		out.set("test", "value");
		assertEquals("value", out.get("test"));
	}


	public void testIsOff() 
	{
		ExecutionContext out = new ExecutionContext();
		assertFalse(out.isOff("test"));
		out.set("test", ExecutionContext.OFF);
		assertTrue(out.isOff("test"));
		
	}

	public void testIsOn() {
		ExecutionContext out = new ExecutionContext();
		assertFalse(out.isOn("test"));
		out.set("test", ExecutionContext.ON);
		assertTrue(out.isOn("test"));
	}

}
