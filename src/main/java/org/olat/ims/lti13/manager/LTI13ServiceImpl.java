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

import java.math.BigDecimal;
import java.net.URL;
import java.security.Key;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.admin.securitygroup.gui.IdentitiesAddEvent;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.manager.AuthenticationDAO;
import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.cache.CacheWrapper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.crypto.CryptoUtil;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.MailPackage;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.nodes.BasicLTICourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupRef;
import org.olat.group.BusinessGroupService;
import org.olat.group.DeletableGroupData;
import org.olat.ims.lti13.LTI13Constants;
import org.olat.ims.lti13.LTI13Constants.UserSub;
import org.olat.ims.lti13.LTI13JsonUtil;
import org.olat.ims.lti13.LTI13Key;
import org.olat.ims.lti13.LTI13Module;
import org.olat.ims.lti13.LTI13Platform;
import org.olat.ims.lti13.LTI13PlatformScope;
import org.olat.ims.lti13.LTI13Service;
import org.olat.ims.lti13.LTI13SharedToolDeployment;
import org.olat.ims.lti13.LTI13SharedToolService;
import org.olat.ims.lti13.LTI13SharedToolService.ServiceType;
import org.olat.ims.lti13.LTI13Tool;
import org.olat.ims.lti13.LTI13ToolDeployment;
import org.olat.ims.lti13.LTI13ToolType;
import org.olat.ims.lti13.OIDCApi;
import org.olat.ims.lti13.model.AccessTokenKey;
import org.olat.ims.lti13.model.AccessTokenTimed;
import org.olat.ims.lti13.model.AssessmentEntryWithUserId;
import org.olat.ims.lti13.model.LTI13PlatformImpl;
import org.olat.ims.lti13.model.LTI13PlatformWithInfos;
import org.olat.ims.lti13.model.json.LineItem;
import org.olat.ims.lti13.model.json.Result;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryDataDeletable;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.user.UserManager;
import org.olat.user.UserModule;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 * 
 * Initial date: 17 févr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LTI13ServiceImpl implements LTI13Service, RepositoryEntryDataDeletable, DeletableGroupData, InitializingBean {
	
	private static final Logger log = Tracing.createLoggerFor(LTI13ServiceImpl.class);

	@Autowired
	private DB dbInstance;
	@Autowired
	private UserModule userModule;
	@Autowired
	private I18nManager i18nManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private LTI13Module lti13Module;
	@Autowired
	private LTI13KeyDAO lti13KeyDao;
	@Autowired
	private LTI13ToolDAO lti13ToolDao;
	@Autowired
	private LTI13IDGenerator idGenerator;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private AuthenticationDAO authenticationDao;
	@Autowired
	private LTI13PlatformDAO lti13SharedToolDao;
	@Autowired
	private CoordinatorManager coordinatorManager;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private LTI13ToolDeploymentDAO lti13ToolDeploymentDao;
	@Autowired
	private LTI13AssessmentEntryDAO lti13AssessmentEntryDao;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	@Autowired
	private LTI13SharedToolServiceDAO lti13SharedToolServiceDao;
	@Autowired
	private LTI13SharedToolDeploymentDAO sharedToolDeploymentDao;
	
	private CacheWrapper<AccessTokenKey,AccessTokenTimed> accessTokensCache;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		accessTokensCache = coordinatorManager.getCoordinator().getCacher().getCache("LTI", "accessTokens");
	}

	@Override
	public boolean deleteRepositoryEntryData(RepositoryEntry re) {
		// shared tool service, shared tool deployment, shared tool,
		List<LTI13SharedToolDeployment> sharedDeployments = sharedToolDeploymentDao.getSharedToolDeployment(re);
		for(LTI13SharedToolDeployment sharedDeployment:sharedDeployments) {
			lti13SharedToolServiceDao.deleteSharedToolServices(sharedDeployment);
			sharedToolDeploymentDao.deleteSharedDeployment(sharedDeployment);
		}
		dbInstance.commit();
		//TODO lti lti13SharedToolDao.deleteSharedTools(re);
		return true;
	}

	@Override
	public boolean deleteGroupDataFor(BusinessGroup group) {
		// shared tool service, shared tool deployment, shared tool,
		List<LTI13SharedToolDeployment> sharedDeployments = sharedToolDeploymentDao.getSharedToolDeployment(group);
		for(LTI13SharedToolDeployment sharedDeployment:sharedDeployments) {
			lti13SharedToolServiceDao.deleteSharedToolServices(sharedDeployment);
			sharedToolDeploymentDao.deleteSharedDeployment(sharedDeployment);
		}
		dbInstance.commit();
		//TODO lti lti13SharedToolDao.deleteSharedTools(group);
		return true;
	}

	@Override
	public void deleteToolsAndDeployments(RepositoryEntryRef entry, String subIdent) {
		List<LTI13Tool> localTools = new ArrayList<>();
		List<LTI13ToolDeployment> deployments = lti13ToolDeploymentDao.loadDeploymentsBy(entry, subIdent);
		for(LTI13ToolDeployment deployment:deployments) {
			if(deployment.getTool().getToolTypeEnum() == LTI13ToolType.EXTERNAL) {
				localTools.add(deployment.getTool());
			}
			lti13ToolDeploymentDao.deleteToolDeployment(deployment);
		}
		for(LTI13Tool tool:localTools) {
			lti13ToolDao.deleteTool(tool);
		}
	}

	@Override
	public String newClientId() {
		return idGenerator.newId();
	}

	@Override
	public LTI13Tool createExternalTool(String toolName, String toolUrl, String clientId, String initiateLoginUrl, String redirectUrls, LTI13ToolType type) {
		return lti13ToolDao.createTool(toolName, toolUrl, clientId, initiateLoginUrl, redirectUrls, type);
	}

	@Override
	public LTI13Platform createTransientPlatform(LTI13PlatformScope type) {
		LTI13PlatformImpl platform = new LTI13PlatformImpl();
		platform.setCreationDate(new Date());
		platform.setLastModified(platform.getLastModified());
		platform.setKeyId(UUID.randomUUID().toString());
		platform.setScopeEnum(type);
		
		KeyPair keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256);
	
		String publicEncoded = CryptoUtil.getPublicEncoded(keyPair.getPublic());
		publicEncoded = CryptoUtil.getPublicEncoded(publicEncoded);
		platform.setPublicKey(publicEncoded);
		String privateEncoded = CryptoUtil.getPrivateEncoded(keyPair.getPrivate());
		privateEncoded = CryptoUtil.getPublicEncoded(privateEncoded);
		platform.setPrivateKey(privateEncoded);
		
		return platform;
	}

	@Override
	public LTI13Platform updatePlatform(LTI13Platform tool) {
		return lti13SharedToolDao.updatePlatform(tool);
	}

	@Override
	public LTI13Platform getPlatform(String issuer, String clientId) {
		return lti13SharedToolDao.loadByClientId(issuer, clientId);
	}

	@Override
	public LTI13Platform getPlatformByKey(Long key) {
		return lti13SharedToolDao.loadByKey(key);
	}

	@Override
	public List<LTI13Platform> getPlatforms() {
		return lti13SharedToolDao.getPlatforms().stream()
				.map(LTI13PlatformWithInfos::getPlatform)
				.collect(Collectors.toList());
	}

	@Override
	public List<LTI13PlatformWithInfos> getPlatformWithInfos() {
		return lti13SharedToolDao.getPlatforms();
	}

	@Override
	public LTI13SharedToolDeployment createSharedToolDeployment(String deploymentId, LTI13Platform platform,
			RepositoryEntry repositoryEntry, BusinessGroup businessGroup) {
		List<LTI13SharedToolDeployment> deployments = sharedToolDeploymentDao.getSharedToolDeployment(deploymentId, platform);
		if(deployments.isEmpty()) {
			if(repositoryEntry != null && businessGroup == null) {
				String groupName = "LTI: " + platform.getName();
				String groupDescription =  "LTI group for: " + platform.getName();
				businessGroup = businessGroupService.createBusinessGroup(null, groupName, groupDescription,
						LTI13Service.LTI_GROUP_TYPE, -1, -1, false, false, repositoryEntry);
			}
			LTI13SharedToolDeployment newDeployment = sharedToolDeploymentDao
					.createDeployment(deploymentId, platform, repositoryEntry, businessGroup);
			dbInstance.commit();
			return newDeployment;
		}
		if(deployments.size() == 1) {
			return deployments.get(0);
		}
		log.error("Shared tool with same deployments: {} {}", platform.getKey(), deploymentId);
		return null;
	}

	@Override
	public LTI13SharedToolDeployment updateSharedToolDeployment(LTI13SharedToolDeployment deployment) {
		return sharedToolDeploymentDao.updateDeployment(deployment);
	}

	@Override
	public LTI13SharedToolDeployment getSharedToolDeployment(String deploymentId, LTI13Platform platform) {
		List<LTI13SharedToolDeployment> deployments = sharedToolDeploymentDao.getSharedToolDeployment(deploymentId, platform);
		if(deployments.size() == 1) {
			return deployments.get(0);
		}
		log.error("Shared tool with problematic deployment: {} {} (num. of deployments: {})", platform.getKey(), deploymentId, deployments.size());
		return null;
	}

	@Override
	public List<LTI13SharedToolDeployment> getSharedToolDeployments(LTI13Platform sharedTool) {
		return sharedToolDeploymentDao.loadSharedToolDeployments(sharedTool);
	}

	@Override
	public List<LTI13SharedToolDeployment> getSharedToolDeployments(RepositoryEntryRef entry) {
		return sharedToolDeploymentDao.getSharedToolDeployment(entry);
	}

	@Override
	public void deleteSharedToolDeployment(LTI13SharedToolDeployment deployment) {
		deployment = sharedToolDeploymentDao.loadByKey(deployment.getKey());
		if(deployment != null) {
			sharedToolDeploymentDao.deleteSharedDeployment(deployment);
		}
	}

	@Override
	public List<LTI13SharedToolDeployment> getSharedToolDeployments(BusinessGroupRef businessGroup) {
		return sharedToolDeploymentDao.getSharedToolDeployment(businessGroup);
	}

	@Override
	public void updateSharedToolServiceEndpoint(String contextId, ServiceType type, String endpointUrl, LTI13SharedToolDeployment deployment) {
		List<LTI13SharedToolService> services = lti13SharedToolServiceDao.loadServiceEndpoint(contextId, type, endpointUrl, deployment);
		if(services.isEmpty()) {
			lti13SharedToolServiceDao.createServiceEndpoint(contextId, type, endpointUrl, deployment);
			dbInstance.commit();
		}
	}

	@Override
	public LTI13ToolDeployment createToolDeployment(String targetUrl, LTI13Tool tool, RepositoryEntry entry, String subIdent) {
		return lti13ToolDeploymentDao.createDeployment(targetUrl, tool, entry, subIdent);
	}

	@Override
	public LTI13ToolDeployment updateToolDeployment(LTI13ToolDeployment deployment) {
		return lti13ToolDeploymentDao.updateToolDeployment(deployment);
	}

	@Override
	public List<LTI13Tool> getTools(LTI13ToolType type) {
		return lti13ToolDao.getTools(type);
	}

	@Override
	public LTI13Tool updateTool(LTI13Tool tool) {
		return lti13ToolDao.updateTool(tool);
	}

	@Override
	public LTI13Tool getToolByKey(Long key) {
		return lti13ToolDao.loadToolByKey(key);
	}

	@Override
	public List<LTI13Tool> getToolsByClientId(String clientId) {
		return lti13ToolDao.loadToolsByClientId(clientId);
	}

	@Override
	public LTI13Tool getToolBy(String toolUrl, String clientId) {
		return lti13ToolDao.loadToolBy(toolUrl, clientId);
	}

	@Override
	public LTI13ToolDeployment getToolDeployment(RepositoryEntryRef entry, String subIdent) {
		return lti13ToolDeploymentDao.loadDeploymentBy(entry, subIdent);
	}

	@Override
	public LTI13ToolDeployment getToolDeploymentByKey(Long key) {
		return lti13ToolDeploymentDao.loadDeploymentByKey(key);
	}
	
	@Override
	public List<LTI13ToolDeployment> getToolDeployments(LTI13Tool tool) {
		return lti13ToolDeploymentDao.loadDeployments(tool);
	}

	@Override
	public Identity loadIdentity(String sub, String issuer) {
		Authentication ltiAuth = authenticationDao.getAuthentication(sub, LTI_PROVIDER, issuer);
		if(ltiAuth != null) {
			return ltiAuth.getIdentity();
		}
		return null;
	}

	@Override
	public String subIdentity(Identity identity, String issuer) {
		Authentication ltiAuth = authenticationDao.getAuthentication(identity, LTI_PROVIDER, issuer);
		if(ltiAuth == null) {
			String uuid = UUID.randomUUID().toString();
			ltiAuth = securityManager.createAndPersistAuthentication(identity, LTI_PROVIDER, issuer, uuid, null, null);
		}
		
		if(ltiAuth != null) {
			return ltiAuth.getAuthusername();
		}
		return null;
	}
	
	@Override
	public Identity matchIdentity(Claims body, LTI13Platform platform) {
		String issuer = body.getIssuer();
		String sub = body.getSubject();
		String givenName = body.get(UserSub.GIVEN_NAME, String.class);
		String familyName = body.get(UserSub.FAMILY_NAME, String.class);
		String email = body.get(UserSub.EMAIL, String.class);
		
		Identity identity = loadIdentity(sub, issuer);
		if(identity == null) {
			String language = body.get(UserSub.LOCALE, String.class);
			log.debug("sub: {}, given_name: {} family_name: {}", sub, givenName, familyName);
			
			if(StringHelper.containsNonWhitespace(email) && lti13Module.isMatchingByEmailEnabled()
					&& platform.isEmailMatching() && userModule.isEmailUnique()) {
				List<Identity> currentIdentities = userManager.findIdentitiesByEmail(List.of(email));
				if(currentIdentities.size() == 1) {
					identity = currentIdentities.get(0);
					securityManager.createAndPersistAuthentication(identity, LTI13Service.LTI_PROVIDER, issuer, sub, null, null);
					return identity;
				}
			}
			
			Locale locale = i18nManager.getLocaleOrDefault(language);
			Organisation ltiOrganisation = getLTIOrganisation();
			if(userModule.isEmailUnique() && !userManager.isEmailAllowed(email)) {
				email = null;
			}
			User user = userManager.createUser(givenName, familyName, email);
			user.getPreferences().setLanguage(locale.toString());
			String nickName = "l" + CodeHelper.getForeverUniqueID();
			identity = securityManager.createAndPersistIdentityAndUserWithOrganisation(null, nickName, null, user,
					LTI13Service.LTI_PROVIDER, issuer, sub, null, ltiOrganisation, null);
		} else if(isLTIOnlyUser(identity) &&
				(!StringHelper.isSame(identity.getUser().getFirstName(), givenName)
						|| !StringHelper.isSame(identity.getUser().getLastName(), familyName)
						|| !StringHelper.isSame(identity.getUser().getEmail(), email))) {
			User user = identity.getUser();
			user.setProperty(UserConstants.FIRSTNAME, givenName);
			user.setProperty(UserConstants.LASTNAME, familyName);
			user.setProperty(UserConstants.EMAIL, email);
			userManager.updateUserFromIdentity(identity);
		}
		return identity;
	}
	
	private boolean isLTIOnlyUser(Identity identity) {
		List<Authentication> authentications = securityManager.getAuthentications(identity);
		return authentications.size() == 1;
	}
	
	@Override
	public void checkMembership(Identity identity, GroupRoles role, LTI13SharedToolDeployment tool) {
		if(tool.getBusinessGroup() != null) {
			BusinessGroup businessGroup = tool.getBusinessGroup();
			if(!businessGroupService.hasRoles(identity, businessGroup, role.name())) {
				Roles roles = securityManager.getRoles(identity);
				if(role == GroupRoles.participant) {
					businessGroupService.addParticipants(identity, roles, List.of(identity), businessGroup, new MailPackage(false));
				} else if(role == GroupRoles.coach) {
					businessGroupService.addOwners(identity, roles, List.of(identity), businessGroup, new MailPackage(false));
				} else {
					log.warn("Roles not supported: {}", role);
				}
			}
		} else if(tool.getEntry() != null) {
			// check if participant, if not
			RepositoryEntry entry = tool.getEntry();
			if(!repositoryService.hasRole(identity, entry, role.name())) {
				Roles roles = securityManager.getRoles(identity);
				IdentitiesAddEvent iae = new IdentitiesAddEvent(identity);
				if(role == GroupRoles.participant) {
					repositoryManager.addParticipants(identity, roles, iae, entry, new MailPackage(false));
				} else if(role == GroupRoles.coach) {
					repositoryManager.addTutors(identity, roles, iae, entry, new MailPackage(false));
				} else {
					log.warn("Roles not supported: {}", role);
				}
			}
		} 
	}
	
	private Organisation getLTIOrganisation() {
		Organisation organisation = null;
		if(StringHelper.isLong(lti13Module.getDefaultOrganisationKey())) {
			Long organisationKey = Long.valueOf(lti13Module.getDefaultOrganisationKey());
			organisation = organisationService.getOrganisation(new OrganisationRefImpl(organisationKey));
		}
		if(organisation == null) {
			organisation = organisationService.getDefaultOrganisation();
		}
		return organisation;
	}
	
	@Override
	public LTI13Key getLastPlatformKey() {
		List<LTI13Key> keys = lti13KeyDao.getKeys(lti13Module.getPlatformIss());
		
		LTI13Key key = null;
		if(keys == null || keys.isEmpty()) {
			key = lti13KeyDao.generateKey(lti13Module.getPlatformIss());
			dbInstance.commit();
		} else {
			key = keys.get(0);
		}
		return key;
	}

	@Override
	public List<LTI13Key> getPlatformKeys() {
		return lti13KeyDao.getKeys(lti13Module.getPlatformIss());
	}

	@Override
	public PublicKey getPlatformPublicKey(String kid) {
		//kid is unique
		List<LTI13Platform> platforms = lti13SharedToolDao.loadByKid(kid);
		if(!platforms.isEmpty()) {
			String publicKeyText = platforms.get(0).getPublicKey();
			return CryptoUtil.string2PublicKey(publicKeyText);
		}
		List<LTI13Key> keys = lti13KeyDao.getKey(kid, lti13Module.getPlatformIss());
		if(keys.size() == 1) {
			return keys.get(0).getPublicKey();
		}
		return null;
	}

	@Override
	public LTI13Key getKey(String jwkSetUri, String kid) {
		List<LTI13Key> keys = lti13KeyDao.getKeys(jwkSetUri);
		for(LTI13Key key:keys) {
			if(key.getKeyId().equals(kid)) {
				return key;
			}
		}
		
		dbInstance.commit();
		try {
			JWKSet publicKeys = JWKSet.load(new URL(jwkSetUri));
			JWK jwk = publicKeys.getKeyByKeyId(kid);
			String algorithm = jwk.getAlgorithm().toString();
			PublicKey publicKey = jwk.toRSAKey().toPublicKey();
			return lti13KeyDao.createKey(jwkSetUri, kid, algorithm, publicKey);
		} catch (Exception e) {
			log.error("", e);
			return null;
		} finally {
			dbInstance.commit();
		}
	}

	@Override
	public OAuth2AccessToken getAccessToken(LTI13Platform tool, List<String> scopes) {
		AccessTokenKey tokenKey = new AccessTokenKey(tool, scopes);
		if(accessTokensCache.containsKey(tokenKey)) {
			AccessTokenTimed obj = accessTokensCache.get(tokenKey);
			if(obj == null || obj.hasExpired()) {
				accessTokensCache.remove(tokenKey);
			} else {
				return obj.getAccessToken();
			}
		}

		String clientId = tool.getClientId();
		String tokenUrl = tool.getTokenUri();

		OAuth20Service service = new ServiceBuilder(clientId)
		        .callback(lti13Module.getPlatformAuthorizationUri())
		        .defaultScope("openid")
		        .responseType(LTI13Constants.OAuth.ID_TOKEN)
		        .build(new OIDCApi());
		
		OAuthRequest request = new OAuthRequest(Verb.POST, tokenUrl);
		request.addHeader("Content-Type", "application/x-www-form-urlencoded");
		
		request.addQuerystringParameter(LTI13Constants.OAuth.GRANT_TYPE, LTI13Constants.OAuth.CLIENT_CREDENTIALS);
		request.addQuerystringParameter(LTI13Constants.OAuth.CLIENT_ASSERTION_TYPE, LTI13Constants.OAuth.CLIENT_ASSERTION_TYPE_BEARER);
	
		String scope = String.join(" ", scopes);
		request.addQuerystringParameter("scope", scope);
		
		Date expirationDate = DateUtils.addMinutes(new Date(), 60);
		
		JwtBuilder builder = Jwts.builder()
				.setIssuedAt(new Date())
				.setExpiration(expirationDate)
				.setIssuer(lti13Module.getPlatformIss())
				.setAudience(tokenUrl)
				.setSubject(clientId);

		builder.setHeaderParam(LTI13Constants.Keys.TYPE, LTI13Constants.Keys.JWT);
		builder.setHeaderParam(LTI13Constants.Keys.ALGORITHM, "RS256");
		builder.setHeaderParam(LTI13Constants.Keys.KEY_IDENTIFIER, tool.getKeyId());
		
		Key key = CryptoUtil.string2PrivateKey(tool.getPrivateKey());
		
		String assertion = builder
				.signWith(key)
				.compact();

		request.addQuerystringParameter(LTI13Constants.OAuth.CLIENT_ASSERTION, assertion);
		
		try {
			Response response = service.execute(request);
			OAuth2AccessToken token = service.getApi().getAccessTokenExtractor().extract(response);
			if(token != null && token.getExpiresIn() != null) {
				AccessTokenTimed cachedToken = new AccessTokenTimed(token);
				accessTokensCache.put(tokenKey, cachedToken, token.getExpiresIn().intValue());
			}
			return token;
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	@Override
	public Result getResult(String userId, Identity assessedId, LTI13ToolDeployment deployment) {
		RepositoryEntry entry = deployment.getEntry();
		String subIdent = deployment.getSubIdent();
		ICourse course = CourseFactory.loadCourse(entry);
		CourseNode courseNode = course.getRunStructure().getNode(subIdent);
		if(courseNode instanceof BasicLTICourseNode) {
			BasicLTICourseNode ltiNode = (BasicLTICourseNode)courseNode;
			
			UserCourseEnvironment userCourseEnv = getUserCourseEnvironment(assessedId, course);
			ScoreEvaluation eval = courseAssessmentService.getAssessmentEvaluation(courseNode, userCourseEnv);
			Float score = getScoreFromEvalutation(eval, ltiNode);
			Float maxScore = getMaxScoreFromNode(ltiNode);
			return LTI13JsonUtil.createResult(userId, score, maxScore, deployment);
		}
		return null;
	}
	
	@Override
	public List<Result> getResults(LTI13ToolDeployment deployment, int firstResult, int maxResults) {
		RepositoryEntry entry = deployment.getEntry();
		String subIdent = deployment.getSubIdent();
		ICourse course = CourseFactory.loadCourse(entry);
		CourseNode courseNode = course.getRunStructure().getNode(subIdent);
		
		List<Result> results = new ArrayList<>();
		if(courseNode instanceof BasicLTICourseNode) {
			BasicLTICourseNode ltiNode = (BasicLTICourseNode)courseNode;
			Float maxScore = getMaxScoreFromNode(ltiNode);
			
			List<AssessmentEntryWithUserId> assessmentEntries = lti13AssessmentEntryDao.getAssessmentEntriesWithUserIds(deployment, firstResult, maxResults);
			for(AssessmentEntryWithUserId assessmentEntry:assessmentEntries) {
				Float score = getScoreFromEvalutation(assessmentEntry, ltiNode);
				Result result = LTI13JsonUtil.createResult(assessmentEntry.getUserId(), score, maxScore, deployment);
				results.add(result);
			}
		}
		return results;
	}
	
	@Override
	public LineItem getLineItem(LTI13ToolDeployment deployment) {
		RepositoryEntry entry = deployment.getEntry();
		
		String subIdent = deployment.getSubIdent();
		ICourse course = CourseFactory.loadCourse(entry);
		CourseNode courseNode = course.getRunStructure().getNode(subIdent);
		
		LineItem lineItem = new LineItem();
		lineItem.setId(deployment.getSubIdent());
		if(StringHelper.containsNonWhitespace(courseNode.getShortTitle())) {
			lineItem.setLabel(courseNode.getShortTitle());
		} else {
			lineItem.setLabel(courseNode.getLongTitle());
		}
		
		if(courseNode instanceof BasicLTICourseNode) {
			Float maxScore = getMaxScoreFromNode((BasicLTICourseNode)courseNode);
			if(maxScore != null) {
				lineItem.setScoreMaximum(maxScore.doubleValue());
			}
		}
		lineItem.setResourceId(subIdent);
		RepositoryEntryLifecycle lifeCycle = entry.getLifecycle();
		if(lifeCycle != null) {
			lineItem.setStartDateTime(lifeCycle.getValidFrom());
			lineItem.setEndDateTime(lifeCycle.getValidTo());
		}
		return lineItem;
	}
	
	private Float getMaxScoreFromNode(BasicLTICourseNode ltiNode) {
		Float maxScore = null;
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(ltiNode);
		if(assessmentConfig.getScoreMode() == Mode.setByNode) {
			maxScore = assessmentConfig.getMaxScore();
			if(maxScore != null && maxScore.floatValue() > 0.0f) {
				float scale = ltiNode.getScalingFactor();
				maxScore = Float.valueOf(maxScore.floatValue() / scale);
			}
		}
		return maxScore;
	}
	
	private Float getScoreFromEvalutation(AssessmentEntryWithUserId eval, BasicLTICourseNode ltiNode) {
		Float score = null;
		if(eval != null && eval.getAssessmentEntry() != null && eval.getAssessmentEntry().getScore() != null) {
			BigDecimal scaledScore = eval.getAssessmentEntry().getScore();
			if(scaledScore != null && scaledScore.doubleValue() > 0.0d) {
				float scale = ltiNode.getScalingFactor();
				score = Float.valueOf(scaledScore.floatValue() / scale);
			} else if(scaledScore != null) {
				score = Float.valueOf(0.0f);
			}
		}
		return score;
	}
	
	private Float getScoreFromEvalutation(ScoreEvaluation eval, BasicLTICourseNode ltiNode) {
		Float score = null;
		if(eval != null && eval.getScore() != null) {
			float scaledScore = eval.getScore();
			if(scaledScore > 0.0f) {
				float scale = ltiNode.getScalingFactor();
				scaledScore = scaledScore / scale;
			}
			score = Float.valueOf(scaledScore);
		}
		return score;
	}
		
	private UserCourseEnvironment getUserCourseEnvironment(Identity identity, ICourse course) {
		IdentityEnvironment identityEnvironment = new IdentityEnvironment();
		identityEnvironment.setIdentity(identity);
		return new UserCourseEnvironmentImpl(identityEnvironment, course.getCourseEnvironment());
	}
	
	public List<Result> getResults() {
		return null;
	}
}
