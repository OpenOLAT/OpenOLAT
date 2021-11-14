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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Locale;

import javax.servlet.http.Cookie;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.OLATSecurityException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.login.LoginModule;
import org.olat.login.auth.AuthenticationController;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Initial Date: 04.08.2004
 * 
 * @author Mike Stock
 * <P>
 * Description:<br>
 * Replaces the old ShibbolethAuthenticationController which used to have an own WAYF.
 * <p>
 * This ShibbolethAuthenticationController uses the EmbeddedWAYF provided by SWITCH
 * (see the shibbolethlogin.html)
 *
 */

public class ShibbolethAuthenticationController extends AuthenticationController {
	
	private static final Logger log = Tracing.createLoggerFor(ShibbolethAuthenticationController.class);
	
	protected static final String IDP_HOMESITE_COOKIE = "idpsite-presel";
	protected static final String SHIB_MOBILE = "shibbolet-mobile";

	private Translator fallbackTranslator;	
	
	@Autowired
	private ShibbolethModule shibbolethModule;
		
	public ShibbolethAuthenticationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		// extends authControll which is a BasicController, so we have to set the
		// Base new to resolve our velocity pages
		setBasePackage(this.getClass());
		// Manually set translator that uses a fallback translator to the login module
		// Can't use constructor with fallback translator because it gets overriden by setBasePackage call above
		setTranslator(Util.createPackageTranslator(this.getClass(), ureq.getLocale(), Util.createPackageTranslator(LoginModule.class, ureq.getLocale())));

		if (!shibbolethModule.isEnableShibbolethLogins()) throw new OLATSecurityException(
				"Tried to access shibboleth wayf but shibboleth is not enabled.");
		
		VelocityContainer loginComp = createVelocityContainer(shibbolethModule.getLoginTemplate());
		
		SwitchShibbolethAuthenticationConfigurator config = (SwitchShibbolethAuthenticationConfigurator)CoreSpringFactory.getBean(SwitchShibbolethAuthenticationConfigurator.class);
		loginComp.contextPut("wayfSPEntityID", config.getWayfSPEntityID());
		loginComp.contextPut("wayfSPHandlerURL", config.getWayfSPHandlerURL());
		loginComp.contextPut("wayfSPSamlDSURL", config.getWayfSPSamlDSURL());
		boolean mobile = isMobile(ureq);
		if(mobile) {
			loginComp.contextPut("wayfReturnUrl", config.getWayfReturnMobileUrl());
		} else {
			loginComp.contextPut("wayfReturnUrl", config.getWayfReturnUrl());
		}
		loginComp.contextPut("additionalIDPs", config.getAdditionalIdentityProviders());
		

		// displays warning after logout
		// logout=true is set by the AuthHelper.doLogout(..) as URL param
		// assuming the Shibboleth Authentication Controller is the first on the DMZ
		// this check shows an info message, that for a complete logout the browser must be closed
		// important for users on public work stations
		String param = ureq.getParameter("logout");
		if (param!=null && param.equals("true")){
			showWarning("info.browser.close");
		}
		
		putInitialPanel(loginComp);
	}
	
	private boolean isMobile(UserRequest ureq) {
		boolean mobile = false;
		String mparam = ureq.getHttpReq().getParameter("mshib");
		if("true".equals(mparam)) {
			ureq.getUserSession().putEntry(SHIB_MOBILE, Boolean.TRUE);
			mobile = true;
		} else {
			Object mobileFlag = ureq.getUserSession().getEntry(SHIB_MOBILE);
			if(mobileFlag != null && Boolean.TRUE.equals(mobileFlag)) {
				mobile = true;
			}
		}
		return mobile;
	}

	/**
	 * @see org.olat.login.auth.AuthenticationController#changeLocale(java.util.Locale)
	 */
	public void changeLocale(Locale newLocale) {
		getTranslator().setLocale(newLocale);
		fallbackTranslator.setLocale(newLocale);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		// nothing to do
	}

	/**
	 * gets the cookie for setting the dropdown in the shib login to the entry
	 * selected the last time
	 * 
	 * @param ureq
	 * @return the Cookie object
	 */
	public static String getHomeSiteCookieValue(UserRequest ureq) {
		// get Cookie for preselection of HomeSite
		Cookie[] cookies = ureq.getHttpReq().getCookies();
		Cookie cookie = null;
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				if (log.isDebugEnabled()) {
					log.debug("found cookie with name: " + cookies[i].getName() + " and value: " + cookies[i].getValue());
				}
				if (cookies[i].getName().equals(IDP_HOMESITE_COOKIE)) {
					cookie = cookies[i];
					break;
				}
			}
			if (cookie != null) {
				try {
					return URLDecoder.decode(cookie.getValue(), "utf-8");
				} catch (UnsupportedEncodingException e) {/* utf-8 is present */}
			}
		}
		// else cookie was null
		return null;
	}

	/**
	 * Sets a cookie with the home site last selected in the shib login form.
	 * 
	 * @param homeSite
	 * @param ureq
	 */
	public static void setHomeSiteCookie(String homeSite, UserRequest ureq) {
		Cookie cookie = null;
		try {
			cookie = new Cookie(IDP_HOMESITE_COOKIE, URLEncoder.encode(homeSite, "utf-8"));
			cookie.setHttpOnly(true);
		} catch (UnsupportedEncodingException e) {/* utf-8 is always present */}
		cookie.setMaxAge(100 * 24 * 60 * 60); // 100 days lifetime
		cookie.setPath(WebappHelper.getServletContextPath());
		cookie.setComment("cookie for preselection of AAI homesite");
		ureq.getHttpResp().addCookie(cookie);
	}

}
