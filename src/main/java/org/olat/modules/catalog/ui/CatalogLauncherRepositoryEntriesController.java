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
import java.util.Optional;

import org.olat.NewControllerFactory;
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
import org.olat.course.CorruptedCourseException;
import org.olat.modules.catalog.CatalogRepositoryEntry;
import org.olat.modules.catalog.CatalogV2Module;
import org.olat.modules.catalog.CatalogV2Module.CatalogCardView;
import org.olat.modules.taxonomy.model.TaxonomyLevelNamePath;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.repository.ui.RepositoryEntryImageMapper;
import org.olat.repository.ui.RepositoyUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogLauncherRepositoryEntriesController extends BasicController implements Controller {
	
	public static final int PREFERRED_NUMBER_CARDS = 15;
	private static final String[] SWIPER_JS = new String[] { "js/swiper/swiper-bundle.min.js" };

	private final VelocityContainer mainVC;
	private Link titleLink;
	private Link showAllLink;
	
	private final List<CatalogRepositoryEntry> entries;
	private final CatalogRepositoryEntryState state;
	private final MapperKey mapperThumbnailKey;

	@Autowired
	private CatalogV2Module catalogModule;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private MapperService mapperService;

	public CatalogLauncherRepositoryEntriesController(UserRequest ureq, WindowControl wControl,
			List<CatalogRepositoryEntry> entries, String title, boolean showMore, CatalogRepositoryEntryState state) {
		super(ureq, wControl);
		this.entries = entries;
		this.state = state;
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		// Two times the with of the card in mobile view.
		this.mapperThumbnailKey = mapperService.register(null, "repositoryentryImage", new RepositoryEntryImageMapper(900, 600));
		
		mainVC = createVelocityContainer("launch_repository_entry");
		
		JSAndCSSComponent js = new JSAndCSSComponent("js", SWIPER_JS, null);
		mainVC.put("js", js);
		
		
		if (entries.isEmpty()) {
			EmptyState emptyState = EmptyStateFactory.create("emptyState", mainVC, this);
			emptyState.setIconCss("o_CourseModule_icon");
		}
		
		List<LauncherItem> items = new ArrayList<>(entries.size());
		for (CatalogRepositoryEntry entry : entries) {
			LauncherItem item = new LauncherItem();
			
			appendRepositoryEntryData(item, entry);
			
			VFSLeaf image = repositoryManager.getImage(entry.getKey(), entry.getOlatResource());
			if (image != null) {
				item.setThumbnailRelPath(RepositoryEntryImageMapper.getImageUrl(mapperThumbnailKey.getUrl() , image));
			}
			
			String id = "o_dml_" + CodeHelper.getRAMUniqueID();
			Link displayNameLink = LinkFactory.createLink(id, id, "open", null, getTranslator(), mainVC, this, Link.LINK + Link.NONTRANSLATED);
			displayNameLink.setCustomDisplayText(entry.getDisplayname());
			displayNameLink.setUserObject(entry.getKey());
			item.setDisplayNameLink(displayNameLink);
			
			items.add(item);
		}
		mainVC.contextPut("items", items);
		
		mainVC.contextPut("launcherId", CodeHelper.getRAMUniqueID());
		if (showMore) {
			titleLink = LinkFactory.createLink("title", "title", getTranslator(), mainVC, this, Link.LINK | Link.NONTRANSLATED);
			titleLink.setCustomDisplayText(title);
			titleLink.setElementCssClass("o_link_plain");
			
			showAllLink = LinkFactory.createLink("show.all", mainVC, this);
			showAllLink.setIconRightCSS("o_icon o_icon_start");
		} else {
			mainVC.contextPut("title", title);
		}
		
		putInitialPanel(mainVC);
	}

	private void appendRepositoryEntryData(LauncherItem item, CatalogRepositoryEntry entry) {
		item.setKey(entry.getKey());
		item.setEducationalType(entry.getEducationalType());
		if (catalogModule.getCardView().contains(CatalogCardView.externalRef)) {
			item.setExternalRef(entry.getExternalRef());
		}
		if (catalogModule.getCardView().contains(CatalogCardView.teaserText)) {
			item.setTeaser(entry.getTeaser());
		}
		if (catalogModule.getCardView().contains(CatalogCardView.taxonomyLevels)) {
			List<TaxonomyLevelNamePath> taxonomyLevels = TaxonomyUIFactory.getNamePaths(getTranslator(), entry.getTaxonomyLevels());
			item.setTaxonomyLevels(taxonomyLevels);
		}
		if (catalogModule.getCardView().contains(CatalogCardView.educationalType)) {
			if (entry.getEducationalType() != null) {
				String educationalTypeName = translate(RepositoyUIFactory.getI18nKey(entry.getEducationalType()));
				item.setEducationalTypeName(educationalTypeName);
			}
		}
		if (catalogModule.getCardView().contains(CatalogCardView.mainLanguage)) {
			item.setLanguage(entry.getMainLanguage());
		}
		if (catalogModule.getCardView().contains(CatalogCardView.location)) {
			item.setLocation(entry.getLocation());
		}
		if (catalogModule.getCardView().contains(CatalogCardView.executionPeriod)) {
			RepositoryEntryLifecycle lifecycle = entry.getLifecycle();
			if (lifecycle != null) {
				String executionPeriod = null;
				if(lifecycle.isPrivateCycle()) {
					if (lifecycle.getValidFrom() != null) {
						executionPeriod = Formatter.getInstance(getLocale()).formatDate(lifecycle.getValidFrom());
					}
					if (lifecycle.getValidTo() != null) {
						if (StringHelper.containsNonWhitespace(executionPeriod)) {
							executionPeriod += " - ";
						}
						executionPeriod += Formatter.getInstance(getLocale()).formatDate(lifecycle.getValidTo());
					}
				} else if (StringHelper.containsNonWhitespace(lifecycle.getSoftKey())) {
					executionPeriod = lifecycle.getSoftKey();
				} else if (StringHelper.containsNonWhitespace(lifecycle.getLabel())) {
					executionPeriod = lifecycle.getLabel();
				}
				item.setExecutionPeriod(executionPeriod);
			}
		}
		if (catalogModule.getCardView().contains(CatalogCardView.authors)) {
			item.setAuthors(entry.getAuthors());
		}
		if (catalogModule.getCardView().contains(CatalogCardView.expenditureOfWork)) {
			item.setExpenditureOfWork(entry.getExpenditureOfWork());
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if ("select".equals(event.getCommand())) {
			String key = ureq.getParameter("key");
			Long repositoryEntryKey = Long.valueOf(key);
			launchOrOpen(ureq, repositoryEntryKey);
		} else if (source == titleLink) {
			fireEvent(ureq, new OpenSearchEvent(state, null));
		} else if (source == showAllLink) {
			fireEvent(ureq, new OpenSearchEvent(state, null));
		} else if (source instanceof Link) {
			Link link = (Link)source;
			if ("open".equals(link.getCommand())) {
				Long repositoryEntryKey = (Long)link.getUserObject();
				launchOrOpen(ureq, repositoryEntryKey);
			}
		}
	}
	
	private void launchOrOpen(UserRequest ureq, Long repositoryEntryKey) {
		Optional<CatalogRepositoryEntry> found = entries.stream().filter(re -> re.getKey().equals(repositoryEntryKey)).findFirst();
		if (found.isPresent() && found.get().isMember()) {
			boolean started = doStart(ureq, repositoryEntryKey);
			if (started) {
				return ;
			}
		}
		fireEvent(ureq, new OpenSearchEvent(state, repositoryEntryKey));
	}
	
	private boolean doStart(UserRequest ureq, Long repositoryEntryKey) {
		try {
			String businessPath = "[RepositoryEntry:" + repositoryEntryKey + "]";
			NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
			return true;
		} catch (CorruptedCourseException e) {
			return false;
		}
	}
	
	public static final class LauncherItem {
		private Long key;
		private String externalRef;
		private String teaser;
		private String language;
		private String location;
		private String executionPeriod;
		private String authors;
		private String expenditureOfWork;
		private RepositoryEntryStatusEnum status;
		private RepositoryEntryEducationalType educationalType;
		private String educationalTypeName;
		private String thumbnailRelPath;
		private List<TaxonomyLevelNamePath> taxonomyLevels;
		private Link displayNameLink;
		
		public Long getKey() {
			return key;
		}

		public void setKey(Long key) {
			this.key = key;
		}

		public String getExternalRef() {
			return externalRef;
		}

		public void setExternalRef(String externalRef) {
			this.externalRef = externalRef;
		}

		public String getTeaser() {
			return teaser;
		}

		public void setTeaser(String teaser) {
			this.teaser = teaser;
		}

		public String getLanguage() {
			return language;
		}

		public void setLanguage(String language) {
			this.language = language;
		}

		public String getLocation() {
			return location;
		}

		public void setLocation(String location) {
			this.location = location;
		}

		public String getExecutionPeriod() {
			return executionPeriod;
		}

		public void setExecutionPeriod(String executionPeriod) {
			this.executionPeriod = executionPeriod;
		}

		public String getAuthors() {
			return authors;
		}

		public void setAuthors(String authors) {
			this.authors = authors;
		}

		public String getExpenditureOfWork() {
			return expenditureOfWork;
		}

		public void setExpenditureOfWork(String expenditureOfWork) {
			this.expenditureOfWork = expenditureOfWork;
		}

		public RepositoryEntryStatusEnum getStatus() {
			return status;
		}

		public void setStatus(RepositoryEntryStatusEnum status) {
			this.status = status;
		}

		public boolean isClosed() {
			return status.decommissioned();
		}

		public RepositoryEntryEducationalType getEducationalType() {
			return educationalType;
		}

		public void setEducationalType(RepositoryEntryEducationalType educationalType) {
			this.educationalType = educationalType;
		}

		public String getEducationalTypeName() {
			return educationalTypeName;
		}

		public void setEducationalTypeName(String educationalTypeName) {
			this.educationalTypeName = educationalTypeName;
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

		public Link getDisplayNameLink() {
			return displayNameLink;
		}

		public void setDisplayNameLink(Link displayNameLink) {
			this.displayNameLink = displayNameLink;
		}
		
		public String getDisplayNameLinkName() {
			return displayNameLink.getComponentName();
		}
		
	}

}
