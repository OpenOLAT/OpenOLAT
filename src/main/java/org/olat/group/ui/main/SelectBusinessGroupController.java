package org.olat.group.ui.main;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * 
 * Generic controller to select a business group
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SelectBusinessGroupController extends BasicController {
	
	private final Link markedGroupsLink, ownedGroupsLink, courseGroupsLink, searchOpenLink;
	private final SegmentViewComponent segmentView;
	private final VelocityContainer mainVC;

	private FavoritBusinessGroupListController favoritGroupsCtrl;
	private OwnedBusinessGroupListController ownedGroupsCtrl;
	private SearchBusinessGroupListController searchGroupsCtrl;
	
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
		ownedGroupsLink = LinkFactory.createLink("course.groups", mainVC, this);
		segmentView.addSegment(ownedGroupsLink, false);
		courseGroupsLink = LinkFactory.createLink("opengroups.all", mainVC, this);
		segmentView.addSegment(courseGroupsLink, !marked);
		searchOpenLink = LinkFactory.createLink("opengroups.search", mainVC, this);
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
				if (clickedLink == markedGroupsLink) {
					updateMarkedGroups(ureq);
				} else if (clickedLink == ownedGroupsLink){
					updateOwnedGroups(ureq);
				} else if (clickedLink == courseGroupsLink){
					updateCourseGroups(ureq);
				} else if (clickedLink == searchOpenLink) {
					updateSearch(ureq);
				}
			}
		}
	}

	private FavoritBusinessGroupListController updateMarkedGroups(UserRequest ureq) {
		if(favoritGroupsCtrl == null) {
			favoritGroupsCtrl = new FavoritBusinessGroupListController(ureq, getWindowControl());
			listenTo(favoritGroupsCtrl);
		}
		favoritGroupsCtrl.updateMarkedGroups();
		mainVC.put("groupList", favoritGroupsCtrl.getInitialComponent());
		return favoritGroupsCtrl;
	}
	
	private OwnedBusinessGroupListController updateOwnedGroups(UserRequest ureq) {
		if(ownedGroupsCtrl == null) {
			ownedGroupsCtrl = new OwnedBusinessGroupListController(ureq, getWindowControl());
			listenTo(ownedGroupsCtrl);
		}
		ownedGroupsCtrl.updateOwnedGroups();
		mainVC.put("groupList", ownedGroupsCtrl.getInitialComponent());
		return ownedGroupsCtrl;
	}
	
	private void updateCourseGroups(UserRequest ureq) {
		mainVC.put("groupList", new Panel("empty"));
	}
	
	private SearchBusinessGroupListController updateSearch(UserRequest ureq) {
		if(searchGroupsCtrl == null) {
			searchGroupsCtrl = new SearchBusinessGroupListController(ureq, getWindowControl());
			listenTo(searchGroupsCtrl);
		}
		searchGroupsCtrl.updateSearch(ureq);
		mainVC.put("groupList", searchGroupsCtrl.getInitialComponent());
		return searchGroupsCtrl;
	}
}
