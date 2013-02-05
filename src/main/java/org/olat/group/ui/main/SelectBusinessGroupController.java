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

import org.olat.core.CoreSpringFactory;
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
import org.olat.core.id.Roles;
import org.olat.group.BusinessGroupModule;
import org.olat.group.model.BusinessGroupSelectionEvent;

/**
 * 
 * Generic controller to select a business group
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SelectBusinessGroupController extends BasicController {
	
	private final Link markedGroupsLink, ownedGroupsLink, courseGroupsLink, searchOpenLink;
	private Link adminSearchOpenLink;
	private final SegmentViewComponent segmentView;
	private final VelocityContainer mainVC;

	private SelectFavoritBusinessGroupController favoritGroupsCtrl;
	private SelectOwnedBusinessGroupController ownedGroupsCtrl;
	private SelectBusinessGroupCourseAuthorController authorGroupsCtrL;
	private SelectSearchBusinessGroupController searchGroupsCtrl;
	private SelectSearchBusinessGroupController searchAdminGroupsCtrl;
	
	public SelectBusinessGroupController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("group_list_overview");
		
		boolean marked = updateMarkedGroups(ureq).updateMarkedGroups();
		if(!marked) {
			updateOwnedGroups(ureq);
		}
		
		//segmented view
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		markedGroupsLink = LinkFactory.createLink("marked.groups", mainVC, this);
		segmentView.addSegment(markedGroupsLink, marked);
		courseGroupsLink = LinkFactory.createLink("course.groups", mainVC, this);
		segmentView.addSegment(courseGroupsLink, false);
		ownedGroupsLink = LinkFactory.createLink("owned.groups.2", mainVC, this);
		segmentView.addSegment(ownedGroupsLink, !marked);
		searchOpenLink = LinkFactory.createLink("opengroups.search", mainVC, this);
		segmentView.addSegment(searchOpenLink, false);
		if(isAdminSearchAllowed(ureq)) {
			adminSearchOpenLink = LinkFactory.createLink("opengroups.search.admin", mainVC, this);
			segmentView.addSegment(adminSearchOpenLink, false);
		}
		putInitialPanel(mainVC);
	}
	
	private boolean isAdminSearchAllowed(UserRequest ureq) {
		Roles roles = ureq.getUserSession().getRoles();
		return roles.isOLATAdmin() ||
				(roles.isInstitutionalResourceManager() && roles.isGroupManager()) ||
				(ureq.getUserSession().getRoles().isInstitutionalResourceManager()
						&& CoreSpringFactory.getImpl(BusinessGroupModule.class).isResourceManagersAllowedToLinkGroups());
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
				if (clickedLink == markedGroupsLink) {
					updateMarkedGroups(ureq);
				} else if (clickedLink == ownedGroupsLink){
					updateOwnedGroups(ureq);
				} else if (clickedLink == courseGroupsLink){
					updateCourseGroups(ureq);
				} else if (clickedLink == searchOpenLink) {
					updateSearch(ureq);
				} else if (clickedLink == adminSearchOpenLink) {
					updateAdminSearch(ureq);
				}
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(event instanceof BusinessGroupSelectionEvent) {
			fireEvent(ureq, event);
		} else {
			super.event(ureq, source, event);
		}
	}

	private SelectFavoritBusinessGroupController updateMarkedGroups(UserRequest ureq) {
		if(favoritGroupsCtrl == null) {
			favoritGroupsCtrl = new SelectFavoritBusinessGroupController(ureq, getWindowControl());
			listenTo(favoritGroupsCtrl);
		}
		favoritGroupsCtrl.updateMarkedGroups();
		mainVC.put("groupList", favoritGroupsCtrl.getInitialComponent());
		return favoritGroupsCtrl;
	}
	
	private SelectOwnedBusinessGroupController updateOwnedGroups(UserRequest ureq) {
		if(ownedGroupsCtrl == null) {
			ownedGroupsCtrl = new SelectOwnedBusinessGroupController(ureq, getWindowControl());
			listenTo(ownedGroupsCtrl);
		}
		ownedGroupsCtrl.updateOwnedGroups();
		mainVC.put("groupList", ownedGroupsCtrl.getInitialComponent());
		return ownedGroupsCtrl;
	}
	
	private SelectBusinessGroupCourseAuthorController updateCourseGroups(UserRequest ureq) {
		if(authorGroupsCtrL == null) {
			authorGroupsCtrL = new SelectBusinessGroupCourseAuthorController(ureq, getWindowControl());
			listenTo(authorGroupsCtrL);
		}
		authorGroupsCtrL.updateOwnedGroups();
		mainVC.put("groupList", authorGroupsCtrL.getInitialComponent());
		return authorGroupsCtrL;
	}
	
	private SelectSearchBusinessGroupController updateSearch(UserRequest ureq) {
		if(searchGroupsCtrl == null) {
			searchGroupsCtrl = new SelectSearchBusinessGroupController(ureq, getWindowControl(), true);
			listenTo(searchGroupsCtrl);
		}
		searchGroupsCtrl.updateSearch(ureq);
		mainVC.put("groupList", searchGroupsCtrl.getInitialComponent());
		return searchGroupsCtrl;
	}
	
	private SelectSearchBusinessGroupController updateAdminSearch(UserRequest ureq) {
		if(searchAdminGroupsCtrl == null) {
			searchAdminGroupsCtrl = new SelectSearchBusinessGroupController(ureq, getWindowControl(), false);
			listenTo(searchAdminGroupsCtrl);
		}
		searchAdminGroupsCtrl.updateSearch(ureq);
		mainVC.put("groupList", searchAdminGroupsCtrl.getInitialComponent());
		return searchAdminGroupsCtrl;
	}
}
