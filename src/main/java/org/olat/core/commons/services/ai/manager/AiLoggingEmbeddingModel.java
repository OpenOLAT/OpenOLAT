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

import java.util.List;

import org.olat.core.commons.services.ai.AiUsageLogStatus;
import org.olat.core.commons.services.ai.model.AiUsageContext;
import org.olat.core.commons.services.ai.model.AiUsageLogImpl;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;

/**
 * Wrapper for EmbeddingModel that logs all AI embedding requests to the database.
 *
 * Initial date: 17.06.2026<br>
 *
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class AiLoggingEmbeddingModel implements EmbeddingModel {

	private final EmbeddingModel delegate;
	private final AiUsageLogDAO usageLogDao;
	private final String spiId;
	private final String modelName;
	private final String aiFeature;
	private final AiUsageContext context;

	public AiLoggingEmbeddingModel(EmbeddingModel delegate, AiUsageLogDAO usageLogDao, String spiId,
			String modelName, String aiFeature, AiUsageContext context) {
		this.delegate = delegate;
		this.usageLogDao = usageLogDao;
		this.spiId = spiId;
		this.modelName = modelName;
		this.aiFeature = aiFeature;
		this.context = context;
	}

	@Override
	public String modelName() {
		return modelName;
	}

	@Override
	public Response<List<Embedding>> embedAll(List<TextSegment> textSegments) {
		long startTime = System.currentTimeMillis();
		try {
			Response<List<Embedding>> response = delegate.embedAll(textSegments);
			usageLogDao.create(buildLog(textSegments, response, null, startTime));
			return response;
		} catch (Exception e) {
			usageLogDao.create(buildLog(textSegments, null, e, startTime));
			throw new AiUsageLoggedException(e);
		}
	}

	private AiUsageLogImpl buildLog(List<TextSegment> textSegments, Response<List<Embedding>> response,
			Exception error, long startTime) {
		AiUsageLogImpl log = new AiUsageLogImpl();
		log.setAiFeature(aiFeature);
		log.setUsageContextType(context.usageContextType());
		log.setUsageContextId(context.usageContextId());
		log.setIdentity(context.identity());
		log.setResourceType(context.resourceType());
		log.setResourceId(context.resourceId());
		log.setResourceSubId(context.resourceSubId());
		if (context.locale() != null) {
			log.setLocale(context.locale().toString());
		}
		log.setDurationMillis(System.currentTimeMillis() - startTime);
		log.setModelProvider(spiId);
		log.setRequestModel(modelName);
		log.setRequestNumMessages(Long.valueOf(textSegments.size()));
		long textLength = 0;
		for (TextSegment seg : textSegments) {
			if (seg.text() != null) {
				textLength += seg.text().length();
			}
		}
		log.setRequestTextLength(Long.valueOf(textLength));
		if (error != null) {
			log.setStatus(AiUsageLogStatus.FAILED);
			log.setErrorCode(error.getClass().getSimpleName());
			log.setErrorMessage(error.getMessage());
		} else {
			log.setStatus(AiUsageLogStatus.SUCCESS);
			if (response != null) {
				TokenUsage tokenUsage = response.tokenUsage();
				if (tokenUsage != null) {
					if (tokenUsage.inputTokenCount() != null) {
						log.setInputTokens(Long.valueOf(tokenUsage.inputTokenCount()));
					}
					if (tokenUsage.outputTokenCount() != null) {
						log.setOutputTokens(Long.valueOf(tokenUsage.outputTokenCount()));
					}
					if (tokenUsage.totalTokenCount() != null) {
						log.setTotalTokens(Long.valueOf(tokenUsage.totalTokenCount()));
					}
				}
			}
		}
		return log;
	}
}
