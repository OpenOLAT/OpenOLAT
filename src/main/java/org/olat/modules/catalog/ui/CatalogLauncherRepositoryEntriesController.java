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
package org.olat.modules.catalog.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.emptystate.EmptyState;
import org.olat.core.gui.components.emptystate.EmptyStateFactory;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.catalog.CatalogRepositoryEntry;
import org.olat.modules.taxonomy.model.TaxonomyLevelNamePath;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.RepositoryEntryImageMapper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogLauncherRepositoryEntriesController extends BasicController implements Controller {
	
	public static final int PREFERED_NUMBER_CARDS = 15;
	private static final String[] SWIPER_JS = new String[] { "js/swiper/swiper-bundle.min.js" };

	private final VelocityContainer mainVC;
	private Link titleLink;
	
	private final CatalogRepositoryEntryState state;
	private final MapperKey mapperThumbnailKey;

	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private MapperService mapperService;

	public CatalogLauncherRepositoryEntriesController(UserRequest ureq, WindowControl wControl,
			List<CatalogRepositoryEntry> entries, String title, boolean showMore, CatalogRepositoryEntryState state) {
		super(ureq, wControl);
		this.state = state;
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.mapperThumbnailKey = mapperService.register(null, "repositoryentryImage", new RepositoryEntryImageMapper());
		
		mainVC = createVelocityContainer("launch_repository_entry");
		
		JSAndCSSComponent js = new JSAndCSSComponent("js", SWIPER_JS, null);
		mainVC.put("js", js);
		
		
		if (entries.isEmpty()) {
			EmptyState emptyState = EmptyStateFactory.create("emptyState", mainVC, this);
			emptyState.setIconCss("o_CourseModule_icon");
		}
		
		List<LauncherItem> items = new ArrayList<>(entries.size());
		for (CatalogRepositoryEntry entry : entries) {
			LauncherItem item = new LauncherItem(entry);
			
			VFSLeaf image = repositoryManager.getImage(entry.getKey(), entry.getOlatResource());
			if (image != null) {
				item.setThumbnailRelPath(mapperThumbnailKey.getUrl() + "/" + image.getName());
			}
			
			List<TaxonomyLevelNamePath> taxonomyLevels = TaxonomyUIFactory.getNamePaths(getTranslator(), entry.getTaxonomyLevels());
			item.setTaxonomyLevels(taxonomyLevels);
			
			String id = "o_llm_" + CodeHelper.getRAMUniqueID();
			Link learnMoreLink = LinkFactory.createLink(id, id, "launcher.learn.more", "launcher.learn.more", getTranslator(), mainVC, this, Link.LINK);
			learnMoreLink.setIconRightCSS("o_icon o_icon_start");
			learnMoreLink.setUserObject(entry.getKey());
			item.setLearnMoreLink(learnMoreLink);
			
			items.add(item);
		}
		mainVC.contextPut("items", items);
		
		if (showMore) {
			titleLink = LinkFactory.createLink("title", "title", getTranslator(), mainVC, this, Link.LINK | Link.NONTRANSLATED);
			titleLink.setCustomDisplayText(title);
			titleLink.setElementCssClass("o_link_plain");
			titleLink.setIconRightCSS("o_icon o_icon_start");
		} else {
			mainVC.contextPut("title", title);
		}
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if ("select".equals(event.getCommand())) {
			String key = ureq.getParameter("key");
			fireEvent(ureq, new OpenSearchEvent(state, Long.valueOf(key)));
		} else if (source == titleLink) {
			fireEvent(ureq, new OpenSearchEvent(state, null));
		} else if (source instanceof Link) {
			Link link = (Link)source;
			if ("launcher.learn.more".equals(link.getCommand())) {
				fireEvent(ureq, new OpenSearchEvent(state, (Long)link.getUserObject()));
			}
		}
	}
	
	public static final class LauncherItem {
		private final Long key;
		private final String externalRef;
		private final String displayName;
		private final String teaser;
		private final RepositoryEntryStatusEnum status;
		private final RepositoryEntryEducationalType educationalType;
		private String thumbnailRelPath;
		private List<TaxonomyLevelNamePath> taxonomyLevels;
		private Link learnMoreLink;
		
		public LauncherItem(CatalogRepositoryEntry entry) {
			this.key = entry.getKey();
			this.externalRef = entry.getExternalRef();
			this.displayName = entry.getDisplayname();
			this.teaser = Formatter.truncate(entry.getTeaser(), 250);
			this.status = entry.getStatus();
			educationalType = entry.getEducationalType();
		}

		public Long getKey() {
			return key;
		}

		public String getExternalRef() {
			return externalRef;
		}

		public String getDisplayName() {
			return displayName;
		}

		public String getTeaser() {
			return teaser;
		}
		
		public boolean isClosed() {
			return status.decommissioned();
		}

		public RepositoryEntryEducationalType getEducationalType() {
			return educationalType;
		}

		public String getThumbnailRelPath() {
			return thumbnailRelPath;
		}

		public void setThumbnailRelPath(String thumbnailRelPath) {
			this.thumbnailRelPath = thumbnailRelPath;
		}
		
		public boolean isThumbnailAvailable() {
			return StringHelper.containsNonWhitespace(thumbnailRelPath);
		}

		public List<TaxonomyLevelNamePath> getTaxonomyLevels() {
			return taxonomyLevels;
		}

		public void setTaxonomyLevels(List<TaxonomyLevelNamePath> taxonomyLevels) {
			this.taxonomyLevels = taxonomyLevels;
		}

		public Link getLearnMoreLink() {
			return learnMoreLink;
		}

		public void setLearnMoreLink(Link learnMoreLink) {
			this.learnMoreLink = learnMoreLink;
		}
		
		public String getLearnMoreLinkName() {
			return learnMoreLink.getComponentName();
		}
		
	}

}
