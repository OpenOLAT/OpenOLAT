/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.roommanagement.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;

/**
 * Initial date: 28 May 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RoomManagementAdminController extends BasicController implements Activateable2 {

	private static final String ORES_TYPE_SETTINGS = "Settings";
	private static final String ORES_TYPE_ROOM_SCHEDULING = "RoomScheduling";
	private static final String ORES_TYPE_ROOMS = "Rooms";
	private static final String ORES_TYPE_BUILDINGS = "Buildings";

	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	private final Link settingsLink;
	private final Link roomSchedulingLink;
	private final Link roomsLink;
	private final Link buildingsLink;

	private RoomsAdminSettingsController settingsCtrl;
	private RoomSchedulingController roomSchedulingCtrl;
	private BreadcrumbedStackedPanel roomsPanel;
	private RoomListController roomListCtrl;
	private BuildingListController buildingListCtrl;

	public RoomManagementAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		mainVC = createVelocityContainer("admin");
		putInitialPanel(mainVC);

		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		segmentView.setDontShowSingleSegment(true);

		settingsLink = LinkFactory.createLink("admin.settings", mainVC, this);
		segmentView.addSegment(settingsLink, true);

		roomSchedulingLink = LinkFactory.createLink("admin.room.scheduling", mainVC, this);
		segmentView.addSegment(roomSchedulingLink, false);

		roomsLink = LinkFactory.createLink("admin.rooms", mainVC, this);
		segmentView.addSegment(roomsLink, false);

		buildingsLink = LinkFactory.createLink("admin.buildings", mainVC, this);
		segmentView.addSegment(buildingsLink, false);

		doOpenSettings(ureq);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (entries == null || entries.isEmpty()) return;

		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if (ORES_TYPE_SETTINGS.equalsIgnoreCase(type) && settingsLink.isVisible()) {
			doOpenSettings(ureq);
			segmentView.select(settingsLink);
		} else if (ORES_TYPE_ROOM_SCHEDULING.equalsIgnoreCase(type) && roomSchedulingLink.isVisible()) {
			doOpenRoomScheduling(ureq);
			segmentView.select(roomSchedulingLink);
		} else if (ORES_TYPE_ROOMS.equalsIgnoreCase(type) && roomsLink.isVisible()) {
			doOpenRooms(ureq);
			segmentView.select(roomsLink);
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			if (!subEntries.isEmpty()) {
				roomListCtrl.activate(ureq, subEntries, entries.get(0).getTransientState());
			}
		} else if (ORES_TYPE_BUILDINGS.equalsIgnoreCase(type) && buildingsLink.isVisible()) {
			doOpenBuildings(ureq);
			segmentView.select(buildingsLink);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (event instanceof OpenBuildingEvent openBuildingEvent
				&& (source == roomListCtrl || source == roomSchedulingCtrl)) {
			doOpenBuildings(ureq);
			segmentView.select(buildingsLink);
			buildingListCtrl.selectBuilding(ureq, openBuildingEvent.getBuildingKey());
		} else if (event instanceof OpenRoomEvent openRoomEvent && source == roomSchedulingCtrl) {
			doOpenRooms(ureq);
			segmentView.select(roomsLink);
			roomListCtrl.selectRoom(ureq, openRoomEvent.getRoomKey());
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == roomsPanel && event instanceof PopEvent) {
			// panel handles controller disposal automatically
		} else if (source == segmentView && event instanceof SegmentViewEvent sve) {
			String segmentCName = sve.getComponentName();
			Component clickedLink = mainVC.getComponent(segmentCName);
			if (clickedLink == settingsLink) {
				doOpenSettings(ureq);
			} else if (clickedLink == roomSchedulingLink) {
				doOpenRoomScheduling(ureq);
			} else if (clickedLink == roomsLink) {
				doOpenRooms(ureq);
			} else if (clickedLink == buildingsLink) {
				doOpenBuildings(ureq);
			}
		}
	}

	private void doOpenSettings(UserRequest ureq) {
		if (settingsCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ORES_TYPE_SETTINGS), null);
			settingsCtrl = new RoomsAdminSettingsController(ureq, swControl);
			listenTo(settingsCtrl);
		} else {
			addToHistory(ureq, settingsCtrl);
		}
		mainVC.put("segmentCmp", settingsCtrl.getInitialComponent());
	}

	private void doOpenRoomScheduling(UserRequest ureq) {
		if (roomSchedulingCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ORES_TYPE_ROOM_SCHEDULING), null);
			roomSchedulingCtrl = new RoomSchedulingController(ureq, swControl);
			listenTo(roomSchedulingCtrl);
		} else {
			addToHistory(ureq, roomSchedulingCtrl);
		}
		mainVC.put("segmentCmp", roomSchedulingCtrl.getInitialComponent());
	}

	private void doOpenRooms(UserRequest ureq) {
		if (roomListCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ORES_TYPE_ROOMS), null);
			roomsPanel = new BreadcrumbedStackedPanel("roomsPanel", getTranslator(), this);
			roomListCtrl = new RoomListController(ureq, swControl, roomsPanel);
			listenTo(roomListCtrl);
			roomsPanel.pushController(translate("admin.rooms"), roomListCtrl);
		} else {
			addToHistory(ureq, roomListCtrl);
			roomListCtrl.activate(ureq, List.of(), null);
		}
		mainVC.put("segmentCmp", roomsPanel);
	}

	private void doOpenBuildings(UserRequest ureq) {
		if (buildingListCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ORES_TYPE_BUILDINGS), null);
			buildingListCtrl = new BuildingListController(ureq, swControl);
			listenTo(buildingListCtrl);
		} else {
			addToHistory(ureq, buildingListCtrl);
		}
		mainVC.put("segmentCmp", buildingListCtrl.getInitialComponent());
	}
}
