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

import org.olat.core.commons.services.ai.manager.AiTaskExecutorService;
import org.olat.core.commons.services.ai.spi.generic.GenericAiSPI;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.httpclient.ProtectionProfile;
import org.olat.modules.taxonomy.matching.TaxonomyMatchingModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * The AI module provides a service to use various (generative) AI services
 * within OpenOlat. Multiple providers can be enabled simultaneously. Each
 * feature (e.g. MC question generation) is configured to use a specific
 * provider and model.
 *
 * Initial date: 22.05.2024<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
@Service
public class AiModule extends AbstractSpringModule {
	
	public static final ProtectionProfile PROTECTION_PROFILE = ProtectionProfile.CONFIGURED;
	
	// Feature config property keys
	private static final String AI_MC_GENERATOR_ENABLED = "ai.feature.mc-question-generator.enabled";
	private static final String AI_MC_GENERATOR_SPI = "ai.feature.mc-question-generator.spi";
	private static final String AI_MC_GENERATOR_MODEL = "ai.feature.mc-question-generator.model";
	private static final String AI_IMG_DESC_ENABLED = "ai.feature.image-description-generator.enabled";
	private static final String AI_IMG_DESC_SPI = "ai.feature.image-description-generator.spi";
	private static final String AI_IMG_DESC_MODEL = "ai.feature.image-description-generator.model";
	private static final String AI_ESSAY_GENERATION_ENABLED = "ai.feature.essay-generation.enabled";
	private static final String AI_ESSAY_GENERATION_SPI = "ai.feature.essay-generation.spi";
	private static final String AI_ESSAY_GENERATION_MODEL = "ai.feature.essay-generation.model";
	private static final String AI_ESSAY_GRADING_ENABLED = "ai.feature.essay-grading.enabled";
	private static final String AI_ESSAY_GRADING_SPI = "ai.feature.essay-grading.spi";
	private static final String AI_ESSAY_GRADING_MODEL = "ai.feature.essay-grading.model";
	private static final String AI_TASK_POOL_INTERACTIVE_SIZE = "ai.task.pool.interactive.size";
	private static final String AI_TASK_POOL_BATCH_SIZE = "ai.task.pool.batch.size";
	private static final String AI_MC_GENERATOR_MAX_OUTPUT_TOKENS = "ai.mc.generator.max.output.tokens";
	private static final String AI_IMG_DESC_MAX_OUTPUT_TOKENS = "ai.img.desc.max.output.tokens";
	private static final String AI_ESSAY_GENERATION_MAX_OUTPUT_TOKENS = "ai.essay.generation.max.output.tokens";
	private static final String AI_ESSAY_GRADING_MAX_OUTPUT_TOKENS = "ai.essay.grading.max.output.tokens";
	private static final String AI_MC_GENERATOR_TIMEOUT_SECONDS = "ai.mc.generator.timeout.seconds";
	private static final String AI_IMG_DESC_TIMEOUT_SECONDS = "ai.img.desc.timeout.seconds";
	private static final String AI_ESSAY_GENERATION_TIMEOUT_SECONDS = "ai.essay.generation.timeout.seconds";
	private static final String AI_ESSAY_GRADING_TIMEOUT_SECONDS = "ai.essay.grading.timeout.seconds";
	private static final String AI_MC_GENERATOR_MAX_INPUT_CHARS = "ai.mc.generator.max.input.chars";
	private static final String AI_ESSAY_GENERATION_MAX_INPUT_CHARS = "ai.essay.generation.max.input.chars";
	private static final String AI_ESSAY_GRADING_MAX_INPUT_WORDS = "ai.essay.grading.max.input.words";

	// Per-user rate limit defaults (calls / minute / identity). Sized so a
	// fast-typing learner submitting essay answers across many questions in a
	// course is not throttled, but a scripted loop that re-submits the same
	// answer is. The author-side generation budget is tighter — generation
	// calls are far more expensive than grading calls.
	// TODO Wire to persisted properties via setStringProperty()/getStringPropertyValue
	// once the admin UI surface for per-feature rate limits exists.
	private static final int DEFAULT_ESSAY_GRADING_MAX_CALLS_PER_MINUTE_PER_USER = 30;
	private static final int DEFAULT_ESSAY_GENERATION_MAX_CALLS_PER_MINUTE_PER_USER = 10;

