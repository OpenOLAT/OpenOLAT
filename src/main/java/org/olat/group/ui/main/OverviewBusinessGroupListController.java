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
import org.olat.core.util.resource.OresHelper;

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
		
		boolean marked = updateMarkedGroups(ureq).updateMarkedGroups();
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
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			favoritGroupsCtrl = new FavoritBusinessGroupListController(ureq, bwControl);
		}
		mainVC.put("groupList", favoritGroupsCtrl.getInitialComponent());
		addToHistory(ureq, favoritGroupsCtrl);
		return favoritGroupsCtrl;
	}
	
	private BusinessGroupListController updateAllGroups(UserRequest ureq) {
		if(allGroupsCtrl == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("AllGroups", 0l);
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			allGroupsCtrl = new BusinessGroupListController(ureq, bwControl);
		}
		allGroupsCtrl.updateAllGroups();
		mainVC.put("groupList", allGroupsCtrl.getInitialComponent());
		addToHistory(ureq, allGroupsCtrl);
		return allGroupsCtrl;
	}
	
	private OwnedBusinessGroupListController updateOwnedGroups(UserRequest ureq) {
		if(ownedGroupsCtrl == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("OwnedGroups", 0l);
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			ownedGroupsCtrl = new OwnedBusinessGroupListController(ureq, bwControl);
		}
		ownedGroupsCtrl.updateOwnedGroups();
		mainVC.put("groupList", ownedGroupsCtrl.getInitialComponent());
		addToHistory(ureq, ownedGroupsCtrl);
		return ownedGroupsCtrl;
	}
	
	private SearchBusinessGroupListController updateSearch(UserRequest ureq) {
		if(searchGroupsCtrl == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("Search", 0l);
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			searchGroupsCtrl = new SearchBusinessGroupListController(ureq, bwControl);
		}
		searchGroupsCtrl.updateSearch(ureq);
		mainVC.put("groupList", searchGroupsCtrl.getInitialComponent());
		addToHistory(ureq, searchGroupsCtrl);
		return searchGroupsCtrl;
	}
}
