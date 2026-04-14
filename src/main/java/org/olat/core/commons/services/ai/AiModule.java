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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.commons.services.ai.spi.generic.GenericAiSPI;
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
	private static final String AI_IMG_DESC_SPI = "ai.feature.image-description-generator.spi";
	private static final String AI_IMG_DESC_MODEL = "ai.feature.image-description-generator.model";

	// List of all Spring-registered SPI implementations (OpenAI, Anthropic)
	private List<AiSPI> springProviders = List.of();

	// Generic SPI factory for user-created instances
	@Autowired
	private GenericAiSPI genericAiSPI;

	// Per-feature configuration
	private String mcGeneratorSpiId;
	private String mcGeneratorModel;
	private String imgDescSpiId;
	private String imgDescModel;

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
		imgDescSpiId = getStringPropertyValue(AI_IMG_DESC_SPI, imgDescSpiId);
		imgDescModel = getStringPropertyValue(AI_IMG_DESC_MODEL, imgDescModel);
	}

	/**
	 * @return true: the MC question generator feature is configured and available
	 */
	public boolean isMCQuestionGeneratorEnabled() {
		return resolveProvider(mcGeneratorSpiId) != null && StringHelper.containsNonWhitespace(mcGeneratorModel);
	}

	/**
	 * Get the configured MC question generator model name.
	 *
	 * @return The model name or null
	 */
	public String getMCGeneratorModel() {
		return mcGeneratorModel;
	}

	public void setMCQuestionGeneratorConfig(String spiId, String model) {
		this.mcGeneratorSpiId = spiId;
		this.mcGeneratorModel = model;
		setStringProperty(AI_MC_GENERATOR_SPI, StringHelper.containsNonWhitespace(spiId) ? spiId : "", true);
		setStringProperty(AI_MC_GENERATOR_MODEL, StringHelper.containsNonWhitespace(model) ? model : "", true);
	}

	public String getMCGeneratorSpiId() {
		return mcGeneratorSpiId;
	}

	/**
	 * @return true: the image description generator feature is configured and available
	 */
	public boolean isImageDescriptionGeneratorEnabled() {
		return resolveProvider(imgDescSpiId) != null && StringHelper.containsNonWhitespace(imgDescModel);
	}

	public void setImageDescriptionGeneratorConfig(String spiId, String model) {
		this.imgDescSpiId = spiId;
		this.imgDescModel = model;
		setStringProperty(AI_IMG_DESC_SPI, StringHelper.containsNonWhitespace(spiId) ? spiId : "", true);
		setStringProperty(AI_IMG_DESC_MODEL, StringHelper.containsNonWhitespace(model) ? model : "", true);
	}

	/**
	 * @return The SPI ID configured for image description generation, or null
	 */
	public String getImgDescSpiId() {
		return imgDescSpiId;
	}

	/**
	 * @return The model name configured for image description generation, or null
	 */
	public String getImgDescModel() {
		return imgDescModel;
	}

	/**
	 * @return All enabled AI providers
	 */
	public List<AiSPI> getEnabledProviders() {
		return getAiProviders().stream()
				.filter(AiSPI::isEnabled)
				.collect(Collectors.toList());
	}

	/**
	 * Resolve an enabled provider by its SPI ID.
	 *
	 * @param spiId The SPI identifier
	 * @return The enabled provider, or null if not found or disabled
	 */
	public AiSPI resolveProvider(String spiId) {
		if (!StringHelper.containsNonWhitespace(spiId)) {
			return null;
		}
		for (AiSPI spi : getAiProviders()) {
			if (spi.getId().equals(spiId) && spi.isEnabled()) {
				return spi;
			}
		}
		return null;
	}

	/**
	 * Set the list of Spring-registered AI service implementations (injected by Spring)
	 * @param aiSPIs
	 */
	@Autowired
	public void setSpringProviders(List<AiSPI> aiSPIs) {
		this.springProviders = aiSPIs;
	}

	/**
	 * Get all available AI service implementations including generic instances.
	 * @return Combined list of Spring-registered SPIs and generic instances
	 */
	public List<AiSPI> getAiProviders() {
		List<AiSPI> all = new ArrayList<>(springProviders);
		all.addAll(genericAiSPI.getInstances());
		return all;
	}

	/**
	 * Get only the Spring-registered providers (OpenAI, Anthropic) without generic instances.
	 * @return List of Spring-registered SPIs
	 */
	public List<AiSPI> getSpringProviders() {
		return springProviders;
	}

	/**
	 * @return The generic SPI factory
	 */
	public GenericAiSPI getGenericAiSPI() {
		return genericAiSPI;
	}
}
