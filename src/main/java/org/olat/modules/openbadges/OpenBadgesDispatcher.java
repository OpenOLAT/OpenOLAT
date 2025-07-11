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
package org.olat.modules.openbadges;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.olat.NewControllerFactory;
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
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.dispatcher.LocaleNegotiator;
import org.olat.login.DmzBFWCParts;
import org.olat.modules.openbadges.model.BadgeCryptoKey;
import org.olat.modules.openbadges.model.BadgeSigningOrganization;
import org.olat.modules.openbadges.ui.BadgeAssertionPublicController;
import org.olat.modules.openbadges.v2.Assertion;
import org.olat.modules.openbadges.v2.Badge;
import org.olat.modules.openbadges.v2.Constants;
import org.olat.modules.openbadges.v2.Criteria;
import org.olat.modules.openbadges.v2.CryptographicKey;
import org.olat.modules.openbadges.v2.Profile;
import org.olat.modules.openbadges.v2.RevocationList;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Initial date: 2023-05-09<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service(value="openbadgesbean")
public class OpenBadgesDispatcher implements Dispatcher {

	private static final Logger log = Tracing.createLoggerFor(OpenBadgesDispatcher.class);

	@Autowired
	private OpenBadgesModule openBadgesModule;

	@Autowired
	private OpenBadgesManager openBadgesManager;

	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!openBadgesModule.isEnabled()) {
			DispatcherModule.sendForbidden("not_enabled", response);
			return;
		}

		String uriPrefix = DispatcherModule.getLegacyUriPrefix(request);
		String requestUri = request.getRequestURI();
		String commandUriWithTrailingForwardSlash = requestUri.substring(uriPrefix.length());
		String commandUri = StringUtils.trimTrailingCharacter(commandUriWithTrailingForwardSlash, '/');
		log.debug("Method: " + request.getMethod() + ", URI prefix: " + uriPrefix + ", request URI: " + requestUri);

		if (commandUri.startsWith(OpenBadgesFactory.ASSERTION_PATH)) {
			handleAssertion(request, response, commandUri.substring(OpenBadgesFactory.ASSERTION_PATH.length()));
		} else if (commandUri.startsWith(OpenBadgesFactory.CLASS_PATH)) {
			handleClass(response, commandUri.substring(OpenBadgesFactory.CLASS_PATH.length()));
		} else if (commandUri.startsWith(OpenBadgesFactory.ISSUER_PATH)) {
			handleIssuer(response, commandUri.substring(OpenBadgesFactory.ISSUER_PATH.length()));
		} else if (commandUri.startsWith(OpenBadgesFactory.IMAGE_PATH)) {
			handleImage(response, commandUri.substring(OpenBadgesFactory.IMAGE_PATH.length()));
		} else if (commandUri.startsWith(OpenBadgesFactory.CRITERIA_PATH)) {
			handleCriteria(response, commandUri.substring(OpenBadgesFactory.CRITERIA_PATH.length()));
		} else if (commandUri.startsWith(OpenBadgesFactory.KEY_PATH)) {
			handleKey(response, commandUri.substring(OpenBadgesFactory.KEY_PATH.length()));
		} else if (commandUri.startsWith(OpenBadgesFactory.ORGANIZATION_PATH)) {
			handleOrganization(response, commandUri.substring(OpenBadgesFactory.ORGANIZATION_PATH.length()));
		} else if (commandUri.startsWith(OpenBadgesFactory.REVOCATION_LIST_PATH)) {
			handleRevocationList(response, commandUri.substring(OpenBadgesFactory.REVOCATION_LIST_PATH.length()));
		}
	}

	private void handleAssertion(HttpServletRequest request, HttpServletResponse response, String pathOrUuid) {
		if (pathOrUuid.endsWith(OpenBadgesFactory.WEB_SUFFIX)) {
			String uuid = pathOrUuid.substring(0, pathOrUuid.length() - OpenBadgesFactory.WEB_SUFFIX.length());
			handleAssertionWeb(request, response, uuid);
		} else {
			handleAssertionJson(response, pathOrUuid);
		}
	}

	private void handleAssertionWeb(HttpServletRequest request, HttpServletResponse response, String uuid) {
		BadgeAssertion assertion = openBadgesManager.getBadgeAssertion(uuid);
		if (assertion == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			log.warn("Could not find assertion for UUID {}", uuid);
			return;
		}

		String pathInfo = request.getPathInfo();
		String uriPrefix = DispatcherModule.getLegacyUriPrefix(request);
		UserRequest ureq;

		try {
			ureq = new UserRequestImpl(uriPrefix, request, response);
		} catch (Exception e) {
			log.debug("Bad request", e);
			DispatcherModule.sendBadRequest(pathInfo, response);
			return;
		}

		if (pathInfo.contains("close-window")) {
			DispatcherModule.setNotContent(request.getPathInfo(), response);
			return;
		}

		if (ureq.isValidDispatchURI()) {
			dispatchAssertionWeb(ureq);
			return;
		}

		if (ureq.getUserSession().isAuthenticated() && ureq.getIdentity() == assertion.getRecipient()) {
			redirectAssertionWeb(response, uuid, ureq.getIdentity());
			return;
		}

		launchAssertionWeb(ureq, uuid, uriPrefix);
	}

	private void launchAssertionWeb(UserRequest ureq, String uuid, String uriPrefix) {
		UserSession usess = ureq.getUserSession();

		usess.setLocale(LocaleNegotiator.getPreferedLocale(ureq));
		I18nManager.updateLocaleInfoToThread(usess);

		Windows windows = Windows.getWindows(ureq);
		boolean windowHere = windows.isExisting(uriPrefix, ureq.getWindowID());
		if (!windowHere) {
			DmzBFWCParts bfwcParts = new DmzBFWCParts();
			bfwcParts.showTopNav(false);
			ControllerCreator controllerCreator = (lureq, lwControl) -> new BadgeAssertionPublicController(lureq, lwControl, uuid, false);
			bfwcParts.setContentControllerCreator(controllerCreator);

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
			w.dispatchRequest(ureq, true);
			chiefController.resetReload();
		} catch (Exception e) {
			log.error("", e);
		}
	}

	private void redirectAssertionWeb(HttpServletResponse response, String uuid, Identity identity) {
		String redirectUrl = Settings.getServerContextPathURI()
				.concat("/auth/HomeSite/")
				.concat(identity.getKey().toString())
				.concat("/badges/0");
		DispatcherModule.redirectTo(response, redirectUrl);
	}

	private void dispatchAssertionWeb(UserRequest ureq) {
		Windows windows = Windows.getWindows(ureq);
		ChiefController chiefController = windows.getChiefController(ureq);
		try {
			Window w = chiefController.getWindow().getWindowBackOffice().getWindow();
			w.dispatchRequest(ureq, false);
		} catch (Exception e) {
			log.error("", e);
		}
	}

	private void handleAssertionJson(HttpServletResponse response, String uuid) {
		BadgeAssertion badgeAssertion = openBadgesManager.getBadgeAssertion(uuid);
		if (badgeAssertion == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			log.warn("Could not find assertion for UUID {}", uuid);
			return;
		}

		try {
			Assertion assertion = new Assertion(badgeAssertion);
			JSONObject jsonObject = assertion.asJsonObject();
			jsonObject.write(response.getWriter());
			response.setContentType("application/json; charset=utf-8");
		} catch (IOException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			log.warn("Could not deliver badge assertion", e);
		}
	}

	private void handleClass(HttpServletResponse response, String uuid) {
		BadgeClass badgeClass = openBadgesManager.getBadgeClassByUuid(uuid);
		if (badgeClass == null || !StringHelper.containsNonWhitespace(badgeClass.getImage())) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			log.warn("Could not find class for UUID {}", uuid);
			return;
		}

		try {
			Badge badge = new Badge(badgeClass);
			JSONObject jsonObject = badge.asJsonObject();
			jsonObject.write(response.getWriter());
			response.setContentType("application/json; charset=utf-8");
		} catch (IOException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			log.warn("Could not deliver badge class", e);
		}
	}

	private void handleIssuer(HttpServletResponse response, String uuid) {
		BadgeClass badgeClass = openBadgesManager.getBadgeClassByUuid(uuid);
		if (badgeClass == null || !StringHelper.containsNonWhitespace(badgeClass.getImage())) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			log.warn("Could not find issuer for UUID {}", uuid);
			return;
		}

		try {
			Badge badge = new Badge(badgeClass);
			JSONObject jsonObject = badge.asJsonObject();
			JSONObject issuerObject = jsonObject.getJSONObject("issuer");
			issuerObject.write(response.getWriter());
			response.setContentType("application/json; charset=utf-8");
		} catch (IOException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			log.warn("Could not deliver badge issuer", e);
		}
	}
	private void handleImage(HttpServletResponse response, String uuid) {
		BadgeClass badgeClass = openBadgesManager.getBadgeClassByUuid(uuid);
		if (badgeClass == null || !StringHelper.containsNonWhitespace(badgeClass.getImage())) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			log.warn("Could not find image for UUID {}", uuid);
			return;
		}

		handleImage(response, openBadgesManager.getBadgeClassVfsLeaf(badgeClass.getImage()));
		if (response != null) {
			return;
		}

		String suffix = FileUtils.getFileSuffix(badgeClass.getImage());
		if ("svg".equals(suffix)) {
			response.setContentType("image/svg+xml");
		} else if ("png".equals(suffix)) {
			response.setContentType("image/png");
		} else if (StringHelper.containsNonWhitespace(suffix)) {
			response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
			log.warn("Image type not supported: {}", suffix);
			return;
		} else {
			response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
			log.warn("No type found for image");
			return;
		}

		VFSLeaf imageLeaf = openBadgesManager.getBadgeClassVfsLeaf(badgeClass.getImage());
		if (imageLeaf == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			log.warn("Could not find image {}", badgeClass.getImage());
			return;
		}

		response.setContentLengthLong(imageLeaf.getSize());

		InputStream inputStream = imageLeaf.getInputStream();
		BufferedInputStream bufferedInputStream = null;

		try {
			int bufferSize = response.getBufferSize();
			bufferedInputStream = new BufferedInputStream(inputStream, bufferSize);
			IOUtils.copyLarge(bufferedInputStream, response.getOutputStream(), new byte[bufferSize]);
		} catch (IOException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			log.warn("Exception trying to deliver image", e);
		} finally {
			IOUtils.closeQuietly(inputStream);
			IOUtils.closeQuietly(bufferedInputStream);
		}
	}

	private void handleImage(HttpServletResponse response, VFSLeaf imageLeaf) {
		if (imageLeaf == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			log.warn("Could not find image");
			return;
		}

		String imageFileName = imageLeaf.getName();
		String suffix = FileUtils.getFileSuffix(imageFileName);
		if ("svg".equals(suffix)) {
			response.setContentType("image/svg+xml");
		} else if ("png".equals(suffix)) {
			response.setContentType("image/png");
		} else if (StringHelper.containsNonWhitespace(suffix)) {
			response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
			log.warn("Image type not supported: {}", suffix);
			return;
		} else {
			response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
			log.warn("No type found for image");
			return;
		}

		response.setContentLengthLong(imageLeaf.getSize());

		InputStream inputStream = imageLeaf.getInputStream();
		BufferedInputStream bufferedInputStream = null;

		try {
			int bufferSize = response.getBufferSize();
			bufferedInputStream = new BufferedInputStream(inputStream, bufferSize);
			IOUtils.copyLarge(bufferedInputStream, response.getOutputStream(), new byte[bufferSize]);
		} catch (IOException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			log.warn("Exception trying to deliver image", e);
		} finally {
			IOUtils.closeQuietly(inputStream);
			IOUtils.closeQuietly(bufferedInputStream);
		}
	}

	private void handleCriteria(HttpServletResponse response, String uuid) {
		BadgeClass badgeClass = openBadgesManager.getBadgeClassByUuid(uuid);
		if (badgeClass == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			log.warn("Could not find criteria for UUID {}", uuid);
			return;
		}

		try {
			Criteria criteria = new Criteria(badgeClass);
			JSONObject jsonObject = criteria.asJsonObject();
			jsonObject.write(response.getWriter());
			response.setContentType("application/json; charset=utf-8");
		} catch (IOException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			log.warn("Could not deliver badge criteria", e);
		}
	}
	
	private void handleKey(HttpServletResponse response, String relativePath) {
		if (!relativePath.endsWith(OpenBadgesFactory.PUBLIC_KEY_JSON) || relativePath.equals(OpenBadgesFactory.PUBLIC_KEY_JSON)) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			log.warn("Could not find public key file");
			return;
		}

		String uuid = relativePath.substring(0, relativePath.length() - OpenBadgesFactory.PUBLIC_KEY_JSON.length() - 1);
		if (!StringHelper.containsNonWhitespace(uuid)) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			log.warn("Could not find UUID for public key file");
			return;
		}

		BadgeClass badgeClass = openBadgesManager.getBadgeClassByUuid(uuid);
		if (badgeClass == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			log.warn("Could not find public key file for badge class with UUID {}", uuid);
			return;
		}

		try {
			BadgeCryptoKey badgeCryptoKey = openBadgesManager.getCryptoKey(badgeClass);
			CryptographicKey cryptographicKey = new CryptographicKey(badgeCryptoKey);
			JSONObject jsonObject = cryptographicKey.asJsonObject();
			jsonObject.write(response.getWriter());
			response.setContentType("application/json; charset=utf-8");
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			log.warn("Could not find public key file", e);
		}
	}
	
	private void handleRevocationList(HttpServletResponse response, String relativePath) {
		if (!relativePath.endsWith(OpenBadgesFactory.REVOCATION_LIST_JSON) || relativePath.equals(OpenBadgesFactory.REVOCATION_LIST_JSON)) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			log.warn("Could not find revocation file");
			return;
		}

		String uuid = relativePath.substring(0, relativePath.length() - OpenBadgesFactory.REVOCATION_LIST_JSON.length() - 1);
		if (!StringHelper.containsNonWhitespace(uuid)) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			log.warn("Could not find UUID for revocation list file");
			return;
		}

		BadgeClass badgeClass = openBadgesManager.getBadgeClassByUuid(uuid);
		if (badgeClass == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			log.warn("Could not find revocation list file for badge class with UUID {}", uuid);
			return;
		}

		try {
			List<String> revocationUuids = openBadgesManager.getRevokedBadgeAssertionIds(badgeClass);
			String issuerUrl = OpenBadgesFactory.createIssuerUrl(badgeClass.getUuid());
			RevocationList revocationList = new RevocationList(badgeClass.getUuid(), issuerUrl, revocationUuids);
			JSONObject jsonObject = revocationList.asJsonObject();
			jsonObject.write(response.getWriter());
			response.setContentType("application/json; charset=utf-8");
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			log.warn("Could not find revocation list file", e);
		}
	}
	
	private void handleOrganization(HttpServletResponse response, String relativePath) {
		if (!relativePath.endsWith(OpenBadgesFactory.ORGANIZATION_JSON) || relativePath.equals(OpenBadgesFactory.ORGANIZATION_JSON)) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			log.warn("Could not find organization file");
			return;
		}

		String uuid = relativePath.substring(0, relativePath.length() - OpenBadgesFactory.ORGANIZATION_JSON.length() - 1);
		if (!StringHelper.containsNonWhitespace(uuid)) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			log.warn("Could not find UUID for organization file");
			return;
		}

		BadgeClass badgeClass = openBadgesManager.getBadgeClassByUuid(uuid);
		if (badgeClass == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			log.warn("Could not find organization file for badge class with UUID {}", uuid);
			return;
		}

		try {
			BadgeSigningOrganization badgeSigningOrganization = openBadgesManager.getSigningOrganization(badgeClass);
			Profile profile = new Profile(badgeSigningOrganization);
			JSONObject jsonObject = profile.asJsonObject(Constants.TYPE_VALUE_ISSUER);
			jsonObject.write(response.getWriter());
			response.setContentType("application/json; charset=utf-8");
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			log.warn("Could not find organization file", e);
		}
	}

	public void setOpenBadgesManager(OpenBadgesManager openBadgesManager) {
		this.openBadgesManager = openBadgesManager;
	}
}
