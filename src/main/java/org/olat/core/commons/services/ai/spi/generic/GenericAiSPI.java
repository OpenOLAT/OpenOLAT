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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.ai.AiPromptHelper;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.httpclient.HttpClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Factory and registry for generic OpenAI-compatible AI provider instances.
 * Each instance is stored as numbered properties in this module's config file:
 * <pre>
 *   generic.instances=1,2,5
 *   generic.1.name=My vLLM Server
 *   generic.1.base.url=https://my-server:8000/v1/
 *   generic.1.api.key=
 *   generic.1.models=meta-llama/Llama-3-70b,mistralai/Mixtral-8x7B
 *   generic.1.enabled=true
 * </pre>
 *
 * Initial date: 09.03.2026<br>
 *
 * @author gnaegi@frentix.com, https://www.frentix.com
 *
 */
@Service
public class GenericAiSPI extends AbstractSpringModule {
	private static final Logger log = Tracing.createLoggerFor(GenericAiSPI.class);

	private static final String GENERIC_INSTANCES = "generic.instances";

	// Reserved id for the preset instance defined via olat.properties. User
	// created instances always start at 1, so this id can never collide.
	private static final int PRESET_ID = 0;

	// Preset generic provider defined via olat.properties / olat.local.properties.
	// These values act as defaults and can be overridden in the admin UI (the
	// overridden values are persisted under generic.0.*). Leave the base URL
	// empty to not provide a preset generic provider.
	@Value("${ai.generic.preset.name:}")
	private String presetNameDefault;
	@Value("${ai.generic.preset.base.url:}")
	private String presetBaseUrlDefault;
	@Value("${ai.generic.preset.api.key:}")
	private String presetApiKeyDefault;
	@Value("${ai.generic.preset.models:}")
	private String presetModelsDefault;
	@Value("${ai.generic.preset.enabled:false}")
	private boolean presetEnabledDefault;

	private final List<GenericAiSpiInstance> instances = new ArrayList<>();
	private final AtomicInteger nextId = new AtomicInteger(1);

	@Autowired
	private AiPromptHelper aiPromptHelper;

	@Autowired
	private HttpClientService httpClientService;

	@Autowired
	public GenericAiSPI(CoordinatorManager coordinatorManager) {
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
		instances.clear();
		loadPresetInstance();

		String instancesStr = getStringPropertyValue(GENERIC_INSTANCES, "");
		if (!StringHelper.containsNonWhitespace(instancesStr)) {
			return;
		}

		int maxId = 0;
		for (String idStr : instancesStr.split(",")) {
			idStr = idStr.trim();
			if (!StringHelper.containsNonWhitespace(idStr)) continue;
			try {
				int id = Integer.parseInt(idStr);
				if (id > maxId) maxId = id;
				GenericAiSpiInstance instance = new GenericAiSpiInstance(id, this);
				String name = getStringPropertyValue("generic." + id + ".name", "");
				String baseUrl = getStringPropertyValue("generic." + id + ".base.url", "");
				String apiKey = getStringPropertyValue("generic." + id + ".api.key", "");
				String models = getStringPropertyValue("generic." + id + ".models", "");
				boolean enabled = "true".equals(getStringPropertyValue("generic." + id + ".enabled", "false"));
				instance.load(name, baseUrl, apiKey, models, enabled);
				instances.add(instance);
			} catch (NumberFormatException e) {
				log.warn("Invalid generic SPI instance ID: {}", idStr);
			}
		}
		nextId.set(maxId + 1);
	}

	/**
	 * Load the preset instance (id 0) defined via olat.properties. The values
	 * from olat.properties act as defaults; if an admin has overridden them in
	 * the UI, the persisted generic.0.* values take precedence. The preset is
	 * only created when an effective base URL is available. Id 0 is never part
	 * of the {@link #GENERIC_INSTANCES} list and is always listed first.
	 */
	private void loadPresetInstance() {
		String baseUrl = getStringPropertyValue("generic." + PRESET_ID + ".base.url", presetBaseUrlDefault);
		if (!StringHelper.containsNonWhitespace(baseUrl)) {
			return;
		}
		String name = getStringPropertyValue("generic." + PRESET_ID + ".name", presetNameDefault);
		String apiKey = getStringPropertyValue("generic." + PRESET_ID + ".api.key", presetApiKeyDefault);
		String models = getStringPropertyValue("generic." + PRESET_ID + ".models", presetModelsDefault);
		boolean enabled = "true".equals(getStringPropertyValue("generic." + PRESET_ID + ".enabled",
				String.valueOf(presetEnabledDefault)));

		GenericAiSpiInstance preset = new GenericAiSpiInstance(PRESET_ID, this);
		preset.load(name, baseUrl, apiKey, models, enabled);
		preset.setPreset(true);
		instances.add(preset);
	}

	/**
	 * @return All configured generic SPI instances
	 */
	public List<GenericAiSpiInstance> getInstances() {
		return List.copyOf(instances);
	}

	/**
	 * Create a new generic SPI instance with default values.
	 *
	 * @return The newly created instance
	 */
	public GenericAiSpiInstance createInstance() {
		int id = nextId.getAndIncrement();
		GenericAiSpiInstance instance = new GenericAiSpiInstance(id, this);
		instance.load("", "", "", "", true);
		instances.add(instance);
		saveInstanceIds();
		return instance;
	}

	/**
	 * Delete an instance by its ID. Removes all properties and the instance
	 * from the list.
	 *
	 * @param instanceId The instance ID to delete
	 */
	void deleteInstance(int instanceId) {
		if (instanceId == PRESET_ID) {
			// The preset instance is defined via olat.properties and cannot be deleted
			log.warn("Attempt to delete the preset generic SPI instance (id {}) ignored", PRESET_ID);
			return;
		}
		instances.removeIf(i -> i.getInstanceId() == instanceId);
		// Remove all properties for this instance
		removeProperty("generic." + instanceId + ".name", true);
		removeProperty("generic." + instanceId + ".base.url", true);
		removeProperty("generic." + instanceId + ".api.key", true);
		removeProperty("generic." + instanceId + ".models", true);
		removeProperty("generic." + instanceId + ".enabled", true);
		saveInstanceIds();
	}

	/**
	 * Get an instance by its SPI ID (e.g. "Generic_3").
	 *
	 * @param spiId The SPI ID
	 * @return The instance, or null if not found
	 */
	public GenericAiSpiInstance getInstanceBySpiId(String spiId) {
		if (spiId == null || !spiId.startsWith("Generic_")) {
			return null;
		}
		try {
			int id = Integer.parseInt(spiId.substring("Generic_".length()));
			return instances.stream()
					.filter(i -> i.getInstanceId() == id)
					.findFirst()
					.orElse(null);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Set a property for a specific instance.
	 */
	void setInstanceProperty(int instanceId, String suffix, String value) {
		setStringProperty("generic." + instanceId + "." + suffix,
				value != null ? value : "", true);
	}

	/**
	 * Save the current list of instance IDs to the properties.
	 */
	private void saveInstanceIds() {
		String ids = instances.stream()
				.filter(i -> i.getInstanceId() != PRESET_ID)
				.map(i -> String.valueOf(i.getInstanceId()))
				.reduce((a, b) -> a + "," + b)
				.orElse("");
		setStringProperty(GENERIC_INSTANCES, ids, true);
	}

	AiPromptHelper getAiPromptHelper() {
		return aiPromptHelper;
	}

	HttpClientService getHttpClientService() {
		return httpClientService;
	}
}
