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
import org.olat.ims.lti13.LTI13SharedTool;
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
	private LTI13SharedToolDeploymentDAO lti13SharedToolDeploymentDao;
	
	@Test
	public void createSharedToolDeployment() {
		String clientId = UUID.randomUUID().toString();
		String issuer = "https://cuberai.openolat.org";
		LTI13SharedTool sharedTool = createSharedTool(issuer, clientId);
		
		String deploymentId = UUID.randomUUID().toString();
		LTI13SharedToolDeployment deployment = lti13SharedToolDeploymentDao.createDeployment(deploymentId, sharedTool);
		dbInstance.commit();
		
		Assert.assertNotNull(deployment);
		Assert.assertNotNull(deployment.getCreationDate());
		Assert.assertNotNull(deployment.getLastModified());
		Assert.assertEquals(sharedTool, deployment.getSharedTool());
		Assert.assertEquals(deploymentId, deployment.getDeploymentId());
	}
	
	@Test
	public void getSharedToolDeploymentByDeploymentId() {
		String clientId = UUID.randomUUID().toString();
		String issuer = "https://cuberai.openolat.org";
		LTI13SharedTool sharedTool = createSharedTool(issuer, clientId);
		
		String deploymentId = UUID.randomUUID().toString();
		LTI13SharedToolDeployment deployment = lti13SharedToolDeploymentDao.createDeployment(deploymentId, sharedTool);
		dbInstance.commit();
		
		List<LTI13SharedToolDeployment> foundDeployments = lti13SharedToolDeploymentDao.getSharedToolDeployment(deploymentId, sharedTool);
		Assert.assertNotNull(foundDeployments);
		Assert.assertEquals(1, foundDeployments.size());
		LTI13SharedToolDeployment foundDeployment = foundDeployments.get(0);
		
		Assert.assertNotNull(foundDeployment);
		Assert.assertEquals(deployment, foundDeployment);
		Assert.assertEquals(sharedTool, foundDeployment.getSharedTool());
		Assert.assertEquals(deploymentId, foundDeployment.getDeploymentId());
	}
	
	@Test
	public void getSharedToolDeploymentByRepositoryEntry() {
		String clientId = UUID.randomUUID().toString();
		String issuer = "https://z.openolat.org";
		LTI13SharedTool sharedTool = createSharedTool(issuer, clientId);
		RepositoryEntry entry = sharedTool.getEntry();
		
		String deploymentId = UUID.randomUUID().toString();
		LTI13SharedToolDeployment deployment = lti13SharedToolDeploymentDao.createDeployment(deploymentId, sharedTool);
		dbInstance.commit();
		
		List<LTI13SharedToolDeployment> foundDeployments = lti13SharedToolDeploymentDao.getSharedToolDeployment(entry);
		Assert.assertNotNull(foundDeployments);
		Assert.assertEquals(1, foundDeployments.size());
		LTI13SharedToolDeployment foundDeployment = foundDeployments.get(0);
		
		Assert.assertNotNull(foundDeployment);
		Assert.assertEquals(deployment, foundDeployment);
		Assert.assertEquals(sharedTool, foundDeployment.getSharedTool());
		Assert.assertEquals(entry, foundDeployment.getSharedTool().getEntry());
	}
	
	@Test
	public void hasDeployment() {
		LTI13SharedTool sharedTool = createSharedTool("https://s.openolat.org", UUID.randomUUID().toString());
		RepositoryEntry entry = sharedTool.getEntry();
		LTI13SharedToolDeployment deployment = lti13SharedToolDeploymentDao.createDeployment(UUID.randomUUID().toString(), sharedTool);
		dbInstance.commit();
		Assert.assertNotNull(deployment);
		
		boolean hasDeployment = lti13SharedToolDeploymentDao.hasDeployment(entry.getKey());
		Assert.assertTrue(hasDeployment);
	}
	
	
	private LTI13SharedTool createSharedTool(String issuer, String clientId) {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("lti-13-author-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		
		LTI13SharedTool sharedTool = lti13Service.createTransientSharedTool(entry);
		sharedTool.setClientId(clientId);
		sharedTool.setIssuer(issuer);
		sharedTool.setAuthorizationUri(issuer + "/ltideploy/auth");
		sharedTool.setTokenUri(issuer + "/ltideploy/token");
		sharedTool.setJwkSetUri(issuer + "/ltideploy/jwks");
		return lti13Service.updateSharedTool(sharedTool);
	}
}
