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
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

/**
 * Unit tests for {@link FormativeFeedback} factory shorthands:
 * {@code rejected}, {@code refusedLong}, {@code ok}.
 *
 * Initial date: 2026-05-04<br>
 * @author Tom (AIT), https://www.frentix.com
 */
public class FormativeFeedbackFactoryTest {

	// ---------------------------------------------------------------- rejected

	@Test
	public void rejected_typeIsRejected() {
		RejectionReason reason = new RejectionReason("ai.essay.error.empty", "empty");
		FormativeFeedback fb = FormativeFeedback.rejected(reason);
		assertEquals(FormativeFeedback.Type.REJECTED, fb.type());
	}

	@Test
	public void rejected_carriesRejectionReason() {
		RejectionReason reason = new RejectionReason("ai.essay.error.empty", "empty");
		FormativeFeedback fb = FormativeFeedback.rejected(reason);
		assertNotNull(fb.rejection());
		assertEquals("ai.essay.error.empty", fb.rejection().messageKey());
	}

	@Test
	public void rejected_tierIsNull() {
		FormativeFeedback fb = FormativeFeedback.rejected(new RejectionReason("k", "d"));
		assertNull(fb.tier());
	}

	@Test
	public void rejected_suggestionIsNull() {
		FormativeFeedback fb = FormativeFeedback.rejected(new RejectionReason("k", "d"));
		assertNull(fb.suggestion());
	}

	@Test
	public void rejected_warningsIsEmpty() {
		FormativeFeedback fb = FormativeFeedback.rejected(new RejectionReason("k", "d"));
		assertNotNull(fb.warnings());
		assertTrue(fb.warnings().isEmpty());
	}

	@Test
	public void rejected_usageLogKeyIsNull() {
		FormativeFeedback fb = FormativeFeedback.rejected(new RejectionReason("k", "d"));
		assertNull(fb.usageLogKey());
	}

	// ---------------------------------------------------------------- refusedLong

	@Test
	public void refusedLong_typeIsRefusedLong() {
		RejectionReason reason = new RejectionReason(LengthPreFilter.REASON_TOO_LONG, "too long");
		FormativeFeedback fb = FormativeFeedback.refusedLong(reason);
		assertEquals(FormativeFeedback.Type.REFUSED_LONG, fb.type());
	}

	@Test
	public void refusedLong_tierIsLong() {
		FormativeFeedback fb = FormativeFeedback.refusedLong(
				new RejectionReason(LengthPreFilter.REASON_TOO_LONG, "d"));
		assertEquals(AiGradingTier.LONG, fb.tier());
	}

	@Test
	public void refusedLong_suggestionIsNull() {
		FormativeFeedback fb = FormativeFeedback.refusedLong(
				new RejectionReason(LengthPreFilter.REASON_TOO_LONG, "d"));
		assertNull(fb.suggestion());
	}

	// ---------------------------------------------------------------- ok

	@Test
	public void ok_typeIsOk() {
		FormativeFeedback fb = FormativeFeedback.ok(AiGradingTier.SHORT, buildSuggestion(), List.of(), 99L);
		assertEquals(FormativeFeedback.Type.OK, fb.type());
	}

	@Test
	public void ok_tierIsPreserved() {
		FormativeFeedback fb = FormativeFeedback.ok(AiGradingTier.MEDIUM, buildSuggestion(), List.of(), 99L);
		assertEquals(AiGradingTier.MEDIUM, fb.tier());
	}

	@Test
	public void ok_suggestionIsPreserved() {
		GradingSuggestion s = buildSuggestion();
		FormativeFeedback fb = FormativeFeedback.ok(AiGradingTier.SHORT, s, List.of(), 42L);
		assertEquals(s, fb.suggestion());
	}

	@Test
	public void ok_usageLogKeyIsPreserved() {
		FormativeFeedback fb = FormativeFeedback.ok(AiGradingTier.SHORT, buildSuggestion(), List.of(), 1234L);
		assertEquals(Long.valueOf(1234L), fb.usageLogKey());
	}

	@Test
	public void ok_nullWarningListBecomesEmptyList() {
		FormativeFeedback fb = FormativeFeedback.ok(AiGradingTier.SHORT, buildSuggestion(), null, 1L);
		assertNotNull(fb.warnings());
		assertTrue(fb.warnings().isEmpty());
	}

	@Test
	public void ok_warningsAreCopied() {
		List<RejectionReason> warnings = List.of(new RejectionReason("k", "d"));
		FormativeFeedback fb = FormativeFeedback.ok(AiGradingTier.SHORT, buildSuggestion(), warnings, 1L);
		assertEquals(1, fb.warnings().size());
	}

	@Test
	public void ok_rejectionIsNull() {
		FormativeFeedback fb = FormativeFeedback.ok(AiGradingTier.SHORT, buildSuggestion(), List.of(), 1L);
		assertNull(fb.rejection());
	}

	// ---------------------------------------------------------------- helpers

	private GradingSuggestion buildSuggestion() {
		return new GradingSuggestion(null, null, GradingSuggestion.OffTopicFlag.NONE,
				GradingSuggestion.Confidence.HIGH, null, "", "Good answer", 85, List.of());
	}
}
