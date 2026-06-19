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
	// Feature config property keys
	private static final String AI_MC_GENERATOR_SPI = "ai.feature.mc-question-generator.spi";
	private static final String AI_MC_GENERATOR_MODEL = "ai.feature.mc-question-generator.model";
	private static final String AI_IMG_DESC_SPI = "ai.feature.image-description-generator.spi";
	private static final String AI_IMG_DESC_MODEL = "ai.feature.image-description-generator.model";
	private static final String AI_ESSAY_GENERATION_SPI = "ai.feature.essay-generation.spi";
	private static final String AI_ESSAY_GENERATION_MODEL = "ai.feature.essay-generation.model";
	private static final String AI_ESSAY_GRADING_SPI = "ai.feature.essay-grading.spi";
	private static final String AI_ESSAY_GRADING_MODEL = "ai.feature.essay-grading.model";
	private static final String AI_TASK_POOL_INTERACTIVE_SIZE = "ai.task.pool.interactive.size";
	private static final String AI_TASK_POOL_BATCH_SIZE = "ai.task.pool.batch.size";

	// Per-user rate limit defaults (calls / minute / identity). Sized so a
	// fast-typing learner submitting essay answers across many questions in a
	// course is not throttled, but a scripted loop that re-submits the same
	// answer is. The author-side generation budget is tighter — generation
	// calls are far more expensive than grading calls.
	// TODO Wire to persisted properties via setStringProperty()/getStringPropertyValue
	// once the admin UI surface for per-feature rate limits exists.
	private static final int DEFAULT_ESSAY_GRADING_MAX_CALLS_PER_MINUTE_PER_USER = 30;
	private static final int DEFAULT_ESSAY_GENERATION_MAX_CALLS_PER_MINUTE_PER_USER = 10;

	// AI task pool defaults (worker threads per node). The right values
	// depend on the infrastructure behind the provider: cloud APIs handle
	// 10+ parallel calls, a single self-hosted GPU saturates at 2-4.
	private static final int DEFAULT_AI_TASK_POOL_INTERACTIVE_SIZE = 4;
	private static final int DEFAULT_AI_TASK_POOL_BATCH_SIZE = 2;

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
	@Value("${ai.feature.mc-question-generator.spi:}")
	private String mcGeneratorSpiId;
	@Value("${ai.feature.mc-question-generator.model:}")
	private String mcGeneratorModel;
	@Value("${ai.feature.image-description-generator.spi:}")
	private String imgDescSpiId;
	@Value("${ai.feature.image-description-generator.model:}")
	private String imgDescModel;
	@Value("${ai.feature.essay-generation.spi:}")
	private String essayGenerationSpiId;
	@Value("${ai.feature.essay-generation.model:}")
	private String essayGenerationModel;
	@Value("${ai.feature.essay-grading.spi:}")
	private String essayGradingSpiId;
	@Value("${ai.feature.essay-grading.model:}")
	private String essayGradingModel;
	private int aiTaskPoolInteractiveSize = DEFAULT_AI_TASK_POOL_INTERACTIVE_SIZE;
	private int aiTaskPoolBatchSize = DEFAULT_AI_TASK_POOL_BATCH_SIZE;

	@Autowired
	private org.olat.core.commons.services.ai.manager.AiTaskExecutorService aiTaskExecutorService;

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
		essayGenerationSpiId = getStringPropertyValue(AI_ESSAY_GENERATION_SPI, essayGenerationSpiId);
		essayGenerationModel = getStringPropertyValue(AI_ESSAY_GENERATION_MODEL, essayGenerationModel);
		essayGradingSpiId = getStringPropertyValue(AI_ESSAY_GRADING_SPI, essayGradingSpiId);
		essayGradingModel = getStringPropertyValue(AI_ESSAY_GRADING_MODEL, essayGradingModel);
		aiTaskPoolInteractiveSize = getIntPropertyValue(AI_TASK_POOL_INTERACTIVE_SIZE,
				DEFAULT_AI_TASK_POOL_INTERACTIVE_SIZE);
		aiTaskPoolBatchSize = getIntPropertyValue(AI_TASK_POOL_BATCH_SIZE,
				DEFAULT_AI_TASK_POOL_BATCH_SIZE);
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
	 * @return true: the essay-question generator feature is configured and the
	 *         backing SPI is enabled
	 */
	public boolean isEssayGenerationEnabled() {
		return resolveProvider(essayGenerationSpiId) != null
				&& StringHelper.containsNonWhitespace(essayGenerationModel);
	}

	/**
	 * @return true: the essay grading (formative feedback) feature is
	 *         configured and the backing SPI is enabled
	 */
	public boolean isEssayGradingEnabled() {
		return resolveProvider(essayGradingSpiId) != null
				&& StringHelper.containsNonWhitespace(essayGradingModel);
	}

	public String getEssayGenerationSpiId() {
		return essayGenerationSpiId;
	}

	public String getEssayGenerationModel() {
		return essayGenerationModel;
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
