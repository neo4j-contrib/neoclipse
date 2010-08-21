/**
 * 
 */
package org.neo4j.neoclipse.graphdb;

import org.neo4j.graphdb.GraphDatabaseService;

public interface GraphCallable<T>
{
    T call( GraphDatabaseService graphDb );
}