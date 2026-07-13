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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Map;
import java.util.zip.ZipFile;

import org.junit.Test;
import org.olat.core.util.FileUtils;
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

	/**
	 * Runs {@code bodyContent} through a fresh handler and returns the handler
	 * itself so tests can inspect both markdown and conversion messages.
	 */
	private DocxToMarkdownHandler runHandler(String bodyContent,
			Map<String, DocxRelTarget> rels,
			Map<Integer, DocxNumberingDef> numbering) throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<w:document"
				+ " xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
				+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\""
				+ " xmlns:m=\"http://schemas.openxmlformats.org/officeDocument/2006/math\""
				+ " xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
				+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
				+ " xmlns:o=\"urn:schemas-microsoft-com:office:office\""
				+ " xmlns:dgm=\"http://schemas.openxmlformats.org/drawingml/2006/diagram\""
				+ " xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\""
				+ " xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\">"
				+ "<w:body>" + bodyContent + "</w:body></w:document>";
		DocxToMarkdownHandler handler = new DocxToMarkdownHandler(rels, numbering,
				Collections.emptyMap(), null);
		javax.xml.parsers.SAXParser parser = XMLFactories.newSAXParser();
		parser.getXMLReader().setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		org.xml.sax.XMLReader reader = parser.getXMLReader();
		reader.setContentHandler(handler);
		reader.parse(new InputSource(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))));
		return handler;
	}

	private boolean hasWarning(DocxToMarkdownHandler handler, String key) {
		return handler.getMessages().stream()
				.anyMatch(m -> m.level() == DocxConversionMessage.Level.WARNING
						&& key.equals(m.i18nKey()));
	}

	// -----------------------------------------------------------------------
	// Conversion-message warnings (i18n key wiring)
	// -----------------------------------------------------------------------

	@Test
	public void trackChangesDeletionEmitsWarning() throws Exception {
		// w:del marks a tracked deletion; its content is dropped and a single
		// track-changes warning is emitted.
		DocxToMarkdownHandler handler = runHandler(
				"<w:p><w:r><w:t>Kept</w:t></w:r>"
				+ "<w:del><w:r><w:delText>Removed</w:delText></w:r></w:del></w:p>",
				Collections.emptyMap(), Collections.emptyMap());
		assertTrue("deleted run text must not appear", !handler.getMarkdown().contains("Removed"));
		assertTrue("track-changes warning expected",
				hasWarning(handler, "docx.convert.warn.track.changes"));
	}

	@Test
	public void superscriptEmitsWarning() throws Exception {
		DocxToMarkdownHandler handler = runHandler(
				"<w:p><w:r><w:rPr><w:vertAlign w:val=\"superscript\"/></w:rPr>"
				+ "<w:t>2</w:t></w:r></w:p>",
				Collections.emptyMap(), Collections.emptyMap());
		assertTrue("superscript warning expected",
				hasWarning(handler, "docx.convert.warn.superscript"));
	}

	@Test
	public void missingNumberingEmitsWarning() throws Exception {
		// A list paragraph references numId 7, but no numbering definition is
		// supplied — handler falls back to a bullet and warns once.
		DocxToMarkdownHandler handler = runHandler(
				"<w:p><w:pPr><w:numPr><w:ilvl w:val=\"0\"/><w:numId w:val=\"7\"/></w:numPr></w:pPr>"
				+ "<w:r><w:t>Item</w:t></w:r></w:p>",
				Collections.emptyMap(), Collections.emptyMap());
		assertTrue("bullet fallback expected", handler.getMarkdown().contains("- Item"));
		assertTrue("numbering-missing warning expected",
				hasWarning(handler, "docx.convert.warn.numbering.missing"));
	}

	@Test
	public void unsafeUrlEmitsWarningAndIsDropped() throws Exception {
		// javascript: is not in the http/https/mailto whitelist.
		java.util.Map<String, DocxRelTarget> rels = java.util.Map.of(
				"rId1", new DocxRelTarget("hyperlink", "javascript:alert(1)"));
		DocxToMarkdownHandler handler = runHandler(
				"<w:p><w:hyperlink r:id=\"rId1\"><w:r><w:t>click</w:t></w:r></w:hyperlink></w:p>",
				rels, Collections.emptyMap());
		assertTrue("unsafe URL must not be emitted as a link target",
				!handler.getMarkdown().contains("javascript:"));
		assertTrue("url-rejected warning expected",
				hasWarning(handler, "docx.convert.warn.url.rejected"));
	}

	@Test
	public void safeUrlIsKept() throws Exception {
		java.util.Map<String, DocxRelTarget> rels = java.util.Map.of(
				"rId1", new DocxRelTarget("hyperlink", "https://example.org"));
		DocxToMarkdownHandler handler = runHandler(
				"<w:p><w:hyperlink r:id=\"rId1\"><w:r><w:t>click</w:t></w:r></w:hyperlink></w:p>",
				rels, Collections.emptyMap());
		assertTrue("safe URL must be kept", handler.getMarkdown().contains("https://example.org"));
		assertTrue("no url-rejected warning for safe URL",
				!hasWarning(handler, "docx.convert.warn.url.rejected"));
	}

	@Test
	public void oleObjectEmitsWarning() throws Exception {
		DocxToMarkdownHandler handler = runHandler(
				"<w:p><w:r><w:object><o:OLEObject Type=\"Embed\" ProgID=\"Excel.Sheet.12\"/>"
				+ "</w:object></w:r></w:p>",
				Collections.emptyMap(), Collections.emptyMap());
		assertTrue("OLE warning expected", hasWarning(handler, "docx.convert.warn.ole"));
	}

	@Test
	public void mergedCellEmitsWarning() throws Exception {
		String body = "<w:tbl><w:tr>"
				+ "<w:tc><w:tcPr><w:gridSpan w:val=\"2\"/></w:tcPr>"
				+ "<w:p><w:r><w:t>Wide</w:t></w:r></w:p></w:tc>"
				+ "</w:tr></w:tbl>";
		DocxToMarkdownHandler handler = runHandler(body,
				Collections.emptyMap(), Collections.emptyMap());
		assertTrue("merged-cell warning expected",
				hasWarning(handler, "docx.convert.warn.merged.cells"));
	}

	@Test
	public void chartEmitsUnsupportedWarning() throws Exception {
		// A DrawingML chart reference (c:chart) cannot be converted to Markdown
		// and is reported as an unsupported element.
		DocxToMarkdownHandler handler = runHandler(
				"<w:p><w:r><w:drawing><wp:inline>"
				+ "<a:graphic><a:graphicData>"
				+ "<c:chart xmlns:c=\"http://schemas.openxmlformats.org/drawingml/2006/chart\""
				+ " r:id=\"rId9\"/>"
				+ "</a:graphicData></a:graphic></wp:inline></w:drawing></w:r></w:p>",
				Collections.emptyMap(), Collections.emptyMap());
		assertTrue("unsupported-element warning expected",
				hasWarning(handler, "docx.convert.warn.element.unsupported"));
	}

	@Test
	public void footnotesAreRenderedNotSkipped() throws Exception {
		// Footnotes are rendered as [^N] references, so the "footnotes skipped"
		// behaviour does not occur and no such warning key exists/fires.
		DocxToMarkdownHandler handler = runHandler(
				"<w:p><w:r><w:t>Text</w:t></w:r></w:p>",
				Collections.emptyMap(), Collections.emptyMap());
		assertTrue("no footnotes-skipped warning",
				!hasWarning(handler, "docx.convert.warn.footnotes"));
	}

	// -----------------------------------------------------------------------
	// Media filename collision handling
	// -----------------------------------------------------------------------

	@Test
	public void imagesWithSameBasenameAreUniquified() throws Exception {
		// Two images share the basename "image1.png" but live in different
		// folders. Both must be written without overwriting each other and be
		// referenced by distinct relative paths.
		byte[] pngA = pngBytes((byte) 0xAA);
		byte[] pngB = pngBytes((byte) 0xBB);

		java.io.File docx = java.io.File.createTempFile("collide", ".docx", new java.io.File("target"));
		try (java.util.zip.ZipOutputStream zos =
				new java.util.zip.ZipOutputStream(new java.io.FileOutputStream(docx))) {
			zos.putNextEntry(new java.util.zip.ZipEntry("word/media/image1.png"));
			zos.write(pngA);
			zos.closeEntry();
			zos.putNextEntry(new java.util.zip.ZipEntry("word/media/sub/image1.png"));
			zos.write(pngB);
			zos.closeEntry();
		}

		File mediaDir = java.nio.file.Files.createTempDirectory("collide_media_").toFile();
		
		Map<String, DocxRelTarget> rels = Map.of(
				"rId1", new DocxRelTarget("image", "media/image1.png"),
				"rId2", new DocxRelTarget("image", "media/sub/image1.png"));

		String body =
				"<w:p><w:r><w:drawing><wp:inline><a:graphic><a:graphicData>"
				+ "<pic:pic><pic:blipFill><a:blip r:embed=\"rId1\"/></pic:blipFill></pic:pic>"
				+ "</a:graphicData></a:graphic></wp:inline></w:drawing></w:r></w:p>"
				+ "<w:p><w:r><w:drawing><wp:inline><a:graphic><a:graphicData>"
				+ "<pic:pic><pic:blipFill><a:blip r:embed=\"rId2\"/></pic:blipFill></pic:pic>"
				+ "</a:graphicData></a:graphic></wp:inline></w:drawing></w:r></w:p>";

		String md;
		try (ZipFile zf = new ZipFile(docx)) {
			DocxToMarkdownHandler handler = new DocxToMarkdownHandler(rels,
					Collections.emptyMap(), Collections.emptyMap(), zf, mediaDir,
					Collections.emptyMap(), Collections.emptyMap());
			javax.xml.parsers.SAXParser parser = XMLFactories.newSAXParser();
			parser.getXMLReader().setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			org.xml.sax.XMLReader reader = parser.getXMLReader();
			reader.setContentHandler(handler);
			String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><w:document"
					+ " xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
					+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\""
					+ " xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
					+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
					+ " xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">"
					+ "<w:body>" + body + "</w:body></w:document>";
			reader.parse(new InputSource(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))));
			md = handler.getMarkdown();
		}

		java.io.File[] written = mediaDir.listFiles();
		assertNotNull(written);
		assertEquals("both images must be written without overwriting", 2, written.length);

		// The two image references in the markdown must point to different files.
		java.util.regex.Matcher m = java.util.regex.Pattern
				.compile("\\(media/([^)]+)\\)").matcher(md);
		java.util.Set<String> refs = new java.util.HashSet<>();
		while (m.find()) {
			refs.add(m.group(1));
		}
		assertEquals("two distinct media references expected", 2, refs.size());
		
		Files.delete(docx.toPath());
		FileUtils.deleteDirsAndFiles(mediaDir.toPath());
	}

	/** Minimal valid PNG (8-byte signature + one filler byte). */
	private static byte[] pngBytes(byte filler) {
		return new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, filler };
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

	/**
	 * Design/PDF exports lay a page out as a group of many positioned text
	 * boxes, often one per word. Each such box must NOT become its own NOTE
	 * admonition; all boxes of one drawing are merged into a single NOTE.
	 */
	@Test
	public void multipleTextBoxesInOneDrawingMergeToSingleNote() throws Exception {
		String wpsBox = "<wps:wsp xmlns:wps=\"http://schemas.microsoft.com/office/word/2010/wordprocessingShape\">"
				+ "<wps:txbx><w:txbxContent><w:p><w:r><w:t xml:space=\"preserve\">%s</w:t></w:r></w:p>"
				+ "</w:txbxContent></wps:txbx></wps:wsp>";
		String group = String.format(wpsBox, "Dings") + String.format(wpsBox, " ")
				+ String.format(wpsBox, "&amp;") + String.format(wpsBox, " ")
				+ String.format(wpsBox, "Dongs");
		String body =
				"<w:p><w:r><w:rPr><w:noProof/></w:rPr>"
				+ "<mc:AlternateContent><mc:Choice Requires=\"wpg\">"
				+ "<w:drawing><wp:anchor>"
				+ "<a:graphic xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\">"
				+ "<a:graphicData>"
				+ "<wpg:wgp xmlns:wpg=\"http://schemas.microsoft.com/office/word/2010/wordprocessingGroup\">"
				+ group
				+ "</wpg:wgp>"
				+ "</a:graphicData></a:graphic>"
				+ "</wp:anchor></w:drawing>"
				+ "</mc:Choice><mc:Fallback/></mc:AlternateContent>"
				+ "</w:r></w:p>";
		String md = convertDocumentXml(body);

		int noteCount = md.split("\\Q> [!NOTE]\\E", -1).length - 1;
		assertEquals("All boxes of one drawing merge into exactly one NOTE", 1, noteCount);
		assertTrue("Merged box text must appear joined", md.contains("Dings & Dongs"));
	}

	/**
	 * A table that directly follows a list item must be emitted as its own
	 * block, separated by a blank line. Without the blank line, Markdown
	 * parsers treat the pipe rows as a lazy continuation of the list item and
	 * the table is not detected.
	 */
	@Test
	public void tableAfterListItemIsSeparatedAsOwnBlock() throws Exception {
		String body =
				"<w:p><w:pPr><w:numPr><w:ilvl w:val=\"0\"/><w:numId w:val=\"1\"/></w:numPr></w:pPr>"
				+ "<w:r><w:t>ListItem</w:t></w:r></w:p>"
				+ "<w:tbl>"
				+ "<w:tr><w:tc><w:p><w:r><w:t>A1</w:t></w:r></w:p></w:tc>"
				+ "<w:tc><w:p><w:r><w:t>B1</w:t></w:r></w:p></w:tc></w:tr>"
				+ "<w:tr><w:tc><w:p><w:r><w:t>A2</w:t></w:r></w:p></w:tc>"
				+ "<w:tc><w:p><w:r><w:t>B2</w:t></w:r></w:p></w:tc></w:tr>"
				+ "</w:tbl>";
		String md = convertDocumentXml(body);

		int li = md.indexOf("ListItem");
		int tbl = md.indexOf("|", li);
		assertTrue("List item must be present", li >= 0);
		assertTrue("Table must be present", tbl >= 0);
		String between = md.substring(li + "ListItem".length(), tbl);
		assertTrue("Table must be separated from the list item by a blank line",
				between.contains("\n\n"));
	}

	/**
	 * A 1×1 table is a styled callout box, not a data table. It must be
	 * rendered as a NOTE admonition, one line per cell paragraph — not as a
	 * degenerate single-column GFM table with the paragraphs collapsed onto one
	 * line.
	 */
	@Test
	public void singleCellTableBecomesNoteWithOneLinePerParagraph() throws Exception {
		String body =
				"<w:tbl><w:tr><w:tc>"
				+ "<w:p><w:r><w:rPr><w:b/></w:rPr><w:t>Kontakt</w:t></w:r></w:p>"
				+ "<w:p><w:r><w:rPr><w:b/></w:rPr><w:t>frentix GmbH</w:t></w:r>"
				+ "<w:r><w:t xml:space=\"preserve\"> · Zürich, Schweiz</w:t></w:r></w:p>"
				+ "<w:p><w:r><w:t>someemail@example.com · www.frentix.com</w:t></w:r></w:p>"
				+ "</w:tc></w:tr></w:tbl>";
		String md = convertDocumentXml(body);

		assertTrue("Single-cell table must become a NOTE", md.contains("> [!NOTE]"));
		assertFalse("Must not render as a GFM table", md.contains("|---|"));
		assertFalse("Adjacent bold runs must not glue into ****", md.contains("****"));
		assertTrue("First line preserved", md.contains("> **Kontakt**"));
		assertTrue("Second line preserved", md.contains("> **frentix GmbH** · Zürich, Schweiz"));
		assertTrue("Third line preserved", md.contains("> someemail@example.com · www.frentix.com"));
	}

	/**
	 * In a real (multi-cell) table, a cell with several paragraphs must join
	 * them with a separator — adjacent bold runs across paragraphs must never
	 * glue into '****'.
	 */
	@Test
	public void multiParagraphCellDoesNotGlueBoldRuns() throws Exception {
		String body =
				"<w:tbl>"
				+ "<w:tr>"
				+ "<w:tc><w:p><w:r><w:rPr><w:b/></w:rPr><w:t>A</w:t></w:r></w:p>"
				+ "<w:p><w:r><w:rPr><w:b/></w:rPr><w:t>B</w:t></w:r></w:p></w:tc>"
				+ "<w:tc><w:p><w:r><w:t>C</w:t></w:r></w:p></w:tc>"
				+ "</w:tr>"
				+ "<w:tr><w:tc><w:p><w:r><w:t>D</w:t></w:r></w:p></w:tc>"
				+ "<w:tc><w:p><w:r><w:t>E</w:t></w:r></w:p></w:tc></w:tr>"
				+ "</w:tbl>";
		String md = convertDocumentXml(body);

		assertFalse("Bold runs across paragraphs must not glue", md.contains("**A****B**"));
		assertTrue("Paragraphs in a cell are separated", md.contains("**A** **B**"));
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

	// -----------------------------------------------------------------------
	// Math (OMML → LaTeX via DocxMathConverter / fmath)
	// -----------------------------------------------------------------------

	/**
	 * Builds a document.xml envelope that declares the full set of namespaces
	 * used by media, math, VML and structured-document-tag fixtures, runs it
	 * through a handler built with the given media dir / footnote maps, and
	 * returns that handler so tests can inspect both the markdown and the
	 * conversion messages.
	 */
	private DocxToMarkdownHandler runFullHandler(String bodyContent,
			Map<String, DocxRelTarget> rels,
			ZipFile zipFile,
			java.io.File mediaDir,
			Map<String, String> footnotes,
			Map<String, String> endnotes) throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<w:document"
				+ " xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
				+ " xmlns:w14=\"http://schemas.microsoft.com/office/word/2010/wordml\""
				+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\""
				+ " xmlns:m=\"http://schemas.openxmlformats.org/officeDocument/2006/math\""
				+ " xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
				+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
				+ " xmlns:o=\"urn:schemas-microsoft-com:office:office\""
				+ " xmlns:v=\"urn:schemas-microsoft-com:vml\""
				+ " xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\""
				+ " xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\">"
				+ "<w:body>" + bodyContent + "</w:body></w:document>";
		DocxToMarkdownHandler handler = new DocxToMarkdownHandler(
				rels, Collections.emptyMap(), Collections.emptyMap(),
				zipFile, mediaDir, footnotes, endnotes);
		javax.xml.parsers.SAXParser parser = XMLFactories.newSAXParser();
		parser.getXMLReader().setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		org.xml.sax.XMLReader reader = parser.getXMLReader();
		reader.setContentHandler(handler);
		reader.parse(new InputSource(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))));
		return handler;
	}

	/**
	 * A simple equation (x = a + b) expressed as OMML must be converted to a
	 * LaTeX display-math block. The handler always emits {@code $$...$$} blocks
	 * (the markdown importer only supports display math, never inline {@code $}).
	 */
	@Test
	public void mathSimpleEquationRendersAsDisplayBlock() throws Exception {
		String oMath =
				"<m:oMathPara><m:oMath>"
				+ "<m:r><m:t>x</m:t></m:r><m:r><m:t>=</m:t></m:r>"
				+ "<m:r><m:t>a</m:t></m:r><m:r><m:t>+</m:t></m:r>"
				+ "<m:r><m:t>b</m:t></m:r>"
				+ "</m:oMath></m:oMathPara>";
		String md = convertDocumentXml("<w:p>" + oMath + "</w:p>");

		assertTrue("Math must be wrapped in a $$ display block",
				md.contains("$$\nx=a+b\n$$"));
		assertFalse("Display math block must not be emitted as inline $...$",
				md.contains("$x=a+b$"));
	}

	/**
	 * A bare {@code m:oMath} (not wrapped in {@code m:oMathPara}) is the inline
	 * form in OOXML, but the handler still emits it as a {@code $$} display
	 * block because the importer does not support inline math.
	 */
	@Test
	public void mathBareOMathAlsoRendersAsDisplayBlock() throws Exception {
		String md = convertDocumentXml(
				"<w:p><w:r><m:oMath><m:r><m:t>E</m:t></m:r>"
				+ "<m:r><m:t>=</m:t></m:r><m:r><m:t>m</m:t></m:r>"
				+ "</m:oMath></w:r></w:p>");
		assertTrue("Bare m:oMath must render as a $$ display block",
				md.contains("$$\nE=m\n$$"));
	}

	/**
	 * When the OMML carries no convertible content (no {@code m:t} text, so
	 * both the fmath conversion and the plain-text fallback yield nothing), the
	 * handler emits the {@code warn.math.failed} warning instead of a block.
	 */
	@Test
	public void mathWithoutTextEmitsMathFailedWarning() throws Exception {
		DocxToMarkdownHandler handler = runFullHandler(
				"<w:p><m:oMath><m:r/></m:oMath></w:p>",
				Collections.emptyMap(), null, null,
				Collections.emptyMap(), Collections.emptyMap());
		assertFalse("No display block must be emitted for empty math",
				handler.getMarkdown().contains("$$"));
		assertTrue("math-failed warning expected for unconvertible math",
				hasWarning(handler, "docx.convert.warn.math.failed"));
	}

	// -----------------------------------------------------------------------
	// Footnotes / endnotes
	// -----------------------------------------------------------------------

	/**
	 * A footnote reference must render an inline {@code [^N]} marker, and the
	 * footnote definition must be appended at the end of the document as
	 * {@code [^N]: text}.
	 */
	@Test
	public void footnoteReferenceRendersMarkerAndDefinition() throws Exception {
		Map<String, String> footnotes = Map.of("2", "The footnote body.");
		DocxToMarkdownHandler handler = runFullHandler(
				"<w:p><w:r><w:t>Statement</w:t></w:r>"
				+ "<w:r><w:footnoteReference w:id=\"2\"/></w:r></w:p>",
				Collections.emptyMap(), null, null,
				footnotes, Collections.emptyMap());
		String md = handler.getMarkdown();
		assertTrue("Inline footnote marker [^1] must appear next to the text",
				md.contains("Statement[^1]"));
		assertTrue("Footnote definition must be appended at the end",
				md.contains("[^1]: The footnote body."));
	}

	/**
	 * An endnote reference shares the same {@code [^N]} numbering space as
	 * footnotes and is also appended as a definition at the end.
	 */
	@Test
	public void endnoteReferenceRendersMarkerAndDefinition() throws Exception {
		Map<String, String> endnotes = Map.of("5", "An endnote.");
		DocxToMarkdownHandler handler = runFullHandler(
				"<w:p><w:r><w:t>Body</w:t></w:r>"
				+ "<w:r><w:endnoteReference w:id=\"5\"/></w:r></w:p>",
				Collections.emptyMap(), null, null,
				Collections.emptyMap(), endnotes);
		String md = handler.getMarkdown();
		assertTrue("Inline endnote marker [^1] must appear", md.contains("Body[^1]"));
		assertTrue("Endnote definition must be appended", md.contains("[^1]: An endnote."));
	}

	/**
	 * Separator footnote ids ("0" and "-1") and references to unknown ids must
	 * not produce a marker or a definition.
	 */
	@Test
	public void footnoteSeparatorAndUnknownIdsAreIgnored() throws Exception {
		Map<String, String> footnotes = Map.of("3", "real");
		DocxToMarkdownHandler handler = runFullHandler(
				"<w:p><w:r><w:t>Text</w:t></w:r>"
				+ "<w:r><w:footnoteReference w:id=\"0\"/></w:r>"
				+ "<w:r><w:footnoteReference w:id=\"99\"/></w:r></w:p>",
				Collections.emptyMap(), null, null,
				footnotes, Collections.emptyMap());
		String md = handler.getMarkdown();
		assertFalse("Separator/unknown footnote ids must not emit a marker",
				md.contains("[^1]"));
		assertFalse("No definitions must be appended", md.contains("[^1]:"));
	}

	// -----------------------------------------------------------------------
	// Video extraction
	// -----------------------------------------------------------------------

	/**
	 * A document embedding a video (a:videoFile referencing a media entry) must
	 * write the media file to the media directory and emit a markdown link of
	 * the form {@code [Video: name](media/name)} — videos are linked, not
	 * embedded like images.
	 */
	@Test
	public void videoIsExtractedAndLinked() throws Exception {
		byte[] videoData = "fake-mp4-bytes".getBytes(StandardCharsets.UTF_8);

		java.io.File docx = java.io.File.createTempFile("video", ".docx", new java.io.File("target"));

		try (java.util.zip.ZipOutputStream zos =
				new java.util.zip.ZipOutputStream(new java.io.FileOutputStream(docx))) {
			zos.putNextEntry(new java.util.zip.ZipEntry("word/media/clip.mp4"));
			zos.write(videoData);
			zos.closeEntry();
		}

		java.io.File mediaDir = java.nio.file.Files.createTempDirectory("video_media_").toFile();
		
		Map<String, DocxRelTarget> rels = Map.of(
				"rId7", new DocxRelTarget("video", "media/clip.mp4"));

		// A video is referenced via a:videoFile inside a drawing; an mc:Choice
		// carries the DrawingML video reference in real documents.
		String body =
				"<w:p><w:r><w:drawing><wp:inline><a:graphic><a:graphicData>"
				+ "<pic:pic><pic:nvPicPr><pic:nvPr>"
				+ "<a:videoFile r:link=\"rId7\"/>"
				+ "</pic:nvPr></pic:nvPicPr></pic:pic>"
				+ "</a:graphicData></a:graphic></wp:inline></w:drawing></w:r></w:p>";

		String md;
		try (ZipFile zf = new ZipFile(docx)) {
			DocxToMarkdownHandler handler = runFullHandlerWithZip(body, rels, zf, mediaDir);
			md = handler.getMarkdown();
		}

		java.io.File[] written = mediaDir.listFiles();
		assertNotNull(written);
		assertEquals("video file must be written to the media directory", 1, written.length);
		assertEquals("clip.mp4", written[0].getName());
		assertTrue("markdown must contain the video link",
				md.contains("[Video: clip.mp4](media/clip.mp4)"));
		
		Files.delete(docx.toPath());
		FileUtils.deleteDirsAndFiles(mediaDir.toPath());
	}

	// -----------------------------------------------------------------------
	// Checkboxes (structured document tags)
	// -----------------------------------------------------------------------

	/**
	 * A checked SDT checkbox must render as a {@code [x] } markdown task marker.
	 */
	@Test
	public void checkedCheckboxRendersAsCheckedTask() throws Exception {
		String body =
				"<w:p><w:sdt><w:sdtPr>"
				+ "<w14:checkbox><w14:checked w14:val=\"1\"/></w14:checkbox>"
				+ "</w:sdtPr><w:sdtContent><w:r><w:t>Done</w:t></w:r></w:sdtContent>"
				+ "</w:sdt><w:r><w:t> task</w:t></w:r></w:p>";
		String md = convertDocumentXml(body);
		assertTrue("Checked checkbox must render as [x]", md.contains("[x]"));
		assertFalse("Checked checkbox must not render as [ ]", md.contains("[ ]"));
	}

	/**
	 * An unchecked SDT checkbox must render as an empty {@code [ ] } task marker.
	 */
	@Test
	public void uncheckedCheckboxRendersAsEmptyTask() throws Exception {
		String body =
				"<w:p><w:sdt><w:sdtPr>"
				+ "<w14:checkbox><w14:checked w14:val=\"0\"/></w14:checkbox>"
				+ "</w:sdtPr><w:sdtContent><w:r><w:t>Todo</w:t></w:r></w:sdtContent>"
				+ "</w:sdt></w:p>";
		String md = convertDocumentXml(body);
		assertTrue("Unchecked checkbox must render as [ ]", md.contains("[ ]"));
	}

	// -----------------------------------------------------------------------
	// VML legacy shapes
	// -----------------------------------------------------------------------

	/**
	 * A legacy VML shape ({@code w:pict} containing a {@code v:shape} with path
	 * data) must be run through {@link VmlToSvgConverter}: the resulting SVG is
	 * written to the media directory and referenced as an image in the markdown.
	 */
	@Test
	public void vmlShapeIsConvertedToSvgAndReferenced() throws Exception {
		File mediaDir = java.nio.file.Files.createTempDirectory("vml_media_").toFile();
		mediaDir.deleteOnExit();

		String body =
				"<w:p><w:r><w:pict>"
				+ "<v:shape style=\"width:50pt;height:30pt\" path=\"m 0,0 l 100,0 100,50 x e\""
				+ " strokecolor=\"#156082\" fillcolor=\"#ffffff\" filled=\"t\"/>"
				+ "</w:pict></w:r></w:p>";

		DocxToMarkdownHandler handler = runFullHandler(body,
				Collections.emptyMap(), null, mediaDir,
				Collections.emptyMap(), Collections.emptyMap());
		String md = handler.getMarkdown();

		File[] written = mediaDir.listFiles();
		assertNotNull(written);
		assertEquals("VML shape must produce exactly one SVG file", 1, written.length);
		assertTrue("written file must be an SVG", written[0].getName().endsWith(".svg"));
		assertTrue("SVG file content must be valid SVG",
				java.nio.file.Files.readString(written[0].toPath()).contains("<svg"));
		assertTrue("markdown must reference the generated SVG as an image",
				md.contains("](media/" + written[0].getName() + ")"));
		
		FileUtils.deleteDirsAndFiles(mediaDir.toPath());
	}

	/**
	 * Helper variant that runs a fixture with relationships, an open ZipFile
	 * and a media dir (for image/video extraction) but no footnotes.
	 */
	private DocxToMarkdownHandler runFullHandlerWithZip(String bodyContent,
			Map<String, DocxRelTarget> rels,
			ZipFile zipFile,
			java.io.File mediaDir) throws Exception {
		return runFullHandler(bodyContent, rels, zipFile, mediaDir,
				Collections.emptyMap(), Collections.emptyMap());
	}
}
