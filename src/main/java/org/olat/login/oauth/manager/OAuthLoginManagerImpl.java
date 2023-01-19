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
package org.olat.login.oauth.manager;

import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.olat.admin.user.imp.TransientIdentity;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.manager.AuthenticationDAO;
import org.olat.core.gui.components.form.ValidationError;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.httpclient.HttpClientService;
import org.olat.login.auth.AuthenticationProviderSPI;
import org.olat.login.auth.OLATAuthManager;
import org.olat.login.oauth.OAuthLoginManager;
import org.olat.login.oauth.OAuthLoginModule;
import org.olat.login.oauth.OAuthSPI;
import org.olat.login.oauth.model.OAuthUser;
import org.olat.login.oauth.ui.OAuthRegistrationController;
import org.olat.login.validation.AllOkValidationResult;
import org.olat.login.validation.SyntaxValidator;
import org.olat.login.validation.ValidationResult;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 9 avr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class OAuthLoginManagerImpl implements OAuthLoginManager, AuthenticationProviderSPI {
	
	private static final Logger log = Tracing.createLoggerFor(OAuthLoginManagerImpl.class);
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private OAuthLoginModule oauthModule;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private OLATAuthManager olatAuthManager;
	@Autowired
	private AuthenticationDAO authenticationDao;
	@Autowired
	private HttpClientService httpClientService;
	

	@Override
	public List<String> getProviderNames() {
		List<OAuthSPI> spies = oauthModule.getEnableSPIs();
		return spies.stream()
				.map(OAuthSPI::getProviderName)
				.collect(Collectors.toList());
	}

	@Override
	public boolean canAddAuthenticationUsername(String provider) {
		return canChangeAuthenticationUsername(provider);
	}

	@Override
	public boolean canChangeAuthenticationUsername(String provider) {
		List<OAuthSPI> spies = oauthModule.getEnableSPIs();
		for(OAuthSPI spi:spies) {
			if(spi.getProviderName().equals(provider) && spi.isEnabled()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean changeAuthenticationUsername(Authentication authentication, String newUsername) {
		authentication.setAuthusername(newUsername);
		authentication = authenticationDao.updateAuthentication(authentication);
		return authentication != null;
	}

	@Override
	public ValidationResult validateAuthenticationUsername(String name, Identity identity) {
		return new AllOkValidationResult();
	}

	@Override
	public boolean isValid(OAuthUser oauthUser) {
		String username = null;
		if(StringHelper.containsNonWhitespace(oauthUser.getNickName())) {
			username = oauthUser.getNickName();
		} else if(StringHelper.containsNonWhitespace(oauthUser.getId())) {
			username = oauthUser.getId();
		}
		
		TransientIdentity newIdentity = new TransientIdentity();
		newIdentity.setName(username);

		SyntaxValidator usernameSyntaxValidator = olatAuthManager.createUsernameSytaxValidator();
		ValidationResult validationResult = usernameSyntaxValidator.validate(username, newIdentity);
		if (!validationResult.isValid()) {
			return false;
		}

		List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(OAuthRegistrationController.USERPROPERTIES_FORM_IDENTIFIER, false);
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;
			
			ValidationError error = new ValidationError();
			String value = oauthUser.getProperty(userPropertyHandler.getName());
			if (!userPropertyHandler.isValidValue(newIdentity.getUser(), value, error, Locale.ENGLISH)
					|| (!StringHelper.containsNonWhitespace(value) && userManager.isMandatoryUserProperty(OAuthRegistrationController.USERPROPERTIES_FORM_IDENTIFIER, userPropertyHandler))) {
				return  false;
			}
		}
		
		return true;
	}
	
	public Identity createIdentity(OAuthUser oauthUser, String provider) {
		String username = null;
		if(StringHelper.containsNonWhitespace(oauthUser.getNickName())) {
			username = oauthUser.getNickName();
		} else if(StringHelper.containsNonWhitespace(oauthUser.getId())) {
			username = oauthUser.getId();
		}
		
		ValidationError error = new ValidationError();
		User newUser = userManager.createUser(null, null, null);
		List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(OAuthRegistrationController.USERPROPERTIES_FORM_IDENTIFIER, false);
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;
			
			String value = oauthUser.getProperty(userPropertyHandler.getName());
			if (userPropertyHandler.isValidValue(newUser, value, error, Locale.ENGLISH)) {
				newUser.setProperty(userPropertyHandler.getName(), value);
			}
		}
		
		// Init preferences
		String lang = oauthUser.getLang();
		newUser.getPreferences().setLanguage(lang);
		newUser.getPreferences().setInformSessionTimeout(true);
		
		String id;
		if(StringHelper.containsNonWhitespace(oauthUser.getId())) {
			id = oauthUser.getId();
		} else if(StringHelper.containsNonWhitespace(oauthUser.getEmail())) {
			id = oauthUser.getEmail();
		} else {
			id = username;
		}
		return securityManager.createAndPersistIdentityAndUserWithOrganisation(null, username, null, newUser,
				provider, BaseSecurity.DEFAULT_ISSUER, id, null, null, null);
	}

	@Override
	public JSONObject loadDiscoveryUrl(String url) {
		try(CloseableHttpClient httpClient=httpClientService.createHttpClient()) {
			HttpGet get = new HttpGet(new URL(url).toURI());
			HttpResponse response = httpClient.execute(get);
			String content = EntityUtils.toString(response.getEntity());
			return new JSONObject(content);
		} catch(Exception e) {
			log.error("", e);
		}
		return null;
	}
}
