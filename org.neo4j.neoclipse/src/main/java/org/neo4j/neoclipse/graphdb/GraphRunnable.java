package org.neo4j.neoclipse.graphdb;

import org.neo4j.graphdb.GraphDatabaseService;

public interface GraphRunnable
{
    void run( GraphDatabaseService graphDb );
}
