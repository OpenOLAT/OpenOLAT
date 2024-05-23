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
package org.olat.core.commons.services.ai.spi.openAI;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.ai.AiSPI;
import org.olat.core.commons.services.ai.model.AiMCQuestionsResponse;
import org.olat.core.commons.services.ai.ui.OpenAIAdminConfigFormController;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.Tracing;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.github.sashirestela.openai.SimpleOpenAI;
import io.github.sashirestela.openai.domain.chat.Chat;
import io.github.sashirestela.openai.domain.chat.ChatMessage.SystemMessage;
import io.github.sashirestela.openai.domain.chat.ChatMessage.UserMessage;
import io.github.sashirestela.openai.domain.chat.ChatRequest;

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
public class OpenAiSPI extends AbstractSpringModule implements AiSPI {
	private static final Logger log = Tracing.createLoggerFor(OpenAiSPI.class);
	private static final String SPI_NAME = "OpenAI";
	private static final String SPI_ID = "OpenAI";
		
    private static final String OPENAI_API_KEY = "openai.api.key";
    private static final String OPENAI_CHAT_MODEL = "openai.chat.model";
	
	@Value("${ai.openai.api.key}")
	private String apiKey;

	@Value("${ai.openai.chat.model}")
	private String chatModel;

	private SimpleOpenAI openAI;
	
	@Autowired
	OpenAiPromptHelper openAiPromptHelper;
	
	@Autowired
	public OpenAiSPI(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}	

	@Override
	public void init() {
		updateProperties();
	}

	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}

	private void updateProperties() {
		apiKey = getStringPropertyValue(OPENAI_API_KEY, apiKey);
		chatModel = getStringPropertyValue(OPENAI_CHAT_MODEL, chatModel);
		openAI = SimpleOpenAI.builder().apiKey(apiKey).build();
	}
	
	/**
	 * @return The OpenAI API Key
	 */
	public String getApiKey() {
		return apiKey;
	}

	/**
	 * @param apiKey The OpenAI API Key
	 */
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
		setStringProperty(OPENAI_API_KEY, apiKey, true);
		openAI = SimpleOpenAI.builder().apiKey(apiKey).build();			
	}

	/**
	 * @return The model used for the chat API
	 */
	public String getChatModel() {
		return chatModel;
	}

	/**
	 * @param chatModel The model used for the chat API
	 */
	public void setChatModel(String chatModel) {
		this.chatModel = chatModel;
		setStringProperty(OPENAI_CHAT_MODEL, chatModel, true);
	}

	
	/**********************
	 * SPI related methods
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
	public Controller createAdminController(UserRequest ureq, WindowControl wControl) {
		return new OpenAIAdminConfigFormController(ureq, wControl);
	}

	@Override
	public String getQuestionGenerationModel() {
		return getChatModel();
	}

	@Override
	public boolean isQuestionGenerationEnabled() {
		// check if configured
		return true;
	}
	
	@Override
	public AiMCQuestionsResponse generateMCQuestionsResponse(String input, int number) {
		AiMCQuestionsResponse response = new AiMCQuestionsResponse();
		try {			
			Locale locale = openAiPromptHelper.detectSupportedLocale(input);
			if (locale == null) {
				
				response.setError("Could not detect language. Only DE and EN supported.");
				return response;
			}			
			
			//TODO: check input length
			//TODO: split into multiple queries
			//TODO: use embeddings etc
			
			SystemMessage systemMessage = openAiPromptHelper.createQuestionSystemMessage(locale);
			UserMessage userMessage = openAiPromptHelper.createChoiceQuestionUserMessage(input, number, 2, 3, locale);
			ChatRequest chatRequest = ChatRequest.builder().model(getChatModel())
					.message(systemMessage)
					.message(userMessage)
					.temperature(0.2)
					.frequencyPenalty(0.2)
					.presencePenalty(0.2)
					.maxTokens(4000)
					.build();
			CompletableFuture<Chat> futureChat = openAI.chatCompletions().create(chatRequest);
			Chat chatResponse = futureChat.join();
			String result = chatResponse.firstContent();
	
			if (log.isDebugEnabled()) {
				log.debug("OpenAI chat response for MC question:: " + result);
			}
			
			response = openAiPromptHelper.parseQuestionResult(result);
			
		} catch (Exception e) {
			log.warn("Error while creating an MC question via AI service", e);
			response.setError(parseErrorString(e.getMessage()));
		}
		return response;
	}

	
	private String parseErrorString(String errormessage) {
		// parse for fancy CleverClientException that hide the real openAI errors: 
		if (errormessage.contains("\"message\": \"")) {
			errormessage = errormessage.substring(errormessage.indexOf("\"message\": \"") + 12);
			errormessage = errormessage.substring(0, errormessage.indexOf("\""));
		}
		return errormessage;
	}
	
	
	
	
}
