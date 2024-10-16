/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.catalog;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

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
import org.olat.core.gui.control.creator.AutoCreator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.Tracing;
import org.olat.core.servlets.RequestAbortedException;
import org.olat.core.util.ArrayHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.session.UserSessionManager;
import org.olat.dispatcher.LocaleNegotiator;
import org.olat.login.DmzBFWCParts;
import org.olat.modules.catalog.ui.WebCatalogMainController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 1 Oct 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class WebCatalogDispatcher implements Dispatcher {

	private static final Logger log = Tracing.createLoggerFor(WebCatalogDispatcher.class);
	
	public static final String PATH_CATALOG = "catalog";
	private static final String PATH_TEMPORARILY_DISABLED = "catalogtemporarilydisabled";
	private static final String URI_TEMPORARILY_DISABLED =
			WebappHelper.getServletContextPath() + "/" + PATH_TEMPORARILY_DISABLED + "/";
	
	public static StringBuilder getBaseUrl() {
		return new StringBuilder()
				.append(Settings.getServerContextPathURI())
				.append("/")
				.append(PATH_CATALOG)
				.append("/");
	}
	
	@Autowired
	private CatalogV2Module catalogModule;

	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!catalogModule.isEnabled() || !catalogModule.isWebPublishEnabled()) {
			DispatcherModule.sendBadRequest(request.getPathInfo(), response);
			return;
		}
		
		if (catalogModule.isWebPublishTemporarilyDisabled()) {
			DispatcherModule.redirectTo(response, URI_TEMPORARILY_DISABLED);
			return;
		}
		
		UserRequest ureq = null;
		String uriPrefix = DispatcherModule.getLegacyUriPrefix(request);
		try {
			ureq = new UserRequestImpl(uriPrefix, request, response);
		} catch (RequestAbortedException | NumberFormatException nfe) {
			log.debug("Bad Request {}", request.getPathInfo());
			DispatcherModule.sendBadRequest(request.getPathInfo(), response);
			return;
		}
		
		if (ureq.getUserSession().isAuthenticated()) {
			UserSession usess = ureq.getUserSession();
			synchronized (usess) {
				CoreSpringFactory.getImpl(UserSessionManager.class).signOffAndClear(usess);
				usess.setLocale(LocaleNegotiator.getPreferedLocale(ureq));
				I18nManager.updateLocaleInfoToThread(usess);
			}
		}
		
		UserSession usess = ureq.getUserSession();
		usess.setLocale(LocaleNegotiator.getPreferedLocale(ureq));
		I18nManager.updateLocaleInfoToThread(usess);
		
		Windows windows = Windows.getWindows(ureq);
		windows.getWindowManager().setAjaxWanted(ureq);
		
		if (ureq.isValidDispatchURI()) {
			Window window = windows.getWindow(ureq);
			window.dispatchRequest(ureq, true);
			return;
		}
		
		DmzBFWCParts bfwcParts = new DmzBFWCParts();
		AutoCreator controllerCreator = new AutoCreator();
		controllerCreator.setClassName(WebCatalogMainController.class.getName());
		bfwcParts.setContentControllerCreator(controllerCreator);
		
		boolean windowHere = windows.isExisting(uriPrefix, ureq.getWindowID());
		if (!windowHere) {
			synchronized (windows) {
				ChiefController cc = new BaseFullWebappController(ureq, bfwcParts);
				Window window = cc.getWindow();
				window.setUriPrefix(uriPrefix);
				ureq.overrideWindowComponentID(window.getDispatchID());
				windows.registerPersistentWindow(cc, request.getSession());
			}
		}
		
		ChiefController chiefController = windows.getChiefController(ureq);
		try {
			WindowControl wControl = chiefController.getWindowControl();
			NewControllerFactory.getInstance().launch(ureq, wControl);	
			Window w = chiefController.getWindow().getWindowBackOffice().getWindow();
			
			String[] restParts = extractRestParts(request, uriPrefix);
			if (restParts.length > 0 && restParts.length % 2 == 0) {
				String businessPath = BusinessControlFactory.getInstance().formatFromSplittedURI(restParts);
				List<ContextEntry> ces = BusinessControlFactory.getInstance().createCEListFromString(businessPath);
				w.getDTabs().activate(ureq, null, ces);
			}
			
			w.dispatchRequest(ureq, true);
			chiefController.resetReload();
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	private String[] extractRestParts(HttpServletRequest request, String uriPrefix) {
		String uri = request.getRequestURI();
		String restPart = uri.substring(uriPrefix.length());
		try {
			restPart = URLDecoder.decode(restPart, "UTF8");
		} catch (UnsupportedEncodingException e) {
			log.error("Unsupported encoding", e);
		}
		
		if (restPart == null) {
			return ArrayHelper.emptyStrings();
		}
		
		return restPart.split("/");
	}

}
