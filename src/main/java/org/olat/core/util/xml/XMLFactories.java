/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.util.xml;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;

import org.xml.sax.SAXException;

/**
 * A factory to return XML related factories and parsers with
 * the standard security features enabled.
 * 
 * Initial date: 22 sept. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class XMLFactories {
	
	private XMLFactories() {
		//
	}
	
	public static final DocumentBuilderFactory newDocumentBuilderFactory()
	throws ParserConfigurationException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance("com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl", null);
        documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, ""); // Compliant
        documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, ""); // compliant
		return documentBuilderFactory;
	}
	
	public static final TransformerFactory newTransformerFactory()
	throws TransformerConfigurationException {
		TransformerFactory tFactory = TransformerFactory.newInstance("com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl", null);
		tFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		tFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, ""); // Compliant
		tFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, ""); // Compliant
		return tFactory;
	}
	
	public static final SAXParser newSAXParser()
	throws ParserConfigurationException, SAXException {
		SAXParserFactory factory = SAXParserFactory.newInstance("com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl", null);
		factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		SAXParser parser = factory.newSAXParser();
		parser.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, ""); // Compliant
		parser.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, ""); // compliant
		return parser;
	}
	
	public static final SAXParser newSAXParser(boolean validating, boolean namespaceAware)
	throws ParserConfigurationException, SAXException {
		SAXParserFactory factory = SAXParserFactory.newInstance("com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl", null);
		factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		factory.setValidating(validating);
		factory.setNamespaceAware(namespaceAware);
		SAXParser parser = factory.newSAXParser();
		parser.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, ""); // Compliant
		parser.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, ""); // compliant
		return parser;
	}
}
