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
public class OnyxToQtiWorksHandler extends DefaultHandler2 {
	
	private final QTI21Infos infos;
	private final XMLStreamWriter xtw;
	
	private boolean itemBody = false;
	private StringBuilder itemCharacterBuffer;

	private boolean rubricBlock = false;
	private StringBuilder rubricCharacterBuffer;
	
	private boolean prompt = false;
	
	
	public OnyxToQtiWorksHandler(XMLStreamWriter xtw, QTI21Infos infos) {
		this.xtw = xtw;
		this.infos = infos;
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
			} else if("assessmentItem".equals(qName) || "assessmentTest".equals(qName)) {
				writeAssessmentElementAttributes(attributes);
			} else if("object".equals(qName)) {
				writeObjectElementAttributes(attributes);
			} else if("img".equals(qName)) {
				writeImgElementAttributes(attributes);
			} else if("customOperator".equals(qName)) {
				writeCustomOperatorAttributes(attributes);
			} else if("mapTolResponse".equals(qName)) {
				writeMapTo1ResponseElement(attributes);
			}  else {
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
	
	private void writeObjectElementAttributes(Attributes attributes)
	throws XMLStreamException {
		int numOfAttributes = attributes.getLength();
		for(int i=0;i<numOfAttributes; i++) {
			String attrQName = attributes.getQName(i);
			String attrValue = attributes.getValue(i);
			if("data".equals(attrQName)) {
				if(attrValue.contains("%2F")) {
					attrValue = attrValue.replace("%2F", "/");
				}
			}
			xtw.writeAttribute(attrQName, attrValue);
		}
	}
	
	private void writeImgElementAttributes(Attributes attributes)
	throws XMLStreamException {
		int numOfAttributes = attributes.getLength();
		for(int i=0;i<numOfAttributes; i++) {
			String attrQName = attributes.getQName(i);
			String attrValue = attributes.getValue(i);
			if("src".equals(attrQName)) {
				if(attrValue.contains("%2F")) {
					attrValue = attrValue.replace("%2F", "/");
				}
			}
			xtw.writeAttribute(attrQName, attrValue);
		}
	}
	
	private void writeAssessmentElementAttributes(Attributes attributes)
	throws XMLStreamException {
		boolean hasToolName = false;
		boolean hasEditor = false;
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
				hasEditor = true;
			}
		}
		
		if(!hasToolName && infos != null && StringHelper.containsNonWhitespace(infos.getEditor())) {
			xtw.writeAttribute("toolName", infos.getEditor());
		}
		if(!hasEditor && infos != null && StringHelper.containsNonWhitespace(infos.getVersion())) {
			xtw.writeAttribute("toolVersion", infos.getVersion());
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
	
	/**
	 * The customOperator accept the class attribute or the definition attribute but not
	 * both at the same time.
	 * 
	 * @param attributes The attributes
	 * @param withDefinition true if you want to write the definition attribute, false if you want to skip it
	 * @throws XMLStreamException
	 */
	private void writeCustomOperatorAttributes(Attributes attributes)
	throws XMLStreamException {
		String customOperatorDefinition = attributes.getValue("definition");
		boolean maxima = "MAXIMA".equals(customOperatorDefinition);
		if(maxima) {
			xtw.writeAttribute("class", "org.olat.ims.qti21.manager.extensions.MaximaOperator");
		}
		
		int numOfAttributes = attributes.getLength();
		for(int i=0;i<numOfAttributes; i++) {
			String attrQName = attributes.getQName(i);
			if(maxima && "class".equals(attrQName)) {
				continue;
			}
			
			String attrValue = attributes.getValue(i);
			xtw.writeAttribute(attrQName, attrValue);
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
				// replace MathJax start and end \( \
				String text = new String(ch, start, length);
				if(text.contains("\\(") || text.contains("\\)")) {
					processLatexParenthesis(text);
				} else if(text.contains("$$")) {
					processLatexDollar(text);
				} else {
					xtw.writeCharacters(ch, start, length);
				}
			} catch (XMLStreamException e) {
				throw new SAXException(e);
			}
		}
	}
	
	private boolean latexDollarOpen = false;
	
	private void processLatexDollar(String text)
	throws XMLStreamException {
		for(int i=100; i-->0; ) {
		
			int index = text.indexOf("$$");
			if(index < 0) {
				char[] lastBits = text.toCharArray();
				xtw.writeCharacters(lastBits, 0, lastBits.length);
				break;
			} else {
				String startText = text.substring(0, index);
				if(startText.length() > 0) {
					char[] startBits = startText.toCharArray();
					xtw.writeCharacters(startBits, 0, startBits.length);
				}
				
				if(latexDollarOpen) {
					xtw.writeEndElement();
					latexDollarOpen = false;
				} else {
					xtw.writeStartElement("span");
					xtw.writeAttribute("class", "math");
					latexDollarOpen = true;
				}
				text = text.substring(index + 2, text.length());
			}
		}
	}
	
	private void processLatexParenthesis(String text)
	throws XMLStreamException {
		for(int i=100; i-->0; ) {
		
			int indexOpen = text.indexOf("\\(");
			int indexClose = text.indexOf("\\)");
			if(indexOpen < 0 && indexClose < 0) {
				char[] lastBits = text.toCharArray();
				xtw.writeCharacters(lastBits, 0, lastBits.length);
				break;
			} else if((indexOpen >= 0 && indexOpen < indexClose) || (indexOpen >= 0 && indexClose < 0)) {
				String startText = text.substring(0, indexOpen);
				if(startText.length() > 0) {
					char[] startBits = startText.toCharArray();
					xtw.writeCharacters(startBits, 0, startBits.length);
				}
				
				xtw.writeStartElement("span");
				xtw.writeAttribute("class", "math");
				text = text.substring(indexOpen + 2, text.length());
			} else if((indexClose >= 0 && indexOpen > indexClose) || (indexClose >= 0 && indexOpen < 0))  {
				String startText = text.substring(0, indexClose);
				if(startText.length() > 0) {
					char[] startBits = startText.toCharArray();
					xtw.writeCharacters(startBits, 0, startBits.length);
				}
				
				xtw.writeEndElement();
				text = text.substring(indexClose + 2, text.length());
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