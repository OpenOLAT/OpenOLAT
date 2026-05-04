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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Unit tests for {@link AiSourceCompanionFileStore} — mirrors EssayAiGradingFileStore
 * tests for the smaller MC-provenance payload.
 *
 * Initial date: 2026-05-04<br>
 * @author Tom (AIT), https://www.frentix.com
 */
public class AiSourceCompanionFileStoreTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private final AiSourceCompanionFileStore store = new AiSourceCompanionFileStore();

	// ---------------------------------------------------------------- null-safety

	@Test
	public void load_nullDirReturnsNull() {
		assertNull(store.load(null));
	}

	@Test
	public void save_nullDirIsNoOp() {
		// Must not throw
		store.save(null, new AiSourceCompanion("spi1", "model1", null));
	}

	@Test
	public void save_nullCompanionIsNoOp() throws Exception {
		// Must not throw
		store.save(tmp.newFolder(), null);
	}

	@Test
	public void exists_nullReturnsFalse() {
		assertFalse(store.exists(null));
	}

	@Test
	public void fileFor_nullReturnsNull() {
		assertNull(store.fileFor(null));
	}

	// ---------------------------------------------------------------- round-trip

	@Test
	public void save_thenLoad_roundTrip() throws Exception {
		File dir = tmp.newFolder();
		AiSourceCompanion companion = new AiSourceCompanion("anthropic", "claude-3-5", "2026-04-20T10:00:00Z");
		store.save(dir, companion);

		assertTrue(store.exists(dir));
		AiSourceCompanion loaded = store.load(dir);
		assertNotNull(loaded);
		assertEquals("anthropic", loaded.getSpi());
		assertEquals("claude-3-5", loaded.getModel());
		assertEquals("2026-04-20T10:00:00Z", loaded.getGeneratedAt());
	}

	@Test
	public void load_nonExistentDirReturnsNull() throws Exception {
		File dir = tmp.newFolder("empty");
		assertNull(store.load(dir));
	}

	@Test
	public void save_stampsGeneratedAtWhenBlank() throws Exception {
		File dir = tmp.newFolder();
		AiSourceCompanion companion = new AiSourceCompanion("openai", "gpt-4o", null);
		store.save(dir, companion);
		AiSourceCompanion loaded = store.load(dir);
		assertNotNull(loaded.getGeneratedAt());
		assertFalse("generatedAt must be stamped", loaded.getGeneratedAt().isBlank());
	}

	@Test
	public void save_doesNotOverwriteExistingGeneratedAt() throws Exception {
		File dir = tmp.newFolder();
		AiSourceCompanion companion = new AiSourceCompanion("openai", "gpt-4o", "2025-01-01T00:00:00Z");
		store.save(dir, companion);
		AiSourceCompanion loaded = store.load(dir);
		assertEquals("2025-01-01T00:00:00Z", loaded.getGeneratedAt());
	}

	@Test
	public void fileFor_returnsCorrectFilename() throws Exception {
		File dir = tmp.newFolder();
		File f = store.fileFor(dir);
		assertNotNull(f);
		assertEquals(AiSourceCompanionFileStore.FILENAME, f.getName());
	}

	// ---------------------------------------------------------------- ignore unknown fields

	@Test
	public void load_ignoresUnknownFieldsInJson() throws Exception {
		File dir = tmp.newFolder();
		// Write a JSON with an extra field manually
		File file = new File(dir, AiSourceCompanionFileStore.FILENAME);
		java.nio.file.Files.writeString(file.toPath(),
				"{\"spi\":\"openai\",\"model\":\"gpt-4o\",\"generatedAt\":\"2026-01-01T00:00:00Z\",\"futureField\":\"ignored\"}");
		AiSourceCompanion loaded = store.load(dir);
		assertNotNull(loaded);
		assertEquals("openai", loaded.getSpi());
		assertEquals("gpt-4o", loaded.getModel());
	}
}
