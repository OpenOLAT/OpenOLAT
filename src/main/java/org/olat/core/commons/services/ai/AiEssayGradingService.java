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
package org.olat.core.commons.services.ai;

import java.util.Locale;

import org.olat.core.commons.services.ai.essay.AiGradingTier;
import org.olat.core.commons.services.ai.essay.EssayAiGrading;
import org.olat.core.commons.services.ai.essay.GradingSuggestion;
import org.olat.core.commons.services.ai.model.AiUsageContext;

/**
 * Spring service for AI-assisted essay grading. Mirrors the shape of
 * {@link AiMCQuestionService}: resolves the configured provider and model
 * internally via {@link AiModule}, builds a {@code ChatModel} via the SPI, and
 * drives a LangChain4j {@code AiServices} proxy that returns a structured
 * {@link GradingSuggestion}.
 *
 * Initial date: 2026-04-20<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
public interface AiEssayGradingService {

	boolean isEnabled();

	/**
	 * Result holder exposing both the typed suggestion and the usage-log key so
	 * the caller can attach essay-specific provenance columns ({@code
	 * essayAiGradingId}, {@code contentHashAtCall}, {@code promptTemplateVersion},
	 * {@code tier}, {@code assessmentItemSessionKey}).
	 */
	record GradingRun(GradingSuggestion suggestion, Long usageLogKey) { }

	GradingSuggestion grade(AiUsageContext usageContext, EssayAiGrading grading,
			String studentAnswer, Locale language, AiGradingTier tier);

	GradingRun gradeWithLog(AiUsageContext usageContext, EssayAiGrading grading,
			String studentAnswer, Locale language, AiGradingTier tier);

	GradingRun gradeWithLog(AiUsageContext usageContext, EssayAiGrading grading,
			String studentAnswer, Locale language, AiGradingTier tier,
			String spiId, String modelName);

	/**
	 * @return the prompt template version constant stamped on every usage-log row.
	 */
	String getPromptTemplateVersion();

	/**
	 * @return the SPI id currently configured for essay grading, or {@code null}
	 */
	String getConfiguredSpiId();

	/**
	 * @return the model name currently configured for essay grading, or {@code null}
	 */
	String getConfiguredModel();

}
