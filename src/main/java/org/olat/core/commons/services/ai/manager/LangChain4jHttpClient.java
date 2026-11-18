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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.Timeout;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.ai.AiModule;
import org.olat.core.util.httpclient.HttpClientModule;
import org.olat.core.util.httpclient.HttpClientService;

import dev.langchain4j.exception.HttpException;
import dev.langchain4j.http.client.HttpClient;
import dev.langchain4j.http.client.HttpMethod;
import dev.langchain4j.http.client.HttpRequest;
import dev.langchain4j.http.client.SuccessfulHttpResponse;
import dev.langchain4j.http.client.sse.ServerSentEventListener;
import dev.langchain4j.http.client.sse.ServerSentEventParser;

/**
 * LangChain4j {@link HttpClient} implementation that delegates to OpenOlat's
 * {@link HttpClientService} (Apache HttpClient). This ensures all AI provider
 * HTTP calls go through the centrally configured HTTP infrastructure, including
 * proxy settings and connection pooling.
 * <p>
 * The global socket/read timeout defaults to {@code http.connect.socket.timeout}
 * (30s) from {@code HttpClientModule}. A per-call override can be provided via
 * {@link LangChain4jHttpClientBuilder#readTimeout(Duration)} — the LangChain4j
 * ChatModel builders translate their {@code .timeout(Duration)} value into
 * that call, and it is then applied as a per-request
 * {@link RequestConfig#getResponseTimeout()} override so only calls that
 * explicitly raise the ceiling wait longer than the global default. Essay
 * generation, for example, overrides this to 3 minutes.
 *
 * Initial date: 2026-03-23<br>
 * @author gnaegi@frentix.com, https://www.frentix.com
 */
public class LangChain4jHttpClient implements HttpClient {

	private final HttpClientService httpClientService;
	private final Duration readTimeout;

	public LangChain4jHttpClient(HttpClientService httpClientService) {
		this(httpClientService, null);
	}

	public LangChain4jHttpClient(HttpClientService httpClientService, Duration readTimeout) {
		this.httpClientService = httpClientService;
		this.readTimeout = readTimeout;
	}

	@Override
	public SuccessfulHttpResponse execute(HttpRequest request) throws HttpException {
		HttpUriRequestBase apacheRequest = toApacheRequest(request);
		try (CloseableHttpClient httpClient = httpClientService.createHttpClient(AiModule.PROTECTION_PROFILE)) {
			return httpClient.execute(apacheRequest, response -> {
				int statusCode = response.getCode();
				String body = response.getEntity() != null
						? EntityUtils.toString(response.getEntity()) : null;

				if (statusCode < 200 || statusCode >= 300) {
					throw new HttpException(statusCode, body);
				}

				Map<String, List<String>> headers = extractHeaders(response);
				return SuccessfulHttpResponse.builder()
						.statusCode(statusCode)
						.headers(headers)
						.body(body)
						.build();
			});
		} catch (HttpException e) {
			throw e;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void execute(HttpRequest request, ServerSentEventParser parser, ServerSentEventListener listener) {
		HttpUriRequestBase apacheRequest = toApacheRequest(request);
		CompletableFuture.runAsync(() -> {
			try (CloseableHttpClient httpClient = httpClientService.createHttpClient(AiModule.PROTECTION_PROFILE);
					ClassicHttpResponse response = httpClient.executeOpen(null, apacheRequest, null)) {

				int statusCode = response.getCode();
				if (statusCode < 200 || statusCode >= 300) {
					String body = response.getEntity() != null
							? EntityUtils.toString(response.getEntity()) : null;
					listener.onError(new HttpException(statusCode, body));
					return;
				}

				Map<String, List<String>> headers = extractHeaders(response);
				SuccessfulHttpResponse successResponse = SuccessfulHttpResponse.builder()
						.statusCode(statusCode)
						.headers(headers)
						.build();
				listener.onOpen(successResponse);

				try (InputStream inputStream = response.getEntity().getContent()) {
					parser.parse(inputStream, listener);
					listener.onClose();
				}
			} catch (Exception e) {
				listener.onError(e);
			}
		}).exceptionally(throwable -> {
			listener.onError(throwable);
			return null;
		});
	}

	private HttpUriRequestBase toApacheRequest(HttpRequest request) {
		HttpUriRequestBase apacheRequest;
		if (request.method() == HttpMethod.POST) {
			HttpPost post = new HttpPost(request.url());
			if (request.body() != null) {
				post.setEntity(new StringEntity(request.body(), StandardCharsets.UTF_8));
			}
			apacheRequest = post;
		} else if (request.method() == HttpMethod.DELETE) {
			apacheRequest = new HttpDelete(request.url());
		} else {
			apacheRequest = new HttpGet(request.url());
		}
		if (request.headers() != null) {
			request.headers().forEach((name, values) -> {
				if (values != null) {
					values.forEach(value -> apacheRequest.addHeader(name, value));
				}
			});
		}
		if (readTimeout != null) {
			// Per-request override: raise only the socket/read timeout ceiling
			// for this call; preserve the other timeouts from HttpClientModule
			// so the global HTTP defaults stay in effect for connect and
			// connection-request timings.
			HttpClientModule httpClientModule = CoreSpringFactory.getImpl(HttpClientModule.class);
			// The connect timeout is set by the connection manager of the client.
			RequestConfig.Builder perRequestConfig = RequestConfig.custom()
					.setResponseTimeout(Timeout.ofMilliseconds(readTimeout.toMillis()));
			if (httpClientModule.getHttpConnectRequestTimeout() >= 0) {
				perRequestConfig.setConnectionRequestTimeout(Timeout.ofMilliseconds(httpClientModule.getHttpConnectRequestTimeout()));
			}
			apacheRequest.setConfig(perRequestConfig.build());
		}
		return apacheRequest;
	}

	private Map<String, List<String>> extractHeaders(ClassicHttpResponse response) {
		Map<String, List<String>> headers = new HashMap<>();
		for (Header header : response.getHeaders()) {
			headers.put(header.getName(), List.of(header.getValue()));
		}
		return headers;
	}
}
