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

import java.util.ArrayDeque;
import java.util.Deque;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.olat.core.util.StringHelper;
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
public class Onyx38ToQtiWorksHandler extends DefaultHandler2 {

	private static final String VERSION_MARKER = "Version ";
	
	private final XMLStreamWriter xtw;

	private String version;
	private int pLevel = -1;
	private int liLevel = -1;
	private int itemBodySubLevel = -1;
	private Deque<String> skipTags = new ArrayDeque<>();

	private boolean envelopP = false;
	
	private boolean rubricBlock = false;
	private StringBuilder rubricCharacterBuffer;
	
	private boolean prompt = false;
	
	public Onyx38ToQtiWorksHandler(XMLStreamWriter xtw) {
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
			if(comment.contains("Onyx Editor")) {
				int versionIndex = comment.indexOf(VERSION_MARKER);
				if(versionIndex > 0) {
					int offset = VERSION_MARKER.length();
					version = comment.substring(versionIndex + offset, comment.indexOf(' ', versionIndex + offset));
				}
			}
			xtw.writeComment(comment);
		} catch (XMLStreamException e) {
			throw new SAXException(e);
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes)
	throws SAXException {
		try {
			if(prompt || checkIfNeedToSkip(qName, attributes)) {
				return;
			}
			
			if(rubricBlock) {
				String characters = rubricCharacterBuffer.toString().trim();
				if(characters.length() > 0) {
					xtw.writeStartElement("p");
					xtw.writeCharacters(characters);
					xtw.writeEndElement();
				}
				
				rubricBlock = false;
				rubricCharacterBuffer = null;
			}
			
			if(itemBodySubLevel == 0 && envelopP && isBlock(qName)) {
				xtw.writeEndElement();
				envelopP = false;
			}
			
			if("p".equals(qName)) {
				pLevel++;
				if(pLevel == 0) {
					writeStartElement(qName, attributes);
				}
			} else if("li".equals(qName)) {
				liLevel++;
				if(liLevel == 0) {
					writeStartElement(qName, attributes);
				}
			} else if("assessmentItem".equals(qName) || "assessmentTest".equals(qName)) {
				writeAssessmentElement(qName, attributes);
			} else if("mapTolResponse".equals(qName)) {
				writeMapTo1ResponseElement(attributes);
			} else {
				if(itemBodySubLevel == 0 && !envelopP && !isBlock(qName)) {
					xtw.writeStartElement("p");
					envelopP = true;
				}

				if("label".equals(qName)) {
					//convert label and font which are not part of QTI 2.1 standard to span
					writeStartElement("span", null);
				} else {
					writeStartElement(qName, attributes);
				}
			}

			if("itemBody".equals(qName)) {
				itemBodySubLevel = 0;
			} else if("rubricBlock".equals(qName)) {
				rubricBlock = true;
				rubricCharacterBuffer = new StringBuilder();
			} else if("prompt".equals(qName)) {
				prompt = true;
			} else if(itemBodySubLevel >= 0) {
				itemBodySubLevel++;
			}
		} catch (XMLStreamException e) {
			throw new SAXException(e);
		}
	}
	
	private void writeStartElement(String qName, Attributes attributes)
	throws XMLStreamException {
		xtw.writeStartElement(qName);
		if(attributes != null) {
			if("imscp:resource".equals(qName)) {
				convertImsCPResource(attributes);
			} else {
				convertAttributes(attributes);
			}
		}
	}
	
	private boolean checkIfNeedToSkip(String qName, Attributes attributes) {
		if("font".equals(qName)) {
			skipTags.push(qName);
			return true;
		} else if("a".equals(qName) && attributes.getValue("name") != null) {
			skipTags.push(qName);
			return true;
		} else if("span".equals(qName) && itemBodySubLevel == 0) {
			skipTags.push(qName);
			return true;
		}
		
		return false;
	}
	
	private void convertImsCPResource(Attributes attributes)
	throws XMLStreamException {
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
	}
	
