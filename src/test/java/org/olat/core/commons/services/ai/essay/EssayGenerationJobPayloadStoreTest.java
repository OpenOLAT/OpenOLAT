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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.After;
import org.junit.Test;

/**
 * Unit tests for {@link EssayGenerationJobPayloadStore} — in-memory
 * store/take/has, isolation, double-take, null-safety.
 *
 * Initial date: 2026-05-04<br>
 * @author Tom (AIT), https://www.frentix.com
 */
public class EssayGenerationJobPayloadStoreTest {

	private final EssayGenerationJobPayloadStore store = new EssayGenerationJobPayloadStore();

	@After
	public void tearDown() {
		store.clear();
	}

	// ---------------------------------------------------------------- basic operations

	@Test
	public void store_thenHas_returnsTrue() {
		store.store(1L, buildRequest());
		assertTrue(store.has(1L));
	}

	@Test
	public void has_beforeStore_returnsFalse() {
		assertFalse(store.has(99L));
	}

	@Test
	public void take_returnsPayload() {
		EssayGenerationService.GenerationRequest req = buildRequest();
		store.store(2L, req);
		EssayGenerationService.GenerationRequest taken = store.take(2L);
		assertNotNull(taken);
	}

	@Test
	public void take_removesEntry() {
		store.store(3L, buildRequest());
		store.take(3L);
		assertFalse("Entry must be removed after take()", store.has(3L));
	}

	@Test
	public void take_secondCallReturnsNull() {
		store.store(4L, buildRequest());
		store.take(4L);
		assertNull("Second take() must return null", store.take(4L));
	}

	// ---------------------------------------------------------------- null-safety

	@Test
	public void store_nullKeyIsNoOp() {
		// Must not throw; subsequent has(null) must return false
		store.store(null, buildRequest());
		assertFalse(store.has(null));
	}

	@Test
	public void store_nullRequestIsNoOp() {
		store.store(5L, null);
		assertFalse("Null request must not be stored", store.has(5L));
	}

	@Test
	public void take_nullKeyReturnsNull() {
		assertNull(store.take(null));
	}

	@Test
	public void has_nullKeyReturnsFalse() {
		assertFalse(store.has(null));
	}

	// ---------------------------------------------------------------- isolation

	@Test
	public void storeMultipleKeys_isolatedFromEachOther() {
		store.store(10L, buildRequest());
		store.store(11L, buildRequest());
		assertTrue(store.has(10L));
		assertTrue(store.has(11L));

		store.take(10L);
		assertFalse("Key 10 must be removed", store.has(10L));
		assertTrue("Key 11 must be untouched", store.has(11L));
	}

	// ---------------------------------------------------------------- helper

	private EssayGenerationService.GenerationRequest buildRequest() {
		return EssayGenerationService.GenerationRequest.forPool(
				"Some markdown content", null, Locale.ENGLISH, null, 2, 3, null);
	}
}
