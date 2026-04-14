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
package org.olat.core.util.docxToMarkdown;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.xml.XMLFactories;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX parser for word/footnotes.xml and word/endnotes.xml.
 * Extracts footnote/endnote text content keyed by ID.
 *
 * @author gnaegi, https://www.frentix.com
 */
class DocxFootnoteParser extends DefaultHandler {

	private static final Logger log = Tracing.createLoggerFor(DocxFootnoteParser.class);

	private final Map<String, String> notes = new HashMap<>();
	private String currentId;
	private final StringBuilder currentText = new StringBuilder();
	private boolean inNote;

	private DocxFootnoteParser() {
		// use parse()
	}

	/**
	 * Parse footnotes.xml or endnotes.xml and return a map of id → text content.
	 * IDs "0" and "-1" are skipped (separator and continuation separator).
	 */
	static Map<String, String> parse(byte[] xml) {
		if (xml == null) {
			return Collections.emptyMap();
		}
		DocxFootnoteParser handler = new DocxFootnoteParser();
		try {
			SAXParser parser = XMLFactories.newSAXParser();
			parser.getXMLReader().setFeature(
				"http://apache.org/xml/features/disallow-doctype-decl", true);
			parser.parse(new ByteArrayInputStream(xml), handler);
		} catch (Exception e) {
			log.warn("Failed to parse footnotes/endnotes XML.", e);
			return Collections.emptyMap();
		}
		return Collections.unmodifiableMap(handler.notes);
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes)
			throws SAXException {
		String name = stripPrefix(qName);
		if ("footnote".equals(name) || "endnote".equals(name)) {
			currentId = getAttr(attributes, "id");
			currentText.setLength(0);
			inNote = true;
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (inNote) {
			currentText.append(ch, start, length);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		String name = stripPrefix(qName);
		if (("footnote".equals(name) || "endnote".equals(name)) && inNote) {
			inNote = false;
			if (currentId != null && !"0".equals(currentId) && !"-1".equals(currentId)) {
				String text = currentText.toString().trim();
				if (!text.isEmpty()) {
					notes.put(currentId, text);
				}
			}
			currentId = null;
		}
	}

	private static String stripPrefix(String qName) {
		int idx = qName.indexOf(':');
		return idx >= 0 ? qName.substring(idx + 1) : qName;
	}

	private static String getAttr(Attributes attributes, String name) {
		String val = attributes.getValue("w:" + name);
		if (val == null) {
			val = attributes.getValue(name);
		}
		return val;
	}
}
