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
package org.olat.modules.contacttracing;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.NewControllerFactory;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.BaseFullWebappController;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.UserRequestImpl;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.Tracing;
import org.olat.core.util.UserSession;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.session.UserSessionManager;
import org.olat.dispatcher.LocaleNegotiator;
import org.olat.login.DmzBFWCParts;
import org.olat.modules.contacttracing.manager.ContactTracingManagerImpl;

/**
 * Initial date: 20.10.20<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ContactTracingDispatcher implements Dispatcher {

	private static final Logger log = Tracing.createLoggerFor(ContactTracingDispatcher.class);

	private static final String CONTACT_TRACING_PATH = "trace";

	public static String getRegistrationUrl(String identifier) {
		return new StringBuilder()
				.append(Settings.getServerContextPathURI())
				.append("/")
				.append(CONTACT_TRACING_PATH)
				.append("/")
				.append(identifier)
				.toString();
	}

	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		UserRequest ureq;
		final String pathInfo = request.getPathInfo();
		String uriPrefix = DispatcherModule.getLegacyUriPrefix(request);

		try {
			ureq = new UserRequestImpl(uriPrefix, request, response);
		} catch (NumberFormatException nfe) {
			log.debug("Bad Request {}", pathInfo);
			DispatcherModule.sendBadRequest(pathInfo, response);
			return;
		}

		if(pathInfo.contains("close-window")) {
			DispatcherModule.setNotContent(request.getPathInfo(), response);
			return;
		}

		// Redirect to default dispatcher if module is not enabled
		if (!CoreSpringFactory.getImpl(ContactTracingModule.class).isEnabled()) {
			DispatcherModule.redirectToDefaultDispatcher(response);
			return;
		}

		// Controller has been created already
		if (ureq.isValidDispatchURI()) {
			dispatch(ureq);
			return;
		}

		// Get location from URL
		ContactTracingLocation location = getLocation(request);

		// Check if location is existing
		if (location == null) {
			// Go to default screen
			DispatcherModule.redirectToDefaultDispatcher(response);
		} else if(ureq.getUserSession().isAuthenticated()) {
			// already authenticated
			String redirectURL = new StringBuilder()
					.append(Settings.getServerContextPathURI())
					.append("/auth/")
					.append(ContactTracingManagerImpl.CONTACT_TRACING_CONTEXT_KEY).append("/")
					.append(location.getKey())
					.toString();
			DispatcherModule.redirectTo(response, redirectURL);
		} else {
			// Dispatch with location
			launch(ureq, location, uriPrefix);
		}
	}

	private void dispatch(UserRequest ureq) {
		UserSession usess = ureq.getUserSession();
		Windows windows = Windows.getWindows(usess);

		ChiefController chiefController = windows.getChiefController(ureq);
		try {
			Window w = chiefController.getWindow().getWindowBackOffice().getWindow();
			w.dispatchRequest(ureq, false); // renderOnly
		} catch (Exception e) {
			log.error("", e);
		}
	}

	private void launch(UserRequest ureq, ContactTracingLocation location, String uriPrefix) {
		UserSession usess = ureq.getUserSession();
		usess.putEntryInNonClearedStore(UserSessionManager.EXTENDED_DMZ_TIMEOUT, Boolean.TRUE);
		usess.setLocale(LocaleNegotiator.getPreferedLocale(ureq));
		I18nManager.updateLocaleInfoToThread(usess);

		Windows windows = Windows.getWindows(usess);
		boolean windowHere = windows.isExisting(uriPrefix, ureq.getWindowID());
		if (windowHere) {
			dispatch(ureq);
		} else {
			synchronized (windows) {
				DmzBFWCParts bfwcParts = new DmzBFWCParts();
				bfwcParts.showTopNav(false);
				ControllerCreator controllerCreator = new ContactTracingRegistrationExternalWrapperControllerCreator(location);
				bfwcParts.setContentControllerCreator(controllerCreator);

				ChiefController cc = new BaseFullWebappController(ureq, bfwcParts);
				Window window = cc.getWindow();
				window.setUriPrefix(uriPrefix);
				ureq.overrideWindowComponentID(window.getDispatchID());
				windows.registerWindow(cc);

				NewControllerFactory.getInstance().launch(ureq, cc.getWindowControl());
				Window w = cc.getWindow().getWindowBackOffice().getWindow();
				w.dispatchRequest(ureq, true); // renderOnly
				cc.resetReload();
			}
		}
	}

	private ContactTracingLocation getLocation(HttpServletRequest request) {
		String uriPrefix = DispatcherModule.getLegacyUriPrefix(request);
		final String origUri = request.getRequestURI();
		String encodedRestPart = origUri.substring(uriPrefix.length());
		String restPart = encodedRestPart;
		try {
			restPart = URLDecoder.decode(encodedRestPart, "UTF8");
		} catch (UnsupportedEncodingException e) {
			log.error("Unsupported encoding", e);
		}
		return CoreSpringFactory.getImpl(ContactTracingManager.class).getLocation(restPart);
	}
}
