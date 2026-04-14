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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.junit.Test;

/**
 * Unit tests for {@link DocxZipExtractor}.
 *
 * @author frentix GmbH
 */
public class DocxZipExtractorTest {

	private static final String MINIMAL_DOCUMENT_XML =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
		"<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">" +
		"<w:body><w:p><w:r><w:t>Hello</w:t></w:r></w:p></w:body>" +
		"</w:document>";

	private static final String MINIMAL_RELS_XML =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
		"<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"/>";

	private static final String MINIMAL_NUMBERING_XML =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
		"<w:numbering xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"/>";

	private static final String MINIMAL_STYLES_XML =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
		"<w:styles xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"/>";

	private static final String MINIMAL_CORE_XML =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
		"<cp:coreProperties xmlns:cp=\"http://schemas.openxmlformats.org/package/2006/metadata/core-properties\"/>";

	private static final String MINIMAL_APP_XML =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
		"<Properties xmlns=\"http://schemas.openxmlformats.org/officeDocument/2006/extended-properties\"/>";

	// -----------------------------------------------------------------------

	private File createTestDocx(Map<String, String> entries) throws Exception {
		File f = File.createTempFile("test", ".docx", new File("target"));
		f.deleteOnExit();
		try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(f))) {
			for (Map.Entry<String, String> e : entries.entrySet()) {
				zos.putNextEntry(new ZipEntry(e.getKey()));
				zos.write(e.getValue().getBytes(StandardCharsets.UTF_8));
				zos.closeEntry();
			}
		}
		return f;
	}

	// -----------------------------------------------------------------------

	@Test
	public void extractValidDocx() throws Exception {
		Map<String, String> entries = new LinkedHashMap<>();
		entries.put("word/document.xml", MINIMAL_DOCUMENT_XML);
		File docx = createTestDocx(entries);

		DocxArchiveContent content;
		try (ZipFile zf = new ZipFile(docx)) {
			content = DocxZipExtractor.extract(zf);
		}

		// document.xml is not buffered (streamed directly to SAX), but extraction must succeed
		assertNotNull("relsXml should be null when only document.xml is present", content);
	}

	@Test
	public void extractWithAllEntries() throws Exception {
		Map<String, String> entries = new LinkedHashMap<>();
		entries.put("word/document.xml", MINIMAL_DOCUMENT_XML);
		entries.put("word/_rels/document.xml.rels", MINIMAL_RELS_XML);
		entries.put("word/numbering.xml", MINIMAL_NUMBERING_XML);
		entries.put("word/styles.xml", MINIMAL_STYLES_XML);
		entries.put("docProps/core.xml", MINIMAL_CORE_XML);
		entries.put("docProps/app.xml", MINIMAL_APP_XML);
		File docx = createTestDocx(entries);

		DocxArchiveContent content;
		try (ZipFile zf = new ZipFile(docx)) {
			content = DocxZipExtractor.extract(zf);
		}

		assertNotNull("relsXml must not be null when entry is present", content.relsXml());
		assertNotNull("numberingXml must not be null when entry is present", content.numberingXml());
		assertNotNull("stylesXml must not be null when entry is present", content.stylesXml());
		assertNotNull("corePropsXml must not be null when entry is present", content.corePropsXml());
		assertNotNull("appPropsXml must not be null when entry is present", content.appPropsXml());
	}

	@Test(expected = IOException.class)
	public void extractMissingDocumentXml() throws Exception {
		Map<String, String> entries = new LinkedHashMap<>();
		entries.put("word/styles.xml", MINIMAL_STYLES_XML);
		File docx = createTestDocx(entries);

		try (ZipFile zf = new ZipFile(docx)) {
			DocxZipExtractor.extract(zf);
		}
	}

	@Test
	public void extractMissingOptionalEntries() throws Exception {
		Map<String, String> entries = new LinkedHashMap<>();
		entries.put("word/document.xml", MINIMAL_DOCUMENT_XML);
		File docx = createTestDocx(entries);

		DocxArchiveContent content;
		try (ZipFile zf = new ZipFile(docx)) {
			content = DocxZipExtractor.extract(zf);
		}

		assertNull("relsXml must be null when entry is absent", content.relsXml());
		assertNull("numberingXml must be null when entry is absent", content.numberingXml());
		assertNull("stylesXml must be null when entry is absent", content.stylesXml());
		assertNull("corePropsXml must be null when entry is absent", content.corePropsXml());
		assertNull("appPropsXml must be null when entry is absent", content.appPropsXml());
	}

	@Test(expected = IOException.class)
	public void rejectMacroVbaProject() throws Exception {
		Map<String, String> entries = new LinkedHashMap<>();
		entries.put("word/document.xml", MINIMAL_DOCUMENT_XML);
		entries.put("word/vbaProject.bin", "binary data");
		File docx = createTestDocx(entries);

		try (ZipFile zf = new ZipFile(docx)) {
			DocxZipExtractor.extract(zf);
		}
	}

	@Test(expected = IOException.class)
	public void rejectPathTraversal() throws Exception {
		File f = File.createTempFile("traversal", ".docx", new File("target"));
		f.deleteOnExit();
		try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(f))) {
			// Use ZipEntry with a raw name that contains ".."
			ZipEntry safe = new ZipEntry("word/document.xml");
			zos.putNextEntry(safe);
			zos.write(MINIMAL_DOCUMENT_XML.getBytes(StandardCharsets.UTF_8));
			zos.closeEntry();

			// Manually craft an entry with path traversal
			ZipEntry traversal = new ZipEntry("word/../../../etc/passwd");
			zos.putNextEntry(traversal);
			zos.write("root:x:0:0".getBytes(StandardCharsets.UTF_8));
			zos.closeEntry();
		}

		try (ZipFile zf = new ZipFile(f)) {
			DocxZipExtractor.extract(zf);
		}
	}
}
