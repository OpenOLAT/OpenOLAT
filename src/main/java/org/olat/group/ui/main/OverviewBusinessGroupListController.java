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
	
	private final Link markedGroupsLink, allGroupsLink, ownedGroupsLink, searchOpenLink;
	private final SegmentViewComponent segmentView;
	private final VelocityContainer mainVC;

	private FavoritBusinessGroupListController favoritGroupsCtrl;
	private BusinessGroupListController allGroupsCtrl;
	private OwnedBusinessGroupListController ownedGroupsCtrl;
	private SearchBusinessGroupListController searchGroupsCtrl;
	
	public OverviewBusinessGroupListController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("group_list_overview");
		
		boolean markedEmpty = updateMarkedGroups(ureq).isEmpty();
		if(markedEmpty) {
			updateAllGroups(ureq);
		}
		
		//segmented view
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		markedGroupsLink = LinkFactory.createLink("marked.groups", mainVC, this);
		markedGroupsLink.setElementCssClass("o_sel_group_bookmarked_groups_seg");
		segmentView.addSegment(markedGroupsLink, !markedEmpty);
		allGroupsLink = LinkFactory.createLink("opengroups.all", mainVC, this);
		allGroupsLink.setElementCssClass("o_sel_group_all_groups_seg");
		segmentView.addSegment(allGroupsLink, markedEmpty);
		ownedGroupsLink = LinkFactory.createLink("owned.groups", mainVC, this);
		ownedGroupsLink.setElementCssClass("o_sel_group_owned_groups_seg");
		segmentView.addSegment(ownedGroupsLink, false);
		searchOpenLink = LinkFactory.createLink("opengroups.search", mainVC, this);
		searchOpenLink.setElementCssClass("o_sel_group_search_groups_seg");
		segmentView.addSegment(searchOpenLink, false);
		
		putInitialPanel(mainVC);
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
				} else if (clickedLink == allGroupsLink){
					selectedController = updateAllGroups(ureq);
				} else if (clickedLink == ownedGroupsLink){
					selectedController = updateOwnedGroups(ureq);
				} else if (clickedLink == searchOpenLink) {
					selectedController = updateSearch(ureq);
				}
				addToHistory(ureq, selectedController);
			}
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		ContextEntry entry = entries.get(0);
		String segment = entry.getOLATResourceable().getResourceableTypeName();
		List<ContextEntry> subEntries = entries.subList(1, entries.size());
		if("Favorits".equals(segment)) {
			updateMarkedGroups(ureq).activate(ureq, subEntries, entry.getTransientState());
			segmentView.select(markedGroupsLink);
		} else if("AllGroups".equals(segment)) {
			updateAllGroups(ureq).activate(ureq, subEntries, entry.getTransientState());
			segmentView.select(allGroupsLink);
		} else if("OwnedGroups".equals(segment)) {
			updateOwnedGroups(ureq).activate(ureq, subEntries, entry.getTransientState());
			segmentView.select(ownedGroupsLink);
		} else if("Search".equals(segment)) {
			updateSearch(ureq).activate(ureq, subEntries, entry.getTransientState());
			segmentView.select(searchOpenLink);
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
		favoritGroupsCtrl.updateMarkedGroups();
		mainVC.put("groupList", favoritGroupsCtrl.getInitialComponent());
		addToHistory(ureq, favoritGroupsCtrl);
		return favoritGroupsCtrl;
	}
	
	private BusinessGroupListController updateAllGroups(UserRequest ureq) {
		if(allGroupsCtrl == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("AllGroups", 0l);
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			allGroupsCtrl = new BusinessGroupListController(ureq, bwControl);
			listenTo(allGroupsCtrl);
		}
		allGroupsCtrl.updateAllGroups();
		mainVC.put("groupList", allGroupsCtrl.getInitialComponent());
		addToHistory(ureq, allGroupsCtrl);
		return allGroupsCtrl;
	}
	
	private OwnedBusinessGroupListController updateOwnedGroups(UserRequest ureq) {
		if(ownedGroupsCtrl == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("OwnedGroups", 0l);
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			ownedGroupsCtrl = new OwnedBusinessGroupListController(ureq, bwControl);
			listenTo(ownedGroupsCtrl);
		}
		ownedGroupsCtrl.updateOwnedGroups();
		mainVC.put("groupList", ownedGroupsCtrl.getInitialComponent());
		addToHistory(ureq, ownedGroupsCtrl);
		return ownedGroupsCtrl;
	}
	
	private SearchBusinessGroupListController updateSearch(UserRequest ureq) {
		if(searchGroupsCtrl == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("Search", 0l);
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			searchGroupsCtrl = new SearchBusinessGroupListController(ureq, bwControl);
			listenTo(searchGroupsCtrl);
		}
		searchGroupsCtrl.updateSearch(ureq);
		mainVC.put("groupList", searchGroupsCtrl.getInitialComponent());
		addToHistory(ureq, searchGroupsCtrl);
		return searchGroupsCtrl;
	}
}
