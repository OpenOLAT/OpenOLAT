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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;

/**
 * Unit tests for {@link AssessmentItemAiGradingMarker} — inject, extract,
 * remove, readXmlFile, writeXmlFile, and DOM round-trip via temp files.
 *
 * Initial date: 2026-05-04<br>
 * @author Tom (AIT), https://www.frentix.com
 */
public class AssessmentItemAiGradingMarkerTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	// ---------------------------------------------------------------- minimal QTI item XML

	private static final String MINIMAL_QTI_XML =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<assessmentItem xmlns=\"http://www.imsglobal.org/xsd/imsqti_v2p1\""
			+ " identifier=\"test-item\" title=\"Test\""
			+ " adaptive=\"false\" timeDependent=\"false\">"
			+ "</assessmentItem>";

	// ---------------------------------------------------------------- parse helper

	@Test
	public void parse_validXmlReturnsDocument() {
		Document doc = AssessmentItemAiGradingMarker.parse(MINIMAL_QTI_XML);
		assertNotNull(doc);
	}

	@Test
	public void parse_nullReturnsNull() {
		assertNull(AssessmentItemAiGradingMarker.parse(null));
	}

	@Test
	public void parse_invalidXmlReturnsNull() {
		assertNull(AssessmentItemAiGradingMarker.parse("not xml at all <<<"));
	}

	// ---------------------------------------------------------------- inject

	@Test
	public void inject_returnsTrue() {
		Document doc = AssessmentItemAiGradingMarker.parse(MINIMAL_QTI_XML);
		AssessmentItemAiGradingMarker.Marker marker = new AssessmentItemAiGradingMarker.Marker(
				"abc123", 1, "kit-1", Instant.parse("2026-04-20T10:00:00Z"));
		assertTrue(AssessmentItemAiGradingMarker.inject(doc, marker));
	}

	@Test
	public void inject_nullDocReturnsFalse() {
		AssessmentItemAiGradingMarker.Marker marker = new AssessmentItemAiGradingMarker.Marker(
				"abc", 1, "kit", Instant.now());
		assertFalse(AssessmentItemAiGradingMarker.inject(null, marker));
	}

	@Test
	public void inject_nullMarkerReturnsFalse() {
		Document doc = AssessmentItemAiGradingMarker.parse(MINIMAL_QTI_XML);
		assertFalse(AssessmentItemAiGradingMarker.inject(doc, null));
	}

	// ---------------------------------------------------------------- extract

	@Test
	public void extract_afterInject_returnsMarker() {
		Document doc = AssessmentItemAiGradingMarker.parse(MINIMAL_QTI_XML);
		Instant now = Instant.parse("2026-04-20T10:00:00Z");
		AssessmentItemAiGradingMarker.Marker marker =
				new AssessmentItemAiGradingMarker.Marker("hash-xyz", 1, "kit-99", now);

		AssessmentItemAiGradingMarker.inject(doc, marker);

		Optional<AssessmentItemAiGradingMarker.Marker> extracted = AssessmentItemAiGradingMarker.extract(doc);
		assertTrue(extracted.isPresent());
		assertEquals("hash-xyz", extracted.get().hash());
		assertEquals(1, extracted.get().version());
		assertEquals("kit-99", extracted.get().kitId());
		assertEquals(now, extracted.get().generatedAt());
	}

	@Test
	public void extract_fromDocumentWithoutMarkerReturnsEmpty() {
		Document doc = AssessmentItemAiGradingMarker.parse(MINIMAL_QTI_XML);
		Optional<AssessmentItemAiGradingMarker.Marker> result = AssessmentItemAiGradingMarker.extract(doc);
		assertFalse(result.isPresent());
	}

	@Test
	public void extract_nullDocReturnsEmpty() {
		assertFalse(AssessmentItemAiGradingMarker.extract(null).isPresent());
	}

	// ---------------------------------------------------------------- remove

	@Test
	public void remove_markerIsRemovedAfterInject() {
		Document doc = AssessmentItemAiGradingMarker.parse(MINIMAL_QTI_XML);
		AssessmentItemAiGradingMarker.inject(doc, new AssessmentItemAiGradingMarker.Marker("h", 1, "k", null));

		boolean removed = AssessmentItemAiGradingMarker.remove(doc);
		assertTrue(removed);
		assertFalse(AssessmentItemAiGradingMarker.extract(doc).isPresent());
	}

	@Test
	public void remove_whenNoMarkerReturnsFalse() {
		Document doc = AssessmentItemAiGradingMarker.parse(MINIMAL_QTI_XML);
		assertFalse(AssessmentItemAiGradingMarker.remove(doc));
	}

	@Test
	public void remove_nullDocReturnsFalse() {
		assertFalse(AssessmentItemAiGradingMarker.remove(null));
	}

	// ---------------------------------------------------------------- idempotent inject (replace)

	@Test
	public void inject_isIdempotent() {
		Document doc = AssessmentItemAiGradingMarker.parse(MINIMAL_QTI_XML);
		AssessmentItemAiGradingMarker.Marker m1 =
				new AssessmentItemAiGradingMarker.Marker("hash-v1", 1, "kit-1", null);
		AssessmentItemAiGradingMarker.Marker m2 =
				new AssessmentItemAiGradingMarker.Marker("hash-v2", 1, "kit-2", null);

		AssessmentItemAiGradingMarker.inject(doc, m1);
		AssessmentItemAiGradingMarker.inject(doc, m2);

		// Only the second marker should survive
		Optional<AssessmentItemAiGradingMarker.Marker> extracted = AssessmentItemAiGradingMarker.extract(doc);
		assertTrue(extracted.isPresent());
		assertEquals("hash-v2", extracted.get().hash());
	}

	// ---------------------------------------------------------------- invalid version attribute

	@Test
	public void extract_invalidVersionAttributeDefaultsToZero() {
		String xmlWithBadVersion =
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<assessmentItem xmlns=\"http://www.imsglobal.org/xsd/imsqti_v2p1\""
				+ " identifier=\"test-item\" title=\"Test\""
				+ " adaptive=\"false\" timeDependent=\"false\">"
				+ "<ooExt:aiGrading xmlns:ooExt=\"http://www.openolat.org/xsd/qti/ext/ai-grading/v1\""
				+ " hash=\"abc\" version=\"notanumber\" kitId=\"kit\"/>"
				+ "</assessmentItem>";
		Document doc = AssessmentItemAiGradingMarker.parse(xmlWithBadVersion);
		Optional<AssessmentItemAiGradingMarker.Marker> extracted = AssessmentItemAiGradingMarker.extract(doc);
		assertTrue(extracted.isPresent());
		assertEquals(0, extracted.get().version());
	}

	// ---------------------------------------------------------------- readXmlFile / writeXmlFile round-trip

	@Test
	public void readWriteXmlFile_roundTrip() throws Exception {
		File xmlFile = tmp.newFile("item.xml");
		Files.write(xmlFile.toPath(), MINIMAL_QTI_XML.getBytes(StandardCharsets.UTF_8));

		Document doc = AssessmentItemAiGradingMarker.readXmlFile(xmlFile);
		assertNotNull(doc);

		AssessmentItemAiGradingMarker.Marker marker =
				new AssessmentItemAiGradingMarker.Marker("hash-file", 1, "kit-file",
						Instant.parse("2026-04-20T12:00:00Z"));
		AssessmentItemAiGradingMarker.inject(doc, marker);

		File outFile = tmp.newFile("item-out.xml");
		AssessmentItemAiGradingMarker.writeXmlFile(doc, outFile);

		// Re-read and verify marker survives file round-trip
		Document reloaded = AssessmentItemAiGradingMarker.readXmlFile(outFile);
		assertNotNull(reloaded);
		Optional<AssessmentItemAiGradingMarker.Marker> extracted = AssessmentItemAiGradingMarker.extract(reloaded);
		assertTrue(extracted.isPresent());
		assertEquals("hash-file", extracted.get().hash());
		assertEquals("kit-file", extracted.get().kitId());
	}

	@Test
	public void readXmlFile_nonExistentFileReturnsNull() throws Exception {
		File notExisting = new File(tmp.getRoot(), "missing.xml");
		assertNull(AssessmentItemAiGradingMarker.readXmlFile(notExisting));
	}

	@Test
	public void readXmlFile_nullReturnsNull() {
		assertNull(AssessmentItemAiGradingMarker.readXmlFile(null));
	}

	// ---------------------------------------------------------------- toXmlString

	@Test
	public void toXmlString_roundTrip() {
		Document doc = AssessmentItemAiGradingMarker.parse(MINIMAL_QTI_XML);
		AssessmentItemAiGradingMarker.inject(doc,
				new AssessmentItemAiGradingMarker.Marker("hash-str", 1, "kit-str", null));
		String xml = AssessmentItemAiGradingMarker.toXmlString(doc);
		assertNotNull(xml);
		assertTrue(xml.contains("aiGrading"));
		assertTrue(xml.contains("hash-str"));
	}

	// ---------------------------------------------------------------- namespace and constants

	@Test
	public void constants_areExpectedValues() {
		assertEquals("http://www.openolat.org/xsd/qti/ext/ai-grading/v1",
				AssessmentItemAiGradingMarker.NAMESPACE_URI);
		assertEquals("ooExt", AssessmentItemAiGradingMarker.NAMESPACE_PREFIX);
		assertEquals("aiGrading", AssessmentItemAiGradingMarker.LOCAL_NAME);
	}
}
