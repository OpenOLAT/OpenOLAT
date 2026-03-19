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
package org.olat.core.commons.services.ai.spi.generic;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.ai.AiApiKeySPI;
import org.olat.core.commons.services.ai.AiImageDescriptionSPI;
import org.olat.core.commons.services.ai.AiMCQuestionGeneratorSPI;
import org.olat.core.commons.services.ai.AiPromptHelper;
import org.olat.core.commons.services.ai.AiSPI;
import org.olat.core.commons.services.ai.model.AiImageDescriptionResponse;
import org.olat.core.commons.services.ai.model.AiMCQuestionsResponse;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiModelCatalog;

/**
 * A single instance of a generic OpenAI-compatible AI provider. Multiple
 * instances can coexist, each configured with its own base URL, API key,
 * and model list. Uses the OpenAI-compatible LangChain4j client which
 * works with vLLM, Ollama, LiteLLM, NeuralMagic, and similar servers.
 *
 * Initial date: 09.03.2026<br>
 *
 * @author gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class GenericAiSpiInstance implements AiSPI, AiApiKeySPI, AiMCQuestionGeneratorSPI, AiImageDescriptionSPI {
	private static final Logger log = Tracing.createLoggerFor(GenericAiSpiInstance.class);

	private final int instanceId;
	private final GenericAiSPI parent;

	private String name;
	private String baseUrl;
	private String apiKey;
	private String models;
	private boolean enabled;

	private String mcGeneratorModel;
	private ChatModel chatModel;

	private String imageDescriptionModelName;
	private ChatModel imageDescChatModel;

	GenericAiSpiInstance(int instanceId, GenericAiSPI parent) {
		this.instanceId = instanceId;
		this.parent = parent;
	}

	void load(String name, String baseUrl, String apiKey, String models, boolean enabled) {
		this.name = name;
		this.baseUrl = baseUrl;
		this.apiKey = apiKey;
		this.models = models;
		this.enabled = enabled;
	}

	// ------ AiSPI ------

	@Override
	public String getId() {
		return "Generic_" + instanceId;
	}

	@Override
	public String getName() {
		return StringHelper.containsNonWhitespace(name) ? name : "Generic #" + instanceId;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		parent.setInstanceProperty(instanceId, "enabled", String.valueOf(enabled));
	}

	@Override
	public Controller createAdminController(UserRequest ureq, WindowControl wControl) {
		return new GenericAiSpiAdminController(ureq, wControl, this);
	}

	// ------ AiApiKeySPI ------

	@Override
	public String getApiKey() {
		return apiKey;
	}

	@Override
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
		parent.setInstanceProperty(instanceId, "api.key", apiKey);
		rebuildChatModel();
		rebuildImageDescChatModel();
	}

	@Override
	public List<String> verifyApiKey(String testApiKey) throws Exception {
		if (!StringHelper.containsNonWhitespace(baseUrl)) {
			throw new IllegalStateException("Base URL is not configured");
		}
		// Query the /v1/models endpoint of the OpenAI-compatible server
		return fetchModelsFromServer(testApiKey);
	}

	@Override
	public String getAdminTitleI18nKey() {
		return "ai.generic.title";
	}

	@Override
	public String getAdminDescI18nKey() {
		return "ai.generic.desc";
	}

	@Override
	public String getAdminApiKeyI18nKey() {
		return "ai.generic.apikey";
	}

	// ------ AiMCQuestionGeneratorSPI ------

	@Override
	public void setMCGeneratorModel(String model) {
		if (!Objects.equals(this.mcGeneratorModel, model)) {
			this.mcGeneratorModel = model;
			rebuildChatModel();
		}
	}

	@Override
	public String getMCGeneratorModel() {
		return mcGeneratorModel;
	}

	@Override
	public List<String> getAvailableMCGeneratorModels() {
		// Try to fetch models from server first, fall back to configured list
		if (StringHelper.containsNonWhitespace(baseUrl)) {
			try {
				List<String> serverModels = fetchModelsFromServer(apiKey);
				if (!serverModels.isEmpty()) {
					return serverModels;
				}
			} catch (Exception e) {
				log.debug("Could not fetch models from generic server [{}], using configured list", getName(), e);
			}
		}
		return getModelList();
	}

	@Override
	public AiMCQuestionsResponse generateMCQuestionsResponse(String input, int number) {
		AiMCQuestionsResponse response = new AiMCQuestionsResponse();
		if (chatModel == null) {
			response.setError("AI provider is not properly configured.");
			return response;
		}
		try {
			AiPromptHelper promptHelper = parent.getAiPromptHelper();
			Locale locale = promptHelper.detectSupportedLocale(input);
			if (locale == null) {
				response.setError("Could not detect language. Only DE and EN supported.");
				return response;
			}

			SystemMessage systemMessage = promptHelper.createQuestionSystemMessage(locale);
			UserMessage userMessage = promptHelper.createChoiceQuestionUserMessage(input, number, 2, 3, locale);

			ChatResponse chatResponse = chatModel.chat(systemMessage, userMessage);
			String result = chatResponse.aiMessage().text();

			if (log.isDebugEnabled()) {
				log.debug("Generic AI [{}] response for MC question:: {}", getName(), result);
			}

			response = promptHelper.parseQuestionResult(result);

		} catch (Exception e) {
			log.warn("Error while creating an MC question via generic AI service [{}]", getName(), e);
			response.setError(e.getMessage());
		}
		return response;
	}

	// ------ AiImageDescriptionSPI ------

	@Override
	public void setImageDescriptionModel(String model) {
		if (!Objects.equals(this.imageDescriptionModelName, model)) {
			this.imageDescriptionModelName = model;
			rebuildImageDescChatModel();
		}
	}

	@Override
	public String getImageDescriptionModel() {
		return imageDescriptionModelName;
	}

	@Override
	public List<String> getAvailableImageDescriptionModels() {
		return getAvailableMCGeneratorModels();
	}

	@Override
	public AiImageDescriptionResponse generateImageDescription(String imageBase64, String mimeType, Locale locale) {
		AiImageDescriptionResponse response = new AiImageDescriptionResponse();
		if (imageDescChatModel == null) {
			response.setError("AI provider is not properly configured.");
			return response;
		}
		try {
			AiPromptHelper promptHelper = parent.getAiPromptHelper();
			SystemMessage systemMessage = promptHelper.createImageDescriptionSystemMessage(locale);
			UserMessage userMessage = promptHelper.createImageDescriptionUserMessage(imageBase64, mimeType, locale);

			ChatResponse chatResponse = imageDescChatModel.chat(systemMessage, userMessage);
			String result = chatResponse.aiMessage().text();

			if (log.isDebugEnabled()) {
				log.debug("Generic AI [{}] response for image description:: {}", getName(), result);
			}

			response = promptHelper.parseImageDescriptionResult(result);

		} catch (Exception e) {
			log.warn("Error while creating an image description via generic AI service [{}]", getName(), e);
			response.setError(e.getMessage());
		}
		return response;
	}

	// ------ Instance-specific getters / setters ------

	public int getInstanceId() {
		return instanceId;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
		parent.setInstanceProperty(instanceId, "base.url", baseUrl);
		rebuildChatModel();
		rebuildImageDescChatModel();
	}

	public String getModels() {
		return models;
	}

	public void setModels(String models) {
		this.models = models;
		parent.setInstanceProperty(instanceId, "models", models);
	}

	public void setName(String name) {
		this.name = name;
		parent.setInstanceProperty(instanceId, "name", name);
	}

	/**
	 * Delete this instance by removing all its properties from the parent.
	 */
	public void delete() {
		parent.deleteInstance(instanceId);
	}

	// ------ Internal ------

	/**
	 * Query the /v1/models endpoint of the OpenAI-compatible server.
	 */
	private List<String> fetchModelsFromServer(String key) throws Exception {
		var builder = OpenAiModelCatalog.builder().baseUrl(baseUrl);
		if (StringHelper.containsNonWhitespace(key)) {
			builder.apiKey(key);
		} else {
			builder.apiKey("no-key");
		}
		return builder.build()
				.listModels()
				.stream()
				.map(m -> m.name())
				.sorted()
				.collect(Collectors.toList());
	}

	List<String> getModelList() {
		if (!StringHelper.containsNonWhitespace(models)) {
			return List.of();
		}
		return Arrays.stream(models.split(","))
				.map(String::trim)
				.filter(StringHelper::containsNonWhitespace)
				.toList();
	}

	private String getFirstModel() {
		List<String> list = getModelList();
		return list.isEmpty() ? null : list.get(0);
	}

	private void rebuildChatModel() {
		String modelName = StringHelper.containsNonWhitespace(mcGeneratorModel) ? mcGeneratorModel : getFirstModel();
		chatModel = buildChatModel(apiKey, modelName);
	}

	private void rebuildImageDescChatModel() {
		String modelName = StringHelper.containsNonWhitespace(imageDescriptionModelName) ? imageDescriptionModelName : getFirstModel();
		imageDescChatModel = buildChatModel(apiKey, modelName);
	}

	private ChatModel buildChatModel(String key, String modelName) {
		if (!StringHelper.containsNonWhitespace(baseUrl) || !StringHelper.containsNonWhitespace(modelName)) {
			return null;
		}
		var builder = OpenAiChatModel.builder()
				.baseUrl(baseUrl)
				.modelName(modelName)
				.maxCompletionTokens(4000);
		if (StringHelper.containsNonWhitespace(key)) {
			builder.apiKey(key);
		} else {
			builder.apiKey("no-key");
		}
		return builder.build();
	}
}
