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
package org.olat.modules.lecture.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.id.Roles;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.table.LabelCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectSelectionBrowserEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.ui.AddRoomsRow.RoomAvailability;
import org.olat.modules.roommanagement.Room;
import org.olat.modules.roommanagement.RoomBooking;
import org.olat.modules.roommanagement.RoomManagementService;
import org.olat.modules.roommanagement.RoomStatus;
import org.olat.modules.roommanagement.model.SearchRoomParameters;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Room selection popup for the event edit dialog. Presents a table of rooms with
 * their availability for the event's time window and pre-selects already booked rooms.
 *
 * Initial date: 30 Jun 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class AddRoomsController extends FormBasicController {

	private FlexiTableElement tableEl;
	private AddRoomsDataModel tableModel;

	private final Date startDate;
	private final Date endDate;
	private final LectureBlock lectureBlock;
	private final Set<Long> preSelectedRoomKeys;
	private Roles roles;

	private List<Room> selectedRooms = new ArrayList<>();

	@Autowired(required = false)
	private RoomManagementService roomManagementService;

	public AddRoomsController(UserRequest ureq, WindowControl wControl,
			Date startDate, Date endDate, List<Room> preSelectedRooms, LectureBlock lectureBlock) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.startDate = startDate;
		this.endDate = endDate;
		this.lectureBlock = lectureBlock;
		this.preSelectedRoomKeys = new HashSet<>();
		if (preSelectedRooms != null) {
			for (Room r : preSelectedRooms) {
				preSelectedRoomKeys.add(r.getKey());
				selectedRooms.add(r);
			}
		}
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		roles = ureq.getUserSession().getRoles();

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AddRoomsDataModel.AddRoomsCols.reference));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AddRoomsDataModel.AddRoomsCols.description));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AddRoomsDataModel.AddRoomsCols.seats));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AddRoomsDataModel.AddRoomsCols.availability,
				new RoomAvailabilityCellRenderer()));

		tableModel = new AddRoomsDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "rooms.table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(false);
		tableEl.setEmptyStateConfig(EmptyStateConfig.builder()
				.withMessageI18nKey("add.rooms.empty")
				.build());

		loadRooms();

		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
		formLayout.add(buttonGroupLayout);
		uifactory.addFormSubmitButton("add", buttonGroupLayout);
		uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
	}

	private void loadRooms() {
		if (roomManagementService == null) {
			tableModel.setObjects(List.of());
			tableEl.reset(true, true, true);
			return;
		}

		List<RoomBooking> bookingsInWindow = roomManagementService.getBookings(startDate, endDate);

		// Determine which rooms are occupied by other events
		Map<Long, RoomBooking> occupiedByOther = new HashMap<>();
		for (RoomBooking booking : bookingsInWindow) {
			boolean isOwnEvent = lectureBlock != null && lectureBlock.getKey() != null
					&& booking.getLectureBlock() != null
					&& booking.getLectureBlock().getKey().equals(lectureBlock.getKey());
			if (!isOwnEvent) {
				occupiedByOther.put(booking.getRoom().getKey(), booking);
			}
		}

		SearchRoomParameters params = new SearchRoomParameters();
		params.setStatus(List.of(RoomStatus.active));
		List<Room> rooms = roomManagementService.searchRooms(params, roles);

		List<AddRoomsRow> rows = new ArrayList<>();
		for (Room room : rooms) {
			boolean myEvent = preSelectedRoomKeys.contains(room.getKey());
			RoomBooking occupiedBy = occupiedByOther.get(room.getKey());
			rows.add(new AddRoomsRow(room, occupiedBy, myEvent));
		}

		tableModel.setObjects(rows);

		Set<Integer> preSelected = new HashSet<>();
		for (int i = 0; i < rows.size(); i++) {
			if (preSelectedRoomKeys.contains(rows.get(i).getKey())) {
				preSelected.add(i);
			}
		}
		tableEl.setSelectAllEnable(true);
		tableEl.reset(true, true, true);
		tableEl.setMultiSelectedIndex(preSelected);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		selectedRooms.clear();
		List<AddRoomsRow> allRows = tableModel.getObjects();
		Set<Integer> selectedIndexes = tableEl.getMultiSelectedIndex();
		List<String> selectedKeys = new ArrayList<>();
		for (Integer idx : selectedIndexes) {
			if (idx < allRows.size()) {
				Room room = allRows.get(idx).getRoom();
				selectedRooms.add(room);
				selectedKeys.add(room.getKey().toString());
			}
		}
		fireEvent(ureq, new ObjectSelectionBrowserEvent(selectedKeys));
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	private static class RoomAvailabilityCellRenderer extends LabelCellRenderer {

		@Override
		protected String getCellValue(Object val, Translator translator) {
			if (val instanceof RoomAvailability availability) {
				return switch (availability) {
					case AVAILABLE -> translator.translate("lecture.rooms.available");
					case OCCUPIED -> translator.translate("lecture.rooms.occupied");
					case MY_EVENT -> translator.translate("lecture.rooms.my.event");
				};
			}
			return "";
		}

		@Override
		protected String getIconCssClass(Object val) {
			if (val instanceof RoomAvailability availability) {
				return switch (availability) {
					case AVAILABLE -> "o_icon-fw o_icon_timetable";
					case OCCUPIED -> "o_icon-fw o_icon_calendar_xmark";
					case MY_EVENT -> "o_icon-fw o_icon_calendar_check";
				};
			}
			return "";
		}

		@Override
		protected String getElementCssClass(Object val) {
			if (val instanceof RoomAvailability availability) {
				return "o_lecture_room_availability_" + availability.name().toLowerCase();
			}
			return "";
		}
	}
}
