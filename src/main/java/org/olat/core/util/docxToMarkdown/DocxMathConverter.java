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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.SAXParser;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.xml.XMLFactories;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import fmath.conversion.ConvertFromMathMLToLatex;
import fmath.conversion.ConvertFromWordToMathML;

/**
 * Converts OOXML math (m:oMath XML) to LaTeX via fmath.
 * Pipeline: m:oMath XML → minimal DOCX in-memory → fmath getMathMLFromDocStream → MathML → LaTeX
 *
 * @author gnaegi, https://www.frentix.com
 */
class DocxMathConverter {

	private static final Logger log = Tracing.createLoggerFor(DocxMathConverter.class);

	/** Minimal document.xml wrapper for a single m:oMath element */
	private static final String DOC_PREFIX =
		"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
		+ "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
		+ " xmlns:m=\"http://schemas.openxmlformats.org/officeDocument/2006/math\">"
		+ "<w:body><w:p><m:oMathPara>";

	private static final String DOC_SUFFIX =
		"</m:oMathPara></w:p></w:body></w:document>";

	/** Minimal [Content_Types].xml required for a valid DOCX ZIP */
	private static final String CONTENT_TYPES =
		"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
		+ "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
		+ "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
		+ "<Override PartName=\"/word/document.xml\""
		+ " ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/>"
		+ "</Types>";

	/** Minimal _rels/.rels */
	private static final String RELS =
		"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
		+ "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
		+ "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\""
		+ " Target=\"word/document.xml\"/>"
		+ "</Relationships>";

	private DocxMathConverter() {
		// utility
	}

	/**
	 * Pre-extract MathML from the original DOCX file using fmath.
	 * Returns the full MathML string, or null if extraction fails.
	 */
	static String extractMathMLFromDocx(File docxFile) {
		if (docxFile == null || !docxFile.exists()) {
			return null;
		}
		try (InputStream in = new FileInputStream(docxFile)) {
			String mathml = ConvertFromWordToMathML.getMathMLFromDocStream(in, "");
			if (mathml != null && !mathml.isBlank()) {
				log.debug("Pre-extracted MathML from DOCX ({} chars)", mathml.length());
				return mathml;
			}
		} catch (Exception e) {
			log.debug("fmath pre-extraction failed: {}", e.getMessage());
		}
		return null;
	}

	/**
	 * Convert pre-extracted MathML to LaTeX.
	 * @param mathml the MathML string from fmath
	 * @return LaTeX without delimiters, or null
	 */
	static String convertMathMLToLatex(String mathml) {
		if (mathml == null || mathml.isBlank()) return null;
		try {
			String latex = ConvertFromMathMLToLatex.convertToLatex(mathml);
			if (latex == null || latex.isBlank()) return null;
			return stripDelimiters(latex.trim());
		} catch (Exception e) {
			log.debug("MathML→LaTeX conversion failed: {}", e.getMessage());
			return null;
		}
	}

