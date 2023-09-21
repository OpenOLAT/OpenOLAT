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

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;
import org.imsglobal.basiclti.BasicLTIUtil;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.User;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.Tracing;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.session.UserSessionManager;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.BasicLTICourseNode;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.ims.lti.LTIManager;
import org.olat.ims.lti13.LTI13Constants.Errors;
import org.olat.ims.lti13.LTI13Constants.MessageTypes;
import org.olat.ims.lti13.LTI13Constants.OpenOlatClaims;
import org.olat.ims.lti13.LTI13Constants.UserAttributes;
import org.olat.ims.lti13.manager.LTI13ContentItemClaimParser;
import org.olat.ims.lti13.manager.LTI13PlatformSigningPrivateKeyResolver;
import org.olat.ims.lti13.manager.LTI13ToolSigningKeyResolver;
import org.olat.ims.lti13.model.JwtToolBundle;
import org.olat.ims.lti13.model.json.AccessToken;
import org.olat.ims.lti13.model.json.Context;
import org.olat.ims.lti13.model.json.LaunchPresentation;
import org.olat.ims.lti13.model.json.LineItem;
import org.olat.ims.lti13.model.json.LineItemScore;
import org.olat.ims.lti13.model.json.Member;
import org.olat.ims.lti13.model.json.MembershipContainer;
import org.olat.ims.lti13.model.json.Result;
import org.olat.ims.lti13.ui.LTI13ChooseResourceController;
import org.olat.ims.lti13.ui.LTI13ChooseResourceEvent;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.util.IOUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;

