/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.restapi.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.time.Instant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.logging.Tracing;
import org.olat.core.util.ratelimit.manager.RequestRateLimiterImpl;
import org.olat.restapi.RestModule;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Drives the rate limit of the filter with mock servlet objects. The context
 * of the URLs in JUnit mode is /olat, /olat/api is an open URL and limited
 * by the IP of the client. Every test uses its own IP.
 *
 * Initial date: 23 sept. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class RestApiRateLimitFilterTest extends OlatTestCase {

	private static final Logger log = Tracing.createLoggerFor(RestApiRateLimitFilterTest.class);
	
	private static final int SC_TOO_MANY_REQUESTS = 429;
	private static final String OPEN_URI = "/olat/api/openapi.json";
	private static final String PING_URI = "/olat/ping";

	@Autowired
	private RestModule restModule;

	@Test
	public void burstOverLimit() throws Exception {
		int currentAnonymousLimit = restModule.getRateLimitAnonymousRequestsPerMinute();
		restModule.setRateLimitAnonymousRequestsPerMinute(3);
		waitMessageAreConsumed();
		waitForFreshWindow();
		
		FilterChain chain = mock(FilterChain.class);
		RestApiLoginFilter filter = new RestApiLoginFilter();
		String ip = "10.99.1.1";

		for(int i=0; i<3; i++) {
			MockHttpServletResponse response = doGet(filter, chain, OPEN_URI, ip);
			Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
			Assert.assertEquals("3", response.getHeader(RestApiLoginFilter.HEADER_RATELIMIT_LIMIT));
			Assert.assertEquals(Integer.toString(2 - i), response.getHeader(RestApiLoginFilter.HEADER_RATELIMIT_REMAINING));
		}
		verify(chain, times(3)).doFilter(any(), any());

		MockHttpServletResponse rejected = doGet(filter, chain, OPEN_URI, ip);
		Assert.assertEquals(SC_TOO_MANY_REQUESTS, rejected.getStatus());
		Assert.assertEquals("3", rejected.getHeader(RestApiLoginFilter.HEADER_RATELIMIT_LIMIT));
		Assert.assertEquals("0", rejected.getHeader(RestApiLoginFilter.HEADER_RATELIMIT_REMAINING));
		Assertions.assertThat(rejected.getHeader(RestApiLoginFilter.HEADER_RATELIMIT_RESET)).matches("\\d+");
		Assertions.assertThat(Integer.parseInt(rejected.getHeader(RestApiLoginFilter.HEADER_RETRY_AFTER)))
			.isBetween(1, RequestRateLimiterImpl.WINDOW_SECONDS);
		Assertions.assertThat(rejected.getContentType()).startsWith("application/json");
		Assertions.assertThat(rejected.getContentAsString()).contains("\"code\":429");
		// the rejected request never reaches the chain
		verify(chain, times(3)).doFilter(any(), any());
		
		restModule.setRateLimitAnonymousRequestsPerMinute(currentAnonymousLimit);
		waitMessageAreConsumed();
	}

	@Test
	public void pingIsExempt() throws Exception {
		FilterChain chain = mock(FilterChain.class);
		RestApiLoginFilter filter = new RestApiLoginFilter();
		for(int i=0; i<6; i++) {
			MockHttpServletResponse response = doGet(filter, chain, PING_URI, "10.99.1.2");
			Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
			Assert.assertNull(response.getHeader(RestApiLoginFilter.HEADER_RATELIMIT_LIMIT));
		}
		verify(chain, times(6)).doFilter(any(), any());
	}

	@Test
	public void rateLimitDisabled() throws Exception {
		boolean currentRateLimitEnabled = restModule.isRateLimitEnabled();
		restModule.setRateLimitEnabled(false);
		waitMessageAreConsumed();

		FilterChain chain = mock(FilterChain.class);
		RestApiLoginFilter filter = new RestApiLoginFilter();
		for(int i=0; i<6; i++) {
			MockHttpServletResponse response = doGet(filter, chain, OPEN_URI, "10.99.1.3");
			Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
			Assert.assertNull(response.getHeader(RestApiLoginFilter.HEADER_RATELIMIT_LIMIT));
		}
		verify(chain, times(6)).doFilter(any(), any());
		
		restModule.setRateLimitEnabled(currentRateLimitEnabled);
		waitMessageAreConsumed();
	}

	@Test
	public void separateIps() throws Exception {
		int currentAnonymousLimit = restModule.getRateLimitAnonymousRequestsPerMinute();
		restModule.setRateLimitAnonymousRequestsPerMinute(3);
		waitMessageAreConsumed();
		waitForFreshWindow();
		
		FilterChain chain = mock(FilterChain.class);
		RestApiLoginFilter filter = new RestApiLoginFilter();
		for(int i=0; i<3; i++) {
			doGet(filter, chain, OPEN_URI, "10.99.1.4");
		}
		Assert.assertEquals(SC_TOO_MANY_REQUESTS, doGet(filter, chain, OPEN_URI, "10.99.1.4").getStatus());
		Assert.assertEquals(HttpServletResponse.SC_OK, doGet(filter, chain, OPEN_URI, "10.99.1.5").getStatus());
		
		restModule.setRateLimitAnonymousRequestsPerMinute(currentAnonymousLimit);
		waitMessageAreConsumed();
	}

	@Test
	public void parallelRequests() throws Exception {
		int currentParallelLimit = restModule.getRateLimitMaxParallel();
		int currentAnonymousLimit = restModule.getRateLimitAnonymousRequestsPerMinute();
		restModule.setRateLimitMaxParallel(1);
		restModule.setRateLimitAnonymousRequestsPerMinute(100);
		waitMessageAreConsumed();
		waitForFreshWindow();

		RestApiLoginFilter filter = new RestApiLoginFilter();
		String ip = "10.99.1.6";

		// the chain keeps the slot while a second request of the same IP is sent by an other thread
		MockHttpServletResponse[] inner = new MockHttpServletResponse[1];
		FilterChain blockingChain = (req, res) -> {
			Thread parallel = new Thread(() -> {
				try {
					inner[0] = doGet(filter, mock(FilterChain.class), OPEN_URI, ip);
				} catch (IOException | ServletException e) {
					log.error("", e);
				}
			});
			parallel.start();
			try {
				parallel.join(10000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		};
		MockHttpServletResponse outer = doGet(filter, blockingChain, OPEN_URI, ip);

		Assert.assertEquals(HttpServletResponse.SC_OK, outer.getStatus());
		Assert.assertNotNull(inner[0]);
		Assert.assertEquals(SC_TOO_MANY_REQUESTS, inner[0].getStatus());
		Assert.assertEquals("1", inner[0].getHeader(RestApiLoginFilter.HEADER_RETRY_AFTER));
		Assert.assertNull(inner[0].getHeader(RestApiLoginFilter.HEADER_RATELIMIT_LIMIT));

		// the slot is released after the request
		Assert.assertEquals(HttpServletResponse.SC_OK, doGet(filter, mock(FilterChain.class), OPEN_URI, ip).getStatus());
		
		restModule.setRateLimitMaxParallel(currentParallelLimit);
		restModule.setRateLimitAnonymousRequestsPerMinute(currentAnonymousLimit);
		waitMessageAreConsumed();
	}

	private MockHttpServletResponse doGet(RestApiLoginFilter filter, FilterChain chain, String uri, String remoteAddr)
	throws IOException, ServletException {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
		request.setRemoteAddr(remoteAddr);
		request.addHeader("User-Agent", "JUnit");
		MockHttpServletResponse response = new MockHttpServletResponse();
		filter.doFilter(request, response, chain);
		return response;
	}

	/**
	 * Prevents a burst to be split by the start of a new window.
	 */
	private void waitForFreshWindow() {
		long secondInWindow = Instant.now().getEpochSecond() % RequestRateLimiterImpl.WINDOW_SECONDS;
		if(secondInWindow >= RequestRateLimiterImpl.WINDOW_SECONDS - 5) {
			sleep((int)(RequestRateLimiterImpl.WINDOW_SECONDS - secondInWindow + 1) * 1000);
		}
	}
}
