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

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.edusharing.CreateUsageParameter;
import org.olat.modules.edusharing.EdusharingConversionService;
import org.olat.modules.edusharing.EdusharingModule;
import org.olat.modules.edusharing.EdusharingResponse;
import org.olat.modules.edusharing.EdusharingSecurityService;
import org.olat.modules.edusharing.EdusharingSignature;
import org.olat.modules.edusharing.GetRenderedParameter;
import org.olat.modules.edusharing.NodeIdentifier;
import org.olat.modules.edusharing.Ticket;
import org.olat.modules.edusharing.model.About;
import org.olat.modules.edusharing.model.TicketImpl;
import org.olat.modules.edusharing.model.Usage;
import org.olat.modules.edusharing.model.Usages;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Runs against a real edu-sharing repository. Not part of AllTestsJunit4.
 * Skipped unless these system properties are set:
 * <ul>
 * <li>test.env.edusharing.config: path to the EdusharingModule properties file of an
 * OpenOlat instance that is registered in the repository</li>
 * <li>test.env.edusharing.object: object URL of an existing object, e.g. ccrep://local/{nodeId}</li>
 * <li>optional test.env.edusharing.url: repository URL instead of the one in the config file</li>
 * </ul>
 *
 * Initial date: 13 Nov 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class EdusharingLiveTest extends OlatTestCase {
	
	private static final String COURSE_ID = "OpenOlatLiveTest";
	
	@Autowired
	private EdusharingModule edusharingModule;
	@Autowired
	private EdusharingSecurityService securityService;
	@Autowired
	private EdusharingConversionService conversionService;
	@Autowired
	private EdusharingUserFactory userFactory;
	@Autowired
	private EdusharingRestClient restClient;
	@Autowired
	private EdusharingHttpClient httpClient;
	
	private Identity identity;
	private String objectUrl;
	private NodeIdentifier nodeIdentifier;
	
	@Before
	public void setUp() throws Exception {
		String configPath = System.getProperty("test.env.edusharing.config");
		objectUrl = System.getProperty("test.env.edusharing.object");
		Assume.assumeTrue(StringHelper.containsNonWhitespace(configPath));
		Assume.assumeTrue(StringHelper.containsNonWhitespace(objectUrl));
		
		Properties config = new Properties();
		try (InputStream in = new FileInputStream(configPath)) {
			config.load(in);
		}
		edusharingModule.setBaseUrl(System.getProperty("test.env.edusharing.url", config.getProperty("edusharing.url")));
		edusharingModule.setAppId(config.getProperty("edusharing.app.id"));
		edusharingModule.setSoapKeys(new KeyPair(
				securityService.toPublicKey(config.getProperty("edusharing.soap.public.key")),
				securityService.toPrivateKey(config.getProperty("edusharing.soap.private.key"))));
		edusharingModule.setRepoPublicKeyString(httpClient.getMetadata().getPublicKey());
		
		nodeIdentifier = conversionService.toNodeIdentifier(objectUrl);
		identity = JunitTestHelper.createAndPersistIdentityAsUser("edusharing-live-test");
	}
	
	@Test
	public void shouldDetectRepository() {
		About about = restClient.refreshAbout();
		
		assertThat(about).isNotNull();
		assertThat(about.getVersion().getRepository()).isNotBlank();
	}
	
	@Test
	public void shouldCreateAndValidateTicket() {
		String ticket = restClient.createTicket(identity);
		
		assertThat(ticket).isNotBlank();
		assertThat(restClient.validateTicket(new TicketImpl(ticket))).isTrue();
	}
	
	@Test
	public void shouldCreateRenderAndDeleteUsage() throws Exception {
		Ticket ticket = new TicketImpl(restClient.createTicket(identity));
		String resourceId = UUID.randomUUID().toString();
		String userIdentifier = userFactory.getUserIdentifier(identity);
		CreateUsageParameter parameter = new CreateUsageParameter(resourceId, objectUrl, userIdentifier, COURSE_ID, nodeIdentifier);
		
		restClient.createUsage(ticket, parameter);
		try {
			assertThat(findUsage(ticket, resourceId)).isPresent();
			
			try (EdusharingResponse response = httpClient.getRendered(createRenderParameter(resourceId, userIdentifier))) {
				assertThat(response.getStatus()).isEqualTo(200);
				assertThat(response.hasContent()).isTrue();
			}
		} finally {
			findUsage(ticket, resourceId).ifPresent(usage -> restClient.deleteUsage(ticket, nodeIdentifier, usage.getNodeId()));
		}
		
		assertThat(findUsage(ticket, resourceId)).isEmpty();
	}
	
	private Optional<Usage> findUsage(Ticket ticket, String resourceId) {
		Usages usages = restClient.getUsages(ticket, nodeIdentifier);
		if (usages == null || usages.getUsages() == null) {
			return Optional.empty();
		}
		return Arrays.stream(usages.getUsages())
				.filter(usage -> resourceId.equals(usage.getResourceId()))
				.findFirst();
	}
	
	private GetRenderedParameter createRenderParameter(String resourceId, String userIdentifier) {
		EdusharingSignature signature = securityService.createSignature(null, restClient.getSignatureAlgorithm());
		String encryptedUserIdentifier = securityService.encrypt(edusharingModule.getRepoPublicKey(), userIdentifier);
		GetRenderedParameter parameter = new GetRenderedParameter(signature.getAppId(), nodeIdentifier.getRepositoryId(),
				nodeIdentifier.getNodeId(), resourceId, COURSE_ID, "-1", "de", "de", signature.getSigned(),
				signature.getSignature(), signature.getTimeStamp(), encryptedUserIdentifier, "inline");
		parameter.setSignatureAlgorithm(signature.getAlgorithm());
		return parameter;
	}
	
}
