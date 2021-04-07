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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.imsglobal.basiclti.BasicLTIUtil;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.logging.Tracing;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.session.UserSessionManager;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.nodes.BasicLTICourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.ims.lti.LTIManager;
import org.olat.ims.lti13.LTI13Constants.Errors;
import org.olat.ims.lti13.LTI13Constants.UserAttributes;
import org.olat.ims.lti13.manager.LTI13ExternalToolSigningKeyResolver;
import org.olat.ims.lti13.manager.LTI13PlatformSigningPrivateKeyResolver;
import org.olat.ims.lti13.model.json.AccessToken;
import org.olat.ims.lti13.model.json.Context;
import org.olat.ims.lti13.model.json.LaunchPresentation;
import org.olat.ims.lti13.model.json.LineItemScore;
import org.olat.ims.lti13.model.json.Member;
import org.olat.ims.lti13.model.json.MembershipContainer;
import org.olat.ims.lti13.model.json.Result;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.util.IOUtils;

import io.jsonwebtoken.Claims;
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
	private LTIManager ltiManager;
	@Autowired
	private LTI13Module lti13Module;
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
	private CourseAssessmentService courseAssessmentService;


	/**
	 * OpenOlat authorize an external tool to do something.
	 * 
	 * @param request The HTTP request
	 * @param response The HTTP response
	 */
	public void handleAuthorization(HttpServletRequest request, HttpServletResponse response) {
		UserSession result = userSessionMgr.getUserSession(request);
		Identity identity = result.getIdentity();
		if(identity == null) {
			// H5P doesn't respect session cookies
			String messageHint = request.getParameter("lti_message_hint");
			if(StringHelper.containsNonWhitespace(messageHint)) {
				identity = securityManager.loadIdentityByKey(Long.valueOf(messageHint));
			}	
		}
		
		String nonce = request.getParameter("nonce");
		String state = request.getParameter("state");
		String redirectUri = request.getParameter("redirect_uri");
		Jwt<?,?> loginHintJwt = loginHint(request.getParameter("login_hint"));
		log.debug("Start Authorization: state: {} redirectUri: {} loginHint: {} nonce: {}", state, redirectUri, loginHintJwt, nonce);
		if(loginHintJwt != null) {
			Claims loginHint = (Claims)loginHintJwt.getBody();
			Long deploymentKey = loginHint.get("deploymentKey", Long.class);
			LTI13ToolDeployment deployment = lti13Service.getToolDeploymentByKey(deploymentKey);
			
			Map<String,String> params = new HashMap<>();
			params.put("state", state);
			String idToken = generateIdToken(identity, loginHint, deployment, nonce);
			params.put("id_token", idToken);
			sendFormRedirect(request, response, redirectUri, params);
		} else {
			DispatcherModule.sendBadRequest("", response);
		}
	}
	
	private Jwt<?,?> loginHint(String loginHint) {
		LTI13PlatformSigningPrivateKeyResolver signingResolver = new LTI13PlatformSigningPrivateKeyResolver();
		return Jwts.parserBuilder()
				.setSigningKeyResolver(signingResolver)
				.build()
				.parse(loginHint);
	}
	
	/**
	 * OpenOlat start the launch procedure of an external LTI 1.3 tool.
	 * 
	 * @param identity The identity of the user wo try to launch something
	 * @param deployment The tool deployment
	 * @param nonce A nonce
	 * @return The id_token, signed
	 */
	private String generateIdToken(Identity identity, Claims loginHint, LTI13ToolDeployment deployment, String nonce) {
		Date expirationDate = DateUtils.addMinutes(new Date(), 60);
		
		LTI13Tool tool = deployment.getTool();
		String deploymentId = deployment.getDeploymentId();
		String clientId = tool.getClientId();
		String targetUrl = deployment.getTargetUrl();
		if(!StringHelper.containsNonWhitespace(targetUrl)) {
			targetUrl = deployment.getTool().getToolUrl();
		}

		Map<String,String> resourceLink = appendResourceClaims(deployment);

		Map<String,Object> nrpsMap = new LinkedHashMap<>();
		nrpsMap.put("context_memberships_url", Settings.getServerContextPathURI() + LTI13Dispatcher.LTI_NRPS_PATH);
		nrpsMap.put("service_versions", List.of("2.0"));
		
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
			.claim("nonce", nonce)
			
			.claim(LTI13Constants.Claims.MESSAGE_TYPE.url(), "LtiResourceLinkRequest")
			.claim(LTI13Constants.Claims.RESOURCE_LINK.url(), resourceLink)
			.claim(LTI13Constants.Claims.NAMES_AND_ROLES_SERVICE.url(), nrpsMap);

		appendPresentation(deployment, builder);
		appendAttributes(identity, deployment, builder);
		appendCustomAttributes(identity, deployment, builder);
		appendRolesClaim(deployment, loginHint, builder);
		appendAGSClaims(deployment, builder);
		//appendBasicOutcomes(sub, deployment, builder);
		
		builder
			.claim(LTI13Constants.Claims.VERSION.url(), "1.3.0")
			.claim(LTI13Constants.Claims.DEPLOYMENT_ID.url(), deploymentId)
			.claim(LTI13Constants.Claims.TARGET_LINK_URI.url(), targetUrl);
		
		return builder
			.signWith(platformKey.getPrivateKey())
			.compact();
	}
	
	private void appendRolesClaim(LTI13ToolDeployment deployment, Claims loginHint, JwtBuilder builder) {
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
		if(roles.isEmpty()) {
			// at least one role is often mandatory
			roles.add(LTI13Constants.Roles.LEARNER.roleV2());
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
	
	private void appendCustomAttributes(Identity identity, LTI13ToolDeployment deployment, JwtBuilder builder) {
		String custom = deployment.getSendCustomAttributes();
		if(!StringHelper.containsNonWhitespace(custom)) return;
		
		Map<String,Object> map = new LinkedHashMap<>();
		String[] params = custom.split("[\n;]");
		for (int i = 0; i < params.length; i++) {
			String[] pair = getKeyPair(params[i]);
			if(pair.length == 2) {
				String prop = pair[0];
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
				}
				if (value != null) {
					map.put(prop, value);
				}
			}
		}

		if(!map.isEmpty()) {
			builder.claim(LTI13Constants.Claims.CUSTOM.url(), map);
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
	
	private void appendAGSClaims(LTI13ToolDeployment deployment, JwtBuilder builder) {
		if(!deployment.isAssessable()) return;
		
		Map<String,Object> map = new LinkedHashMap<>();
		if(deployment.getEntry() != null && StringHelper.containsNonWhitespace(deployment.getSubIdent())) {
			List<String> agsScopes = List.of(LTI13Constants.Scopes.AGS_LINE_ITEM.url(),
					LTI13Constants.Scopes.AGS_LINE_ITEM_READ_ONLY.url(),
					LTI13Constants.Scopes.AGS_RESULT_READ_ONLY.url(),
					LTI13Constants.Scopes.AGS_SCORE.url());
			map.put("scope", agsScopes);
			map.put("lineitems", Settings.getServerContextPathURI() + LTI13Dispatcher.LTI_AGS_PATH + "/lineitems/" + deployment.getKey());
			map.put("lineitem", Settings.getServerContextPathURI() + LTI13Dispatcher.LTI_AGS_PATH + "/lineitem/" + deployment.getKey());
		}
		builder.claim(LTI13Constants.Claims.ASSIGNMENT_AND_GRADING_SERVICE.url(), map);
	}
	
	private Map<String,String> appendResourceClaims(LTI13ToolDeployment deployment) {
		Map<String,String> resourceLink = new LinkedHashMap<>();

		if(deployment.getEntry() != null && StringHelper.containsNonWhitespace(deployment.getSubIdent())) {
			resourceLink.put("id", deployment.getEntry().getKey() + "_" + StringHelper.containsNonWhitespace(deployment.getSubIdent()));
			resourceLink.put("title", deployment.getEntry().getDisplayname());
			if(StringHelper.containsNonWhitespace(deployment.getEntry().getDescription())) {
				resourceLink.put("description", deployment.getEntry().getDescription());
			}
		}
		
		return resourceLink;
	}
	
	/*
	private void appendBasicOutcomes(String sub, LTI13ToolDeployment deployment, JwtBuilder builder) {
		if(deployment.getEntry() == null) return;
		
		Map<String,String> map = new LinkedHashMap<>();
		map.put("lis_result_sourcedid", deployment.getEntry().getKey() + "_" + deployment.getSubIdent() + "_" + sub);
		map.put("lis_outcome_service_url", Settings.getServerContextPathURI() + LTI13Dispatcher.LTI_BS_PATH + "/" + deployment.getKey() + "/results/" + cn.getIdent()));
		builder.claim(LTI13Constants.Claims.BASIC_OUTCOMES.url(), map);
	}*/
	
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
		sb.append("</form>\n");
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
		LTI13ExternalToolSigningKeyResolver signingResolver = new LTI13ExternalToolSigningKeyResolver();
		Jwt<?,?> jwt = Jwts.parserBuilder()
				.setSigningKeyResolver(signingResolver)
				.build()
				.parse(clientassertion);
		log.debug("Token: {}", jwt);
		
		LTI13Tool tool = signingResolver.getTool();
		handleTokenClientCredentials(jwt, tool, request, response);
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
			LTI13ExternalToolSigningKeyResolver signingResolver = new LTI13ExternalToolSigningKeyResolver();
			String clientassertion = request.getParameter(LTI13Constants.OAuth.CLIENT_ASSERTION);
			Jwt<?,?> jwt = Jwts.parserBuilder()
				.setSigningKeyResolver(signingResolver)
				.build()
				.parse(clientassertion);
			log.debug("Token: {}", jwt);
			handleTokenClientCredentials(jwt, signingResolver.getTool(), request, response);
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
	
	private Jwt<?,?> getAccessToken(HttpServletRequest request) {
		String authorization = request.getHeader("authorization");
		if(authorization.startsWith("Bearer ")) {
			authorization = authorization.substring("Bearer ".length());
			try {
				LTI13PlatformSigningPrivateKeyResolver resolver = new LTI13PlatformSigningPrivateKeyResolver();
				return Jwts.parserBuilder()
					.setSigningKeyResolver(resolver)
					.build()
					.parse(authorization);
			} catch (Exception e) {
				log.error("", e);
			}
		}
		return null;
	}
	
	////////////////////////
	// Names services
	////////////////////////
	
	public void handleNrps(HttpServletRequest request, HttpServletResponse response) {
		String accept = request.getHeader("accept");
		if(!"application/vnd.ims.lti-nrps.v2.membershipcontainer+json".equals(accept)) {
			DispatcherModule.sendBadRequest("", response);
			return;
		}
		
		Jwt<?,?> jwt = getAccessToken(request);
		if(jwt == null) {
			DispatcherModule.sendForbidden("", response);
		} else {
			Claims claims = (Claims)jwt.getBody();
			String toolIss = claims.getAudience();
			String clientId = claims.getSubject();
			
			LTI13Tool tool = lti13Service.getToolBy(toolIss, clientId);
			List<LTI13ToolDeployment> deployments = lti13Service.getToolDeployments(tool);
			if(deployments.size() == 1) {
				LTI13ToolDeployment deployment = deployments.get(0);
				List<Identity> identities = repositoryService.getMembers(deployment.getEntry(), RepositoryEntryRelationType.defaultGroup, GroupRoles.participant.name());
				
				MembershipContainer container = new MembershipContainer();
				container.setId(Settings.getServerContextPathURI() + "/auth/RepositoryEntry/" + deployment.getEntry().getKey());
				
				Context context = new Context();
				context.setId(deployment.getEntry().getKey().toString());
				context.setLabel(deployment.getEntry().getDisplayname());
				context.setTitle(deployment.getEntry().getDisplayname());
				container.setContext(context);
				
				List<Member> members = new ArrayList<>();
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
				
				container.setMembers(members);
				
				sendJSON(container, response);
			}
		}
	}
	
	//////////////////
	// Assignment and grading service
	//////////////////
	
	public void handleAgs(String[] path, HttpServletRequest request, HttpServletResponse response) {
		if(path.length < 3 || (!"ags".equals(path[0]) || !"lineitem".equals(path[1]) || !StringHelper.isLong(path[2]))) {
			DispatcherModule.sendBadRequest("", response);
			return;
		}
		
		Jwt<?,?> jwt = getAccessToken(request);
		if(jwt == null) {
			DispatcherModule.sendForbidden("", response);
			return;
		}
		
		Long deploymentKey = Long.valueOf(path[2]);
		LTI13ToolDeployment deployment = lti13Service.getToolDeploymentByKey(deploymentKey);
		
		if(path.length > 3) {
			String ext = path[3];
			if("scores".equals(ext)) {
				handleAgsScore(deployment, request, response);
			} else if("results".equals(ext)) {
				handleAgsResult(deployment, request, response);
			}
		}
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
		List<Result> results = new ArrayList<>();
		sendJSON(results, LTI13Constants.ContentTypes.RESULT_CONTAINER_CONTENT_TYPE, response);
	}

	private void handleAgsResult(String userId, Identity identity, LTI13ToolDeployment deployment, HttpServletResponse response) {
		RepositoryEntry entry = deployment.getEntry();
		String subIdent = deployment.getSubIdent();
		ICourse course = CourseFactory.loadCourse(entry);
		CourseNode courseNode = course.getRunStructure().getNode(subIdent);
		if(courseNode instanceof BasicLTICourseNode) {
			AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseNode);
			Float score = ltiManager.getScore(identity, course, subIdent);
			Float maxScore = null;
			if(assessmentConfig.getScoreMode() == Mode.setByNode) {
				maxScore = assessmentConfig.getMaxScore();
			}
			Result result = createResult(userId, score, maxScore, deployment);
			sendJSON(List.of(result), LTI13Constants.ContentTypes.RESULT_CONTAINER_CONTENT_TYPE, response);
		}
	}
	
	private Result createResult(String userId, Float score, Float maxScore, LTI13ToolDeployment deployment) {
		Result result = new Result();
		result.setId(Settings.getServerContextPathURI() + LTI13Dispatcher.LTI_AGS_PATH + "/lineitem/" + deployment.getKey() + "/results/" + deployment.getSubIdent());
		result.setScoreOf(Settings.getServerContextPathURI() + LTI13Dispatcher.LTI_AGS_PATH + "/lineitem/" + deployment.getKey());
		result.setUserId(userId);
		if(score != null) {
			result.setResultScore(score.doubleValue());
		} else {
			result.setResultScore(0.0d);
		}
		if(maxScore != null) {
			result.setResultMaximum(maxScore.doubleValue());
		}
		return result;
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
