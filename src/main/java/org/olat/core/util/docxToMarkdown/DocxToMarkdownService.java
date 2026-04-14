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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

import javax.xml.parsers.SAXParser;

import org.apache.logging.log4j.Logger;
import org.olat.core.util.vfs.JavaIOItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.logging.Tracing;
import org.olat.core.util.docxToMarkdown.DocxConversionMessage.Level;
import org.olat.core.util.xml.XMLFactories;
import org.springframework.stereotype.Service;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * Converts DOCX files to Markdown via a 6-stage SAX pipeline.
 * Thread-safe: all state is local to each convert() call.
 *
 * @author gnaegi, https://www.frentix.com
 */
@Service
public class DocxToMarkdownService {

	private static final Logger log = Tracing.createLoggerFor(DocxToMarkdownService.class);

	/**
	 * Convert a DOCX VFS item to markdown with optional YAML front matter.
	 * For VFS items backed by a local file, the file is accessed directly.
	 * For non-local VFS items (e.g., S3), the content is copied to a temp file first.
	 *
	 * @param vfsLeaf the .docx file as a VFS leaf
	 * @return result with markdown string and conversion messages
	 */
	public DocxToMarkdownResult convert(VFSLeaf vfsLeaf) {
		if (vfsLeaf == null) {
			return new DocxToMarkdownResult("", null,
				List.of(new DocxConversionMessage(DocxConversionMessage.Level.ERROR, "docx.convert.error.read.failed", new String[]{"null input"})));
		}
		// If the VFS item is backed by a local file, use it directly
		if (vfsLeaf instanceof JavaIOItem javaIOItem) {
			return convert(javaIOItem.getBasefile());
		}
		// Otherwise, copy to a temp file
		File tempFile = null;
		try {
			tempFile = File.createTempFile("docx_vfs_", ".docx");
			try (InputStream is = vfsLeaf.getInputStream();
					FileOutputStream fos = new FileOutputStream(tempFile)) {
				if (is == null) {
					return new DocxToMarkdownResult("", null,
						List.of(new DocxConversionMessage(DocxConversionMessage.Level.ERROR, "docx.convert.error.read.failed", new String[]{"cannot read VFS item"})));
				}
				is.transferTo(fos);
			}
			return convert(tempFile);
		} catch (IOException e) {
			log.error("Failed to copy VFS item to temp file", e);
			return new DocxToMarkdownResult("", null,
				List.of(new DocxConversionMessage(DocxConversionMessage.Level.ERROR, "docx.convert.error.read.failed", new String[]{e.getMessage()})));
		} finally {
			if (tempFile != null) {
				tempFile.delete();
			}
		}
	}

