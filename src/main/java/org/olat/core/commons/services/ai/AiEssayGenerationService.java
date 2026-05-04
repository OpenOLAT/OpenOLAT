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

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.services.ai.essay.AiBloomLevel;
import org.olat.core.commons.services.ai.essay.AiContentChunk;
import org.olat.core.commons.services.ai.essay.EssayItemDraft;
import org.olat.core.commons.services.ai.model.AiUsageContext;

/**
 * Spring service for essay question generation via AI. Mirrors the shape of
 * {@link AiMCQuestionService}: resolves the configured provider and model
 * internally via {@link AiModule}, builds a {@code ChatModel} via the SPI, and
 * drives a LangChain4j {@code AiServices} proxy that returns structured
 * {@link EssayItemDraft}s.
 *
 * Initial date: 2026-04-20<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
public interface AiEssayGenerationService {

	boolean isEnabled();

	List<EssayItemDraft> generateEssayQuestions(AiUsageContext usageContext,
			List<AiContentChunk> chunks,
			List<String> learningObjectives,
			List<AiBloomLevel> targetBloomLevels,
			int numberOfQuestions,
			Locale language);

	List<EssayItemDraft> generateEssayQuestions(AiUsageContext usageContext,
			List<AiContentChunk> chunks,
			List<String> learningObjectives,
			List<AiBloomLevel> targetBloomLevels,
			int numberOfQuestions,
			Locale language,
			String spiId,
			String modelName);

	/**
	 * @return the SPI id currently configured for essay generation, or {@code null}
	 */
	String getConfiguredSpiId();

	/**
	 * @return the model name currently configured for essay generation, or {@code null}
	 */
	String getConfiguredModel();

}
