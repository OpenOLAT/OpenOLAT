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

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;

/**
 * Stage 1: extracts known XML parts from a DOCX ZIP archive into byte arrays.
 * Media files are not extracted — they are streamed on-demand later from the
 * open ZipFile.
 *
 * @author gnaegi, https://www.frentix.com
 */
class DocxZipExtractor {

	private static final Logger log = Tracing.createLoggerFor(DocxZipExtractor.class);

	private static final int MAX_ENTRIES = 64;
	private static final long MAX_ENTRY_SIZE = 50L * 1024L * 1024L;

	private static final String ENTRY_DOCUMENT   = "word/document.xml";
	private static final String ENTRY_RELS       = "word/_rels/document.xml.rels";
	private static final String ENTRY_NUMBERING  = "word/numbering.xml";
	private static final String ENTRY_STYLES     = "word/styles.xml";
	private static final String ENTRY_CORE       = "docProps/core.xml";
	private static final String ENTRY_APP        = "docProps/app.xml";
	private static final String ENTRY_FOOTNOTES  = "word/footnotes.xml";
	private static final String ENTRY_ENDNOTES   = "word/endnotes.xml";
	private static final String ENTRY_THEME      = "word/theme/theme1.xml";

	private DocxZipExtractor() {
		// utility
	}

	static DocxArchiveContent extract(ZipFile zipFile) throws IOException {
		// Security scan: check entry count, path traversal, macros
		int count = 0;
		for (Enumeration<? extends ZipEntry> entries = zipFile.entries();
				entries.hasMoreElements() && count < MAX_ENTRIES; count++) {
			ZipEntry entry = entries.nextElement();
			String name = entry.getName();

			if (name.contains("..")) {
				throw new IOException("Rejected ZIP entry with path traversal: " + name);
			}
			if (name.endsWith("vbaProject.bin")) {
				throw new IOException("DOCX contains VBA macro project — rejected.");
			}
		}

		// Validate that document.xml exists (required entry)
		if (zipFile.getEntry(ENTRY_DOCUMENT) == null) {
			throw new IOException("Required DOCX entry missing: " + ENTRY_DOCUMENT);
		}
		// document.xml is NOT buffered here — it will be streamed directly
		// to the SAX parser later to avoid holding large XML in memory.
		byte[] relsXml      = readOptional(zipFile, ENTRY_RELS);
		byte[] numberingXml = readOptional(zipFile, ENTRY_NUMBERING);
		byte[] stylesXml    = readOptional(zipFile, ENTRY_STYLES);
		byte[] corePropsXml  = readOptional(zipFile, ENTRY_CORE);
		byte[] appPropsXml   = readOptional(zipFile, ENTRY_APP);
		byte[] footnotesXml  = readOptional(zipFile, ENTRY_FOOTNOTES);
		byte[] endnotesXml   = readOptional(zipFile, ENTRY_ENDNOTES);
		byte[] themeXml      = readOptional(zipFile, ENTRY_THEME);

		return new DocxArchiveContent(relsXml, numberingXml, stylesXml,
			corePropsXml, appPropsXml, footnotesXml, endnotesXml, themeXml);
	}

	private static byte[] readRequired(ZipFile zipFile, String entryName) throws IOException {
		byte[] data = readOptional(zipFile, entryName);
		if (data == null) {
			throw new IOException("Required DOCX entry missing: " + entryName);
		}
		return data;
	}

	private static byte[] readOptional(ZipFile zipFile, String entryName) throws IOException {
		ZipEntry entry = zipFile.getEntry(entryName);
		if (entry == null) {
			return null;
		}
		long size = entry.getSize();
		if (size > MAX_ENTRY_SIZE) {
			log.warn("DOCX entry '{}' exceeds 50 MB limit ({} bytes); skipping.", entryName, size);
			return null;
		}
		try (InputStream in = zipFile.getInputStream(entry)) {
			byte[] data = in.readAllBytes();
			if (data.length > MAX_ENTRY_SIZE) {
				log.warn("DOCX entry '{}' expanded to {} bytes exceeding limit; discarding.", entryName, data.length);
				return null;
			}
			return data;
		}
	}
}
