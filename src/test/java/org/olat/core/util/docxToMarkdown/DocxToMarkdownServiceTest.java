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

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Test;
import org.olat.core.util.docxToMarkdown.DocxConversionMessage.Level;

/**
 * Unit tests for {@link DocxToMarkdownService}.
 * <p>
 * The service is stateless and has no Spring dependencies, so it is
 * instantiated directly. DOCX files are built programmatically as in-memory
 * ZIP archives.
 *
 * @author frentix GmbH
 */
public class DocxToMarkdownServiceTest {

	private final DocxToMarkdownService service = new DocxToMarkdownService();

	// -----------------------------------------------------------------------
	// DOCX builder helpers
	// -----------------------------------------------------------------------

	/**
	 * Builds a minimal DOCX from the given {@code word/document.xml} body
	 * fragment. Only the mandatory {@code word/document.xml} entry is written.
	 */
	private File createMinimalDocx(String bodyContent) throws Exception {
		String documentXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
			+ "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
			+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\""
			+ " xmlns:m=\"http://schemas.openxmlformats.org/officeDocument/2006/math\">"
			+ "<w:body>" + bodyContent + "</w:body></w:document>";

		return createDocxFile(documentXml, null, null, null, null, null);
	}

	private File createDocxFile(String documentXml, String relsXml, String numberingXml,
			String stylesXml, String coreXml, String appXml) throws Exception {
		File f = File.createTempFile("test", ".docx", new File("target"));
		f.deleteOnExit();
		try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(f))) {
			addEntry(zos, "word/document.xml", documentXml);
			if (relsXml != null)      addEntry(zos, "word/_rels/document.xml.rels", relsXml);
			if (numberingXml != null) addEntry(zos, "word/numbering.xml", numberingXml);
			if (stylesXml != null)    addEntry(zos, "word/styles.xml", stylesXml);
			if (coreXml != null)      addEntry(zos, "docProps/core.xml", coreXml);
			if (appXml != null)       addEntry(zos, "docProps/app.xml", appXml);
		}
		return f;
	}

	private void addEntry(ZipOutputStream zos, String name, String content) throws Exception {
		zos.putNextEntry(new ZipEntry(name));
		zos.write(content.getBytes(StandardCharsets.UTF_8));
		zos.closeEntry();
	}

	// -----------------------------------------------------------------------
	// Tests: successful conversion
	// -----------------------------------------------------------------------

	@Test
	public void convertSimpleParagraph() throws Exception {
		String body = "<w:p><w:r><w:t>Hello world</w:t></w:r></w:p>";
		File docx = createMinimalDocx(body);

		DocxToMarkdownResult result = service.convert(docx);

		assertNotNull("result must not be null", result);
		assertNotNull("markdown must not be null", result.markdown());
		assertTrue("markdown must contain the paragraph text",
				result.markdown().contains("Hello world"));
	}

	@Test
	public void convertWithMetadata() throws Exception {
		String body = "<w:p><w:r><w:t>Body text</w:t></w:r></w:p>";
		String coreXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
			+ "<cp:coreProperties"
			+ " xmlns:cp=\"http://schemas.openxmlformats.org/package/2006/metadata/core-properties\""
			+ " xmlns:dc=\"http://purl.org/dc/elements/1.1/\">"
			+ "<dc:title>Test Document</dc:title>"
			+ "<dc:creator>Jane Author</dc:creator>"
			+ "</cp:coreProperties>";

		File docx = createDocxFile(
			"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
				+ "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
				+ "<w:body>" + body + "</w:body></w:document>",
			null, null, null, coreXml, null);

		DocxToMarkdownResult result = service.convert(docx);

		assertNotNull(result);
		String md = result.markdown();
		assertTrue("markdown must start with YAML front matter delimiter",
				md.startsWith("---\n"));
		assertTrue("YAML must contain the title", md.contains("title:"));
		assertTrue("YAML must contain the extracted title value",
				md.contains("Test Document"));
	}

	@Test
	public void convertHeading() throws Exception {
		// Heading 1 via explicit styleId "Heading1" in styles.xml +
		// a w:pStyle reference in the paragraph properties.
		String stylesXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
			+ "<w:styles xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
			+ "<w:style w:type=\"paragraph\" w:styleId=\"Heading1\">"
			+ "<w:name w:val=\"heading 1\"/>"
			+ "</w:style>"
			+ "</w:styles>";

		String body = "<w:p>"
			+ "<w:pPr><w:pStyle w:val=\"Heading1\"/></w:pPr>"
			+ "<w:r><w:t>Chapter One</w:t></w:r>"
			+ "</w:p>";

		File docx = createDocxFile(
			"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
				+ "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
				+ "<w:body>" + body + "</w:body></w:document>",
			null, null, stylesXml, null, null);

		DocxToMarkdownResult result = service.convert(docx);

		assertNotNull(result);
		assertTrue("markdown must contain an H1 heading marker",
				result.markdown().contains("# "));
		assertTrue("markdown must contain the heading text",
				result.markdown().contains("Chapter One"));
	}

	@Test
	public void noMessagesForCleanDocument() throws Exception {
		String body = "<w:p><w:r><w:t>Clean content</w:t></w:r></w:p>";
		File docx = createMinimalDocx(body);

		DocxToMarkdownResult result = service.convert(docx);

		assertNotNull(result);
		boolean hasErrorOrWarning = result.messages() != null
			&& result.messages().stream()
				.anyMatch(m -> m.level() == Level.ERROR || m.level() == Level.WARNING);
		assertFalse("a clean document must not produce error or warning messages",
				hasErrorOrWarning);
	}

	// -----------------------------------------------------------------------
	// Tests: rejection / security
	// -----------------------------------------------------------------------

	@Test
	public void convertWithAnyExtension() throws Exception {
		// The service does not check extensions — that's the controller's job.
		// A valid DOCX ZIP with any file extension should convert successfully.
		File f = File.createTempFile("test", ".tmp", new File("target"));
		f.deleteOnExit();
		try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(f))) {
			addEntry(zos, "word/document.xml",
				"<?xml version=\"1.0\"?><w:document"
					+ " xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
					+ "<w:body><w:p><w:r><w:t>Works</w:t></w:r></w:p></w:body></w:document>");
		}

		DocxToMarkdownResult result = service.convert(f);

		assertNotNull(result);
		assertTrue("content must be converted regardless of file extension",
				result.markdown().contains("Works"));
	}

	@Test
	public void rejectNonZipFile() throws Exception {
		File f = File.createTempFile("test", ".docx", new File("target"));
		f.deleteOnExit();
		Files.writeString(f.toPath(), "This is not a ZIP file");

		DocxToMarkdownResult result = service.convert(f);

		assertNotNull(result);
		assertTrue("result must have an error message for non-ZIP .docx",
				result.hasMessages());
		assertTrue("error key must reference invalid.format",
				result.messages().stream()
					.anyMatch(m -> m.level() == Level.ERROR
						&& m.i18nKey().contains("invalid.format")));
		assertTrue("markdown must be empty for rejected file",
				result.markdown().isEmpty());
	}

	@Test
	public void rejectVbaProject() throws Exception {
		File f = File.createTempFile("test", ".docx", new File("target"));
		f.deleteOnExit();
		try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(f))) {
			addEntry(zos, "word/document.xml",
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
					+ "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
					+ "<w:body><w:p><w:r><w:t>text</w:t></w:r></w:p></w:body></w:document>");
			addEntry(zos, "word/vbaProject.bin", "fake binary macro data");
		}

		DocxToMarkdownResult result = service.convert(f);

		assertNotNull(result);
		assertTrue("result must have an error message when vbaProject.bin is present",
				result.hasMessages());
		assertTrue("error must be at ERROR level",
				result.messages().stream().anyMatch(m -> m.level() == Level.ERROR));
		assertTrue("markdown must be empty for rejected file",
				result.markdown().isEmpty());
	}

	// -----------------------------------------------------------------------
	// VFS convert method
	// -----------------------------------------------------------------------

	@Test
	public void convertVfsLeafNull() {
		DocxToMarkdownResult result = service.convert((org.olat.core.util.vfs.VFSLeaf) null);
		assertNotNull(result);
		assertTrue("null VFS leaf must produce error", result.hasMessages());
		assertTrue(result.markdown().isEmpty());
	}

	@Test
	public void convertVfsLeafLocalFile() throws Exception {
		// LocalFileImpl implements both VFSLeaf and JavaIOItem
		File docx = createMinimalDocx("<w:p><w:r><w:t>VFS test</w:t></w:r></w:p>");
		org.olat.core.util.vfs.LocalFileImpl vfsLeaf =
			new org.olat.core.util.vfs.LocalFileImpl(docx);

		DocxToMarkdownResult result = service.convert(vfsLeaf);

		assertNotNull(result);
		assertTrue("VFS conversion must produce markdown", result.markdown().contains("VFS test"));
	}
}
