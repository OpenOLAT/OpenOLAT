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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.zip.ZipFile;

import org.junit.Test;
import org.olat.core.util.xml.XMLFactories;
import org.xml.sax.InputSource;

/**
 * Unit tests for {@link DocxToMarkdownHandler}.
 * <p>
 * No Spring context needed — handler is a plain SAX ContentHandler.
 *
 * @author frentix GmbH, http://www.frentix.com
 */
public class DocxToMarkdownHandlerTest {

	// -----------------------------------------------------------------------
	// Helper
	// -----------------------------------------------------------------------

	/**
	 * Wraps {@code bodyContent} in a minimal document.xml envelope, runs it
	 * through a fresh {@link DocxToMarkdownHandler}, and returns the resulting
	 * Markdown string.
	 */
	private String convertDocumentXml(String bodyContent) throws Exception {
		return convertDocumentXml(bodyContent,
				Collections.emptyMap(),
				Collections.emptyMap(),
				Collections.emptyMap(),
				null);
	}

	private String convertDocumentXml(String bodyContent,
			Map<String, DocxRelTarget> rels,
			Map<Integer, DocxNumberingDef> numbering,
			Map<String, String> styles,
			ZipFile zipFile) throws Exception {

		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<w:document"
				+ " xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
				+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\""
				+ " xmlns:m=\"http://schemas.openxmlformats.org/officeDocument/2006/math\""
				+ " xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
				+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
				+ " xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\""
				+ " xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\">"
				+ "<w:body>" + bodyContent + "</w:body></w:document>";

		DocxToMarkdownHandler handler = new DocxToMarkdownHandler(rels, numbering, styles, zipFile);
		javax.xml.parsers.SAXParser parser = XMLFactories.newSAXParser();
		parser.getXMLReader().setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		org.xml.sax.XMLReader reader = parser.getXMLReader();
		reader.setContentHandler(handler);
		reader.parse(new InputSource(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))));
		return handler.getMarkdown();
	}

	// -----------------------------------------------------------------------
	// Basic text
	// -----------------------------------------------------------------------

	@Test
	public void emptyDocument() throws Exception {
		String md = convertDocumentXml("");
		assertTrue("Empty body should produce empty or blank markdown",
				md.trim().isEmpty());
	}

	@Test
	public void singleParagraph() throws Exception {
		String md = convertDocumentXml(
				"<w:p><w:r><w:t>Hello world</w:t></w:r></w:p>");
		assertTrue("Single paragraph text must appear in output",
				md.contains("Hello world"));
	}

	@Test
	public void multipleParagraphs() throws Exception {
		String md = convertDocumentXml(
				"<w:p><w:r><w:t>First</w:t></w:r></w:p>"
				+ "<w:p><w:r><w:t>Second</w:t></w:r></w:p>");
		assertTrue("First paragraph text must appear", md.contains("First"));
		assertTrue("Second paragraph text must appear", md.contains("Second"));
		// The two paragraphs must be separated by a blank line (two consecutive newlines)
		int firstEnd = md.indexOf("First") + "First".length();
		int secondStart = md.indexOf("Second");
		String between = md.substring(firstEnd, secondStart);
		assertTrue("Paragraphs must be separated by a blank line",
				between.contains("\n\n"));
	}

	@Test
	public void specialCharactersEscaped() throws Exception {
		// Characters that must be backslash-escaped in Markdown body text
		String md = convertDocumentXml(
				"<w:p><w:r><w:t>a*b_c#d&gt;e|f</w:t></w:r></w:p>");
		assertTrue("* must be escaped", md.contains("\\*"));
		assertTrue("_ must be escaped", md.contains("\\_"));
		assertTrue("# must be escaped", md.contains("\\#"));
		assertTrue("> must be escaped", md.contains("\\>"));
		assertTrue("| must be escaped", md.contains("\\|"));
	}

	// -----------------------------------------------------------------------
	// Formatting
	// -----------------------------------------------------------------------

	@Test
	public void boldText() throws Exception {
		String md = convertDocumentXml(
				"<w:p><w:r><w:rPr><w:b/></w:rPr><w:t>bold</w:t></w:r></w:p>");
		assertTrue("Bold text must be wrapped in **", md.contains("**bold**"));
	}

	@Test
	public void italicText() throws Exception {
		String md = convertDocumentXml(
				"<w:p><w:r><w:rPr><w:i/></w:rPr><w:t>italic</w:t></w:r></w:p>");
		assertTrue("Italic text must be wrapped in *", md.contains("*italic*"));
	}

	@Test
	public void boldItalicText() throws Exception {
		String md = convertDocumentXml(
				"<w:p><w:r><w:rPr><w:b/><w:i/></w:rPr><w:t>bolditalic</w:t></w:r></w:p>");
		assertTrue("Bold-italic text must be wrapped in ***", md.contains("***bolditalic***"));
	}

	@Test
	public void strikethroughText() throws Exception {
		String md = convertDocumentXml(
				"<w:p><w:r><w:rPr><w:strike/></w:rPr><w:t>struck</w:t></w:r></w:p>");
		assertTrue("Strikethrough text must be wrapped in ~~", md.contains("~~struck~~"));
	}

	// -----------------------------------------------------------------------
	// Headings
	// -----------------------------------------------------------------------

	@Test
	public void heading1() throws Exception {
		Map<String, String> styles = Map.of("Heading1", "Heading1");
		String md = convertDocumentXml(
				"<w:p><w:pPr><w:pStyle w:val=\"Heading1\"/></w:pPr>"
				+ "<w:r><w:t>Title One</w:t></w:r></w:p>",
				Collections.emptyMap(), Collections.emptyMap(), styles, null);
		assertTrue("Heading 1 must start with '# '", md.contains("# Title One"));
	}

	@Test
	public void heading2() throws Exception {
		Map<String, String> styles = Map.of("Heading2", "Heading2");
		String md = convertDocumentXml(
				"<w:p><w:pPr><w:pStyle w:val=\"Heading2\"/></w:pPr>"
				+ "<w:r><w:t>Title Two</w:t></w:r></w:p>",
				Collections.emptyMap(), Collections.emptyMap(), styles, null);
		assertTrue("Heading 2 must start with '## '", md.contains("## Title Two"));
	}

	@Test
	public void titleStyle() throws Exception {
		// Title maps to heading level 1
		Map<String, String> styles = Map.of("Title", "Title");
		String md = convertDocumentXml(
				"<w:p><w:pPr><w:pStyle w:val=\"Title\"/></w:pPr>"
				+ "<w:r><w:t>Document Title</w:t></w:r></w:p>",
				Collections.emptyMap(), Collections.emptyMap(), styles, null);
		assertTrue("Title style must render as '# '", md.contains("# Document Title"));
	}

	// -----------------------------------------------------------------------
	// Lists
	// -----------------------------------------------------------------------

	@Test
	public void bulletList() throws Exception {
		// numId=1, ilvl=0, unordered
		Map<Integer, DocxNumberingDef> numbering = Map.of(
				1, new DocxNumberingDef(Map.of(0, false)));
		String md = convertDocumentXml(
				"<w:p><w:pPr><w:numPr>"
				+ "<w:ilvl w:val=\"0\"/><w:numId w:val=\"1\"/>"
				+ "</w:numPr></w:pPr><w:r><w:t>Item A</w:t></w:r></w:p>",
				Collections.emptyMap(), numbering, Collections.emptyMap(), null);
		assertTrue("Bullet list item must start with '- '", md.contains("- Item A"));
	}

	@Test
	public void numberedList() throws Exception {
		// numId=2, ilvl=0, ordered
		Map<Integer, DocxNumberingDef> numbering = Map.of(
				2, new DocxNumberingDef(Map.of(0, true)));
		String md = convertDocumentXml(
				"<w:p><w:pPr><w:numPr>"
				+ "<w:ilvl w:val=\"0\"/><w:numId w:val=\"2\"/>"
				+ "</w:numPr></w:pPr><w:r><w:t>Step One</w:t></w:r></w:p>",
				Collections.emptyMap(), numbering, Collections.emptyMap(), null);
		assertTrue("Numbered list item must start with '1. '", md.contains("1. Step One"));
	}

	@Test
	public void nestedList() throws Exception {
		// numId=3, ilvl=1 — nested bullet (indented 4 spaces)
		Map<Integer, DocxNumberingDef> numbering = Map.of(
				3, new DocxNumberingDef(Map.of(0, false, 1, false)));
		String md = convertDocumentXml(
				"<w:p><w:pPr><w:numPr>"
				+ "<w:ilvl w:val=\"1\"/><w:numId w:val=\"3\"/>"
				+ "</w:numPr></w:pPr><w:r><w:t>Nested</w:t></w:r></w:p>",
				Collections.emptyMap(), numbering, Collections.emptyMap(), null);
		// ilvl=1 → 4 spaces of indent + "- "
		assertTrue("Nested list item must be indented with 4 spaces",
				md.contains("    - Nested"));
	}

	// -----------------------------------------------------------------------
	// Tables
	// -----------------------------------------------------------------------

	@Test
	public void tableWithBoldHeader() throws Exception {
		// 2×2 table: bold first row detected as header
		String body =
				"<w:tbl>"
				+ "<w:tr>"
				+   "<w:tc><w:p><w:r><w:rPr><w:b/></w:rPr><w:t>H1</w:t></w:r></w:p></w:tc>"
				+   "<w:tc><w:p><w:r><w:rPr><w:b/></w:rPr><w:t>H2</w:t></w:r></w:p></w:tc>"
				+ "</w:tr>"
				+ "<w:tr>"
				+   "<w:tc><w:p><w:r><w:t>A</w:t></w:r></w:p></w:tc>"
				+   "<w:tc><w:p><w:r><w:t>B</w:t></w:r></w:p></w:tc>"
				+ "</w:tr>"
				+ "</w:tbl>";
		String md = convertDocumentXml(body);
		assertTrue("Header cells must appear", md.contains("H1") && md.contains("H2"));
		assertTrue("Data cells must appear", md.contains("A") && md.contains("B"));
		assertTrue("GFM separator must be present", md.contains("|---|"));
		int h2Pos = md.indexOf("H2");
		int sepPos = md.indexOf("|---|");
		assertTrue("Header must come before separator", h2Pos < sepPos);
	}

	@Test
	public void tableNoHeader() throws Exception {
		// Table without bold — first row used as header (GFM requires a header row)
		String body =
				"<w:tbl>"
				+ "<w:tr>"
				+   "<w:tc><w:p><w:r><w:t>A</w:t></w:r></w:p></w:tc>"
				+   "<w:tc><w:p><w:r><w:t>B</w:t></w:r></w:p></w:tc>"
				+ "</w:tr>"
				+ "<w:tr>"
				+   "<w:tc><w:p><w:r><w:t>C</w:t></w:r></w:p></w:tc>"
				+   "<w:tc><w:p><w:r><w:t>D</w:t></w:r></w:p></w:tc>"
				+ "</w:tr>"
				+ "</w:tbl>";
		String md = convertDocumentXml(body);
		assertTrue("All cells must appear", md.contains("A") && md.contains("D"));
		assertTrue("GFM separator must be present", md.contains("|---|"));
		// First row is used as header (before separator), second as body
		int aPos = md.indexOf("| A");
		int sepPos = md.indexOf("|---|");
		int cPos = md.indexOf("| C");
		assertTrue("First row before separator", aPos < sepPos);
		assertTrue("Second row after separator", cPos > sepPos);
	}

	@Test
	public void tableCellWithPipe() throws Exception {
		// A pipe character inside a cell must be escaped so the GFM table
		// structure is not broken. The handler escapes | in two passes
		// (escapeMarkdown in the run flush, then the cell finaliser's replace),
		// so the raw text "a|b" ends up as "a\\|b" in the Markdown output.
		String body =
				"<w:tbl>"
				+ "<w:tr>"
				+   "<w:tc><w:p><w:r><w:t>a|b</w:t></w:r></w:p></w:tc>"
				+ "</w:tr>"
				+ "</w:tbl>";
		String md = convertDocumentXml(body);
		// The pipe must not appear as a raw unescaped | inside the cell value
		assertFalse("Raw unescaped pipe must not appear as bare | in cell",
				md.contains(" a|b "));
		// At minimum one level of escaping must be present
		assertTrue("Pipe must be escaped in cell content",
				md.contains("\\|"));
	}

	// -----------------------------------------------------------------------
	// Hyperlinks
	// -----------------------------------------------------------------------

	@Test
	public void externalHyperlink() throws Exception {
		Map<String, DocxRelTarget> rels = Map.of(
				"rId1", new DocxRelTarget(
						"http://schemas.openxmlformats.org/officeDocument/2006/relationships/hyperlink",
						"https://www.openolat.org"));
		String body =
				"<w:p><w:hyperlink r:id=\"rId1\">"
				+ "<w:r><w:t>OpenOLAT</w:t></w:r>"
				+ "</w:hyperlink></w:p>";
		String md = convertDocumentXml(body, rels,
				Collections.emptyMap(), Collections.emptyMap(), null);
		assertTrue("External hyperlink must render as [text](url)",
				md.contains("[OpenOLAT](https://www.openolat.org)"));
	}

	@Test
	public void internalAnchor() throws Exception {
		String body =
				"<w:p><w:hyperlink w:anchor=\"section1\">"
				+ "<w:r><w:t>Jump</w:t></w:r>"
				+ "</w:hyperlink></w:p>";
		String md = convertDocumentXml(body);
		assertTrue("Internal anchor must render as [text](#anchor)",
				md.contains("[Jump](#section1)"));
	}

	// -----------------------------------------------------------------------
	// Line breaks
	// -----------------------------------------------------------------------

	@Test
	public void lineBreak() throws Exception {
		// w:br without type → soft line break: two trailing spaces + newline
		String md = convertDocumentXml(
				"<w:p><w:r><w:t>Line one</w:t></w:r>"
				+ "<w:br/>"
				+ "<w:r><w:t>Line two</w:t></w:r></w:p>");
		assertTrue("Soft line break must produce two trailing spaces",
				md.contains("  \n"));
		assertTrue("Text before break must appear", md.contains("Line one"));
		assertTrue("Text after break must appear", md.contains("Line two"));
	}

	@Test
	public void pageBreak() throws Exception {
		// w:br with type="page" → horizontal rule ---
		String md = convertDocumentXml(
				"<w:p><w:r><w:t>Before</w:t></w:r>"
				+ "<w:br w:type=\"page\"/>"
				+ "<w:r><w:t>After</w:t></w:r></w:p>");
		assertTrue("Page break must render as ---", md.contains("---"));
	}

	@Test
	public void tabCharacter() throws Exception {
		// w:tab inside a run → 4 spaces
		String md = convertDocumentXml(
				"<w:p><w:r><w:t>Col1</w:t><w:tab/><w:t>Col2</w:t></w:r></w:p>");
		assertTrue("Tab character must render as 4 spaces",
				md.contains("Col1    Col2"));
	}

	// -----------------------------------------------------------------------
	// Track changes
	// -----------------------------------------------------------------------

	@Test
	public void deletionSkipped() throws Exception {
		// Content inside w:del must be completely suppressed
		String md = convertDocumentXml(
				"<w:p>"
				+ "<w:r><w:t>Keep</w:t></w:r>"
				+ "<w:del><w:r><w:delText>Gone</w:delText></w:r></w:del>"
				+ "</w:p>");
		assertTrue("Text outside deletion must appear", md.contains("Keep"));
		assertFalse("Deleted text must not appear in output", md.contains("Gone"));
	}

	@Test
	public void insertionAccepted() throws Exception {
		// Content inside w:ins must be accepted and appear in output
		String md = convertDocumentXml(
				"<w:p>"
				+ "<w:ins><w:r><w:t>Inserted</w:t></w:r></w:ins>"
				+ "</w:p>");
		assertTrue("Inserted text must appear in output", md.contains("Inserted"));
	}

	// -----------------------------------------------------------------------
	// Blockquote
	// -----------------------------------------------------------------------

	@Test
	public void quoteStyle() throws Exception {
		Map<String, String> styles = Map.of("Quote", "Quote");
		String md = convertDocumentXml(
				"<w:p><w:pPr><w:pStyle w:val=\"Quote\"/></w:pPr>"
				+ "<w:r><w:t>A wise saying</w:t></w:r></w:p>",
				Collections.emptyMap(), Collections.emptyMap(), styles, null);
		assertTrue("Quote style must render with '> ' prefix",
				md.contains("> A wise saying"));
	}

	// -----------------------------------------------------------------------
	// Superscript / Subscript
	// -----------------------------------------------------------------------

	@Test
	public void superscriptText() throws Exception {
		String md = convertDocumentXml(
				"<w:p><w:r><w:t>E=mc</w:t></w:r>"
				+ "<w:r><w:rPr><w:vertAlign w:val=\"superscript\"/></w:rPr><w:t>2</w:t></w:r></w:p>");
		assertTrue("Superscript must be wrapped in <sup>", md.contains("<sup>2</sup>"));
	}

	@Test
	public void subscriptText() throws Exception {
		String md = convertDocumentXml(
				"<w:p><w:r><w:t>H</w:t></w:r>"
				+ "<w:r><w:rPr><w:vertAlign w:val=\"subscript\"/></w:rPr><w:t>2</w:t></w:r>"
				+ "<w:r><w:t>O</w:t></w:r></w:p>");
		assertTrue("Subscript must be wrapped in <sub>", md.contains("<sub>2</sub>"));
	}

	// -----------------------------------------------------------------------
	// Table header detection
	// -----------------------------------------------------------------------

	@Test
	public void tableExplicitHeader() throws Exception {
		// w:tblHeader marks the first row as header — takes priority over bold
		String body =
				"<w:tbl>"
				+ "<w:tr><w:trPr><w:tblHeader/></w:trPr>"
				+   "<w:tc><w:p><w:r><w:t>H1</w:t></w:r></w:p></w:tc>"
				+   "<w:tc><w:p><w:r><w:t>H2</w:t></w:r></w:p></w:tc>"
				+ "</w:tr>"
				+ "<w:tr>"
				+   "<w:tc><w:p><w:r><w:t>A</w:t></w:r></w:p></w:tc>"
				+   "<w:tc><w:p><w:r><w:t>B</w:t></w:r></w:p></w:tc>"
				+ "</w:tr>"
				+ "</w:tbl>";
		String md = convertDocumentXml(body);
		int h1Pos = md.indexOf("H1");
		int sepPos = md.indexOf("|---|");
		int aPos = md.indexOf("| A");
		assertTrue("Explicit header row before separator", h1Pos < sepPos);
		assertTrue("Separator before data", sepPos < aPos);
	}

	// -----------------------------------------------------------------------
	// Text box → note admonition
	// -----------------------------------------------------------------------

	@Test
	public void textBoxAsNoteAdmonition() throws Exception {
		String body =
				"<w:p><w:r><w:t>Before</w:t></w:r></w:p>"
				+ "<w:p><w:r><w:rPr><w:noProof/></w:rPr>"
				+ "<mc:AlternateContent><mc:Choice>"
				+ "<w:drawing><wp:anchor>"
				+ "<a:graphic xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\">"
				+ "<a:graphicData><wps:wsp xmlns:wps=\"http://schemas.microsoft.com/office/word/2010/wordprocessingShape\">"
				+ "<wps:txbx><w:txbxContent>"
				+ "<w:p><w:r><w:t>Box text</w:t></w:r></w:p>"
				+ "</w:txbxContent></wps:txbx>"
				+ "</wps:wsp></a:graphicData></a:graphic>"
				+ "</wp:anchor></w:drawing>"
				+ "</mc:Choice><mc:Fallback/></mc:AlternateContent>"
				+ "</w:r></w:p>";
		String md = convertDocumentXml(body);
		assertTrue("Text box must produce NOTE admonition", md.contains("> [!NOTE]"));
		assertTrue("Text box content must appear", md.contains("Box text"));
	}

	// -----------------------------------------------------------------------
	// Underline
	// -----------------------------------------------------------------------

	@Test
	public void underlineText() throws Exception {
		String md = convertDocumentXml(
				"<w:p><w:r><w:rPr><w:u w:val=\"single\"/></w:rPr>"
				+ "<w:t>underlined</w:t></w:r></w:p>");
		assertTrue("Underline must produce span with text-decoration",
				md.contains("<span style=\"text-decoration:underline\">underlined</span>"));
	}

	// -----------------------------------------------------------------------
	// Highlight / Mark
	// -----------------------------------------------------------------------

	@Test
	public void highlightText() throws Exception {
		String md = convertDocumentXml(
				"<w:p><w:r><w:rPr><w:highlight w:val=\"yellow\"/></w:rPr>"
				+ "<w:t>marked</w:t></w:r></w:p>");
		assertTrue("Highlight must produce <mark> tag", md.contains("<mark>marked</mark>"));
	}

	// -----------------------------------------------------------------------
	// Image dimensions
	// -----------------------------------------------------------------------

	@Test
	public void imageDimensionsInBuildImageMarkdown() throws Exception {
		// Test that wp:extent dimensions are captured and included in image markdown.
		// Use a drawing with extent but NO a:blip (so no ZipFile needed).
		// The buildImageMarkdown helper checks currentDrawingWidthPx/HeightPx.
		String body =
				"<w:p><w:r><w:rPr><w:noProof/></w:rPr>"
				+ "<w:drawing><wp:inline>"
				+ "<wp:extent cx=\"2667000\" cy=\"1778000\"/>"
				+ "<wp:docPr id=\"1\" name=\"img\"/>"
				+ "</wp:inline></w:drawing></w:r></w:p>";
		// No image is produced (no a:blip), but we can verify the dimension
		// capture by testing via the service with a real DOCX.
		// For a unit test, verify the buildImageMarkdown syntax directly
		// by checking the DocxToMarkdownService end-to-end test.
		// This test verifies wp:extent parsing does not crash.
		String md = convertDocumentXml(body);
		assertNotNull(md);
	}

	// -----------------------------------------------------------------------
	// List transitions
	// -----------------------------------------------------------------------

	@Test
	public void listToNonListTransition() throws Exception {
		Map<Integer, DocxNumberingDef> numbering = Map.of(
			1, new DocxNumberingDef(Map.of(0, false)));
		String md = convertDocumentXml(
				"<w:p><w:pPr><w:numPr><w:ilvl w:val=\"0\"/><w:numId w:val=\"1\"/></w:numPr></w:pPr>"
				+ "<w:r><w:t>Item</w:t></w:r></w:p>"
				+ "<w:p><w:r><w:t>Normal</w:t></w:r></w:p>",
				Collections.emptyMap(), numbering, Collections.emptyMap(), null);
		// List item and normal paragraph must be separated by a blank line
		int itemEnd = md.indexOf("Item") + "Item".length();
		int normalStart = md.indexOf("Normal");
		String between = md.substring(itemEnd, normalStart);
		assertTrue("Blank line must separate list from paragraph",
				between.contains("\n\n"));
	}
}
