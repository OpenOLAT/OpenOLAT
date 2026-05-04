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

import java.util.Optional;

import org.junit.Test;

/**
 * Unit tests for {@link GibberishPreFilter}.
 *
 * Initial date: 2026-05-04<br>
 * @author Tom (AIT), https://www.frentix.com
 */
public class GibberishPreFilterTest {

	// ---------------------------------------------------------------- null / blank

	@Test
	public void check_nullReturnsEmpty() {
		assertFalse(GibberishPreFilter.check(null).isPresent());
	}

	@Test
	public void check_blankReturnsEmpty() {
		assertFalse(GibberishPreFilter.check("   ").isPresent());
	}

	// ---------------------------------------------------------------- short text skips entropy check

	@Test
	public void check_tooShortTextPassesRegardless() {
		// < MIN_LENGTH_FOR_ENTROPY_CHECK (15 non-whitespace chars) — must pass
		assertFalse(GibberishPreFilter.check("aaa bbb ccc").isPresent());
	}

	// ---------------------------------------------------------------- natural language passes

	@Test
	public void check_naturalEnglishProsePasses() {
		String prose = "The mitochondria is the powerhouse of the cell. "
				+ "It generates most of the cell's supply of adenosine triphosphate (ATP), "
				+ "used as a source of chemical energy.";
		assertFalse("Natural English prose must pass gibberish filter",
				GibberishPreFilter.check(prose).isPresent());
	}

	@Test
	public void check_naturalGermanProsePasses() {
		String prose = "Der Klimawandel ist eine der grössten Herausforderungen unserer Zeit. "
				+ "Wissenschaftler fordern sofortige Massnahmen zur Reduktion von Treibhausgasen.";
		assertFalse("Natural German prose must pass gibberish filter",
				GibberishPreFilter.check(prose).isPresent());
	}

	// ---------------------------------------------------------------- keyboard mash rejected

	@Test
	public void check_keyboardMashIsRejected() {
		// aaaa repeated — very low trigram entropy
		String mash = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
		Optional<RejectionReason> result = GibberishPreFilter.check(mash);
		assertTrue("Keyboard mash must be rejected", result.isPresent());
	}

	// ---------------------------------------------------------------- symbol spam (low letter ratio)

	@Test
	public void check_symbolSpamIsRejected() {
		// Long string of numbers and punctuation — letter ratio below threshold
		String symbols = "1234567890!@#$%^&*()1234567890!@#$%^&*()";
		Optional<RejectionReason> result = GibberishPreFilter.check(symbols);
		assertTrue("Symbol spam must be rejected by letter-ratio check", result.isPresent());
	}
}
