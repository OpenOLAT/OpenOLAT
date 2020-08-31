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
package org.olat.core.commons.services.doceditor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.NewControllerFactory;
import org.olat.core.commons.fullWebApp.BaseFullWebappController;
import org.olat.core.commons.services.doceditor.ui.DocEditorStandaloneController;
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
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.i18n.I18nManager;
import org.olat.dispatcher.LocaleNegotiator;
import org.olat.login.DmzBFWCParts;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10 Jul 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DocEditorDispatcher implements Dispatcher {

	private static final Logger log = Tracing.createLoggerFor(DocEditorDispatcher.class);
	
	private static final String DOC_EDITOR_PATH = "document";
	
	public static final String getDocumentUrl(Access access) {
		StringBuilder sb = new StringBuilder();
		sb.append(Settings.getServerContextPathURI());
		appendDocumentPath(sb, access);
		return sb.toString();
	}
	
	/**
	 * @param access
	 * @return the path to the document with a leading "/".
	 */
	public static final String getDocumentPath(Access access) {
		StringBuilder sb = new StringBuilder();
		appendDocumentPath(sb, access);
		return sb.toString();
	}
	
	private static final void appendDocumentPath(StringBuilder sb, Access access) {
		sb.append("/")
			.append(DOC_EDITOR_PATH)
			.append("/")
			.append(access.getToken());
	}
	
	@Autowired
	private DocEditorService docEditorService;
	
	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("Dispatch dispatcher request: {}", request.getRequestURI());
		
		String accessToken = getAccessToken(request);
		if (!StringHelper.containsNonWhitespace(accessToken) || accessToken.contains(":")) {
			log.debug("Invalid access token {}", accessToken);
			DispatcherModule.sendNotFound(request.getPathInfo(), response);
			return;
		}
		
		log.debug("Document dispatcher: Get document. Token {}", accessToken);
		Access access = docEditorService.getAccess(accessToken);
		if (access == null) {
			log.warn("No access for token {}", accessToken);
			DispatcherModule.sendNotFound(request.getPathInfo(), response);
			return;
		}
		
		if (!docEditorService.isEditorEnabled(access)) {
			log.warn("Doc editor {} not enbaled. Token {}", access.getEditorType(), accessToken);
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			response.setContentType("text/plain");
			return;
		}
		
		UserRequest ureq = null;
		String uriPrefix = DispatcherModule.getLegacyUriPrefix(request);
		try{
			String uriPrefixWithToken = uriPrefix + access.getToken();
			ureq = new UserRequestImpl(uriPrefixWithToken, request, response);
		} catch(NumberFormatException nfe) {
			DispatcherModule.sendBadRequest(request.getPathInfo(), response);
			return;
		}
		
		if (!access.getIdentity().equals(ureq.getIdentity())) {
			log.warn("Document dispatcher: Access forbidden to {}. Token {}", ureq.getIdentity(), accessToken);
			DispatcherModule.sendForbidden(request.getPathInfo(), response);
			return;
		}
		
		UserSession usess = ureq.getUserSession();
		DocEditorConfigs configs = getConfigs(usess, access);
		if (configs == null) {
			log.warn("Document dispatcher: No configs found. Token {}", accessToken);
			DispatcherModule.sendNotFound(request.getPathInfo(), response);
			return;
		}
		
		if (usess.isAuthenticated()) {
			dispatch(ureq, usess, uriPrefix, access, configs);
		} else {
			log.warn("Document dispatcher: {} not authenticated. Token {}", ureq.getIdentity(),  accessToken);
			DispatcherModule.sendForbidden(request.getPathInfo(), response);
		}
	}
	
	private String getAccessToken(HttpServletRequest request) {
		String uriPrefix = DispatcherModule.getLegacyUriPrefix(request);
		final String origUri = request.getRequestURI();
		String encodedRestPart = origUri.substring(uriPrefix.length());
		String restPart = encodedRestPart;
		try {
			restPart = URLDecoder.decode(encodedRestPart, "UTF8");
		} catch (UnsupportedEncodingException e) {
			log.error("Unsupported encoding", e);
		}

		// .../document/accessToken
		String[] split = restPart.split("/");
		if (split.length >= 1) {
			return split[0];
		}
		return null;
	}
	
	private DocEditorConfigs getConfigs(UserSession usess, Access access) {
		Object entry = usess.getEntry(access.getToken());
		if (entry instanceof DocEditorConfigs) {
			return (DocEditorConfigs)entry;
		}
		return null;
	}
	
	private void dispatch(UserRequest ureq, UserSession usess, String uriPrefix,
			Access access, DocEditorConfigs configs) {
		usess.setLocale(LocaleNegotiator.getPreferedLocale(ureq));
		I18nManager.updateLocaleInfoToThread(usess);
		
		DmzBFWCParts bfwcParts = new DmzBFWCParts();
		bfwcParts.showTopNav(false);
		ControllerCreator controllerCreator = (lureq, lwControl) -> {
			return new DocEditorStandaloneController(lureq, lwControl, access, configs);
		};
		bfwcParts.setContentControllerCreator(controllerCreator);
		
		Windows windows = Windows.getWindows(usess);
		String uriPrefixWithAccess = uriPrefix + access.getToken() + "/";
		boolean windowHere = windows.isExisting(ureq);
		if (!windowHere) {
			synchronized (windows) {
				ChiefController cc = new BaseFullWebappController(ureq, bfwcParts);
				Window window = cc.getWindow();
				window.setUriPrefix(uriPrefixWithAccess);
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
