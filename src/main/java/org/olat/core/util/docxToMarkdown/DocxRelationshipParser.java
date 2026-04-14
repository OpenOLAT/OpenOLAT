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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.SAXParser;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.xml.XMLFactories;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Stage 2: SAX parser for word/_rels/document.xml.rels.
 * Builds relationship id → DocxRelTarget map.
 *
 * @author gnaegi, https://www.frentix.com
 */
class DocxRelationshipParser extends DefaultHandler {

	private static final Logger log = Tracing.createLoggerFor(DocxRelationshipParser.class);

	static final String TYPE_HYPERLINK =
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/hyperlink";
	static final String TYPE_IMAGE =
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/image";
	static final String TYPE_VIDEO =
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/video";
	static final String TYPE_MEDIA =
		"http://schemas.microsoft.com/office/2007/relationships/media";

	private static final String TARGET_MODE_EXTERNAL = "External";
	private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https", "mailto");

	private final Map<String, DocxRelTarget> relationships = new HashMap<>();

	private DocxRelationshipParser() {
		// use parse()
	}

	static Map<String, DocxRelTarget> parse(byte[] relsXml) {
		if (relsXml == null) {
			return Collections.emptyMap();
		}
		DocxRelationshipParser handler = new DocxRelationshipParser();
		try {
			SAXParser parser = XMLFactories.newSAXParser();
			parser.getXMLReader().setFeature(
				"http://apache.org/xml/features/disallow-doctype-decl", true);
			parser.parse(new ByteArrayInputStream(relsXml), handler);
		} catch (Exception e) {
			log.warn("Failed to parse DOCX relationships; returning empty map.", e);
			return Collections.emptyMap();
		}
		return Collections.unmodifiableMap(handler.relationships);
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes)
			throws SAXException {
		String name = localName != null && !localName.isEmpty() ? localName : qName;
		if (!"Relationship".equals(name)) {
			return;
		}

		String id = attributes.getValue("Id");
		String type = attributes.getValue("Type");
		String target = attributes.getValue("Target");
		String targetMode = attributes.getValue("TargetMode");

		if (id == null || type == null || target == null) {
			return;
		}

		if (TARGET_MODE_EXTERNAL.equalsIgnoreCase(targetMode)) {
			target = sanitizeExternalTarget(id, type, target);
			if (target == null) {
				return;
			}
		}

		relationships.put(id, new DocxRelTarget(type, target));
	}

	private static String sanitizeExternalTarget(String id, String type, String target) {
		String lower = target.toLowerCase();
		int colonIdx = lower.indexOf(':');
		if (colonIdx < 0) {
			log.warn("External relationship '{}' has no URI scheme in '{}'; rejected.", id, target);
			return null;
		}
		String scheme = lower.substring(0, colonIdx);
		if (!ALLOWED_SCHEMES.contains(scheme)) {
			log.warn("External relationship '{}' uses disallowed scheme '{}'; rejected.", id, scheme);
			return null;
		}
		return target;
	}
}
