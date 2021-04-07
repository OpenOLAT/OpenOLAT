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
import org.olat.ims.lti13.LTI13Platform;
import org.olat.ims.lti13.LTI13PlatformScope;
import org.olat.ims.lti13.LTI13Service;
import org.olat.ims.lti13.LTI13SharedToolDeployment;
import org.olat.ims.lti13.model.LTI13PlatformWithInfos;
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
public class LTI13PlatformDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LTI13Service lti13Service;
	@Autowired
	private LTI13PlatformDAO lti13PlatformDao;
	@Autowired
	private LTI13SharedToolDeploymentDAO lti13SharedToolDeploymentDao;
	
	@Test
	public void createPlatform() {
		String clientId = UUID.randomUUID().toString();
		String issuer = "https://openolat.com";
		
		LTI13Platform platform = lti13Service.createTransientPlatform(LTI13PlatformScope.SHARED);
		platform.setClientId(clientId);
		platform.setIssuer(issuer);
		platform.setAuthorizationUri("https://openolat.com/lti/auth");
		platform.setTokenUri("https://openolat.com/lti/token");
		platform.setJwkSetUri("https://openolat.com/lti/jwks");
		
		LTI13Platform savedPlatform = lti13PlatformDao.updatePlatform(platform);
		Assert.assertNotNull(savedPlatform);
		dbInstance.commit();
		
		Assert.assertNotNull(platform.getCreationDate());
		Assert.assertNotNull(platform.getLastModified());
		Assert.assertEquals(issuer, platform.getIssuer());
		Assert.assertEquals(clientId, platform.getClientId());
		Assert.assertEquals("https://openolat.com/lti/auth", platform.getAuthorizationUri());
		Assert.assertEquals("https://openolat.com/lti/token", platform.getTokenUri());
		Assert.assertEquals("https://openolat.com/lti/jwks", platform.getJwkSetUri());
	}
	
	@Test
	public void loadByKey() {

		String clientId = UUID.randomUUID().toString();
		LTI13Platform platform = createPlatform("https://openolat.org", clientId);
		LTI13Platform savedPlatform = lti13PlatformDao.updatePlatform(platform);
		Assert.assertNotNull(savedPlatform);
		dbInstance.commit();
		
		LTI13Platform reloadedTool = lti13PlatformDao.loadByKey(savedPlatform.getKey());
		dbInstance.commit();
		Assert.assertNotNull(reloadedTool);
		
		Assert.assertNotNull(reloadedTool.getCreationDate());
		Assert.assertNotNull(reloadedTool.getLastModified());
		Assert.assertEquals(savedPlatform, reloadedTool);
		Assert.assertEquals("https://openolat.org", reloadedTool.getIssuer());
		Assert.assertEquals(clientId, reloadedTool.getClientId());
		Assert.assertEquals("https://openolat.org/lti/auth", reloadedTool.getAuthorizationUri());
		Assert.assertEquals("https://openolat.org/lti/token", reloadedTool.getTokenUri());
		Assert.assertEquals("https://openolat.org/lti/jwks", reloadedTool.getJwkSetUri());
	}
	
	@Test
	public void getPlatforms() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("lti-13-author-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		
		String clientId = UUID.randomUUID().toString();
		LTI13Platform transientPlatform = createPlatform("https://openolat.org", clientId);
		final LTI13Platform platform = lti13PlatformDao.updatePlatform(transientPlatform);
		LTI13SharedToolDeployment deployment1 = lti13SharedToolDeploymentDao.createDeployment("1", platform, entry, null);
		LTI13SharedToolDeployment deployment2 = lti13SharedToolDeploymentDao.createDeployment("2", platform, entry, null);
		dbInstance.commit();
		Assert.assertNotNull(deployment1);
		Assert.assertNotNull(deployment2);
		
		List<LTI13PlatformWithInfos> platformsWithInfos = lti13PlatformDao.getPlatforms();
		dbInstance.commit();
		Assert.assertNotNull(platformsWithInfos);
		
		LTI13PlatformWithInfos platformWithInfos = platformsWithInfos.stream()
				.filter(infos -> platform.equals(infos.getPlatform()))
				.findFirst()
				.get();
		Assert.assertEquals(2, platformWithInfos.getNumOfDeployments());
		Assert.assertEquals(platform, platformWithInfos.getPlatform());
	}
	
	@Test
	public void loadByClientId() {
		String clientId = UUID.randomUUID().toString();
		String issuer = "https://cuberai.openolat.org";
		LTI13Platform platform = createPlatform(issuer, clientId);
		LTI13Platform savedPlatform = lti13PlatformDao.updatePlatform(platform);
		Assert.assertNotNull(savedPlatform);
		dbInstance.commit();
		
		LTI13Platform reloadedTool = lti13PlatformDao.loadByClientId(issuer, clientId);
		dbInstance.commit();
		Assert.assertNotNull(reloadedTool);
		Assert.assertEquals(savedPlatform, reloadedTool);
	}
	
	@Test
	public void loadByKid() {
		String clientId = UUID.randomUUID().toString();
		String issuer = "https://cuberai.openolat.org";
		LTI13Platform platform = createPlatform(issuer, clientId);
		LTI13Platform savedPlatform = lti13PlatformDao.updatePlatform(platform);
		Assert.assertNotNull(savedPlatform);
		Assert.assertNotNull(savedPlatform.getKeyId());
		dbInstance.commit();
		
		List<LTI13Platform> loadPlatforms = lti13PlatformDao.loadByKid(savedPlatform.getKeyId());
		Assert.assertNotNull(loadPlatforms);
		Assert.assertEquals(1, loadPlatforms.size());
		Assert.assertEquals(savedPlatform, loadPlatforms.get(0));
		
	}
	
	private LTI13Platform createPlatform(String issuer, String clientId) {
		LTI13Platform platform = lti13Service.createTransientPlatform(LTI13PlatformScope.SHARED);
		platform.setClientId(clientId);
		platform.setIssuer(issuer);
		platform.setAuthorizationUri(issuer + "/lti/auth");
		platform.setTokenUri(issuer + "/lti/token");
		platform.setJwkSetUri(issuer + "/lti/jwks");
		return platform;
	}
}
