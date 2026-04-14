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
package org.olat.core.commons.services.ai.spi.openAI;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.ai.AiApiKeySPI;
import org.olat.core.commons.services.ai.AiSPI;
import org.olat.core.commons.services.ai.manager.LangChain4jHttpClientBuilder;
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

import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiModelCatalog;

/**
 *
 * AI service implementation based on OpenAI services
 *
 * Initial date: 22.05.2024<br>
 *
 * @author gnaegi@frentix.com, https://www.frentix.com
 *
 */
@Service
public class OpenAiSPI extends AbstractSpringModule implements AiSPI, AiApiKeySPI {
	private static final Logger log = Tracing.createLoggerFor(OpenAiSPI.class);
	private static final String SPI_NAME = "OpenAI";
	private static final String SPI_ID = "OpenAI";

	private static final String OPENAI_API_KEY = "openai.api.key";
	private static final String OPENAI_ENABLED = "openai.enabled";

	@Value("${ai.openai.api.key:}")
	private String apiKey;

	@Value("${ai.openai.enabled:false}")
	private boolean enabledDefault;

	private boolean enabled;

	@Autowired
	public OpenAiSPI(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		setBooleanPropertyDefault(OPENAI_ENABLED, enabledDefault);
		updateProperties();
	}

	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}

	private void updateProperties() {
		apiKey = getStringPropertyValue(OPENAI_API_KEY, apiKey);
		enabled = getBooleanPropertyValue(OPENAI_ENABLED);
	}

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
		setBooleanProperty(OPENAI_ENABLED, enabled, true);
	}

	@Override
	public Controller createAdminController(UserRequest ureq, WindowControl wControl) {
		return new GenericAiApiKeyAdminController(ureq, wControl, this);
	}

	@Override
	public ChatModel buildChatModel(String modelName, int maxTokens) {
		return OpenAiChatModel.builder()
				.httpClientBuilder(new LangChain4jHttpClientBuilder())
				.apiKey(apiKey)
				.modelName(modelName)
				.maxCompletionTokens(maxTokens)
				.supportedCapabilities(Set.of(Capability.RESPONSE_FORMAT_JSON_SCHEMA))
				.build();
	}

	@Override
	public List<String> getAvailableModels() {
		if (!StringHelper.containsNonWhitespace(apiKey)) {
			return List.of();
		}
		try {
			return verifyApiKey(apiKey);
		} catch (Exception e) {
			log.warn("Could not fetch available models from OpenAI API", e);
			return List.of();
		}
	}

	@Override
	public String getApiKey() {
		return apiKey;
	}

	@Override
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
		setStringProperty(OPENAI_API_KEY, apiKey, true);
	}

	@Override
	public List<String> verifyApiKey(String apiKey) throws Exception {
		return OpenAiModelCatalog.builder()
				.httpClientBuilder(new LangChain4jHttpClientBuilder())
				.apiKey(apiKey)
				.build()
				.listModels()
				.stream()
				.map(m -> m.name())
				.sorted()
				.collect(Collectors.toList());
	}

	@Override
	public String getAdminTitleI18nKey() {
		return "ai.openai.title";
	}

	@Override
	public String getAdminDescI18nKey() {
		return "ai.openai.desc";
	}

	@Override
	public String getAdminApiKeyI18nKey() {
		return "ai.openai.apikey";
	}
}
