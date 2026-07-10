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

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
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
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateWithDayFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DetailsToggleEvent;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionDelegateCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableElementImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRenderEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TimeFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableOneClickSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.scope.DateScope;
import org.olat.core.gui.components.scope.FormDateScopeSelection;
import org.olat.core.gui.components.scope.ScopeFactory;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings.CalloutOrientation;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementRefImpl;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.Reference;
import org.olat.modules.lecture.ui.LectureListRepositoryController;
import org.olat.modules.lecture.ui.component.LectureBlockStatusCellRenderer;
import org.olat.modules.lecture.ui.component.ReferenceRenderer;
import org.olat.modules.roommanagement.Building;
import org.olat.modules.roommanagement.Room;
import org.olat.modules.roommanagement.RoomBooking;
import org.olat.modules.roommanagement.RoomManagementService;
import org.olat.modules.roommanagement.RoomStatus;
import org.olat.core.util.DateRange;
import org.olat.modules.roommanagement.model.SearchBuildingParameters;
import org.olat.modules.roommanagement.model.SearchRoomParameters;
import org.olat.NewControllerFactory;
import org.olat.modules.roommanagement.ui.RoomSchedulingDataModel.SchedulingCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 12 Jun 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RoomSchedulingController extends FormBasicController implements FlexiTableComponentDelegate, FlexiTableCssDelegate {

	private static final String FILTER_WITH_WARNINGS = "withWarnings";
	private static final String FILTER_WITH_WARNINGS_ON = "on";
	private static final String FILTER_BUILDINGS = "buildings";
	private static final String FILTER_ROOMS = "rooms";
	private static final String TAB_ID_ALL = "all";
	private static final String TAB_ID_TODAY = "today";
	private static final String TAB_ID_UPCOMING = "upcoming";
	private static final String TAB_ID_WITH_WARNINGS = "withWarnings";
	private static final String TOGGLE_DETAILS_CMD = "toggle-details";

	private FormDateScopeSelection scopeEl;
	private FlexiTableElement tableEl;
	private RoomSchedulingDataModel dataModel;
	private FullCalendarElement calendarEl;

	private CloseableCalloutWindowController calloutCtrl;

	private FlexiFiltersTab tabAll;
	private FlexiFiltersTab tabToday;
	private FlexiFiltersTab tabUpcoming;
	private FlexiFiltersTab tabWithWarnings;

	private final Roles roles;

	@Autowired
	private CalendarModule calendarModule;
	@Autowired
	private ColorService colorService;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private RoomManagementService roomManagementService;

	public RoomSchedulingController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "room_scheduling");
		setTranslator(Util.createPackageTranslator(LectureListRepositoryController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(CalendarManager.class, getLocale(), getTranslator()));

		roles = ureq.getUserSession().getRoles();
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		List<DateScope> scopes = ScopeFactory.dateScopesBuilder(getLocale())
				.todayAndUpcoming()
				.lastMonths(3)
				.build();
		scopeEl = uifactory.addDateScopeSelection(getWindowControl(), "scope", null, formLayout, scopes, getLocale());

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();

		DefaultFlexiColumnModel warningsCol = new DefaultFlexiColumnModel(SchedulingCols.warnings, new WarningsCellRenderer());
		warningsCol.setIconHeader("o_icon o_icon_warning");
		warningsCol.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(warningsCol);

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SchedulingCols.date, TOGGLE_DETAILS_CMD,
				new DateWithDayFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SchedulingCols.from, new TimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SchedulingCols.to, new TimeFlexiCellRenderer(getLocale())));

		DefaultFlexiColumnModel refCol = new DefaultFlexiColumnModel(SchedulingCols.reference);
		refCol.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(refCol);

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SchedulingCols.description));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, SchedulingCols.status, new RoomStatusCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SchedulingCols.building, new BuildingCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SchedulingCols.event));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, SchedulingCols.statusEvent,
				new LectureBlockStatusCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, SchedulingCols.element, "openElement", new ReferenceRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, SchedulingCols.course, "openCourse", new ReferenceRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SchedulingCols.numParticipants));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SchedulingCols.numSeats));

		dataModel = new RoomSchedulingDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "schedulings", dataModel, 20, 
				false, getTranslator(), formLayout);
		tableEl.setSearchEnabled(true);
		tableEl.setAndLoadPersistedPreferences(ureq, "room-management-scheduling");
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.external, FlexiTableRendererType.classic);
		tableEl.setRendererType(FlexiTableRendererType.classic);
		tableEl.setExternalRenderer(new RoomCalendarRenderer(), "o_icon_calendar o_icon-lg");
		tableEl.getExternalTypeButton().setTitle(translate("room.view.calendar"));
		tableEl.setCssDelegate(this);

		calendarEl = new FullCalendarElement(ureq, RoomCalendarRenderer.CALENDAR_ITEM_NAME, new ArrayList<>(), getTranslator());
		calendarEl.setShowEventDuration(true);
		formLayout.add(RoomCalendarRenderer.CALENDAR_ITEM_NAME, calendarEl);

		VelocityContainer detailsVC = createVelocityContainer("room_scheduling_details");
		tableEl.setDetailsRenderer(detailsVC, this);

		initFilters();
		initFilterTabs(ureq);
	}

	@Override
	public boolean isDetailsRow(int row, Object rowObject) {
		return true;
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> components = new ArrayList<>();
		if (rowObject instanceof RoomSchedulingRow schedulingRow && schedulingRow.getDetailsController() != null) {
			components.add(schedulingRow.getDetailsController().getInitialFormItem().getComponent());
		}
		return components;
	}

	@Override
	public String getWrapperCssClass(FlexiTableRendererType type) {
		return null;
	}

	@Override
	public String getTableCssClass(FlexiTableRendererType type) {
		return null;
	}

	@Override
	public String getRowCssClass(FlexiTableRendererType type, int pos) {
		RoomSchedulingRow row = dataModel.getObject(pos);
		return row != null && !row.getWarnings().isEmpty() ? "warning" : null;
	}

	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();

		SelectionValues warningsValues = new SelectionValues();
		warningsValues.add(SelectionValues.entry(FILTER_WITH_WARNINGS_ON, translate("room.scheduling.filter.with.warnings")));
		filters.add(new FlexiTableOneClickSelectionFilter(translate("room.scheduling.filter.with.warnings"),
				FILTER_WITH_WARNINGS, warningsValues, true));

		List<RoomStatus> filterStatuses = List.of(RoomStatus.active, RoomStatus.inactive);
		initBuildingFilter(filters, filterStatuses);
		initRoomFilter(filters, filterStatuses);

		tableEl.setFilters(true, filters, false, false);
	}

	private void initBuildingFilter(List<FlexiTableExtendedFilter> filters, List<RoomStatus> statuses) {
		SearchBuildingParameters buildingParams = new SearchBuildingParameters();
		buildingParams.setStatus(statuses);
		List<Building> buildings = roomManagementService.searchBuildings(buildingParams, roles);
		SelectionValues buildingValues = RoomUIHelper.buildBuildingFilterValues(buildings, getTranslator());
		if (!buildingValues.isEmpty()) {
			filters.add(new FlexiTableMultiSelectionFilter(translate("room.scheduling.filter.buildings"),
					FILTER_BUILDINGS, buildingValues, true));
		}
	}

	private void initRoomFilter(List<FlexiTableExtendedFilter> filters, List<RoomStatus> statuses) {
		SearchRoomParameters roomParams = new SearchRoomParameters();
		roomParams.setStatus(statuses);
		List<Room> rooms = roomManagementService.searchRooms(roomParams, roles);
		SelectionValues roomValues = RoomUIHelper.buildRoomFilterValues(rooms, getTranslator());
		if (!roomValues.isEmpty()) {
			filters.add(new FlexiTableMultiSelectionFilter(translate("room.scheduling.filter.rooms"),
					FILTER_ROOMS, roomValues, true));
		}
	}

	private void initFilterTabs(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>();

		tabAll = FlexiFiltersTabFactory.tab(TAB_ID_ALL, translate("room.scheduling.tab.all"), TabSelectionBehavior.reloadData);
		tabs.add(tabAll);

		tabToday = FlexiFiltersTabFactory.tab(TAB_ID_TODAY, translate("room.scheduling.tab.today"), TabSelectionBehavior.reloadData);
		tabs.add(tabToday);

		tabUpcoming = FlexiFiltersTabFactory.tab(TAB_ID_UPCOMING, translate("room.scheduling.tab.upcoming"), TabSelectionBehavior.reloadData);
		tabs.add(tabUpcoming);

		tabWithWarnings = FlexiFiltersTabFactory.tab(TAB_ID_WITH_WARNINGS, translate("room.scheduling.tab.with.warnings"), TabSelectionBehavior.reloadData);
		tabs.add(tabWithWarnings);

		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, tabAll);
	}

	private List<RoomBooking> loadFilteredBookings() {
		DateRange dateRange = scopeEl.isSelected() ? scopeEl.getSelectedDateRange() : null;
		Date from = dateRange != null ? dateRange.getFrom() : null;
		Date to = dateRange != null ? dateRange.getTo() : null;

		List<RoomBooking> bookings = roomManagementService.getBookings(from, to);

		FlexiFiltersTab selectedTab = tableEl.getSelectedFilterTab();
		if (selectedTab == tabToday) {
			Date startOfToday = DateUtils.getStartOfDay(new Date());
			Date endOfToday = DateUtils.getEndOfDay(new Date());
			bookings = bookings.stream()
					.filter(b -> b.getStartDate() != null
							&& !b.getStartDate().before(startOfToday)
							&& !b.getStartDate().after(endOfToday))
					.collect(Collectors.toList());
		} else if (selectedTab == tabUpcoming) {
			Date tomorrow = DateUtils.getStartOfDay(DateUtils.addDays(new Date(), 1));
			bookings = bookings.stream()
					.filter(b -> b.getStartDate() != null && !b.getStartDate().before(tomorrow))
					.collect(Collectors.toList());
		}

		Set<Long> selectedBuildingKeys = getSelectedLongKeys(FILTER_BUILDINGS);
		Set<Long> selectedRoomKeys = getSelectedLongKeys(FILTER_ROOMS);

		if (!selectedBuildingKeys.isEmpty()) {
			bookings = bookings.stream()
					.filter(b -> b.getRoom() != null && b.getRoom().getBuilding() != null
							&& selectedBuildingKeys.contains(b.getRoom().getBuilding().getKey()))
					.collect(Collectors.toList());
		}
		if (!selectedRoomKeys.isEmpty()) {
			bookings = bookings.stream()
					.filter(b -> b.getRoom() != null
							&& selectedRoomKeys.contains(b.getRoom().getKey()))
					.collect(Collectors.toList());
		}

		return bookings;
	}

	private void loadModel() {
		FlexiFiltersTab selectedTab = tableEl.getSelectedFilterTab();
		List<RoomBooking> bookings = loadFilteredBookings();

		List<RoomSchedulingRow> rows = new ArrayList<>(bookings.size());
		for (RoomBooking booking : bookings) {
			rows.add(forgeRow(booking));
		}
		computeWarnings(rows);

		boolean withWarningsFilterActive = tableEl.getFilters() != null && tableEl.getFilters().stream()
				.anyMatch(f -> FILTER_WITH_WARNINGS.equals(f.getFilter()) && f.isSelected());
		if (selectedTab == tabWithWarnings || withWarningsFilterActive) {
			rows = rows.stream().filter(r -> !r.getWarnings().isEmpty()).collect(Collectors.toList());
		}

		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	private void loadCalendar() {
		FlexiFiltersTab selectedTab = tableEl.getSelectedFilterTab();
		List<RoomBooking> bookings = loadFilteredBookings();

		Set<Long> warningKeys = RoomUIHelper.computeBookingKeysWithWarnings(bookings, lectureService);

		boolean withWarningsFilterActive = tableEl.getFilters() != null && tableEl.getFilters().stream()
				.anyMatch(f -> FILTER_WITH_WARNINGS.equals(f.getFilter()) && f.isSelected());
		if (selectedTab == tabWithWarnings || withWarningsFilterActive) {
			bookings = bookings.stream()
					.filter(b -> warningKeys.contains(b.getKey()))
					.collect(Collectors.toList());
		}

		Map<Long, Building> buildingByKey = new LinkedHashMap<>();
		Map<Long, List<RoomBooking>> bookingsByBuildingKey = new LinkedHashMap<>();
		List<RoomBooking> bookingsWithoutBuilding = new ArrayList<>();

		for (RoomBooking booking : bookings) {
			Room room = booking.getRoom();
			if (room != null && room.getBuilding() != null) {
				Building building = room.getBuilding();
				buildingByKey.put(building.getKey(), building);
				bookingsByBuildingKey.computeIfAbsent(building.getKey(), k -> new ArrayList<>()).add(booking);
			} else {
				bookingsWithoutBuilding.add(booking);
			}
		}

		List<KalendarRenderWrapper> wrappers = new ArrayList<>();

		for (Map.Entry<Long, Building> entry : buildingByKey.entrySet()) {
			Building building = entry.getValue();
			String calId = "scheduling.building." + building.getKey();
			Kalendar calendar = new Kalendar(calId, "Room");
			for (RoomBooking booking : bookingsByBuildingKey.getOrDefault(building.getKey(), List.of())) {
				addCalendarEvent(calendar, booking, warningKeys);
			}
			String displayName = StringHelper.containsNonWhitespace(building.getExternalRef())
					? building.getExternalRef() : building.getDescription();
			if (!StringHelper.containsNonWhitespace(displayName)) {
				displayName = calId;
			}
			KalendarRenderWrapper wrapper = new KalendarRenderWrapper(calendar, displayName, calId);
			wrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_ONLY);
			wrapper.setPrivateEventsVisible(true);
			String colorCss = StringHelper.containsNonWhitespace(building.getColorCss())
					? building.getColorCss() : colorService.getDefaultColor();
			wrapper.setCssClass("o_rm_cal_pastel o_color_border " + colorCss);
			wrappers.add(wrapper);
		}

		if (!bookingsWithoutBuilding.isEmpty()) {
			String calId = "scheduling.nobuilding";
			Kalendar calendar = new Kalendar(calId, "Room");
			for (RoomBooking booking : bookingsWithoutBuilding) {
				addCalendarEvent(calendar, booking, warningKeys);
			}
			KalendarRenderWrapper wrapper = new KalendarRenderWrapper(calendar, "", calId);
			wrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_ONLY);
			wrapper.setPrivateEventsVisible(true);
			wrapper.setCssClass("o_rm_cal_pastel o_color_border " + colorService.getDefaultColor());
			wrappers.add(wrapper);
		}

		calendarEl.setCalendars(wrappers);
	}

	private void addCalendarEvent(Kalendar calendar, RoomBooking booking, Set<Long> warningKeys) {
		if (booking.getStartDate() == null || booking.getEndDate() == null) return;
		Room room = booking.getRoom();
		String roomRef = room != null && StringHelper.containsNonWhitespace(room.getExternalRef())
				? room.getExternalRef() : (room != null ? room.getDescription() : null);
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
		ZonedDateTime zStart = DateUtils.toZonedDateTime(booking.getStartDate(), calendarModule.getDefaultZoneId());
		ZonedDateTime zEnd = DateUtils.toZonedDateTime(booking.getEndDate(), calendarModule.getDefaultZoneId());
		KalendarEvent event = new KalendarEvent(eventId, null, subject, zStart, zEnd);
		if (warningKeys.contains(booking.getKey())) {
			event.setComment("warning");
		}
		calendar.addEvent(event);
	}

	private void computeWarnings(List<RoomSchedulingRow> rows) {
		// Double-booking: same room, overlapping time periods
		Map<Long, List<RoomSchedulingRow>> byRoom = rows.stream()
				.filter(r -> r.getBooking().getRoom() != null)
				.collect(Collectors.groupingBy(r -> r.getBooking().getRoom().getKey()));
		Set<Long> doubleBookedKeys = new HashSet<>();
		for (List<RoomSchedulingRow> roomRows : byRoom.values()) {
			for (int i = 0; i < roomRows.size(); i++) {
				for (int j = i + 1; j < roomRows.size(); j++) {
					RoomBooking a = roomRows.get(i).getBooking();
					RoomBooking b = roomRows.get(j).getBooking();
					if (bookingsOverlap(a, b)) {
						doubleBookedKeys.add(a.getKey());
						doubleBookedKeys.add(b.getKey());
					}
				}
			}
		}

		// Overbooked: total seats for all rooms on the same event < participants
		Map<Long, List<RoomSchedulingRow>> byLectureBlock = rows.stream()
				.filter(r -> r.getBooking().getLectureBlock() != null)
				.collect(Collectors.groupingBy(r -> r.getBooking().getLectureBlock().getKey()));
		
		Map<Long, Integer> participantCountByLb = new HashMap<>();
		Map<Long, Integer> totalSeatsByLb = new HashMap<>();
		for (Map.Entry<Long, List<RoomSchedulingRow>> entry : byLectureBlock.entrySet()) {
			LectureBlock lb = entry.getValue().get(0).getBooking().getLectureBlock();
			int participants = lectureService.getParticipants(lb).size();
			participantCountByLb.put(entry.getKey(), participants);
			int totalSeats = entry.getValue().stream()
					.filter(r -> r.getBooking().getRoom() != null && r.getBooking().getRoom().getSeats() != null)
					.mapToInt(r -> r.getBooking().getRoom().getSeats())
					.sum();
			totalSeatsByLb.put(entry.getKey(), totalSeats);
			for (RoomSchedulingRow r : entry.getValue()) {
				r.setNumParticipants(participants);
			}
		}

		// Assign warnings per row
		for (RoomSchedulingRow row : rows) {
			RoomBooking booking = row.getBooking();
			Room room = booking.getRoom();
			List<String> warnings = new ArrayList<>();

			if (room != null && RoomStatus.inactive == room.getStatus()) {
				String ref = StringHelper.containsNonWhitespace(room.getExternalRef()) ? room.getExternalRef() : room.getDescription();
				warnings.add(translate("room.scheduling.warning.inactive", ref));
			}
			if (doubleBookedKeys.contains(booking.getKey())) {
				String ref = room != null && StringHelper.containsNonWhitespace(room.getExternalRef()) ? room.getExternalRef() : (room != null ? room.getDescription() : "");
				warnings.add(translate("room.scheduling.warning.double.booked", ref));
			}
			LectureBlock lb = booking.getLectureBlock();
			if (lb != null) {
				int participants = participantCountByLb.getOrDefault(lb.getKey(), 0);
				int seats = totalSeatsByLb.getOrDefault(lb.getKey(), 0);
				if (participants > seats) {
					warnings.add(translate("room.scheduling.warning.overbooked"));
				}
			}

			row.setWarnings(warnings);
		}
	}

	private static boolean bookingsOverlap(RoomBooking a, RoomBooking b) {
		return RoomUIHelper.bookingsOverlap(a, b);
	}

	private Set<Long> getSelectedLongKeys(String filterId) {
		List<FlexiTableFilter> filters = tableEl.getFilters();
		if (filters == null) return Set.of();
		return filters.stream()
				.filter(f -> filterId.equals(f.getFilter()) && f instanceof FlexiTableMultiSelectionFilter)
				.map(f -> ((FlexiTableMultiSelectionFilter) f).getValues())
				.filter(values -> values != null)
				.flatMap(List::stream)
				.map(Long::valueOf)
				.collect(Collectors.toSet());
	}

	private RoomSchedulingRow forgeRow(RoomBooking booking) {
		RoomSchedulingRow row = new RoomSchedulingRow(booking);

		// Room reference link
		Room room = booking.getRoom();
		if (room != null) {
			String roomRef = StringHelper.containsNonWhitespace(room.getExternalRef()) ? room.getExternalRef() : room.getDescription();
			FormLink roomLink = uifactory.addFormLink("room_" + booking.getKey(), "selectRoom",
					StringHelper.escapeHtml(roomRef), null, null, Link.LINK | Link.NONTRANSLATED);
			roomLink.setUserObject(row);
			row.setRoomLink(roomLink);

			// Building link
			Building building = room.getBuilding();
			if (building != null) {
				String buildingRef = StringHelper.containsNonWhitespace(building.getExternalRef()) ? building.getExternalRef() : building.getDescription();
				FormLink buildingLink = uifactory.addFormLink("bld_" + booking.getKey(), "building",
						StringHelper.escapeHtml(buildingRef), null, null, Link.LINK | Link.NONTRANSLATED);
				buildingLink.setUserObject(building);
				row.setBuildingLink(buildingLink);
			}
		}

		// Curriculum element and course references
		LectureBlock lb = booking.getLectureBlock();
		if (lb != null) {
			CurriculumElement ce = lb.getCurriculumElement();
			if (ce != null) {
				row.setElementReference(new Reference(ce.getKey(), ce.getDisplayName(), ce.getIdentifier()));
			}
			var entry = lb.getEntry();
			if (entry != null) {
				row.setCourseReference(new Reference(entry.getKey(), entry.getDisplayname(), entry.getExternalRef()));
			}
		}

		// Event link
		if (lb != null) {
			String eventText = StringHelper.escapeHtml(lb.getTitle());
			if (StringHelper.containsNonWhitespace(lb.getExternalRef())) {
				eventText += " · <small>" + StringHelper.escapeHtml(lb.getExternalRef()) + "</small>";
			}
			FormLink eventLink = uifactory.addFormLink("event_" + booking.getKey(), "openEvent",
					eventText, null, null, Link.LINK | Link.NONTRANSLATED);
			eventLink.setUrl(BusinessControlFactory.getInstance().getRelativeURLFromBusinessPathString(RoomUIHelper.getEventsBusinessPath(lb)));
			eventLink.setUserObject(row);
			row.setEventLink(eventLink);
		}

		return row;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == scopeEl) {
			if (tableEl.getRendererType() == FlexiTableRendererType.classic) {
				loadModel();
			} else {
				loadCalendar();
			}
		} else if (source == tableEl) {
			if (event instanceof FlexiTableRenderEvent renderEvent
					&& FlexiTableRenderEvent.CHANGE_RENDER_TYPE.equals(renderEvent.getCommand())) {
				if (renderEvent.getRendererType() == FlexiTableRendererType.classic) {
					loadModel();
				} else {
					loadCalendar();
				}
			} else if (event instanceof FlexiTableFilterTabEvent || event instanceof FlexiTableSearchEvent) {
				if (tableEl.getRendererType() == FlexiTableRendererType.classic) {
					loadModel();
				} else {
					loadCalendar();
				}
			} else if (event instanceof DetailsToggleEvent toggleEvent) {
				RoomSchedulingRow row = dataModel.getObject(toggleEvent.getRowIndex());
				if (toggleEvent.isVisible()) {
					doOpenDetails(ureq, row, toggleEvent.getRowIndex());
				} else {
					doCloseDetails(row);
				}
			} else if (event instanceof SelectionEvent se && TOGGLE_DETAILS_CMD.equals(se.getCommand())) {
				RoomSchedulingRow row = dataModel.getObject(se.getIndex());
				if (tableEl.isDetailsExpended(se.getIndex())) {
					doCloseDetails(row);
					tableEl.collapseDetails(se.getIndex());
				} else {
					doOpenDetails(ureq, row, se.getIndex());
				}
			} else if (event instanceof SelectionEvent se && "openElement".equals(se.getCommand())) {
				RoomSchedulingRow row = dataModel.getObject(se.getIndex());
				doOpenElement(ureq, row);
			} else if (event instanceof SelectionEvent se && "openCourse".equals(se.getCommand())) {
				RoomSchedulingRow row = dataModel.getObject(se.getIndex());
				doOpenCourse(ureq, row);
			} else if (event instanceof SelectionEvent se && WarningsCellRenderer.CMD_WARNINGS.equals(se.getCommand())) {
				String targetId = WarningsCellRenderer.getId(se.getIndex());
				RoomSchedulingRow row = dataModel.getObject(se.getIndex());
				doOpenWarningsCallout(ureq, row, targetId);
			}
		} else if (source instanceof FormLink link && "building".equals(link.getCmd())) {
			doOpenBuilding(ureq, link);
		} else if (source instanceof FormLink link && "selectRoom".equals(link.getCmd())) {
			doOpenRoom(ureq, link);
		} else if (source instanceof FormLink link && "openEvent".equals(link.getCmd())) {
			doOpenEvent(ureq, link);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == calloutCtrl) {
			calloutCtrl.deactivate();
			removeAsListenerAndDispose(calloutCtrl);
			calloutCtrl = null;
		}
		super.event(ureq, source, event);
	}

	private void doOpenWarningsCallout(UserRequest ureq, RoomSchedulingRow row, String targetId) {
		removeAsListenerAndDispose(calloutCtrl);
		RoomSchedulingWarningsCalloutController warningsCtrl =
				new RoomSchedulingWarningsCalloutController(ureq, getWindowControl(), row.getWarnings());
		listenTo(warningsCtrl);
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				warningsCtrl.getInitialComponent(), targetId, "", true, "",
				new CalloutSettings(true, CalloutOrientation.bottom, false, "", true));
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}

	private void doOpenDetails(UserRequest ureq, RoomSchedulingRow row, int rowIndex) {
		doCloseDetails(row);
		RoomSchedulingDetailsController detailsCtrl = new RoomSchedulingDetailsController(ureq, getWindowControl(), row, mainForm);
		listenTo(detailsCtrl);
		row.setDetailsController(detailsCtrl);
		flc.add(detailsCtrl.getInitialFormItem());
		tableEl.expandDetails(rowIndex);
	}

	private void doCloseDetails(RoomSchedulingRow row) {
		if (row.getDetailsController() == null) return;
		removeAsListenerAndDispose(row.getDetailsController());
		flc.remove(row.getDetailsController().getInitialFormItem());
		row.setDetailsController(null);
	}

	private void doOpenBuilding(UserRequest ureq, FormLink link) {
		if (link.getUserObject() instanceof Building building) {
			fireEvent(ureq, new OpenBuildingEvent(building.getKey()));
		}
	}

	private void doOpenRoom(UserRequest ureq, FormLink link) {
		if (link.getUserObject() instanceof RoomSchedulingRow row && row.getBooking().getRoom() != null) {
			fireEvent(ureq, new OpenRoomEvent(row.getBooking().getRoom().getKey()));
		}
	}

	private void doOpenEvent(UserRequest ureq, FormLink link) {
		if (link.getUserObject() instanceof RoomSchedulingRow row) {
			LectureBlock lb = row.getBooking().getLectureBlock();
			if (lb != null) {
				NewControllerFactory.getInstance().launch(RoomUIHelper.getEventsBusinessPath(lb), ureq, getWindowControl());
			}
		}
	}

	private void doOpenElement(UserRequest ureq, RoomSchedulingRow row) {
		if (row.getElementReference() == null) return;
		CurriculumElement el = curriculumService.getCurriculumElement(new CurriculumElementRefImpl(row.getElementReference().key()));
		if (el == null) return;
		String path = "[CurriculumAdmin:0][Curriculums:0][Curriculum:" + el.getCurriculum().getKey()
				+ "][CurriculumElement:" + el.getKey() + "]";
		NewControllerFactory.getInstance().launch(path, ureq, getWindowControl());
	}

	private void doOpenCourse(UserRequest ureq, RoomSchedulingRow row) {
		if (row.getCourseReference() == null || row.getCourseReference().key() == null) return;
		String path = "[RepositoryEntry:" + row.getCourseReference().key() + "]";
		NewControllerFactory.getInstance().launch(path, ureq, getWindowControl());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// read-only table
	}

	private static final class WarningsCellRenderer implements FlexiCellRenderer, ActionDelegateCellRenderer {

		static final String CMD_WARNINGS = "openWarnings";
		private static final List<String> ACTIONS = List.of(CMD_WARNINGS);

		static String getId(int row) {
			return "o_c" + CMD_WARNINGS + "_" + row;
		}

		@Override
		public List<String> getActions() {
			return ACTIONS;
		}

		@Override
		public void render(Renderer renderer, StringOutput target, Object cellValue,
				int row, FlexiTableComponent source, URLBuilder ubu, Translator translator) {
			if (!(cellValue instanceof RoomSchedulingRow schedulingRow)) return;
			if (schedulingRow.getWarnings().isEmpty()) return;

			FlexiTableElementImpl ftE = source.getFormItem();
			String id = source.getFormDispatchId();
			Form rootForm = ftE.getRootForm();
			String actionId = getId(row);

			NameValuePair pair = new NameValuePair(CMD_WARNINGS, Integer.toString(row));
			String jsCode = FormJSHelper.getXHRFnCallFor(rootForm, id, 1, false, true, false, pair);
			target.append("<a id=\"").append(actionId).append("\" href=\"javascript:;\" onclick=\"")
				  .append(jsCode).append("; return false;\">")
				  .append("<i class=\"o_icon o_icon_warn\"> </i>")
				  .append("</a>");
		}
	}

	private static final class BuildingCellRenderer extends AbstractBuildingCellRenderer {
		@Override
		protected FormLink getBuildingLink(Object cellValue) {
			return cellValue instanceof RoomSchedulingRow schedulingRow ? schedulingRow.getBuildingLink() : null;
		}
	}
}
