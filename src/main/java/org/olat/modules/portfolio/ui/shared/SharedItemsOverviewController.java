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
package org.olat.modules.portfolio.ui.shared;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.portfolio.PageStatus;
import org.olat.modules.portfolio.PageUserStatus;
import org.olat.modules.portfolio.model.SearchSharePagesParameters;
import org.olat.modules.portfolio.ui.PortfolioHomeController;

/**
 * 
 * Initial date: 11 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SharedItemsOverviewController extends BasicController implements Activateable2 {

	private final VelocityContainer mainVC;
	private final Link favoriteLink, bindersLink, pagesLink;
	private final SegmentViewComponent segmentView;
	private final TooledStackedPanel stackPanel;
	
	private SharedPagesController pagesCtrl;
	private SharedBindersController bindersCtrl;
	private SharedPagesController bookmarkedPagesCtrl;
	
	public SharedItemsOverviewController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(PortfolioHomeController.class, getLocale(), getTranslator()));
		this.stackPanel = stackPanel;
		
		mainVC = createVelocityContainer("shared_overview");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		segmentView.setReselect(true);
		
		favoriteLink = LinkFactory.createLink("shared.bookmarks", mainVC, this);
		favoriteLink.setElementCssClass("o_sel_shared_favorites_seg");
		segmentView.addSegment(favoriteLink, false);
		pagesLink = LinkFactory.createLink("shared.entries", mainVC, this);
		pagesLink.setElementCssClass("o_sel_shared_pages_seg");
		segmentView.addSegment(pagesLink, false);
		bindersLink = LinkFactory.createLink("shared.binders", mainVC, this);
		bindersLink.setElementCssClass("o_sel_shared_binders_seg");
		segmentView.addSegment(bindersLink, false);
		
		putInitialPanel(mainVC);
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			activateCold(ureq);
		} else {
			String name = entries.get(0).getOLATResourceable().getResourceableTypeName();
			if("Favorits".equalsIgnoreCase(name)) {
				List<ContextEntry> subEntries = entries.subList(1, entries.size());
				doOpenMark(ureq).activate(ureq, subEntries, entries.get(0).getTransientState());
				segmentView.select(favoriteLink);
			} else if("Pages".equalsIgnoreCase(name)) {
				List<ContextEntry> subEntries = entries.subList(1, entries.size());
				doOpenPages(ureq).activate(ureq, subEntries, entries.get(0).getTransientState());
				segmentView.select(pagesLink);
			} else if("Binders".equalsIgnoreCase(name)) {
				List<ContextEntry> subEntries = entries.subList(1, entries.size());
				doOpenBinders(ureq).activate(ureq, subEntries, entries.get(0).getTransientState());
				segmentView.select(bindersLink);
			} else if("Binder".equalsIgnoreCase(name)) {
				doOpenBinders(ureq).activate(ureq, entries, state);
				segmentView.select(bindersLink);
			} else {
				activateCold(ureq);
			}
		}
	}
	
	private void activateCold(UserRequest ureq) {
		doOpenMark(ureq);
		if(bookmarkedPagesCtrl.getRowCount() == 0) {
			doOpenPages(ureq);
			if(pagesCtrl.getRowCount() == 0) {
				doOpenBinders(ureq);
				segmentView.select(bindersLink);
			} else {
				segmentView.select(pagesLink);
			}
		} else {
			segmentView.select(favoriteLink);
		}
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
				} else if (clickedLink == pagesLink) {
					doOpenPages(ureq);
				} else if (clickedLink == bindersLink) {
					doOpenBinders(ureq);
				}
			}
		}
	}
	
	private SharedPagesController doOpenMark(UserRequest ureq) {
		if(bookmarkedPagesCtrl == null) {
			SearchSharePagesParameters searchParams = new SearchSharePagesParameters();
			searchParams.setBookmarkOnly(true);
			searchParams.addExcludedPageStatus(PageStatus.deleted);
			List<PageStatus> filters = new ArrayList<>(5);
			filters.add(PageStatus.draft);
			filters.add(PageStatus.inRevision);
			filters.add(PageStatus.published);
			filters.add(PageStatus.closed);

			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance("Favorits", 0l), null);
			bookmarkedPagesCtrl = new SharedPagesController(ureq, swControl, stackPanel,
					searchParams, filters, null);
			listenTo(bookmarkedPagesCtrl);
		} else {
			bookmarkedPagesCtrl.loadModel();
			addToHistory(ureq, bookmarkedPagesCtrl);
		}
		mainVC.put("segmentCmp", bookmarkedPagesCtrl.getInitialComponent());
		return bookmarkedPagesCtrl;
	}
	
	private SharedPagesController doOpenPages(UserRequest ureq) {
		if(pagesCtrl == null) {
			SearchSharePagesParameters searchParams = new SearchSharePagesParameters();
			searchParams.setBookmarkOnly(false);
			searchParams.addExcludedPageStatus(PageStatus.closed, PageStatus.deleted);
			searchParams.addExcludedPageUserStatus(PageUserStatus.done);
			List<PageStatus> filters = new ArrayList<>(5);
			filters.add(PageStatus.draft);
			filters.add(PageStatus.inRevision);
			filters.add(PageStatus.published);

			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance("Pages", 0l), null);
			pagesCtrl = new SharedPagesController(ureq, swControl, stackPanel,
					searchParams, filters, PageStatus.published);
			listenTo(pagesCtrl);
		} else {
			pagesCtrl.loadModel();
			addToHistory(ureq, pagesCtrl);
		}
		mainVC.put("segmentCmp", pagesCtrl.getInitialComponent());
		return pagesCtrl;
	}
	
	public SharedBindersController doOpenBinders(UserRequest ureq) {
		if(bindersCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance("Binders", 0l), null);
			bindersCtrl = new SharedBindersController(ureq, swControl, stackPanel);
			listenTo(bindersCtrl);
		} else {
			addToHistory(ureq, bindersCtrl);
		}
		mainVC.put("segmentCmp", bindersCtrl.getInitialComponent());
		return bindersCtrl;
	}
}
