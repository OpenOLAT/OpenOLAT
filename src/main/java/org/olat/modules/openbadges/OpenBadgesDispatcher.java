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

import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.openbadges.v2.Badge;
import org.olat.modules.openbadges.v2.Criteria;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: 2023-05-09<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service(value="openbadgesbean")
public class OpenBadgesDispatcher implements Dispatcher {

	private static final Logger log = Tracing.createLoggerFor(OpenBadgesDispatcher.class);

	public static final String BADGE_PATH = "badge/";
	public static final String ASSERTION_PATH = "assertion/";
	public static final String CLASS_PATH = "class/";
	public static final String IMAGE_PATH = "image/";
	public static final String BAKED_PATH = "baked/";
	public static final String CRITERIA_PATH = "criteria/";
	public static final String ISSUER_PATH = "issuer/";

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
		String commandUri = requestUri.substring(uriPrefix.length());

		log.debug("Method: " + request.getMethod() + ", URI prefix: " + uriPrefix + ", request URI: " + requestUri);

		if (commandUri.startsWith(ASSERTION_PATH)) {
			handleAssertion(response, commandUri.substring(ASSERTION_PATH.length()));
		} else if (commandUri.startsWith(CLASS_PATH)) {
			handleClass(response, commandUri.substring(CLASS_PATH.length()));
		} else if (commandUri.startsWith(IMAGE_PATH)) {
			handleImage(response, commandUri.substring(IMAGE_PATH.length()));
		} else if (commandUri.startsWith(CRITERIA_PATH)) {
			handleCriteria(response, commandUri.substring(CRITERIA_PATH.length()));
		}
	}

	private void handleAssertion(HttpServletResponse response, String uuid) {
		BadgeAssertion assertion = openBadgesManager.getBadgeAssertion(uuid);
		if (assertion == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			log.warn("Could not find assertion for UUID {}", uuid);
			return;
		}


	}

	private void handleClass(HttpServletResponse response, String uuid) {
		BadgeClass badgeClass = openBadgesManager.getBadgeClass(uuid);
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

	private void handleImage(HttpServletResponse response, String uuid) {
		BadgeClass badgeClass = openBadgesManager.getBadgeClass(uuid);
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
		BadgeClass badgeClass = openBadgesManager.getBadgeClass(uuid);
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

	public void setOpenBadgesManager(OpenBadgesManager openBadgesManager) {
		this.openBadgesManager = openBadgesManager;
	}
}
