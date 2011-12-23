/**
 * Licensed to Neo Technology under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Neo Technology licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.neo4j.neoclipse.reltype;

import java.io.File;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.preference.DecoratorPreferences;
import org.neo4j.neoclipse.util.ApplicationUtil;

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
        String location = getPreferenceStore().getString( DecoratorPreferences.NODE_ICON_LOCATION );
        if ( ( location == null ) || ( location.trim().length() == 0 ) )
        {
            File iconsDir = ApplicationUtil.dirInWorkspace( "neoclipse", "icons" );
            location = iconsDir.getAbsolutePath();
            preferenceStore.setValue( DecoratorPreferences.NODE_ICON_LOCATION, location );
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
            location = getPreferenceStore().getString( DecoratorPreferences.NODE_ICON_LOCATION );
            dest = new File( location );
            if ( !dest.exists() || !dest.isDirectory() )
            {
                MessageDialog.openError( null, "Error message", "The icon location can not be found." );
                return null;
            }
        }
        return dest;
    }
}
