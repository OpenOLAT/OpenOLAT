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
package org.olat.core.commons.services.robots;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 30.11.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RobotsDispatcher implements Dispatcher {
	
	private static final Logger log = Tracing.createLoggerFor(RobotsDispatcher.class);

	@Autowired
	private RobotsService robotsService;

	@Override
	public boolean isSessionRequired() {
		return false;
	}

	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
		StringBuilder sb = new StringBuilder();
		sb.append("User-agent: *\n");
		sb.append("Disallow: /");
		List<String> allows = robotsService.getRobotsAllows();
		for (String allow : allows) {
			sb.append("\nAllow: ").append(allow);
			// Disallow indexing the ajax responses
			sb.append("\nDisallow: ").append(allow).append("1:");
			sb.append("\nDisallow: ").append(allow).append("1%3A1");
		}

		String sitemapIndexPath = robotsService.getSitemapIndexPath();
		if (StringHelper.containsNonWhitespace(sitemapIndexPath)) {
			sb.append("\nAllow: ").append(sitemapIndexPath);
			sb.append("\n\nSitemap: ").append(Settings.getServerContextPathURI()).append(sitemapIndexPath);
		}

		try {
			ServletUtil.servePublicContent(request, response, sb.toString(), "text/plain", ServletUtil.CACHE_ONE_HOUR);
		} catch (IOException e) {
			log.error("", e);
		}
	}
}
