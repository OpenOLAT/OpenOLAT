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
import java.util.Objects;

import org.olat.core.commons.services.ai.AiSPI;

import dev.langchain4j.model.chat.ChatModel;

/**
 * Immutable holder for a cached ChatModel, keyed by (spiId, modelName, timeout).
 * Used as a volatile field to allow safe, lock-free cache invalidation
 * when the AI configuration changes. The AiServices proxy is built per-call
 * in the service implementations using AiLoggingChatModel wrapper.
 * <p>
 * The {@code timeout} is part of the cache key because the underlying
 * {@link dev.langchain4j.model.openai.OpenAiChatModel} /
 * {@link dev.langchain4j.model.anthropic.AnthropicChatModel} build its
 * LangChain4j {@code HttpClient} at construction time with the configured
 * {@code .timeout(Duration)} value, so two features that want different
 * timeouts cannot share a cached instance.
 *
 * Initial date: 31.03.2026<br>
 *
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
record CachedChatModel(String spiId, String modelName, Duration timeout, ChatModel chatModel) {

	boolean matches(String spiId, String modelName, Duration timeout) {
		return this.spiId.equals(spiId)
				&& this.modelName.equals(modelName)
				&& Objects.equals(this.timeout, timeout);
	}

	static CachedChatModel getOrRefresh(CachedChatModel cached,
			AiSPI spi, String spiId, String modelName, int maxTokens) {
		return getOrRefresh(cached, spi, spiId, modelName, maxTokens, null);
	}

	static CachedChatModel getOrRefresh(CachedChatModel cached,
			AiSPI spi, String spiId, String modelName, int maxTokens, Duration timeout) {
		if (cached != null && cached.matches(spiId, modelName, timeout)) {
			return cached;
		}
		return new CachedChatModel(spiId, modelName, timeout,
				spi.buildChatModel(modelName, maxTokens, timeout));
	}

}
