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
package org.olat.ims.qti21.pool;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.olat.core.util.StringHelper;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 
 * Convert the special TinyMCE options for the QTI 1.2 editor (without root
 * elements) in a QTI 2.1 compliant HTML code with Block elements as roots.
 * For the sake of XML, the handler had a tag &lt;start&gt; as root element
 * that the developer need to remove.
 * 
 * Initial date: 28 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
class QTI12To21HtmlHandler extends DefaultHandler {

	private final XMLStreamWriter xtw;

	private int subLevel = 0;
	private Deque<String> skipTags = new ArrayDeque<>();
	private Map<String,String> materialsMapping = new HashMap<>();

	private boolean envelopP = false;
	private boolean started = false;
	
	public QTI12To21HtmlHandler(XMLStreamWriter xtw) {
		this.xtw = xtw;
	}
	
	public Map<String,String> getMaterialsMapping() {
		return materialsMapping;
	}

	@Override
	public void startDocument() throws SAXException {
		try {
			xtw.writeStartElement("start");
		} catch (XMLStreamException e) {
			throw new SAXException(e);
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes)
	throws SAXException {
		try {
			qName = qName.toLowerCase();
			if("html".equals(qName) || "head".equals(qName)) {
				return;
			}
			if("body".equals(qName)) {
				started = true;
				return;
			}

			if(subLevel == 0 && envelopP && isBlock(qName)) {
				xtw.writeEndElement();
				envelopP = false;
			}

			if(subLevel == 0 && !envelopP && !isBlock(qName)) {
				xtw.writeStartElement("p");
				envelopP = true;
			}

			if("label".equals(qName)) {
				//convert label and font which are not part of QTI 2.1 standard to span
				writeStartElement("span", null);
			} else {
				writeStartElement(qName.toLowerCase(), attributes);
			}

			if(subLevel >= 0) {
				subLevel++;
			}
		} catch (XMLStreamException e) {
			throw new SAXException(e);
		}
	}
	
	private void writeStartElement(String qName, Attributes attributes)
	throws XMLStreamException {
		xtw.writeStartElement(qName);
		if(attributes != null) {
			convertAttributes(attributes);
		}
	}
	
	private void convertAttributes(Attributes attributes)
	throws XMLStreamException {
		int numOfAttributes = attributes.getLength();
		for(int i=0;i<numOfAttributes; i++) {
			String attrQName = attributes.getQName(i);
			String attrValue = attributes.getValue(i);
			if("align".equals(attrQName)) {
				//ignore align
			} else if("xmlns".equals(attrQName) && !StringHelper.containsNonWhitespace(attrValue)) {
				//ignore empty schema
			} else if("src".equals(attrQName)) {
				if(attrValue.contains(" ")) {
					String newValue = attrValue.replace(' ', '_');
					materialsMapping.put(attrValue, newValue);
					attrValue = newValue;
				}
				xtw.writeAttribute(attrQName, attrValue);
			} else {
				xtw.writeAttribute(attrQName, attrValue);
			}
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length)
	throws SAXException {
		if(!started) return;
		
		try {
			if(subLevel == 0) {
				if(!envelopP && isCharacterRelevant(ch, start, length)) {
					xtw.writeStartElement("p");
					int diff = trimStart(ch, start, length);
					start += diff;
					length -= diff;
					envelopP = true;
				}
				if(start < 0) {
					start = 0;//Bug neko
				}
				if(start + length > ch.length) {
					length = ch.length - start;// Make sure the length is correct
				}
				xtw.writeCharacters(ch, start, length);
			} else {
				xtw.writeCharacters(ch, start, length);
			}
		} catch (XMLStreamException e) {
			throw new SAXException(e);
		}
	}
	
	private int trimStart(char[] chArray, int start, int length) {
		int end = start + length;
		for(int i=start; i<end; i++) {
			char ch = chArray[i];
			if(ch != '\n' && ch != '\r' && ch != '\t' && ch != ' ') {
				return start - i;
			}
		}
		return 0;
	}
	
	private boolean isCharacterRelevant(char[] chArray, int start, int length) {
		int end = start + length;
		for(int i=start; i<end; i++) {
			char ch = chArray[i];
			if(ch != '\n' && ch != '\r' && ch != '\t' && ch != ' ') {
				return true;
			}
		}
		return false;
	}

	@Override
	public void endElement(String uri, String localName, String qName)
	throws SAXException {
		try {
			qName = qName.toLowerCase();
			if("html".equals(qName) || "head".equals(qName) || "body".equals(qName)) {
				return;
			}
			
			if(skipTags.size() > 0 && skipTags.peek().equals(qName)) {
				skipTags.pop();
				return;
			}
			
			if(subLevel >= 0) {
				subLevel--;
			}

			xtw.writeEndElement();
		} catch (XMLStreamException e) {
			throw new SAXException(e);
		}
	}

	@Override
	public void endDocument() throws SAXException {
		try {
			xtw.writeEndDocument();
	        xtw.flush();
	        xtw.close();
		} catch (XMLStreamException e) {
			throw new SAXException(e);
		}
	}
	
	private boolean isBlock(String qName) {
		switch(qName) {
			case "p":
			case "div":
			case "math":
			case "pre":
			case "h1":
			case "h2":
			case "h3":
			case "h4":
			case "h5":
			case "h6":
			case "address":
			case "dl":
			case "ol":
			case "hr":
			case "ul":
			case "blockquote":
			case "table": return true;
			default: return false;
		}
	}
}