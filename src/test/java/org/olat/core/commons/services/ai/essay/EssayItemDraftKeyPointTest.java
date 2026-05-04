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
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for {@link EssayItemDraft.KeyPoint#isRequiredEffective()} — the
 * null→true default applied to LLM output that omits the required field.
 *
 * Initial date: 2026-05-04<br>
 * @author Tom (AIT), https://www.frentix.com
 */
public class EssayItemDraftKeyPointTest {

	@Test
	public void isRequiredEffective_nullDefaultsToTrue() {
		EssayItemDraft.KeyPoint kp = new EssayItemDraft.KeyPoint("kp1", "text", 0.8, null);
		assertTrue("null required must default to true", kp.isRequiredEffective());
	}

	@Test
	public void isRequiredEffective_explicitTrue() {
		EssayItemDraft.KeyPoint kp = new EssayItemDraft.KeyPoint("kp2", "text", 0.8, Boolean.TRUE);
		assertTrue(kp.isRequiredEffective());
	}

	@Test
	public void isRequiredEffective_explicitFalse() {
		EssayItemDraft.KeyPoint kp = new EssayItemDraft.KeyPoint("kp3", "text", 0.2, Boolean.FALSE);
		assertFalse(kp.isRequiredEffective());
	}
}
