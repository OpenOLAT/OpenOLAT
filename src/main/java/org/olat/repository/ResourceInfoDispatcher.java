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
package org.olat.repository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.Logger;
import org.olat.NewControllerFactory;
import org.olat.basesecurity.AuthHelper;
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
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.Tracing;
import org.olat.core.util.UserSession;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.dispatcher.LocaleNegotiator;
import org.olat.login.DmzBFWCParts;
import org.olat.modules.oaipmh.OAIPmhModule;
import org.olat.repository.ui.list.RepositoryEntryPublicInfosController;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ResourceInfoDispatcher implements Dispatcher {

	private static final Logger log = Tracing.createLoggerFor(ResourceInfoDispatcher.class);

	private static final String RESOURCEINFO_PATH = "resourceinfo";

	@Autowired
	private OAIPmhModule oaiPmhModule;
	@Autowired
	private RepositoryService repositoryService;

	public static final String getUrl(String requestedData) {
		return new StringBuilder()
				.append(Settings.getServerContextPathURI())
				.append("/")
				.append(RESOURCEINFO_PATH)
				.append("/")
				.append(requestedData)
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

		if (!oaiPmhModule.isEnabled()) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		// requestedData is either a repositoryEntryId or a teaserImage file name for that respective learningResource
		String requestedData = ureq.getNonParsedUri();

		if (pathInfo.contains("close-window")) {
			DispatcherModule.setNotContent(request.getPathInfo(), response);
		} else if (ureq.isValidDispatchURI()) {
			Windows ws = Windows.getWindows(ureq);
			Window window = ws.getWindow(ureq);
			if (window == null) {
				DispatcherModule.sendNotFound(request.getRequestURI(), response);
			} else {
				window.dispatchRequest(ureq);
			}
		} else if (IsValidImage(requestedData)) {
			String mimeType = requestedData.substring(requestedData.indexOf('.') + 1);
			requestedData = requestedData.substring(0, requestedData.indexOf('.'));
			RepositoryEntry entry = getRepositoryEntryById(Long.valueOf(requestedData));

			MediaResource image = getRepositoryEntryImage(entry);
			// Only return image if correct mimeType is used
			if (image.getContentType().contains(mimeType)) {
				ServletUtil.serveResource(request, response, image);
			} else {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
		} else {
			dispatch(ureq, uriPrefix, requestedData, response);
		}
	}

	private void dispatch(UserRequest ureq, String uriPrefix, String requestedData, HttpServletResponse response) {
		UserSession usess = ureq.getUserSession();

		usess.setLocale(LocaleNegotiator.getPreferedLocale(ureq));
		I18nManager.updateLocaleInfoToThread(usess);

		if (!usess.isAuthenticated()) {
			int loginStatus = AuthHelper.doAnonymousLogin(ureq, usess.getLocale());
			if (loginStatus == AuthHelper.LOGIN_OK) {
				//
			} else if (loginStatus == AuthHelper.LOGIN_NOTAVAILABLE) {
				DispatcherModule.redirectToServiceNotAvailable(response);
				return;
			} else {
				//error, redirect to login screen
				DispatcherModule.redirectToDefaultDispatcher(response);
				return;
			}
		}

		DmzBFWCParts bfwcParts = new DmzBFWCParts();
		bfwcParts.showTopNav(false);
		final RepositoryEntry entry = getRepositoryEntryById(Long.valueOf(requestedData));
		ControllerCreator controllerCreator = (uureq, wControl) -> new RepositoryEntryPublicInfosController(uureq, wControl, entry);
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
			w.dispatchRequest(ureq, false); // renderOnly
			chiefController.resetReload();
		} catch (Exception e) {
			log.error("", e);
		}
	}

	private boolean IsValidImage(String requestedData) {
		String[] allowedTeaserImageTypes = {"jpg", "jpeg", "png", "gif"};
		return Arrays.asList(allowedTeaserImageTypes).contains(requestedData.substring(requestedData.lastIndexOf('.') + 1));
	}

	private MediaResource getRepositoryEntryImage(RepositoryEntry entry) {
		VFSLeaf image = repositoryService.getIntroductionImage(entry);
		return new VFSMediaResource(image);
	}

	private RepositoryEntry getRepositoryEntryById(Long entryId) {
		return repositoryService.loadByKey(entryId);
	}

}
