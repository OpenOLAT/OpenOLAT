/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
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
package org.olat.modules.edusharing.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.olat.modules.edusharing.EdusharingModule.PROTECTION_PROFILE;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicStatusLine;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.olat.core.id.Identity;
import org.olat.core.util.httpclient.HttpClientService;
import org.olat.modules.edusharing.EdusharingModule;
import org.olat.modules.edusharing.EdusharingSecurityService;
import org.olat.modules.edusharing.Ticket;
import org.olat.modules.edusharing.model.EdusharingSignatureImpl;
import org.olat.modules.edusharing.model.NodeIdentifierImpl;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * Initial date: 13 Nov 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class EdusharingRestClientTest {
	
	private static final String ABOUT_9 = """
			{"plugins":[],"features":[],"themesUrl":"themes","lastCacheUpdate":0,
			"version":{"repository":"9.1","renderservice":"9.1","major":1,"minor":1},"services":[]}
			""";
	private static final String ABOUT_10 = """
			{"plugins":[],"features":[],"themesUrl":"themes","lastCacheUpdate":0,
			"version":{"repository":"10.0","renderservice":"10.0","major":1,"minor":1},"services":[],
			"renderingService2":null,"defaultSignatureAlgorithm":"SHA512withRSA",
			"signatureAlgorithms":["SHA224withRSA","SHA256withRSA","SHA384withRSA","SHA512withRSA"]}
			""";
	
	@Mock
	private EdusharingModule edusharingModuleMock;
	@Mock
	private EdusharingSecurityService edusharingSecurityServiceMock;
	@Mock
	private EdusharingUserFactory edusharingUserFactoryMock;
	@Mock
	private HttpClientService httpClientServiceMock;
	@Mock
	private CloseableHttpClient httpClientMock;
	@Mock
	private CloseableHttpResponse httpResponseMock;
	@Mock
	private CloseableHttpResponse aboutResponseMock;
	
	@InjectMocks
	private EdusharingRestClient sut;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.openMocks(this);
		when(edusharingModuleMock.getBaseUrl()).thenReturn("https://repo.example.org/edu-sharing/");
		when(edusharingSecurityServiceMock.createSignature(any(), any())).thenAnswer(invocation ->
				new EdusharingSignatureImpl("openolat", "123", "openolat123", "sig", invocation.getArgument(1)));
		when(httpClientServiceMock.createHttpClient(PROTECTION_PROFILE)).thenReturn(httpClientMock);
		when(httpClientMock.execute(any(HttpUriRequest.class))).thenAnswer(invocation ->
				isAbout(invocation.getArgument(0, HttpUriRequest.class))? aboutResponseMock: httpResponseMock);
		when(httpResponseMock.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK"));
		when(aboutResponseMock.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK"));
		when(aboutResponseMock.getEntity()).thenReturn(new StringEntity(ABOUT_9));
	}
	
	@Test
	public void shouldSignCreateTicket() throws Exception {
		Identity identity = mock(Identity.class);
		when(edusharingUserFactoryMock.getUserIdentifier(identity)).thenReturn("user");
		when(edusharingUserFactoryMock.getUserProfile(identity)).thenReturn(new ObjectMapper().createObjectNode());
		when(httpResponseMock.getEntity()).thenReturn(new StringEntity("{\"ticket\":\"t\"}"));
		
		String ticket = sut.createTicket(identity);
		
		assertThat(ticket).isEqualTo("t");
		verify(edusharingSecurityServiceMock).createSignature("user", EdusharingSecurityService.SIGNATURE_SHA1);
		assertLegacySignatureHeaders(executedRequest());
	}
	
	@Test
	public void shouldSignDeleteUsage() throws Exception {
		Ticket ticket = mock(Ticket.class);
		when(ticket.getTooken()).thenReturn("t");
		
		sut.deleteUsage(ticket, new NodeIdentifierImpl("repo", "node"), "usage");
		
		verify(edusharingSecurityServiceMock).createSignature("nodeusage", EdusharingSecurityService.SIGNATURE_SHA1);
		HttpUriRequest request = executedRequest();
		assertLegacySignatureHeaders(request);
		assertThat(request.getFirstHeader("Authorization").getValue()).isEqualTo("EDU-TICKET t");
	}
	
	@Test
	public void shouldSignWithSHA512OnEdusharing10() throws Exception {
		when(aboutResponseMock.getEntity()).thenReturn(new StringEntity(ABOUT_10));
		Ticket ticket = mock(Ticket.class);
		when(ticket.getTooken()).thenReturn("t");
		
		sut.deleteUsage(ticket, new NodeIdentifierImpl("repo", "node"), "usage");
		
		verify(edusharingSecurityServiceMock).createSignature("nodeusage", EdusharingSecurityService.SIGNATURE_SHA512);
		HttpUriRequest request = executedRequest();
		assertThat(request.getFirstHeader("X-Edu-App-Id").getValue()).isEqualTo("openolat");
		assertThat(request.getFirstHeader("X-Edu-App-SignedAlg").getValue()).isEqualTo("SHA512withRSA");
	}
	
	@Test
	public void shouldDetectEdusharing9() throws Exception {
		assertThat(sut.getAbout().getVersion().getRepository()).isEqualTo("9.1");
		assertThat(sut.getSignatureAlgorithm()).isEqualTo(EdusharingSecurityService.SIGNATURE_SHA1);
		assertThat(executedRequests().get(0).getURI()).hasToString("https://repo.example.org/edu-sharing/rest/_about");
	}
	
	@Test
	public void shouldDetectEdusharing10() throws Exception {
		when(aboutResponseMock.getEntity()).thenReturn(new StringEntity(ABOUT_10));
		
		assertThat(sut.getAbout().getVersion().getRepository()).isEqualTo("10.0");
		assertThat(sut.getSignatureAlgorithm()).isEqualTo(EdusharingSecurityService.SIGNATURE_SHA512);
	}
	
	@Test
	public void shouldCacheDetection() throws Exception {
		when(aboutResponseMock.getEntity()).thenReturn(new StringEntity(ABOUT_10));
		
		sut.getSignatureAlgorithm();
		sut.getSignatureAlgorithm();
		
		verify(httpClientMock, times(1)).execute(any(HttpUriRequest.class));
	}
	
	@Test
	public void shouldNotCacheFailedDetection() throws Exception {
		when(httpClientMock.execute(any(HttpUriRequest.class))).thenThrow(new IOException()).thenReturn(aboutResponseMock);
		when(aboutResponseMock.getEntity()).thenReturn(new StringEntity(ABOUT_10));
		
		assertThat(sut.getSignatureAlgorithm()).isEqualTo(EdusharingSecurityService.SIGNATURE_SHA1);
		assertThat(sut.getSignatureAlgorithm()).isEqualTo(EdusharingSecurityService.SIGNATURE_SHA1);
		verify(httpClientMock, times(1)).execute(any(HttpUriRequest.class));
		
		sut.aboutRetryPauseMillis = 0;
		assertThat(sut.getSignatureAlgorithm()).isEqualTo(EdusharingSecurityService.SIGNATURE_SHA512);
		verify(httpClientMock, times(2)).execute(any(HttpUriRequest.class));
	}
	
	@Test
	public void shouldNotDetectOnHttpError() throws Exception {
		when(aboutResponseMock.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, 503, "Service Unavailable"));
		when(aboutResponseMock.getEntity()).thenReturn(new StringEntity(""));
		
		assertThat(sut.getAbout()).isNull();
		assertThat(sut.getSignatureAlgorithm()).isEqualTo(EdusharingSecurityService.SIGNATURE_SHA1);
	}
	
	@Test
	public void shouldRefreshDetection() throws Exception {
		when(aboutResponseMock.getEntity()).thenReturn(new StringEntity(ABOUT_9), new StringEntity(ABOUT_10));
		
		assertThat(sut.getSignatureAlgorithm()).isEqualTo(EdusharingSecurityService.SIGNATURE_SHA1);
		sut.refreshAbout();
		
		assertThat(sut.getSignatureAlgorithm()).isEqualTo(EdusharingSecurityService.SIGNATURE_SHA512);
	}
	
	private boolean isAbout(HttpUriRequest request) {
		return request.getURI().getPath().endsWith("/rest/_about");
	}
	
	private List<HttpUriRequest> executedRequests() throws Exception {
		ArgumentCaptor<HttpUriRequest> captor = ArgumentCaptor.forClass(HttpUriRequest.class);
		verify(httpClientMock, atLeastOnce()).execute(captor.capture());
		return captor.getAllValues();
	}
	
	private HttpUriRequest executedRequest() throws Exception {
		List<HttpUriRequest> requests = executedRequests().stream().filter(request -> !isAbout(request)).toList();
		assertThat(requests).hasSize(1);
		return requests.get(0);
	}
	
	private void assertLegacySignatureHeaders(HttpUriRequest request) {
		assertThat(request.getFirstHeader("X-Edu-App-Id").getValue()).isEqualTo("openolat");
		assertThat(request.getFirstHeader("X-Edu-App-Sig").getValue()).isEqualTo("sig");
		assertThat(request.getFirstHeader("X-Edu-App-Signed").getValue()).isEqualTo("openolat123");
		assertThat(request.getFirstHeader("X-Edu-App-Ts").getValue()).isEqualTo("123");
		assertThat(request.getFirstHeader("X-Edu-App-SignedAlg")).isNull();
	}
	
}
