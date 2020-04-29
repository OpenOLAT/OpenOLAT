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

import java.io.InputStream;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.olat.core.logging.OLATRuntimeException;
import org.xml.sax.EntityResolver;

/**
 * Description: <br>
 * purpose: may parse and validate documents
 * 
 * @author Felix Jost
 */
public class XMLParser {
	private EntityResolver er;

	/**
	 * Constructor for XMLParser.
	 */
	public XMLParser() {
		this.er = new DefaultEntityResolver();
	}

	/**
	 * Constructor for XMLParser.
	 * 
	 * @param er
	 */
	public XMLParser(EntityResolver er) {
		this.er = er;
	}

	/**
	 * @param in
	 * @param validateXML
	 * @return parsed document
	 */
	public Document parse(InputStream in, boolean validateXML) {
		Document document;
		try {
			SAXReader reader = new SAXReader();
			reader.setEntityResolver(er);
			reader.setValidation(validateXML);
			document = reader.read(in, "");
			document.normalize();
		} catch (Exception e) {
			throw new OLATRuntimeException(XMLParser.class, "Exception reading XML", e);
		}
		return document;
	}

}