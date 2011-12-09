/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 
package org.olat.core.util.xml;

import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;
import org.olat.core.logging.OLATRuntimeException;

/**
 * enclosing_type Description: <br>
 * 
 * @author Felix Jost
 */
public class Util {

	/**
	 * @param o
	 * @param file
	 */
	public static void writeObjectAsXML(Object o, String file) {

		XMLEncoder e;
		try {
			e = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(file)));
			e.writeObject(o);
			e.close();
		} catch (FileNotFoundException e1) {
			throw new OLATRuntimeException(Util.class, "Error writing object to XML.", e1);
		}

	}

	/**
	 * @param elem
	 * @return String
	 */
	public static String getTextsOnly(Element elem) {
		StringBuilder sb = new StringBuilder();
		visit(sb, elem);
		return sb.toString();
	}

	private static void visit(StringBuilder sb, Element elem) {
		sb.append(elem.getTextTrim());
		List children = elem.elements();
		for (Iterator it_ch = children.iterator(); it_ch.hasNext();) {
			Element element = (Element) it_ch.next();
			visit(sb, element);
		}
	}

}