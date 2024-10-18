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
package org.olat.core.commons.services.robots.manager;

import java.util.List;
import java.util.Objects;

import org.olat.core.commons.services.robots.RobotsProvider;
import org.olat.core.commons.services.robots.RobotsService;
import org.olat.core.commons.services.robots.SitemapProvider;
import org.olat.core.commons.services.robots.model.SitemapIndexItem;
import org.olat.core.helpers.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 17 Oct 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Service
public class RobotsServiceImpl implements RobotsService {
	
	// Defined in spring (dispatcherContext.xml)
	private static final String SITEMAP_URL =  Settings.getServerContextPathURI() + "/sitemap.xml";
	
	@Autowired
	private List<RobotsProvider> robotsProviders;
	@Autowired
	private List<SitemapProvider> sitemapProviders;
	
	@Override
	public List<String> getRobotsAllows() {
		return robotsProviders.stream()
				.map(RobotsProvider::getRobotAllows)
				.filter(Objects::nonNull)
				.flatMap(List::stream)
				.toList();
	}

	@Override
	public String getSitemapIndexUrl() {
		boolean hasSitemap = sitemapProviders.stream()
				.map(SitemapProvider::getSitemapUrls)
				.anyMatch(urls -> urls != null && !urls.isEmpty());
		return hasSitemap? SITEMAP_URL: null;
	}

	@Override
	public List<SitemapIndexItem> getSitemapIndexItems() {
		return sitemapProviders.stream()
				.map(SitemapProvider::getSitemapUrls)
				.filter(Objects::nonNull)
				.flatMap(List::stream)
				.map(SitemapIndexItem::new)
				.toList();
	}
}