/**
 * OpenOlat acts as a platform and authorize external tools configured
 * in a course element to launch.
 * <ul>
 * 	<li>lti_message_hint: identity primary key
 *  <li>login_hint:
 * </ul>
 * 
 * Initial date: 17 févr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LTI13PlatformDispatcherDelegate {
	
	private static final Logger log = Tracing.createLoggerFor(LTI13PlatformDispatcherDelegate.class);
	
	private static final String cookieName = "JSESSIONID";

	@Autowired
	private DB dbInstance;
	@Autowired
	private LTIManager ltiManager;
	@Autowired
	private LTI13Module lti13Module;
	@Autowired
	private I18nManager i18nManager;
	@Autowired
	private LTI13Service lti13Service;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private UserSessionManager userSessionMgr;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CoordinatorManager coordinatorManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private LTI13ContentItemClaimParser lti13ContentItemClaimParser;


	/**
	 * OpenOlat authorize an external tool to do something.
	 * 
	 * @param request The HTTP request
	 * @param response The HTTP response
	 */
	public void handleAuthorization(HttpServletRequest request, HttpServletResponse response) {
		UserSession result = userSessionMgr.getUserSession(request);
		Identity identity = result.getIdentity();
		String messageHintString = request.getParameter("lti_message_hint");
		Claims messageHint = null;
		if(identity == null && StringHelper.isLong(messageHintString)) {
			// H5P doesn't respect session cookies / compatibility mode
			identity = securityManager.loadIdentityByKey(Long.valueOf(messageHintString));
		} else if(StringHelper.containsNonWhitespace(messageHintString) && !StringHelper.isLong(messageHintString)) {
			Jws<Claims> messageHintJws = parseClaimsHint(messageHintString);
			if(messageHintJws != null) {
				messageHint = messageHintJws.getBody();
				Long identityKey = messageHint.get("identityKey", Long.class);
				if(identity == null) {
					identity = securityManager.loadIdentityByKey(identityKey);
				}
			}
		}
		
		String nonce = request.getParameter("nonce");
		String state = request.getParameter("state");
		String redirectUri = request.getParameter("redirect_uri");
		Jws<Claims> loginHintJws = parseClaimsHint(request.getParameter("login_hint"));
		log.debug("Start Authorization: state: {} redirectUri: {} loginHint: {} nonce: {}", state, redirectUri, loginHintJws, nonce);
		if(loginHintJws != null) {
			Claims loginHint = loginHintJws.getBody();
			Long deploymentKey = loginHint.get("deploymentKey", Long.class);
			Long contentItemKey = loginHint.get("contentItemKey", Long.class);
			
			LTI13ToolDeployment deployment;
			LTI13ContentItem contentItem = null;
			if(contentItemKey != null) {
				contentItem = lti13Service.getContentItemByKey(contentItemKey);
				deployment = contentItem.getDeployment();
			} else if(deploymentKey != null) {
				deployment = lti13Service.getToolDeploymentByKey(deploymentKey);
			} else {
				DispatcherModule.sendBadRequest("No deployment found ", response);
				return;
			}
			
			if(isRedirectUriAllowed(redirectUri, deployment)) {
				Map<String,String> params = new HashMap<>();
				params.put("state", state);
				String idToken = generateIdToken(identity, loginHint, messageHint, deployment, contentItem, nonce);
				params.put("id_token", idToken);
				sendFormRedirect(request, response, redirectUri, params);
			} else {
				DispatcherModule.sendBadRequest("Unregistered redirect_uri " + redirectUri, response);
			}
		} else {
			DispatcherModule.sendBadRequest("", response);
		}
	}
	
	protected boolean isRedirectUriAllowed(String redirectUri, LTI13ToolDeployment deployment) {
		if(deployment == null || deployment.getTool() == null) return false;
		
		LTI13Tool tool = deployment.getTool();
		if(!StringHelper.containsNonWhitespace(tool.getRedirectUrl())) {
			return true;
		}
		String[] urls = tool.getRedirectUrl().split("[\\r\\n]");
		for(String url:urls) {
			if(isRedirectUriAllowed(redirectUri, url)) {
				return true;
			}
		}
		log.warn("LTI Redirect URI not allowed {} for tool: {} with urls: {} ", redirectUri, tool.getKey(), urls);
		return false;
	}
	
	private boolean isRedirectUriAllowed(String redirectUri, String referenceUri) {
		if(redirectUri.equals(referenceUri)) {
			return true;
		}
		
		int index = referenceUri.indexOf('?');
		if(index >= 1) {
			String referenceUriWithoutParameters = referenceUri.substring(0, index);
			if(redirectUri.equals(referenceUriWithoutParameters)) {
				return true;
			}
		}
		return false;
	}
	
	private Jws<Claims> parseClaimsHint(String hint) {
		LTI13PlatformSigningPrivateKeyResolver signingResolver = new LTI13PlatformSigningPrivateKeyResolver();
		return Jwts.parserBuilder()
				.setSigningKeyResolver(signingResolver)
				.build()
				.parseClaimsJws(hint);
	}
	
	/**
	 * OpenOlat start the launch procedure of an external LTI 1.3 tool.
	 * 
	 * @param identity The identity of the user who try to launch something
	 * @param deployment The tool deployment
	 * @param contentItem The content item (optional)
	 * @return The id_token, signed
	 */
	private String generateIdToken(Identity identity, Claims loginHint, Claims messageHint,
			LTI13ToolDeployment deployment, LTI13ContentItem contentItem, String nonce) {
		Date expirationDate = DateUtils.addMinutes(new Date(), 60);
		
		LTI13Tool tool = deployment.getTool();
		String deploymentId = deployment.getDeploymentId();
		String clientId = tool.getClientId();
		String targetUrl = findTargetUrl(deployment, contentItem);
		String sub = lti13Service.subIdentity(identity, tool.getToolDomain());
		LTI13Key platformKey = lti13Service.getLastPlatformKey();
		
		JwtBuilder builder = Jwts.builder()
			//headers
			.setHeaderParam(LTI13Constants.Keys.TYPE, LTI13Constants.Keys.JWT)
			.setHeaderParam(LTI13Constants.Keys.ALGORITHM, platformKey.getAlgorithm())
			.setHeaderParam(LTI13Constants.Keys.KEY_IDENTIFIER, platformKey.getKeyId())
			//body
			.setIssuedAt(new Date())
			.setExpiration(expirationDate)
			.setIssuer(lti13Module.getPlatformIss())
			.setAudience(clientId)
			.setSubject(sub)
			.claim("nonce", nonce);
		
		String messageType;
		LTI13Constants.Roles defaultRole = null;
		if(messageHint != null && MessageTypes.LTI_DEEP_LINKING_REQUEST.equals(messageHint.get("messageType", String.class))) {
			messageType = MessageTypes.LTI_DEEP_LINKING_REQUEST;
			defaultRole = LTI13Constants.Roles.INSTRUCTOR;
			appendDeepLinkSettings(deploymentId, messageHint, builder);
		} else {
			messageType = MessageTypes.LTI_RESOURCE_LINK_REQUEST;
		}
	
		builder = builder
				.claim(LTI13Constants.Claims.MESSAGE_TYPE.url(), messageType);
		appendResourceClaims(deployment, builder);
		appendRolesClaim(deployment, loginHint, builder, defaultRole);
		appendContext(deployment, builder);
		appendNameAndRolesProvisioningServices(deployment, builder);
		appendPresentation(deployment, builder);
		appendAttributes(identity, deployment, builder);
		appendCustomAttributes(identity, deployment, contentItem, builder);
		appendAGSClaims(deployment, builder);
		
		builder
			.claim(LTI13Constants.Claims.VERSION.url(), "1.3.0")
			.claim(LTI13Constants.Claims.DEPLOYMENT_ID.url(), deploymentId)
			.claim(LTI13Constants.Claims.TARGET_LINK_URI.url(), targetUrl);
		
		return builder
			.signWith(platformKey.getPrivateKey())
			.compact();
	}
	
	private String findTargetUrl(LTI13ToolDeployment deployment, LTI13ContentItem contentItem) {
		String targetUrl = null;
		if(contentItem != null) {
			targetUrl = contentItem.getUrl();
		}
		if(!StringHelper.containsNonWhitespace(targetUrl)) {
			targetUrl = deployment.getTargetUrl();
		}		
		if(!StringHelper.containsNonWhitespace(targetUrl)) {
			targetUrl = deployment.getTool().getToolUrl();
		}
		return targetUrl;
	}
	
	private void appendDeepLinkSettings(String deploymentId, Claims messageHint, JwtBuilder builder) {
		Map<String,Object> settings = new HashMap<>();
		String deepLinkReturnUrl = Settings.createServerURI() + "/lti/dl/" + deploymentId;
		settings.put("deep_link_return_url", deepLinkReturnUrl);
		settings.put("accept_types", List.of(LTI13ContentItemTypesEnum.ltiResourceLink.name(), LTI13ContentItemTypesEnum.link.name(),
				LTI13ContentItemTypesEnum.image.name(), LTI13ContentItemTypesEnum.html.name(), LTI13ContentItemTypesEnum.file.name()));
		settings.put("accept_presentation_document_targets", List.of("iframe","window","embed"));
		settings.put("accept_media_types", "image/:::asterisk:::,text/html");
		settings.put("accept_multiple", Boolean.TRUE);
		Boolean acceptLineItem = Boolean.FALSE;
		if(messageHint != null && messageHint.get(OpenOlatClaims.REPOSITORY_ENTRY_KEY) != null
				 && messageHint.get(OpenOlatClaims.SUB_IDENT) != null) {
			acceptLineItem = Boolean.TRUE;
		}
		settings.put("accept_lineitem", acceptLineItem);
		settings.put("auto_create", Boolean.TRUE);
		
		String returnUrl = messageHint != null ? messageHint.get(OpenOlatClaims.RETURN_URL, String.class) : null;
		if(StringHelper.containsNonWhitespace(returnUrl)) {
			settings.put("data", returnUrl);
		}
		builder.claim(LTI13Constants.Claims.DL_DEEP_LINK_SETTINGS.url(), settings);
	}
	
	private void appendRolesClaim(LTI13ToolDeployment deployment, Claims loginHint, JwtBuilder builder, LTI13Constants.Roles defaultRole) {
		Set<String> roles = new LinkedHashSet<>();
		Boolean courseAdmin = loginHint.get("courseadmin", Boolean.class);
		if(courseAdmin != null && courseAdmin.booleanValue()) {
			roles.addAll(LTI13Constants.Roles.editorToRoleV2(deployment.getAuthorRolesList()));
		}
		Boolean coach = loginHint.get("coach", Boolean.class);
		if(coach != null && coach.booleanValue()) {
			roles.addAll(LTI13Constants.Roles.editorToRoleV2(deployment.getCoachRolesList()));
		}
		Boolean participant = loginHint.get("participant", Boolean.class);
		if(participant != null && participant.booleanValue()) {
			roles.addAll(LTI13Constants.Roles.editorToRoleV2(deployment.getParticipantRolesList()));
		}
		if(roles.isEmpty() && defaultRole != null) {
			// at least one role is often mandatory
			roles.add(defaultRole.roleV2());
		}
		builder.claim(LTI13Constants.Claims.ROLES.url(), roles);
	}
	
	private void appendAttributes(Identity identity, LTI13ToolDeployment deployment, JwtBuilder builder) {
		List<String> sendAttributes = deployment.getSendUserAttributesList();
		for(UserAttributes userAttribute:UserAttributes.values()) {
			if(sendAttributes.contains(userAttribute.openolatAttribute())) {
				String val = identity.getUser().getProperty(userAttribute.openolatAttribute(), Locale.ENGLISH);
				builder.claim(userAttribute.ltiAttribute(), val);
			}
		}
	}
	
	private void appendCustomAttributes(Identity identity, LTI13ToolDeployment deployment, LTI13ContentItem contentItem, JwtBuilder builder) {
		String custom = deployment.getSendCustomAttributes();
		Map<String,Object> map = new LinkedHashMap<>();
		if(StringHelper.containsNonWhitespace(custom)) {
			appendCustomAttributes(custom, identity, deployment, map);
		}
		
		if(contentItem != null && StringHelper.containsNonWhitespace(contentItem.getCustom())) {
			Map<Object,Object> customMap = lti13ContentItemClaimParser.customToObject(contentItem.getCustom());
			appendCustomAttributes(customMap, map);
		}

		if(!map.isEmpty()) {
			builder.claim(LTI13Constants.Claims.CUSTOM.url(), map);
		}
	}
	
	private void appendCustomAttributes(Map<Object,Object> customMap, Map<String,Object> map) {
		if(customMap == null || customMap.isEmpty()) return;
		
		for(Map.Entry<Object, Object> entry:customMap.entrySet()) {
			String key = entry.getKey().toString();
			Object value = entry.getValue();
			map.put(key, value);
		}
	}
	
	private void appendCustomAttributes(String custom, Identity identity, LTI13ToolDeployment deployment, Map<String,Object> map) {
		String[] params = custom.split("[\n;]");
		for (int i = 0; i < params.length; i++) {
			String[] pair = getKeyPair(params[i]);
			if(pair.length == 2) {
				final String prop = pair[0];
				String value = pair[1];
				if(value.startsWith(LTIManager.USER_PROPS_PREFIX)) {
					String userProp = value.substring(LTIManager.USER_PROPS_PREFIX.length(), value.length());
					if(LTIManager.USER_NAME_PROP.equals(userProp)) {
						value = ltiManager.getUsername(identity);
					} else {
						value = identity.getUser().getProperty(userProp, null);
					}
					if (value != null) {
						map.put(userProp, value);
					}
				} else if (value.startsWith(LTIManager.COURSE_INFO_PREFIX)) {
					String key = value.substring(LTIManager.COURSE_INFO_PREFIX.length());
					switch (key) {
						case LTIManager.COURSE_INFO_COURSE_ID -> value = deployment.getEntry().getKey().toString();
						case LTIManager.COURSE_INFO_COURSE_URL -> {
							String businessPath = "[RepositoryEntry:" + deployment.getEntry().getKey() + "]";
							value = BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathStrings(businessPath);
						}
						case LTIManager.COURSE_INFO_NODE_ID -> value = deployment.getSubIdent();
						case LTIManager.COURSE_INFO_NODE_URL -> {
							String businessPath = "[RepositoryEntry:" + deployment.getEntry().getKey() + "][CourseNode:" + deployment.getSubIdent() + "]";
							value = BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathStrings(businessPath);
						}
					}
					if (value != null) {
						map.put(prop, value);
					}
				}
				if (value != null) {
					map.put(prop, value);
				}
			}
		}
	}
	
	private String[] getKeyPair(String param) {
		if (!StringHelper.containsNonWhitespace(param)) {
			return new String[0];
		}
		
		int pos = param.indexOf("=");
		if (pos < 1 || pos + 1 > param.length()) {
			return new String[0];
		}
		
		String key = BasicLTIUtil.mapKeyName(param.substring(0, pos));
		if(!StringHelper.containsNonWhitespace(key)) {
			return new String[0];
		}
		
		String value = param.substring(pos + 1).trim();
		if(value.length() < 1) {
			return new String[0];
		}
		return new String[] { key, value };
	}
	
	private void appendPresentation(LTI13ToolDeployment deployment, JwtBuilder builder) {
		LaunchPresentation launchPresentation = new LaunchPresentation();
		launchPresentation.setDocumentTarget(deployment.getDisplayOptions().lti13Value());
		launchPresentation.setHeight(toSize(deployment.getDisplayHeight()));
		launchPresentation.setWidth(toSize(deployment.getDisplayWidth()));
		builder.claim(LTI13Constants.Claims.LAUNCH_PRESENTATION.url(), launchPresentation);
	}
	
	private Integer toSize(String value) {
		if(StringHelper.containsNonWhitespace(value) && !BasicLTICourseNode.CONFIG_HEIGHT_AUTO.equals(value)) {
			value = value.replace("px", "");
			if(StringHelper.isLong(value)) {
				return Integer.valueOf(value);
			}
		}
		return null;
	}
	
	private void appendContext(LTI13ToolDeployment deployment, JwtBuilder builder) {
		Map<String,Object> contextMap = new LinkedHashMap<>();
		contextMap.put("id", deployment.getContextId());
		if(deployment.getEntry() != null) {
			RepositoryEntry entry = deployment.getEntry();
			contextMap.put("label", entry.getDisplayname());
			contextMap.put("title", entry.getDisplayname());
			contextMap.put("type", List.of(LTI13Constants.ContextTypes.COURSE_SECTION));
		} else if(deployment.getBusinessGroup() != null) {
			BusinessGroup businessGroup = deployment.getBusinessGroup();
			contextMap.put("label", businessGroup.getName());
			contextMap.put("title", businessGroup.getName());
			contextMap.put("type", List.of(LTI13Constants.ContextTypes.GROUP));
		}
		builder.claim(LTI13Constants.Claims.CONTEXT.url(), contextMap);
	}
	
	private void appendNameAndRolesProvisioningServices(LTI13ToolDeployment deployment, JwtBuilder builder) {
		if(!deployment.isNameAndRolesProvisioningServicesEnabled()) return;
		
		Map<String,Object> nrpsMap = new LinkedHashMap<>();
		nrpsMap.put("context_memberships_url", getNameAndRolesProvisioningServicesURL(deployment));
		nrpsMap.put("service_versions", List.of("2.0"));
		builder.claim(LTI13Constants.Claims.NAMES_AND_ROLES_SERVICE.url(), nrpsMap);
	}
	
	public static String getNameAndRolesProvisioningServicesURL(LTI13ToolDeployment deployment) {
		if(deployment.getEntry() != null && deployment.getEntry().toString().equals(deployment.getContextId())) {
			// This is the beta version of the URL before 17.0
			return Settings.getServerContextPathURI() + LTI13Dispatcher.LTI_NRPS_PATH;
		}
		return Settings.getServerContextPathURI() + LTI13Dispatcher.LTI_NRPS_PATH + "/" + deployment.getContextId() + "/memberships/";
	}
	
	/**
	 * The assignment and grading service is only supported on course element of courses.
	 * 
	 * @param deployment The tool deployment
	 * @param builder The JWT to enhance with an ASG claim
	 */
	private void appendAGSClaims(LTI13ToolDeployment deployment, JwtBuilder builder) {
		if(!deployment.isAssessable() || deployment.getEntry() == null || !StringHelper.containsNonWhitespace(deployment.getSubIdent())) return;
		
		Map<String,Object> map = new LinkedHashMap<>();
		if(StringHelper.containsNonWhitespace(deployment.getSubIdent())) {
			List<String> agsScopes = List.of(LTI13Constants.Scopes.AGS_LINE_ITEM.url(),
					LTI13Constants.Scopes.AGS_LINE_ITEM_READ_ONLY.url(),
					LTI13Constants.Scopes.AGS_RESULT_READ_ONLY.url(),
					LTI13Constants.Scopes.AGS_SCORE.url());
			map.put("scope", agsScopes);
			//	    /{contextId}/lineitems/
			map.put("lineitems", getLineItemsURL(deployment));
			//      /{contextId}/lineitems/{lineItemId}/lineitem
			// GET  /{contextId}/lineitems/{lineItemId}/lineitem/results
			// POST /{contextId}/lineitems/{lineItemId}/lineitem/scores
			map.put("lineitem", getLineItemURL(deployment));
		}
		builder.claim(LTI13Constants.Claims.ASSIGNMENT_AND_GRADING_SERVICE.url(), map);
	}
	
	public static String getLineItemsURL(LTI13ToolDeployment deployment) {	
		RepositoryEntry re = deployment.getEntry();
		OLATResource resource = re.getOlatResource();
		return Settings.getServerContextPathURI() + LTI13Dispatcher.LTI_AGS_PATH + "/" + deployment.getKey()
			+ "/context/" + resource.getResourceableId() + "/lineitems/";
	}
	
	public static String getLineItemURL(LTI13ToolDeployment deployment) {
		return getLineItemsURL(deployment) + deployment.getSubIdent() + "/lineitem";
	}
	
	private void appendResourceClaims(LTI13ToolDeployment deployment, JwtBuilder builder) {
		Map<String,String> resourceLink = new LinkedHashMap<>();

		if(deployment.getEntry() != null && StringHelper.containsNonWhitespace(deployment.getSubIdent())) {
			RepositoryEntry entry = deployment.getEntry();
			resourceLink.put("id", deployment.getDeploymentResourceId());
			resourceLink.put("title", entry.getDisplayname());
			if(StringHelper.containsNonWhitespace(entry.getDescription())) {
				resourceLink.put("description", entry.getDescription());
			}
		} else if(deployment.getBusinessGroup() != null) {
			BusinessGroup businessGroup = deployment.getBusinessGroup();
			resourceLink.put("id", deployment.getDeploymentResourceId());
			resourceLink.put("title", businessGroup.getName());
			if(StringHelper.containsNonWhitespace(businessGroup.getDescription())) {
				resourceLink.put("description", businessGroup.getDescription());
			}
		}
		
		builder.claim(LTI13Constants.Claims.RESOURCE_LINK.url(), resourceLink);
	}
	
	private void sendFormRedirect(HttpServletRequest request, HttpServletResponse response, String redirectUri, Map<String,String> params) {
		StringBuilder sb = new StringBuilder();
		sb.append("<!DOCTYPE html>\n");
		sb.append("<html lang=\"en\">\n");
		sb.append(" <head><title>Redirect</title></head>\n");
		sb.append("<body><h4>Loading...</h4>\n");
		sb.append("<form action=\"").append(redirectUri).append("\" name=\"ltiAuthForm\" id=\"ltiAuthForm\" method=\"post\" enctype=\"application/x-www-form-urlencoded\">\n");
		if(!params.isEmpty()) {
		    for(Map.Entry<String,String> param:params.entrySet()) {
		    	String key = param.getKey();
		    	String value = param.getValue();
		    	sb.append("  <input type=\"hidden\" name=\"").append(key).append("\" value=\"").append(value).append("\"/>\n");
		    }
		}
		sb.append("Wait</form>\n");
		sb.append("<script>\n");
		sb.append(" document.ltiAuthForm.submit();\n");
		sb.append("</script>\n");
		sb.append("</body>");

		String sessionCookie = ServletUtil.getCookie(request, cookieName);
		log.debug("Send redirect with cookie: {}", sessionCookie);
		ServletUtil.setStringResourceHeaders(response);
		try(Writer writer= response.getWriter()) {
			writer.append(sb.toString());
		} catch (IOException unlikely) {
			log.error("failed redirect {}", unlikely.getMessage());
			DispatcherModule.sendBadRequest("Redirect failed " + unlikely.getMessage(), response);
		}
	}
	
	/////////////////////////
	// Handle the platform public key
	////////////////////////
	
	public void handleKeys(HttpServletResponse response) {
		ServletUtil.setJSONResourceHeaders(response);
		try(Writer w=response.getWriter()) {
			List<LTI13Key> platformKeys = lti13Service.getPlatformKeys();
			String encoded = LTI13JsonUtil.publicKeysToJwks(platformKeys);
			w.append(encoded);
		} catch(IOException e) {
			log.error("", e);
		}
	}
	
	public void handleKey(String kid, HttpServletResponse response) {
		ServletUtil.setJSONResourceHeaders(response);
		try(Writer w=response.getWriter()) {
			PublicKey key = lti13Service.getPlatformPublicKey(kid);
			if(key != null) {
				String encoded = LTI13JsonUtil.publicKeysToJwks(kid, key);
				w.append(encoded);
			} else {
				DispatcherModule.sendNotFound(response);
			}
		} catch(IOException e) {
			log.error("", e);
		}
	}
	
	/////////////////////////
	// Handle tokens to access the services
	////////////////////////
	
	public void handleToken(HttpServletRequest request, HttpServletResponse response) {
		String grantType = request.getParameter(LTI13Constants.OAuth.GRANT_TYPE);
		if(LTI13Constants.OAuth.CLIENT_CREDENTIALS.equals(grantType)) {
			handleTokenClientCredentials(request, response);
		} else {
			DispatcherModule.sendBadRequest(grantType, response);
		}
	}
	
	public void handleTokenClientCredentials(HttpServletRequest request, HttpServletResponse response) {
		String clientassertion = request.getParameter(LTI13Constants.OAuth.CLIENT_ASSERTION);
		JwtToolBundle bundle = lti13Service.getAndVerifyClientAssertion(clientassertion);
		handleTokenClientCredentials(bundle.getJwt(), bundle.getTool(), request, response);
	}
	
	public void handleToken(String obj, HttpServletRequest request, HttpServletResponse response) {
		String grantType = request.getParameter(LTI13Constants.OAuth.GRANT_TYPE);
		if(LTI13Constants.OAuth.CLIENT_CREDENTIALS.equals(grantType)) {
			handleTokenClientCredentials(obj, request, response);
		} else {
			DispatcherModule.sendBadRequest(grantType, response);
		}
	}

	/**
	 * 
	 * @param request The HTTP request
	 * @param response The HTTP response
	 */
	private void handleTokenClientCredentials(String obj, HttpServletRequest request, HttpServletResponse response) {
		if(!StringHelper.isLong(obj)) {
			DispatcherModule.sendBadRequest("", response);
			return;
		}
		
		try {
			String clientassertion = request.getParameter(LTI13Constants.OAuth.CLIENT_ASSERTION);
			JwtToolBundle bundle = lti13Service.getAndVerifyClientAssertion(clientassertion);
			handleTokenClientCredentials(bundle.getJwt(), bundle.getTool(), request, response);
		} catch (Exception e) {
			log.error("",  e);
		}
	}
	
	private void handleTokenClientCredentials(Jwt<?,?> jwt, LTI13Tool tool, HttpServletRequest request, HttpServletResponse response) {
		String scope = request.getParameter(LTI13Constants.OAuth.SCOPE);
		Claims body = (Claims)jwt.getBody();
		String subject = body.getSubject();
		String toolIss = body.getIssuer();
		String clientId = tool.getClientId();
		if(!subject.equals(clientId)) {
			DispatcherModule.sendBadRequest("Bad client id", response);
			return;
		}

		Date expirationDate = DateUtils.addMinutes(new Date(), 60);
		LTI13Key platformKey = lti13Service.getLastPlatformKey();
		
		JwtBuilder builder = Jwts.builder()
			// Encryption parameters
			.setHeaderParam(LTI13Constants.Keys.TYPE, LTI13Constants.Keys.JWT)
			.setHeaderParam(LTI13Constants.Keys.ALGORITHM, platformKey.getAlgorithm())
			.setHeaderParam(LTI13Constants.Keys.KEY_IDENTIFIER, platformKey.getKeyId())
			//
			.setIssuedAt(new Date())
			.setExpiration(expirationDate)
			.setIssuer(lti13Module.getPlatformIss())
			.setAudience(toolIss)
			.setSubject(clientId)
			.claim(LTI13Constants.OAuth.SCOPE, scope);

		String jwtString = builder
				.signWith(platformKey.getPrivateKey())
				.compact();
		
		AccessToken at = new AccessToken();
		at.setAccessToken(jwtString);
		sendJSON(at, response);
	}
	
	private Jws<Claims> getAccessToken(HttpServletRequest request) {
		String authorization = request.getHeader("authorization");
		if(authorization.startsWith("Bearer ")) {
			authorization = authorization.substring("Bearer ".length());
			try {
				LTI13PlatformSigningPrivateKeyResolver resolver = new LTI13PlatformSigningPrivateKeyResolver();
				return Jwts.parserBuilder()
					.setSigningKeyResolver(resolver)
					.build()
					.parseClaimsJws(authorization);
			} catch (Exception e) {
				log.error("", e);
			}
		}
		return null;
	}
	
	////////////////////////
	// Names services
	////////////////////////
	
	public void handleNrps(String[] path, HttpServletRequest request, HttpServletResponse response) {
		String accept = request.getHeader("accept");
		if(!"application/vnd.ims.lti-nrps.v2.membershipcontainer+json".equals(accept) && !"*/*".equals(accept)) {
			DispatcherModule.sendBadRequest("", response);
			return;
		}

		Jws<Claims> jws = getAccessToken(request);
		if(jws == null) {
			DispatcherModule.sendForbidden("", response);
		} else {
			Claims claims = jws.getBody();
			String toolIss = claims.getAudience();
			String clientId = claims.getSubject();
			
			if(path.length == 1 && "nrps".equals(path[0])) {
				LTI13Tool tool = lti13Service.getToolBy(toolIss, clientId);
				List<LTI13ToolDeployment> deployments = lti13Service.getToolDeployments(tool);
				if(deployments.size() == 1) {
					MembershipContainer container = handleNrps(deployments.get(0));
					sendJSON(container, response);
				}
			} else if(path.length == 3 && "nrps".equals(path[0]) && StringHelper.containsNonWhitespace(path[1]) && "memberships".equals(path[2])) {
				LTI13ToolDeployment deployment = lti13Service.getToolDeploymentByContextId(path[1]);
				if(deployment != null) {
					MembershipContainer container = handleNrps(deployment);
					sendJSON(container, response);
				}
			}
		}
	}
	
	private MembershipContainer handleNrps(LTI13ToolDeployment deployment) {
		if(deployment.getEntry() != null) {
			return handleNrpsRepositoryEntry(deployment);
		}
		if(deployment.getBusinessGroup() != null) {
			return handleNrpsBusinessGroup(deployment);
		}
		return null;
	}
	
	private MembershipContainer handleNrpsRepositoryEntry(LTI13ToolDeployment deployment) {
		List<Identity> participants = repositoryService.getMembers(deployment.getEntry(), RepositoryEntryRelationType.defaultGroup, GroupRoles.participant.name());
		
		MembershipContainer container = new MembershipContainer();
		RepositoryEntry entry = deployment.getEntry();
		container.setId(Settings.getServerContextPathURI() + "/auth/RepositoryEntry/" + entry.getKey());
		
		Context context = new Context();
		context.setId(deployment.getContextId());
		context.setLabel(entry.getDisplayname());
		context.setTitle(entry.getDisplayname());
		container.setContext(context);

		container.setMembers(toLtiLearners(participants));
		
		return container;
	}
	
	private MembershipContainer handleNrpsBusinessGroup(LTI13ToolDeployment deployment) {
		List<Identity> participants = businessGroupService.getMembers(deployment.getBusinessGroup(), GroupRoles.participant.name());
		
		MembershipContainer container = new MembershipContainer();
		BusinessGroup businessGroup = deployment.getBusinessGroup();
		container.setId(Settings.getServerContextPathURI() + "/auth/BusinessGroup/" + businessGroup.getKey());
		
		Context context = new Context();
		context.setId(deployment.getContextId());
		context.setLabel(businessGroup.getName());
		context.setTitle(businessGroup.getName());
		container.setContext(context);
		
		container.setMembers(toLtiLearners(participants));
		
		return container;
	}
	
	private List<Member> toLtiLearners(List<Identity> identities) {
		List<Member> members = new ArrayList<>();
		if(identities != null) {
			for(Identity identity:identities) {
				User user = identity.getUser();
				Member member = new Member();
				member.setStatus("Active");
				member.setGivenName(user.getFirstName());
				member.setFamilyName(user.getLastName());
				member.setName(userManager.getUserDisplayName(identity));
				member.setEmail(user.getEmail());
				member.setUserId(identity.getKey().toString());
				member.setRoles(List.of(LTI13Constants.Roles.LEARNER.name()));
				members.add(member);
			}
		}
		return members;
	}
	
	public void handleDl(String[] path, HttpServletRequest request, HttpServletResponse response) {
		// /dl/{deploymentId}
		try {
			HttpSession session = request.getSession(false);
			String jwt = request.getParameter("JWT");
			LTI13ToolDeployment deployment = lti13Service.getToolDeploymentByDeploymentId(path[1]);
			Jws<Claims> jws = Jwts.parserBuilder()
					.setSigningKeyResolver(new LTI13ToolSigningKeyResolver(deployment.getTool()))
					.build()
					.parseClaimsJws(jwt);
			
			Claims body = jws.getBody();
			String messageType = body.get(LTI13Constants.Claims.MESSAGE_TYPE.url(), String.class);
			String deploymentId = body.get(LTI13Constants.Claims.DEPLOYMENT_ID.url(), String.class);
			if(MessageTypes.LTI_DEEP_LINKING_RESPONSE.equals(messageType) && deployment.getDeploymentId().equals(deploymentId)) {
				lti13Service.createContentItems(body, deployment);
				dbInstance.commitAndCloseSession();
				String returnUrl = body.get(LTI13Constants.Claims.DL_DATA.url(), String.class);
				// Trigger the close button only if the session is an old one
				if(StringHelper.containsNonWhitespace(returnUrl) && session != null && !session.isNew()) {
					handleDlClose(response, returnUrl);
				} else {
					OLATResourceable ltiResourceOres = OresHelper.createOLATResourceableInstance(MessageTypes.LTI_DEEP_LINKING_RESPONSE, deployment.getKey());
					coordinatorManager.getCoordinator().getEventBus().fireEventToListenersOf(new LTI13ChooseResourceEvent(deploymentId), ltiResourceOres);
					handleDlWait(response);
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	private void handleDlWait(HttpServletResponse response) {
		try(StringOutput clientSideWindowCheck = new StringOutput()) {
			Translator translator = Util.createPackageTranslator(LTI13ChooseResourceController.class, i18nManager.getLocaleOrDefault(null));
			clientSideWindowCheck.append("<!DOCTYPE html>\n<html><head><title>Reload</title></head><body>")
			  .append(translator.translate("wait.little"))
			  .append("</body></html>");
			ServletUtil.serveStringResource(response, clientSideWindowCheck);
		} catch(IOException e) {
			log.error("", e);
		}
	}
	
	private void handleDlClose(HttpServletResponse response, String returnUrl) {
		try(StringOutput clientSideWindowCheck = new StringOutput()) {
			Translator translator = Util.createPackageTranslator(LTI13ChooseResourceController.class, i18nManager.getLocaleOrDefault(null));
			clientSideWindowCheck.append("<!DOCTYPE html>\n<html><head><title>Reload</title><script>")
				.append("parent.document.location.href='").append(returnUrl).append("';")
				.append("</script></head><body>")
				.append(translator.translate("wait.little"))
				.append("</body></html>");
			ServletUtil.serveStringResource(response, clientSideWindowCheck);
		} catch(IOException e) {
			log.error("", e);
		}
	}
	
	//////////////////
	// Assignment and grading service
	//////////////////
	
	public void handleAgs(String[] path, HttpServletRequest request, HttpServletResponse response) {
		// /ags/{deploymentId}/context/{entryResourceId}/lineitems/{subIdent}/lineitem
		if(path.length < 4 || !"ags".equals(path[0]) || !"lineitems".equals(path[4])
				|| !StringHelper.isLong(path[1]) || !StringHelper.isLong(path[3])) {
			DispatcherModule.sendBadRequest("", response);
			return;
		}
		
		Jws<Claims> jws = getAccessToken(request);
		if(jws == null) {
			DispatcherModule.sendForbidden("", response);
			return;
		}
		
		Long deploymentKey = Long.valueOf(path[1]);
		Long resourceId = Long.valueOf(path[3]);
		LTI13ToolDeployment deployment = lti13Service.getToolDeploymentByKey(deploymentKey);
		if(deployment == null || deployment.getEntry() == null || !resourceId.equals(deployment.getEntry().getOlatResource().getResourceableId())) {
			DispatcherModule.sendBadRequest("", response);
			return;
		}
		
		if(path.length == 5) {
			// GET /ags/{deploymentId}/{entryResourceId}/lineitems
			// LTI: /{contextId}/lineitems
			if("GET".equalsIgnoreCase(request.getMethod())) {
				handleAgsLineItems(deployment, request, response);
			} else if("POST".equalsIgnoreCase(request.getMethod())) {
				// not supported
				DispatcherModule.sendForbidden(response);
			}
		} else if(path.length == 7) {
			// GET /ags/{deploymentId}/{entryResourceId}/lineitems/{subIdent}/lineitem
			// LTI: /{contextId}/lineitems/{lineItemId}/lineitem
			if("GET".equalsIgnoreCase(request.getMethod())) {
				handleAgsLineItem(deployment, request, response);
			} else if("PUT".equalsIgnoreCase(request.getMethod())
					|| "DELETE".equalsIgnoreCase(request.getMethod())) {
				// not supported
				DispatcherModule.sendForbidden(response);
			}
		} else if(path.length >= 8 && "lineitem".equals(path[6])) {
			String ext = path[7];
			if("scores".equals(ext)) {
				handleAgsScore(deployment, request, response);
			} else if("results".equals(ext)) {
				handleAgsResult(deployment, request, response);
			}
		}  
	}
	
	private void handleAgsLineItem(LTI13ToolDeployment deployment, HttpServletRequest request, HttpServletResponse response) {
		log.debug("Handle AGS Line item: {} {}", request.getMethod(), request.getRequestURL());
		
		LineItem lineItem = lti13Service.getLineItem(deployment);
		sendJSON(lineItem, LTI13Constants.ContentTypes.LINE_ITEM_CONTENT_TYPE, response);
	}
	
	private void handleAgsLineItems(LTI13ToolDeployment deployment, HttpServletRequest request, HttpServletResponse response) {
		log.debug("Handle AGS Line items: {} {}", request.getMethod(), request.getRequestURL());

		LineItem lineItem = lti13Service.getLineItem(deployment);
		sendJSON(List.of(lineItem), LTI13Constants.ContentTypes.LINE_ITEM_CONTAINER_CONTENT_TYPE, response);
	}
	
	private void handleAgsScore(LTI13ToolDeployment deployment, HttpServletRequest request, HttpServletResponse response) {
		log.debug("Handle AGS: {} {}", request.getMethod(), request.getRequestURL());
		
		if(!request.getContentType().startsWith(LTI13Constants.ContentTypes.SCORE_CONTENT_TYPE)) {
			DispatcherModule.sendBadRequest(Errors.INVALID_CONTENT_TYPE, response);
			return;
		}
		
		LineItemScore score = null;
		try(InputStream in=request.getInputStream()) {
			String content = IOUtils.readInputStreamToString(in, StandardCharsets.UTF_8);
			score = LTI13JsonUtil.readValue(content, LineItemScore.class);
		} catch(Exception e) {
			log.error("", e);
			DispatcherModule.setServerError(response);
			return;
		}
		
		Identity identity = lti13Service.loadIdentity(score.getUserId(), deployment.getTool().getToolDomain());
		if(identity == null || deployment.getEntry() == null) {
			DispatcherModule.sendNotFound(response);
			return;
		}

		RepositoryEntry entry = deployment.getEntry();
		ICourse course = CourseFactory.loadCourse(entry);
		if(score.getScoreGiven() != null && score.getScoreMaximum() != null) {
			Float scoreGiven = Float.valueOf(score.getScoreGiven().floatValue());
			ltiManager.updateScore(identity, scoreGiven, course, deployment.getSubIdent());
		}
		response.setStatus(HttpServletResponse.SC_OK);
	}

	private void handleAgsResult(LTI13ToolDeployment deployment, HttpServletRequest request, HttpServletResponse response) {
		String userId = request.getParameter("user_id");
		if(StringHelper.containsNonWhitespace(userId)) {
			Identity identity = lti13Service.loadIdentity(userId, deployment.getTool().getToolDomain());
			if(identity != null) {
				handleAgsResult(userId, identity, deployment, response);
			} else {
				DispatcherModule.sendBadRequest("", response);
			}
		} else {
			handleAgsResults(deployment, request, response);
		}
	}

	private void handleAgsResults(LTI13ToolDeployment deployment, HttpServletRequest request, HttpServletResponse response) {
		String limit = request.getParameter("limit");
		String page = request.getParameter("page");
		
		int firstResult = 0;
		int maxResults = -1;
		if(StringHelper.isLong(limit)) {
			maxResults = Integer.parseInt(limit);
		}
		if(StringHelper.isLong(page)) {
			firstResult = maxResults * Integer.parseInt(page);
		}
		List<Result> results = lti13Service.getResults(deployment, firstResult, maxResults);
		if(results != null) {
			sendJSON(results, LTI13Constants.ContentTypes.RESULT_CONTAINER_CONTENT_TYPE, response);
		} else {
			DispatcherModule.sendNotFound(response);
		}
	}

	private void handleAgsResult(String userId, Identity identity, LTI13ToolDeployment deployment, HttpServletResponse response) {
		Result result = lti13Service.getResult(userId, identity, deployment);
		if(result == null) {
			DispatcherModule.sendNotFound(response);
		} else {
			sendJSON(List.of(result), LTI13Constants.ContentTypes.RESULT_CONTAINER_CONTENT_TYPE, response);
		}
	}
	
	private void sendJSON(Object object, HttpServletResponse response) {
		sendJSON(object, "application/json;charset=utf-8", response);
	}
	
	private void sendJSON(Object object, String contentType, HttpServletResponse response) {
		response.setContentType(contentType);
		ServletUtil.setNoCacheHeaders(response);
		try(Writer writer= response.getWriter()) {
			String json = LTI13JsonUtil.prettyPrint(object);
			writer.append(json);
		} catch (IOException unlikely) {
			log.error("failed redirect {}", unlikely.getMessage());
			DispatcherModule.sendBadRequest("Redirect failed " + unlikely.getMessage(), response);
		}
	}
}
