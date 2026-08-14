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
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.ai.AiEssayGradingService;
import org.olat.core.commons.services.ai.AiFeature;
import org.olat.core.commons.services.ai.AiModule;
import org.olat.core.commons.services.ai.AiSPI;
import org.olat.core.commons.services.ai.essay.AiEssayGradingException;
import org.olat.core.commons.services.ai.essay.AiEssayResponseTruncatedException;
import org.olat.core.commons.services.ai.essay.AiGradingTier;
import org.olat.core.commons.services.ai.essay.EssayAiGrading;
import org.olat.core.commons.services.ai.essay.GradingSuggestion;
import org.olat.core.commons.services.ai.model.AiUsageContext;
import org.olat.core.commons.services.ai.service.EssayGradingAiService;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;

/**
 * Spring service implementation for AI-assisted essay grading. Mirrors the
 * shape of {@link AiMCQuestionServiceImpl}: resolves the admin-configured
 * provider via {@link AiModule}, builds a {@link ChatModel} via
 * {@link AiSPI#buildChatModel(String, int)}, wraps it with
 * {@link AiLoggingChatModel}, and invokes the {@link EssayGradingAiService}
 * LangChain4j proxy returning a structured {@link GradingSuggestion}.
 *
 * Initial date: 2026-04-20<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
@Service
public class AiEssayGradingServiceImpl implements AiEssayGradingService {
	private static final Logger log = Tracing.createLoggerFor(AiEssayGradingServiceImpl.class);

	/** Inlined constant stamped on every usage-log row — mirrors MC feature. */
	public static final String PROMPT_TEMPLATE_VERSION = "essay-grading-v1";

	@Autowired
	private AiModule aiModule;
	@Autowired
	private AiUsageLogDAO aiUsageLogDAO;

	@Override
	public boolean isEnabled() {
		return aiModule.isEssayGradingEnabled() && aiModule.isEssayGradingConfigured();
	}

	@Override
	public String getPromptTemplateVersion() {
		return PROMPT_TEMPLATE_VERSION;
	}

	@Override
	public String getConfiguredSpiId() {
		return aiModule.getEssayGradingSpiId();
	}

	@Override
	public String getConfiguredModel() {
		return aiModule.getEssayGradingModel();
	}

	@Override
	public GradingSuggestion grade(AiUsageContext usageContext, EssayAiGrading grading,
			String studentAnswer, Locale language, AiGradingTier tier) {
		return gradeWithLog(usageContext, grading, studentAnswer, language, tier).suggestion();
	}

	@Override
	public GradingRun gradeWithLog(AiUsageContext usageContext, EssayAiGrading grading,
			String studentAnswer, Locale language, AiGradingTier tier) {
		return gradeWithLog(usageContext, grading, studentAnswer, language, tier,
				aiModule.getEssayGradingSpiId(), aiModule.getEssayGradingModel());
	}

	@Override
	public GradingRun gradeWithLog(AiUsageContext usageContext, EssayAiGrading grading,
			String studentAnswer, Locale language, AiGradingTier tier,
			String spiId, String modelName) {
		AiSPI spi = aiModule.resolveProvider(spiId);
		if (spi == null) {
			throw new AiEssayGradingException("AI provider is not configured or not available.");
		}
		long startTime = System.currentTimeMillis();
		// Declared outside the try so the catch block can read getLogKey() to
		// detect "LLM already returned, post-processing failed" and avoid
		// writing a second usage log row.
		AiLoggingChatModel loggingModel = null;
		try {
			// The admin-configured budget always applies. AiGradingTier.maxTokens() is not
			// used here: since all three tiers were raised to the same 16_384 ceiling, that
			// value no longer differentiates anything and would silently override the
			// admin setting for every caller, because every caller supplies a non-null tier.
			int maxTokens = aiModule.getEssayGradingMaxOutputTokens();
			Duration timeout = Duration.ofSeconds(aiModule.getEssayGradingTimeoutSeconds());
			ChatModel chatModel = spi.buildChatModel(modelName, maxTokens, timeout);

			AiUsageContext ctx = usageContext != null
					? usageContext
					: new AiUsageContext(null, null, null, null, null, null, language);
			loggingModel = new AiLoggingChatModel(chatModel, aiUsageLogDAO,
					spiId, AiFeature.EssayGrading.getType(), ctx);
			AiServices<EssayGradingAiService> builder = AiServices.builder(EssayGradingAiService.class)
					.chatModel(loggingModel);
			EssayGradingAiService service = builder.build();

			String languageName = language == null ? "English" : language.getDisplayLanguage(Locale.ENGLISH);
			Integer difficultyLevel = grading.getDifficulty();
			String difficultyInstruction;
			if (difficultyLevel != null && difficultyLevel <= 1) {
				difficultyInstruction = "This is a QUIZ-MODE item (difficulty 1): the expected answer is very "
						+ "short, a single sentence or even just a few correct keywords is a complete answer. Do "
						+ "NOT expect an essay. Be especially generous and motivating; if the core idea or the "
						+ "right keywords are present, score high (85-100) and celebrate what the student got right.";
			} else if (difficultyLevel != null) {
				difficultyInstruction = "Question difficulty on a 1-5 scale (1 = very easy / quiz, 5 = hardest): "
						+ difficultyLevel + ". Calibrate your expectations to this level (easier questions need only "
						+ "the core idea, harder questions expect fuller coverage) but stay generous and supportive.";
			} else {
				difficultyInstruction = "Assume a moderate difficulty and grade generously and supportively.";
			}
			GradingSuggestion suggestion = service.gradeEssayAnswer(
					nullToEmpty(grading.getReferenceExcerpt()),
					nullToEmpty(grading.getModelAnswer()),
					nullToEmpty(grading.getKeyPointsJson()),
					nullToEmpty(grading.getRubricCriteriaJson()),
					nullToEmpty(grading.getGradingHints()),
					nullToEmpty(studentAnswer),
					languageName,
					difficultyInstruction);
			return new GradingRun(suggestion, loggingModel.getLogKey());
		} catch (AiEssayGradingException e) {
			throw e;
		} catch (Exception e) {
			log.warn("Essay grading call failed: {}", e.getMessage());
			Exception cause = e instanceof AiUsageLoggedException ? (Exception) e.getCause() : e;
			Long existingLogKey = loggingModel == null ? null : loggingModel.getLogKey();
			if (e instanceof AiUsageLoggedException) {
				// AiLoggingChatModel already wrote a FAILED row — nothing to do.
			} else if (existingLogKey != null) {
				// SUCCESS row already exists from AiLoggingChatModel; the failure
				// occurred in post-call structured-output parsing. Flip the row
				// to FAILED instead of writing a second one.
				aiUsageLogDAO.updateAsFailed(existingLogKey, cause);
			} else {
				aiUsageLogDAO.createErrorLog(spiId, modelName, AiFeature.EssayGrading.getType(), usageContext,
						System.currentTimeMillis() - startTime, cause);
			}
			if (AiJsonParseFailure.isJsonParseFailure(cause)) {
				throw new AiEssayResponseTruncatedException(
						"AI response could not be parsed (likely truncated): "
								+ (cause.getMessage() != null ? cause.getMessage() : cause.getClass().getName()),
						existingLogKey, cause);
			}
			throw new AiEssayGradingException(
					cause.getMessage() != null ? cause.getMessage() : cause.getClass().getName(), cause);
		}
	}

	private static String nullToEmpty(String s) {
		return s == null ? "" : s;
	}

}
