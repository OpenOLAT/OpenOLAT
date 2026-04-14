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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.xml.XMLFactories;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Stage 4: SAX parser for styles.xml.
 * Maps styleId to normalized style name for paragraph styles.
 *
 * @author gnaegi, https://www.frentix.com
 */
class DocxStyleParser extends DefaultHandler {

	private static final Logger log = Tracing.createLoggerFor(DocxStyleParser.class);
	private static final Pattern HEADING_PATTERN = Pattern.compile("heading\\s*(\\d+)", Pattern.CASE_INSENSITIVE);

	private final Map<String, String> styles = new HashMap<>();

	private boolean inRelevantStyle;
	private String currentStyleId;

	private DocxStyleParser() {
		// use parse()
	}

	static Map<String, String> parse(byte[] stylesXml) {
		if (stylesXml == null) {
			return Collections.emptyMap();
		}
		DocxStyleParser handler = new DocxStyleParser();
		try {
			SAXParser parser = XMLFactories.newSAXParser();
			parser.getXMLReader().setFeature(
				"http://apache.org/xml/features/disallow-doctype-decl", true);
			parser.parse(new ByteArrayInputStream(stylesXml), handler);
		} catch (Exception e) {
			log.warn("Failed to parse DOCX styles.xml; returning empty map.", e);
			return Collections.emptyMap();
		}
		return Collections.unmodifiableMap(handler.styles);
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes)
			throws SAXException {
		String name = stripPrefix(qName);

		if ("style".equals(name)) {
			String type = getAttr(attributes, "type");
			// Collect both paragraph and character styles
			if ("paragraph".equals(type) || "character".equals(type)) {
				inRelevantStyle = true;
				currentStyleId = getAttr(attributes, "styleId");
			} else {
				inRelevantStyle = false;
				currentStyleId = null;
			}
		} else if ("name".equals(name) && inRelevantStyle && currentStyleId != null) {
			String val = getAttr(attributes, "val");
			if (val != null) {
				styles.put(currentStyleId, normalizeName(val));
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if ("style".equals(stripPrefix(qName))) {
			inRelevantStyle = false;
			currentStyleId = null;
		}
	}

	static String normalizeName(String rawName) {
		String lower = rawName.toLowerCase().trim();

		Matcher headingMatcher = HEADING_PATTERN.matcher(lower);
		if (headingMatcher.matches()) {
			return "Heading" + headingMatcher.group(1);
		}
		return switch (lower) {
			// Paragraph styles
			case "title" -> "Title";
			case "subtitle", "untertitel" -> "Subtitle";
			case "quote", "block quote", "blockquote", "zitat", "citation" -> "Quote";
			case "intense quote", "intensequote", "intensives zitat" -> "IntenseQuote";
			case "list paragraph", "listparagraph", "listenabsatz" -> "ListParagraph";
			// Character styles
			case "emphasis", "hervorhebung" -> "Emphasis";
			case "intense emphasis", "intenseemphasis", "starke hervorhebung",
				 "intensive hervorhebung" -> "IntenseEmphasis";
			case "strong", "fett" -> "Strong";
			case "subtle emphasis", "subtleemphasis", "schwache hervorhebung" -> "SubtleEmphasis";
			case "book title", "buchtitel" -> "BookTitle";
			default -> rawName;
		};
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
