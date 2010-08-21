package org.neo4j.neoclipse.view;

import org.eclipse.jface.dialogs.MessageDialog;

public class ErrorMessage
{
    private static final int MAX_DEPTH = 10;

    private static String getErrorMessage( final Throwable exception )
    {
        String message = exception.getMessage();
        Throwable throwable = exception.getCause();
        int depth = 0;
        while ( throwable != null && depth++ < MAX_DEPTH )
        {
            if ( throwable.getMessage() != null )
            {
                message += ": " + throwable.getMessage();
            }
            throwable = throwable.getCause();
        }
        return message;
    }

    public static void showDialog( final String heading, final String message )
    {
        UiHelper.asyncExec( new Runnable()
        {
            public void run()
            {
                MessageDialog.openInformation( null, heading, message );
            }
        } );
    }

    public static void showDialog( final String heading,
            final Throwable throwable )
    {
        throwable.printStackTrace();
        String message = getErrorMessage( throwable );
        showDialog( heading, message );
    }
}
