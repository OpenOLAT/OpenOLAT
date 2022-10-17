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
package org.olat.modules.forms;

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
import org.olat.core.gui.control.WindowBackOffice;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.helpers.Settings;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.i18n.I18nManager;
import org.olat.dispatcher.LocaleNegotiator;
import org.olat.login.DmzBFWCParts;
import org.olat.modules.forms.ui.StandaloneExecutionCreator;

/**
 * Dispatcher to execute a survey participation without user authentication.
 * 
 * Initial date: 21 Nov 2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormDispatcher implements Dispatcher {

	private static final Logger log = Tracing.createLoggerFor(EvaluationFormDispatcher.class);
	
	private static final String SURVEY_PATH = "survey";
	
	public static final String getExecutionUrl(EvaluationFormParticipationIdentifier identifier) {
		return new StringBuilder()
				.append(Settings.getServerContextPathURI())
				.append("/")
				.append(SURVEY_PATH)
				.append("/")
				.append(identifier.getType())
				.append("/")
				.append(identifier.getKey())
				.toString();
	}
	
	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("Dispatch survey request: {}", request.getRequestURI());
		
		EvaluationFormParticipationIdentifier identifier = getIdentifier(request);
		if (identifier == null) {
			log.debug("Invalid survey identifiers.");
			DispatcherModule.sendBadRequest(request.getRequestURI(), response);
			return;
		}
		
		UserRequest ureq = null;
		String uriPrefix = DispatcherModule.getLegacyUriPrefix(request);
		try{
			uriPrefix = uriPrefix + identifier.getType() + "/";
			ureq = new UserRequestImpl(uriPrefix, request, response);
		} catch(NumberFormatException nfe) {
			log.debug("Bad Request {}", request.getPathInfo());
			DispatcherModule.sendBadRequest(request.getPathInfo(), response);
			return;
		}
		
		UserSession usess = ureq.getUserSession();
		if (usess.isAuthenticated()) {
			dispatchAuthenticated(response, identifier, ureq, usess);
		} else if (ureq.isValidDispatchURI()) {
			dispatchValidUnauthenticated(ureq);
		} else {
			dispatchUnautenticated(ureq, identifier, uriPrefix);
		}
	}

	private void dispatchAuthenticated(HttpServletResponse response, EvaluationFormParticipationIdentifier identifier,
			UserRequest ureq, UserSession usess) {
		EvaluationFormParticipation participation = CoreSpringFactory.getImpl(EvaluationFormManager.class)
				.loadParticipationByIdentifier(identifier);
		EvaluationFormStandaloneProvider standaloneProvider = CoreSpringFactory
				.getImpl(EvaluationFormStandaloneProviderFactory.class)
				.getProvider(participation.getSurvey().getIdentifier().getOLATResourceable());
		if (participation.getExecutor().equals(usess.getIdentity()) && standaloneProvider.hasBusinessPath(participation)) {
			String path = standaloneProvider.getBusinessPath(participation);
			dispatchAuthenticated(ureq, usess, path);
		} else {
			DispatcherModule.redirectToDefaultDispatcher(response);
		}
	}
	
	private void dispatchAuthenticated(UserRequest ureq, UserSession usess, String path) {
		ChiefController chiefController = Windows.getWindows(usess).getChiefController(ureq);
		if(chiefController == null) {
			// not yet checked
			String requestUri = BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(path);
			try(StringOutput clientSideWindowCheck = new StringOutput()) {
				clientSideWindowCheck.append("<!DOCTYPE html>\n<html><head><title>Reload</title><script>")
					.append("window.location.replace('").append(requestUri).append("?");
				clientSideWindowCheck
					.append("oow=' + window.name").append(");")
					.append("</script></head><body></body></html>");
				ServletUtil.serveStringResource(ureq.getHttpResp(), clientSideWindowCheck);
			} catch(IOException e) {
				log.error("", e);
			}
			return;
		}
		
		WindowBackOffice windowBackOffice = chiefController.getWindow().getWindowBackOffice();
		WindowControl wControl = chiefController.getWindowControl();
		NewControllerFactory.getInstance().launch(path, ureq, wControl);

		Window w = windowBackOffice.getWindow();
		log.debug("Dispatch survey request by window {}", w.getInstanceId());
		w.dispatchRequest(ureq, true);
		chiefController.resetReload();
	}
	
	private void dispatchValidUnauthenticated(UserRequest ureq) {
		Windows windows = Windows.getWindows(ureq);
		Window window = windows.getWindow(ureq);
		window.dispatchRequest(ureq);
		log.debug("Dispatch survey request by window {}", window.getInstanceId());
	}
	
	private void dispatchUnautenticated(UserRequest ureq, EvaluationFormParticipationIdentifier identifier, String uriPrefix) {
		UserSession usess = ureq.getUserSession();
		
		usess.setLocale(LocaleNegotiator.getPreferedLocale(ureq));
		I18nManager.updateLocaleInfoToThread(usess);
		
		DmzBFWCParts bfwcParts = new DmzBFWCParts();
		bfwcParts.showTopNav(false);
		ControllerCreator controllerCreator = new StandaloneExecutionCreator(identifier);
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
	
	private EvaluationFormParticipationIdentifier getIdentifier(HttpServletRequest request) {
		String uriPrefix = DispatcherModule.getLegacyUriPrefix(request);
		final String origUri = request.getRequestURI();
		String encodedRestPart = origUri.substring(uriPrefix.length());
		String restPart = encodedRestPart;
		try {
			restPart = URLDecoder.decode(encodedRestPart, "UTF8");
		} catch (UnsupportedEncodingException e) {
			log.error("Unsupported encoding", e);
		}
		
		String[] split = restPart.split("/");
		// It's ok to have more than two parts. The third part and the following are just ignored.
		if (split.length >= 2) {
			String type = split[0];
			String key = split[1];
			if (StringHelper.containsNonWhitespace(type) && StringHelper.containsNonWhitespace(key)) {
				return new EvaluationFormParticipationIdentifier(type, key);
			}
		}
		return null;
	}
	
}
