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

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.core.commons.services.ai.model.MCQuestionData;

/**
 *
 * Callback sink that lets the generic question-generation pipeline deliver
 * accepted drafts to a concrete ceditor QuizPart without the
 * {@code core.commons.services.ai.essay} package taking a hard
 * compile-time dependency on the ceditor module.
 * <p>
 * Implemented by the ceditor module as a Spring {@code @Service} and looked
 * up via {@code CoreSpringFactory} from
 * {@link EssayGenerationService#runJob(Long)} when a
 * {@link EssayGenerationService.GenerationRequest} carries both a
 * {@code pageKey} and a {@code quizPartKey}.
 * <p>
 * Despite the legacy "essay" in the type/table/bean names, the sink
 * attaches a mixed QTI item set (multiple-choice + essay) because the
 * Markdown-import generation flow now produces both kinds. The underlying
 * {@code o_essay_generation_job} row is kept named as-is for schema
 * stability — the job is effectively a "generate questions from content"
 * job, not essay-only.
 *
 * Initial date: 2026-04-20<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
public interface EssayGenerationQuizPartSink {

	/**
	 * Attach the given accepted essay drafts and AI-generated MC questions
	 * as QTI items to the QuizPart identified by {@code quizPartKey}.
	 * <p>
	 * For each accepted essay draft the sink writes the corresponding
	 * {@link EssayAiGrading} POJO from {@code draftToGrading} as
	 * {@code ai-grading.json} into the question's directory next to the QTI
	 * item XML. Drafts not present in the map are persisted as plain QTI
	 * essay items without an AI grading file.
	 * <p>
	 * Implementation interleaves question types so the learner does not see
	 * a long block of the same interaction kind (e.g. MC-essay-MC-essay).
	 * On success it removes the "generating" placeholder marker. Silent
	 * no-op if the QuizPart is gone. Errors are logged by the
	 * implementation; this method must not throw into the caller (the
	 * caller is a background job hook).
	 *
	 * @param pageKey         the ceditor page key, never {@code null}
	 * @param quizPartKey     the target QuizPart key, never {@code null}
	 * @param essayDrafts     accepted essay drafts (may be {@code null} or empty)
	 * @param draftToGrading  per-draft AI grading POJO to persist on disk;
	 *                        may be {@code null} or empty
	 * @param mcQuestions     accepted MC questions from
	 *                        {@link org.olat.core.commons.services.ai.AiMCQuestionService}
	 *                        (may be {@code null} or empty)
	 * @param locale          locale used for default labels on generated items
	 */
	void attachDraftsAsEssayItems(Long pageKey, Long quizPartKey,
			List<EssayItemDraft> essayDrafts, Map<EssayItemDraft, EssayAiGrading> draftToGrading,
			List<MCQuestionData> mcQuestions, Locale locale);

	/**
	 * Mark the placeholder QuizPart as failed (e.g. render an error state).
	 * Called when the generation job itself fails after the placeholder
	 * has been created. Must not throw.
	 */
	void markGenerationFailed(Long pageKey, Long quizPartKey, String reason);
}
