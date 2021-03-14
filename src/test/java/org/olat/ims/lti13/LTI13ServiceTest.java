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
package org.olat.ims.lti13;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.ims.lti13.LTI13SharedToolService.ServiceType;
import org.olat.ims.lti13.manager.LTI13PlatformSigningPrivateKeyResolver;
import org.olat.ims.lti13.manager.LTI13ServiceImpl;
import org.olat.ims.lti13.manager.LTI13SharedToolDeploymentDAO;
import org.olat.ims.lti13.manager.LTI13ToolDAO;
import org.olat.ims.lti13.manager.LTI13ToolDeploymentDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;

/**
 * 
 * Initial date: 13 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LTI13ServiceTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LTI13ToolDAO toolDao;
	@Autowired
	private LTI13ServiceImpl lti13Service;
	@Autowired
	private LTI13ToolDeploymentDAO toolDeploymentDao;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private LTI13SharedToolDeploymentDAO sharedToolDeploymentDao;
	
	/**
	 * Write and read JWT
	 */
	@Test
	public void keys() {
		LTI13Key currentPlatformKey = lti13Service.getLastPlatformKey();
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(currentPlatformKey);
		Assert.assertNotNull("RS256", currentPlatformKey.getAlgorithm());
		
		String nonce = UUID.randomUUID().toString();
		
		// signed jwt
		JwtBuilder builder = Jwts.builder()
			//headers
			.setHeaderParam(LTI13Constants.Keys.TYPE, LTI13Constants.Keys.JWT)
			.setHeaderParam(LTI13Constants.Keys.ALGORITHM, currentPlatformKey.getAlgorithm())
			.setHeaderParam(LTI13Constants.Keys.KEY_IDENTIFIER, currentPlatformKey.getKeyId())
			.claim("nonce", nonce);
		
		String jwtString = builder
			.signWith(currentPlatformKey.getPrivateKey())
			.compact();
		
		Assert.assertNotNull(jwtString);
		
		LTI13PlatformSigningPrivateKeyResolver signingResolver = new LTI13PlatformSigningPrivateKeyResolver();
		Jwt<?,?> jwt = Jwts.parserBuilder()
				.setSigningKeyResolver(signingResolver)
				.build()
				.parse(jwtString);
		
		Assert.assertNotNull(jwt);
		Assert.assertEquals(nonce, ((Claims)jwt.getBody()).get("nonce"));
	}
	
	@Test
	public void deleteRepositoryEntry() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("lti-13-author-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		String clientId = UUID.randomUUID().toString();
		String issuer = "https://m.openolat.com";
		LTI13SharedTool sharedTool = createSharedTool(issuer, clientId, entry);
		sharedTool = lti13Service.updateSharedTool(sharedTool);
		LTI13SharedToolDeployment deployment = lti13Service.getOrCreateSharedToolDeployment(UUID.randomUUID().toString(), sharedTool);
		lti13Service.updateSharedToolServiceEndpoint(clientId, ServiceType.lineitem, issuer, deployment);
		dbInstance.commitAndCloseSession();

		lti13Service.deleteRepositoryEntryData(entry);
		dbInstance.commit();
		
		boolean hasDeployments = sharedToolDeploymentDao.hasDeployment(entry.getKey());
		Assert.assertFalse(hasDeployments);	
	}
	
	@Test
	public void deleteCourseNode() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("lti-13-author-2");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		String clientId = UUID.randomUUID().toString();
		String issuer = "https://n.openolat.com";
		String ident = UUID.randomUUID().toString();
		LTI13Tool tool = lti13Service.createExternalTool("Tool 1", issuer, clientId, "https://login", LTI13ToolType.EXTERNAL);
		LTI13ToolDeployment deployment = lti13Service.createToolDeployment("https://target", tool, entry, ident);
		dbInstance.commitAndCloseSession();
		
		lti13Service.deleteToolsAndDeployments(entry, ident);
		dbInstance.commit();
		
		LTI13ToolDeployment deletedDeployment = toolDeploymentDao.loadDeploymentByKey(deployment.getKey());
		Assert.assertNull(deletedDeployment);
		LTI13Tool deletedTool = toolDao.loadToolByKey(tool.getKey());
		Assert.assertNull(deletedTool);
	}
	
	@Test
	public void deleteCourseNodeTemplateTool() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("lti-13-author-3");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		String clientId = UUID.randomUUID().toString();
		String issuer = "https://n.openolat.com";
		String ident = UUID.randomUUID().toString();
		LTI13Tool tool = lti13Service.createExternalTool("Tool template 1", issuer, clientId, "https://login", LTI13ToolType.EXT_TEMPLATE);
		LTI13ToolDeployment deployment = lti13Service.createToolDeployment("https://target", tool, entry, ident);
		dbInstance.commitAndCloseSession();
		
		lti13Service.deleteToolsAndDeployments(entry, ident);
		dbInstance.commit();
		
		LTI13ToolDeployment deletedDeployment = toolDeploymentDao.loadDeploymentByKey(deployment.getKey());
		Assert.assertNull(deletedDeployment);
		LTI13Tool templateTool = toolDao.loadToolByKey(tool.getKey());
		Assert.assertNotNull(templateTool);
	}
	
	/**
	 * Check that not too much data is deleted.
	 */
	@Test
	public void deleteCourseNodeComplex() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("lti-13-author-4");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		String clientId = UUID.randomUUID().toString();
		String issuer = "https://n.openolat.com";
		String identToDelete = UUID.randomUUID().toString();
		String ident = UUID.randomUUID().toString();
		LTI13Tool templateTool = lti13Service.createExternalTool("Tool template 2", issuer, clientId, "https://login", LTI13ToolType.EXT_TEMPLATE);
		LTI13Tool tool1 = lti13Service.createExternalTool("Tool template 3", issuer, clientId, "https://login", LTI13ToolType.EXTERNAL);
		LTI13Tool tool2 = lti13Service.createExternalTool("Tool template 4", issuer, clientId, "https://login", LTI13ToolType.EXTERNAL);
		
		LTI13ToolDeployment deployment1 = lti13Service.createToolDeployment("https://target1", templateTool, entry, identToDelete);
		LTI13ToolDeployment deployment2 = lti13Service.createToolDeployment("https://target2", tool1, entry, identToDelete);
		LTI13ToolDeployment deployment3 = lti13Service.createToolDeployment("https://target3", tool2, entry, ident);
		dbInstance.commitAndCloseSession();
		
		lti13Service.deleteToolsAndDeployments(entry, identToDelete);
		dbInstance.commit();
		
		LTI13ToolDeployment deletedDeployment1 = toolDeploymentDao.loadDeploymentByKey(deployment1.getKey());
		Assert.assertNull(deletedDeployment1);
		LTI13ToolDeployment deletedDeployment2 = toolDeploymentDao.loadDeploymentByKey(deployment2.getKey());
		Assert.assertNull(deletedDeployment2);
		LTI13ToolDeployment reloadedDeployment3 = toolDeploymentDao.loadDeploymentByKey(deployment3.getKey());
		Assert.assertNotNull(reloadedDeployment3);
		
		LTI13Tool reloadedTemplateTool = toolDao.loadToolByKey(templateTool.getKey());
		Assert.assertNotNull(reloadedTemplateTool);
		LTI13Tool reloadedTool1 = toolDao.loadToolByKey(tool1.getKey());
		Assert.assertNull(reloadedTool1);
		LTI13Tool reloadedTool2 = toolDao.loadToolByKey(tool2.getKey());
		Assert.assertNotNull(reloadedTool2);
	}
	
	@Test
	public void deleteBusinessGroup() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndAuthor("lti-13-coach-1");
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(coach, "LTI service group", "Group with LTI 1.3", -1, -1, false, false, null);
		String clientId = UUID.randomUUID().toString();
		String issuer = "https://sg.openolat.com";
		LTI13SharedTool sharedTool = lti13Service.createTransientSharedTool(businessGroup);
		sharedTool.setClientId(clientId);
		sharedTool.setIssuer(issuer);
		sharedTool.setAuthorizationUri(issuer + "/mod/lti/auth.php");
		sharedTool.setTokenUri(issuer + "/mod/lti/token.php");
		sharedTool.setJwkSetUri(issuer + "/mod/lti/certs.php");
		sharedTool = lti13Service.updateSharedTool(sharedTool);
		LTI13SharedToolDeployment deployment = lti13Service.getOrCreateSharedToolDeployment(UUID.randomUUID().toString(), sharedTool);
		lti13Service.updateSharedToolServiceEndpoint(clientId, ServiceType.lineitem, issuer, deployment);
		dbInstance.commitAndCloseSession();

		lti13Service.deleteGroupDataFor(businessGroup);
		dbInstance.commit();
		
		List<LTI13SharedToolDeployment> deployments = sharedToolDeploymentDao.getSharedToolDeployment(businessGroup);
		Assert.assertTrue(deployments.isEmpty());	
	}
	
	private LTI13SharedTool createSharedTool(String issuer, String clientId, RepositoryEntry entry) {
		LTI13SharedTool sharedTool = lti13Service.createTransientSharedTool(entry);
		sharedTool.setClientId(clientId);
		sharedTool.setIssuer(issuer);
		sharedTool.setAuthorizationUri(issuer + "/mod/lti/auth.php");
		sharedTool.setTokenUri(issuer + "/mod/lti/token.php");
		sharedTool.setJwkSetUri(issuer + "/mod/lti/certs.php");
		return sharedTool;
	}

}
