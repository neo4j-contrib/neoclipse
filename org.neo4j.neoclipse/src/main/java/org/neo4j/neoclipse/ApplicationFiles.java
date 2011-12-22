package org.neo4j.neoclipse;

import java.io.File;

public class ApplicationFiles
{

    /** Name of folder to contain users settings. */
    public static final String USER_SETTINGS_FOLDER = Activator.getDefault().getStateLocation().toFile().getAbsolutePath();

    /** Name of file that contains database aliases. */
    public static final String USER_ALIAS_FILE_NAME = USER_SETTINGS_FOLDER + File.separator + "NeoDbAliases.xml";

    static
    {
        new File( USER_SETTINGS_FOLDER ).mkdir();
    }

}
