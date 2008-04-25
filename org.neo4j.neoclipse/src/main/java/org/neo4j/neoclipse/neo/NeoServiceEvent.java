/*
 * NeoServiceEvent.java
 */
package org.neo4j.neoclipse.neo;

import java.util.EventObject;

/**
 * This class represents a change in the neo service.
 * 
 * @author	Peter H&auml;nsgen
 */
public class NeoServiceEvent extends EventObject
{
    private static final long serialVersionUID = 1L;

    /**
     * The status.
     */
    protected NeoServiceStatus status;
    
    /**
     * The constructor.
     */
    public NeoServiceEvent(Object source, NeoServiceStatus status)
    {
        super(source);
        
        this.status = status;
    }
    
    /**
     * Returns the service status.
     */
    public NeoServiceStatus getStatus()
    {
        return status;
    }
}
