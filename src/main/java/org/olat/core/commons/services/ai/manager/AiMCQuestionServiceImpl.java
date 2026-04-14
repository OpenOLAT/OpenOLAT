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

import java.util.Locale;

import org.olat.core.commons.services.ai.AiFeature;
import org.olat.core.commons.services.ai.AiMCQuestionService;
import org.olat.core.commons.services.ai.AiModule;
import org.olat.core.commons.services.ai.AiSPI;
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
	public AiMCQuestionsResponse generateMCQuestionsResponse(AiUsageContext usageContext, String input, int number) {
		return generateMCQuestionsResponse(usageContext, input, number, aiModule.getMCGeneratorSpiId(), aiModule.getMCGeneratorModel());
	}

	@Override
	public AiMCQuestionsResponse generateMCQuestionsResponse(AiUsageContext usageContext, String input, int number, String spiId, String modelName) {
		AiMCQuestionsResponse response = new AiMCQuestionsResponse();
		AiSPI spi = aiModule.resolveProvider(spiId);
		if (spi == null) {
			response.setError("AI provider is not configured or not available.");
			return response;
		}
		long startTime = System.currentTimeMillis();
		try {
			Locale locale = textService.detectLocale(input);
			if (locale == null) {
				response.setError("Could not detect language of the input text.");
				return response;
			}

			cachedAiService = CachedChatModel.getOrRefresh(cachedAiService, spi, spiId, modelName, MAX_TOKENS);
			ChatModel chatModel = cachedAiService.chatModel();

			String language = locale.getDisplayLanguage(Locale.ENGLISH);
			AiServices<MCQuestionAiService> builder = AiServices.builder(MCQuestionAiService.class);
			AiLoggingChatModel.configureBuilder(builder, chatModel, aiUsageLogDAO, spiId, AiFeature.MCQuestionGenerator.getType(), usageContext);
			MCQuestionAiService service = builder.build();

			service.generateQuestions(number, 2, 3, language, input)
					.forEach(response::addQuestion);

		} catch (Exception e) {
			Exception cause = e instanceof AiUsageLoggedException ? (Exception) e.getCause() : e;
			response.setError(cause.getMessage() != null ? cause.getMessage() : cause.getClass().getName());
			if (!(e instanceof AiUsageLoggedException)) {
				aiUsageLogDAO.createErrorLog(spiId, modelName, AiFeature.MCQuestionGenerator.getType(), usageContext,
						System.currentTimeMillis() - startTime, cause);
			}
		}
		return response;
	}

}
