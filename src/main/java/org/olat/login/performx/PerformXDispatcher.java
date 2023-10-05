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
package org.olat.login.performx;

import java.io.IOException;
import java.net.URI;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MediaType;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.olat.basesecurity.AuthHelper;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.UserRequestImpl;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.RedirectMediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ThreadLocalUserActivityLoggerInstaller;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.session.UserSessionManager;
import org.olat.shibboleth.MessageWindowController;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 28.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class PerformXDispatcher implements Dispatcher {
	
	private static final Logger log = Tracing.createLoggerFor(PerformXDispatcher.class);
	
	private Translator translator;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private PerformXModule performxModule;
	@Autowired
	private UserSessionManager userSessionManager;

	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
		if(translator == null) {
			translator = Util.createPackageTranslator(PerformXDispatcher.class, I18nModule.getDefaultLocale());
		}
		
		String uriPrefix = DispatcherModule.getLegacyUriPrefix(request);

		UserSession usess = userSessionManager.getUserSession(request);
		if(usess != null) {
			ThreadLocalUserActivityLoggerInstaller.initUserActivityLogger(request);
		}
		UserRequest ureq = null;
		try {
			ureq = new UserRequestImpl(uriPrefix, request, response);
		} catch(NumberFormatException nfe) {
			log.debug("Bad Request {}", request.getPathInfo());
			DispatcherModule.sendBadRequest(request.getPathInfo(), response);
			return;
		}

		String userIdentifier = getParameterIgnoreCase(ureq, "Username");
		String token = getParameterIgnoreCase(ureq, "Token");
		String clientId = getParameterIgnoreCase(ureq, "ClientID");
		if (performxModule.performxClientIdCheckEnabled()) {
			// Enforce the existence of the clientId parameter and make sure it matches the userIdentifier that the token is bound to
			if (!StringHelper.containsNonWhitespace(clientId) || !StringHelper.containsNonWhitespace(userIdentifier) || !clientId.equals(userIdentifier)) {
				notAuthorizedMessage(ureq);		
				return;
			}
		} 
		if(checkToken(token, clientId)) {
			Identity authenticatedIdentity = findAuthenticatedIdentity(userIdentifier);
			int loginStatus = AuthHelper.doLogin(authenticatedIdentity, PerformXModule.PERFORMX_AUTH, ureq);			
			if (loginStatus == AuthHelper.LOGIN_OK) {
				//save info
				ureq.getUserSession().putEntryInNonClearedStore("SSO-Username", userIdentifier);
				ureq.getUserSession().putEntryInNonClearedStore("SSO-Token", token);
				ureq.getUserSession().putEntryInNonClearedStore("SSO-ClientID", clientId);
				//must be done
				securityManager.setIdentityLastLogin(authenticatedIdentity);
				//redirect
				MediaResource mr = ureq.getDispatchResult().getResultingMediaResource();
				if (mr instanceof RedirectMediaResource) {
					RedirectMediaResource rmr = (RedirectMediaResource)mr;
					rmr.prepare(response);
				} else {
					DispatcherModule.redirectToDefaultDispatcher(response); // error, redirect to login screen
				}
			} else {
				notAuthorizedMessage(ureq);
			}
		} else {
			notAuthorizedMessage(ureq);
		}
	}
	
	private String getParameterIgnoreCase(UserRequest ureq, String parameter) {
		String value = ureq.getParameter(parameter);
		if(value == null) {
			value = ureq.getParameter(parameter.toLowerCase());
		}
		return value;
	}
	
	private Identity findAuthenticatedIdentity(String login) {
		Identity identity = null;
		if (MailHelper.isValidEmailAddress(login)){
			identity = userManager.findUniqueIdentityByEmail(login);
			if(identity == null) {
				Authentication authentication = securityManager.findAuthenticationByAuthusername(login, PerformXModule.PERFORMX_AUTH,BaseSecurity.DEFAULT_ISSUER);
				if(authentication != null) {
					identity = authentication.getIdentity();
				}
			}
		} else {
			Authentication authentication = securityManager.findAuthenticationByAuthusername(login, PerformXModule.PERFORMX_AUTH, BaseSecurity.DEFAULT_ISSUER);
			if(authentication != null) {
				identity = authentication.getIdentity();
			}
		}
		return identity;
	}
	
	private void notAuthorizedMessage(UserRequest ureq) {
		String userMsg = translator.translate("error.performx.not.authorized"); 
		ChiefController msgcc = MessageWindowController.createMessageChiefController(ureq, null, userMsg, null);
		msgcc.getWindow().dispatchRequest(ureq, true);
	}

	private boolean checkToken(String token, String clientId) {
		try (CloseableHttpClient httpclient = HttpClientBuilder.create().build()) {
			StringBuilder query = new StringBuilder();
			query.append("token=").append(token);
			if(StringHelper.containsNonWhitespace(clientId)) {
				query.append("&clientid=").append(clientId);
			}
			
			String path = "/rest2/SecUserMethods/method/ValidateToken";
			URI performxUri = URI.create(performxModule.getPerformxServerUrl());
			URI uri = new URI(performxUri.getScheme(), null, performxUri.getHost(), performxUri.getPort(),
					path, query.toString(), null);
			
			HttpGet validateTokenRequest = new HttpGet(uri);
			validateTokenRequest.addHeader("Accept", MediaType.APPLICATION_JSON);
			validateTokenRequest.addHeader("Accept-Language", "de");
			String authorization = performxModule.getPerformxServerUsername() + ":" + performxModule.getPerformxServerPassword();
			String authorization64 = StringHelper.encodeBase64(authorization.getBytes());
			validateTokenRequest.addHeader("Authorization", "Basic " + authorization64);

			HttpResponse response = httpclient.execute(validateTokenRequest);
			HttpEntity entity = response.getEntity();
			int statusCode = response.getStatusLine().getStatusCode();
			String returnContent = EntityUtils.toString(entity);
			if(statusCode == 200) {
				return getValidateTokenData(returnContent);
			}
			return false;
		} catch (Exception e) {
			log.error("", e);
			return false;
		}
	}
	
	private boolean getValidateTokenData(String content) {
		try {
			JSONObject obj = new JSONObject(content);
			JSONArray resourceArr = obj.getJSONArray("resource");
			JSONObject resource = resourceArr.getJSONObject(0);
			JSONArray data = resource.getJSONArray("data");
			JSONArray dataL1 = data.getJSONArray(0);
			return dataL1.getBoolean(0);
		} catch (JSONException e) {
			log.error("", e);
			return false;
		}
	}
}
