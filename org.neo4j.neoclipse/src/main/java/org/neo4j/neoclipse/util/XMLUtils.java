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

import java.io.File;
import java.io.FileOutputStream;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.neo4j.neoclipse.view.ErrorMessage;

/**
 * Helper class for reading and writing XML files
 * 
 * @author Radhakrishna Kalyan
 * 
 */
public class XMLUtils
{

    public static void save( Element pRoot, File pFile )
    {
        try
        {
            pFile.getParentFile().mkdirs();
            FileOutputStream fileOutputStream = new FileOutputStream( pFile );
            XMLWriter xmlWriter = new XMLWriter( fileOutputStream, OutputFormat.createPrettyPrint() );
            xmlWriter.startDocument();
            xmlWriter.write( pRoot );
            xmlWriter.endDocument();
            xmlWriter.flush();
            xmlWriter.close();
        }
        catch ( Exception e )
        {
            ErrorMessage.showDialog( "Couldn't save: " + pFile.getAbsolutePath(), e );
        }

    }

    public static Element readRoot( File pFile )
    {
        if ( !pFile.exists() )
        {
            return null;
        }
        try
        {
            SAXReader reader = new SAXReader();
            return reader.read( pFile ).getRootElement();
        }
        catch ( DocumentException e )
        {
            ErrorMessage.showDialog( "Cannot load: " + pFile.getAbsolutePath(), e );
        }

        return null;
    }
}
