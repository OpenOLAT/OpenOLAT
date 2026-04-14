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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.Test;

/**
 * Unit tests for {@link DocxMetadataParser}.
 *
 * @author frentix GmbH
 */
public class DocxMetadataParserTest {

	// Namespace declarations used in core.xml
	private static final String CORE_NS =
		"xmlns:cp=\"http://schemas.openxmlformats.org/package/2006/metadata/core-properties\" " +
		"xmlns:dc=\"http://purl.org/dc/elements/1.1/\" " +
		"xmlns:dcterms=\"http://purl.org/dc/terms/\"";

	private static byte[] coreXml(String body) {
		String full = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<cp:coreProperties " + CORE_NS + ">" + body + "</cp:coreProperties>";
		return full.getBytes(StandardCharsets.UTF_8);
	}

	// -----------------------------------------------------------------------

	@Test
	public void parseBothNull() {
		DocxMetadata result = DocxMetadataParser.parse(null, null);
		assertNotNull(result);
		assertFalse("Both-null input must yield hasContent()=false", result.hasContent());
	}

	@Test
	public void parseFullMetadata() {
		byte[] coreXml = coreXml(
			"<dc:title>Annual Report</dc:title>" +
			"<dc:creator>Jane Smith</dc:creator>" +
			"<dc:subject>Finance</dc:subject>" +
			"<cp:keywords>budget, forecast, q3</cp:keywords>" +
			"<dc:description>Summary of fiscal year</dc:description>" +
			"<dcterms:created>2026-01-01T00:00:00Z</dcterms:created>" +
			"<dcterms:modified>2026-03-31T12:00:00Z</dcterms:modified>" +
			"<cp:lastModifiedBy>John Doe</cp:lastModifiedBy>" +
			"<cp:category>Reports</cp:category>" +
			"<cp:revision>5</cp:revision>"
		);
		DocxMetadata result = DocxMetadataParser.parse(coreXml, null);

		assertTrue("Full metadata must have content", result.hasContent());
		assertEquals("Annual Report", result.title());
		assertEquals("Jane Smith", result.author());
		assertEquals("Finance", result.subject());
		assertEquals("Summary of fiscal year", result.description());
		assertEquals("2026-01-01T00:00:00Z", result.created());
		assertEquals("2026-03-31T12:00:00Z", result.modified());
		assertEquals("John Doe", result.lastModifiedBy());
		assertEquals("Reports", result.category());
		assertEquals("5", result.revision());
	}

	@Test
	public void parsePartialMetadata() {
		byte[] coreXml = coreXml(
			"<dc:title>My Document</dc:title>" +
			"<dc:creator>Alice</dc:creator>"
		);
		DocxMetadata result = DocxMetadataParser.parse(coreXml, null);

		assertTrue("Partial metadata must have content", result.hasContent());
		assertEquals("My Document", result.title());
		assertEquals("Alice", result.author());
		assertNull("subject must be null when absent", result.subject());
		assertNull("description must be null when absent", result.description());
		assertNull("created must be null when absent", result.created());
		assertNull("modified must be null when absent", result.modified());
		assertNull("lastModifiedBy must be null when absent", result.lastModifiedBy());
		assertNull("category must be null when absent", result.category());
	}

	@Test
	public void parseKeywords() {
		byte[] coreXml = coreXml(
			"<cp:keywords>budget, forecast, q3</cp:keywords>"
		);
		DocxMetadata result = DocxMetadataParser.parse(coreXml, null);

		List<String> keywords = result.keywords();
		assertNotNull(keywords);
		assertEquals("Expected 3 keywords", 3, keywords.size());
		assertEquals("budget", keywords.get(0));
		assertEquals("forecast", keywords.get(1));
		assertEquals("q3", keywords.get(2));
	}

	@Test
	public void parseCreatedDate() {
		String isoDateTime = "2026-03-15T10:30:00Z";
		byte[] coreXml = coreXml(
			"<dcterms:created>" + isoDateTime + "</dcterms:created>"
		);
		DocxMetadata result = DocxMetadataParser.parse(coreXml, null);

		// The raw ISO string is stored as-is; toYamlFrontMatter extracts the date part.
		assertEquals("created must be stored as the full ISO string",
			isoDateTime, result.created());

		// Verify that toYamlFrontMatter extracts only the date portion.
		String yaml = result.toYamlFrontMatter();
		assertTrue("YAML front matter must contain the date-only value",
			yaml.contains("date: \"2026-03-15\""));
	}
}
