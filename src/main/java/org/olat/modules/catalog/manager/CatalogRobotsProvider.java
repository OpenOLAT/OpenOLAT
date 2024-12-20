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
package org.olat.modules.catalog.manager;

import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.commons.services.robots.RobotsProvider;
import org.olat.core.commons.services.robots.SitemapProvider;
import org.olat.core.commons.services.robots.model.SitemapItem;
import org.olat.core.helpers.Settings;
import org.olat.modules.catalog.CatalogEntrySearchParams;
import org.olat.modules.catalog.CatalogV2Module;
import org.olat.modules.catalog.WebCatalogDispatcher;
import org.olat.modules.catalog.ui.CatalogBCFactory;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 18 Oct 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Service
public class CatalogRobotsProvider implements RobotsProvider, SitemapProvider {
	
	@Autowired
	private CatalogV2Module catalogModule;
	@Autowired
	private CatalogQueries queries;
	@Autowired
	private CurriculumModule curriculumModule;
	
	private boolean isEnabled() {
		return catalogModule.isEnabled() && catalogModule.isWebPublishEnabled() && !catalogModule.isWebPublishTemporarilyDisabled();
	}

	@Override
	public List<String> getRobotAllows() {
		if (isEnabled()) {
			return List.of(Settings.getServerContextPath() + "/" + WebCatalogDispatcher.PATH_CATALOG);
		}
		return null;
	}

	@Override
	public List<String> getSitemapUrls() {
		if (isEnabled()) {
			return List.of(WebCatalogDispatcher.getBaseUrl().append("sitemap.xml").toString());
		}
		return null;
	}
	
	public List<SitemapItem> getSitemapItems() {
		if (isEnabled()) {
			CatalogEntrySearchParams searchParams = new CatalogEntrySearchParams();
			searchParams.setWebPublish(true);
			List<SitemapItem> items = queries.loadRepositoryEntries(searchParams)
					.stream()
					.map(this::toSitemapItem)
					.collect(Collectors.toList());
			
			if (curriculumModule.isEnabled()) {
				List<SitemapItem> ceItems = queries.loadCurriculumElements(searchParams)
						.stream()
						.map(this::toSitemapItem)
						.collect(Collectors.toList());
				items.addAll(ceItems);
			}
			
			return items;
		}
		return null;
	}
	
	private SitemapItem toSitemapItem(RepositoryEntry repositoryEntry) {
		String url = CatalogBCFactory.get(true).getOfferUrl(repositoryEntry.getOlatResource());
		return new SitemapItem(url, repositoryEntry.getLastModified(), SitemapItem.FREQ_WEEKLY);
	}

	private SitemapItem toSitemapItem(CurriculumElement curriculumelement) {
		String url = CatalogBCFactory.get(true).getOfferUrl(curriculumelement.getResource());
		return new SitemapItem(url, curriculumelement.getLastModified(), SitemapItem.FREQ_WEEKLY);
	}

}
