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
package org.olat.core.commons.services.ai.spi.anthropic;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.ai.AiApiKeySPI;
import org.olat.core.commons.services.ai.AiMCQuestionGeneratorSPI;
import org.olat.core.commons.services.ai.AiPromptHelper;
import org.olat.core.commons.services.ai.AiSPI;
import org.olat.core.commons.services.ai.model.AiMCQuestionsResponse;
import org.olat.core.commons.services.ai.ui.GenericAiApiKeyAdminController;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.anthropic.AnthropicModelCatalog;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;

/**
 *
 * AI service implementation based on Anthropic Claude services
 *
 * Initial date: 19.02.2026<br>
 *
 * @author gnaegi@frentix.com, https://www.frentix.com
 *
 */
@Service
public class AnthropicAiSPI extends AbstractSpringModule implements AiSPI, AiApiKeySPI, AiMCQuestionGeneratorSPI {
	private static final Logger log = Tracing.createLoggerFor(AnthropicAiSPI.class);
	private static final String SPI_NAME = "Anthropic Claude";
	private static final String SPI_ID = "Anthropic";

	private static final String ANTHROPIC_API_KEY = "anthropic.api.key";
	private static final String ANTHROPIC_ENABLED = "anthropic.enabled";

	@Value("${ai.anthropic.api.key:}")
	private String apiKey;

	@Value("${ai.anthropic.enabled:false}")
	private boolean enabledDefault;

	private boolean enabled;

	// Model name is managed by AiModule per feature, not stored here
	private String mcGeneratorModel;

	private ChatModel model;

	@Autowired
	AiPromptHelper aiPromptHelper;

	@Autowired
	public AnthropicAiSPI(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		setBooleanPropertyDefault(ANTHROPIC_ENABLED, enabledDefault);
		updateProperties();
	}

	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}

	private void updateProperties() {
		apiKey = getStringPropertyValue(ANTHROPIC_API_KEY, apiKey);
		enabled = getBooleanPropertyValue(ANTHROPIC_ENABLED);
		rebuildModel();
	}

	private void rebuildModel() {
		if (StringHelper.containsNonWhitespace(apiKey) && StringHelper.containsNonWhitespace(mcGeneratorModel)) {
			model = AnthropicChatModel.builder()
					.apiKey(apiKey)
					.modelName(mcGeneratorModel)
					.temperature(0.2)
					.maxTokens(4000)
					.build();
		} else {
			model = null;
		}
	}

	/**
	 * @return The Anthropic API Key
	 */
	public String getApiKey() {
		return apiKey;
	}

	/**
	 * @param apiKey The Anthropic API Key
	 */
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
		setStringProperty(ANTHROPIC_API_KEY, apiKey, true);
		rebuildModel();
	}


	/**********************
	 * AiSPI methods
	 **********************/

	@Override
	public String getId() {
		return SPI_ID;
	}

	@Override
	public String getName() {
		return SPI_NAME;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		setBooleanProperty(ANTHROPIC_ENABLED, enabled, true);
	}

	@Override
	public String getAdminTitleI18nKey() {
		return "ai.anthropic.title";
	}

	@Override
	public String getAdminDescI18nKey() {
		return "ai.anthropic.desc";
	}

	@Override
	public String getAdminApiKeyI18nKey() {
		return "ai.anthropic.apikey";
	}

	@Override
	public Controller createAdminController(UserRequest ureq, WindowControl wControl) {
		return new GenericAiApiKeyAdminController(ureq, wControl, this);
	}


	/**********************
	 * AiMCQuestionGeneratorSPI methods
	 **********************/

	@Override
	public void setMCGeneratorModel(String model) {
		if (!Objects.equals(this.mcGeneratorModel, model)) {
			this.mcGeneratorModel = model;
			rebuildModel();
		}
	}

	@Override
	public String getMCGeneratorModel() {
		return mcGeneratorModel;
	}

	/**
	 * Verify that the given API key is accepted by the Anthropic API.
	 * On success returns the list of available model names.
	 * On failure throws an exception whose message contains the provider's error details.
	 *
	 * @param apiKey The API key to verify
	 * @return List of available model names
	 * @throws Exception if the API key is rejected or the API is unreachable
	 */
	public List<String> verifyApiKey(String apiKey) throws Exception {
		return AnthropicModelCatalog.builder()
				.apiKey(apiKey)
				.build()
				.listModels()
				.stream()
				.map(m -> m.name())
				.sorted()
				.collect(Collectors.toList());
	}

	@Override
	public List<String> getAvailableMCGeneratorModels() {
		if (!StringHelper.containsNonWhitespace(apiKey)) {
			return List.of();
		}
		try {
			return verifyApiKey(apiKey);
		} catch (Exception e) {
			log.warn("Could not fetch available models from Anthropic API", e);
			return List.of();
		}
	}

	@Override
	public AiMCQuestionsResponse generateMCQuestionsResponse(String input, int number) {
		AiMCQuestionsResponse response = new AiMCQuestionsResponse();
		try {
			Locale locale = aiPromptHelper.detectSupportedLocale(input);
			if (locale == null) {
				response.setError("Could not detect language. Only DE and EN supported.");
				return response;
			}

			//TODO: check input length
			//TODO: split into multiple queries

			SystemMessage systemMessage = aiPromptHelper.createQuestionSystemMessage(locale);
			UserMessage userMessage = aiPromptHelper.createChoiceQuestionUserMessage(input, number, 2, 3, locale);

			ChatResponse chatResponse = model.chat(systemMessage, userMessage);
			String result = chatResponse.aiMessage().text();

			if (log.isDebugEnabled()) {
				log.debug("Anthropic messages response for MC question:: " + result);
			}

			response = aiPromptHelper.parseQuestionResult(result);

		} catch (Exception e) {
			log.warn("Error while creating an MC question via Anthropic AI service", e);
			response.setError(e.getMessage());
		}
		return response;
	}

}
