/*
 * NeoPreferences.java
 */
package org.neo4j.neoclipse.preference;

/**
 * Defines the preferences of the neo plugin.
 * 
 * @author	Peter H&auml;nsgen
 */
public interface NeoPreferences
{
    /**
     * The location of the neo database in the file system.
     */
    public static final String DATABASE_LOCATION = "databaseLocation";
    /**
     * The property to view inside nodes in the graph view.
     */
    public static final String DEFAULT_PROPERTY_NAME = "defaultPropertyName";
}
