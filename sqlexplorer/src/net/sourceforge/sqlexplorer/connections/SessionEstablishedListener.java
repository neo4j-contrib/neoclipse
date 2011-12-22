package net.sourceforge.sqlexplorer.connections;

import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.dbproduct.User;

/**
 * This interface is the callback for the ConnectJob
 * @author John Spackman
 */
public interface SessionEstablishedListener {
	
	/**
	 * Called when a session has been successfully established
	 * @param session
	 */
	public void sessionEstablished(Session session);
	
	/**
	 * Called when a connection cannot be established
	 * @param user
	 */
	public void cannotEstablishSession(User user);
}
