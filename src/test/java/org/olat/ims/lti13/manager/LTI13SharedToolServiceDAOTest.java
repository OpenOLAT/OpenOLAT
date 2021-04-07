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
package org.olat.ims.lti13.manager;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.ims.lti13.LTI13Service;
import org.olat.ims.lti13.LTI13Platform;
import org.olat.ims.lti13.LTI13PlatformScope;
import org.olat.ims.lti13.LTI13SharedToolDeployment;
import org.olat.ims.lti13.LTI13SharedToolService;
import org.olat.ims.lti13.LTI13SharedToolService.ServiceType;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 8 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LTI13SharedToolServiceDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LTI13Service lti13Service;
	@Autowired
	private LTI13SharedToolServiceDAO lti13SharedToolServiceDao;
	
	@Test
	public void createServiceEndpoint() {
		String issuer = "https://openolat.edu";
		String clientId = UUID.randomUUID().toString();
		String endpointUrl = "https://openolat.edu/lti13/lineitems/3/lineitem/4?type_id=1";
		LTI13SharedToolDeployment deployment = createSharedToolDeployment(issuer, clientId);
		
		LTI13SharedToolService service = lti13SharedToolServiceDao.createServiceEndpoint("2", ServiceType.lineitem, endpointUrl, deployment);
		dbInstance.commit();
		
		Assert.assertNotNull(service);
		Assert.assertNotNull(service.getCreationDate());
		Assert.assertNotNull(service.getLastModified());
		Assert.assertEquals(ServiceType.lineitem, service.getTypeEnum());
		Assert.assertEquals(endpointUrl, service.getEndpointUrl());
		Assert.assertEquals(deployment, service.getDeployment());
	}
	
	@Test
	public void loadByKey() {
		String issuer = "https://openolat.edu";
		String clientId = UUID.randomUUID().toString();
		String endpointUrl = "https://openolat.edu/lti13/lineitems/8/lineitem/32?type_id=1";
		LTI13SharedToolDeployment deployment = createSharedToolDeployment(issuer, clientId);
		
		LTI13SharedToolService service = lti13SharedToolServiceDao.createServiceEndpoint("8", ServiceType.lineitem, endpointUrl, deployment);
		dbInstance.commit();
		
		LTI13SharedToolService reloadedService = lti13SharedToolServiceDao.loadByKey(service.getKey());
		Assert.assertEquals(service, reloadedService);
		Assert.assertEquals(ServiceType.lineitem, reloadedService.getTypeEnum());
		Assert.assertEquals(endpointUrl, reloadedService.getEndpointUrl());
		Assert.assertEquals(deployment, reloadedService.getDeployment());
		Assert.assertEquals("8", reloadedService.getContextId());
	}
	
	@Test
	public void loadServiceEndpoint() {
		String issuer = "https://openolat.edu";
		String clientId = UUID.randomUUID().toString();
		String endpointUrl = "https://openolat.edu/lti13/lineitems/3/lineitem/4?type_id=1";
		LTI13SharedToolDeployment deployment = createSharedToolDeployment(issuer, clientId);
		
		LTI13SharedToolService service = lti13SharedToolServiceDao.createServiceEndpoint("4", ServiceType.lineitem, endpointUrl, deployment);
		dbInstance.commit();
		
		List<LTI13SharedToolService> reloadedServices = lti13SharedToolServiceDao.loadServiceEndpoint("4", ServiceType.lineitem, endpointUrl, deployment);
		Assert.assertNotNull(reloadedServices);
		Assert.assertEquals(1, reloadedServices.size());
		
		LTI13SharedToolService reloadedService = reloadedServices.get(0);
		Assert.assertEquals(service, reloadedService);
		Assert.assertEquals(ServiceType.lineitem, reloadedService.getTypeEnum());
		Assert.assertEquals(endpointUrl, reloadedService.getEndpointUrl());
		Assert.assertEquals(deployment, reloadedService.getDeployment());
		Assert.assertEquals("4", reloadedService.getContextId());
	}
	
	@Test
	public void getSharedToolServices() {
		String issuer = "https://openolat.edu";
		String clientId = UUID.randomUUID().toString();
		String endpointUrl = "https://openolat.edu/lti13/lineitems/3/lineitem/4?type_id=1";
		LTI13SharedToolDeployment deployment = createSharedToolDeployment(issuer, clientId);
		RepositoryEntry entry = deployment.getEntry();
		
		LTI13SharedToolService service = lti13SharedToolServiceDao.createServiceEndpoint("4", ServiceType.lineitem, endpointUrl, deployment);
		dbInstance.commit();
		
		List<LTI13SharedToolService> reloadedServices = lti13SharedToolServiceDao.getSharedToolServices(entry, ServiceType.lineitem, List.of(issuer));
		Assert.assertNotNull(reloadedServices);
		Assert.assertEquals(1, reloadedServices.size());
		
		LTI13SharedToolService reloadedService = reloadedServices.get(0);
		Assert.assertEquals(service, reloadedService);
		Assert.assertEquals(ServiceType.lineitem, reloadedService.getTypeEnum());
		Assert.assertEquals(endpointUrl, reloadedService.getEndpointUrl());
		Assert.assertEquals(deployment, reloadedService.getDeployment());
		Assert.assertEquals("4", reloadedService.getContextId());
	}
	
	
	private LTI13SharedToolDeployment createSharedToolDeployment(String issuer, String clientId) {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("lti-13-author-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		
		LTI13Platform platform = lti13Service.createTransientPlatform(LTI13PlatformScope.PRIVATE);
		platform.setClientId(clientId);
		platform.setIssuer(issuer);
		platform.setAuthorizationUri(issuer + "/ltideploy/auth");
		platform.setTokenUri(issuer + "/ltideploy/token");
		platform.setJwkSetUri(issuer + "/ltideploy/jwks");
		platform =  lti13Service.updatePlatform(platform);
		
		LTI13SharedToolDeployment deployment = lti13Service.createSharedToolDeployment("4", platform, entry, null);
		dbInstance.commit();
		return deployment;
	}
}
