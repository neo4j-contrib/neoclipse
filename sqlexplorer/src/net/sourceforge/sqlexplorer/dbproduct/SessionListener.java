/**
 * 
 */
package net.sourceforge.sqlexplorer.dbproduct;

/**
 * @author Heiko
 *
 */
public interface SessionListener {
	public static final int CATALOG_CHANGED = 1;
	
	public void sessionChanged(int pType, Session pSession);
}
