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
package org.olat.modules.teams;

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
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.Tracing;
import org.olat.core.util.UserSession;
import org.olat.core.util.i18n.I18nManager;
import org.olat.dispatcher.LocaleNegotiator;
import org.olat.login.DmzBFWCParts;
import org.olat.modules.teams.ui.TeamsGuestCreator;

/**
 * 
 * Initial date: 14 déc. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeamsDispatcher implements Dispatcher {

	private static final Logger log = Tracing.createLoggerFor(TeamsDispatcher.class);
	
	private static final String TEAMS_PATH = "teams";
	
	public static final String getMeetingUrl(String identifier) {
		return new StringBuilder()
				.append(Settings.getServerContextPathURI())
				.append("/")
				.append(TEAMS_PATH)
				.append("/")
				.append(identifier)
				.toString();
	}
	
	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		UserRequest ureq = null;
		final String pathInfo = request.getPathInfo();
		String uriPrefix = DispatcherModule.getLegacyUriPrefix(request);
		try{
			ureq = new UserRequestImpl(uriPrefix, request, response);
		} catch(NumberFormatException nfe) {
			log.debug("Bad Request {}", pathInfo);
			DispatcherModule.sendBadRequest(pathInfo, response);
			return;
		}
		
		if(pathInfo != null && pathInfo.contains("close-window")) {
			DispatcherModule.setNotContent(pathInfo, response);
		} else if(ureq.isValidDispatchURI()) {
			Windows ws = Windows.getWindows(ureq);
			Window window = ws.getWindow(ureq);
			if (window == null) {
				DispatcherModule.sendNotFound(request.getRequestURI(), response);
			} else {
				window.dispatchRequest(ureq);
			}
		} else {
			TeamsMeeting meeting = getMeeting(request);
			dispatch(ureq, meeting, uriPrefix);
		}
	}
	
	private void dispatch(UserRequest ureq, TeamsMeeting meeting, String uriPrefix) {
		UserSession usess = ureq.getUserSession();
		
		usess.setLocale(LocaleNegotiator.getPreferedLocale(ureq));
		I18nManager.updateLocaleInfoToThread(usess);
		
		DmzBFWCParts bfwcParts = new DmzBFWCParts();
		bfwcParts.showTopNav(false);
		ControllerCreator controllerCreator = new TeamsGuestCreator(meeting);
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
	
	private TeamsMeeting getMeeting(HttpServletRequest request) {
		String uriPrefix = DispatcherModule.getLegacyUriPrefix(request);
		final String origUri = request.getRequestURI();
		String encodedRestPart = origUri.substring(uriPrefix.length());
		String restPart = encodedRestPart;
		try {
			restPart = URLDecoder.decode(encodedRestPart, "UTF8");
		} catch (UnsupportedEncodingException e) {
			log.error("Unsupported encoding", e);
		}
		return CoreSpringFactory.getImpl(TeamsService.class).getMeeting(restPart);
	}
}
