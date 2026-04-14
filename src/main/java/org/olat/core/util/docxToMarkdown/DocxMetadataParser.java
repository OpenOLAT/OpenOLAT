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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.SAXParser;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.xml.XMLFactories;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Stage 5: SAX parser for docProps/core.xml and docProps/app.xml.
 * Extracts document metadata into a DocxMetadata record.
 *
 * @author gnaegi, https://www.frentix.com
 */
class DocxMetadataParser extends DefaultHandler {

	private static final Logger log = Tracing.createLoggerFor(DocxMetadataParser.class);

	private static final DocxMetadata EMPTY = new DocxMetadata(
		null, null, null, Collections.emptyList(), null, null, null, null, null, null
	);

	private String title;
	private String author;
	private String subject;
	private String keywords;
	private String description;
	private String created;
	private String modified;
	private String lastModifiedBy;
	private String category;
	private String revision;

	private StringBuilder charBuffer;
	private String currentElement;

	private DocxMetadataParser() {
		// use parse()
	}

	static DocxMetadata parse(byte[] corePropsXml, byte[] appPropsXml) {
		if (corePropsXml == null && appPropsXml == null) {
			return EMPTY;
		}
		DocxMetadataParser handler = new DocxMetadataParser();
		if (corePropsXml != null) {
			try {
				SAXParser parser = XMLFactories.newSAXParser();
				parser.getXMLReader().setFeature(
					"http://apache.org/xml/features/disallow-doctype-decl", true);
				parser.parse(new ByteArrayInputStream(corePropsXml), handler);
			} catch (Exception e) {
				log.warn("Failed to parse docProps/core.xml.", e);
			}
		}
		// appPropsXml reserved for future use
		return handler.buildResult();
	}

	private DocxMetadata buildResult() {
		List<String> keywordList = Collections.emptyList();
		if (keywords != null && !keywords.isBlank()) {
			List<String> list = new ArrayList<>();
			for (String kw : keywords.split(",")) {
				String trimmed = kw.trim();
				if (!trimmed.isEmpty()) {
					list.add(trimmed);
				}
			}
			keywordList = List.copyOf(list);
		}
		return new DocxMetadata(title, author, subject, keywordList, description,
			created, modified, lastModifiedBy, category, revision);
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes)
			throws SAXException {
		// Element names in non-namespace-aware mode include prefixes:
		// dc:title, dc:creator, dc:subject, cp:keywords, dc:description,
		// dcterms:created, dcterms:modified, cp:lastModifiedBy, cp:category, cp:revision
		currentElement = qName;
		charBuffer = new StringBuilder();
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (charBuffer != null) {
			charBuffer.append(ch, start, length);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (charBuffer == null || currentElement == null) {
			return;
		}
		String value = charBuffer.toString().trim();
		if (value.isEmpty()) {
			charBuffer = null;
			currentElement = null;
			return;
		}

		switch (qName) {
			case "dc:title" -> title = value;
			case "dc:creator" -> author = value;
			case "dc:subject" -> subject = value;
			case "cp:keywords" -> keywords = value;
			case "dc:description" -> description = value;
			case "dcterms:created" -> created = value;
			case "dcterms:modified" -> modified = value;
			case "cp:lastModifiedBy" -> lastModifiedBy = value;
			case "cp:category" -> category = value;
			case "cp:revision" -> revision = value;
			default -> { /* ignore */ }
		}

		charBuffer = null;
		currentElement = null;
	}
}
