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
package org.olat.core.util.openxml;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class OpenXMLStyles {
	
	private static final Logger log = Tracing.createLoggerFor(OpenXMLStyles.class);
	
	private final Document document;
	private final Element stylesElement;
	
	public OpenXMLStyles() {
		document = createDocument();
		stylesElement = createRootElement(document);
		createDocDefaultElement(stylesElement, document);
	}
	
	public Document getDocument() {
		return document;
	}
	
	public Element getStylesElement() {
		return stylesElement;
	}
	
	private final Document createDocument() {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			// Turn on validation, and turn off namespaces
			factory.setValidating(true);
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			return builder.newDocument();
		} catch (ParserConfigurationException e) {
			log.error("", e);
			return null;
		}
	}

	/*
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:styles xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006"
  xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
  xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
  xmlns:w14="http://schemas.microsoft.com/office/word/2010/wordml" mc:Ignorable="w14">
	 */
	private final Element createRootElement(Document doc) {
		Element docEl = (Element)doc.appendChild(doc.createElement("w:styles"));
		docEl.setAttribute("xmlns:mc","http://schemas.openxmlformats.org/markup-compatibility/2006");
		docEl.setAttribute("xmlns:r","http://schemas.openxmlformats.org/officeDocument/2006/relationships");
		docEl.setAttribute("xmlns:w","http://schemas.openxmlformats.org/wordprocessingml/2006/main");
		docEl.setAttribute("xmlns:w14","http://schemas.microsoft.com/office/word/2010/wordml");
		docEl.setAttribute("mc:Ignorable","w14");
		return docEl;
	}
	
	/*
	<w:docDefaults>
		<w:rPrDefault>
			<w:rPr>
				<w:rFonts w:asciiTheme="minorHAnsi" w:eastAsiaTheme="minorEastAsia" w:hAnsiTheme="minorHAnsi" w:cstheme="minorBidi" />
				 <w:sz w:val="24" />
				 <w:szCs w:val="24" />
				 <w:lang w:val="fr-FR" w:eastAsia="de-DE" w:bidi="ar-SA" />
			</w:rPr>
		</w:rPrDefault>
		<w:pPrDefault />
	</w:docDefaults>
	 */
	
	private final Element createDocDefaultElement(Element rootElement, Document doc) {
		Element docDefaultsEl = (Element)rootElement.appendChild(doc.createElement("w:docDefaults"));
		Node rPrDefaultEl = docDefaultsEl.appendChild(doc.createElement("w:rPrDefault"));
		Node rPrEl = rPrDefaultEl.appendChild(doc.createElement("w:rPr"));
		
		//default fonts
		Element rFontsEl = (Element)rPrEl.appendChild(doc.createElement("w:rFonts"));
		rFontsEl.setAttribute("w:asciiTheme", "minorHAnsi");
		rFontsEl.setAttribute("w:eastAsiaTheme", "minorEastAsia");
		rFontsEl.setAttribute("w:hAnsiTheme", "minorHAnsi");
		rFontsEl.setAttribute("w:cstheme", "minorBidi");

		//size
		Element sizeEl = (Element)rPrEl.appendChild(doc.createElement("w:sz"));
		sizeEl.setAttribute("w:val", "24");
		Element sizeCsEl = (Element)rPrEl.appendChild(doc.createElement("w:szCs"));
		sizeCsEl.setAttribute("w:val", "24");

		return docDefaultsEl;
	}
}
