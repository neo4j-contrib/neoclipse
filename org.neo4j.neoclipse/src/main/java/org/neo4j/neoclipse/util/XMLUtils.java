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
package org.neo4j.neoclipse.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Map;

import org.neo4j.neoclipse.view.ErrorMessage;

import com.google.gson.Gson;

/**
 * Helper class for reading and writing XML files
 * 
 * @author Radhakrishna Kalyan
 * 
 */
public class XMLUtils
{
	public static Gson gson = new Gson();
    
    public static void save( Object pRoot, File pFile )
    {
        try
        {
        	pFile.getParentFile().mkdirs();
            BufferedOutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream( pFile ));
            fileOutputStream.write(gson.toJson(pRoot).getBytes());
            fileOutputStream.close();
            
        }
        catch ( Exception e )
        {
            ErrorMessage.showDialog( "Couldn't save: " + pFile.getAbsolutePath(), e );
        }

    }

    public static Map<String, Object> readRoot( File pFile )
    {
    	System.out.println(String.format("Loading settings from file %s" , pFile));
        if ( !pFile.exists() )
        {
            return null;
        }
        try
        {
            return gson.fromJson(new FileReader(pFile),	 Map.class);
        }
        catch ( Exception e )
        {
            ErrorMessage.showDialog( "Cannot load: " + pFile.getAbsolutePath(), e );
        }

        return null;
    }
}
