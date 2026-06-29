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
package org.olat.core.commons.services.ai.manager;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.services.ai.AiFeature;
import org.olat.core.commons.services.ai.AiMCQuestionService;
import org.olat.core.commons.services.ai.AiModule;
import org.olat.core.commons.services.ai.AiSPI;
import org.olat.core.commons.services.ai.essay.AiBloomLevel;
import org.olat.core.commons.services.ai.model.AiMCQuestionsResponse;
import org.olat.core.commons.services.ai.model.AiUsageContext;
import org.olat.core.commons.services.ai.service.MCQuestionAiService;
import org.olat.core.commons.services.text.TextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;


/**
 * Spring service implementation for multiple choice question generation via AI.
 *
 * Initial date: 31.03.2026<br>
 *
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Service
public class AiMCQuestionServiceImpl implements AiMCQuestionService {
	
	private static final int MAX_TOKENS = 4000;

	@Autowired
	private AiModule aiModule;
	@Autowired
	private TextService textService;
	@Autowired
	private AiUsageLogDAO aiUsageLogDAO;

	private volatile CachedChatModel cachedAiService;

	@Override
	public boolean isEnabled() {
		return aiModule.isMCQuestionGeneratorEnabled();
	}

	@Override
	public AiMCQuestionsResponse generateMCQuestionsResponse(AiUsageContext usageContext, String input, int number,
			List<AiBloomLevel> bloomLevels, Integer targetDifficulty, List<String> learningObjectives) {
		return generateMCQuestionsResponse(usageContext, input, number,
				bloomLevels, targetDifficulty, learningObjectives,
				aiModule.getMCGeneratorSpiId(), aiModule.getMCGeneratorModel());
	}

	@Override
	public AiMCQuestionsResponse generateMCQuestionsResponse(AiUsageContext usageContext, String input, int number, String spiId, String modelName) {
		return generateMCQuestionsResponse(usageContext, input, number, null, null, List.of(), spiId, modelName);
	}

	private AiMCQuestionsResponse generateMCQuestionsResponse(AiUsageContext usageContext, String input, int number,
			List<AiBloomLevel> bloomLevels, Integer targetDifficulty, List<String> learningObjectives,
			String spiId, String modelName) {
		AiMCQuestionsResponse response = new AiMCQuestionsResponse();
		AiSPI spi = aiModule.resolveProvider(spiId);
		if (spi == null) {
			response.setError("AI provider is not configured or not available.");
			return response;
		}
		long startTime = System.currentTimeMillis();
		AiLoggingChatModel loggingModel = null;
		try {
			Locale locale = textService.detectLocale(input);
			if (locale == null) {
				response.setError("Could not detect language of the input text.");
				return response;
			}

			cachedAiService = CachedChatModel.getOrRefresh(cachedAiService, spi, spiId, modelName, MAX_TOKENS);
			ChatModel chatModel = cachedAiService.chatModel();

			String language = locale.getDisplayLanguage(Locale.ENGLISH);
			String bloomLevelsStr = bloomLevels == null || bloomLevels.isEmpty()
					? "UNDERSTAND, APPLY"
					: bloomLevels.stream().map(Enum::name).reduce((a, b) -> a + ", " + b).orElse("UNDERSTAND");
			String targetDifficultyStr = targetDifficulty == null
					? "No target difficulty was selected: vary the difficulty across the generated questions, "
							+ "spread randomly across the full 1-5 range (1 = easiest, 5 = hardest), calibrating "
							+ "distractor plausibility and recall depth per question."
					: "Calibrate distractor plausibility and recall depth to a target difficulty of "
					  + targetDifficulty + " on a 1-5 scale (1 = easiest, 5 = hardest).";
			String objectives = learningObjectives == null || learningObjectives.isEmpty()
					? ""
					: "- " + String.join("\n- ", learningObjectives);

			AiServices<MCQuestionAiService> builder = AiServices.builder(MCQuestionAiService.class);
			loggingModel = AiLoggingChatModel.configureBuilder(builder, chatModel, aiUsageLogDAO, spiId, AiFeature.MCQuestionGenerator.getType(), usageContext);
			MCQuestionAiService service = builder.build();

			service.generateQuestions(number, 2, 3, language, bloomLevelsStr, targetDifficultyStr, objectives, input)
					.forEach(response::addQuestion);

		} catch (Exception e) {
			Exception cause = e instanceof AiUsageLoggedException ? (Exception) e.getCause() : e;
			response.setError(cause.getMessage() != null ? cause.getMessage() : cause.getClass().getName());
			Long existingLogKey = loggingModel == null ? null : loggingModel.getLogKey();
			if (e instanceof AiUsageLoggedException) {
				// AiLoggingChatModel already wrote a FAILED row.
			} else if (existingLogKey != null) {
				// Post-LLM parse / structured-output failure — flip existing
				// SUCCESS row to FAILED rather than writing a second one.
				aiUsageLogDAO.updateAsFailed(existingLogKey, cause);
			} else {
				aiUsageLogDAO.createErrorLog(spiId, modelName, AiFeature.MCQuestionGenerator.getType(), usageContext,
						System.currentTimeMillis() - startTime, cause);
			}
		}
		return response;
	}

}
