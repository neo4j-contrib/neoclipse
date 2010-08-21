package org.neo4j.neoclipse.view;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

public class Dialog
{
    public static void openError( final Shell parent, final String title,
            final String message )
    {
        UiHelper.asyncExec( new Runnable()
        {
            public void run()
            {
                MessageDialog.openError( parent, title, message );
            }
        } );
    }

    public static void openError( final String title, final String message )
    {
        Dialog.openError( null, title, message );
    }

    public static void openWarning( final Shell parent, final String title,
            final String message )
    {
        UiHelper.asyncExec( new Runnable()
        {
            public void run()
            {
                MessageDialog.openWarning( parent, title, message );
            }
        } );
    }

    public static void openWarning( final String title, final String message )
    {
        Dialog.openWarning( null, title, message );
    }
}
