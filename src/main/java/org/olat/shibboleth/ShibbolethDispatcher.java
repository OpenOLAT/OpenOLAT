/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.shibboleth;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.AuthHelper;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.UserRequestImpl;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.exception.MsgFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.RedirectMediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.OLATSecurityException;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ThreadLocalUserActivityLoggerInstaller;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.i18n.I18nModule;
import org.olat.registration.RegistrationManager;
import org.olat.shibboleth.manager.ShibbolethAttributes;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date:  17.07.2004
 *
 * @author Mike Stock
 */
public class ShibbolethDispatcher implements Dispatcher{

	private static final Logger log = Tracing.createLoggerFor(ShibbolethDispatcher.class);

	/** Provider identifier */
	public static final String PROVIDER_SHIB = "Shib";
	/** Identifies requests for the ShibbolethDispatcher */
	public static final String PATH_SHIBBOLETH = "/shib/";

	private Translator translator;
	private BaseSecurity securityManager;
	private ShibbolethModule shibbolethModule;

	@Autowired
	private ShibbolethManager shibbolethManager;

	/**
	 * [used by Spring]
	 * @param shibbolethModule
	 */
	public void setShibbolethModule(ShibbolethModule shibbolethModule) {
		this.shibbolethModule = shibbolethModule;
	}

	/**
	 * [used by Spring]
	 * @param securityManager
	 */
	public void setSecurityManager(BaseSecurity securityManager) {
		this.securityManager = securityManager;
	}

	/**
	 * Main method called by OpenOLATServlet.
	 * This processess all shibboleth requests.
	 *
	 * @param req
	 * @param resp
	 * @param uriPrefix
	 */
	@Override
	public void execute(HttpServletRequest req,	HttpServletResponse resp) {
		if(translator==null) {
			translator = Util.createPackageTranslator(ShibbolethDispatcher.class, I18nModule.getDefaultLocale());
		}

		if (!shibbolethModule.isEnableShibbolethLogins()){
			throw new OLATSecurityException("Got shibboleth request but shibboleth is not enabled");
		}
		String uriPrefix = DispatcherModule.getLegacyUriPrefix(req);

		Map<String, String> attributesMap = getShibbolethAttributesFromRequest(req);
		ShibbolethAttributes shibbolethAttriutes = CoreSpringFactory.getImpl(ShibbolethAttributes.class);
		shibbolethAttriutes.init(attributesMap);
		String uid = shibbolethAttriutes.getUID();
		if(uid == null) {
			handleException(new ShibbolethException(ShibbolethException.UNIQUE_ID_NOT_FOUND,"Unable to get unique identifier for subject. Make sure you are listed in the metadata.xml file and your resources your are trying to access are available and your are allowed to see them. (Resourceregistry). "),
					req, resp, translator);
			return;
		}

		if(!authorization(req, resp, shibbolethAttriutes)) {
			return;
		}

		UserRequest ureq = null;
		try{
			//upon creation URL is checked for
			ureq = new UserRequestImpl(uriPrefix, req, resp);
		} catch(NumberFormatException nfe) {
			//MODE could not be decoded
			//typically if robots with wrong urls hit the system
			//or user have bookmarks
			//or authors copy-pasted links to the content.
			//showing redscreens for non valid URL is wrong instead
			//a 404 message must be shown -> e.g. robots correct their links.
			if(log.isDebugEnabled()){
				log.debug("Bad Request {}", req.getPathInfo());
			}
			DispatcherModule.sendBadRequest(req.getPathInfo(), resp);
			return;
		}

		Authentication auth = securityManager.findAuthenticationByAuthusername(uid, PROVIDER_SHIB, BaseSecurity.DEFAULT_ISSUER);
		if (auth == null) { // no matching authentication...
			ShibbolethRegistrationController.putShibAttributes(req, shibbolethAttriutes);
			ShibbolethRegistrationController.putShibUniqueID(req, uid);
			register(resp);
			return;
		}
		if(ureq.getUserSession() != null) {
			//re-init the activity logger
			ThreadLocalUserActivityLoggerInstaller.initUserActivityLogger(req);
		}
		int loginStatus = AuthHelper.doLogin(auth.getIdentity(), ShibbolethDispatcher.PROVIDER_SHIB, ureq);
		if (loginStatus != AuthHelper.LOGIN_OK) {
			if (loginStatus == AuthHelper.LOGIN_NOTAVAILABLE) {
				DispatcherModule.redirectToServiceNotAvailable(resp);
			} else {
				DispatcherModule.redirectToDefaultDispatcher(resp); // error, redirect to login screen
			}
		} else {
			// Successful login
			Identity authenticationedIdentity = ureq.getIdentity();
			securityManager.setIdentityLastLogin(authenticationedIdentity);
			if (Identity.STATUS_INACTIVE.equals(authenticationedIdentity.getStatus())) {
				authenticationedIdentity = securityManager.reactivatedIdentity(authenticationedIdentity);
			}
			shibbolethManager.syncUser(authenticationedIdentity, shibbolethAttriutes);
			ureq.getUserSession().getIdentityEnvironment().addAttributes(
					shibbolethModule.getAttributeTranslator().translateAttributesMap(shibbolethAttriutes.toMap()));
			
			if (CoreSpringFactory.getImpl(RegistrationManager.class).needsToConfirmDisclaimer(authenticationedIdentity)) {
				disclaimer(resp);
			} else {
				MediaResource mr = ureq.getDispatchResult().getResultingMediaResource();
				if (mr instanceof RedirectMediaResource) {
					RedirectMediaResource rmr = (RedirectMediaResource)mr;
					rmr.prepare(resp);
				} else {
					DispatcherModule.redirectToDefaultDispatcher(resp); // error, redirect to login screen
				}
			}
		}
	}

