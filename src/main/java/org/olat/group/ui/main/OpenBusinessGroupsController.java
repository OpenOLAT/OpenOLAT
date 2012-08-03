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
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OpenBusinessGroupsController extends BasicController implements Activateable2 {
	
	private final Link allOpenLink, searchOpenLink;
	private final SegmentViewComponent segmentView;
	private final VelocityContainer mainVC;
	
	private OpenBusinessGroupListController listController;
	private SearchOpenBusinessGroupListController searchController;

	public OpenBusinessGroupsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("open_group_overview");
		
		//segment view
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		allOpenLink = LinkFactory.createLink("opengroups.all", mainVC, this);
		segmentView.addSegment(allOpenLink, true);
		searchOpenLink = LinkFactory.createLink("opengroups.search", mainVC, this);
		segmentView.addSegment(searchOpenLink, false);
		
		getOpenGroups(ureq);
		
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
				if (clickedLink == allOpenLink) {
					getOpenGroups(ureq);
				} else if (clickedLink == searchOpenLink){
					getSearchGroupsController(ureq);
				}
			}
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		ContextEntry entry = entries.get(0);
		String segment = entry.getOLATResourceable().getResourceableTypeName();
		if("All".equals(segment)) {
			getOpenGroups(ureq).activate(ureq, entries.subList(1, entries.size()), entry.getTransientState());
			segmentView.select(allOpenLink);
		} else if ("Search".equals(segment)) {
			getSearchGroupsController(ureq).activate(ureq, entries.subList(1, entries.size()), entry.getTransientState());
			segmentView.select(searchOpenLink);
		}
	}

	private SearchOpenBusinessGroupListController getSearchGroupsController(UserRequest ureq) {
		if(searchController == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("Search", 0l);
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			searchController = new SearchOpenBusinessGroupListController(ureq, bwControl);
			listenTo(searchController);
		}
		
		mainVC.put("groupList", searchController.getInitialComponent());
		addToHistory(ureq, searchController);
		return searchController;
	}
	
	private OpenBusinessGroupListController getOpenGroups(UserRequest ureq) {
		if(listController == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("All", 0l);
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			listController = new OpenBusinessGroupListController(ureq, bwControl);
			listenTo(listController);
		}
		
		mainVC.put("groupList", listController.getInitialComponent());
		addToHistory(ureq, listController);
		return listController;
	}
}