	// List of all Spring-registered SPI implementations (OpenAI, Anthropic)
	private List<AiSPI> springProviders = List.of();

	// Generic SPI factory for user-created instances
	@Autowired
	private GenericAiSPI genericAiSPI;

	@Autowired
	private TaxonomyMatchingModule taxonomyMatchingModule;

	// Per-feature configuration. The @Value defaults are read from
	// olat.properties / olat.local.properties and act as presets: they are used
	// as long as no value has been saved in the admin UI. Presets are applied
	// regardless of whether the feature or the referenced provider is enabled.
	@Value("${ai.feature.mc-question-generator.enabled:false}")
	private boolean mcGeneratorEnabled;
	@Value("${ai.feature.mc-question-generator.spi:}")
	private String mcGeneratorSpiId;
	@Value("${ai.feature.mc-question-generator.model:}")
	private String mcGeneratorModel;
	@Value("${ai.feature.image-description-generator.enabled:false}")
	private boolean imgDescEnabled;
	@Value("${ai.feature.image-description-generator.spi:}")
	private String imgDescSpiId;
	@Value("${ai.feature.image-description-generator.model:}")
	private String imgDescModel;
	@Value("${ai.feature.essay-generation.enabled:false}")
	private boolean essayGenerationEnabled;
	@Value("${ai.feature.essay-generation.spi:}")
	private String essayGenerationSpiId;
	@Value("${ai.feature.essay-generation.model:}")
	private String essayGenerationModel;
	@Value("${ai.feature.essay-grading.enabled:false}")
	private boolean essayGradingEnabled;
	@Value("${ai.feature.essay-grading.spi:}")
	private String essayGradingSpiId;
	@Value("${ai.feature.essay-grading.model:}")
	private String essayGradingModel;
	@Value("${ai.task.pool.interactive.size:8}")
	private int aiTaskPoolInteractiveSize;
	@Value("${ai.task.pool.batch.size:2}")
	private int aiTaskPoolBatchSize;
	@Value("${ai.mc.generator.max.output.tokens:16384}")
	private int mcGeneratorMaxOutputTokens;
	@Value("${ai.img.desc.max.output.tokens:8192}")
	private int imgDescMaxOutputTokens;
	@Value("${ai.essay.generation.max.output.tokens:16384}")
	private int essayGenerationMaxOutputTokens;
	@Value("${ai.essay.grading.max.output.tokens:16384}")
	private int essayGradingMaxOutputTokens;
	@Value("${ai.mc.generator.timeout.seconds:180}")
	private int mcGeneratorTimeoutSeconds;
	@Value("${ai.img.desc.timeout.seconds:180}")
	private int imgDescTimeoutSeconds;
	@Value("${ai.essay.generation.timeout.seconds:180}")
	private int essayGenerationTimeoutSeconds;
	@Value("${ai.essay.grading.timeout.seconds:600}")
	private int essayGradingTimeoutSeconds;
	@Value("${ai.mc.generator.max.input.chars:60000}")
	private int mcGeneratorMaxInputChars;
	@Value("${ai.essay.generation.max.input.chars:60000}")
	private int essayGenerationMaxInputChars;
	@Value("${ai.essay.grading.max.input.words:400}")
	private int essayGradingMaxInputWords;

