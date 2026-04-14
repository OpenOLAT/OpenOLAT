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

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.ai.AiUsageLogStatus;
import org.olat.core.commons.services.ai.model.AiUsageContext;
import org.olat.core.commons.services.ai.model.AiUsageLogImpl;
import org.olat.core.logging.Tracing;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.anthropic.AnthropicTokenUsage;
import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.ChatResponseMetadata;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.observability.api.listener.AiServiceCompletedListener;
import dev.langchain4j.service.AiServices;

/**
 * Wrapper for ChatModel that logs all AI requests and responses to the database.
 * Captures context, metadata, and error information for audit and analytics purposes.
 *
 * Initial date: 31.03.2026<br>
 *
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class AiLoggingChatModel implements ChatModel {
	private static final Logger log = Tracing.createLoggerFor(AiLoggingChatModel.class);

	private final ChatModel delegate;
	private final String spiId;
	private final String aiFeature;
	private final AiUsageContext context;
	private final AtomicReference<Long> logKeyRef = new AtomicReference<>();

	private final AiUsageLogDAO usageLogDao;

	public static <T> void configureBuilder(AiServices<T> builder, ChatModel chatModel,
			AiUsageLogDAO usageLogDao, String spiId, String aiFeature, AiUsageContext usageContext) {
		if (usageContext != null) {
			AiLoggingChatModel loggingModel = new AiLoggingChatModel(chatModel, usageLogDao, spiId, aiFeature, usageContext);
			builder.chatModel(loggingModel);
			builder.registerListener((AiServiceCompletedListener) event -> {
				var ctx = event.invocationContext();
				if (ctx != null) {
					String invocationId = ctx.invocationId() != null ? ctx.invocationId().toString() : null;
					usageLogDao.updateInvocationFields(loggingModel.getLogKey(), invocationId, ctx.interfaceName(), ctx.methodName());
				}
			});
		} else {
			builder.chatModel(chatModel);
		}
	}

	public Long getLogKey() {
		return logKeyRef.get();
	}

	public AiLoggingChatModel(ChatModel delegate, AiUsageLogDAO usageLogDao, String spiId, String aiFeature, AiUsageContext context) {
		this.delegate = delegate;
		this.spiId = spiId;
		this.aiFeature = aiFeature;
		this.context = context;
		this.usageLogDao = usageLogDao;
	}

	@Override
	public Set<Capability> supportedCapabilities() {
		return delegate.supportedCapabilities();
	}

	@Override
	public ChatResponse chat(ChatRequest request) {
		if (log.isDebugEnabled()) {
			log.debug("AI request [spi={}, feature={}]: {}", spiId, aiFeature, request.messages());
		}
		long startTime = System.currentTimeMillis();
		try {
			ChatResponse response = delegate.chat(request);
			if (log.isDebugEnabled()) {
				log.debug("AI response [spi={}, feature={}]: {}", spiId, aiFeature, response.aiMessage());
			}
			AiUsageLogImpl usageLog = buildLog(request, response, null, startTime);
			usageLogDao.create(usageLog);
			logKeyRef.set(usageLog.getKey());
			return response;
		} catch (Exception e) {
			AiUsageLogImpl usageLog = buildLog(request, null, e, startTime);
			usageLogDao.create(usageLog);
			logKeyRef.set(usageLog.getKey());
			throw new AiUsageLoggedException(e);
		}
	}

	private AiUsageLogImpl buildLog(ChatRequest request, ChatResponse response, Exception error, long startTime) {
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

		long duration = System.currentTimeMillis() - startTime;
		log.setDurationMillis(duration);

		ChatRequestParameters params = delegate.defaultRequestParameters().overrideWith(request.parameters());
		log.setModelProvider(spiId);
		log.setRequestModel(params.modelName());
		if (params.temperature() != null) {
			log.setRequestTemperature(Double.valueOf(params.temperature()));
		}
		if (params.topP() != null) {
			log.setRequestTopP(Double.valueOf(params.topP()));
		}
		if (params.maxOutputTokens() != null) {
			log.setRequestMaxOutputTokens(Long.valueOf(params.maxOutputTokens()));
		}

		log.setRequestNumMessages(Long.valueOf(request.messages().size()));
		long textLength = 0;
		for (ChatMessage msg : request.messages()) {
			if (msg instanceof SystemMessage sm) {
				textLength += sm.text().length();
			} else if (msg instanceof UserMessage um) {
				for (Content content : um.contents()) {
					if (content instanceof TextContent tc) {
						textLength += tc.text().length();
					}
				}
			} else if (msg instanceof AiMessage am && am.text() != null) {
				textLength += am.text().length();
			}
		}
		log.setRequestTextLength(Long.valueOf(textLength));

		if (error != null) {
			log.setStatus(AiUsageLogStatus.FAILED);
			log.setErrorCode(error.getClass().getSimpleName());
			log.setErrorMessage(error.getMessage());
		} else if (response != null) {
			log.setStatus(AiUsageLogStatus.SUCCESS);
			ChatResponseMetadata metadata = response.metadata();
			log.setResponseId(metadata.id());
			log.setResponseModel(metadata.modelName());
			if (metadata.finishReason() != null) {
				log.setResponseFinishReason(metadata.finishReason().toString());
			}

			TokenUsage tokenUsage = metadata.tokenUsage();
			if (tokenUsage != null) {
				log.setInputTokens(Long.valueOf(tokenUsage.inputTokenCount()));
				log.setOutputTokens(Long.valueOf(tokenUsage.outputTokenCount()));
				log.setTotalTokens(Long.valueOf(tokenUsage.totalTokenCount()));

				if (tokenUsage instanceof dev.langchain4j.model.openai.OpenAiTokenUsage openAiTokenUsage) {
					if (openAiTokenUsage.inputTokensDetails() != null && openAiTokenUsage.inputTokensDetails().cachedTokens() != null) {
						log.setCachedInputTokens(Long.valueOf(openAiTokenUsage.inputTokensDetails().cachedTokens()));
					}
					if (openAiTokenUsage.outputTokensDetails() != null && openAiTokenUsage.outputTokensDetails().reasoningTokens() != null) {
						log.setReasoningTokens(Long.valueOf(openAiTokenUsage.outputTokensDetails().reasoningTokens()));
					}
				} else if (tokenUsage instanceof AnthropicTokenUsage anthropicTokenUsage) {
					if (anthropicTokenUsage.cacheReadInputTokens() != null) {
						log.setCachedInputTokens(Long.valueOf(anthropicTokenUsage.cacheReadInputTokens()));
					}
					if (anthropicTokenUsage.cacheCreationInputTokens() != null) {
						log.setCacheCreationInputTokens(Long.valueOf(anthropicTokenUsage.cacheCreationInputTokens()));
					}
				}
			}
		}

		return log;
	}
}
