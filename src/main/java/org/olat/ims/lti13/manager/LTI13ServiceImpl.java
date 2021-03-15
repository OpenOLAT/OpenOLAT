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

import java.net.URL;
import java.security.Key;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

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
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.id.User;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.cache.CacheWrapper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.crypto.CryptoUtil;
import org.olat.core.util.i18n.I18nManager;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupRef;
import org.olat.group.BusinessGroupService;
import org.olat.group.DeletableGroupData;
import org.olat.ims.lti13.LTI13Constants;
import org.olat.ims.lti13.LTI13Constants.UserSub;
import org.olat.ims.lti13.LTI13Key;
import org.olat.ims.lti13.LTI13Module;
import org.olat.ims.lti13.LTI13Service;
import org.olat.ims.lti13.LTI13SharedTool;
import org.olat.ims.lti13.LTI13SharedToolDeployment;
import org.olat.ims.lti13.LTI13SharedToolService;
import org.olat.ims.lti13.LTI13SharedToolService.ServiceType;
import org.olat.ims.lti13.LTI13Tool;
import org.olat.ims.lti13.LTI13ToolDeployment;
import org.olat.ims.lti13.LTI13ToolType;
import org.olat.ims.lti13.OIDCApi;
import org.olat.ims.lti13.model.AccessTokenKey;
import org.olat.ims.lti13.model.AccessTokenTimed;
import org.olat.ims.lti13.model.LTI13SharedToolImpl;
import org.olat.ims.lti13.model.LTI13SharedToolWithInfos;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryDataDeletable;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
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
	private LTI13SharedToolDAO lti13SharedToolDao;
	@Autowired
	private CoordinatorManager coordinatorManager;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private LTI13ToolDeploymentDAO lti13ToolDeploymentDao;
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
		lti13SharedToolDao.deleteSharedTools(re);
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
		lti13SharedToolDao.deleteSharedTools(group);
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
	public LTI13Tool createExternalTool(String toolName, String toolUrl, String clientId, String initiateLoginUrl, LTI13ToolType type) {
		return lti13ToolDao.createTool(toolName, toolUrl, clientId, initiateLoginUrl, type);
	}

	@Override
	public LTI13SharedTool createTransientSharedTool(BusinessGroup businessGroup) {
		LTI13SharedToolImpl sharedTool = createTransientSharedTool();
		sharedTool.setBusinessGroup(businessGroup);
		return sharedTool;
	}

	@Override
	public LTI13SharedTool createTransientSharedTool(RepositoryEntry entry) {
		LTI13SharedToolImpl sharedTool = createTransientSharedTool();
		sharedTool.setEntry(entry);
		return sharedTool;
	}
	
	private LTI13SharedToolImpl createTransientSharedTool() {
		LTI13SharedToolImpl sharedTool = new LTI13SharedToolImpl();
		sharedTool.setCreationDate(new Date());
		sharedTool.setLastModified(sharedTool.getLastModified());
		sharedTool.setKeyId(UUID.randomUUID().toString());
		
		KeyPair keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256);
	
		String publicEncoded = CryptoUtil.getPublicEncoded(keyPair.getPublic());
		publicEncoded = CryptoUtil.getPublicEncoded(publicEncoded);
		sharedTool.setPublicKey(publicEncoded);
		String privateEncoded = CryptoUtil.getPrivateEncoded(keyPair.getPrivate());
		privateEncoded = CryptoUtil.getPublicEncoded(privateEncoded);
		sharedTool.setPrivateKey(privateEncoded);
		
		return sharedTool;
	}

	@Override
	public LTI13SharedTool updateSharedTool(LTI13SharedTool tool) {
		return lti13SharedToolDao.updateSharedTool(tool);
	}

	@Override
	public List<LTI13SharedToolWithInfos> getSharedToolsWithInfos(RepositoryEntryRef entry) {
		return lti13SharedToolDao.getSharedTools(entry);
	}

	@Override
	public List<LTI13SharedToolWithInfos> getSharedToolsWithInfos(BusinessGroupRef businessGroup) {
		return lti13SharedToolDao.getSharedTools(businessGroup);
	}

	@Override
	public LTI13SharedTool getSharedTool(String issuer, String clientId) {
		return lti13SharedToolDao.loadByClientId(issuer, clientId);
	}

	@Override
	public LTI13SharedTool getSharedToolByKey(Long key) {
		return lti13SharedToolDao.loadByKey(key);
	}

	@Override
	public LTI13SharedToolDeployment getOrCreateSharedToolDeployment(String deploymentId, LTI13SharedTool sharedTool) {
		List<LTI13SharedToolDeployment> deployments = sharedToolDeploymentDao.getSharedToolDeployment(deploymentId, sharedTool);
		if(deployments.isEmpty()) {
			LTI13SharedToolDeployment newDeployment = sharedToolDeploymentDao.createDeployment(deploymentId, sharedTool);
			dbInstance.commit();
			return newDeployment;
		}
		if(deployments.size() == 1) {
			return deployments.get(0);
		}
		log.error("Shared tool with same deployments: {} {}", sharedTool.getKey(), deploymentId);
		return null;
	}

	@Override
	public List<LTI13SharedToolDeployment> getSharedToolDeployments(LTI13SharedTool sharedTool) {
		return sharedToolDeploymentDao.loadSharedToolDeployments(sharedTool);
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
	public Identity matchIdentity(Claims body) {
		String issuer = body.getIssuer();
		String sub = body.getSubject();
		Identity identity = loadIdentity(sub, issuer);
		if(identity == null) {
			String givenName = body.get(UserSub.GIVEN_NAME, String.class);
			String familyName = body.get(UserSub.FAMILY_NAME, String.class);
			String email = body.get(UserSub.EMAIL, String.class);
			String language = body.get(UserSub.LOCALE, String.class);
			log.debug("sub: {}, given_name: {} family_name: {}", sub, givenName, familyName);
			
			if(StringHelper.containsNonWhitespace(email) && lti13Module.isMatchingByEmailEnabled() && userModule.isEmailUnique()) {
				List<Identity> currentIdentities = userManager.findIdentitiesByEmail(List.of(email));
				if(currentIdentities.size() == 1) {
					identity = currentIdentities.get(0);
					securityManager.createAndPersistAuthentication(identity, LTI13Service.LTI_PROVIDER, issuer, sub, null, null);
					return identity;
				}
			}
			
			Locale locale = i18nManager.getLocaleOrDefault(language);
			
			Organisation ltiOrganisation = getLTIOrganisation();
			User user = userManager.createUser(givenName, familyName, email);
			user.getPreferences().setLanguage(locale.toString());
			String nickName = "l" + CodeHelper.getForeverUniqueID();
			identity = securityManager.createAndPersistIdentityAndUserWithOrganisation(null, nickName, null, user,
					LTI13Service.LTI_PROVIDER, issuer, sub, null, ltiOrganisation, null);
		}
		return identity;
	}
	
	@Override
	public void checkMembership(Identity identity, LTI13SharedTool tool) {
		if(tool.getEntry() != null) {
			// check if participant, if not
			RepositoryEntry entry = tool.getEntry();
			if(!repositoryService.hasRole(identity, entry, GroupRoles.owner.name(), GroupRoles.coach.name(), GroupRoles.participant.name())) {
				Roles roles = securityManager.getRoles(identity);
				IdentitiesAddEvent iae = new IdentitiesAddEvent(identity);
				repositoryManager.addParticipants(identity, roles, iae, entry, null);
			}
		} else if(tool.getBusinessGroup() != null) {
			BusinessGroup businessGroup = tool.getBusinessGroup();
			if(!businessGroupService.hasRoles(identity, businessGroup, GroupRoles.coach.name())
					&& !businessGroupService.hasRoles(identity, businessGroup, GroupRoles.participant.name())) {
				Roles roles = securityManager.getRoles(identity);
				businessGroupService.addParticipants(identity, roles, List.of(identity), businessGroup, null);
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
	public OAuth2AccessToken getAccessToken(LTI13SharedTool tool, List<String> scopes) {
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
}
