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

import org.olat.core.CoreSpringFactory;
import org.olat.core.util.httpclient.HttpClientService;

import dev.langchain4j.http.client.HttpClient;
import dev.langchain4j.http.client.HttpClientBuilder;

/**
 * LangChain4j {@link HttpClientBuilder} that creates {@link LangChain4jHttpClient}
 * instances backed by OpenOlat's {@link HttpClientService}. The connect
 * timeout is always taken from OpenOlat's central
 * {@code HttpClientModule} configuration and the builder setter is ignored.
 * The {@code readTimeout} setter, by contrast, is honoured: LangChain4j's
 * ChatModel builders translate their {@code .timeout(Duration)} value into
 * {@link #readTimeout(Duration)}, which is then applied as a per-request
 * socket-timeout override in {@link LangChain4jHttpClient}. When
 * {@code readTimeout} is not set, the socket timeout falls through to the
 * global default (30 s from {@code http.connect.socket.timeout}).
 *
 * Initial date: 2026-03-23<br>
 * @author gnaegi@frentix.com, https://www.frentix.com
 */
public class LangChain4jHttpClientBuilder implements HttpClientBuilder {

	private Duration connectTimeout;
	private Duration readTimeout;

	@Override
	public Duration connectTimeout() {
		return connectTimeout;
	}

	@Override
	public HttpClientBuilder connectTimeout(Duration timeout) {
		this.connectTimeout = timeout;
		return this;
	}

	@Override
	public Duration readTimeout() {
		return readTimeout;
	}

	@Override
	public HttpClientBuilder readTimeout(Duration timeout) {
		this.readTimeout = timeout;
		return this;
	}

	@Override
	public HttpClient build() {
		return new LangChain4jHttpClient(CoreSpringFactory.getImpl(HttpClientService.class), readTimeout);
	}
}
