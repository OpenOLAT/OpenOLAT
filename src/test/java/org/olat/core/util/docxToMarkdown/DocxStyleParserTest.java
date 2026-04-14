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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.Test;

/**
 * Unit tests for {@link DocxStyleParser}.
 *
 * @author frentix GmbH
 */
public class DocxStyleParserTest {

	private static final String W_NS =
		"xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static byte[] xml(String body) {
		String full = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<w:styles " + W_NS + ">" + body + "</w:styles>";
		return full.getBytes(StandardCharsets.UTF_8);
	}

	/** Builds a paragraph style element. */
	private static String paragraphStyle(String styleId, String nameVal) {
		return "<w:style w:type=\"paragraph\" w:styleId=\"" + styleId + "\">" +
			"<w:name w:val=\"" + nameVal + "\"/>" +
			"</w:style>";
	}

	/** Builds a character style element. */
	private static String characterStyle(String styleId, String nameVal) {
		return "<w:style w:type=\"character\" w:styleId=\"" + styleId + "\">" +
			"<w:name w:val=\"" + nameVal + "\"/>" +
			"</w:style>";
	}

	// -----------------------------------------------------------------------

	@Test
	public void parseNull() {
		Map<String, String> result = DocxStyleParser.parse(null);
		assertNotNull(result);
		assertTrue("Null input must produce an empty map", result.isEmpty());
	}

	@Test
	public void parseHeadingStyles() {
		StringBuilder body = new StringBuilder();
		for (int i = 1; i <= 6; i++) {
			body.append(paragraphStyle("Heading" + i, "heading " + i));
		}
		Map<String, String> result = DocxStyleParser.parse(xml(body.toString()));

		for (int i = 1; i <= 6; i++) {
			String key = "Heading" + i;
			assertTrue("Style '" + key + "' must be present", result.containsKey(key));
			assertEquals("heading " + i + " must normalize to Heading" + i,
				"Heading" + i, result.get(key));
		}
	}

	@Test
	public void parseTitleStyle() {
		byte[] stylesXml = xml(paragraphStyle("Title", "Title"));
		Map<String, String> result = DocxStyleParser.parse(stylesXml);

		assertTrue("Title style must be present", result.containsKey("Title"));
		assertEquals("Title", result.get("Title"));
	}

	@Test
	public void parseQuoteStyle() {
		byte[] stylesXml = xml(paragraphStyle("Quote", "Quote"));
		Map<String, String> result = DocxStyleParser.parse(stylesXml);

		assertTrue("Quote style must be present", result.containsKey("Quote"));
		assertEquals("Quote", result.get("Quote"));
	}

	@Test
	public void parseIntenseQuoteStyle() {
		byte[] stylesXml = xml(paragraphStyle("IntenseQuote", "Intense Quote"));
		Map<String, String> result = DocxStyleParser.parse(stylesXml);

		assertTrue("IntenseQuote style must be present", result.containsKey("IntenseQuote"));
		assertEquals("IntenseQuote", result.get("IntenseQuote"));
	}

	@Test
	public void parseCharacterStyle() {
		byte[] stylesXml = xml(characterStyle("Emphasis", "Emphasis"));
		Map<String, String> result = DocxStyleParser.parse(stylesXml);

		assertTrue("Character styles must be included in the map",
			result.containsKey("Emphasis"));
		assertEquals("Emphasis", result.get("Emphasis"));
	}

	@Test
	public void parseGermanEmphasis() {
		byte[] stylesXml = xml(characterStyle("Hervorhebung", "Hervorhebung"));
		Map<String, String> result = DocxStyleParser.parse(stylesXml);

		assertEquals("German Hervorhebung must normalize to Emphasis",
			"Emphasis", result.get("Hervorhebung"));
	}
}
