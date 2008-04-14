/*
 * NeoServiceEventListener.java
 */
package org.neo4j.neoclipse.neo;

import java.util.EventListener;

/**
 * This interface can be implemented by listeners that want to be updated
 * about neo service changes.
 * 
 * @author	Peter H&auml;nsgen
 */
public interface NeoServiceEventListener extends EventListener
{
    /**
     * Called when the service has changed.
     */
    public void serviceChanged(NeoServiceEvent event);
}
