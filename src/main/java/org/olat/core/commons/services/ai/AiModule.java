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
import java.util.stream.Collectors;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The AI module provides a service to use various (generative) AI services
 * within OpenOlat. Multiple providers can be enabled simultaneously. Each
 * feature (e.g. MC question generation) is configured to use a specific
 * provider and model.
 *
 * Initial date: 22.05.2024<br>
 *
 * @author Florian Gnägi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
@Service
public class AiModule extends AbstractSpringModule {
	// Feature config property keys
	private static final String AI_MC_GENERATOR_SPI = "ai.feature.mc-question-generator.spi";
	private static final String AI_MC_GENERATOR_MODEL = "ai.feature.mc-question-generator.model";

	// List of all available SPI implementations
	private List<AiSPI> aiProviders;

	// Per-feature configuration
	private String mcGeneratorSpiId;
	private String mcGeneratorModel;

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
		mcGeneratorSpiId = getStringPropertyValue(AI_MC_GENERATOR_SPI, mcGeneratorSpiId);
		mcGeneratorModel = getStringPropertyValue(AI_MC_GENERATOR_MODEL, mcGeneratorModel);
	}

	/**
	 * @return true: at least one AI feature is configured and ready to use
	 */
	public boolean isAiEnabled() {
		return isMCQuestionGeneratorEnabled();
	}

	/**
	 * @return true: the MC question generator feature is configured and available
	 */
	public boolean isMCQuestionGeneratorEnabled() {
		if (!StringHelper.containsNonWhitespace(mcGeneratorSpiId)) {
			return false;
		}
		for (AiSPI spi : aiProviders) {
			if (spi.getId().equals(mcGeneratorSpiId) && spi.isEnabled() && spi instanceof AiMCQuestionGeneratorSPI) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get the configured MC question generator with the model pre-set.
	 * Returns null if no generator is configured or available.
	 *
	 * @return The configured AiMCQuestionGeneratorSPI or null
	 */
	public AiMCQuestionGeneratorSPI getMCQuestionGenerator() {
		if (!StringHelper.containsNonWhitespace(mcGeneratorSpiId)) {
			return null;
		}
		for (AiSPI spi : aiProviders) {
			if (spi.getId().equals(mcGeneratorSpiId) && spi.isEnabled() && spi instanceof AiMCQuestionGeneratorSPI) {
				AiMCQuestionGeneratorSPI generator = (AiMCQuestionGeneratorSPI) spi;
				generator.setMCGeneratorModel(mcGeneratorModel);
				return generator;
			}
		}
		return null;
	}

	/**
	 * Configure which SPI and model to use for MC question generation.
	 *
	 * @param spiId The SPI identifier, or null/empty to disable
	 * @param model The model name
	 */
	public void setMCQuestionGeneratorConfig(String spiId, String model) {
		this.mcGeneratorSpiId = spiId;
		this.mcGeneratorModel = model;
		setStringProperty(AI_MC_GENERATOR_SPI, StringHelper.containsNonWhitespace(spiId) ? spiId : "", true);
		setStringProperty(AI_MC_GENERATOR_MODEL, StringHelper.containsNonWhitespace(model) ? model : "", true);
	}

	/**
	 * @return The SPI ID configured for MC question generation, or null
	 */
	public String getMCGeneratorSpiId() {
		return mcGeneratorSpiId;
	}

	/**
	 * @return The model name configured for MC question generation, or null
	 */
	public String getMCGeneratorModel() {
		return mcGeneratorModel;
	}

	/**
	 * Get all enabled SPIs that implement a given feature interface.
	 *
	 * @param featureClass The feature interface class
	 * @return List of enabled SPIs implementing the feature
	 */
	public List<AiSPI> getEnabledSPIsFor(Class<?> featureClass) {
		return aiProviders.stream()
				.filter(spi -> spi.isEnabled() && featureClass.isInstance(spi))
				.collect(Collectors.toList());
	}

	/**
	 * Set the list of available AI service implementations (injected by Spring)
	 * @param aiSPIs
	 */
	@Autowired
	public void setAiProviders(List<AiSPI> aiSPIs) {
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
