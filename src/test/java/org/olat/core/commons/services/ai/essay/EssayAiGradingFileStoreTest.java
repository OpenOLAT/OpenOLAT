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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Unit tests for {@link EssayAiGradingFileStore} — load/save/exists, JSON
 * round-trip, missing fields, unicode content, null-safety.
 *
 * Initial date: 2026-05-04<br>
 * @author Tom (AIT), https://www.frentix.com
 */
public class EssayAiGradingFileStoreTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private final EssayAiGradingFileStore store = new EssayAiGradingFileStore();

	// ---------------------------------------------------------------- null-safety

	@Test
	public void load_nullDirReturnsNull() {
		assertNull(store.load(null));
	}

	@Test
	public void save_nullDirReturnsEmptyArray() {
		byte[] result = store.save(null, new EssayAiGrading());
		assertNotNull(result);
		assertEquals(0, result.length);
	}

	@Test
	public void save_nullGradingReturnsEmptyArray() throws Exception {
		byte[] result = store.save(tmp.newFolder(), null);
		assertNotNull(result);
		assertEquals(0, result.length);
	}

	@Test
	public void exists_nullDirReturnsFalse() {
		assertFalse(store.exists(null));
	}

	@Test
	public void fileFor_nullReturnsNull() {
		assertNull(store.fileFor(null));
	}

	// ---------------------------------------------------------------- round-trip

	@Test
	public void save_thenLoad_roundTrip() throws Exception {
		File dir = tmp.newFolder("q-abc123");
		EssayAiGrading grading = buildSample();

		store.save(dir, grading);

		assertTrue("File must exist after save", store.exists(dir));
		EssayAiGrading loaded = store.load(dir);
		assertNotNull("Loaded grading must not be null", loaded);
		assertEquals("en", loaded.getLanguage());
		assertEquals("model answer here", loaded.getModelAnswer());
		assertEquals("[{\"id\":\"kp1\",\"text\":\"key point\",\"weight\":0.8,\"required\":true}]", loaded.getKeyPointsJson());
		assertEquals(42, loaded.getTokenEstimate());
	}

	@Test
	public void load_populatesAssessmentItemIdentifierFromDirName() throws Exception {
		File dir = tmp.newFolder("my-item-identifier");
		store.save(dir, buildSample());
		EssayAiGrading loaded = store.load(dir);
		assertEquals("my-item-identifier", loaded.getAssessmentItemIdentifier());
	}

	@Test
	public void load_nonExistentFileReturnsNull() throws Exception {
		File dir = tmp.newFolder("empty-dir");
		assertNull("Missing file must return null", store.load(dir));
	}

	@Test
	public void save_stampsKitIdWhenMissing() throws Exception {
		File dir = tmp.newFolder();
		EssayAiGrading g = new EssayAiGrading();
		assertNull(g.getKitId());
		store.save(dir, g);
		EssayAiGrading loaded = store.load(dir);
		assertNotNull("kitId must be stamped by save()", loaded.getKitId());
		assertFalse(loaded.getKitId().isBlank());
	}

	@Test
	public void save_stampsGeneratedAtWhenMissing() throws Exception {
		File dir = tmp.newFolder();
		EssayAiGrading g = new EssayAiGrading();
		store.save(dir, g);
		EssayAiGrading loaded = store.load(dir);
		assertNotNull(loaded.getGeneratedAt());
		assertFalse(loaded.getGeneratedAt().isBlank());
	}

	@Test
	public void save_doesNotOverrideExistingKitId() throws Exception {
		File dir = tmp.newFolder();
		EssayAiGrading g = new EssayAiGrading();
		g.setKitId("my-stable-kit-id");
		store.save(dir, g);
		EssayAiGrading loaded = store.load(dir);
		assertEquals("my-stable-kit-id", loaded.getKitId());
	}

	// ---------------------------------------------------------------- unicode

	@Test
	public void save_thenLoad_unicodeContent() throws Exception {
		File dir = tmp.newFolder();
		EssayAiGrading g = new EssayAiGrading();
		g.setLanguage("zh");
		g.setModelAnswer("这是一个测试答案。Ümlauts: äöü. Greek: αβγδ. Emoji: 😀");
		store.save(dir, g);
		EssayAiGrading loaded = store.load(dir);
		assertEquals(g.getModelAnswer(), loaded.getModelAnswer());
	}

	// ---------------------------------------------------------------- unknown fields (forward compat)

	@Test
	public void fromCanonicalJsonBytes_ignoresUnknownFields() {
		String json = "{\"language\":\"de\",\"unknownFutureField\":\"ignored\",\"modelAnswer\":\"test\"}";
		EssayAiGrading g = store.fromCanonicalJsonBytes(json.getBytes(StandardCharsets.UTF_8));
		assertNotNull(g);
		assertEquals("de", g.getLanguage());
		assertEquals("test", g.getModelAnswer());
	}

	@Test
	public void fromCanonicalJsonBytes_nullInputReturnsNull() {
		assertNull(store.fromCanonicalJsonBytes(null));
	}

	@Test
	public void fromCanonicalJsonBytes_emptyArrayReturnsNull() {
		assertNull(store.fromCanonicalJsonBytes(new byte[0]));
	}

	// ---------------------------------------------------------------- toCanonicalJsonBytes

	@Test
	public void toCanonicalJsonBytes_nullReturnsEmptyArray() {
		byte[] result = store.toCanonicalJsonBytes(null);
		assertNotNull(result);
		assertEquals(0, result.length);
	}

	@Test
	public void toCanonicalJsonBytes_producesValidJson() {
		EssayAiGrading g = new EssayAiGrading();
		g.setLanguage("fr");
		byte[] bytes = store.toCanonicalJsonBytes(g);
		assertTrue(bytes.length > 0);
		String json = new String(bytes, StandardCharsets.UTF_8);
		assertTrue(json.contains("\"language\""));
		assertTrue(json.contains("\"fr\""));
	}

	// ---------------------------------------------------------------- parseKeyPoints helper

	@Test
	public void parseKeyPoints_nullJsonReturnsEmptyList() {
		assertEquals(0, EssayAiGradingFileStore.parseKeyPoints(null).size());
	}

	@Test
	public void parseKeyPoints_blankJsonReturnsEmptyList() {
		assertEquals(0, EssayAiGradingFileStore.parseKeyPoints("  ").size());
	}

	@Test
	public void parseKeyPoints_validJson() {
		String json = "[{\"id\":\"kp1\",\"text\":\"key point one\",\"weight\":0.5,\"required\":true}]";
		java.util.List<EssayItemDraft.KeyPoint> list = EssayAiGradingFileStore.parseKeyPoints(json);
		assertEquals(1, list.size());
		assertEquals("kp1", list.get(0).id());
		assertEquals("key point one", list.get(0).text());
	}

	@Test
	public void parseMisconceptions_validJson() {
		String json = "[\"misconception A\",\"misconception B\"]";
		java.util.List<String> list = EssayAiGradingFileStore.parseMisconceptions(json);
		assertEquals(2, list.size());
		assertEquals("misconception A", list.get(0));
	}

	// ---------------------------------------------------------------- fileFor

	@Test
	public void fileFor_returnsCorrectPath() throws Exception {
		File dir = tmp.newFolder("x-item");
		File f = store.fileFor(dir);
		assertNotNull(f);
		assertEquals(EssayAiGradingFileStore.FILENAME, f.getName());
		assertEquals(dir.getAbsolutePath(), f.getParentFile().getAbsolutePath());
	}

	// ---------------------------------------------------------------- helpers

	private EssayAiGrading buildSample() {
		EssayAiGrading g = new EssayAiGrading();
		g.setLanguage("en");
		g.setModelAnswer("model answer here");
		g.setKeyPointsJson("[{\"id\":\"kp1\",\"text\":\"key point\",\"weight\":0.8,\"required\":true}]");
		g.setTokenEstimate(42);
		return g;
	}
}
