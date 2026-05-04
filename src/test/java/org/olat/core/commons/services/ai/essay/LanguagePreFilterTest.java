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
 * software distributed under the LICENSE is distributed on an "AS IS" BASIS, <br>
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

import org.junit.Test;

/**
 * Unit tests for {@link LanguagePreFilter} — null/blank short-circuits only.
 * The positive language-detection path requires a running {@code TextService}
 * (Spring + Nutch), which is an integration concern; those tests are deferred.
 *
 * Initial date: 2026-05-04<br>
 * @author Tom (AIT), https://www.frentix.com
 */
public class LanguagePreFilterTest {

	@Test
	public void check_nullAnswerReturnsEmpty() {
		assertFalse(LanguagePreFilter.check(null, "en").isPresent());
	}

	@Test
	public void check_blankAnswerReturnsEmpty() {
		assertFalse(LanguagePreFilter.check("   ", "en").isPresent());
	}

	@Test
	public void check_nullExpectedLanguageReturnsEmpty() {
		assertFalse(LanguagePreFilter.check("Some answer text", null).isPresent());
	}

	@Test
	public void check_blankExpectedLanguageReturnsEmpty() {
		assertFalse(LanguagePreFilter.check("Some answer text", "  ").isPresent());
	}

	@Test
	public void check_bothNullReturnsEmpty() {
		assertFalse(LanguagePreFilter.check(null, null).isPresent());
	}
}
