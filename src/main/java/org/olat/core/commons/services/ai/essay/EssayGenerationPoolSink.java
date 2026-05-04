/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
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

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.core.commons.services.ai.model.MCQuestionData;
import org.olat.core.id.Identity;

/**
 * Callback sink that lets the generic question-generation pipeline deliver
 * accepted drafts into the question pool without the
 * {@code core.commons.services.ai.essay} package taking a hard
 * compile-time dependency on the {@code modules.qpool} module.
 * <p>
 * Implemented by the question pool module as a Spring {@code @Service}
 * and looked up via {@code CoreSpringFactory} from
 * {@link EssayGenerationService#runJob(Long)} when a
 * {@link EssayGenerationService.GenerationRequest} carries
 * {@code destination == POOL}.
 * <p>
 * For each accepted essay draft the implementation persists a QTI essay
 * item plus an {@code ai-grading.json} file (so the runtime AI grading
 * pipeline can pick up the draft's rubric, key points and reference
 * excerpt at submission time).
 *
 * Initial date: 2026-04-20<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
public interface EssayGenerationPoolSink {

	/**
	 * Persist the given accepted essay drafts and AI-generated MC questions
	 * as fresh entries in the question pool, owned by {@code owner}.
	 *
	 * @param owner          the identity that owns the new question items
	 *                       (= the user who triggered the AI generation)
	 * @param essayDrafts    accepted essay drafts (may be {@code null} or empty)
	 * @param draftToGrading per-draft AI grading POJO to persist on disk
	 *                       next to the QTI item XML; may be {@code null}
	 *                       or empty
	 * @param mcQuestions    accepted MC questions; may be {@code null} or empty
	 * @param locale         locale used for default labels and pool import
	 * @param taxonomyLevelKey optional taxonomy level key — when non-null,
	 *                         the new items are stamped with this taxonomy
	 *                         level (typically the one the user was browsing)
	 * @return the keys of the newly created question items, in creation order;
	 *         never {@code null}, may be empty when no payload survived
	 */
	List<Long> persistGeneratedItems(Identity owner,
			List<EssayItemDraft> essayDrafts,
			Map<EssayItemDraft, EssayAiGrading> draftToGrading,
			List<MCQuestionData> mcQuestions,
			Locale locale,
			Long taxonomyLevelKey);
}
