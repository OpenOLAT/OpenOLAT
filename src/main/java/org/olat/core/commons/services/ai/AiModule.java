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
package org.olat.core.commons.services.ai;

import java.util.List;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * The AI module provides a service to use various (generative) AI services
 * within OpenOlat. There might be different implementations available. To
 * provide your own, implement the AiSPI interface. 
 * 
 * Initial date: 22.05.2024<br>
 * 
 * @author Florian Gnägi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
@Service
public class AiModule extends AbstractSpringModule {
	// module config key to remember the active SPI
	private static final String AI_PROVIDER = "ai.provider";
	private static final String AI_PROVIDER_NONE = "NONE";
	// list of all availableSPI
	private List<AiSPI> aiProviders;
	// the currently configured SPI or NULL if disabled
	private AiSPI aiProvider = null;

	@Value("${ai.provider:NONE}")
	private String aiProviderDefault;	
	
	/**
	 * Spring constructor
	 * 
	 * @param coordinatorManager
	 */
	public AiModule(CoordinatorManager coordinatorManager) {
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

	/**
	 * Internal helper to read the config from the module and init the module settings
	 */
	private void updateProperties() {
		String enabledSpiID = getStringPropertyValue(AI_PROVIDER, aiProviderDefault);
		if (StringHelper.containsNonWhitespace(enabledSpiID)) {
			for (AiSPI aiSPI : aiProviders) {
				if (enabledSpiID.equals(aiSPI.getId())) {
					aiProvider = aiSPI;
					return;
				}
			}
		}
		aiProvider = null;
	}

	/**
	 * @return true: AI service is enabled; false: AI is disabled
	 */
	public boolean isAiEnabled() {
		return (aiProvider != null);
	}

	/**
	 * @return the configured AI service or NULL if not configured
	 */
	public AiSPI getAiProvider() {
		return aiProvider;
	}

	/**
	 * Set a new AI service or NULL to disable AI
	 * @param aiProvider
	 */
	public void setAiProvider(AiSPI aiProvider) {
		this.aiProvider = aiProvider;
		if (aiProvider == null) {
			setStringProperty(AI_PROVIDER, AI_PROVIDER_NONE, true);	
		} else {
			setStringProperty(AI_PROVIDER, aiProvider.getId(), true);			
		}
	}
	
	/**
	 * Set the list of available AI service implementations
	 * @param aiSPIs
	 */
	@Autowired
	public void setAiProviders(List<AiSPI> aiSPIs){
		this.aiProviders = aiSPIs;
	}

	/**
	 * Get all available AI service implementations
	 * @return
	 */
	public List<AiSPI> getAiProviders() {
		return aiProviders;
	}
}
