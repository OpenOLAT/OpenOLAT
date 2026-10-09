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
package org.olat.core.util.http;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.websocket.server.HandshakeRequest;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * 
 * Initial date: 9 oct. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class OriginValidatorRequestTest {
	
	private static final String GOOD = "https://olat.example.com";
	private static final String EVIL = "https://evil.com";
	
	private final OriginValidator validator = new OriginValidator(List.of("olat.example.com"), true);
	
	@Test
	public void emptyDomains() {
		OriginValidator emptyValidator = new OriginValidator(List.of(), false);
		Assert.assertFalse(emptyValidator.isAllowed(GOOD));
		
		OriginValidator blankValidator = new OriginValidator(List.of(" ", ""), false);
		Assert.assertFalse(blankValidator.isAllowed(GOOD));
	}
	
	@Test
	public void servletRequestOrigin() {
		Assert.assertTrue(validator.isAllowed(servletRequest(GOOD, null)));
		Assert.assertFalse(validator.isAllowed(servletRequest(EVIL, null)));
	}
	
	@Test
	public void servletRequestOriginPrecedesReferer() {
		Assert.assertFalse(validator.isAllowed(servletRequest(EVIL, GOOD + "/auth/")));
		Assert.assertFalse(validator.isAllowed(servletRequest("null", GOOD + "/auth/")));
		Assert.assertTrue(validator.isAllowed(servletRequest(GOOD, EVIL + "/page")));
	}
	
	@Test
	public void servletRequestRefererFallback() {
		Assert.assertTrue(validator.isAllowed(servletRequest(null, GOOD + "/auth/RepositoryEntry/1234")));
		Assert.assertFalse(validator.isAllowed(servletRequest(null, EVIL + "/page")));
	}
	
	@Test
	public void servletRequestNoHeaders() {
		Assert.assertFalse(validator.isAllowed(servletRequest(null, null)));
	}
	
	@Test
	public void handshakeRequestOrigin() {
		Assert.assertTrue(validator.isAllowed(handshakeRequest(List.of(GOOD), null)));
		Assert.assertFalse(validator.isAllowed(handshakeRequest(List.of(EVIL), null)));
	}
	
	@Test
	public void handshakeRequestFirstOriginOnly() {
		Assert.assertTrue(validator.isAllowed(handshakeRequest(List.of(GOOD, EVIL), null)));
		Assert.assertFalse(validator.isAllowed(handshakeRequest(List.of(EVIL, GOOD), null)));
	}
	
	@Test
	public void handshakeRequestOriginPrecedesReferer() {
		Assert.assertFalse(validator.isAllowed(handshakeRequest(List.of(EVIL), List.of(GOOD + "/auth/"))));
	}
	
	@Test
	public void handshakeRequestIgnoresReferer() {
		// Browsers always send Origin on WebSocket handshakes, no Referer fallback
		Assert.assertFalse(validator.isAllowed(handshakeRequest(null, List.of(GOOD + "/auth/"))));
		Assert.assertFalse(validator.isAllowed(handshakeRequest(List.of(), List.of(GOOD + "/auth/"))));
	}
	
	@Test
	public void handshakeRequestNoHeaders() {
		Assert.assertFalse(validator.isAllowed(handshakeRequest(null, null)));
		Assert.assertFalse(validator.isAllowed(handshakeRequest(List.of(), List.of())));
	}
	
	private static HttpServletRequest servletRequest(String origin, String referer) {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getHeader("Origin")).thenReturn(origin);
		Mockito.when(request.getHeader("Referer")).thenReturn(referer);
		return request;
	}
	
	private static HandshakeRequest handshakeRequest(List<String> origins, List<String> referers) {
		Map<String,List<String>> headers = new HashMap<>();
		if(origins != null) {
			headers.put("Origin", origins);
		}
		if(referers != null) {
			headers.put("Referer", referers);
		}
		HandshakeRequest request = Mockito.mock(HandshakeRequest.class);
		Mockito.when(request.getHeaders()).thenReturn(headers);
		return request;
	}
}