	@Autowired
	private AiTaskExecutorService aiTaskExecutorService;

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
		mcGeneratorEnabled = "true".equalsIgnoreCase(getStringPropertyValue(AI_MC_GENERATOR_ENABLED, Boolean.toString(mcGeneratorEnabled)));
		mcGeneratorSpiId = getStringPropertyValue(AI_MC_GENERATOR_SPI, mcGeneratorSpiId);
		mcGeneratorModel = getStringPropertyValue(AI_MC_GENERATOR_MODEL, mcGeneratorModel);
		imgDescEnabled = "true".equalsIgnoreCase(getStringPropertyValue(AI_IMG_DESC_ENABLED, Boolean.toString(imgDescEnabled)));
		imgDescSpiId = getStringPropertyValue(AI_IMG_DESC_SPI, imgDescSpiId);
		imgDescModel = getStringPropertyValue(AI_IMG_DESC_MODEL, imgDescModel);
		essayGenerationEnabled = "true".equalsIgnoreCase(getStringPropertyValue(AI_ESSAY_GENERATION_ENABLED, Boolean.toString(essayGenerationEnabled)));
		essayGenerationSpiId = getStringPropertyValue(AI_ESSAY_GENERATION_SPI, essayGenerationSpiId);
		essayGenerationModel = getStringPropertyValue(AI_ESSAY_GENERATION_MODEL, essayGenerationModel);
		essayGradingEnabled = "true".equalsIgnoreCase(getStringPropertyValue(AI_ESSAY_GRADING_ENABLED, Boolean.toString(essayGradingEnabled)));
		essayGradingSpiId = getStringPropertyValue(AI_ESSAY_GRADING_SPI, essayGradingSpiId);
		essayGradingModel = getStringPropertyValue(AI_ESSAY_GRADING_MODEL, essayGradingModel);
		aiTaskPoolInteractiveSize = getIntPropertyValue(AI_TASK_POOL_INTERACTIVE_SIZE, aiTaskPoolInteractiveSize);
		aiTaskPoolBatchSize = getIntPropertyValue(AI_TASK_POOL_BATCH_SIZE, aiTaskPoolBatchSize);
		mcGeneratorMaxOutputTokens = getIntPropertyValue(AI_MC_GENERATOR_MAX_OUTPUT_TOKENS, mcGeneratorMaxOutputTokens);
		imgDescMaxOutputTokens = getIntPropertyValue(AI_IMG_DESC_MAX_OUTPUT_TOKENS, imgDescMaxOutputTokens);
		essayGenerationMaxOutputTokens = getIntPropertyValue(AI_ESSAY_GENERATION_MAX_OUTPUT_TOKENS, essayGenerationMaxOutputTokens);
		essayGradingMaxOutputTokens = getIntPropertyValue(AI_ESSAY_GRADING_MAX_OUTPUT_TOKENS, essayGradingMaxOutputTokens);
		mcGeneratorTimeoutSeconds = getIntPropertyValue(AI_MC_GENERATOR_TIMEOUT_SECONDS, mcGeneratorTimeoutSeconds);
		imgDescTimeoutSeconds = getIntPropertyValue(AI_IMG_DESC_TIMEOUT_SECONDS, imgDescTimeoutSeconds);
		essayGenerationTimeoutSeconds = getIntPropertyValue(AI_ESSAY_GENERATION_TIMEOUT_SECONDS, essayGenerationTimeoutSeconds);
		essayGradingTimeoutSeconds = getIntPropertyValue(AI_ESSAY_GRADING_TIMEOUT_SECONDS, essayGradingTimeoutSeconds);
		mcGeneratorMaxInputChars = getIntPropertyValue(AI_MC_GENERATOR_MAX_INPUT_CHARS, mcGeneratorMaxInputChars);
		essayGenerationMaxInputChars = getIntPropertyValue(AI_ESSAY_GENERATION_MAX_INPUT_CHARS, essayGenerationMaxInputChars);
		essayGradingMaxInputWords = getIntPropertyValue(AI_ESSAY_GRADING_MAX_INPUT_WORDS, essayGradingMaxInputWords);
		applyTaskPoolSizes();
	}

	/**
	 * Push the configured pool sizes onto the live executors. Called at
	 * startup and whenever the configuration changes (cluster-wide via
	 * {@code initFromChangedProperties}).
	 */
	private void applyTaskPoolSizes() {
		if (aiTaskExecutorService != null) {
			aiTaskExecutorService.setInteractivePoolSize(aiTaskPoolInteractiveSize);
			aiTaskExecutorService.setBatchPoolSize(aiTaskPoolBatchSize);
		}
	}

	/**
	 * Number of worker threads (per node) for interactive AI tasks — calls
	 * a user is actively waiting on, e.g. essay AI correction at learner
	 * submit.
	 */
	public int getAiTaskPoolInteractiveSize() {
		return aiTaskPoolInteractiveSize;
	}

	public void setAiTaskPoolInteractiveSize(int size) {
		if (size < 1) {
			return;
		}
		aiTaskPoolInteractiveSize = size;
		setIntProperty(AI_TASK_POOL_INTERACTIVE_SIZE, size, true);
		applyTaskPoolSizes();
	}

	/**
	 * Number of worker threads (per node) for AI batch tasks — long-running
	 * jobs like question generation from page content.
	 */
	public int getAiTaskPoolBatchSize() {
		return aiTaskPoolBatchSize;
	}

	public void setAiTaskPoolBatchSize(int size) {
		if (size < 1) {
			return;
		}
		aiTaskPoolBatchSize = size;
		setIntProperty(AI_TASK_POOL_BATCH_SIZE, size, true);
		applyTaskPoolSizes();
	}

	/**
	 * @return true: the MC question generator feature is switched on by the admin,
	 *         regardless of whether the provider/model config is complete
	 */
	public boolean isMCQuestionGeneratorEnabled() {
		return mcGeneratorEnabled;
	}

	/**
	 * @return true: the MC question generator's provider and model are configured and available
	 */
	public boolean isMCQuestionGeneratorConfigured() {
		return resolveProvider(mcGeneratorSpiId) != null && StringHelper.containsNonWhitespace(mcGeneratorModel);
	}

	public void setMCQuestionGeneratorEnabled(boolean enabled) {
		this.mcGeneratorEnabled = enabled;
		setStringProperty(AI_MC_GENERATOR_ENABLED, Boolean.toString(enabled), true);
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
	 * @return maximum number of output tokens for the MC question generator model
	 */
	public int getMCGeneratorMaxOutputTokens() {
		return mcGeneratorMaxOutputTokens;
	}

	public void setMCGeneratorMaxOutputTokens(int maxTokens) {
		if (maxTokens < 1) {
			return;
		}
		mcGeneratorMaxOutputTokens = maxTokens;
		setIntProperty(AI_MC_GENERATOR_MAX_OUTPUT_TOKENS, maxTokens, true);
	}

	/**
	 * @return HTTP timeout in seconds for the MC question generator model
	 */
	public int getMCGeneratorTimeoutSeconds() {
		return mcGeneratorTimeoutSeconds;
	}

	public void setMCGeneratorTimeoutSeconds(int timeoutSeconds) {
		if (timeoutSeconds < 1) {
			return;
		}
		mcGeneratorTimeoutSeconds = timeoutSeconds;
		setIntProperty(AI_MC_GENERATOR_TIMEOUT_SECONDS, timeoutSeconds, true);
	}

	/**
	 * @return maximum number of characters of input text sent to the MC question generator model
	 */
	public int getMCGeneratorMaxInputChars() {
		return mcGeneratorMaxInputChars;
	}

	public void setMCGeneratorMaxInputChars(int maxChars) {
		if (maxChars < 1) {
			return;
		}
		mcGeneratorMaxInputChars = maxChars;
		setIntProperty(AI_MC_GENERATOR_MAX_INPUT_CHARS, maxChars, true);
	}

	/**
	 * @return true: the image description generator feature is switched on by the admin,
	 *         regardless of whether the provider/model config is complete
	 */
	public boolean isImageDescriptionGeneratorEnabled() {
		return imgDescEnabled;
	}

	/**
	 * @return true: the image description generator's provider and model are configured and available
	 */
	public boolean isImageDescriptionGeneratorConfigured() {
		return resolveProvider(imgDescSpiId) != null && StringHelper.containsNonWhitespace(imgDescModel);
	}

	public void setImageDescriptionGeneratorEnabled(boolean enabled) {
		this.imgDescEnabled = enabled;
		setStringProperty(AI_IMG_DESC_ENABLED, Boolean.toString(enabled), true);
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
	 * @return maximum number of output tokens for the image description model
	 */
	public int getImgDescMaxOutputTokens() {
		return imgDescMaxOutputTokens;
	}

	public void setImgDescMaxOutputTokens(int maxTokens) {
		if (maxTokens < 1) {
			return;
		}
		imgDescMaxOutputTokens = maxTokens;
		setIntProperty(AI_IMG_DESC_MAX_OUTPUT_TOKENS, maxTokens, true);
	}

	/**
	 * @return HTTP timeout in seconds for the image description model
	 */
	public int getImgDescTimeoutSeconds() {
		return imgDescTimeoutSeconds;
	}

	public void setImgDescTimeoutSeconds(int timeoutSeconds) {
		if (timeoutSeconds < 1) {
			return;
		}
		imgDescTimeoutSeconds = timeoutSeconds;
		setIntProperty(AI_IMG_DESC_TIMEOUT_SECONDS, timeoutSeconds, true);
	}

	/**
	 * @return true: the taxonomy matching feature is enabled and the configured embedding SPI is available
	 */
	public boolean isTaxonomyMatchingEnabled() {
		if (taxonomyMatchingModule == null) {
			return false;
		}
		return taxonomyMatchingModule.isEnabled()
				&& getConfiguredEmbeddingSPI() != null
				&& StringHelper.containsNonWhitespace(taxonomyMatchingModule.getModel());
	}

	/**
	 * @return minimum cosine similarity score for taxonomy matching, from TaxonomyMatchingModule
	 */
	public double getTaxonomyMatchingMinScore() {
		if (taxonomyMatchingModule == null) {
			return 0.65;
		}
		return taxonomyMatchingModule.getMinScore();
	}

	/**
	 * @return the configured embedding SPI for taxonomy matching, or null if not configured
	 */
	public AiEmbeddingSPI getConfiguredEmbeddingSPI() {
		if (taxonomyMatchingModule == null) {
			return null;
		}
		String spiId = taxonomyMatchingModule.getSpiId();
		if (!StringHelper.containsNonWhitespace(spiId)) {
			return null;
		}
		AiSPI spi = resolveProvider(spiId);
		if (spi instanceof AiEmbeddingSPI embeddingSpi && embeddingSpi.isEmbeddingEnabled()) {
			return embeddingSpi;
		}
		return null;
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

	// ---------------------------------------------------------------------
	// Essay feature — generation + grading routing
	// ---------------------------------------------------------------------

	/**
	 * @return true: the essay-question generator feature is switched on by the admin,
	 *         regardless of whether the provider/model config is complete
	 */
	public boolean isEssayGenerationEnabled() {
		return essayGenerationEnabled;
	}

	/**
	 * @return true: the essay-question generator's provider and model are configured and available
	 */
	public boolean isEssayGenerationConfigured() {
		return resolveProvider(essayGenerationSpiId) != null
				&& StringHelper.containsNonWhitespace(essayGenerationModel);
	}

	public void setEssayGenerationEnabled(boolean enabled) {
		this.essayGenerationEnabled = enabled;
		setStringProperty(AI_ESSAY_GENERATION_ENABLED, Boolean.toString(enabled), true);
	}

	/**
	 * @return true: the essay grading feature is switched on by the admin,
	 *         regardless of whether the provider/model config is complete
	 */
	public boolean isEssayGradingEnabled() {
		return essayGradingEnabled;
	}

	/**
	 * @return true: the essay grading feature's provider and model are configured and available
	 */
	public boolean isEssayGradingConfigured() {
		return resolveProvider(essayGradingSpiId) != null
				&& StringHelper.containsNonWhitespace(essayGradingModel);
	}

	public void setEssayGradingEnabled(boolean enabled) {
		this.essayGradingEnabled = enabled;
		setStringProperty(AI_ESSAY_GRADING_ENABLED, Boolean.toString(enabled), true);
	}

	public String getEssayGenerationSpiId() {
		return essayGenerationSpiId;
	}

	public String getEssayGenerationModel() {
		return essayGenerationModel;
	}

	/**
	 * @return maximum number of output tokens for the essay generation model
	 */
	public int getEssayGenerationMaxOutputTokens() {
		return essayGenerationMaxOutputTokens;
	}

	public void setEssayGenerationMaxOutputTokens(int maxTokens) {
		if (maxTokens < 1) {
			return;
		}
		essayGenerationMaxOutputTokens = maxTokens;
		setIntProperty(AI_ESSAY_GENERATION_MAX_OUTPUT_TOKENS, maxTokens, true);
	}

	/**
	 * @return HTTP timeout in seconds for the essay generation model
	 */
	public int getEssayGenerationTimeoutSeconds() {
		return essayGenerationTimeoutSeconds;
	}

	public void setEssayGenerationTimeoutSeconds(int timeoutSeconds) {
		if (timeoutSeconds < 1) {
			return;
		}
		essayGenerationTimeoutSeconds = timeoutSeconds;
		setIntProperty(AI_ESSAY_GENERATION_TIMEOUT_SECONDS, timeoutSeconds, true);
	}

	/**
	 * @return maximum number of characters of input text sent to the essay generation model
	 */
	public int getEssayGenerationMaxInputChars() {
		return essayGenerationMaxInputChars;
	}

	public void setEssayGenerationMaxInputChars(int maxChars) {
		if (maxChars < 1) {
			return;
		}
		essayGenerationMaxInputChars = maxChars;
		setIntProperty(AI_ESSAY_GENERATION_MAX_INPUT_CHARS, maxChars, true);
	}

	public void setEssayGenerationConfig(String spiId, String model) {
		this.essayGenerationSpiId = spiId;
		this.essayGenerationModel = model;
		setStringProperty(AI_ESSAY_GENERATION_SPI, StringHelper.containsNonWhitespace(spiId) ? spiId : "", true);
		setStringProperty(AI_ESSAY_GENERATION_MODEL, StringHelper.containsNonWhitespace(model) ? model : "", true);
	}

	public String getEssayGradingSpiId() {
		return essayGradingSpiId;
	}

	public String getEssayGradingModel() {
		return essayGradingModel;
	}

	/**
	 * @return maximum number of output tokens for the essay grading model
	 */
	public int getEssayGradingMaxOutputTokens() {
		return essayGradingMaxOutputTokens;
	}

	public void setEssayGradingMaxOutputTokens(int maxTokens) {
		if (maxTokens < 1) {
			return;
		}
		essayGradingMaxOutputTokens = maxTokens;
		setIntProperty(AI_ESSAY_GRADING_MAX_OUTPUT_TOKENS, maxTokens, true);
	}

	/**
	 * @return HTTP timeout in seconds for the essay grading model
	 */
	public int getEssayGradingTimeoutSeconds() {
		return essayGradingTimeoutSeconds;
	}

	public void setEssayGradingTimeoutSeconds(int timeoutSeconds) {
		if (timeoutSeconds < 1) {
			return;
		}
		essayGradingTimeoutSeconds = timeoutSeconds;
		setIntProperty(AI_ESSAY_GRADING_TIMEOUT_SECONDS, timeoutSeconds, true);
	}

	/**
	 * @return maximum number of words of a student answer accepted for essay grading
	 */
	public int getEssayGradingMaxInputWords() {
		return essayGradingMaxInputWords;
	}

	public void setEssayGradingMaxInputWords(int maxWords) {
		if (maxWords < 1) {
			return;
		}
		essayGradingMaxInputWords = maxWords;
		setIntProperty(AI_ESSAY_GRADING_MAX_INPUT_WORDS, maxWords, true);
	}

	public void setEssayGradingConfig(String spiId, String model) {
		this.essayGradingSpiId = spiId;
		this.essayGradingModel = model;
		setStringProperty(AI_ESSAY_GRADING_SPI, StringHelper.containsNonWhitespace(spiId) ? spiId : "", true);
		setStringProperty(AI_ESSAY_GRADING_MODEL, StringHelper.containsNonWhitespace(model) ? model : "", true);
	}

	/**
	 * Per-user rate limit for essay-grading submit calls. Counts of accepted
	 * grading jobs in a sliding 60-second window; when the count reaches this
	 * value the next {@code submit(...)} fails fast with
	 * {@link org.olat.core.commons.services.ai.essay.AiRateLimitExceededException}.
	 *
	 * @return positive call count (calls per minute per identity)
	 */
	public int getEssayGradingMaxCallsPerMinutePerUser() {
		return DEFAULT_ESSAY_GRADING_MAX_CALLS_PER_MINUTE_PER_USER;
	}

	/**
	 * Per-user rate limit for AI question generation submit calls. Same
	 * sliding-window semantics as
	 * {@link #getEssayGradingMaxCallsPerMinutePerUser()} but tighter — each
	 * generation call drives multiple expensive provider invocations.
	 *
	 * @return positive call count (calls per minute per identity)
	 */
	public int getEssayGenerationMaxCallsPerMinutePerUser() {
		return DEFAULT_ESSAY_GENERATION_MAX_CALLS_PER_MINUTE_PER_USER;
	}
}
