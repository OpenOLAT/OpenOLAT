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
 * Unit tests for {@link DocxRelationshipParser}.
 *
 * @author frentix GmbH
 */
public class DocxRelationshipParserTest {

	private static final String RELS_NS =
		"xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"";

	private static final String TYPE_HYPERLINK =
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/hyperlink";
	private static final String TYPE_IMAGE =
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/image";

	private static byte[] xml(String body) {
		String full = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<Relationships " + RELS_NS + ">" + body + "</Relationships>";
		return full.getBytes(StandardCharsets.UTF_8);
	}

	// -----------------------------------------------------------------------

	@Test
	public void parseNull() {
		Map<String, DocxRelTarget> result = DocxRelationshipParser.parse(null);
		assertNotNull(result);
		assertTrue("Null input must produce an empty map", result.isEmpty());
	}

	@Test
	public void parseHyperlinkAndImage() {
		byte[] relsXml = xml(
			"<Relationship Id=\"rId1\" Type=\"" + TYPE_HYPERLINK + "\"" +
			" Target=\"https://example.com\" TargetMode=\"External\"/>" +
			"<Relationship Id=\"rId2\" Type=\"" + TYPE_IMAGE + "\"" +
			" Target=\"media/image1.png\"/>"
		);
		Map<String, DocxRelTarget> result = DocxRelationshipParser.parse(relsXml);

		assertTrue("rId1 (hyperlink) must be present", result.containsKey("rId1"));
		assertTrue("rId2 (image) must be present", result.containsKey("rId2"));

		DocxRelTarget hyperlink = result.get("rId1");
		assertEquals(TYPE_HYPERLINK, hyperlink.type());
		assertEquals("https://example.com", hyperlink.target());

		DocxRelTarget image = result.get("rId2");
		assertEquals(TYPE_IMAGE, image.type());
		assertEquals("media/image1.png", image.target());
	}

	@Test
	public void parseExternalHyperlink() {
		byte[] relsXml = xml(
			"<Relationship Id=\"rId1\" Type=\"" + TYPE_HYPERLINK + "\"" +
			" Target=\"http://openolat.org\" TargetMode=\"External\"/>"
		);
		Map<String, DocxRelTarget> result = DocxRelationshipParser.parse(relsXml);

		assertTrue("http:// external hyperlink must be accepted", result.containsKey("rId1"));
		assertEquals("http://openolat.org", result.get("rId1").target());
	}

	@Test
	public void rejectJavascriptUrl() {
		byte[] relsXml = xml(
			"<Relationship Id=\"rId1\" Type=\"" + TYPE_HYPERLINK + "\"" +
			" Target=\"javascript:alert(1)\" TargetMode=\"External\"/>"
		);
		Map<String, DocxRelTarget> result = DocxRelationshipParser.parse(relsXml);

		assertFalse("javascript: URL must be rejected", result.containsKey("rId1"));
	}

	@Test
	public void rejectDataUrl() {
		byte[] relsXml = xml(
			"<Relationship Id=\"rId1\" Type=\"" + TYPE_HYPERLINK + "\"" +
			" Target=\"data:text/html,<h1>XSS</h1>\" TargetMode=\"External\"/>"
		);
		Map<String, DocxRelTarget> result = DocxRelationshipParser.parse(relsXml);

		assertFalse("data: URL must be rejected", result.containsKey("rId1"));
	}

	@Test
	public void acceptMailtoUrl() {
		byte[] relsXml = xml(
			"<Relationship Id=\"rId1\" Type=\"" + TYPE_HYPERLINK + "\"" +
			" Target=\"mailto:info@frentix.com\" TargetMode=\"External\"/>"
		);
		Map<String, DocxRelTarget> result = DocxRelationshipParser.parse(relsXml);

		assertTrue("mailto: URL must be accepted", result.containsKey("rId1"));
		assertEquals("mailto:info@frentix.com", result.get("rId1").target());
	}

	@Test
	public void parseEmptyRels() {
		byte[] relsXml = xml("");
		Map<String, DocxRelTarget> result = DocxRelationshipParser.parse(relsXml);

		assertNotNull(result);
		assertTrue("Empty Relationships element must produce an empty map", result.isEmpty());
	}
}
