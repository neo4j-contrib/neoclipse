package org.neo4j.neoclipse.view;

import org.eclipse.swt.widgets.Display;

/**
 * Helper to execute in the UI thread.
 * 
 * @author Anders Nawroth
 */
public class UiHelper
{
    public static void asyncExec( final Runnable runnable )
    {
        Display.getDefault().asyncExec( runnable );
    }

    public static void syncExec( final Runnable runnable )
    {
        Display.getDefault().syncExec( runnable );
    }
}
