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
 * Unit tests for {@link DocxNumberingParser}.
 *
 * @author frentix GmbH
 */
public class DocxNumberingParserTest {

	private static final String W_NS =
		"xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static byte[] xml(String body) {
		String full = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<w:numbering " + W_NS + ">" + body + "</w:numbering>";
		return full.getBytes(StandardCharsets.UTF_8);
	}

	// -----------------------------------------------------------------------

	@Test
	public void parseNull() {
		Map<Integer, DocxNumberingDef> result = DocxNumberingParser.parse(null);
		assertNotNull(result);
		assertTrue("Null input must produce an empty map", result.isEmpty());
	}

	@Test
	public void parseBulletList() {
		byte[] numberingXml = xml(
			"<w:abstractNum w:abstractNumId=\"0\">" +
			"  <w:lvl w:ilvl=\"0\"><w:numFmt w:val=\"bullet\"/></w:lvl>" +
			"</w:abstractNum>" +
			"<w:num w:numId=\"1\">" +
			"  <w:abstractNumId w:val=\"0\"/>" +
			"</w:num>"
		);
		Map<Integer, DocxNumberingDef> result = DocxNumberingParser.parse(numberingXml);

		assertTrue("numId 1 must be present", result.containsKey(1));
		DocxNumberingDef def = result.get(1);
		assertFalse("bullet numFmt must produce isOrdered=false at ilvl 0", def.isOrdered(0));
	}

	@Test
	public void parseNumberedList() {
		byte[] numberingXml = xml(
			"<w:abstractNum w:abstractNumId=\"0\">" +
			"  <w:lvl w:ilvl=\"0\"><w:numFmt w:val=\"decimal\"/></w:lvl>" +
			"</w:abstractNum>" +
			"<w:num w:numId=\"1\">" +
			"  <w:abstractNumId w:val=\"0\"/>" +
			"</w:num>"
		);
		Map<Integer, DocxNumberingDef> result = DocxNumberingParser.parse(numberingXml);

		assertTrue("numId 1 must be present", result.containsKey(1));
		DocxNumberingDef def = result.get(1);
		assertTrue("decimal numFmt must produce isOrdered=true at ilvl 0", def.isOrdered(0));
	}

	@Test
	public void parseMultiLevelMixed() {
		byte[] numberingXml = xml(
			"<w:abstractNum w:abstractNumId=\"0\">" +
			"  <w:lvl w:ilvl=\"0\"><w:numFmt w:val=\"bullet\"/></w:lvl>" +
			"  <w:lvl w:ilvl=\"1\"><w:numFmt w:val=\"decimal\"/></w:lvl>" +
			"</w:abstractNum>" +
			"<w:num w:numId=\"1\">" +
			"  <w:abstractNumId w:val=\"0\"/>" +
			"</w:num>"
		);
		Map<Integer, DocxNumberingDef> result = DocxNumberingParser.parse(numberingXml);

		assertTrue("numId 1 must be present", result.containsKey(1));
		DocxNumberingDef def = result.get(1);
		assertFalse("ilvl 0 is bullet → isOrdered must be false", def.isOrdered(0));
		assertTrue("ilvl 1 is decimal → isOrdered must be true", def.isOrdered(1));
	}

	@Test
	public void parseMultipleNums() {
		byte[] numberingXml = xml(
			"<w:abstractNum w:abstractNumId=\"0\">" +
			"  <w:lvl w:ilvl=\"0\"><w:numFmt w:val=\"bullet\"/></w:lvl>" +
			"</w:abstractNum>" +
			"<w:abstractNum w:abstractNumId=\"1\">" +
			"  <w:lvl w:ilvl=\"0\"><w:numFmt w:val=\"decimal\"/></w:lvl>" +
			"</w:abstractNum>" +
			"<w:num w:numId=\"1\">" +
			"  <w:abstractNumId w:val=\"0\"/>" +
			"</w:num>" +
			"<w:num w:numId=\"2\">" +
			"  <w:abstractNumId w:val=\"1\"/>" +
			"</w:num>"
		);
		Map<Integer, DocxNumberingDef> result = DocxNumberingParser.parse(numberingXml);

		assertEquals("Expected 2 num entries", 2, result.size());
		assertFalse("numId 1 → abstractNum 0 → bullet → not ordered", result.get(1).isOrdered(0));
		assertTrue("numId 2 → abstractNum 1 → decimal → ordered", result.get(2).isOrdered(0));
	}
}
