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
package org.olat.modules.immunityproof;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.UserRequestImpl;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.Tracing;
import org.olat.core.util.UserSession;

/**
 * Initial date: 10.09.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ImmunityProofDispatcher implements Dispatcher {

	private static final Logger log = Tracing.createLoggerFor(ImmunityProofDispatcher.class);

	private static final String IMMUNITY_PROOF_PATH = "proof";

	public static String getImmunityProofUrl() {
		return new StringBuilder()
				.append(Settings.getServerContextPathURI())
				.append("/")
				.append(IMMUNITY_PROOF_PATH)
				.toString();
	}

	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		UserRequest ureq = null;
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

		// Controller has been created already
		if (ureq.isValidDispatchURI()) {
			dispatch(ureq);
			return;
		}

		if (!ureq.getUserSession().isAuthenticated()) {
			// Go to default screen
			DispatcherModule.redirectToDefaultDispatcher(response);
		} else if(ureq.getUserSession().isAuthenticated()) {
			// already authenticated
			String redirectURL = new StringBuilder()
					.append(Settings.getServerContextPathURI())
					.append("/auth")
					.append("/HomeSite").append("/")
					.append(ureq.getUserSession().getIdentity().getKey())
					.append("/profil").append("/0")
					.append("/ImmunityProof").append("/0")
					.toString();
			DispatcherModule.redirectTo(response, redirectURL);
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
	
}