	private void writeAssessmentElement(String qName, Attributes attributes)
	throws XMLStreamException {
		boolean hasToolName = false;
		boolean hasVersion = false;
		xtw.writeStartElement(qName);
		int numOfAttributes = attributes.getLength();
		for(int i=0;i<numOfAttributes; i++) {
			String attrQName = attributes.getQName(i);
			String attrValue = attributes.getValue(i);
			if("xsi:schemaLocation".equals(attrQName)) {
				attrValue = attrValue.replace("http://www.w3.org/1998/Math/MathML http://www.w3.org/Math/XMLSchema/mathml2/mathml2.xsd", "");
			}
			xtw.writeAttribute(attrQName, attrValue);
			if("toolName".equals(attrQName)) {
				hasToolName = true;
			} else if("toolVersion".equals(attrQName)) {
				hasVersion = true;
			}
		}
		
		if(!hasToolName) {
			xtw.writeAttribute("toolName", "Onyx Editor");
		}
		if(!hasVersion && StringHelper.containsNonWhitespace(version)) {
			xtw.writeAttribute("toolVersion", version);
		}
	}
	
	private void writeMapTo1ResponseElement(Attributes attributes)
	throws XMLStreamException {
		xtw.writeStartElement("mapResponse");
		int numOfAttributes = attributes.getLength();
		for(int i=0;i<numOfAttributes; i++) {
			String attrQName = attributes.getQName(i);
			if(!"tolerance".equals(attrQName) && !"toleranceMode".equals(attrQName) && !"xmlns".equals(attrQName)) {
				String attrValue = attributes.getValue(i);
				xtw.writeAttribute(attrQName, attrValue);
			}
		}
	}
	
