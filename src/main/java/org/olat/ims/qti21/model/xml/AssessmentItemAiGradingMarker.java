/**
 * <a href="https://www.openolat.org">
 * OpenOlat - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.ims.qti21.model.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * Helper for the {@code <ooExt:aiGrading>} extension marker that bridges
 * a QTI assessment item XML and its companion {@code ai-grading.json}
 * file. The marker is injected as the last child of {@code <assessmentItem>}
 * and carries an integrity {@code hash}, a stable {@code kitId}, the
 * generation timestamp and the schema version.
 * <p>
 * Operations are pure DOM manipulations; no XPath, no jqtiplus parsing.
 * The export pipeline calls {@link #inject(Document, Marker)} after
 * regenerating the marker; the import pipeline calls
 * {@link #extract(Document)} (and then optionally
 * {@link #remove(Document)} once the marker has been verified against the
 * companion file).
 *
 * Initial date: 2026-04-20<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
public final class AssessmentItemAiGradingMarker {

	/** Namespace URI of the OpenOlat AI-grading extension (v1). */
	public static final String NAMESPACE_URI = "http://www.openolat.org/xsd/qti/ext/ai-grading/v1";
	/** Recommended prefix for the namespace. */
	public static final String NAMESPACE_PREFIX = "ooExt";
	/** Local element name. */
	public static final String LOCAL_NAME = "aiGrading";

	private AssessmentItemAiGradingMarker() {
		// utility
	}

	/**
	 * Read an XML file into a DOM document. Returns {@code null} on parse
	 * failure (callers treat this as "no marker").
	 */
	public static Document readXmlFile(File xmlFile) {
		if (xmlFile == null || !xmlFile.exists()) {
			return null;
		}
		try (InputStream in = Files.newInputStream(xmlFile.toPath())) {
			DocumentBuilderFactory factory = newSecureFactory();
			DocumentBuilder builder = factory.newDocumentBuilder();
			return builder.parse(in);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			return null;
		}
	}

	/**
	 * Serialise the document back to disk preserving its current namespace
	 * declarations. UTF-8.
	 */
	public static void writeXmlFile(Document doc, File targetFile) throws IOException {
		try (OutputStream out = Files.newOutputStream(targetFile.toPath())) {
			writeXml(doc, out);
		}
	}

	private static void writeXml(Document doc, OutputStream out) throws IOException {
		try {
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.transform(new DOMSource(doc), new StreamResult(out));
		} catch (TransformerException e) {
			throw new IOException("Failed to serialise XML", e);
		}
	}

	/**
	 * Inject (or replace) an {@code <ooExt:aiGrading>} element as the last
	 * child of the document's root {@code <assessmentItem>} element.
	 * Existing markers are removed first, so this method is idempotent.
	 *
	 * @return {@code true} if the marker was injected; {@code false} if the
	 *         document has no root element to attach to.
	 */
	public static boolean inject(Document doc, Marker marker) {
		if (doc == null || marker == null) {
			return false;
		}
		Element root = doc.getDocumentElement();
		if (root == null) {
			return false;
		}
		removeFromRoot(root);
		Element extEl = doc.createElementNS(NAMESPACE_URI, NAMESPACE_PREFIX + ":" + LOCAL_NAME);
		// Always declare the namespace inline so the element survives
		// re-serialisation cleanly even when the root element does not yet
		// declare the prefix.
		extEl.setAttributeNS("http://www.w3.org/2000/xmlns/",
				"xmlns:" + NAMESPACE_PREFIX, NAMESPACE_URI);
		extEl.setAttribute("hash", nullToEmpty(marker.hash()));
		extEl.setAttribute("version", Integer.toString(marker.version()));
		extEl.setAttribute("kitId", nullToEmpty(marker.kitId()));
		extEl.setAttribute("generatedAt",
				marker.generatedAt() == null ? "" : marker.generatedAt().toString());
		root.appendChild(extEl);
		return true;
	}

	/**
	 * Extract the AI-grading marker from the root element. Returns
	 * {@code Optional.empty()} when no marker is present.
	 */
	public static Optional<Marker> extract(Document doc) {
		if (doc == null) {
			return Optional.empty();
		}
		Element root = doc.getDocumentElement();
		if (root == null) {
			return Optional.empty();
		}
		Element extEl = findMarkerElement(root);
		if (extEl == null) {
			return Optional.empty();
		}
		String hash = extEl.getAttribute("hash");
		String versionAttr = extEl.getAttribute("version");
		int version;
		try {
			version = versionAttr == null || versionAttr.isBlank()
					? 0 : Integer.parseInt(versionAttr.trim());
		} catch (NumberFormatException e) {
			version = 0;
		}
		String kitId = extEl.getAttribute("kitId");
		String generatedAtAttr = extEl.getAttribute("generatedAt");
		Instant generatedAt = null;
		if (generatedAtAttr != null && !generatedAtAttr.isBlank()) {
			try {
				generatedAt = Instant.parse(generatedAtAttr.trim());
			} catch (Exception e) {
				generatedAt = null;
			}
		}
		return Optional.of(new Marker(emptyToNull(hash), version, emptyToNull(kitId), generatedAt));
	}

	/**
	 * Remove all AI-grading markers from the root element. No-op when none
	 * are present. Safe to call before persisting a "clean" XML.
	 */
	public static boolean remove(Document doc) {
		if (doc == null) {
			return false;
		}
		Element root = doc.getDocumentElement();
		if (root == null) {
			return false;
		}
		return removeFromRoot(root);
	}

	private static boolean removeFromRoot(Element root) {
		boolean removed = false;
		NodeList children = root.getChildNodes();
		// Iterate from end to start: removing by index doesn't disturb
		// earlier indices.
		for (int i = children.getLength() - 1; i >= 0; i--) {
			Node node = children.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE && isMarker(node)) {
				root.removeChild(node);
				removed = true;
			}
		}
		return removed;
	}

	private static Element findMarkerElement(Element root) {
		NodeList children = root.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node n = children.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE && isMarker(n)) {
				return (Element) n;
			}
		}
		return null;
	}

	private static boolean isMarker(Node node) {
		return NAMESPACE_URI.equals(node.getNamespaceURI())
				&& LOCAL_NAME.equals(node.getLocalName());
	}

	private static DocumentBuilderFactory newSecureFactory() throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
		factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		factory.setXIncludeAware(false);
		factory.setExpandEntityReferences(false);
		return factory;
	}

	/**
	 * In-memory parsing helper used by tests and lightweight callers.
	 */
	public static Document parse(String xml) {
		if (xml == null) {
			return null;
		}
		try {
			DocumentBuilder builder = newSecureFactory().newDocumentBuilder();
			return builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
		} catch (ParserConfigurationException | SAXException | IOException e) {
			return null;
		}
	}

	/**
	 * In-memory serialisation helper used by tests and lightweight callers.
	 */
	public static String toXmlString(Document doc) {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			writeXml(doc, baos);
			return baos.toString(StandardCharsets.UTF_8);
		} catch (IOException e) {
			return null;
		}
	}

	private static String nullToEmpty(String s) {
		return s == null ? "" : s;
	}

	private static String emptyToNull(String s) {
		return s == null || s.isEmpty() ? null : s;
	}

	/**
	 * Marker payload — the four attributes carried by the
	 * {@code <ooExt:aiGrading>} element. Plain immutable record so it can
	 * cross thread / package boundaries without coupling.
	 */
	public record Marker(String hash, int version, String kitId, Instant generatedAt) { }
}
