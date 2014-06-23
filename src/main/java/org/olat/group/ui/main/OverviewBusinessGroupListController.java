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
package org.olat.group.ui.main;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.MainPanel;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.resource.OresHelper;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * 
 * 
 * 
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OverviewBusinessGroupListController extends BasicController implements Activateable2 {
	
	private final Link markedGroupsLink, myGroupsLink, openGroupsLink;
	private Link searchOpenLink;
	private final SegmentViewComponent segmentView;
	private final VelocityContainer mainVC;

	private FavoritBusinessGroupListController favoritGroupsCtrl;
	private BusinessGroupListController myGroupsCtrl;
	private OpenBusinessGroupListController openGroupsCtrl;
	private SearchBusinessGroupListController searchGroupsCtrl;
	
	public OverviewBusinessGroupListController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("group_list_overview");
		
		MainPanel mainPanel = new MainPanel("myCoursesMainPanel");
		mainPanel.setDomReplaceable(false);
		mainPanel.setContent(mainVC);

		//segmented view
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		markedGroupsLink = LinkFactory.createLink("marked.groups", mainVC, this);
		markedGroupsLink.setElementCssClass("o_sel_group_bookmarked_groups_seg");
		segmentView.addSegment(markedGroupsLink, false);
		myGroupsLink = LinkFactory.createLink("my.groups", mainVC, this);
		myGroupsLink.setElementCssClass("o_sel_group_all_groups_seg");
		segmentView.addSegment(myGroupsLink, false);
		openGroupsLink = LinkFactory.createLink("open.groups", mainVC, this);
		openGroupsLink.setElementCssClass("o_sel_group_open_groups_seg");
		segmentView.addSegment(openGroupsLink, false);
		
		Roles roles = ureq.getUserSession().getRoles();
		if(roles.isGroupManager() || roles.isOLATAdmin()) {
			searchOpenLink = LinkFactory.createLink("opengroups.search.admin", mainVC, this);
			searchOpenLink.setElementCssClass("o_sel_group_search_groups_seg");
			segmentView.addSegment(searchOpenLink, false);
		}
		
		putInitialPanel(mainPanel);
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
				Controller selectedController = null;
				if (clickedLink == markedGroupsLink) {
					selectedController = updateMarkedGroups(ureq);
				} else if (clickedLink == myGroupsLink){
					selectedController = updateMyGroups(ureq);
				} else if (clickedLink == openGroupsLink){
					selectedController = updateOpenGroups(ureq);
				} else if (clickedLink == searchOpenLink) {
					selectedController = updateSearch(ureq);
				}
				addToHistory(ureq, selectedController);
			}
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			boolean markedEmpty = updateMarkedGroups(ureq).isEmpty();
			if(markedEmpty) {
				updateMyGroups(ureq);
				segmentView.select(myGroupsLink);
			} else {
				segmentView.select(markedGroupsLink);
			}
		} else {
			ContextEntry entry = entries.get(0);
			String segment = entry.getOLATResourceable().getResourceableTypeName();
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			if("Favorits".equals(segment)) {
				updateMarkedGroups(ureq).activate(ureq, subEntries, entry.getTransientState());
				segmentView.select(markedGroupsLink);
			} else if("AllGroups".equals(segment)) {
				updateMyGroups(ureq).activate(ureq, subEntries, entry.getTransientState());
				segmentView.select(myGroupsLink);
			} else if("OwnedGroups".equals(segment)) {
				updateOpenGroups(ureq).activate(ureq, subEntries, entry.getTransientState());
				segmentView.select(openGroupsLink);
			} else if("Search".equals(segment) && searchOpenLink != null) {
				updateSearch(ureq).activate(ureq, subEntries, entry.getTransientState());
				segmentView.select(searchOpenLink);
			}
		}
	}

	private FavoritBusinessGroupListController updateMarkedGroups(UserRequest ureq) {
		if(favoritGroupsCtrl == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("Favorits", 0l);
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			favoritGroupsCtrl = new FavoritBusinessGroupListController(ureq, bwControl);
			listenTo(favoritGroupsCtrl);
		}
		favoritGroupsCtrl.doDefaultSearch();
		mainVC.put("groupList", favoritGroupsCtrl.getInitialComponent());
		addToHistory(ureq, favoritGroupsCtrl);
		return favoritGroupsCtrl;
	}
	
	private BusinessGroupListController updateMyGroups(UserRequest ureq) {
		if(myGroupsCtrl == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("AllGroups", 0l);
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			myGroupsCtrl = new BusinessGroupListController(ureq, bwControl);
			listenTo(myGroupsCtrl);
		}
		myGroupsCtrl.doDefaultSearch();
		mainVC.put("groupList", myGroupsCtrl.getInitialComponent());
		addToHistory(ureq, myGroupsCtrl);
		return myGroupsCtrl;
	}
	
	private OpenBusinessGroupListController updateOpenGroups(UserRequest ureq) {
		if(openGroupsCtrl == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("OwnedGroups", 0l);
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			openGroupsCtrl = new OpenBusinessGroupListController(ureq, bwControl);
			listenTo(openGroupsCtrl);
		}
		openGroupsCtrl.doDefaultSearch();
		mainVC.put("groupList", openGroupsCtrl.getInitialComponent());
		addToHistory(ureq, openGroupsCtrl);
		return openGroupsCtrl;
	}
	
	private SearchBusinessGroupListController updateSearch(UserRequest ureq) {
		if(searchGroupsCtrl == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("Search", 0l);
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			searchGroupsCtrl = new SearchBusinessGroupListController(ureq, bwControl);
			listenTo(searchGroupsCtrl);
		}
		mainVC.put("groupList", searchGroupsCtrl.getInitialComponent());
		addToHistory(ureq, searchGroupsCtrl);
		return searchGroupsCtrl;
	}
}
