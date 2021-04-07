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
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.ims.lti13.LTI13Platform;
import org.olat.ims.lti13.LTI13PlatformScope;
import org.olat.ims.lti13.LTI13Service;
import org.olat.ims.lti13.LTI13SharedToolDeployment;
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
public class LTI13SharedToolDeploymentDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LTI13Service lti13Service;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private LTI13SharedToolDeploymentDAO lti13SharedToolDeploymentDao;
	
	
	@Test
	public void createSharedToolDeployment() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("lti-13-author-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		
		String clientId = UUID.randomUUID().toString();
		String issuer = "https://cuberai.openolat.org";
		LTI13Platform platform = createPlatform(issuer, clientId);
		
		String deploymentId = UUID.randomUUID().toString();
		LTI13SharedToolDeployment deployment = lti13SharedToolDeploymentDao.createDeployment(deploymentId, platform, entry, null);
		dbInstance.commit();
		
		Assert.assertNotNull(deployment);
		Assert.assertNotNull(deployment.getCreationDate());
		Assert.assertNotNull(deployment.getLastModified());
		Assert.assertEquals(platform, deployment.getPlatform());
		Assert.assertEquals(deploymentId, deployment.getDeploymentId());
	}
	
	@Test
	public void getSharedToolDeploymentByDeploymentId() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("lti-13-author-2");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		
		String clientId = UUID.randomUUID().toString();
		String issuer = "https://cuberai.openolat.org";
		LTI13Platform platform = createPlatform(issuer, clientId);
		
		String deploymentId = UUID.randomUUID().toString();
		LTI13SharedToolDeployment deployment = lti13SharedToolDeploymentDao.createDeployment(deploymentId, platform, entry, null);
		dbInstance.commit();
		
		List<LTI13SharedToolDeployment> foundDeployments = lti13SharedToolDeploymentDao.getSharedToolDeployment(deploymentId, platform);
		Assert.assertNotNull(foundDeployments);
		Assert.assertEquals(1, foundDeployments.size());
		LTI13SharedToolDeployment foundDeployment = foundDeployments.get(0);
		
		Assert.assertNotNull(foundDeployment);
		Assert.assertEquals(deployment, foundDeployment);
		Assert.assertEquals(platform, foundDeployment.getPlatform());
		Assert.assertEquals(deploymentId, foundDeployment.getDeploymentId());
	}
	
	@Test
	public void getSharedToolDeploymentByRepositoryEntry() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("lti-13-author-3");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		
		String clientId = UUID.randomUUID().toString();
		String issuer = "https://z21.openolat.org";
		LTI13Platform platform = createPlatform(issuer, clientId);

		String deploymentId = UUID.randomUUID().toString();
		LTI13SharedToolDeployment deployment = lti13SharedToolDeploymentDao.createDeployment(deploymentId, platform, entry, null);
		dbInstance.commit();
		
		List<LTI13SharedToolDeployment> foundDeployments = lti13SharedToolDeploymentDao.getSharedToolDeployment(entry);
		Assert.assertNotNull(foundDeployments);
		Assert.assertEquals(1, foundDeployments.size());
		LTI13SharedToolDeployment foundDeployment = foundDeployments.get(0);
		
		Assert.assertNotNull(foundDeployment);
		Assert.assertEquals(deployment, foundDeployment);
		Assert.assertEquals(platform, foundDeployment.getPlatform());
		Assert.assertEquals(entry, foundDeployment.getEntry());
	}

	@Test
	public void getSharedToolDeploymentByBusinessGroup() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndAuthor("lti-13-coach-4");
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(coach,
				"LTI service group", "Group with LTI 1.3 for z21", -1, -1, false, false, null);

		String clientId = UUID.randomUUID().toString();
		String issuer = "https://z21.openolat.org";
		LTI13Platform platform = createPlatform(issuer, clientId);

		String deploymentId = UUID.randomUUID().toString();
		LTI13SharedToolDeployment deployment = lti13Service
				.createSharedToolDeployment(deploymentId, platform, null, businessGroup);
		dbInstance.commit();
	
		List<LTI13SharedToolDeployment> foundDeployments = lti13SharedToolDeploymentDao.getSharedToolDeployment(businessGroup);
		Assert.assertNotNull(foundDeployments);
		Assert.assertEquals(1, foundDeployments.size());
		LTI13SharedToolDeployment foundDeployment = foundDeployments.get(0);
		
		Assert.assertNotNull(foundDeployment);
		Assert.assertEquals(deployment, foundDeployment);
		Assert.assertEquals(platform, foundDeployment.getPlatform());
		Assert.assertEquals(businessGroup, foundDeployment.getBusinessGroup());
	}
	
	@Test
	public void loadSharedToolDeployments() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("lti-13-author-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		
		String clientId = UUID.randomUUID().toString();
		String issuer = "https://z.openolat.org";
		LTI13Platform platform = createPlatform(issuer, clientId);

		String deploymentId = UUID.randomUUID().toString();
		LTI13SharedToolDeployment deployment = lti13SharedToolDeploymentDao.createDeployment(deploymentId, platform, entry, null);
		dbInstance.commit();
		
		List<LTI13SharedToolDeployment> foundDeployments = lti13SharedToolDeploymentDao.loadSharedToolDeployments(platform);
		Assert.assertNotNull(foundDeployments);
		Assert.assertEquals(1, foundDeployments.size());
		LTI13SharedToolDeployment foundDeployment = foundDeployments.get(0);
		
		Assert.assertNotNull(foundDeployment);
		Assert.assertEquals(deployment, foundDeployment);
		Assert.assertEquals(platform, foundDeployment.getPlatform());
		Assert.assertEquals(entry, foundDeployment.getEntry());
	}
	
	@Test
	public void hasDeployment() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("lti-13-author-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		
		LTI13Platform platform = createPlatform("https://s.openolat.org", UUID.randomUUID().toString());
		LTI13SharedToolDeployment deployment = lti13SharedToolDeploymentDao
				.createDeployment(UUID.randomUUID().toString(), platform, entry, null);
		dbInstance.commit();
		Assert.assertNotNull(deployment);
		
		boolean hasDeployment = lti13SharedToolDeploymentDao.hasDeployment(entry.getKey());
		Assert.assertTrue(hasDeployment);
	}
	
	private LTI13Platform createPlatform(String issuer, String clientId) {
		LTI13Platform sharedTool = lti13Service.createTransientPlatform(LTI13PlatformScope.PRIVATE);
		sharedTool.setClientId(clientId);
		sharedTool.setIssuer(issuer);
		sharedTool.setAuthorizationUri(issuer + "/ltideploy/auth");
		sharedTool.setTokenUri(issuer + "/ltideploy/token");
		sharedTool.setJwkSetUri(issuer + "/ltideploy/jwks");
		return lti13Service.updatePlatform(sharedTool);
	}
}
