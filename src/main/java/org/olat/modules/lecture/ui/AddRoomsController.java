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
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarModule;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.ui.components.FullCalendarElement;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.commons.services.color.ColorService;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRenderEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.table.LabelCellRenderer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Roles;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.model.Reference;
import org.olat.modules.lecture.ui.AddRoomsRow.RoomAvailability;
import org.olat.modules.lecture.ui.component.ReferenceRenderer;
import org.olat.modules.roommanagement.Room;
import org.olat.modules.roommanagement.ui.RoomCalendarRenderer;
import org.olat.modules.roommanagement.RoomBooking;
import org.olat.modules.roommanagement.RoomManagementService;
import org.olat.modules.roommanagement.RoomStatus;
import org.olat.modules.roommanagement.model.SearchRoomParameters;
import org.olat.repository.RepositoryEntry;
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
	private FullCalendarElement calendarEl;

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
	@Autowired
	private CalendarModule calendarModule;
	@Autowired
	private ColorService colorService;

	public AddRoomsController(UserRequest ureq, WindowControl wControl,
			Date startDate, Date endDate, List<Room> preSelectedRooms, LectureBlock lectureBlock, int participantCount) {
		super(ureq, wControl, "add_rooms");
		setTranslator(Util.createPackageTranslator(CalendarManager.class, ureq.getLocale(), getTranslator()));
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
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false,
				AddRoomsDataModel.AddRoomsCols.element, new ReferenceRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false,
				AddRoomsDataModel.AddRoomsCols.course, new ReferenceRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AddRoomsDataModel.AddRoomsCols.earlierSlot,
				new TimeSlotCellRenderer(startDate, true, getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AddRoomsDataModel.AddRoomsCols.laterSlot,
				new TimeSlotCellRenderer(endDate, false, getLocale())));
		tableModel = new AddRoomsDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "rooms.table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setCssDelegate(new PreSelectedRowsCssDelegate());
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(false);
		tableEl.setSearchEnabled(true);
		tableEl.setEmptyStateConfig(EmptyStateConfig.builder()
				.withMessageI18nKey("add.rooms.empty")
				.build());
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.external, FlexiTableRendererType.classic);
		tableEl.setRendererType(FlexiTableRendererType.classic);
		tableEl.setExternalRenderer(new RoomCalendarRenderer(), "o_icon_calendar o_icon-lg");
		tableEl.getExternalTypeButton().setTitle(translate("add.rooms.view.calendar"));

		calendarEl = new FullCalendarElement(ureq, RoomCalendarRenderer.CALENDAR_ITEM_NAME, new ArrayList<>(), getTranslator());
		calendarEl.setShowEventDuration(true);
		calendarEl.setFocusDate(startDate);
		calendarEl.setVisible(false);
		formLayout.add(RoomCalendarRenderer.CALENDAR_ITEM_NAME, calendarEl);

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

		// Load all bookings for the same day to compute earlier/later free slots
		Map<Long, List<RoomBooking>> dayBookingsByRoom = loadDayBookingsByRoom();

		SearchRoomParameters params = new SearchRoomParameters();
		params.setStatus(List.of(RoomStatus.active));
		List<Room> rooms = roomManagementService.searchRooms(params, roles);

		allRows = new ArrayList<>(rooms.size());
		for (Room room : rooms) {
			boolean myEvent = preSelectedRoomKeys.contains(room.getKey());
			RoomBooking occupiedBy = occupiedByOther.get(room.getKey());
			AddRoomsRow rowObj = new AddRoomsRow(room, occupiedBy, myEvent, participantCount);

			if (occupiedBy != null) {
				LectureBlock lb = occupiedBy.getLectureBlock();
				if (lb != null) {
					CurriculumElement ce = lb.getCurriculumElement();
					if (ce != null) {
						rowObj.setElementReference(new Reference(ce.getKey(), ce.getDisplayName(), ce.getIdentifier()));
					}
					RepositoryEntry entry = lb.getEntry();
					if (entry != null) {
						rowObj.setCourseReference(new Reference(entry.getKey(), entry.getDisplayname(), entry.getExternalRef()));
					}
				}
			}

			List<RoomBooking> roomDayBookings = dayBookingsByRoom.getOrDefault(room.getKey(), List.of());
			computeAdjacentSlots(rowObj, roomDayBookings);

			allRows.add(rowObj);
		}
	}

	private Map<Long, List<RoomBooking>> loadDayBookingsByRoom() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(startDate);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date dayStart = cal.getTime();
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		Date dayEnd = cal.getTime();

		List<RoomBooking> dayBookings = roomManagementService.getBookings(dayStart, dayEnd);
		Map<Long, List<RoomBooking>> byRoom = new HashMap<>();
		for (RoomBooking booking : dayBookings) {
			boolean isOwnEvent = lectureBlock != null && lectureBlock.getKey() != null
					&& booking.getLectureBlock() != null
					&& booking.getLectureBlock().getKey().equals(lectureBlock.getKey());
			if (!isOwnEvent) {
				byRoom.computeIfAbsent(booking.getRoom().getKey(), k -> new ArrayList<>()).add(booking);
			}
		}
		return byRoom;
	}

	private void computeAdjacentSlots(AddRoomsRow row, List<RoomBooking> roomBookings) {
		// Earlier slot: end of the latest booking that ends at or before startDate
		roomBookings.stream()
				.filter(b -> b.getEndDate() != null && !b.getEndDate().after(startDate))
				.max(Comparator.comparing(RoomBooking::getEndDate))
				.ifPresent(b -> row.setEarlierSlotFrom(b.getEndDate()));

		// Later slot: start of the earliest booking that starts at or after endDate
		roomBookings.stream()
				.filter(b -> b.getStartDate() != null && !b.getStartDate().before(endDate))
				.min(Comparator.comparing(RoomBooking::getStartDate))
				.ifPresent(b -> row.setLaterSlotTo(b.getStartDate()));
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

	private void loadCalendar() {
		calendarEl.setVisible(true);
		List<AddRoomsRow> visibleRows = tableModel.getObjects();
		if (visibleRows == null || visibleRows.isEmpty()) {
			calendarEl.setCalendars(List.of());
			return;
		}

		Set<Long> visibleRoomKeys = visibleRows.stream()
				.map(AddRoomsRow::getKey)
				.collect(Collectors.toSet());

		List<RoomBooking> allBookings = roomManagementService != null
				? roomManagementService.getBookings(null, null)
				: List.of();
		List<RoomBooking> relevant = allBookings.stream()
				.filter(b -> b.getRoom() != null && visibleRoomKeys.contains(b.getRoom().getKey()))
				.toList();

		Map<Long, AddRoomsRow> rowByRoomKey = visibleRows.stream()
				.collect(Collectors.toMap(AddRoomsRow::getKey, r -> r));

		List<KalendarRenderWrapper> wrappers = new ArrayList<>();
		Map<Long, List<RoomBooking>> bookingsByRoom = new LinkedHashMap<>();
		for (AddRoomsRow row : visibleRows) {
			bookingsByRoom.put(row.getKey(), new ArrayList<>());
		}
		for (RoomBooking booking : relevant) {
			bookingsByRoom.computeIfAbsent(booking.getRoom().getKey(), k -> new ArrayList<>()).add(booking);
		}

		for (AddRoomsRow row : visibleRows) {
			String calId = "add.rooms.room." + row.getKey();
			Kalendar calendar = new Kalendar(calId, "Room");
			for (RoomBooking booking : bookingsByRoom.getOrDefault(row.getKey(), List.of())) {
				addCalendarEvent(calendar, booking, rowByRoomKey);
			}
			String displayName = StringHelper.containsNonWhitespace(row.getReference())
					? row.getReference() : (row.getDescription() != null ? row.getDescription() : calId);
			KalendarRenderWrapper wrapper = new KalendarRenderWrapper(calendar, displayName, calId);
			wrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_ONLY);
			wrapper.setPrivateEventsVisible(true);
			String colorCss = row.getRoom().getBuilding() != null
					&& StringHelper.containsNonWhitespace(row.getRoom().getBuilding().getColorCss())
					? row.getRoom().getBuilding().getColorCss()
					: colorService.getDefaultColor();
			wrapper.setCssClass("o_rm_cal_pastel o_color_border " + colorCss);
			wrappers.add(wrapper);
		}

		calendarEl.setCalendars(wrappers);
	}

	private void addCalendarEvent(Kalendar calendar, RoomBooking booking, Map<Long, AddRoomsRow> rowByRoomKey) {
		if (booking.getStartDate() == null || booking.getEndDate() == null) return;
		AddRoomsRow row = rowByRoomKey.get(booking.getRoom().getKey());
		String roomRef = row != null && StringHelper.containsNonWhitespace(row.getReference())
				? row.getReference() : null;
		String blockTitle = booking.getLectureBlock() != null ? booking.getLectureBlock().getTitle() : null;
		String subject;
		if (StringHelper.containsNonWhitespace(roomRef) && StringHelper.containsNonWhitespace(blockTitle)) {
			subject = roomRef + " · " + blockTitle;
		} else if (StringHelper.containsNonWhitespace(roomRef)) {
			subject = roomRef;
		} else if (StringHelper.containsNonWhitespace(blockTitle)) {
			subject = blockTitle;
		} else {
			subject = "";
		}
		String eventId = CodeHelper.getGlobalForeverUniqueID();
		var zStart = DateUtils.toZonedDateTime(booking.getStartDate(), calendarModule.getDefaultZoneId());
		var zEnd = DateUtils.toZonedDateTime(booking.getEndDate(), calendarModule.getDefaultZoneId());
		KalendarEvent event = new KalendarEvent(eventId, null, subject, zStart, zEnd);
		calendar.addEvent(event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == tableEl) {
			if (event instanceof FlexiTableRenderEvent renderEvent
					&& FlexiTableRenderEvent.CHANGE_RENDER_TYPE.equals(renderEvent.getCommand())) {
				if (renderEvent.getRendererType() == FlexiTableRendererType.classic) {
					calendarEl.setVisible(false);
					applyFilters();
				} else {
					loadCalendar();
				}
			} else if (event instanceof FlexiTableFilterTabEvent || event instanceof FlexiTableSearchEvent) {
				if (tableEl.getRendererType() == FlexiTableRendererType.classic) {
					applyFilters();
				} else {
					loadCalendar();
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// Pre-selected rooms are read-only — always included regardless of checkbox state
		Map<Long, Room> resultRooms = new LinkedHashMap<>();
		for (AddRoomsRow row : allRows) {
			if (preSelectedRoomKeys.contains(row.getKey())) {
				resultRooms.put(row.getKey(), row.getRoom());
			}
		}
		// Add any additional rooms the user selected
		List<AddRoomsRow> currentRows = tableModel.getObjects();
		for (Integer idx : tableEl.getMultiSelectedIndex()) {
			if (idx < currentRows.size()) {
				Room room = currentRows.get(idx).getRoom();
				resultRooms.putIfAbsent(room.getKey(), room);
			}
		}
		selectedRooms.clear();
		selectedRooms.addAll(resultRooms.values());
		List<String> selectedKeys = resultRooms.keySet().stream()
				.map(Object::toString)
				.collect(Collectors.toList());
		fireEvent(ureq, new ObjectSelectionBrowserEvent(selectedKeys));
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	private class PreSelectedRowsCssDelegate extends DefaultFlexiTableCssDelegate {
		@Override
		public String getRowCssClass(FlexiTableRendererType type, int pos) {
			List<AddRoomsRow> rows = tableModel.getObjects();
			if (pos >= 0 && pos < rows.size() && preSelectedRoomKeys.contains(rows.get(pos).getKey())) {
				return "o_rm_room_preselected";
			}
			return null;
		}
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

	private static class TimeSlotCellRenderer implements FlexiCellRenderer {

		private final Date boundaryDate;
		private final boolean isBefore;
		private final Formatter formatter;

		TimeSlotCellRenderer(Date boundaryDate, boolean isBefore, Locale locale) {
			this.boundaryDate = boundaryDate;
			this.isBefore = isBefore;
			this.formatter = Formatter.getInstance(locale);
		}

		@Override
		public void render(Renderer renderer, StringOutput target, Object cellValue,
				int row, FlexiTableComponent source, URLBuilder ubu, Translator translator) {
			if (!(cellValue instanceof AddRoomsRow roomRow)) return;
			if (isBefore) {
				Date from = roomRow.getEarlierSlotFrom();
				if (from != null) {
					target.appendHtmlEscaped(formatter.formatTime(from))
							.append(" – ")
							.appendHtmlEscaped(formatter.formatTime(boundaryDate));
				}
			} else {
				Date to = roomRow.getLaterSlotTo();
				if (to != null) {
					target.appendHtmlEscaped(formatter.formatTime(boundaryDate))
							.append(" – ")
							.appendHtmlEscaped(formatter.formatTime(to));
				}
			}
		}
	}

}
