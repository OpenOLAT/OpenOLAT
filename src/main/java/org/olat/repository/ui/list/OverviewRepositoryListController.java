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
import org.olat.modules.curriculum.ui.CurriculumListController;
import org.olat.repository.CatalogEntry;
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

	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	private final Link myCourseLink;
	private Link favoriteLink;
	private Link catalogLink;
	private Link searchCourseLink;
	private Link closedCourseLink;
	private Link curriculumLink;
	
	private Controller currentCtrl;
	private RepositoryEntryListController markedCtrl;
	private BreadcrumbedStackedPanel markedStackPanel;
	private RepositoryEntryListController myCoursesCtrl;
	private BreadcrumbedStackedPanel myCoursesStackPanel;
	private CatalogNodeController catalogCtrl;
	private BreadcrumbedStackedPanel catalogStackPanel;
	private CurriculumListController curriculumListCtrl;
	private BreadcrumbedStackedPanel curriculumStackPanel;
	private RepositoryEntryListController searchCoursesCtrl;
	private RepositoryEntryListController closedCoursesCtrl;
	private BreadcrumbedStackedPanel searchCoursesStackPanel;
	
	private final boolean isGuestOnly;
	private boolean favoritDirty, myDirty;
	
	private final EventBus eventBus;
	
	@Autowired
	private CatalogManager catalogManager;
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private CurriculumModule curriculumModule;
	
	public OverviewRepositoryListController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));
		isGuestOnly = ureq.getUserSession().getRoles().isGuestOnly();

		MainPanel mainPanel = new MainPanel("myCoursesMainPanel");
		mainPanel.setDomReplaceable(false);
		mainVC = createVelocityContainer("overview");
		mainPanel.setContent(mainVC);
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		segmentView.setReselect(true);
		if(!isGuestOnly) {
			favoriteLink = LinkFactory.createLink("search.mark", mainVC, this);
			favoriteLink.setElementCssClass("o_sel_mycourses_fav");
			segmentView.addSegment(favoriteLink, false);
		}
		
		myCourseLink = LinkFactory.createLink("search.mycourses.student", mainVC, this);
		myCourseLink.setElementCssClass("o_sel_mycourses_my");
		segmentView.addSegment(myCourseLink, false);
		
		if(curriculumModule.isEnabled() && curriculumModule.isCurriculumInMyCourses()) {
			curriculumLink = LinkFactory.createLink("search.curriculums", mainVC, this);
			curriculumLink.setElementCssClass("o_sel_mycurriculums");
			segmentView.addSegment(curriculumLink, false);
		}
		
		closedCourseLink = LinkFactory.createLink("search.courses.closed", mainVC, this);
		closedCourseLink.setElementCssClass("o_sel_mycourses_closed");
		segmentView.addSegment(closedCourseLink, false);

		if(repositoryModule.isCatalogEnabled() && repositoryModule.isCatalogBrowsingEnabled()) {
			catalogLink = LinkFactory.createLink("search.catalog", mainVC, this);
			catalogLink.setElementCssClass("o_sel_mycourses_catlog");
			segmentView.addSegment(catalogLink, false);
		}
		if(repositoryModule.isMyCoursesSearchEnabled()) {
			searchCourseLink = LinkFactory.createLink("search.courses.student", mainVC, this);
			searchCourseLink.setElementCssClass("o_sel_mycourses_search");
			segmentView.addSegment(searchCourseLink, false);
		}

		eventBus = ureq.getUserSession().getSingleUserEventCenter();
		eventBus.registerFor(this, getIdentity(), RepositoryService.REPOSITORY_EVENT_ORES);
		
		putInitialPanel(mainPanel);
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			if(currentCtrl == null) {
				if(isGuestOnly) {
					doOpenMyCourses(ureq);
					segmentView.select(myCourseLink);
				} else {
					boolean markEmpty = doOpenMark(ureq).isEmpty();
					if(markEmpty) {
						doOpenMyCourses(ureq);
						segmentView.select(myCourseLink);
					} else {
						segmentView.select(favoriteLink);
					}
				}
			}
			
			if(favoritDirty && markedCtrl != null) {
				markedCtrl.reloadRows();
			}
			if(myDirty && myCoursesCtrl != null) {
				myCoursesCtrl.reloadRows();
			}
			addToHistory(ureq, this);
		} else {
			ContextEntry entry = entries.get(0);
			String segment = entry.getOLATResourceable().getResourceableTypeName();
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			if("Favorits".equalsIgnoreCase(segment)) {
				if(isGuestOnly) {
					doOpenMyCourses(ureq).activate(ureq, subEntries, entry.getTransientState());
					segmentView.select(myCourseLink);
				} else {
					doOpenMark(ureq).activate(ureq, subEntries, entry.getTransientState());
					segmentView.select(favoriteLink);
				}
			} else if("My".equalsIgnoreCase(segment)) {
				doOpenMyCourses(ureq).activate(ureq, subEntries, entry.getTransientState());
				segmentView.select(myCourseLink);
			} else if(("Catalog".equalsIgnoreCase(segment) || "CatalogEntry".equalsIgnoreCase(segment))
					&& catalogLink != null) {
				CatalogNodeController ctrl = doOpenCatalog(ureq);
				if(ctrl != null) {
					ctrl.activate(ureq, entries, entry.getTransientState());
					segmentView.select(catalogLink);
				}
			} else if("Curriculum".equalsIgnoreCase(segment)) {
				CurriculumListController ctrl = doOpenCurriculum(ureq);
				if(ctrl != null) {
					ctrl.activate(ureq, subEntries, entry.getTransientState());
					segmentView.select(curriculumLink);
				}
			} else if("Search".equalsIgnoreCase(segment) && searchCourseLink != null) {
				doOpenSearchCourses(ureq).activate(ureq, subEntries, entry.getTransientState());
				segmentView.select(searchCourseLink);
			} else if("Closed".equalsIgnoreCase(segment) && closedCourseLink != null) {
				doOpenClosedCourses(ureq).activate(ureq, subEntries, entry.getTransientState());
				segmentView.select(closedCourseLink);
			} else {
				//default if the others fail
				doOpenMyCourses(ureq).activate(ureq, subEntries, entry.getTransientState());
				segmentView.select(myCourseLink);
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
				if(markedCtrl != null && !markedCtrl.getName().equals(ece.getSource())) {
					favoritDirty = true;
				}
				if(myCoursesCtrl != null && !myCoursesCtrl.getName().equals(ece.getSource())) {
					myDirty = true;
				}
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
				if (clickedLink == favoriteLink) {
					doOpenMark(ureq);
				} else if (clickedLink == myCourseLink) {
					doOpenMyCourses(ureq);
				} else if (clickedLink == catalogLink) {
					doOpenCatalog(ureq);
				} else if (clickedLink == curriculumLink) {
					doOpenCurriculum(ureq);
				} else if(clickedLink == searchCourseLink) {
					doOpenSearchCourses(ureq);
				} else if(clickedLink == closedCourseLink) {
					doOpenClosedCourses(ureq);
				}
			}
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(catalogCtrl);
		removeAsListenerAndDispose(markedCtrl);
		removeAsListenerAndDispose(myCoursesCtrl);
		removeAsListenerAndDispose(searchCoursesCtrl);
		catalogCtrl = null;
		markedCtrl = null;
		myCoursesCtrl = null;
		searchCoursesCtrl = null;
	}
	
	private RepositoryEntryListController doOpenMark(UserRequest ureq) {
		cleanUp();
		
		SearchMyRepositoryEntryViewParams searchParams
			= new SearchMyRepositoryEntryViewParams(getIdentity(), ureq.getUserSession().getRoles());
		searchParams.setMarked(Boolean.TRUE);

		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Favorits", 0l);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
		markedStackPanel = new BreadcrumbedStackedPanel("mrkstack", getTranslator(), this);
		markedCtrl = new RepositoryEntryListController(ureq, bwControl, searchParams, true, false, "marked", markedStackPanel);
		markedStackPanel.pushController(translate("search.mark"), markedCtrl);
		listenTo(markedCtrl);
		currentCtrl = markedCtrl;
		favoritDirty = false;

		addToHistory(ureq, markedCtrl);
		mainVC.put("segmentCmp", markedStackPanel);
		return markedCtrl;
	}
	
	private RepositoryEntryListController doOpenMyCourses(UserRequest ureq) {
		cleanUp();
	
		SearchMyRepositoryEntryViewParams searchParams
			= new SearchMyRepositoryEntryViewParams(getIdentity(), ureq.getUserSession().getRoles());
		searchParams.setMembershipMandatory(true);
		searchParams.setClosed(Boolean.FALSE);

		OLATResourceable ores = OresHelper.createOLATResourceableInstance("My", 0l);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
		myCoursesStackPanel = new BreadcrumbedStackedPanel("mystack", getTranslator(), this);
		myCoursesCtrl = new RepositoryEntryListController(ureq, bwControl, searchParams, true, false, "my", myCoursesStackPanel);
		myCoursesStackPanel.pushController(translate("search.mycourses.student"), myCoursesCtrl);
		listenTo(myCoursesCtrl);
		currentCtrl = myCoursesCtrl;
		myDirty = false;

		addToHistory(ureq, myCoursesCtrl);
		mainVC.put("segmentCmp", myCoursesStackPanel);
		return myCoursesCtrl;
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
		catalogStackPanel = new BreadcrumbedStackedPanel("catstack", getTranslator(), this);
		catalogCtrl = new CatalogNodeController(ureq, bwControl, getWindowControl(), rootEntry, catalogStackPanel, false);
		catalogStackPanel.pushController(translate("search.catalog"), catalogCtrl);
		listenTo(catalogCtrl);
		currentCtrl = catalogCtrl;

		addToHistory(ureq, catalogCtrl);
		mainVC.put("segmentCmp", catalogStackPanel);
		return catalogCtrl;
	}
	
	private CurriculumListController doOpenCurriculum(UserRequest ureq) {
		if(!curriculumModule.isEnabled() || !curriculumModule.isCurriculumInMyCourses()) {
			return null;
		}
		cleanUp();
		

		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Curriculum", 0l);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
		curriculumStackPanel = new BreadcrumbedStackedPanel("curriculumstack", getTranslator(), this);
		curriculumListCtrl = new CurriculumListController(ureq, bwControl, curriculumStackPanel);
		curriculumStackPanel.pushController(translate("search.curriculums"), curriculumListCtrl);
		listenTo(curriculumListCtrl);
		currentCtrl = curriculumListCtrl;
		
		addToHistory(ureq, curriculumListCtrl);
		mainVC.put("segmentCmp", curriculumStackPanel);
		curriculumListCtrl.activate(ureq, null, null);
		return curriculumListCtrl;
	}
	
	
	private RepositoryEntryListController doOpenSearchCourses(UserRequest ureq) {
		cleanUp();

		SearchMyRepositoryEntryViewParams searchParams
			= new SearchMyRepositoryEntryViewParams(getIdentity(), ureq.getUserSession().getRoles());
		searchParams.setMembershipMandatory(false);

		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Search", 0l);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
		searchCoursesStackPanel = new BreadcrumbedStackedPanel("search", getTranslator(), this);
		searchCoursesCtrl = new RepositoryEntryListController(ureq, bwControl, searchParams, false, true, "my-search", searchCoursesStackPanel);
		searchCoursesStackPanel.pushController(translate("search.mycourses.student"), searchCoursesCtrl);
		listenTo(searchCoursesCtrl);
		currentCtrl = searchCoursesCtrl;
		
		addToHistory(ureq, searchCoursesCtrl);
		mainVC.put("segmentCmp", searchCoursesStackPanel);
		return searchCoursesCtrl;
	}
	
	private RepositoryEntryListController doOpenClosedCourses(UserRequest ureq) {
		cleanUp();
		
		SearchMyRepositoryEntryViewParams searchParams
			= new SearchMyRepositoryEntryViewParams(getIdentity(), ureq.getUserSession().getRoles());
		searchParams.setMembershipMandatory(true);
		searchParams.setClosed(Boolean.TRUE);

		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Closed", 0l);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
		myCoursesStackPanel = new BreadcrumbedStackedPanel("mystack", getTranslator(), this);
		closedCoursesCtrl = new RepositoryEntryListController(ureq, bwControl, searchParams, true, false, "closed", myCoursesStackPanel);
		myCoursesStackPanel.pushController(translate("search.mycourses.student"), closedCoursesCtrl);
		listenTo(closedCoursesCtrl);
		currentCtrl = closedCoursesCtrl;
		myDirty = false;

		addToHistory(ureq, closedCoursesCtrl);
		mainVC.put("segmentCmp", myCoursesStackPanel);
		return closedCoursesCtrl;
	}
}
