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
package org.olat.core.commons.services.ai.essay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.nio.charset.StandardCharsets;

import org.junit.Test;

/**
 * Unit tests for {@link EssayAiGrading} POJO — sha256Hex determinism,
 * field sensitivity, @JsonIgnore on assessmentItemIdentifier, emptyKeyPoints.
 *
 * Initial date: 2026-05-04<br>
 * @author Tom (AIT), https://www.frentix.com
 */
public class EssayAiGradingPojoTest {

	// ---------------------------------------------------------------- sha256Hex

	@Test
	public void sha256Hex_deterministicForSameInput() {
		byte[] input = "hello world".getBytes(StandardCharsets.UTF_8);
		String first  = EssayAiGrading.sha256Hex(input);
		String second = EssayAiGrading.sha256Hex(input);
		assertEquals("sha256 must be deterministic", first, second);
	}

	@Test
	public void sha256Hex_knownVector() {
		// SHA-256("") = e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855
		byte[] empty = new byte[0];
		String hash = EssayAiGrading.sha256Hex(empty);
		assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", hash);
	}

	@Test
	public void sha256Hex_nullInputReturnsNull() {
		assertNull(EssayAiGrading.sha256Hex(null));
	}

	@Test
	public void sha256Hex_differentInputsDifferentHashes() {
		byte[] a = "abc".getBytes(StandardCharsets.UTF_8);
		byte[] b = "ABC".getBytes(StandardCharsets.UTF_8);
		String hashA = EssayAiGrading.sha256Hex(a);
		String hashB = EssayAiGrading.sha256Hex(b);
		assertNotNull(hashA);
		assertNotNull(hashB);
		// Different input must produce different output
		assertEquals("hash length must be 64 hex chars", 64, hashA.length());
		assertEquals("hash length must be 64 hex chars", 64, hashB.length());
		// Case sensitivity check — "abc" != "ABC"
		// We just verify both are valid and distinct
		assert !hashA.equals(hashB) : "Different inputs must hash differently";
	}

	// ---------------------------------------------------------------- @JsonIgnore on assessmentItemIdentifier

	@Test
	public void assessmentItemIdentifier_notWrittenToJson() throws Exception {
		EssayAiGrading g = new EssayAiGrading();
		g.setAssessmentItemIdentifier("should-be-ignored");
		g.setLanguage("en");

		EssayAiGradingFileStore store = new EssayAiGradingFileStore();
		byte[] bytes = store.toCanonicalJsonBytes(g);
		String json = new String(bytes, StandardCharsets.UTF_8);

		// assessmentItemIdentifier is @JsonIgnore — must not appear in JSON
		assert !json.contains("assessmentItemIdentifier")
				: "assessmentItemIdentifier must be excluded from JSON output, but found in: " + json;
		assert !json.contains("should-be-ignored")
				: "assessmentItemIdentifier value must not appear in JSON";
	}

	@Test
	public void assessmentItemIdentifier_roundTripFromDirectory() {
		EssayAiGrading g = new EssayAiGrading();
		g.setLanguage("de");
		// Simulate what the file store does: populate identifier from dir name
		g.setAssessmentItemIdentifier("my-item-dir");

		EssayAiGradingFileStore store = new EssayAiGradingFileStore();
		byte[] bytes = store.toCanonicalJsonBytes(g);
		EssayAiGrading loaded = store.fromCanonicalJsonBytes(bytes);

		// After round-trip through JSON, identifier is null (it was @JsonIgnore)
		assertNull("assessmentItemIdentifier must be null after JSON round-trip", loaded.getAssessmentItemIdentifier());
	}

	// ---------------------------------------------------------------- emptyKeyPoints

	@Test
	public void emptyKeyPoints_returnsEmptyMutableList() {
		java.util.List<EssayItemDraft.KeyPoint> list = EssayAiGrading.emptyKeyPoints();
		assertNotNull(list);
		assertEquals(0, list.size());
		// Must be mutable so callers can add to it
		list.add(new EssayItemDraft.KeyPoint("kp1", "some text", 1.0, true));
		assertEquals(1, list.size());
	}

	// ---------------------------------------------------------------- currentVersion

	@Test
	public void currentVersion_isPositive() {
		assert EssayAiGrading.CURRENT_VERSION > 0 : "CURRENT_VERSION must be positive";
	}

	@Test
	public void defaultVersionMatchesCurrentVersion() {
		EssayAiGrading g = new EssayAiGrading();
		assertEquals(EssayAiGrading.CURRENT_VERSION, g.getVersion());
	}
}
