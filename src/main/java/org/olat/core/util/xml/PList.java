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

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;

/**
 * 
 * Initial date: 25 janv. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PList {
	
	private static final Logger log = Tracing.createLoggerFor(PList.class);
	
	private Element rootDict;
	private Document doc;
	private DocumentType dt;
	
	public PList() {
		try {
			// Create an empty, stand-alone DOM document
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			DOMImplementation di = builder.getDOMImplementation();
			dt = di.createDocumentType("plist", "-//Apple//DTD PLIST 1.0//EN", "http://www.apple.com/DTDs/PropertyList-1.0.dtd");
			doc = di.createDocument("", "plist", dt);
			doc.setXmlStandalone(true);
			
			// Set plist version.
			Element rootElement = doc.getDocumentElement();
			rootElement.setAttribute("version", "1.0");
			
			// dictionary level
			rootDict = doc.createElement("dict");
			rootElement.appendChild(rootDict);
		} catch (DOMException | ParserConfigurationException e) {
			log.error("", e);
		}
	}
	
	public void add(String key, String value) {
		add(rootDict, key, value);
	}
	
	public void add(Element dictElement, String key, String value) {
		Element keyElement = doc.createElement("key");
		keyElement.setTextContent(key);
		dictElement.appendChild(keyElement);
		Element valueElement = doc.createElement("string");
		valueElement.setTextContent(value);
		dictElement.appendChild(valueElement);
	}
	
	public void add(String key, boolean value) {
		add(rootDict, key, value);
	}
	
	public void add(Element dictElement, String key, boolean value) {
		Element keyElement = doc.createElement("key");
		keyElement.setTextContent(key);
		dictElement.appendChild(keyElement);
		Element valueElement = doc.createElement(value ? "true" : "false");
		dictElement.appendChild(valueElement);
	}
	
	public void add(String key, Integer value) {
		add(rootDict, key, value);
	}
	
	public void add(Element dictElement, String key, Integer value) {
		Element keyElement = doc.createElement("key");
		keyElement.setTextContent(key);
		dictElement.appendChild(keyElement);
		Element valueElement = doc.createElement("integer");
		valueElement.setTextContent(value.toString());
		dictElement.appendChild(valueElement);
	}
	
	/**
	 * Add an array element.
	 * 
	 * @param key The key
	 * @return The array element
	 */
	public Element addArray(String key) {
		Element keyElement = doc.createElement("key");
		keyElement.setTextContent(key);
		rootDict.appendChild(keyElement);
		Element valueElement = doc.createElement("array");
		rootDict.appendChild(valueElement);
		return valueElement;
	}
	
	public Element addDictToArray(Element arrayElement) {
		Element dictElement = doc.createElement("dict");
		arrayElement.appendChild(dictElement);
		return dictElement;
	}
	
	
	
	public String toPlistString() throws TransformerException  {
		DOMSource domSource = new DOMSource(doc);
		TransformerFactory tf = XMLFactories.newTransformerFactory();
		Transformer t = tf.newTransformer();
		t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		t.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, dt.getPublicId());
		t.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, dt.getSystemId());
		t.setOutputProperty(OutputKeys.INDENT, "no");
		
		StringWriter stringWriter = new StringWriter();
		StreamResult streamResult = new StreamResult(stringWriter);
		t.transform(domSource, streamResult);
		return stringWriter.toString();
	}

}
