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
import org.olat.ims.lti13.LTI13Service;
import org.olat.ims.lti13.LTI13SharedTool;
import org.olat.ims.lti13.LTI13SharedToolDeployment;
import org.olat.ims.lti13.model.LTI13SharedToolWithInfos;
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
public class LTI13SharedToolDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LTI13Service lti13Service;
	@Autowired
	private LTI13SharedToolDAO lti13SharedToolDao;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private LTI13SharedToolDeploymentDAO lti13SharedToolDeploymentDao;
	
	@Test
	public void createSharedTool() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("lti-13-author-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		String clientId = UUID.randomUUID().toString();
		String issuer = "https://openolat.com";
		
		LTI13SharedTool sharedTool = lti13Service.createTransientSharedTool(entry);
		sharedTool.setClientId(clientId);
		sharedTool.setIssuer(issuer);
		sharedTool.setAuthorizationUri("https://openolat.com/lti/auth");
		sharedTool.setTokenUri("https://openolat.com/lti/token");
		sharedTool.setJwkSetUri("https://openolat.com/lti/jwks");
		
		LTI13SharedTool savedSharedTool = lti13SharedToolDao.updateSharedTool(sharedTool);
		Assert.assertNotNull(savedSharedTool);
		dbInstance.commit();
		
		Assert.assertNotNull(sharedTool.getCreationDate());
		Assert.assertNotNull(sharedTool.getLastModified());
		Assert.assertEquals(issuer, sharedTool.getIssuer());
		Assert.assertEquals(clientId, sharedTool.getClientId());
		Assert.assertEquals("https://openolat.com/lti/auth", sharedTool.getAuthorizationUri());
		Assert.assertEquals("https://openolat.com/lti/token", sharedTool.getTokenUri());
		Assert.assertEquals("https://openolat.com/lti/jwks", sharedTool.getJwkSetUri());
		Assert.assertEquals(entry, sharedTool.getEntry());
	}
	
	@Test
	public void loadByKey() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("lti-13-author-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		String clientId = UUID.randomUUID().toString();
		LTI13SharedTool sharedTool = createSharedTool("https://openolat.org", clientId, entry);
		LTI13SharedTool savedSharedTool = lti13SharedToolDao.updateSharedTool(sharedTool);
		Assert.assertNotNull(savedSharedTool);
		dbInstance.commit();
		
		LTI13SharedTool reloadedTool = lti13SharedToolDao.loadByKey(savedSharedTool.getKey());
		dbInstance.commit();
		Assert.assertNotNull(reloadedTool);
		
		Assert.assertNotNull(reloadedTool.getCreationDate());
		Assert.assertNotNull(reloadedTool.getLastModified());
		Assert.assertEquals(savedSharedTool, reloadedTool);
		Assert.assertEquals("https://openolat.org", reloadedTool.getIssuer());
		Assert.assertEquals(clientId, reloadedTool.getClientId());
		Assert.assertEquals("https://openolat.org/lti/auth", reloadedTool.getAuthorizationUri());
		Assert.assertEquals("https://openolat.org/lti/token", reloadedTool.getTokenUri());
		Assert.assertEquals("https://openolat.org/lti/jwks", reloadedTool.getJwkSetUri());
	}
	
	@Test
	public void getSharedToolsByRepositoryEntry() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("lti-13-author-8");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		String clientId = UUID.randomUUID().toString();
		LTI13SharedTool sharedTool = createSharedTool("https://openolat.org", clientId, entry);
		sharedTool = lti13SharedToolDao.updateSharedTool(sharedTool);
		LTI13SharedToolDeployment deployment1 = lti13SharedToolDeploymentDao.createDeployment("1", sharedTool);
		LTI13SharedToolDeployment deployment2 = lti13SharedToolDeploymentDao.createDeployment("2", sharedTool);
		dbInstance.commit();
		Assert.assertNotNull(deployment1);
		Assert.assertNotNull(deployment2);
		
		List<LTI13SharedToolWithInfos> toolsWithInfos = lti13SharedToolDao.getSharedTools(entry);
		dbInstance.commit();
		Assert.assertNotNull(toolsWithInfos);
		Assert.assertEquals(1, toolsWithInfos.size());
		
		LTI13SharedToolWithInfos toolWithInfos = toolsWithInfos.get(0);
		Assert.assertEquals(2, toolWithInfos.getNumOfDeployments());
		Assert.assertEquals(sharedTool, toolWithInfos.getSharedTool());
	}
	
	@Test
	public void getSharedToolsByBusinessGroup() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndAuthor("lti-13-coach-1");
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(coach, "LTI group", "Group with LTI 1.3", -1, -1, false, false, null);
		String clientId = UUID.randomUUID().toString();
		LTI13SharedTool sharedTool = lti13Service.createTransientSharedTool(businessGroup);
		sharedTool.setClientId(clientId);
		String issuer = "https://group.openolat.org";
		sharedTool.setIssuer(issuer);
		sharedTool.setAuthorizationUri(issuer + "/glti/auth");
		sharedTool.setTokenUri(issuer + "/glti/token");
		sharedTool.setJwkSetUri(issuer + "/glti/jwks");
		sharedTool = lti13SharedToolDao.updateSharedTool(sharedTool);
		LTI13SharedToolDeployment deployment = lti13SharedToolDeploymentDao.createDeployment("1", sharedTool);
		dbInstance.commit();
		Assert.assertNotNull(deployment);
		
		List<LTI13SharedToolWithInfos> toolsWithInfos = lti13SharedToolDao.getSharedTools(businessGroup);
		dbInstance.commit();
		Assert.assertNotNull(toolsWithInfos);
		Assert.assertEquals(1, toolsWithInfos.size());
		
		LTI13SharedToolWithInfos toolWithInfos = toolsWithInfos.get(0);
		Assert.assertEquals(1, toolWithInfos.getNumOfDeployments());
		Assert.assertEquals(sharedTool, toolWithInfos.getSharedTool());
	}
	
	@Test
	public void loadByClientId() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("lti-13-author-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		String clientId = UUID.randomUUID().toString();
		String issuer = "https://cuberai.openolat.org";
		LTI13SharedTool sharedTool = createSharedTool(issuer, clientId, entry);
		LTI13SharedTool savedSharedTool = lti13SharedToolDao.updateSharedTool(sharedTool);
		Assert.assertNotNull(savedSharedTool);
		dbInstance.commit();
		
		LTI13SharedTool reloadedTool = lti13SharedToolDao.loadByClientId(issuer, clientId);
		dbInstance.commit();
		Assert.assertNotNull(reloadedTool);
		Assert.assertEquals(savedSharedTool, reloadedTool);
	}
	
	private LTI13SharedTool createSharedTool(String issuer, String clientId, RepositoryEntry entry) {
		LTI13SharedTool sharedTool = lti13Service.createTransientSharedTool(entry);
		sharedTool.setClientId(clientId);
		sharedTool.setIssuer(issuer);
		sharedTool.setAuthorizationUri(issuer + "/lti/auth");
		sharedTool.setTokenUri(issuer + "/lti/token");
		sharedTool.setJwkSetUri(issuer + "/lti/jwks");
		return sharedTool;
	}
}
