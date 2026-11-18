/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.core.util.httpclient;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.util.httpclient.filter.FilteredHostException;
import org.springframework.test.util.ReflectionTestUtils;

import com.sun.net.httpserver.HttpServer;

/**
 * Checks the SSRF mitigation end to end with the clients built by the service
 * and a local HTTP server: the protection must be wired in the clients, for the
 * first request and for every redirect.
 *
 * Initial date: 17 nov. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class HttpClientServiceTest {

	private HttpServer server;
	private int port;
	private HttpClientModule module;
	private HttpClientServicempl httpClientService;

	@Before
	public void startServer() throws IOException {
		server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
		server.createContext("/ok", exchange -> {
			byte[] body = "ok".getBytes(StandardCharsets.UTF_8);
			exchange.sendResponseHeaders(200, body.length);
			try(OutputStream out = exchange.getResponseBody()) {
				out.write(body);
			}
		});
		server.createContext("/metadata", exchange -> {
			exchange.getResponseHeaders().add("Location", "http://169.254.169.254/latest/meta-data/");
			exchange.sendResponseHeaders(302, -1);
			exchange.close();
		});
		server.createContext("/endless", exchange -> {
			exchange.sendResponseHeaders(200, 0);// Chunked
			byte[] chunk = new byte[8192];
			try(OutputStream out = exchange.getResponseBody()) {
				while(true) {
					out.write(chunk);
				}
			} catch(IOException e) {
				// Client is gone
			}
		});
		server.createContext("/local", exchange -> {
			exchange.getResponseHeaders().add("Location", "http://127.0.0.1:" + port + "/ok");
			exchange.sendResponseHeaders(302, -1);
			exchange.close();
		});
		server.start();
		port = server.getAddress().getPort();

		module = mock(HttpClientModule.class);
		when(module.getHttpConnectTimeout()).thenReturn(5000);
		when(module.getHttpConnectRequestTimeout()).thenReturn(5000);
		when(module.getHttpSocketTimeout()).thenReturn(5000);
		when(module.isSsrfProtectionEnabled()).thenReturn(true);
		when(module.getSsrfAllowedHostsList()).thenReturn(List.of());
		when(module.getSsrfAllowedAddressesList()).thenReturn(List.of());

		httpClientService = new HttpClientServicempl();
		ReflectionTestUtils.setField(httpClientService, "dbInstance", mock(DB.class));
		ReflectionTestUtils.setField(httpClientService, "httpClientModule", module);
	}

	@After
	public void stopServer() {
		server.stop(0);
	}

	@Test
	public void configuredProfileIsNotFiltered() throws IOException {
		try(CloseableHttpClient client = httpClientService.createHttpClient(ProtectionProfile.CONFIGURED)) {
			Assert.assertEquals("ok", get(client, "http://127.0.0.1:" + port + "/ok"));
		}
	}

	@Test
	public void disabledProtectionIsNotFiltered() throws IOException {
		when(module.isSsrfProtectionEnabled()).thenReturn(false);
		try(CloseableHttpClient client = httpClientService.createHttpClient(ProtectionProfile.USER_PROVIDED)) {
			Assert.assertEquals("ok", get(client, "http://127.0.0.1:" + port + "/ok"));
		}
	}

	/**
	 * A literal address is checked by the interceptor.
	 */
	@Test
	public void literalLoopbackIsRejected() throws IOException {
		try(CloseableHttpClient client = httpClientService.createHttpClient(ProtectionProfile.USER_PROVIDED)) {
			Assert.assertThrows(FilteredHostException.class, () -> get(client, "http://127.0.0.1:" + port + "/ok"));
		}
	}

	/**
	 * A host name is checked by the DNS resolver of the connection manager.
	 */
	@Test
	public void loopbackHostNameIsRejected() throws IOException {
		try(CloseableHttpClient client = httpClientService.createHttpClient(ProtectionProfile.USER_PROVIDED)) {
			Assert.assertThrows(FilteredHostException.class, () -> get(client, "http://localhost:" + port + "/ok"));
		}
	}

	@Test
	public void builderRejectsLoopbackHostName() throws IOException {
		try(CloseableHttpClient client = httpClientService
				.createHttpClientBuilder("localhost", port, "user", "secret", ProtectionProfile.USER_PROVIDED).build()) {
			Assert.assertThrows(FilteredHostException.class, () -> get(client, "http://localhost:" + port + "/ok"));
		}
	}

	@Test
	public void threadSafeClientRejectsLoopbackHostName() throws IOException {
		try(CloseableHttpClient client = httpClientService.createThreadSafeHttpClient(true, ProtectionProfile.USER_PROVIDED)) {
			Assert.assertThrows(FilteredHostException.class, () -> get(client, "http://localhost:" + port + "/ok"));
		}
	}

	@Test
	public void allowedHostIsAccepted() throws IOException {
		when(module.getSsrfAllowedHostsList()).thenReturn(List.of("localhost"));
		try(CloseableHttpClient client = httpClientService.createHttpClient(ProtectionProfile.USER_PROVIDED)) {
			Assert.assertEquals("ok", get(client, "http://localhost:" + port + "/ok"));
		}
	}

	/**
	 * The local server is allowed, but it redirects to the cloud metadata service. The
	 * redirect must be blocked.
	 */
	@Test
	public void redirectToMetadataServiceIsRejected() throws IOException {
		when(module.getSsrfAllowedAddressesList()).thenReturn(List.of("127.0.0.1"));
		try(CloseableHttpClient client = httpClientService.createThreadSafeHttpClient(true, ProtectionProfile.USER_PROVIDED)) {
			Assert.assertEquals("ok", get(client, "http://127.0.0.1:" + port + "/local"));
			FilteredHostException e = Assert.assertThrows(FilteredHostException.class,
					() -> get(client, "http://127.0.0.1:" + port + "/metadata"));
			Assert.assertEquals("169.254.169.254", e.getAddress().getHostAddress());
		}
	}

	@Test
	public void redirectsCanBeDisabled() throws IOException {
		try(CloseableHttpClient client = httpClientService.createThreadSafeHttpClient(false, ProtectionProfile.CONFIGURED)) {
			int status = client.execute(new HttpGet("http://127.0.0.1:" + port + "/local"), response -> {
				EntityUtils.consume(response.getEntity());
				return response.getCode();
			});
			Assert.assertEquals(302, status);
		}
	}

	/**
	 * HttpClient 5 reads the rest of the body when a response is closed (to reuse
	 * the connection), HttpClient 4 aborted the connection. A download which is
	 * stopped early (size limit) must cancel the request, or the close never ends
	 * with an endless body.
	 */
	@Test(timeout = 10000)
	public void cancelStopsAnEndlessDownload() throws IOException {
		try(CloseableHttpClient client = httpClientService.createHttpClient(ProtectionProfile.CONFIGURED)) {
			HttpGet get = new HttpGet("http://127.0.0.1:" + port + "/endless");
			IOException e = Assert.assertThrows(IOException.class, () -> client.execute(get, response -> {
				try(InputStream in = response.getEntity().getContent()) {
					try {
						in.readNBytes(64 * 1024);
						throw new IOException("Download exceeds the limit");
					} catch(IOException ex) {
						get.cancel();
						throw ex;
					}
				}
			}));
			Assert.assertEquals("Download exceeds the limit", e.getMessage());
		}
	}
	
	/**
	 * A parser which stops before the end of the body (as the iCal parser at the end
	 * of the calendar) must not hang on close with an endless body.
	 */
	@Test(timeout = 10000)
	public void cancellingInputStreamStopsAnEndlessDownload() throws IOException {
		try(CloseableHttpClient client = httpClientService.createHttpClient(ProtectionProfile.CONFIGURED)) {
			HttpGet get = new HttpGet("http://127.0.0.1:" + port + "/endless");
			try(ClassicHttpResponse response = client.executeOpen(null, get, null);
					InputStream in = CancellingInputStream.content(response, get)) {
				Assert.assertEquals(1024, in.readNBytes(1024).length);
			}
		}
	}
	
	/**
	 * A fully read body is not affected by the cancel of the request.
	 */
	@Test
	public void cancellingInputStreamReadsFullBody() throws IOException {
		try(CloseableHttpClient client = httpClientService.createHttpClient(ProtectionProfile.CONFIGURED)) {
			for(int i=0; i<3; i++) {
				HttpGet get = new HttpGet("http://127.0.0.1:" + port + "/ok");
				try(ClassicHttpResponse response = client.executeOpen(null, get, null);
						InputStream in = CancellingInputStream.content(response, get)) {
					Assert.assertEquals("ok", new String(in.readAllBytes(), StandardCharsets.UTF_8));
				}
			}
		}
	}
	
	/**
	 * A response handler which cancels the request (bad status, only the headers
	 * are needed...) must still return its value.
	 */
	@Test(timeout = 10000)
	public void cancelInHandlerKeepsTheResult() throws IOException {
		try(CloseableHttpClient client = httpClientService.createHttpClient(ProtectionProfile.CONFIGURED)) {
			for(String path:new String[] { "/ok", "/endless" }) {
				HttpGet get = new HttpGet("http://127.0.0.1:" + port + path);
				Integer status = client.execute(get, response -> {
					get.cancel();
					return response.getCode();
				});
				Assert.assertEquals(path, Integer.valueOf(200), status);
			}
		}
	}
	
	private static String get(CloseableHttpClient client, String url) throws IOException {
		return client.execute(new HttpGet(url), response -> EntityUtils.toString(response.getEntity()));
	}
}
