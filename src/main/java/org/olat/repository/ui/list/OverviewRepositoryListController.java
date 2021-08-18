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
package org.olat.repository.ui.list;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.MainPanel;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Util;
import org.olat.core.util.event.EventBus;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.ui.CurriculumListController;
import org.olat.repository.CatalogEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.controllers.EntryChangedEvent.Change;
import org.olat.repository.manager.CatalogManager;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams;
import org.olat.repository.ui.catalog.CatalogNodeController;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28.01.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OverviewRepositoryListController extends BasicController implements Activateable2, GenericEventListener {

	private static final String OVERVIEW_PATH = "[MyCoursesSite:0]";
	
	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	private final Link myCourseLink;
	private Link catalogLink;
	private Link curriculumLink;
	
	private Controller currentCtrl;
	private RepositoryEntryListController entriesCtrl;
	private CatalogNodeController catalogCtrl;
	private CurriculumListController curriculumListCtrl;
	
	private boolean entriesDirty;
	private final boolean guestOnly;
	private final boolean withCurriculums;
	
	private final EventBus eventBus;
	
	@Autowired
	private CatalogManager catalogManager;
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private CurriculumModule curriculumModule;
	@Autowired
	private CurriculumService curriculumService;
	
	public OverviewRepositoryListController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));
		
		guestOnly = ureq.getUserSession().getRoles().isGuestOnly();

		MainPanel mainPanel = new MainPanel("myCoursesMainPanel");
		mainPanel.setDomReplaceable(false);
		mainPanel.setCssClass("o_sel_my_repository_entries");

		mainVC = createVelocityContainer("overview");
		mainPanel.setContent(mainVC);
		
		withCurriculums = withCurriculumTab();
		boolean withCatalog = repositoryModule.isCatalogEnabled() && repositoryModule.isCatalogBrowsingEnabled();
		mainVC.contextPut("withSegments", Boolean.valueOf(withCurriculums || withCatalog));
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		segmentView.setReselect(true);

		myCourseLink = LinkFactory.createLink("search.mycourses.student", mainVC, this);
		myCourseLink.setUrl(BusinessControlFactory.getInstance()
				.getAuthenticatedURLFromBusinessPathStrings(OVERVIEW_PATH, "[My:0]"));
		myCourseLink.setElementCssClass("o_sel_mycourses_my");
		segmentView.addSegment(myCourseLink, false);
		
		if(withCurriculums) {
			curriculumLink = LinkFactory.createLink("search.curriculums", mainVC, this);
			curriculumLink.setUrl(BusinessControlFactory.getInstance()
					.getAuthenticatedURLFromBusinessPathStrings(OVERVIEW_PATH, "[Curriculum:0]"));
			curriculumLink.setElementCssClass("o_sel_mycurriculums");
			segmentView.addSegment(curriculumLink, false);
		}

		if(repositoryModule.isCatalogEnabled() && repositoryModule.isCatalogBrowsingEnabled()) {
			catalogLink = LinkFactory.createLink("search.catalog", mainVC, this);
			catalogLink.setUrl(BusinessControlFactory.getInstance()
					.getAuthenticatedURLFromBusinessPathStrings(OVERVIEW_PATH, "[Catalog:0]"));
			catalogLink.setElementCssClass("o_sel_mycourses_catalog");
			segmentView.addSegment(catalogLink, false);
		}

		eventBus = ureq.getUserSession().getSingleUserEventCenter();
		eventBus.registerFor(this, getIdentity(), RepositoryService.REPOSITORY_EVENT_ORES);
		
		putInitialPanel(mainPanel);
	}
	
	private boolean withCurriculumTab() {
		return curriculumModule.isEnabled() && curriculumModule.isCurriculumInMyCourses()
				&& curriculumService.hasCurriculums(getIdentity());
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			if(currentCtrl == null) {
				activateMyEntries(ureq);
			} else if(entriesDirty && entriesCtrl != null) {
				entriesCtrl.reloadRows();
			}
			addToHistory(ureq, this);
		} else {
			ContextEntry entry = entries.get(0);
			String segment = entry.getOLATResourceable().getResourceableTypeName();
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			if("My".equalsIgnoreCase(segment)) {
				activateMyEntries(ureq);
			} else if(("Catalog".equalsIgnoreCase(segment) || "CatalogEntry".equalsIgnoreCase(segment))
					&& catalogLink != null) {
				CatalogNodeController ctrl = doOpenCatalog(ureq);
				if(ctrl != null) {
					ctrl.activate(ureq, entries, entry.getTransientState());
					segmentView.select(catalogLink);
				} else if(currentCtrl == null) {
					activateMyEntries(ureq);
				}
			} else if("Curriculum".equalsIgnoreCase(segment)) {
				CurriculumListController ctrl = doOpenCurriculum(ureq);
				if(ctrl != null) {
					ctrl.activate(ureq, subEntries, entry.getTransientState());
					segmentView.select(curriculumLink);
				} else if(currentCtrl == null) {
					activateMyEntries(ureq);
				}
			} else {
				activateMyEntries(ureq);
			}
		}
	}
	
	private void activateMyEntries(UserRequest ureq) {
		RepositoryEntryListController listCtrl = doOpenEntries(ureq);
		segmentView.select(myCourseLink);

		if(guestOnly) {
			listCtrl.selectFilterTab(listCtrl.getMyEntriesPreset());
		} else {
			listCtrl.selectFilterTab(listCtrl.getBookmarkPreset());
			if(listCtrl.isEmpty()) {
				listCtrl.selectFilterTab(listCtrl.getMyEntriesPreset());
			}
		}
	}

	@Override
	protected void doDispose() {
		eventBus.deregisterFor(this, RepositoryService.REPOSITORY_EVENT_ORES);
	}
	
	@Override
	public void event(Event event) {
		if(EntryChangedEvent.CHANGE_CMD.equals(event.getCommand()) && event instanceof EntryChangedEvent) {
			EntryChangedEvent ece = (EntryChangedEvent)event;
			if(ece.getChange() == Change.addBookmark || ece.getChange() == Change.removeBookmark
					|| ece.getChange() == Change.added || ece.getChange() == Change.deleted) {
				entriesDirty = true;
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				cleanUp();
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == myCourseLink) {
					doOpenEntries(ureq);
				} else if (clickedLink == catalogLink) {
					doOpenCatalog(ureq);
				} else if (clickedLink == curriculumLink) {
					doOpenCurriculum(ureq);
				}
			}
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(catalogCtrl);
		removeAsListenerAndDispose(entriesCtrl);
		catalogCtrl = null;
		entriesCtrl = null;
	}
	
	private RepositoryEntryListController doOpenEntries(UserRequest ureq) {
		cleanUp();
	
		SearchMyRepositoryEntryViewParams searchParams
			= new SearchMyRepositoryEntryViewParams(getIdentity(), ureq.getUserSession().getRoles());
		searchParams.setMembershipMandatory(true);
		searchParams.setEntryStatus(RepositoryEntryStatusEnum.preparationToPublished());

		OLATResourceable ores = OresHelper.createOLATResourceableInstance("My", 0l);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
		BreadcrumbedStackedPanel entriesStackPanel = new BreadcrumbedStackedPanel("mystack", getTranslator(), this);
		entriesCtrl = new RepositoryEntryListController(ureq, bwControl, searchParams, false, true, true, true, "my", entriesStackPanel);
		entriesStackPanel.pushController(translate("search.mycourses.student"), entriesCtrl);
		listenTo(entriesCtrl);
		currentCtrl = entriesCtrl;
		entriesDirty = false;

		addToHistory(ureq, entriesCtrl);
		mainVC.put("segmentCmp", entriesStackPanel);
		return entriesCtrl;
	}
	
	private CatalogNodeController doOpenCatalog(UserRequest ureq) {
		if(!repositoryModule.isCatalogEnabled() || !repositoryModule.isCatalogBrowsingEnabled()) {
			return null;
		}
		cleanUp();

		List<CatalogEntry> entries = catalogManager.getRootCatalogEntries();
		CatalogEntry rootEntry = null;
		if(!entries.isEmpty()) {
			rootEntry = entries.get(0);
		}
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Catalog", 0l);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
		BreadcrumbedStackedPanel catalogStackPanel = new BreadcrumbedStackedPanel("catstack", getTranslator(), this);
		catalogCtrl = new CatalogNodeController(ureq, bwControl, getWindowControl(), rootEntry, catalogStackPanel, false);
		catalogStackPanel.pushController(translate("search.catalog"), catalogCtrl);
		listenTo(catalogCtrl);
		currentCtrl = catalogCtrl;

		addToHistory(ureq, catalogCtrl);
		mainVC.put("segmentCmp", catalogStackPanel);
		return catalogCtrl;
	}
	
	private CurriculumListController doOpenCurriculum(UserRequest ureq) {
		if(!withCurriculums) {
			return null;
		}
		cleanUp();
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Curriculum", 0l);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
		BreadcrumbedStackedPanel curriculumStackPanel = new BreadcrumbedStackedPanel("curriculumstack", getTranslator(), this);
		curriculumListCtrl = new CurriculumListController(ureq, bwControl, curriculumStackPanel);
		curriculumStackPanel.pushController(translate("search.curriculums"), curriculumListCtrl);
		listenTo(curriculumListCtrl);
		currentCtrl = curriculumListCtrl;
		
		addToHistory(ureq, curriculumListCtrl);
		mainVC.put("segmentCmp", curriculumStackPanel);
		curriculumListCtrl.activate(ureq, null, null);
		return curriculumListCtrl;
	}
}
