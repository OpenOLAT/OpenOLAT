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
 * Parses the OOXML theme (word/theme/theme1.xml) to extract the color scheme.
 * Maps scheme color names (dk1, lt1, accent1, etc.) to hex RGB values.
 *
 * @author gnaegi, https://www.frentix.com
 */
class DocxThemeParser {

	private static final Logger log = Tracing.createLoggerFor(DocxThemeParser.class);

	private DocxThemeParser() { /* utility */ }

	/**
	 * Parse theme XML and return scheme color mappings.
	 *
	 * @param themeXml raw XML bytes of word/theme/theme1.xml (may be null)
	 * @return map from scheme color name to hex RGB (e.g., "accent1" → "4472C4");
	 *         empty map if input is null or parsing fails
	 */
	static Map<String, String> parse(byte[] themeXml) {
		if (themeXml == null) {
			return Collections.emptyMap();
		}
		ThemeHandler handler = new ThemeHandler();
		try {
			SAXParser parser = XMLFactories.newSAXParser();
			parser.getXMLReader().setFeature(
				"http://apache.org/xml/features/disallow-doctype-decl", true);
			parser.parse(new ByteArrayInputStream(themeXml), handler);
		} catch (Exception e) {
			log.debug("Failed to parse theme XML.", e);
			return Collections.emptyMap();
		}
		return Collections.unmodifiableMap(handler.colors);
	}

	private static class ThemeHandler extends DefaultHandler {

		private final Map<String, String> colors = new HashMap<>();
		private boolean inClrScheme;
		private String currentSchemeColor;

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException {
			String name = stripPrefix(qName);
			if ("clrScheme".equals(name)) {
				inClrScheme = true;
			} else if (inClrScheme) {
				if (currentSchemeColor == null) {
					// Direct child of clrScheme — this is the scheme color name (dk1, lt1, accent1, …)
					currentSchemeColor = name;
				} else if ("srgbClr".equals(name)) {
					String val = attributes.getValue("val");
					if (val != null && currentSchemeColor != null) {
						colors.put(currentSchemeColor, val);
					}
				} else if ("sysClr".equals(name)) {
					String lastClr = attributes.getValue("lastClr");
					if (lastClr != null && currentSchemeColor != null) {
						colors.put(currentSchemeColor, lastClr);
					}
				}
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			String name = stripPrefix(qName);
			if ("clrScheme".equals(name)) {
				inClrScheme = false;
				currentSchemeColor = null;
				addAliases();
			} else if (inClrScheme && name.equals(currentSchemeColor)) {
				currentSchemeColor = null;
			}
		}

		private void addAliases() {
			putAlias("bg1", "lt1");
			putAlias("tx1", "dk1");
			putAlias("bg2", "lt2");
			putAlias("tx2", "dk2");
		}

		private void putAlias(String alias, String source) {
			String color = colors.get(source);
			if (color != null) {
				colors.put(alias, color);
			}
		}

		private static String stripPrefix(String qName) {
			int idx = qName.indexOf(':');
			return idx >= 0 ? qName.substring(idx + 1) : qName;
		}
	}
}
