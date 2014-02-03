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
import org.olat.core.gui.components.stack.StackedController;
import org.olat.core.gui.components.stack.StackedControllerAware;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryManager;

/**
 * 
 * Initial date: 28.01.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OverviewRepositoryListController extends BasicController implements StackedControllerAware, Activateable2 {

	private MainPanel mainPanel;
	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	private final Link favoriteLink, myCourseLink, catalogLink;
	
	private FavoritRepositoryEntryListController markedCtrl;
	private RepositoryEntryListController myCoursesCtrl;
	private CatalogNodeController catalogCtrl;
	private StackedController stackPanel;
	
	private CatalogManager catalogManager;
	
	public OverviewRepositoryListController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));
		
		catalogManager = CoreSpringFactory.getImpl(CatalogManager.class);

		mainPanel = new MainPanel("myCoursesMainPanel");
		mainVC = createVelocityContainer("overview");
		mainPanel.setContent(mainVC);
		
		boolean hasMarkedEntries = doOpenMark(ureq);
		if(!hasMarkedEntries) {
			doOpenMyCourses(ureq);
		}
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		favoriteLink = LinkFactory.createLink("search.mark", mainVC, this);
		segmentView.addSegment(favoriteLink, hasMarkedEntries);
		myCourseLink = LinkFactory.createLink("search.mycourses.student", mainVC, this);
		segmentView.addSegment(myCourseLink, !hasMarkedEntries);
		catalogLink = LinkFactory.createLink("search.catalog", mainVC, this);
		segmentView.addSegment(catalogLink, false);
		
		putInitialPanel(mainPanel);
	}
	
	@Override
	public void setStackedController(StackedController stackPanel) {
		this.stackPanel = stackPanel;
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
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
	
	private boolean doOpenMark(UserRequest ureq) {
		boolean hasMarkedEntries = true;
		if(markedCtrl == null) {
			markedCtrl = new FavoritRepositoryEntryListController(ureq, getWindowControl());
			hasMarkedEntries = markedCtrl.updateMarkedEntries();
			listenTo(markedCtrl);
		}
		mainVC.put("segmentCmp", markedCtrl.getInitialComponent());
		return hasMarkedEntries;
	}
	
	private void doOpenMyCourses(UserRequest ureq) {
		if(myCoursesCtrl == null) {
			myCoursesCtrl = new RepositoryEntryListController(ureq, getWindowControl());
			listenTo(myCoursesCtrl);
		}
		mainVC.put("segmentCmp", myCoursesCtrl.getInitialComponent());
	}
	
	private void doOpenCatalog(UserRequest ureq) {
		if(catalogCtrl == null) {
			List<CatalogEntry> entries = catalogManager.getRootCatalogEntries();
			CatalogEntry rootEntry = null;
			if(entries.size() > 0) {
				rootEntry = entries.get(0);
			}
			catalogCtrl = new CatalogNodeController(ureq, getWindowControl(), rootEntry);
			catalogCtrl.setStackedController(stackPanel);
			listenTo(catalogCtrl);
		}
		mainVC.put("segmentCmp", catalogCtrl.getInitialComponent());
	}
}
