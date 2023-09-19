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

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.crypto.CryptoUtil;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.ims.lti13.LTI13Constants.UserSub;
import org.olat.ims.lti13.LTI13SharedToolService.ServiceType;
import org.olat.ims.lti13.LTI13Tool.PublicKeyType;
import org.olat.ims.lti13.manager.LTI13ContentItemDAO;
import org.olat.ims.lti13.manager.LTI13PlatformSigningPrivateKeyResolver;
import org.olat.ims.lti13.manager.LTI13ServiceImpl;
import org.olat.ims.lti13.manager.LTI13SharedToolDeploymentDAO;
import org.olat.ims.lti13.manager.LTI13ToolDAO;
import org.olat.ims.lti13.manager.LTI13ToolDeploymentDAO;
import org.olat.ims.lti13.model.JwtToolBundle;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.repository.model.RepositoryEntryToGroupRelation;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.nimbusds.jose.util.IOUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.jackson.io.JacksonDeserializer;
import io.jsonwebtoken.security.Keys;

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
	private LTI13ContentItemDAO lti13ContentItemDao;
	@Autowired
	private LTI13ToolDeploymentDAO toolDeploymentDao;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private LTI13SharedToolDeploymentDAO sharedToolDeploymentDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	
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
		Jws<Claims> jwt = Jwts.parserBuilder()
				.setSigningKeyResolver(signingResolver)
				.build()
				.parseClaimsJws(jwtString);
		
		Assert.assertNotNull(jwt);
		Assert.assertEquals(nonce, jwt.getBody().get("nonce"));
	}
	
	@Test
	public void getAndVerifyClientAssertionToolWithPublicKey() {
		String issuer = "https://a.openolat.com";
		String clientId = UUID.randomUUID().toString();
		KeyPair keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256);

		LTI13Tool tool = lti13Service.createExternalTool("GAV-1", issuer, clientId, "", "", LTI13ToolType.EXTERNAL);
		tool.setPublicKey(CryptoUtil.getPublicEncoded(keyPair.getPublic()));
		tool.setPublicKeyTypeEnum(PublicKeyType.KEY);
		tool = lti13Service.updateTool(tool);
		dbInstance.commitAndCloseSession();

		String nonce = UUID.randomUUID().toString();
		
		// signed jwt
		JwtBuilder builder = Jwts.builder()
			//headers
			.setHeaderParam(LTI13Constants.Keys.TYPE, LTI13Constants.Keys.JWT)
			.setHeaderParam(LTI13Constants.Keys.ALGORITHM, "RS256")
			.setIssuer(issuer)
			.setSubject(clientId)
			.claim("nonce", nonce);
		
		String jwtString = builder
			.signWith(keyPair.getPrivate())
			.compact();
		
		JwtToolBundle bundle = lti13Service.getAndVerifyClientAssertion(jwtString);
		Jws<Claims> jws = bundle.getJwt();
		Assert.assertNotNull(jws);
		Assert.assertEquals(nonce, jws.getBody().get("nonce"));
		
		LTI13Tool bTool = bundle.getTool();
		Assert.assertNotNull(bTool);
		Assert.assertEquals(tool, bTool);
	}
	
	@Test(expected = UnsupportedJwtException.class)
	public void getAndVerifyClientAssertionToolWithout() {
		String issuer = "https://a.openolat.com";
		String clientId = UUID.randomUUID().toString();
		KeyPair keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256);

		LTI13Tool tool = lti13Service.createExternalTool("GAV-1", issuer, clientId, "", "", LTI13ToolType.EXTERNAL);
		tool.setPublicKey(CryptoUtil.getPublicEncoded(keyPair.getPublic()));
		tool.setPublicKeyTypeEnum(PublicKeyType.KEY);
		tool = lti13Service.updateTool(tool);
		dbInstance.commitAndCloseSession();

		String nonce = UUID.randomUUID().toString();
		
		// signed jwt
		JwtBuilder builder = Jwts.builder()
			//headers
			.setHeaderParam(LTI13Constants.Keys.TYPE, LTI13Constants.Keys.JWT)
			.setHeaderParam(LTI13Constants.Keys.ALGORITHM, "RS256")
			.setIssuer(issuer)
			.setSubject(clientId)
			.claim("nonce", nonce);
		
		String jwtString = builder
			.compact();
		
		JwtToolBundle bundle = lti13Service.getAndVerifyClientAssertion(jwtString);
		Assert.assertNull(bundle);
	}
	
	@Test
	public void matchIdentityNewUser() {
		String clientId = UUID.randomUUID().toString();
		LTI13Platform platform = createPlatform("https://cuberai.openolat.com", clientId);
		
		DefaultClaims claims = new DefaultClaims();
		String sub = Long.toString(CodeHelper.getForeverUniqueID());
		claims.setSubject(sub);
		claims.setIssuer(platform.getIssuer());
		claims.put(UserSub.GIVEN_NAME, "Fabio");
		claims.put(UserSub.FAMILY_NAME, "Orlando");
		String email = "f.orlando." + sub + "@openolat.com";
		claims.put(UserSub.EMAIL, email);
		claims.put(UserSub.LOCALE, "en-US");

		Identity identity = lti13Service.matchIdentity(claims, platform);
		Assert.assertNotNull(identity);
		Assert.assertNotNull(identity.getUser().getNickName());
		Assert.assertNotEquals(sub, identity.getUser().getNickName());
		Assert.assertEquals("Fabio", identity.getUser().getFirstName());
		Assert.assertEquals("Orlando", identity.getUser().getLastName());
		Assert.assertEquals(email, identity.getUser().getEmail());
	}
	
	@Test
	public void matchIdentityCurrentUserByEmail() {
		String clientId = UUID.randomUUID().toString();
		LTI13Platform platform = createPlatform("https://gfx.openolat.com", clientId);
		platform.setEmailMatching(true);
		
		IdentityWithLogin ident = JunitTestHelper.createAndPersistRndUser("lti-user-1");
		User user = ident.getIdentity().getUser();
		
		DefaultClaims claims = new DefaultClaims();
		String sub = Long.toString(CodeHelper.getForeverUniqueID());
		claims.setSubject(sub);
		claims.setIssuer(platform.getIssuer());
		claims.put(UserSub.GIVEN_NAME, user.getFirstName());
		claims.put(UserSub.FAMILY_NAME, user.getLastName());
		claims.put(UserSub.EMAIL, user.getEmail());
		claims.put(UserSub.LOCALE, "en-US");

		Identity identity = lti13Service.matchIdentity(claims, platform);
		Assert.assertEquals(ident.getIdentity(), identity);
	}
	
	@Test
	public void matchNotIdentityCurrentUserByEmail() {
		String clientId = UUID.randomUUID().toString();
		LTI13Platform platform = createPlatform("https://z7.openolat.com", clientId);
		platform.setEmailMatching(false);
		
		IdentityWithLogin ident = JunitTestHelper.createAndPersistRndUser("lti-user-1");
		User user = ident.getIdentity().getUser();
		
		DefaultClaims claims = new DefaultClaims();
		String sub = Long.toString(CodeHelper.getForeverUniqueID());
		claims.setSubject(sub);
		claims.setIssuer(platform.getIssuer());
		claims.put(UserSub.GIVEN_NAME, user.getFirstName());
		claims.put(UserSub.FAMILY_NAME, user.getLastName());
		claims.put(UserSub.EMAIL, user.getEmail());
		claims.put(UserSub.LOCALE, "en-US");

		Identity identity = lti13Service.matchIdentity(claims, platform);
		Assert.assertNotEquals(ident.getIdentity(), identity);
		Assert.assertNull(identity.getUser().getEmail());
	}
	
	@Test
	public void deleteRepositoryEntry() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("lti-13-author-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		String clientId = UUID.randomUUID().toString();
		String issuer = "https://m.openolat.com";
		LTI13Platform platform = createPlatform(issuer, clientId);
		platform = lti13Service.updatePlatform(platform);
		LTI13SharedToolDeployment deployment = lti13Service
				.createSharedToolDeployment(UUID.randomUUID().toString(), platform, entry, null);
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
		LTI13Tool tool = lti13Service.createExternalTool("Tool 1", issuer, clientId, "https://login", null, LTI13ToolType.EXTERNAL);
		LTI13ToolDeployment deployment = lti13Service.createToolDeployment("https://target", tool, entry, ident, null);
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
		LTI13Tool tool = lti13Service.createExternalTool("Tool template 1", issuer, clientId, "https://login", null, LTI13ToolType.EXT_TEMPLATE);
		LTI13ToolDeployment deployment = lti13Service.createToolDeployment("https://target", tool, entry, ident, null);
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
		LTI13Tool templateTool = lti13Service.createExternalTool("Tool template 2", issuer, clientId, "https://login", null, LTI13ToolType.EXT_TEMPLATE);
		LTI13Tool tool1 = lti13Service.createExternalTool("Tool template 3", issuer, clientId, "https://login", "https://login/init", LTI13ToolType.EXTERNAL);
		LTI13Tool tool2 = lti13Service.createExternalTool("Tool template 4", issuer, clientId, "https://login", "https://login/init", LTI13ToolType.EXTERNAL);
		
		LTI13ToolDeployment deployment1 = lti13Service.createToolDeployment("https://target1", templateTool, entry, identToDelete, null);
		LTI13ToolDeployment deployment2 = lti13Service.createToolDeployment("https://target2", tool1, entry, identToDelete, null);
		LTI13ToolDeployment deployment3 = lti13Service.createToolDeployment("https://target3", tool2, entry, ident, null);
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
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(coach, "LTI service group", "Group with LTI 1.3",
				LTI13Service.LTI_GROUP_TYPE, -1, -1, false, false, null);
		String clientId = UUID.randomUUID().toString();
		String issuer = "https://sg.openolat.com";
		LTI13Platform platform = lti13Service.createTransientPlatform(LTI13PlatformScope.PRIVATE);
		platform.setClientId(clientId);
		platform.setIssuer(issuer);
		platform.setAuthorizationUri(issuer + "/mod/lti/auth.php");
		platform.setTokenUri(issuer + "/mod/lti/token.php");
		platform.setJwkSetUri(issuer + "/mod/lti/certs.php");
		platform = lti13Service.updatePlatform(platform);
		LTI13SharedToolDeployment deployment = lti13Service
				.createSharedToolDeployment(UUID.randomUUID().toString(), platform, null, businessGroup);
		lti13Service.updateSharedToolServiceEndpoint(clientId, ServiceType.lineitem, issuer, deployment);
		dbInstance.commitAndCloseSession();

		lti13Service.deleteGroupDataFor(businessGroup);
		dbInstance.commit();
		
		List<LTI13SharedToolDeployment> deployments = sharedToolDeploymentDao.getSharedToolDeployment(businessGroup);
		Assert.assertTrue(deployments.isEmpty());	
	}
	
	@Test
	public void createSharedToolDeployment() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("lti-13-author-10");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		
		String clientId = UUID.randomUUID().toString();
		String issuer = "https://cuberai.openolat.org";
		LTI13Platform platform = createPlatform(issuer, clientId);
		platform = lti13Service.updatePlatform(platform);
		
		String deploymentId = UUID.randomUUID().toString();
		LTI13SharedToolDeployment deployment = lti13Service.createSharedToolDeployment(deploymentId, platform, entry, null);
		dbInstance.commit();
		
		Assert.assertNotNull(deployment);
		Assert.assertNotNull(deployment.getCreationDate());
		Assert.assertNotNull(deployment.getLastModified());
		Assert.assertEquals(platform, deployment.getPlatform());
		Assert.assertEquals(deploymentId, deployment.getDeploymentId());
		
		Assert.assertEquals(entry, deployment.getEntry());
		Assert.assertNotNull(deployment.getBusinessGroup());
		
		BusinessGroup businessGroup = deployment.getBusinessGroup();
		Assert.assertEquals(LTI13Service.LTI_GROUP_TYPE, businessGroup.getTechnicalType());
		
		List<RepositoryEntryToGroupRelation> rels = repositoryEntryRelationDao.getBusinessGroupAndCurriculumRelations(entry);
		Assert.assertNotNull(rels);
		Assert.assertEquals(1, rels.size());
		Assert.assertEquals(businessGroup.getBaseGroup(), rels.get(0).getGroup());
	}
	
	@Test
	public void deleteSharedToolDeployment() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndAuthor("lti-13-coach-1");
		
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(coach, "LTI service group", "Group with LTI 1.3",
				LTI13Service.LTI_GROUP_TYPE, -1, -1, false, false, null);
		String clientId = UUID.randomUUID().toString();
		String issuer = "https://sg2.openolat.com";
		LTI13Platform platform = lti13Service.createTransientPlatform(LTI13PlatformScope.PRIVATE);
		platform.setClientId(clientId);
		platform.setIssuer(issuer);
		platform.setAuthorizationUri(issuer + "/mod/lti/auth.php");
		platform.setTokenUri(issuer + "/mod/lti/token.php");
		platform.setJwkSetUri(issuer + "/mod/lti/certs.php");
		platform = lti13Service.updatePlatform(platform);
		LTI13SharedToolDeployment deployment = lti13Service
				.createSharedToolDeployment(UUID.randomUUID().toString(), platform, null, businessGroup);
		lti13Service.updateSharedToolServiceEndpoint(clientId, ServiceType.lineitem, issuer, deployment);
		lti13Service.updateSharedToolServiceEndpoint(clientId, ServiceType.lineitems, issuer, deployment);
		lti13Service.updateSharedToolServiceEndpoint(clientId, ServiceType.nrps, issuer, deployment);
		dbInstance.commitAndCloseSession();

		lti13Service.deleteSharedToolDeployment(deployment);
		dbInstance.commit();
		
		List<LTI13SharedToolDeployment> deployments = lti13Service.getSharedToolDeployments(businessGroup);
		Assert.assertTrue(deployments.isEmpty());
	}
	
	/**
	 * Example of the LTI 1.3 specification
	 * @see https://www.imsglobal.org/spec/lti-dl/v2p0#deep-linking-response-example
	 * 
	 * @throws Exception
	 */
	@Test
	public void handleContentItemResourceLink() throws Exception {
		LTI13ToolDeployment deployment = createDeployment("LTI 1.3 resource link");
		
		URL jsonUrl = LTI13JsonUtilTest.class.getResource("content_item_lti_resource_link.json");
		File jsonFile = new File(jsonUrl.toURI());
		String content = IOUtils.readFileToString(jsonFile, StandardCharsets.UTF_8);
		Map<String, ?> contentMap = new JacksonDeserializer<Map<String, ?>>()
				.deserialize(content.getBytes(StandardCharsets.UTF_8));
		Claims claims = new DefaultClaims(contentMap);
		
		List<LTI13ContentItem> itemList = lti13Service.createContentItems(claims, deployment);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(itemList);
		Assert.assertEquals(1, itemList.size());
		
		// Check content item
		LTI13ContentItem item = lti13ContentItemDao.loadItemByKey(itemList.get(0).getKey());
		Assert.assertEquals(deployment.getTool(), item.getTool());
		Assert.assertEquals(LTI13ContentItemTypesEnum.ltiResourceLink, item.getType());
		Assert.assertEquals("A title", item.getTitle());
		Assert.assertEquals("This is a link to an activity that will be graded", item.getText());
		Assert.assertEquals("https://lti.example.com/launchMe", item.getUrl());
		
		// Window
		Assert.assertEquals("examplePublisherContent", item.getWindowTargetName());
		
		// Iframe
		Assert.assertEquals(Long.valueOf(890), item.getIframeHeight());
		
		// Thumbnail
		Assert.assertEquals("https://lti.example.com/thumb.jpg", item.getThumbnailUrl());
		Assert.assertEquals(Long.valueOf(90), item.getThumbnailHeight());
		Assert.assertEquals(Long.valueOf(90), item.getThumbnailWidth());
		
		// Icon
		Assert.assertEquals("https://lti.example.com/image.jpg", item.getIconUrl());
		Assert.assertEquals(Long.valueOf(100), item.getIconHeight());
		Assert.assertEquals(Long.valueOf(100), item.getIconWidth());
		
		// Line item
		Assert.assertEquals("Chapter 12 quiz", item.getLineItemLabel());
		Assert.assertEquals("xyzpdq1234", item.getLineItemResourceId());
		Assert.assertEquals("originality", item.getLineItemTag());
		Assert.assertEquals(Boolean.TRUE, item.getLineItemGradesReleased());
		Assert.assertEquals(87.0d, item.getLineItemScoreMaximum(), 0.0001d);
		
		// Submission
		Assert.assertNull(item.getSubmissionStartDateTime());
		Assert.assertNotNull(item.getSubmissionEndDateTime());
		
		// Available
		Assert.assertNotNull(item.getAvailableStartDateTime());
		Assert.assertNotNull(item.getAvailableEndDateTime());
	}
	
	@Test
	public void handleContentItemHtml() throws Exception {
		LTI13ToolDeployment deployment = createDeployment("LTI 1.3 HTML");
		
		URL jsonUrl = LTI13JsonUtilTest.class.getResource("content_item_html.json");
		File jsonFile = new File(jsonUrl.toURI());
		String content = IOUtils.readFileToString(jsonFile, StandardCharsets.UTF_8);
		Map<String, ?> contentMap = new JacksonDeserializer<Map<String, ?>>()
				.deserialize(content.getBytes(StandardCharsets.UTF_8));
		Claims claims = new DefaultClaims(contentMap);
		
		List<LTI13ContentItem> itemList = lti13Service.createContentItems(claims, deployment);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(itemList);
		Assert.assertEquals(1, itemList.size());
		
		// Check content item
		LTI13ContentItem item = lti13ContentItemDao.loadItemByKey(itemList.get(0).getKey());
		Assert.assertEquals(deployment.getTool(), item.getTool());
		Assert.assertEquals(LTI13ContentItemTypesEnum.html, item.getType());
		
		// HTML
		Assert.assertEquals("<h1>A Custom Title</h1>", item.getHtml());
	}
	
	@Test
	public void handleContentItemLink() throws Exception {
		LTI13ToolDeployment deployment = createDeployment("LTI 1.3 link");
		
		URL jsonUrl = LTI13JsonUtilTest.class.getResource("content_item_link_1.json");
		File jsonFile = new File(jsonUrl.toURI());
		String content = IOUtils.readFileToString(jsonFile, StandardCharsets.UTF_8);
		Map<String, ?> contentMap = new JacksonDeserializer<Map<String, ?>>()
				.deserialize(content.getBytes(StandardCharsets.UTF_8));
		Claims claims = new DefaultClaims(contentMap);
		
		List<LTI13ContentItem> itemList = lti13Service.createContentItems(claims, deployment);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(itemList);
		Assert.assertEquals(1, itemList.size());
		
		// Check content item
		LTI13ContentItem item = lti13ContentItemDao.loadItemByKey(itemList.get(0).getKey());
		Assert.assertEquals(deployment.getTool(), item.getTool());
		Assert.assertEquals(LTI13ContentItemTypesEnum.link, item.getType());
		
		// HTML
		Assert.assertEquals("My Home Page", item.getTitle());
		Assert.assertEquals("https://something.example.com/page.html", item.getUrl());
		
		// Thumbnail
		Assert.assertEquals("https://lti.example.com/thumb.jpg", item.getThumbnailUrl());
		Assert.assertEquals(Long.valueOf(90), item.getThumbnailHeight());
		Assert.assertEquals(Long.valueOf(90), item.getThumbnailWidth());
		
		// Icon
		Assert.assertEquals("https://lti.example.com/image.jpg", item.getIconUrl());
		Assert.assertEquals(Long.valueOf(100), item.getIconHeight());
		Assert.assertEquals(Long.valueOf(100), item.getIconWidth());
	}
	
	
	@Test
	public void handleContentItemLinkYT() throws Exception {
		LTI13ToolDeployment deployment = createDeployment("LTI 1.3 link YT");
		
		URL jsonUrl = LTI13JsonUtilTest.class.getResource("content_item_link_2.json");
		File jsonFile = new File(jsonUrl.toURI());
		String content = IOUtils.readFileToString(jsonFile, StandardCharsets.UTF_8);
		Map<String, ?> contentMap = new JacksonDeserializer<Map<String, ?>>()
				.deserialize(content.getBytes(StandardCharsets.UTF_8));
		Claims claims = new DefaultClaims(contentMap);
		
		List<LTI13ContentItem> itemList = lti13Service.createContentItems(claims, deployment);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(itemList);
		Assert.assertEquals(1, itemList.size());
		
		// Check content item
		LTI13ContentItem item = lti13ContentItemDao.loadItemByKey(itemList.get(0).getKey());
		Assert.assertEquals(deployment.getTool(), item.getTool());
		Assert.assertEquals(LTI13ContentItemTypesEnum.link, item.getType());
		Assert.assertEquals("https://www.youtube.com/watch?v=corV3-WsIro", item.getUrl());
		Assert.assertEquals("<iframe width=\"560\" height=\"315\" src=\"https://www.youtube.com/embed/corV3-WsIro\" frameborder=\"0\" allow=\"autoplay; encrypted-media\" allowfullscreen></iframe>", item.getHtml());
		
		// Window
		Assert.assertEquals("youtube-corV3-WsIro", item.getWindowTargetName());
		Assert.assertEquals("height=560,width=315,menubar=no", item.getWindowFeatures());
		
		// Iframe
		Assert.assertEquals(Long.valueOf(315), item.getIframeHeight());
		Assert.assertEquals(Long.valueOf(560), item.getIframeWidth());
		Assert.assertEquals("https://www.youtube.com/embed/corV3-WsIro", item.getIframeSrc());
	}
	
	@Test
	public void handleContentItemImage() throws Exception {
		LTI13ToolDeployment deployment = createDeployment("LTI 1.3 image");
		
		URL jsonUrl = LTI13JsonUtilTest.class.getResource("content_item_image.json");
		File jsonFile = new File(jsonUrl.toURI());
		String content = IOUtils.readFileToString(jsonFile, StandardCharsets.UTF_8);
		Map<String, ?> contentMap = new JacksonDeserializer<Map<String, ?>>()
				.deserialize(content.getBytes(StandardCharsets.UTF_8));
		Claims claims = new DefaultClaims(contentMap);
		
		List<LTI13ContentItem> itemList = lti13Service.createContentItems(claims, deployment);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(itemList);
		Assert.assertEquals(1, itemList.size());
		
		// Check content item
		LTI13ContentItem item = lti13ContentItemDao.loadItemByKey(itemList.get(0).getKey());
		Assert.assertEquals(deployment.getTool(), item.getTool());
		Assert.assertEquals(LTI13ContentItemTypesEnum.image, item.getType());
		Assert.assertEquals("https://www.example.com/image.png", item.getUrl());
	}
	
	@Test
	public void handleContentItemfile() throws Exception {
		LTI13ToolDeployment deployment = createDeployment("LTI 1.3 file");
		
		URL jsonUrl = LTI13JsonUtilTest.class.getResource("content_item_file.json");
		File jsonFile = new File(jsonUrl.toURI());
		String content = IOUtils.readFileToString(jsonFile, StandardCharsets.UTF_8);
		Map<String, ?> contentMap = new JacksonDeserializer<Map<String, ?>>()
				.deserialize(content.getBytes(StandardCharsets.UTF_8));
		Claims claims = new DefaultClaims(contentMap);
		
		List<LTI13ContentItem> itemList = lti13Service.createContentItems(claims, deployment);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(itemList);
		Assert.assertEquals(1, itemList.size());
		
		// Check content item
		LTI13ContentItem item = lti13ContentItemDao.loadItemByKey(itemList.get(0).getKey());
		Assert.assertEquals(deployment.getTool(), item.getTool());
		Assert.assertEquals(LTI13ContentItemTypesEnum.file, item.getType());
		Assert.assertEquals("A file like a PDF that is my assignment submissions", item.getTitle());

		Assert.assertNotNull(item.getExpiresAt());
		Assert.assertEquals("https://my.example.com/assignment1.pdf", item.getUrl());
	}
	
	
	private LTI13Platform createPlatform(String issuer, String clientId) {
		LTI13Platform platform = lti13Service.createTransientPlatform(LTI13PlatformScope.PRIVATE);
		platform.setClientId(clientId);
		platform.setIssuer(issuer);
		platform.setAuthorizationUri(issuer + "/mod/lti/auth.php");
		platform.setTokenUri(issuer + "/mod/lti/token.php");
		platform.setJwkSetUri(issuer + "/mod/lti/certs.php");
		return platform;
	}
	
	private LTI13ToolDeployment createDeployment(String toolName) {
		String toolUrl = "https://www.openolat.com/tool";
		String clientId = UUID.randomUUID().toString();
		String initiateLoginUrl = "https://www.openolat.com/lti/api/login_init";
		String redirectUrl = "https://www.openolat.com/lti/api/login";
		LTI13Tool tool = lti13Service.createExternalTool(toolName, toolUrl, clientId, initiateLoginUrl, redirectUrl, LTI13ToolType.EXTERNAL);
		
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("lti-13-author-20");
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(author, "A LTI Group", "", BusinessGroup.BUSINESS_TYPE, null, null, null, null, false, false, null);
		
		LTI13ToolDeployment deployment = lti13Service.createToolDeployment(null, tool, null, null, businessGroup);
		dbInstance.commitAndCloseSession();
		
		return deployment;
	}
}
