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
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Roles;
import org.olat.group.BusinessGroupModule;
import org.olat.group.model.BusinessGroupSelectionEvent;
import org.olat.group.ui.NewBGController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Generic controller to select a business group
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SelectBusinessGroupController extends BasicController {
	
	private final Link markedGroupsLink, ownedGroupsLink, courseGroupsLink, searchOpenLink;
	private Link createGroup, adminSearchOpenLink;
	private final SegmentViewComponent segmentView;
	private final VelocityContainer mainVC;

	private SelectFavoritBusinessGroupController favoritGroupsCtrl;
	private SelectOwnedBusinessGroupController ownedGroupsCtrl;
	private SelectBusinessGroupCourseAuthorController authorGroupsCtrL;
	private SelectSearchBusinessGroupController searchGroupsCtrl;
	private SelectSearchBusinessGroupController searchAdminGroupsCtrl;
	
	private NewBGController groupCreateController;
	protected CloseableModalController cmc;
	
	private final boolean enableCreate;
	private Object userObject;
	private final BusinessGroupViewFilter filter;

	@Autowired
	private BusinessGroupModule businessGroupModule;
	
	public SelectBusinessGroupController(UserRequest ureq, WindowControl wControl) {
		this(ureq, wControl, null);
	}
	
	public SelectBusinessGroupController(UserRequest ureq, WindowControl wControl, BusinessGroupViewFilter filter) {
		super(ureq, wControl);
		this.filter = filter;
		enableCreate = businessGroupModule.isAllowedCreate(ureq.getUserSession().getRoles());
		mainVC = createVelocityContainer("group_list_overview");
		
		if(enableCreate) {
			createGroup = LinkFactory.createButton("create.group", mainVC, this);
			mainVC.put("create", createGroup);
		}
		
		boolean marked = updateMarkedGroups(ureq);
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
		return roles.isAdministrator() 
				|| roles.isGroupManager() 
				|| (roles.isLearnResourceManager() && businessGroupModule.isResourceManagersAllowedToLinkGroups());
	}
	
	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
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
		} else if(createGroup == source) {
			doCreate(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(event instanceof BusinessGroupSelectionEvent) {
			fireEvent(ureq, event);
		} else if(groupCreateController == source) {
			if(event == Event.DONE_EVENT) {
				//current identity is set as owner -> view them in coach
				updateOwnedGroups(ureq);
				segmentView.select(ownedGroupsLink);
			}
			cmc.deactivate();
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		} else {
			super.event(ureq, source, event);
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(groupCreateController);
		removeAsListenerAndDispose(cmc);
		groupCreateController = null;
		cmc = null;
	}
	
	protected void doCreate(UserRequest ureq) {				
		removeAsListenerAndDispose(groupCreateController);
		groupCreateController = new NewBGController(ureq, getWindowControl(), null, false, null);
		listenTo(groupCreateController);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), groupCreateController.getInitialComponent(), true, translate("create.form.title"));
		cmc.activate();
		listenTo(cmc);
	}

	private boolean updateMarkedGroups(UserRequest ureq) {
		if(favoritGroupsCtrl == null) {
			favoritGroupsCtrl = new SelectFavoritBusinessGroupController(ureq, getWindowControl());
			favoritGroupsCtrl.setFilter(filter);
			listenTo(favoritGroupsCtrl);
		}
		boolean markedFound = favoritGroupsCtrl.doDefaultSearch();
		mainVC.put("groupList", favoritGroupsCtrl.getInitialComponent());
		return markedFound;
	}
	
	private void updateOwnedGroups(UserRequest ureq) {
		if(ownedGroupsCtrl == null) {
			ownedGroupsCtrl = new SelectOwnedBusinessGroupController(ureq, getWindowControl());
			ownedGroupsCtrl.setFilter(filter);
			listenTo(ownedGroupsCtrl);
		}
		ownedGroupsCtrl.doDefaultSearch();
		mainVC.put("groupList", ownedGroupsCtrl.getInitialComponent());
	}
	
	private void updateCourseGroups(UserRequest ureq) {
		if(authorGroupsCtrL == null) {
			authorGroupsCtrL = new SelectBusinessGroupCourseAuthorController(ureq, getWindowControl());
			authorGroupsCtrL.setFilter(filter);
			listenTo(authorGroupsCtrL);
		}
		authorGroupsCtrL.doDefaultSearch();
		mainVC.put("groupList", authorGroupsCtrL.getInitialComponent());
	}
	
	private void updateSearch(UserRequest ureq) {
		if(searchGroupsCtrl == null) {
			searchGroupsCtrl = new SelectSearchBusinessGroupController(ureq, getWindowControl(), true);
			searchGroupsCtrl.setFilter(filter);
			listenTo(searchGroupsCtrl);
		}
		searchGroupsCtrl.updateSearch(ureq);
		mainVC.put("groupList", searchGroupsCtrl.getInitialComponent());
	}
	
	private void updateAdminSearch(UserRequest ureq) {
		if(searchAdminGroupsCtrl == null) {
			searchAdminGroupsCtrl = new SelectSearchBusinessGroupController(ureq, getWindowControl(), false);
			searchAdminGroupsCtrl.setFilter(filter);
			listenTo(searchAdminGroupsCtrl);
		}
		searchAdminGroupsCtrl.updateSearch(ureq);
		mainVC.put("groupList", searchAdminGroupsCtrl.getInitialComponent());
	}
}
