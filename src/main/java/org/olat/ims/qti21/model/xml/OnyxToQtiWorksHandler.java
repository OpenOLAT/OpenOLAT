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
package org.olat.ims.qti21.model.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

/**
 * It converts Onyx final to qtiWorks. It fix:<br>
 * <ul>
 * 	<li>imsmanifest: type from "imsqti_assessment_xmlv2p1" to "imsqti_test_xmlv2p1"</li>
 *  <li>assessmentTest: surround rubricBlock's text only content with &lt;p&gt;</li>
 *  <li>assesementItem: surround itemBody's text only with &lt;p&gt;</li>
 *  <li>assesementItem: strip html code from &lt;prompt&gt;
 * </ul>
 * 
 * 
 * Initial date: 25.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OnyxToQtiWorksHandler extends DefaultHandler2 {
	
	private final XMLStreamWriter xtw;
	
	private boolean itemBody = false;
	private StringBuilder itemCharacterBuffer;

	private boolean rubricBlock = false;
	private StringBuilder rubricCharacterBuffer;
	
	private boolean prompt = false;
	
	
	public OnyxToQtiWorksHandler(XMLStreamWriter xtw) {
		this.xtw = xtw;
	}

	@Override
	public void startDocument() throws SAXException {
		try{
			xtw.writeStartDocument("utf-8", "1.0");
		} catch (XMLStreamException e) {
			throw new SAXException(e);
		}
	}

	@Override
	public void comment(char[] ch, int start, int length)
	throws SAXException {
		try{
			String comment = new String(ch, start, length);
			xtw.writeComment(comment);
		} catch (XMLStreamException e) {
			throw new SAXException(e);
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes)
	throws SAXException {
		try {
			if(prompt) {
				return;
			}
			
			if(itemBody) {
				String characters = itemCharacterBuffer.toString().trim();
				if(characters.length() > 0) {
					xtw.writeStartElement("p");
					xtw.writeCharacters(characters);
					xtw.writeEndElement();
				}
				itemBody = false;
				itemCharacterBuffer = null;
			} else if(rubricBlock) {
				String characters = rubricCharacterBuffer.toString().trim();
				if(characters.length() > 0) {
					xtw.writeStartElement("p");
					xtw.writeCharacters(characters);
					xtw.writeEndElement();
				}
				
				rubricBlock = false;
				rubricCharacterBuffer = null;
			}
			
			if("label".equals(qName)) {//convert label which are not part of QTI 2.1 standard to span
				xtw.writeStartElement("span");
			} else {
				xtw.writeStartElement(qName);
			}

			if("imscp:resource".equals(qName)) {
				int numOfAttributes = attributes.getLength();
				for(int i=0;i<numOfAttributes; i++) {
					String attrQName = attributes.getQName(i);
					String attrValue = attributes.getValue(i);
					if("type".equals(attrQName) && "imsqti_assessment_xmlv2p1".equals(attrValue)) {
						xtw.writeAttribute(attrQName, "imsqti_test_xmlv2p1");
					} else {
						xtw.writeAttribute(attrQName, attrValue);
					}
				}
			} else {
				int numOfAttributes = attributes.getLength();
				for(int i=0;i<numOfAttributes; i++) {
					String attrQName = attributes.getQName(i);
					String attrValue = attributes.getValue(i);
					xtw.writeAttribute(attrQName, attrValue);
				}
			}

			if("itemBody".equals(qName)) {
				itemBody = true;
				itemCharacterBuffer = new StringBuilder();
			} else if("rubricBlock".equals(qName)) {
				rubricBlock = true;
				rubricCharacterBuffer = new StringBuilder();
			} else if("prompt".equals(qName)) {
				prompt = true;
			}
			
			
		} catch (XMLStreamException e) {
			throw new SAXException(e);
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length)
	throws SAXException {
		if(itemBody) {
			itemCharacterBuffer.append(ch, start, length);
		} else if(rubricBlock) {
			rubricCharacterBuffer.append(ch, start, length);
		} else {
			try {
				xtw.writeCharacters(ch, start, length);
			} catch (XMLStreamException e) {
				throw new SAXException(e);
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
	throws SAXException {
		try {
			if(rubricBlock) {
				String characters = rubricCharacterBuffer.toString().trim();
				if(characters.length() > 0) {
					xtw.writeStartElement("p");
					xtw.writeCharacters(characters);
					xtw.writeEndElement();
				}
				
				rubricBlock = false;
				rubricCharacterBuffer = null;
			} else if(prompt) {
				if(!"prompt".equals(qName)) {
					return;//only print characters
				} else {
					prompt = false;
				}
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
}