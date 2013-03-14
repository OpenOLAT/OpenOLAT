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
package org.olat.modules.qpool.manager;

import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * 
 * Initial date: 11.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("lomConverter")
public class LOMConverter {
	
	private static final OLog log = Tracing.createLoggerFor(LOMConverter.class);
	
	protected void toLom(QuestionItemImpl item, OutputStream out) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setNamespaceAware(false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.newDocument();

			Element lomEl = (Element)document.appendChild(document.createElement("lom"));

			generalToLom(item, lomEl, document);

			DOMSource domSource = new DOMSource(document);
			StreamResult streamResult = new StreamResult(out); 
			Transformer serializer = TransformerFactory.newInstance().newTransformer();
			serializer.transform(domSource, streamResult);
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	protected void generalToLom(QuestionItemImpl item, Node lomEl, Document doc) {
		Node generalEl = lomEl.appendChild(doc.createElement("general"));
		
		//language
		Element languageEl = (Element)generalEl.appendChild(doc.createElement("language"));
		languageEl.setAttribute("value", item.getLanguage());
		//title
		Node titleEl = generalEl.appendChild(doc.createElement("title"));
		stringToLom(item, item.getTitle(), titleEl, doc);
		//description
		Node descEl = generalEl.appendChild(doc.createElement("description"));
		stringToLom(item, item.getDescription(), descEl, doc);
		
		
		
	}
	
	protected void stringToLom(QuestionItemImpl item, String value, Node el, Document doc) {
		Element stringEl = (Element)el.appendChild(doc.createElement("string"));
		stringEl.setAttribute("value", value);
		stringEl.setAttribute("language", item.getLanguage());
	}

	protected void toItem(QuestionItemImpl item, InputStream in) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setNamespaceAware(false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(in));

			for(Node child=document.getDocumentElement().getFirstChild(); child != null; child=child.getNextSibling()) {
				if(Node.ELEMENT_NODE != child.getNodeType()) continue;

				String name = child.getNodeName().toLowerCase();
				if("educational".equals(name)) {
					
				} else if("general".equals(name)) {
					generalToItem(item, (Element)child);
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	private void generalToItem(QuestionItemImpl item, Element generalEl) {
		for(Node child=generalEl.getFirstChild(); child != null; child=child.getNextSibling()) {
			if(Node.ELEMENT_NODE != child.getNodeType()) continue;
			
			String name = child.getNodeName().toLowerCase();
			if("title".equals(name)) {
				item.setTitle(getString((Element)child));
			} else if("description".equals(name)) {
				item.setDescription(getString((Element)child));	
			}
		}
	}
	
	private String getString(Element el) {
		String val = null;
		for(Node child=el.getFirstChild(); child != null; child=child.getNextSibling()) {
			if(Node.ELEMENT_NODE != child.getNodeType()) continue;
			
			String name = child.getNodeName().toLowerCase();
			if("string".equals(name)) {
				val = ((Element)child).getAttribute("value");
			}
		}
		return val;
	}
}
