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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
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
 * proxy settings, timeouts, and connection pooling.
 *
 * Initial date: 2026-03-23<br>
 * @author gnaegi@frentix.com, https://www.frentix.com
 */
public class LangChain4jHttpClient implements HttpClient {

	private final HttpClientService httpClientService;

	public LangChain4jHttpClient(HttpClientService httpClientService) {
		this.httpClientService = httpClientService;
	}

	@Override
	public SuccessfulHttpResponse execute(HttpRequest request) throws HttpException {
		HttpUriRequest apacheRequest = toApacheRequest(request);
		try (CloseableHttpClient httpClient = httpClientService.createHttpClient();
				CloseableHttpResponse response = httpClient.execute(apacheRequest)) {

			int statusCode = response.getStatusLine().getStatusCode();
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
		} catch (HttpException e) {
			throw e;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void execute(HttpRequest request, ServerSentEventParser parser, ServerSentEventListener listener) {
		HttpUriRequest apacheRequest = toApacheRequest(request);
		CompletableFuture.runAsync(() -> {
			try (CloseableHttpClient httpClient = httpClientService.createHttpClient();
					CloseableHttpResponse response = httpClient.execute(apacheRequest)) {

				int statusCode = response.getStatusLine().getStatusCode();
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

	private HttpUriRequest toApacheRequest(HttpRequest request) {
		HttpUriRequest apacheRequest;
		if (request.method() == HttpMethod.POST) {
			HttpPost post = new HttpPost(request.url());
			if (request.body() != null) {
				post.setEntity(new StringEntity(request.body(), "UTF-8"));
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
		return apacheRequest;
	}

	private Map<String, List<String>> extractHeaders(CloseableHttpResponse response) {
		Map<String, List<String>> headers = new HashMap<>();
		for (Header header : response.getAllHeaders()) {
			headers.put(header.getName(), List.of(header.getValue()));
		}
		return headers;
	}
}