	/**
	 * Convert a DOCX file to markdown with optional YAML front matter.
	 * The caller is responsible for validating the file extension (.docx).
	 * This method validates the file content (ZIP structure, no macros).
	 *
	 * @param docxFile the .docx file (ZIP archive); the file name is not checked
	 * @return result with markdown string and conversion messages
	 */
	public DocxToMarkdownResult convert(File docxFile) {
		List<DocxConversionMessage> messages = new ArrayList<>();

		// Validate ZIP magic bytes
		if (!isZipFile(docxFile)) {
			messages.add(new DocxConversionMessage(Level.ERROR, "docx.convert.error.invalid.format"));
			return new DocxToMarkdownResult("", null, messages);
		}

		File tempDir = null;
		File mediaDir = null;
		try {
			// Create temp directory for media files
			tempDir = Files.createTempDirectory("docx_import_").toFile();
			mediaDir = new File(tempDir, "media");
			mediaDir.mkdirs();
		} catch (IOException e) {
			log.error("Failed to create temp directory for DOCX import", e);
			messages.add(new DocxConversionMessage(Level.ERROR,
				"docx.convert.error.read.failed", new String[]{ e.getMessage() }));
			return new DocxToMarkdownResult("", null, messages);
		}

		try (ZipFile zipFile = new ZipFile(docxFile)) {
			// Stage 1: Extract XML parts
			DocxArchiveContent content = DocxZipExtractor.extract(zipFile);

			// Stage 2: Parse relationships
			Map<String, DocxRelTarget> relationships = DocxRelationshipParser.parse(content.relsXml());

			// Stage 3: Parse numbering
			Map<Integer, DocxNumberingDef> numberingDefs = DocxNumberingParser.parse(content.numberingXml());

			// Stage 4: Parse styles
			Map<String, String> styleMap = DocxStyleParser.parse(content.stylesXml());

			// Stage 5: Parse metadata
			DocxMetadata metadata = DocxMetadataParser.parse(content.corePropsXml(), content.appPropsXml());

			// Stage 5b: Parse footnotes and endnotes
			Map<String, String> footnotes = DocxFootnoteParser.parse(content.footnotesXml());
			Map<String, String> endnotes = DocxFootnoteParser.parse(content.endnotesXml());

			// Stage 5c: Parse theme colors for SmartArt rendering
			Map<String, String> themeColors = DocxThemeParser.parse(content.themeXml());

			// Stage 5d: Pre-render SmartArt diagrams to SVG
			// The map is keyed by the diagramData rel ID (r:dm in dgm:relIds)
			// so the handler can look up the correct SVG per diagram.
			// Correlation: diagrams/data1.xml ↔ diagrams/drawing1.xml (same index).
			Map<String, String> drawingByIndex = new HashMap<>();
			for (Map.Entry<String, DocxRelTarget> rel : relationships.entrySet()) {
				String type = rel.getValue().type();
				if (type != null && type.contains("diagramDrawing")) {
					String idx = extractDiagramIndex(rel.getValue().target());
					if (idx != null) {
						drawingByIndex.put(idx, rel.getKey());
					}
				}
			}
			Map<String, String> smartArtSvgs = new HashMap<>();
			for (Map.Entry<String, DocxRelTarget> rel : relationships.entrySet()) {
				String type = rel.getValue().type();
				if (type != null && type.contains("diagramData")) {
					String idx = extractDiagramIndex(rel.getValue().target());
					String drawingRelId = idx != null ? drawingByIndex.get(idx) : null;
					if (drawingRelId != null) {
						DocxRelTarget drawingRel = relationships.get(drawingRelId);
						String svgFile = SmartArtRenderer.render(zipFile, drawingRel.target(),
								mediaDir, 5486400, 3200400, themeColors);
						if (svgFile != null) {
							// Key by diagramData rel ID — matches r:dm in dgm:relIds
							smartArtSvgs.put(rel.getKey(), svgFile);
						}
					}
				}
			}

			// Stage 6: Convert document.xml to markdown
			// Images are extracted to mediaDir and referenced relatively
			DocxToMarkdownHandler handler = new DocxToMarkdownHandler(
				relationships, numberingDefs, styleMap, zipFile, mediaDir, footnotes, endnotes
			);
			handler.setSmartArtSvgs(smartArtSvgs);

			SAXParser parser = XMLFactories.newSAXParser();
			parser.getXMLReader().setFeature(
				"http://apache.org/xml/features/disallow-doctype-decl", true);
			XMLReader reader = parser.getXMLReader();
			reader.setContentHandler(handler);
			reader.setErrorHandler(handler);
			// Stream document.xml directly from ZIP to avoid buffering the entire
			// XML in a byte array. For large documents this saves significant memory.
			try (InputStream docStream = zipFile.getInputStream(
					zipFile.getEntry("word/document.xml"))) {
				reader.parse(new InputSource(docStream));
			}

			// Collect messages from handler
			messages.addAll(handler.getMessages());

			// Build final markdown: front matter + body
			String frontMatter = metadata.toYamlFrontMatter();
			String body = handler.getMarkdown();
			String markdown = frontMatter.isEmpty() ? body : frontMatter + "\n" + body;

			return new DocxToMarkdownResult(markdown, tempDir, Collections.unmodifiableList(messages));

		} catch (IOException e) {
			log.error("Failed to read DOCX file: {}", docxFile.getName(), e);
			messages.add(new DocxConversionMessage(Level.ERROR,
				"docx.convert.error.read.failed", new String[]{ e.getMessage() }));
			return new DocxToMarkdownResult("", null, messages);
		} catch (Exception e) {
			log.error("DOCX conversion failed: {}", docxFile.getName(), e);
			messages.add(new DocxConversionMessage(Level.ERROR,
				"docx.convert.error.read.failed", new String[]{ e.getMessage() }));
			return new DocxToMarkdownResult("", null, messages);
		}
	}

	/**
	 * Extract the trailing number from a diagram target path.
	 * E.g., "diagrams/data1.xml" → "1", "diagrams/drawing2.xml" → "2".
	 */
	private static String extractDiagramIndex(String target) {
		if (target == null) return null;
		int lastSlash = target.lastIndexOf('/');
		String filename = lastSlash >= 0 ? target.substring(lastSlash + 1) : target;
		if (filename.endsWith(".xml")) filename = filename.substring(0, filename.length() - 4);
		StringBuilder digits = new StringBuilder();
		for (int i = filename.length() - 1; i >= 0; i--) {
			if (Character.isDigit(filename.charAt(i))) {
				digits.insert(0, filename.charAt(i));
			} else {
				break;
			}
		}
		return digits.isEmpty() ? null : digits.toString();
	}

	private static boolean isZipFile(File file) {
		try (var fis = new java.io.FileInputStream(file)) { //NOSONAR - intentional inline FQN
			byte[] magic = new byte[2];
			int read = fis.read(magic);
			return read == 2 && magic[0] == 0x50 && magic[1] == 0x4B;
		} catch (IOException e) {
			return false;
		}
	}
}
