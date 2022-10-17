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
package org.olat.modules.library.ui;

import java.io.IOException;
import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.olat.basesecurity.AuthHelper;
import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.UserRequestImpl;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.navigation.SiteConfiguration;
import org.olat.core.gui.control.navigation.SiteDefinitions;
import org.olat.core.gui.control.navigation.SiteSecurityCallback;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.UserSession;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.session.UserSessionManager;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.login.LoginModule;
import org.olat.modules.library.LibraryManager;
import org.olat.modules.library.site.LibrarySiteDef;

/**
 * 
 * <h3>Description:</h3>
 * Implement a dispatcher which retrieve the document from the library
 * by their UUID. The security callback is the same as the site definition
 * of the library. The bean id of the library can be configured.
 * <p>
 * <p>
 * Initial Date:  2 déc. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class LibraryDispatcher  implements Dispatcher {
	
	private static final String AUTHDISPATCHER_BUSINESSPATH = "AuthDispatcher:businessPath";
	private static final String GUEST = "guest";
	private static final String TRUE = "true";
	
	private static final Logger log = Tracing.createLoggerFor(LibraryDispatcher.class);
	
	private static final String DEFAULT_BEAN_ID = "frentixsites_library";
	
	private String libraryBeanId;

	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) {
		String uriPrefix = DispatcherModule.getLegacyUriPrefix(request);
		UserSession usess = CoreSpringFactory.getImpl(UserSessionManager.class).getUserSession(request);
		UserRequest ureq = null;
		try {
			//upon creation URL is checked for 
			ureq = new UserRequestImpl(uriPrefix, request, response);
		} catch(NumberFormatException nfe) {
			//MODE could not be decoded
			//typically if robots with wrong urls hit the system
			//or user have bookmarks
			//or authors copy-pasted links to the content.
			//showing redscreens for non valid URL is wrong instead
			//a 404 message must be shown -> e.g. robots correct their links.
			log.debug("Bad Request {}", request.getPathInfo());
			DispatcherModule.sendBadRequest(request.getPathInfo(), response);
			return;
		}

		boolean auth = usess.isAuthenticated();
		if (auth) {
			redirectToDocument(ureq, request, response, uriPrefix);
		} else {
			//prepare for redirect
			String businessPath = getBusinessPath(request, uriPrefix);
			usess.putEntryInNonClearedStore(AUTHDISPATCHER_BUSINESSPATH, businessPath);

			String guestAccess = ureq.getParameter(GUEST);
			if (guestAccess == null || !CoreSpringFactory.getImpl(LoginModule.class).isGuestLoginLinksEnabled()) {
				DispatcherModule.redirectToDefaultDispatcher(response);
			} else if (guestAccess.equals(TRUE)) {
				// try to log in as anonymous
				// use the language from the lang paramter if available, otherwhise use the system default locale
				String guestLang = ureq.getParameter("lang");
				Locale guestLoc;
				if (guestLang == null) {
					guestLoc = I18nModule.getDefaultLocale();
				} else {
					guestLoc = I18nManager.getInstance().getLocaleOrDefault(guestLang);
				}
				int loginStatus = AuthHelper.doAnonymousLogin(ureq, guestLoc);
				if ( loginStatus == AuthHelper.LOGIN_OK) {
					//logged in as anonymous user, continue
					String url = getRedirectToURL(usess, ureq);
					DispatcherModule.redirectTo(response, url);
				} else if (loginStatus == AuthHelper.LOGIN_NOTAVAILABLE) {
					DispatcherModule.redirectToServiceNotAvailable(response);
				} else {
					//error, redirect to login screen
					DispatcherModule.redirectToDefaultDispatcher(response); 
				}
			}
		}
	}
	
	protected String getBusinessPath(HttpServletRequest request, String uriPrefix) {
		final String origUri = request.getRequestURI();
		if(origUri.startsWith(uriPrefix)) {
			LibraryManager libraryManager = CoreSpringFactory.getImpl(LibraryManager.class);
			String uuid = getDocumentUUID(origUri, uriPrefix);
			Long resId = libraryManager.getCatalogRepoEntry().getOlatResource().getResourceableId();
			return "[LibrarySite:" + resId + "][uuid=" + uuid + ":0]";
		}
		return null;
	}
	
	protected void redirectToDocument(UserRequest ureq, HttpServletRequest request, HttpServletResponse response, String uriPrefix) {
		final String origUri = request.getRequestURI();
		if(origUri.startsWith(uriPrefix)) {
			LibrarySiteDef siteDef = CoreSpringFactory.getImpl(LibrarySiteDef.class);
			SiteDefinitions sitesModule = CoreSpringFactory.getImpl(SiteDefinitions.class);
			SiteConfiguration libConfig = sitesModule.getConfigurationSite(siteDef);
			SiteSecurityCallback secCallback = (SiteSecurityCallback)CoreSpringFactory.getBean(libConfig.getSecurityCallbackBeanId());
			
			if(secCallback.isAllowedToLaunchSite(ureq)) {
				String uuid = getDocumentUUID(origUri, uriPrefix);
				LibraryManager libraryManager = CoreSpringFactory.getImpl(LibraryManager.class);
				VFSLeaf file = libraryManager.getFileByUUID(uuid);
				if(file == null) {
					DispatcherModule.sendNotFound(origUri, response);
				} else {
					libraryManager.increaseDownloadCount(file);
					VFSMediaResource resource = new VFSMediaResource(file);
					ServletUtil.serveResource(request, response, resource);
				}
			} else {
				DispatcherModule.sendForbidden(origUri, response);
			}
		}
	}
	
	private String getDocumentUUID(String origUri, String uriPrefix) {
		int lastIndex = origUri.indexOf('/', uriPrefix.length() + 1);
		if(lastIndex <= 0) {
			lastIndex = origUri.length();
		}
		return origUri.substring(uriPrefix.length(), lastIndex);
	}
	
	public String getLibraryBeanId() {
		return libraryBeanId == null ? DEFAULT_BEAN_ID : libraryBeanId;
	}
	
	public void setLibraryBeanId(String beanId) {
		this.libraryBeanId = beanId;
	}
	
	private String getRedirectToURL(UserSession usess, UserRequest ureq) {
		ChiefController cc = Windows.getWindows(usess).getChiefController(ureq);
		Window w = cc.getWindow();
		
		try(StringOutput sout = new StringOutput(30)) {
			URLBuilder ubu = new URLBuilder("", w.getInstanceId(), w.getTimestamp(), usess.getCsrfToken());
			ubu.buildURI(sout, null, null);
			return WebappHelper.getServletContextPath() + DispatcherModule.PATH_AUTHENTICATED + sout.toString();
		} catch(IOException e) {
			log.error("", e);
			return null;
		}
	}
}