	private Map<String, String> getShibbolethAttributesFromRequest(HttpServletRequest req) {
		Map<String, String> attributesMap = new HashMap<>();
		Enumeration<String> headerEnum = req.getHeaderNames();
		Collection<String> attributeNames = shibbolethModule.getShibbolethAttributeNames();
		while(headerEnum.hasMoreElements()) {
			String attributeName = headerEnum.nextElement();
			String attributeValue = req.getHeader(attributeName);

			try {
				attributeValue = new String(attributeValue.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
				if (attributeNames.contains(attributeName)) {
					attributesMap.put(attributeName, attributeValue);
				}
			} catch (Exception e) {
				//bad luck
				throw new AssertException("ISO-8859-1, or UTF-8 Encoding not supported",e);
			}
		}

		log.debug("Shib attribute Map:  \n\n{}\n\n", attributesMap);
		return attributesMap;
	}
	
	private final void disclaimer(HttpServletResponse response) {
		try {
			response.sendRedirect(WebappHelper.getServletContextPath() + DispatcherModule.getPathDefault() + ShibbolethModule.PATH_DISCLAIMER_SHIBBOLETH + "/");
		} catch (IOException e) {
			log.error("Redirect failed: url={}", WebappHelper.getServletContextPath() + DispatcherModule.getPathDefault(), e);
		}
	}

	private final void register(HttpServletResponse response) {
		try {
			response.sendRedirect(WebappHelper.getServletContextPath() + DispatcherModule.getPathDefault() + ShibbolethModule.PATH_REGISTER_SHIBBOLETH + "/");
		} catch (IOException e) {
			log.error("Redirect failed: url={}", WebappHelper.getServletContextPath() + DispatcherModule.getPathDefault(), e);
		}
	}

	private boolean authorization(HttpServletRequest req, HttpServletResponse resp, ShibbolethAttributes shibbolethAttibutes) {
		boolean authorized = false;
		if(shibbolethModule.isAccessControlByAttributes()) {
			if(StringHelper.containsNonWhitespace(shibbolethModule.getAttribute1()) && StringHelper.containsNonWhitespace(shibbolethModule.getAttribute1Values())) {
				authorized |= authorization(shibbolethModule.getAttribute1(), shibbolethModule.getAttribute1Values(), shibbolethAttibutes);
			}
			if(StringHelper.containsNonWhitespace(shibbolethModule.getAttribute2()) && StringHelper.containsNonWhitespace(shibbolethModule.getAttribute2Values())) {
				authorized |= authorization(shibbolethModule.getAttribute2(), shibbolethModule.getAttribute2Values(), shibbolethAttibutes);
			}
		} else {
			authorized = true;
		}

		if(!authorized) {
			UserRequest ureq = new UserRequestImpl(ShibbolethDispatcher.PATH_SHIBBOLETH, req, resp);
			String userMsg = translator.translate("error.shibboleth.not.authorized");
			ChiefController msgcc = MessageWindowController.createMessageChiefController(ureq, null, userMsg, null);
			msgcc.getWindow().dispatchRequest(ureq, true);
		}
		return authorized;
	}

	private boolean authorization(String attributeName, String allowedValues, ShibbolethAttributes shibbolethAttributes) {
		String val = shibbolethAttributes.getValueForAttributeName(attributeName);
		if(StringHelper.containsNonWhitespace(val)) {
			val = val.trim();
			for(StringTokenizer tokenizer = new StringTokenizer(allowedValues, "\n\r,;", false); tokenizer.hasMoreTokens(); ) {
				String allowedValue = tokenizer.nextToken().trim();
				if(val.equalsIgnoreCase(allowedValue)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * It first tries to catch the frequent SAMLExceptions and to ask the user to login again.
	 * It basically lets the user to login again without getting a RedScreen if one of the most
	 * frequent shibboleth error occurs. Else a RedScreen is the last option.
	 * @param e
	 * @param req
	 * @param resp
	 */
	private void handleException(Throwable e, HttpServletRequest req, HttpServletResponse resp, Translator transl) {
		UserRequest ureq = new UserRequestImpl(ShibbolethDispatcher.PATH_SHIBBOLETH, req, resp);
		if(e instanceof ShibbolethException) {
			String userMsg = "";
			int errorCode = ((ShibbolethException)e).getErrorCode();
			switch (errorCode) {
				case ShibbolethException.GENERAL_SAML_ERROR: userMsg = transl.translate("error.shibboleth.generic"); break;
				case ShibbolethException.UNIQUE_ID_NOT_FOUND: userMsg = transl.translate("error.unqueid.notfound"); break;
				default: userMsg = transl.translate("error.shibboleth.generic"); break;
			}
			showMessage(ureq,"org.opensaml.SAMLException: " + e.getMessage(), e, userMsg, ((ShibbolethException)e).getContactPersonEmail());
		} else {
		  try {
			  ChiefController msgcc = MsgFactory.createMessageChiefController(ureq,
					new OLATRuntimeException("Error processing Shibboleth request: " + e.getMessage(), e), false);
			  msgcc.getWindow().dispatchRequest(ureq, true);
		  } catch (Throwable t) {
			  log.error("We're fucked up....",t);
		  }
	  }
	}

	/**
	 *
	 * @param ureq
	 * @param exceptionLogMessage will be recorded into the log file
	 * @param cause
	 * @param userMessage gets shown to the user
	 * @param supportEmail if any available, else null
	 */
	private void showMessage(UserRequest ureq, String exceptionLogMessage, Throwable cause, String userMessage, String supportEmail) {
		ChiefController msgcc = MessageWindowController.createMessageChiefController(ureq,	new OLATRuntimeException(exceptionLogMessage, cause), userMessage, supportEmail);
		msgcc.getWindow().dispatchRequest(ureq, true);
	}

}