	private void convertAttributes(Attributes attributes)
	throws XMLStreamException {
		int numOfAttributes = attributes.getLength();
		for(int i=0;i<numOfAttributes; i++) {
			String attrQName = attributes.getQName(i);
			String attrValue = attributes.getValue(i);
			if("align".equals(attrQName)) {//TODO target??
				//ignore align
			} else if("xmlns".equals(attrQName) && !StringHelper.containsNonWhitespace(attrValue)) {
				//ignore empty schema
			} else {
				xtw.writeAttribute(attrQName, attrValue);
			}
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length)
	throws SAXException {
		try {
			if(itemBodySubLevel == 0) {
				if(!envelopP && isCharacterRelevant(ch, start, length)) {
					xtw.writeStartElement("p");
					int diff = trimStart(ch, start, length);
					start += diff;
					length -= diff;
					envelopP = true;
				}
				xtw.writeCharacters(ch, start, length);
			} else if(rubricBlock) {
				rubricCharacterBuffer.append(ch, start, length);
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
			if(skipTags.size() > 0 && skipTags.peek().equals(qName)) {
				skipTags.pop();
				return;
			}
			
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
			} else if(itemBodySubLevel >= 0) {
				itemBodySubLevel--;
			}

			if("itemBody".equals(qName)) {
				if(envelopP) {
					xtw.writeEndElement();//p
					envelopP = false;
				}
				xtw.writeEndElement();//itemBody
				itemBodySubLevel = -1;
			} else if("p".equals(qName)) {
				if(pLevel == 0) {
					xtw.writeEndElement();
				}
				pLevel--;
			} else if("li".equals(qName)) {
				if(liLevel == 0) {
					xtw.writeEndElement();
				}
				liLevel--;
			} else {
				xtw.writeEndElement();
			}
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
	
	/**
	 * The list of block elements allowed in itemBody.<br>
	 * 
	 * "http://www.imsglobal.org/xsd/imsqti_v2p1":rubricBlock,
	 * "http://www.imsglobal.org/xsd/imsqti_v2p1":positionObjectStage,
	 * "http://www.imsglobal.org/xsd/imsqti_v2p1":customInteraction,
	 * "http://www.imsglobal.org/xsd/imsqti_v2p1":drawingInteraction,
	 * "http://www.imsglobal.org/xsd/imsqti_v2p1":gapMatchInteraction,
	 * "http://www.imsglobal.org/xsd/imsqti_v2p1":matchInteraction,
	 * "http://www.imsglobal.org/xsd/imsqti_v2p1":graphicGapMatchInteraction,
	 * "http://www.imsglobal.org/xsd/imsqti_v2p1":hotspotInteraction,
	 * "http://www.imsglobal.org/xsd/imsqti_v2p1":graphicOrderInteraction,
	 * "http://www.imsglobal.org/xsd/imsqti_v2p1":selectPointInteraction,
	 * "http://www.imsglobal.org/xsd/imsqti_v2p1":graphicAssociateInteraction,
	 * "http://www.imsglobal.org/xsd/imsqti_v2p1":sliderInteraction,
	 * "http://www.imsglobal.org/xsd/imsqti_v2p1":choiceInteraction,
	 * "http://www.imsglobal.org/xsd/imsqti_v2p1":mediaInteraction,
	 * "http://www.imsglobal.org/xsd/imsqti_v2p1":hottextInteraction,
	 * "http://www.imsglobal.org/xsd/imsqti_v2p1":orderInteraction,
	 * "http://www.imsglobal.org/xsd/imsqti_v2p1":extendedTextInteraction,
	 * "http://www.imsglobal.org/xsd/imsqti_v2p1":uploadInteraction,
	 * "http://www.imsglobal.org/xsd/imsqti_v2p1":associateInteraction,
	 * "http://www.imsglobal.org/xsd/imsqti_v2p1":feedbackBlock,
	 * "http://www.imsglobal.org/xsd/imsqti_v2p1":templateBlock,
	 * "http://www.imsglobal.org/xsd/imsqti_v2p1":infoControl,
	 * "http://www.w3.org/1998athathML":math,
	 * "http://www.w3.org/2001/XInclude":include,
	 * "http://www.imsglobal.org/xsd/imsqti_v2p1":pre,
	 * "http://www.imsglobal.org/xsd/imsqti_v2p1":h1, "http://www.imsglobal.org/xsd/imsqti_v2p1":h2, "http://www.imsglobal.org/xsd/imsqti_v2p1":h3, "http://www.imsglobal.org/xsd/imsqti_v2p1":h4, "http://www.imsglobal.org/xsd/imsqti_v2p1":h5, "http://www.imsglobal.org/xsd/imsqti_v2p1":h6,
	 * "http://www.imsglobal.org/xsd/imsqti_v2p1":p,
	 * "http://www.imsglobal.org/xsd/imsqti_v2p1":address,
	 * "http://www.imsglobal.org/xsd/imsqti_v2p1":dl,
	 * "http://www.imsglobal.org/xsd/imsqti_v2p1":ol,
	 * "http://www.imsglobal.org/xsd/imsqti_v2p1":hr,
	 * "http://www.imsglobal.org/xsd/imsqti_v2p1":ul,
	 * "http://www.imsglobal.org/xsd/imsqti_v2p1":blockquote,
	 * "http://www.imsglobal.org/xsd/imsqti_v2p1":table,
	 * "http://www.imsglobal.org/xsd/imsqti_v2p1":div
	 *
	 * @param qName
	 * @return
	 */
	private boolean isBlock(String qName) {
		switch(qName) {
			case "p":
			case "div":
			case "positionObjectStage":
			case "customInteraction":
			case "drawingInteraction":
			case "gapMatchInteraction":
			case "matchInteraction":
			case "graphicGapMatchInteraction":
			case "hotspotInteraction":
			case "selectPointInteraction":
			case "graphicAssociateInteraction":
			case "sliderInteraction":
			case "choiceInteraction":
			case "mediaInteraction":
			case "hottextInteraction":
			case "orderInteraction":
			case "extendedTextInteraction":
			case "uploadInteraction":
			case "associateInteraction":
			case "feedbackBlock":
			case "templateBlock":
			case "infoControl":
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
			case "table":
			case "rubricBlock": return true;
			default: return false;
		}
	}
}