/*
 * Copyright (C) SQL Explorer Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sourceforge.sqlexplorer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

/**
 * helper class for reading and writing XML files
 * 
 * @author Heiko
 *
 */
public class XMLUtils {

	public static void save(Element pRoot, File pFile) {
        try {
        	XMLWriter xmlWriter = new XMLWriter(new FileOutputStream(pFile), OutputFormat.createPrettyPrint());
        	xmlWriter.startDocument();
        	xmlWriter.write(pRoot);
        	xmlWriter.endDocument();
        	xmlWriter.flush();
        	xmlWriter.close();
        } catch (Exception e) {
            SQLExplorerPlugin.error("Couldn't save: " + pFile.getAbsolutePath(), e);
        }
		
	}
	
	public static Element readRoot(File pFile)	{
		if(!pFile.exists())
		{
			return null;
		}
    	try {
    		return readRoot(new FileInputStream(pFile));
    	}
    	catch(DocumentException e) {
    		SQLExplorerPlugin.error("Cannot load: " + pFile.getAbsolutePath(), e);
    	} catch (FileNotFoundException ignored) {
			// impossible :-)
			
		}
		return null;
	}
	public static Element readRoot(InputStream pFile) throws DocumentException	{
        SAXReader reader = new SAXReader();
        return reader.read(pFile).getRootElement();
	}
}