	/**
	 * Convert an OOXML m:oMath XML fragment to a LaTeX string.
	 * Wraps the fragment in a minimal in-memory DOCX, passes it to fmath
	 * for OOXML→MathML conversion, then converts MathML→LaTeX.
	 *
	 * @param oMathXml the raw XML of an m:oMath element
	 * @return the LaTeX string (without delimiters), or null if conversion fails
	 */
	static String convertToLatex(String oMathXml) {
		if (oMathXml == null || oMathXml.isBlank()) {
			return null;
		}
		try {
			// Step 1: Build minimal DOCX ZIP in memory
			byte[] docxBytes = buildMinimalDocx(oMathXml);
			log.debug("Built minimal DOCX for math: {} bytes", docxBytes.length);

			// Step 2: OOXML → MathML via fmath
			String mathml;
			try (InputStream docxStream = new ByteArrayInputStream(docxBytes)) {
				mathml = ConvertFromWordToMathML.getMathMLFromDocStream(docxStream, "");
			}
			if (mathml == null || mathml.isBlank()) {
				log.warn("fmath getMathMLFromDocStream returned empty result for input of {} chars", oMathXml.length());
				// Fallback: extract plain text from math XML
				return extractMathText(oMathXml);
			}
			log.debug("MathML result: {}", mathml.length() > 300 ? mathml.substring(0, 300) + "..." : mathml);

			// Step 3: MathML → LaTeX
			String latex = ConvertFromMathMLToLatex.convertToLatex(mathml);
			if (latex == null || latex.isBlank()) {
				log.warn("fmath ConvertFromMathMLToLatex returned empty for MathML of {} chars", mathml.length());
				// Fallback: extract plain text from math XML
				return extractMathText(oMathXml);
			}
			log.debug("LaTeX result: {}", latex);
			return stripDelimiters(latex.trim());
		} catch (Exception e) {
			log.warn("Math conversion failed: {}", e.getMessage());
			// Fallback: extract plain text from math XML
			return extractMathText(oMathXml);
		}
	}

	/**
	 * Fallback: extract text content from m:t elements in the math XML.
	 * This provides at least the raw formula text when fmath conversion fails.
	 */
	static String extractMathText(String mathXml) {
		if (mathXml == null) return null;
		try {
			StringBuilder text = new StringBuilder();
			SAXParser parser = XMLFactories.newSAXParser();
			parser.getXMLReader().setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			parser.parse(new ByteArrayInputStream(mathXml.getBytes(StandardCharsets.UTF_8)),
				new DefaultHandler() {
					private boolean inMt = false;
					@Override
					public void startElement(String uri, String localName, String qName, Attributes attrs) {
						if ("m:t".equals(qName) || "t".equals(qName)) inMt = true;
					}
					@Override
					public void endElement(String uri, String localName, String qName) {
						if ("m:t".equals(qName) || "t".equals(qName)) inMt = false;
					}
					@Override
					public void characters(char[] ch, int start, int length) {
						if (inMt) text.append(ch, start, length);
					}
				});
			String result = text.toString().trim();
			return result.isEmpty() ? null : result;
		} catch (Exception e) {
			log.debug("Math text extraction failed: {}", e.getMessage());
			return null;
		}
	}

	/**
	 * Builds a minimal DOCX (ZIP) containing only the math fragment wrapped
	 * in a valid document.xml, plus the required [Content_Types].xml and _rels/.rels.
	 */
	private static byte[] buildMinimalDocx(String oMathXml) throws Exception {
		String documentXml = DOC_PREFIX + oMathXml + DOC_SUFFIX;

		ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
		try (ZipOutputStream zos = new ZipOutputStream(baos)) {
			addZipEntry(zos, "[Content_Types].xml", CONTENT_TYPES);
			addZipEntry(zos, "_rels/.rels", RELS);
			addZipEntry(zos, "word/document.xml", documentXml);
		}
		return baos.toByteArray();
	}

	private static void addZipEntry(ZipOutputStream zos, String name, String content) throws Exception {
		zos.putNextEntry(new ZipEntry(name));
		zos.write(content.getBytes(StandardCharsets.UTF_8));
		zos.closeEntry();
	}

	private static String stripDelimiters(String latex) {
		if (latex.startsWith("$$") && latex.endsWith("$$")) {
			return latex.substring(2, latex.length() - 2).trim();
		}
		if (latex.startsWith("$") && latex.endsWith("$") && latex.length() > 2) {
			return latex.substring(1, latex.length() - 1).trim();
		}
		if (latex.startsWith("\\[") && latex.endsWith("\\]")) {
			return latex.substring(2, latex.length() - 2).trim();
		}
		if (latex.startsWith("\\(") && latex.endsWith("\\)")) {
			return latex.substring(2, latex.length() - 2).trim();
		}
		return latex;
	}
}
