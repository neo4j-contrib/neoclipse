/*
 * Copyright (C) 2001 Colin Bell
 * colbell@users.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.sourceforge.sqlexplorer;

import java.io.File;

import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.squirrel_sql.fw.util.IJavaPropertyNames;

public class ApplicationFiles {

    /** Name of folder to contain users settings. */
    public static final String USER_SETTINGS_FOLDER = SQLExplorerPlugin.getDefault().getStateLocation().toFile().getAbsolutePath();

    /** Name of folder that contains Squirrel app. */
    public static final String JFACEDBC_FOLDER = System.getProperty(IJavaPropertyNames.USER_DIR);

    /** Name of file that contains database aliases. */
    public static final String USER_ALIAS_FILE_NAME = USER_SETTINGS_FOLDER + File.separator + "SQLAliases.xml";

    /** Name of file that contains users saved queries. */

    /** Name of file that contains user settings. */
    public static final String USER_PREFS_FILE_NAME = USER_SETTINGS_FOLDER + File.separator + "prefs.txt";

    /** Name of file that contains users database driver information. */
    public static final String USER_DRIVER_FILE_NAME = USER_SETTINGS_FOLDER + File.separator + "SQLDrivers.xml";

    /** Name of file that contains users database product configuration; only present if installed manually by
     * the user to override the defaults in the plugins dbproducts.xml */
    public static final String USER_DBPRODUCTS_XML = USER_SETTINGS_FOLDER + File.separator + "dbproducts.xml";

    /** Flle to log execution information to. */
    public static final String EXECUTION_LOG_FILE = USER_SETTINGS_FOLDER + File.separator + "jfacedbc-sql.log";

    /** File used to persist sql history */
    public static final String SQLHISTORY_FILE_NAME_V300 = USER_SETTINGS_FOLDER + File.separator + "SQLHistory.txt";
    public static final String SQLHISTORY_FILE_NAME_V350 = USER_SETTINGS_FOLDER + File.separator + "SQLHistory.xml";

    private String jfacedbcPluginsDir = JFACEDBC_FOLDER + File.separator + "plugins";

    static {
        new File(USER_SETTINGS_FOLDER).mkdir();
    }


    public ApplicationFiles() {
        super();
        try {
            final File logsDir = getExecutionLogFile().getParentFile();
            logsDir.mkdirs();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }

    }


    public File getExecutionLogFile() {
        return new File(EXECUTION_LOG_FILE);
    }


    public File getPluginsDirectory() {
        return new File(jfacedbcPluginsDir);
    }

}