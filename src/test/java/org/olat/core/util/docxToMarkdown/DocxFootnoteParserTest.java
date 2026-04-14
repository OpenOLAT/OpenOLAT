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

import static org.junit.Assert.*;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.Test;

/**
 * @author gnaegi, https://www.frentix.com
 */
public class DocxFootnoteParserTest {

	@Test
	public void parseNull() {
		Map<String, String> result = DocxFootnoteParser.parse(null);
		assertTrue(result.isEmpty());
	}

	@Test
	public void parseFootnotes() {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<w:footnotes xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
			+ "<w:footnote w:type=\"separator\" w:id=\"-1\"><w:p><w:r><w:separator/></w:r></w:p></w:footnote>"
			+ "<w:footnote w:type=\"continuationSeparator\" w:id=\"0\"><w:p><w:r><w:continuationSeparator/></w:r></w:p></w:footnote>"
			+ "<w:footnote w:id=\"1\"><w:p><w:r><w:t>First footnote text</w:t></w:r></w:p></w:footnote>"
			+ "<w:footnote w:id=\"2\"><w:p><w:r><w:t>Second footnote</w:t></w:r></w:p></w:footnote>"
			+ "</w:footnotes>";

		Map<String, String> result = DocxFootnoteParser.parse(xml.getBytes(StandardCharsets.UTF_8));

		// IDs -1 and 0 are separator/continuation — must be excluded
		assertFalse("Separator footnote must not be included", result.containsKey("-1"));
		assertFalse("Continuation footnote must not be included", result.containsKey("0"));
		assertEquals("First footnote text", result.get("1"));
		assertEquals("Second footnote", result.get("2"));
		assertEquals(2, result.size());
	}

	@Test
	public void parseEndnotes() {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<w:endnotes xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
			+ "<w:endnote w:id=\"0\"/>"
			+ "<w:endnote w:id=\"1\"><w:p><w:r><w:t>Endnote text</w:t></w:r></w:p></w:endnote>"
			+ "</w:endnotes>";

		Map<String, String> result = DocxFootnoteParser.parse(xml.getBytes(StandardCharsets.UTF_8));
		assertFalse(result.containsKey("0"));
		assertEquals("Endnote text", result.get("1"));
	}

	@Test
	public void parseEmptyFootnote() {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<w:footnotes xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
			+ "<w:footnote w:id=\"1\"><w:p/></w:footnote>"
			+ "</w:footnotes>";

		Map<String, String> result = DocxFootnoteParser.parse(xml.getBytes(StandardCharsets.UTF_8));
		// Empty footnote should not be included
		assertFalse(result.containsKey("1"));
	}
}
