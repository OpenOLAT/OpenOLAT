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
package org.olat.commons.services.ai.spi.openAI;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
public class OpenAIProvider extends AbstractSpringModule implements ConfigOnOff {
	//private static final Logger log = Tracing.createLoggerFor(OpenAIProvider.class);
		
	private static final String OPENAI_ENABLED = "openai.enabled";
    private static final String OPENAI_API_KEY = "openai.api.key";
    private static final String OPENAI_CHAT_MODEL = "openai.chat.model";
	
	@Value("${ai.openai.enabled:false}")
	private boolean enabled;
	
	@Value("${ai.openai.api.key}")
	private String apiKey;

	@Value("${ai.openai.chat.model}")
	private String chatModel;
	
	@Autowired
	public OpenAIProvider(CoordinatorManager coordinatorManager) {
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
		enabled = getBooleanPropertyValue(OPENAI_ENABLED);
		apiKey = getStringPropertyValue(OPENAI_API_KEY, apiKey);
		chatModel = getStringPropertyValue(OPENAI_CHAT_MODEL, chatModel);
	}
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		setStringProperty(OPENAI_ENABLED, Boolean.toString(enabled), true);
	}
		
	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
		setStringProperty(OPENAI_API_KEY, apiKey, true);
	}
	
	public String getChatModel() {
		return chatModel;
	}

	public void setChatModel(String chatModel) {
		this.chatModel = chatModel;
		setStringProperty(OPENAI_CHAT_MODEL, chatModel, true);
	}

}
