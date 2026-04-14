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
import java.util.Set;

import javax.xml.parsers.SAXParser;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.xml.XMLFactories;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Stage 3: SAX parser for numbering.xml.
 * Resolves numId to DocxNumberingDef (ordered/unordered per indent level).
 *
 * @author gnaegi, https://www.frentix.com
 */
class DocxNumberingParser extends DefaultHandler {

	private static final Logger log = Tracing.createLoggerFor(DocxNumberingParser.class);

	private static final Set<String> ORDERED_FORMATS = Set.of(
		"decimal", "lowerletter", "upperletter", "lowerroman", "upperroman",
		"decimalzero", "ordinal", "cardinaltext", "ordinaltext"
	);

	private final Map<Integer, Map<Integer, Boolean>> abstractNums = new HashMap<>();
	private final Map<Integer, Integer> numToAbstract = new HashMap<>();

	private int currentAbstractNumId = -1;
	private int currentIlvl = -1;
	private int currentNumId = -1;

	private DocxNumberingParser() {
		// use parse()
	}

	static Map<Integer, DocxNumberingDef> parse(byte[] numberingXml) {
		if (numberingXml == null) {
			return Collections.emptyMap();
		}
		DocxNumberingParser handler = new DocxNumberingParser();
		try {
			SAXParser parser = XMLFactories.newSAXParser();
			parser.getXMLReader().setFeature(
				"http://apache.org/xml/features/disallow-doctype-decl", true);
			parser.parse(new ByteArrayInputStream(numberingXml), handler);
		} catch (Exception e) {
			log.warn("Failed to parse DOCX numbering.xml; returning empty map.", e);
			return Collections.emptyMap();
		}
		return handler.buildResult();
	}

	private Map<Integer, DocxNumberingDef> buildResult() {
		Map<Integer, DocxNumberingDef> result = new HashMap<>();
		for (Map.Entry<Integer, Integer> entry : numToAbstract.entrySet()) {
			int numId = entry.getKey();
			int abstractId = entry.getValue();
			Map<Integer, Boolean> levels = abstractNums.getOrDefault(abstractId, Collections.emptyMap());
			result.put(numId, new DocxNumberingDef(Map.copyOf(levels)));
		}
		return Collections.unmodifiableMap(result);
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes)
			throws SAXException {
		String name = stripPrefix(qName);
		switch (name) {
			case "abstractNum":
				currentAbstractNumId = getIntAttr(attributes, "abstractNumId", -1);
				if (currentAbstractNumId >= 0) {
					abstractNums.putIfAbsent(currentAbstractNumId, new HashMap<>());
				}
				break;
			case "lvl":
				currentIlvl = getIntAttr(attributes, "ilvl", -1);
				break;
			case "numFmt":
				if (currentAbstractNumId >= 0 && currentIlvl >= 0) {
					String val = getAttr(attributes, "val");
					if (val != null) {
						boolean ordered = ORDERED_FORMATS.contains(val.toLowerCase());
						Map<Integer, Boolean> levels = abstractNums.get(currentAbstractNumId);
						if (levels != null) {
							levels.put(currentIlvl, ordered);
						}
					}
				}
				break;
			case "num":
				currentNumId = getIntAttr(attributes, "numId", -1);
				break;
			case "abstractNumId":
				if (currentNumId >= 0) {
					int refId = getIntAttr(attributes, "val", -1);
					if (refId >= 0) {
						numToAbstract.put(currentNumId, refId);
					}
				}
				break;
			default:
				break;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		String name = stripPrefix(qName);
		switch (name) {
			case "abstractNum":
				currentAbstractNumId = -1;
				currentIlvl = -1;
				break;
			case "lvl":
				currentIlvl = -1;
				break;
			case "num":
				currentNumId = -1;
				break;
			default:
				break;
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

	private static int getIntAttr(Attributes attributes, String name, int defaultVal) {
		String val = getAttr(attributes, name);
		if (val == null) return defaultVal;
		try {
			return Integer.parseInt(val);
		} catch (NumberFormatException e) {
			return defaultVal;
		}
	}
}
