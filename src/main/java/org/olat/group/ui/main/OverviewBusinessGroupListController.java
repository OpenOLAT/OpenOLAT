package org.olat.group.ui.main;

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

/**
 * 
 * 
 * 
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OverviewBusinessGroupListController extends BasicController {
	
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
		
		boolean marked = updateMarkedGroups(ureq);
		if(!marked) {
			updateAllGroups(ureq);
		}
		
		//segmented view
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		markedGroupsLink = LinkFactory.createLink("marked.groups", mainVC, this);
		segmentView.addSegment(markedGroupsLink, marked);
		allGroupsLink = LinkFactory.createLink("opengroups.all", mainVC, this);
		segmentView.addSegment(allGroupsLink, !marked);
		ownedGroupsLink = LinkFactory.createLink("owned.groups", mainVC, this);
		segmentView.addSegment(ownedGroupsLink, false);
		searchOpenLink = LinkFactory.createLink("opengroups.search", mainVC, this);
		segmentView.addSegment(searchOpenLink, false);
		
		putInitialPanel(mainVC);
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
				} else if (clickedLink == allGroupsLink){
					updateAllGroups(ureq);
				} else if (clickedLink == ownedGroupsLink){
					updateOwnedGroups(ureq);
				} else if (clickedLink == searchOpenLink) {
					doSearch(ureq, null);
				}
			}
		}
	}

	@Override
	protected void doDispose() {
		//
	}
	
	private boolean updateMarkedGroups(UserRequest ureq) {
		if(favoritGroupsCtrl == null) {
			favoritGroupsCtrl = new FavoritBusinessGroupListController(ureq, getWindowControl());
		}
		boolean hasMark = favoritGroupsCtrl.updateMarkedGroups();
		mainVC.put("groupList", favoritGroupsCtrl.getInitialComponent());
		return hasMark;
	}
	
	private void updateAllGroups(UserRequest ureq) {
		if(allGroupsCtrl == null) {
			allGroupsCtrl = new BusinessGroupListController(ureq, getWindowControl());
		}
		allGroupsCtrl.updateAllGroups();
		mainVC.put("groupList", allGroupsCtrl.getInitialComponent());
	}
	
	private void updateOwnedGroups(UserRequest ureq) {
		if(ownedGroupsCtrl == null) {
			ownedGroupsCtrl = new OwnedBusinessGroupListController(ureq, getWindowControl());
		}
		ownedGroupsCtrl.updateOwnedGroups();
		mainVC.put("groupList", ownedGroupsCtrl.getInitialComponent());
	}
	
	private void doSearch(UserRequest ureq, Object obj) {
		if(searchGroupsCtrl == null) {
			searchGroupsCtrl = new SearchBusinessGroupListController(ureq, getWindowControl());
		}
		searchGroupsCtrl.updateSearch();
		mainVC.put("groupList", searchGroupsCtrl.getInitialComponent());
	}
}
