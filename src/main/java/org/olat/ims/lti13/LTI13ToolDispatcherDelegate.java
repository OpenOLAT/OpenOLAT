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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.NewControllerFactory;
import org.olat.basesecurity.AuthHelper;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.fullWebApp.BaseFullWebappController;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.UserRequestImpl;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.cache.CacheWrapper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.i18n.I18nManager;
import org.olat.dispatcher.LocaleNegotiator;
import org.olat.ims.lti13.LTI13Constants.Errors;
import org.olat.ims.lti13.LTI13SharedToolService.ServiceType;
import org.olat.ims.lti13.OIDCApi.OIDCService;
import org.olat.ims.lti13.manager.LTI13SharedToolSigningKeyResolver;
import org.olat.ims.lti13.ui.LTI13Creator;
import org.olat.login.DmzBFWCParts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;

/**
 * OpenOlat or more exactly a course or group acts as a tool
 * configured in an other LTI Platform / LMS.
 * 
 * Initial date: 17 févr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LTI13ToolDispatcherDelegate {
	
	private static final Logger log = Tracing.createLoggerFor(LTI13ToolDispatcherDelegate.class);

	private CacheWrapper<String,OAuth20Service> stateToRequests;
	
	@Autowired
	private LTI13Service lti13Service;
	
	@Autowired
	public LTI13ToolDispatcherDelegate(CoordinatorManager coordinatorManager) {
		stateToRequests = coordinatorManager.getCoordinator().getCacher().getCache("LTI", "states");
	}

	/**
	 * 1. Other LMS initiate the login 
	 * 
	 * @param request The HTTP request
	 * @param response The HTTP response
	 */
	public void handleInitiationLogin(HttpServletRequest request, HttpServletResponse response) {
		try {
			String iss = request.getParameter("iss");
			String clientId = request.getParameter("client_id");
			String loginHint = request.getParameter("login_hint");
			String ltiMessageHint = request.getParameter("lti_message_hint");
			String ltiDeploymentId = request.getParameter("lti_deployment_id");
			String targetLinkUri = request.getParameter("target_link_uri");
			
			LTI13Platform platform = lti13Service.getPlatform(iss, clientId);
			if(platform == null) {
				DispatcherModule.sendBadRequest(Errors.INVALID_REQUEST, response);
				return;
			}
			
			LTI13SharedToolDeployment sharedToolDeployment = lti13Service.getSharedToolDeployment(ltiDeploymentId, platform);
			if(sharedToolDeployment == null) {
				DispatcherModule.sendBadRequest(Errors.INVALID_REQUEST, response);
				return;
			}
			
			if(!checkTargetLinkUri(targetLinkUri, sharedToolDeployment)) {
				DispatcherModule.sendBadRequest(Errors.INVALID_TARGET_LINK_URI, response);
				return;
			}
			
			log.debug("Initiate login by platform (iss): {} with client_id: {} and deployment id: {} for target: {}",
					iss, clientId, ltiDeploymentId, targetLinkUri);

			String callbackUri = Settings.getServerContextPathURI() + LTI13Dispatcher.LTI_LOGIN_REDIRECT_PATH;
			OAuth20Service service = new ServiceBuilder(clientId)
			        .callback(callbackUri)
			        .defaultScope("openid")
			        .responseType(LTI13Constants.OAuth.ID_TOKEN)
			        .build(new OIDCApi(sharedToolDeployment, platform));

			String state = UUID.randomUUID().toString().replace("-", "");
			Map<String,String> additionalParams = new HashMap<>();
			additionalParams.put(LTI13Constants.OAuth.STATE, state);
			additionalParams.put("prompt", "none");
			additionalParams.put("response_mode", "form_post");
			additionalParams.put("registration_id", "ims-ri");
			if(StringHelper.containsNonWhitespace(loginHint)) {
				additionalParams.put("login_hint", loginHint);
			}
			if(StringHelper.containsNonWhitespace(ltiDeploymentId)) {
				additionalParams.put("lti_deployment_id", ltiDeploymentId);
			}
			if(StringHelper.containsNonWhitespace(ltiMessageHint)) {
				additionalParams.put("lti_message_hint", ltiMessageHint);
			}

			String redirectUrl = service.getAuthorizationUrl(additionalParams);
			log.debug(redirectUrl);
			stateToRequests.put(state, service);
			response.sendRedirect(redirectUrl);
		} catch (IOException e) {
			log.error("", e);
		}
	}

	/**
	 * 2. Other LMS login request to OpenOlat.
	 * 
	 * @param request The HTTP request
	 * @param response The HTTP response
	 */
	public void handleLogin(HttpServletRequest request, HttpServletResponse response) {	
		String state = request.getParameter(LTI13Constants.OAuth.STATE);
		String idToken = request.getParameter(LTI13Constants.OAuth.ID_TOKEN);
		String error = request.getParameter(LTI13Constants.OAuth.ERROR);
		if(Errors.INVALID_REQUEST.equals(error)) {
			DispatcherModule.sendBadRequest(error, response);
			return;
		}
		if(!StringHelper.containsNonWhitespace(state) || !stateToRequests.containsKey(state)) {
			DispatcherModule.sendBadRequest(Errors.INVALID_STATE, response);
			return;
		}
		
		try {
			OIDCService service = (OIDCService)stateToRequests.get(state);
			OIDCApi api = service.getApi();
			Jwt<?,?> jwt = Jwts.parserBuilder()
				.setSigningKeyResolver(new LTI13SharedToolSigningKeyResolver(api.getPlatform()))
				.setAllowedClockSkewSeconds(300)
				.build()
				.parse(idToken);

			Claims body = (Claims)jwt.getBody();
			String targetLinkUri = body.get(LTI13Constants.Claims.TARGET_LINK_URI.url(), String.class);
			if(!checkTargetLinkUri(targetLinkUri, api.getDeployment())) {
				DispatcherModule.sendBadRequest(Errors.INVALID_TARGET_LINK_URI, response);
				return;
			}

			String deploymentId = body.get(LTI13Constants.Claims.DEPLOYMENT_ID.url(), String.class);
			String contextId = null;
			Map<?,?> contextObj = body.get(LTI13Constants.Claims.CONTEXT.url(), Map.class);
			if(contextObj != null) {
				contextId = (String)contextObj.get("id");
			}
			
			String resourceLinkId = null;
			Map<?,?> resourceLinkObj = body.get(LTI13Constants.Claims.RESOURCE_LINK.url(), Map.class);
			if(resourceLinkObj != null) {
				resourceLinkId = (String)resourceLinkObj.get("id");
			}
			
			updateServices(contextId, body, api.getDeployment());
			
			log.debug("Login deployment id: {} context id: {} resource link id: {}", deploymentId, contextId, resourceLinkId);
			
			Identity identity = lti13Service.matchIdentity(body, api.getPlatform());
			if(identity == null) {
				DispatcherModule.sendBadRequest(request.getPathInfo(), response);
			} else {
				UserRequest ureq = createUserRequest(request, response);
				if(ureq != null) {
					GroupRoles role = getRolesFromClaims(body);
					lti13Service.checkMembership(identity, role, service.getApi().getDeployment());
					AuthHelper.doLogin(identity, "LTI", ureq);
					response.sendRedirect(targetLinkUri + "?new-window=minimal");
				} else {
					DispatcherModule.sendBadRequest(request.getPathInfo(), response);
				}
			}
		} catch (Exception e) {
			log.error("", e);
			DispatcherModule.sendServerError(response);
		}
	}
	
	private GroupRoles getRolesFromClaims(Claims claims) {
		Object roles = claims.get(LTI13Constants.Claims.ROLES.url());
		if(roles instanceof List) {
			List<?> roleList = (List<?>)roles;
			for(Object role:roleList) {
				if(role instanceof String  && isRoleToCoach((String)role)) {
					return GroupRoles.coach;
				}
			}
		}
		return GroupRoles.participant;
	}
	
	private boolean isRoleToCoach(String role) {
		return StringHelper.containsNonWhitespace(role)
				&& (role.startsWith(LTI13Constants.Roles.INSTRUCTOR.roleV2())
						|| role.startsWith(LTI13Constants.Roles.MENTOR.roleV2())
						|| role.startsWith(LTI13Constants.Roles.INSTITUTION_INSTRUCTOR.roleV2()));
	}
	
	private boolean checkTargetLinkUri(String targetLinkUri, LTI13SharedToolDeployment deployment) {
		OLATResourceable ores;
		if(deployment.getEntry() != null) {
			ores = deployment.getEntry();
		} else if(deployment.getBusinessGroup() != null) {
			ores = deployment.getBusinessGroup();
		} else {
			return false;
		}
		
		List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromString(ores);
		String authUri = BusinessControlFactory.getInstance().getAsAuthURIString(entries, true);
		String restUri = BusinessControlFactory.getInstance().getAsRestPart(entries, true);
		boolean match = targetLinkUri.startsWith(authUri) || targetLinkUri.startsWith(restUri);
		if(!match) {
			log.warn("Target link uri mismatch, target link uri: {}, allowed deployment: {} ({})", targetLinkUri, authUri, restUri);
		}
		return match;
	}
	
	private void updateServices(String contextId, Claims body, LTI13SharedToolDeployment deployment) {
		// Names and roles service
		updateService(contextId, body, LTI13Constants.Claims.NAMES_AND_ROLES_SERVICE.url(),
				LTI13Constants.NRPS.MEMBERSHIPS_URL, ServiceType.nrps, deployment);
		
		// Assignment and grading service
		updateService(contextId, body, LTI13Constants.Claims.ASSIGNMENT_AND_GRADING_SERVICE.url(),
				LTI13Constants.AGS.LINEITEMS_URL, ServiceType.lineitems, deployment);
		updateService(contextId, body, LTI13Constants.Claims.ASSIGNMENT_AND_GRADING_SERVICE.url(),
				LTI13Constants.AGS.LINEITEM_URL, ServiceType.lineitem, deployment);
	}
	
	private void updateService(String contextId, Claims body, String claimsName, String claimsAttribute,
			ServiceType type, LTI13SharedToolDeployment deployment) {
		Map<?,?> mapObj = body.get(claimsName, Map.class);
		if(mapObj != null) {
			String url = (String)mapObj.get(claimsAttribute);
			if(StringHelper.containsNonWhitespace(url)) {
				lti13Service.updateSharedToolServiceEndpoint(contextId, type, url, deployment);
			}
		}
	}
	
	private UserRequest createUserRequest(HttpServletRequest request, HttpServletResponse response) {
		try{
			//upon creation URL is checked for 
			return new UserRequestImpl("lti", request, response);
		} catch(NumberFormatException nfe) {
			if(log.isDebugEnabled()){
				log.debug("Bad Request {}", request.getPathInfo());
			}
			return null;
		}
	}
	
	public void dispatchDisclaimer(UserRequest ureq, String uriPrefix) {
		UserSession usess = ureq.getUserSession();
		
		usess.setLocale(LocaleNegotiator.getPreferedLocale(ureq));
		I18nManager.updateLocaleInfoToThread(usess);
		
		DmzBFWCParts bfwcParts = new DmzBFWCParts();
		bfwcParts.showTopNav(false);
		ControllerCreator controllerCreator = new LTI13Creator();
		bfwcParts.setContentControllerCreator(controllerCreator);
		
		Windows windows = Windows.getWindows(usess);
		boolean windowHere = windows.isExisting(uriPrefix, ureq.getWindowID());
		if (!windowHere) {
			synchronized (windows) {
				ChiefController cc = new BaseFullWebappController(ureq, bfwcParts);
				Window window = cc.getWindow();
				window.setUriPrefix(uriPrefix);
				ureq.overrideWindowComponentID(window.getDispatchID());
				windows.registerWindow(cc);
			}
		}

		windows.getWindowManager().setAjaxWanted(ureq);
		ChiefController chiefController = windows.getChiefController(ureq);
		try {
			WindowControl wControl = chiefController.getWindowControl();
			NewControllerFactory.getInstance().launch(ureq, wControl);	
			Window w = chiefController.getWindow().getWindowBackOffice().getWindow();
			w.dispatchRequest(ureq, true); // renderOnly
			chiefController.resetReload();
		} catch (Exception e) {
			log.error("", e);
		}
	}
}
