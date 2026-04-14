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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.Test;

/**
 * Tests for DocxThemeParser.
 *
 * @author gnaegi, https://www.frentix.com
 */
public class DocxThemeParserTest {

	private static final String MINIMAL_THEME_XML =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
		+ "<a:theme xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" name=\"Office\">"
		+ "  <a:themeElements>"
		+ "    <a:clrScheme name=\"Office\">"
		+ "      <a:dk1><a:sysClr val=\"windowText\" lastClr=\"000000\"/></a:dk1>"
		+ "      <a:lt1><a:sysClr val=\"window\" lastClr=\"FFFFFF\"/></a:lt1>"
		+ "      <a:dk2><a:srgbClr val=\"44546A\"/></a:dk2>"
		+ "      <a:lt2><a:srgbClr val=\"E7E6E6\"/></a:lt2>"
		+ "      <a:accent1><a:srgbClr val=\"4472C4\"/></a:accent1>"
		+ "      <a:accent2><a:srgbClr val=\"ED7D31\"/></a:accent2>"
		+ "      <a:accent3><a:srgbClr val=\"A5A5A5\"/></a:accent3>"
		+ "      <a:accent4><a:srgbClr val=\"FFC000\"/></a:accent4>"
		+ "      <a:accent5><a:srgbClr val=\"5B9BD5\"/></a:accent5>"
		+ "      <a:accent6><a:srgbClr val=\"70AD47\"/></a:accent6>"
		+ "      <a:hlink><a:srgbClr val=\"0563C1\"/></a:hlink>"
		+ "      <a:folHlink><a:srgbClr val=\"954F72\"/></a:folHlink>"
		+ "    </a:clrScheme>"
		+ "  </a:themeElements>"
		+ "</a:theme>";

	@Test
	public void parseNull() {
		Map<String, String> result = DocxThemeParser.parse(null);
		assertNotNull("Result must not be null for null input", result);
		assertTrue("Result must be empty for null input", result.isEmpty());
	}

	@Test
	public void parseMinimalTheme() {
		byte[] xml = MINIMAL_THEME_XML.getBytes(StandardCharsets.UTF_8);
		Map<String, String> colors = DocxThemeParser.parse(xml);

		assertNotNull("Parsed colors must not be null", colors);
		assertFalse("Parsed colors must not be empty", colors.isEmpty());

		assertEquals("accent1 must be 4472C4", "4472C4", colors.get("accent1"));
		assertEquals("accent2 must be ED7D31", "ED7D31", colors.get("accent2"));
		assertEquals("accent3 must be A5A5A5", "A5A5A5", colors.get("accent3"));
		assertEquals("accent4 must be FFC000", "FFC000", colors.get("accent4"));
		assertEquals("accent5 must be 5B9BD5", "5B9BD5", colors.get("accent5"));
		assertEquals("accent6 must be 70AD47", "70AD47", colors.get("accent6"));
		assertEquals("dk2 must be 44546A", "44546A", colors.get("dk2"));
		assertEquals("lt2 must be E7E6E6", "E7E6E6", colors.get("lt2"));
	}

	@Test
	public void parseSysClr() {
		byte[] xml = MINIMAL_THEME_XML.getBytes(StandardCharsets.UTF_8);
		Map<String, String> colors = DocxThemeParser.parse(xml);

		assertNotNull("Parsed colors must not be null", colors);
		assertEquals("dk1 must use sysClr lastClr value 000000", "000000", colors.get("dk1"));
		assertEquals("lt1 must use sysClr lastClr value FFFFFF", "FFFFFF", colors.get("lt1"));
	}

	@Test
	public void parseAliases() {
		byte[] xml = MINIMAL_THEME_XML.getBytes(StandardCharsets.UTF_8);
		Map<String, String> colors = DocxThemeParser.parse(xml);

		assertNotNull("Parsed colors must not be null", colors);
		assertEquals("bg1 must alias lt1 (FFFFFF)", colors.get("lt1"), colors.get("bg1"));
		assertEquals("tx1 must alias dk1 (000000)", colors.get("dk1"), colors.get("tx1"));
		assertEquals("bg2 must alias lt2 (E7E6E6)", colors.get("lt2"), colors.get("bg2"));
		assertEquals("tx2 must alias dk2 (44546A)", colors.get("dk2"), colors.get("tx2"));
	}

	@Test
	public void parseInvalidXml() {
		byte[] xml = "this is not valid XML <<<>>>".getBytes(StandardCharsets.UTF_8);
		Map<String, String> result = DocxThemeParser.parse(xml);
		assertNotNull("Result must not be null for invalid XML", result);
		assertTrue("Result must be empty for invalid XML", result.isEmpty());
	}

	@Test
	public void parseEmptyClrScheme() {
		String xmlWithoutClrScheme =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<a:theme xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" name=\"Empty\">"
			+ "  <a:themeElements>"
			+ "  </a:themeElements>"
			+ "</a:theme>";
		byte[] xml = xmlWithoutClrScheme.getBytes(StandardCharsets.UTF_8);
		Map<String, String> result = DocxThemeParser.parse(xml);
		assertNotNull("Result must not be null for theme without clrScheme", result);
		assertTrue("Result must be empty for theme without clrScheme", result.isEmpty());
	}
}
