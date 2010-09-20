package org.neo4j.neoclipse.reltype;

import java.io.File;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.graphdb.GraphDbServiceManager;
import org.neo4j.neoclipse.preference.DecoratorPreferences;
import org.neo4j.neoclipse.preference.Preferences;

public class NodeIconUtil
{

    private static IPreferenceStore preferenceStore;
    private static GraphDbServiceManager gsm;

    private static IPreferenceStore getPreferenceStore()
    {
        if ( preferenceStore == null )
        {
            preferenceStore = Activator.getDefault().getPreferenceStore();
        }
        return preferenceStore;
    }

    private static GraphDbServiceManager getGsm()
    {
        if ( gsm == null )
        {
            gsm = Activator.getDefault().getGraphDbServiceManager();
        }
        return gsm;
    }

    public static File getIconLocation()
    {
        if ( getGsm().isRunning()
             && getGsm().isLocal()
             && getPreferenceStore().getBoolean(
                     DecoratorPreferences.LOCAL_NODE_ICON_LOCATION ) )
        {
            File file = new File( getPreferenceStore().getString(
                    Preferences.DATABASE_LOCATION )
                                  + File.separator
                                  + "neoclipse"
                                  + File.separator + "icons" );
            if ( !file.exists() )
            {
                file.mkdirs();
            }
            return file;
        }
        String location = Activator.getDefault().getPreferenceStore().getString(
                DecoratorPreferences.NODE_ICON_LOCATION );
        File dest = new File( location );
        if ( !dest.exists() || !dest.isDirectory() )
        {
            MessageDialog.openInformation( null, "Icon location problem",
                    "Please make sure that the node icon location is correctly set." );
            Activator.getDefault().showDecoratorPreferenceDialog( true );
            location = Activator.getDefault().getPreferenceStore().getString(
                    DecoratorPreferences.NODE_ICON_LOCATION );
            dest = new File( location );
            if ( !dest.exists() || !dest.isDirectory() )
            {
                MessageDialog.openError( null, "Error message",
                        "The icon location can not be found." );
                return null;
            }
        }
        return dest;
    }
}
