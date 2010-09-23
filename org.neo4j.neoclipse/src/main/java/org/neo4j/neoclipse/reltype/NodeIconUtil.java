package org.neo4j.neoclipse.reltype;

import java.io.File;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.service.datalocation.Location;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.preference.DecoratorPreferences;

public class NodeIconUtil
{
    private static IPreferenceStore preferenceStore;

    private static IPreferenceStore getPreferenceStore()
    {
        if ( preferenceStore == null )
        {
            preferenceStore = Activator.getDefault().getPreferenceStore();
        }
        return preferenceStore;
    }

    public static File getIconLocation()
    {
        String location = getPreferenceStore().getString(
                DecoratorPreferences.NODE_ICON_LOCATION );
        if ( ( location == null ) || ( location.trim().length() == 0 ) )
        {
            Location workspace = Platform.getInstanceLocation();
            if ( workspace == null )
            {
                throw new RuntimeException( "Can't find workspace." );
            }
            try
            {
                File iconsDir = new File( workspace.getURL().toURI().getPath()
                                          + File.separator + "neoclipse"
                                          + File.separator + "icons" );
                if ( !iconsDir.exists() )
                {
                    if ( !iconsDir.mkdirs() )
                    {
                        throw new IllegalArgumentException(
                                "Could not create directory for the icons." );
                    }
                }
                location = iconsDir.getAbsolutePath();
                preferenceStore.setValue(
                        DecoratorPreferences.NODE_ICON_LOCATION, location );
            }
            catch ( URISyntaxException e )
            {
                e.printStackTrace();
                throw new IllegalArgumentException(
                        "The icon location is not correctly set." );
            }
        }

        File dest = new File( location );
        if ( !dest.exists() )
        {
            dest.mkdirs();
        }
        if ( !dest.exists() || !dest.isDirectory() )
        {
            MessageDialog.openInformation( null, "Icon location problem",
                    "Please make sure that the node icon location is correctly set." );
            Activator.getDefault().showDecoratorPreferenceDialog( false );
            location = getPreferenceStore().getString(
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
