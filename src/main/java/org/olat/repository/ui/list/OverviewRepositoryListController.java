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

import org.olat.catalog.CatalogEntry;
import org.olat.catalog.CatalogManager;
import org.olat.core.CoreSpringFactory;
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
import org.olat.core.util.resource.OresHelper;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;
import org.olat.repository.SearchMyRepositoryEntryViewParams;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * 
 * Initial date: 28.01.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OverviewRepositoryListController extends BasicController implements Activateable2 {

	private MainPanel mainPanel;
	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	private final Link favoriteLink, myCourseLink;
	private Link catalogLink;
	
	private RepositoryEntryListController markedCtrl;
	private BreadcrumbedStackedPanel markedStackPanel;
	private RepositoryEntryListController myCoursesCtrl;
	private BreadcrumbedStackedPanel myCoursesStackPanel;
	private CatalogNodeController catalogCtrl;
	private BreadcrumbedStackedPanel catalogStackPanel;
	
	private final CatalogManager catalogManager;
	private final RepositoryModule repositoryModule;
	
	public OverviewRepositoryListController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));
		
		catalogManager = CoreSpringFactory.getImpl(CatalogManager.class);
		repositoryModule = CoreSpringFactory.getImpl(RepositoryModule.class);

		mainPanel = new MainPanel("myCoursesMainPanel");
		mainVC = createVelocityContainer("overview");
		mainPanel.setContent(mainVC);
		
		boolean markEmpty = doOpenMark(ureq).isEmpty();
		if(markEmpty) {
			doOpenMyCourses(ureq);
		}
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		favoriteLink = LinkFactory.createLink("search.mark", mainVC, this);
		segmentView.addSegment(favoriteLink, !markEmpty);
		myCourseLink = LinkFactory.createLink("search.mycourses.student", mainVC, this);
		segmentView.addSegment(myCourseLink, markEmpty);
		
		if(repositoryModule.isCatalogEnabled() && repositoryModule.isCatalogBrowsingEnabled()) {
			catalogLink = LinkFactory.createLink("search.catalog", mainVC, this);
			segmentView.addSegment(catalogLink, false);
		}
		
		putInitialPanel(mainPanel);
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		ContextEntry entry = entries.get(0);
		String segment = entry.getOLATResourceable().getResourceableTypeName();
		List<ContextEntry> subEntries = entries.subList(1, entries.size());
		if("Favorits".equals(segment)) {
			doOpenMark(ureq).activate(ureq, subEntries, entry.getTransientState());
			segmentView.select(favoriteLink);
		} else if("My".equals(segment)) {
			doOpenMyCourses(ureq).activate(ureq, subEntries, entry.getTransientState());
			segmentView.select(myCourseLink);
		} else if("Catalog".equals(segment)) {
			CatalogNodeController ctrl = doOpenCatalog(ureq);
			if(ctrl != null) {
				ctrl.activate(ureq, subEntries, entry.getTransientState());
				segmentView.select(catalogLink);
			}
		}
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == favoriteLink) {
					doOpenMark(ureq);
				} else if (clickedLink == myCourseLink) {
					doOpenMyCourses(ureq);
				} else if (clickedLink == catalogLink) {
					doOpenCatalog(ureq);
				}
			}
		}
	}
	
	private RepositoryEntryListController doOpenMark(UserRequest ureq) {
		if(markedCtrl == null) {
			SearchMyRepositoryEntryViewParams searchParams
				= new SearchMyRepositoryEntryViewParams(getIdentity(), ureq.getUserSession().getRoles(), "CourseModule");
			searchParams.setMarked(Boolean.TRUE);

			OLATResourceable ores = OresHelper.createOLATResourceableInstance("Favorits", 0l);
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			markedStackPanel = new BreadcrumbedStackedPanel("mrkstack", getTranslator(), this);
			markedCtrl = new RepositoryEntryListController(ureq, bwControl, searchParams, markedStackPanel);
			markedStackPanel.pushController(translate("search.mark"), markedCtrl);
			listenTo(markedCtrl);
		}

		addToHistory(ureq, markedCtrl);
		mainVC.put("segmentCmp", markedStackPanel);
		return markedCtrl;
	}
	
	private RepositoryEntryListController doOpenMyCourses(UserRequest ureq) {
		if(myCoursesCtrl == null) {
			SearchMyRepositoryEntryViewParams searchParams
				= new SearchMyRepositoryEntryViewParams(getIdentity(), ureq.getUserSession().getRoles(), "CourseModule");
			searchParams.setMembershipMandatory(true);

			OLATResourceable ores = OresHelper.createOLATResourceableInstance("My", 0l);
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			myCoursesStackPanel = new BreadcrumbedStackedPanel("mystack", getTranslator(), this);
			myCoursesCtrl = new RepositoryEntryListController(ureq, bwControl, searchParams, myCoursesStackPanel);
			myCoursesStackPanel.pushController(translate("search.mycourses.student"), myCoursesCtrl);
			listenTo(myCoursesCtrl);
		}
		
		addToHistory(ureq, myCoursesCtrl);
		mainVC.put("segmentCmp", myCoursesStackPanel);
		return myCoursesCtrl;
	}
	
	private CatalogNodeController doOpenCatalog(UserRequest ureq) {
		if(!repositoryModule.isCatalogEnabled() || !repositoryModule.isCatalogBrowsingEnabled()) return null;
		
		if(catalogCtrl == null) {
			List<CatalogEntry> entries = catalogManager.getRootCatalogEntries();
			CatalogEntry rootEntry = null;
			if(entries.size() > 0) {
				rootEntry = entries.get(0);
			}
			
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("Catalog", 0l);
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			catalogStackPanel = new BreadcrumbedStackedPanel("catstack", getTranslator(), this);
			catalogCtrl = new CatalogNodeController(ureq, bwControl, rootEntry, catalogStackPanel, false);
			catalogStackPanel.pushController(translate("search.catalog"), catalogCtrl);
			listenTo(catalogCtrl);
		}

		addToHistory(ureq, catalogCtrl);
		mainVC.put("segmentCmp", catalogStackPanel);
		return catalogCtrl;
	}
}
