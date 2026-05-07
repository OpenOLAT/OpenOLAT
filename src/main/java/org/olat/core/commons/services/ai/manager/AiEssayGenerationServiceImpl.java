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
package org.olat.core.commons.services.ai.manager;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.ai.AiEssayGenerationService;
import org.olat.core.commons.services.ai.AiFeature;
import org.olat.core.commons.services.ai.AiModule;
import org.olat.core.commons.services.ai.AiSPI;
import org.olat.core.commons.services.ai.essay.AiBloomLevel;
import org.olat.core.commons.services.ai.essay.AiContentChunk;
import org.olat.core.commons.services.ai.essay.AiEssayGenerationException;
import org.olat.core.commons.services.ai.essay.EssayItemDraft;
import org.olat.core.commons.services.ai.model.AiUsageContext;
import org.olat.core.commons.services.ai.service.EssayGenerationAiService;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;

/**
 * Spring service implementation for essay question generation via AI.
 * Mirrors the shape of {@link AiMCQuestionServiceImpl}: resolves the
 * admin-configured provider via {@link AiModule}, builds a
 * {@link ChatModel} via {@link AiSPI#buildChatModel(String, int)}, wraps it
 * with {@link AiLoggingChatModel}, and invokes the
 * {@link EssayGenerationAiService} LangChain4j proxy returning a list of
 * structured {@link EssayItemDraft} records.
 *
 * Initial date: 2026-04-20<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
@Service
public class AiEssayGenerationServiceImpl implements AiEssayGenerationService {
	private static final Logger log = Tracing.createLoggerFor(AiEssayGenerationServiceImpl.class);

	private static final int MAX_TOKENS = 4096;

	/**
	 * Per-call HTTP read timeout for essay generation. Generation runs
	 * asynchronously on the task executor and may produce multiple drafts
	 * in a single call, so cloud-provider wall time of 30-120 s is normal.
	 * Three minutes is a safe ceiling that still prevents a truly stuck
	 * call from hanging a worker thread forever.
	 */
	private static final Duration GENERATION_HTTP_TIMEOUT = Duration.ofMinutes(3);

	@Autowired
	private AiModule aiModule;
	@Autowired
	private AiUsageLogDAO aiUsageLogDAO;

	private volatile CachedChatModel cachedAiService;

	@Override
	public boolean isEnabled() {
		return aiModule.isEssayGenerationEnabled();
	}

	@Override
	public String getConfiguredSpiId() {
		return aiModule.getEssayGenerationSpiId();
	}

	@Override
	public String getConfiguredModel() {
		return aiModule.getEssayGenerationModel();
	}

	@Override
	public List<EssayItemDraft> generateEssayQuestions(AiUsageContext usageContext,
			List<AiContentChunk> chunks, List<String> learningObjectives,
			List<AiBloomLevel> targetBloomLevels, Integer targetDifficulty,
			int numberOfQuestions, Locale language) {
		return generateEssayQuestions(usageContext, chunks, learningObjectives, targetBloomLevels,
				targetDifficulty, numberOfQuestions, language, aiModule.getEssayGenerationSpiId(),
				aiModule.getEssayGenerationModel());
	}

	@Override
	public List<EssayItemDraft> generateEssayQuestions(AiUsageContext usageContext,
			List<AiContentChunk> chunks, List<String> learningObjectives,
			List<AiBloomLevel> targetBloomLevels, Integer targetDifficulty,
			int numberOfQuestions, Locale language,
			String spiId, String modelName) {
		AiSPI spi = aiModule.resolveProvider(spiId);
		if (spi == null) {
			throw new AiEssayGenerationException("AI provider is not configured or not available.");
		}
		long startTime = System.currentTimeMillis();
		try {
			cachedAiService = CachedChatModel.getOrRefresh(cachedAiService, spi, spiId, modelName,
					MAX_TOKENS, GENERATION_HTTP_TIMEOUT);
			ChatModel chatModel = cachedAiService.chatModel();

			AiServices<EssayGenerationAiService> builder = AiServices.builder(EssayGenerationAiService.class);
			AiLoggingChatModel.configureBuilder(builder, chatModel, aiUsageLogDAO, spiId,
					AiFeature.EssayGeneration.getType(), usageContext);
			EssayGenerationAiService service = builder.build();

			String bloomLevelsStr = targetBloomLevels == null || targetBloomLevels.isEmpty()
					? "UNDERSTAND, APPLY"
					: targetBloomLevels.stream().map(Enum::name).reduce((a, b) -> a + ", " + b).orElse("UNDERSTAND");
			String objectives = learningObjectives == null || learningObjectives.isEmpty()
					? "- Understand the key concepts in the provided source material."
					: "- " + String.join("\n- ", learningObjectives);
			String targetDifficultyStr = targetDifficulty == null
					? ""
					: "Target difficulty (1 = easiest, 5 = hardest, on a Bloom-aware scale): " + targetDifficulty
					  + ". Each generated question's `difficulty` field should match this target as closely as possible.";
			String languageName = language == null ? "English" : language.getDisplayLanguage(Locale.ENGLISH);
			String serialisedChunks = serialiseChunks(chunks);

			List<EssayItemDraft> drafts = service.generateEssayQuestions(
					numberOfQuestions <= 0 ? 1 : numberOfQuestions,
					bloomLevelsStr, objectives, targetDifficultyStr, languageName, serialisedChunks);
			return drafts == null ? List.of() : drafts;
		} catch (AiEssayGenerationException e) {
			throw e;
		} catch (Exception e) {
			log.warn("Essay generation call failed: {}", e.getMessage());
			Exception cause = e instanceof AiUsageLoggedException ? (Exception) e.getCause() : e;
			if (!(e instanceof AiUsageLoggedException)) {
				aiUsageLogDAO.createErrorLog(spiId, modelName, AiFeature.EssayGeneration.getType(), usageContext,
						System.currentTimeMillis() - startTime, cause);
			}
			throw new AiEssayGenerationException(
					cause.getMessage() != null ? cause.getMessage() : cause.getClass().getName(), cause);
		}
	}

	/**
	 * Render the caller's content chunks as a plain-text block suitable for
	 * the generator prompt.
	 */
	private static String serialiseChunks(List<AiContentChunk> chunks) {
		if (chunks == null || chunks.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder(chunks.size() * 200);
		for (AiContentChunk chunk : chunks) {
			sb.append("### ").append(chunk.id());
			if (chunk.headingPath() != null && !chunk.headingPath().isEmpty()) {
				sb.append(" — ").append(String.join(" / ", chunk.headingPath()));
			}
			sb.append('\n').append(chunk.text()).append("\n\n");
		}
		return sb.toString();
	}
}
