/*
 * Copyright (C) 2007 SQL Explorer Development Team
 * http://sourceforge.net/projects/eclipsesql
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
package net.sourceforge.sqlexplorer.dataset;

import org.dom4j.Element;
import net.sourceforge.sqlexplorer.ExplorerException;

/**
 * DataType that provides XML.  Must be implemented by the database platform.
 * 
 * @author John Spackman
 */
public interface XmlDataType extends DataType {

	/**
	 * Returns the value as XML 
	 * @return
	 * @throws ExplorerException 
	 */
	public Element getRootElement() throws ExplorerException;
}
