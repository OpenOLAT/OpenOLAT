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
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectSelectionBrowserEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.table.LabelCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.ui.AddRoomsRow.RoomAvailability;
import org.olat.modules.roommanagement.Room;
import org.olat.modules.roommanagement.RoomBooking;
import org.olat.modules.roommanagement.RoomManagementService;
import org.olat.modules.roommanagement.RoomStatus;
import org.olat.modules.roommanagement.model.SearchRoomParameters;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Room selection popup for the event edit dialog. Presents a filterable table
 * of rooms with their availability for the event's time window and pre-selects
 * already booked rooms.
 *
 * Initial date: 30 Jun 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class AddRoomsController extends FormBasicController {

	private static final String FILTER_AVAILABILITY = "availability";

	private static final String TAB_ID_ALL = "All";
	private static final String TAB_ID_RELEVANT = "Relevant";
	private static final String TAB_ID_AVAILABLE = "Available";
	private static final String TAB_ID_OCCUPIED = "Occupied";

	private FlexiTableElement tableEl;
	private AddRoomsDataModel tableModel;

	private FlexiFiltersTab tabAll;
	private FlexiFiltersTab tabRelevant;
	private FlexiFiltersTab tabAvailable;
	private FlexiFiltersTab tabOccupied;

	private final Date startDate;
	private final Date endDate;
	private final LectureBlock lectureBlock;
	private final Set<Long> preSelectedRoomKeys;
	private final int participantCount;
	private Roles roles;

	private List<AddRoomsRow> allRows = new ArrayList<>();
	private List<Room> selectedRooms = new ArrayList<>();

	@Autowired(required = false)
	private RoomManagementService roomManagementService;

	public AddRoomsController(UserRequest ureq, WindowControl wControl,
			Date startDate, Date endDate, List<Room> preSelectedRooms, LectureBlock lectureBlock, int participantCount) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.startDate = startDate;
		this.endDate = endDate;
		this.lectureBlock = lectureBlock;
		this.participantCount = participantCount;
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
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AddRoomsDataModel.AddRoomsCols.seats,
				new SeatsCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AddRoomsDataModel.AddRoomsCols.availability,
				new RoomAvailabilityCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AddRoomsDataModel.AddRoomsCols.occupiedBy,
				new OccupiedByCellRenderer()));

		tableModel = new AddRoomsDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "rooms.table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(false);
		tableEl.setSearchEnabled(true);
		tableEl.setEmptyStateConfig(EmptyStateConfig.builder()
				.withMessageI18nKey("add.rooms.empty")
				.build());

		initFilters();
		initFilterTabs(ureq);
		loadRooms();
		applyFilters();

		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
		buttonGroupLayout.setElementCssClass("o_lecture_rooms_add_buttons");
		formLayout.add(buttonGroupLayout);
		uifactory.addFormSubmitButton("add.rooms.confirm", buttonGroupLayout);
		uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
	}

	private void initFilters() {
		SelectionValues availabilityValues = new SelectionValues();
		availabilityValues.add(SelectionValues.entry(RoomAvailability.AVAILABLE.name(), translate("lecture.rooms.available")));
		availabilityValues.add(SelectionValues.entry(RoomAvailability.OCCUPIED.name(), translate("lecture.rooms.occupied")));
		availabilityValues.add(SelectionValues.entry(RoomAvailability.MY_EVENT.name(), translate("lecture.rooms.my.event")));
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		filters.add(new FlexiTableMultiSelectionFilter(translate("add.rooms.col.availability"),
				FILTER_AVAILABILITY, availabilityValues, true));
		tableEl.setFilters(true, filters, false, false);
	}

	private void initFilterTabs(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>();

		tabAll = FlexiFiltersTabFactory.tab(TAB_ID_ALL, translate("add.rooms.filter.all"), TabSelectionBehavior.reloadData);
		tabs.add(tabAll);

		tabRelevant = FlexiFiltersTabFactory.tab(TAB_ID_RELEVANT, translate("add.rooms.filter.relevant"), TabSelectionBehavior.reloadData);
		tabs.add(tabRelevant);

		tabAvailable = FlexiFiltersTabFactory.tab(TAB_ID_AVAILABLE, translate("add.rooms.filter.available"), TabSelectionBehavior.reloadData);
		tabs.add(tabAvailable);

		tabOccupied = FlexiFiltersTabFactory.tab(TAB_ID_OCCUPIED, translate("add.rooms.filter.occupied"), TabSelectionBehavior.reloadData);
		tabs.add(tabOccupied);

		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, tabAll);
	}

	private void loadRooms() {
		if (roomManagementService == null) {
			allRows = new ArrayList<>();
			return;
		}

		List<RoomBooking> bookingsInWindow = roomManagementService.getBookings(startDate, endDate);

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

		allRows = new ArrayList<>(rooms.size());
		for (Room room : rooms) {
			boolean myEvent = preSelectedRoomKeys.contains(room.getKey());
			RoomBooking occupiedBy = occupiedByOther.get(room.getKey());
			allRows.add(new AddRoomsRow(room, occupiedBy, myEvent, participantCount));
		}
	}

	private void applyFilters() {
		FlexiFiltersTab selectedTab = tableEl.getSelectedFilterTab();
		List<AddRoomsRow> filtered;
		if (tabRelevant == selectedTab) {
			filtered = allRows.stream()
					.filter(r -> r.getAvailability() == RoomAvailability.AVAILABLE || r.getAvailability() == RoomAvailability.MY_EVENT)
					.filter(r -> !r.isSeatWarning())
					.collect(Collectors.toList());
		} else if (tabAvailable == selectedTab) {
			filtered = allRows.stream()
					.filter(r -> r.getAvailability() == RoomAvailability.AVAILABLE || r.getAvailability() == RoomAvailability.MY_EVENT)
					.collect(Collectors.toList());
		} else if (tabOccupied == selectedTab) {
			filtered = allRows.stream()
					.filter(r -> r.getAvailability() == RoomAvailability.OCCUPIED)
					.collect(Collectors.toList());
		} else {
			filtered = new ArrayList<>(allRows);
		}

		String searchString = tableEl.getQuickSearchString();
		if (StringHelper.containsNonWhitespace(searchString)) {
			String lc = searchString.toLowerCase();
			filtered = filtered.stream()
					.filter(r -> (r.getReference() != null && r.getReference().toLowerCase().contains(lc))
							|| (r.getDescription() != null && r.getDescription().toLowerCase().contains(lc)))
					.collect(Collectors.toList());
		}

		FlexiTableFilter availFilter = FlexiTableFilter.getFilter(tableEl.getFilters(), FILTER_AVAILABILITY);
		if (availFilter instanceof FlexiTableExtendedFilter extFilter) {
			List<String> values = extFilter.getValues();
			if (values != null && !values.isEmpty()) {
				Set<String> selectedValues = new HashSet<>(values);
				filtered = filtered.stream()
						.filter(r -> selectedValues.contains(r.getAvailability().name()))
						.collect(Collectors.toList());
			}
		}

		Set<Integer> preSelected = new HashSet<>();
		for (int i = 0; i < filtered.size(); i++) {
			if (preSelectedRoomKeys.contains(filtered.get(i).getKey())) {
				preSelected.add(i);
			}
		}
		tableModel.setObjects(filtered);
		tableEl.reset(true, true, true);
		tableEl.setMultiSelectedIndex(preSelected);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == tableEl && (event instanceof FlexiTableFilterTabEvent || event instanceof FlexiTableSearchEvent)) {
			applyFilters();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		selectedRooms.clear();
		List<AddRoomsRow> currentRows = tableModel.getObjects();
		Set<Integer> selectedIndexes = tableEl.getMultiSelectedIndex();
		List<String> selectedKeys = new ArrayList<>();
		for (Integer idx : selectedIndexes) {
			if (idx < currentRows.size()) {
				Room room = currentRows.get(idx).getRoom();
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

	private static class SeatsCellRenderer implements FlexiCellRenderer {
		@Override
		public void render(Renderer renderer, StringOutput target, Object cellValue,
				int row, FlexiTableComponent source, URLBuilder ubu, Translator translator) {
			if (!(cellValue instanceof AddRoomsRow roomRow)) return;
			Integer seats = roomRow.getSeats();
			if (seats == null) return;
			if (roomRow.isSeatWarning()) {
				target.append("<span class=\"o_warn\"><i class=\"o_icon o_icon_warning\"></i> </span>");
			}
			target.append(seats);
		}
	}

	private static class OccupiedByCellRenderer implements FlexiCellRenderer {
		@Override
		public void render(Renderer renderer, StringOutput target, Object cellValue,
				int row, FlexiTableComponent source, URLBuilder ubu, Translator translator) {
			if (!(cellValue instanceof RoomBooking booking)) return;
			LectureBlock lb = booking.getLectureBlock();
			if (lb == null) return;
			String title = lb.getTitle();
			if (!StringHelper.containsNonWhitespace(title)) return;
			target.append(StringHelper.escapeHtml(title));
			if (StringHelper.containsNonWhitespace(lb.getExternalRef())) {
				target.append(" <small class=\"text-muted\">")
						.append(StringHelper.escapeHtml(lb.getExternalRef()))
						.append("</small>");
			}
		}
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
