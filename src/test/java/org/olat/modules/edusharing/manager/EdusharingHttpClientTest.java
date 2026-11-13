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
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.olat.modules.edusharing.EdusharingModule;
import org.olat.modules.edusharing.EdusharingSecurityService;
import org.olat.modules.edusharing.GetRenderedParameter;

/**
 *
 * Initial date: 13 Nov 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class EdusharingHttpClientTest {
	
	@Mock
	private EdusharingModule edusharingModuleMock;
	
	@InjectMocks
	private EdusharingHttpClient sut;
	
	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		when(edusharingModuleMock.getBaseUrl()).thenReturn("https://repo.example.org/edu-sharing/");
	}
	
	@Test
	public void shouldBuildInlineRenderUrl() {
		GetRenderedParameter parameter = createParameter("inline");
		
		String url = sut.getRenderUrl(parameter);
		
		assertThat(url).startsWith("https://repo.example.org/edu-sharing/renderingproxy?app_id=openolat&rep_id=repo&obj_id=node");
		assertThat(url).contains("&signed=openolat123", "&sig=a%2Bb%3D", "&ts=123", "&u=user", "&display=inline");
		assertThat(url).doesNotContain("signedAlg", "&ticket=");
	}
	
	@Test
	public void shouldBuildWindowRenderUrl() {
		GetRenderedParameter parameter = createParameter("window");
		parameter.setEncryptedTicket("ticket");
		
		String url = sut.getRenderUrl(parameter);
		
		assertThat(url).contains("&signed=openolat123", "&sig=a%2Bb%3D", "&ts=123", "&display=window", "&ticket=ticket");
		assertThat(url).doesNotContain("signedAlg");
	}
	
	@Test
	public void shouldAddSignatureAlgorithmForEdusharing10() {
		GetRenderedParameter parameter = createParameter("inline");
		parameter.setSignatureAlgorithm(EdusharingSecurityService.SIGNATURE_SHA512);
		
		String url = sut.getRenderUrl(parameter);
		
		assertThat(url).contains("&signedAlg=SHA512withRSA");
	}
	
	@Test
	public void shouldNotAddLegacySignatureAlgorithm() {
		GetRenderedParameter parameter = createParameter("window");
		parameter.setSignatureAlgorithm(EdusharingSecurityService.SIGNATURE_SHA1);
		
		String url = sut.getRenderUrl(parameter);
		
		assertThat(url).doesNotContain("signedAlg");
	}
	
	private GetRenderedParameter createParameter(String displayMode) {
		return new GetRenderedParameter("openolat", "repo", "node", "resource", "course", "1.0", "de", "de",
				"openolat123", "a+b=", "123", "user", displayMode);
	}
	
}
