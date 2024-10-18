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
import org.olat.core.commons.services.robots.model.SitemapIndexItem;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.gui.media.StringMediaResource;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 Oct 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.comm
 *
 */
public class SitemapDispatcher implements Dispatcher {
	
	private static final Logger log = Tracing.createLoggerFor(SitemapDispatcher.class);
	
	@Autowired
	private RobotsService robotsService;
	
	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		List<SitemapIndexItem> sitemapIndexItems = robotsService.getSitemapIndexItems();
		if (sitemapIndexItems == null || sitemapIndexItems.isEmpty()) {
			// No provider has a sitemap activated
			DispatcherModule.sendBadRequest(request.getPathInfo(), response);
		}
		
		String result = new SitemapIndexWriter(sitemapIndexItems).getSitemapIndex();
		
		StringMediaResource mr = new StringMediaResource();
		mr.setContentType("application/xml");
		mr.setEncoding("UTF-8");
		mr.setData(result);
		
		response.setCharacterEncoding("UTF-8");
		response.setContentType(mr.getContentType());
		
		try {
			ServletUtil.serveResource(request, response, mr);
		} catch (Exception e) {
			log.error("", e);
			DispatcherModule.sendServerError(response);
		}
	}
	
}
