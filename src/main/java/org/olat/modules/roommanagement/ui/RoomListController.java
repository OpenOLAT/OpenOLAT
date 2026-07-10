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
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DetailsToggleEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRenderEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.roommanagement.Building;
import org.olat.modules.roommanagement.Room;
import org.olat.modules.roommanagement.RoomBooking;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.roommanagement.RoomManagementService;
import org.olat.modules.roommanagement.RoomRef;
import org.olat.modules.roommanagement.RoomStatus;
import org.olat.modules.roommanagement.model.RoomRefImpl;
import org.olat.modules.roommanagement.model.SearchBuildingParameters;
import org.olat.modules.roommanagement.model.SearchRoomParameters;
import org.olat.modules.roommanagement.ui.RoomListDataModel.RoomCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 5 Jun 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RoomListController extends FormBasicController implements FlexiTableComponentDelegate, Activateable2 {

	private static final String FILTER_STATUS = "status";
	private static final String FILTER_BUILDINGS = "buildings";
	private static final String FILTER_ROOMS = "rooms";
	private static final String TAB_ID_ALL = "all";
	private static final String TAB_ID_RELEVANT = "relevant";
	private static final String TAB_ID_DELETED = "deleted";
	private static final String TOGGLE_DETAILS_CMD = "toggle-details";

	private FormLink createRoomButton;
	private CloseableModalController cmc;
	private EditRoomController editRoomCtrl;
	private ToolsController toolsCtrl;
	private CloseableCalloutWindowController toolsCalloutWindowCtrl;
	private DialogBoxController confirmDeactivateDialog;
	private DialogBoxController confirmDeleteDialog;
	private FlexiTableElement tableEl;
	private RoomListDataModel dataModel;
	private FullCalendarElement calendarEl;
	private RoomRow expandedRow;

	private FlexiFiltersTab tabAll;
	private FlexiFiltersTab tabRelevant;
	private FlexiFiltersTab tabDeleted;

	private final Roles roles;
	private final boolean readOnly;
	private final BreadcrumbedStackedPanel stackPanel;

	@Autowired
	private CalendarModule calendarModule;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private RoomManagementService roomManagementService;
	@Autowired
	private ColorService colorService;

	public RoomListController(UserRequest ureq, WindowControl wControl, BreadcrumbedStackedPanel stackPanel) {
		this(ureq, wControl, stackPanel, false);
	}

	public RoomListController(UserRequest ureq, WindowControl wControl, BreadcrumbedStackedPanel stackPanel, boolean readOnly) {
		super(ureq, wControl, "rooms_admin");
		setTranslator(Util.createPackageTranslator(CalendarManager.class, ureq.getLocale(), getTranslator()));
		roles = ureq.getUserSession().getRoles();
		this.stackPanel = stackPanel;
		this.readOnly = readOnly;
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		createRoomButton = uifactory.addFormLink("create", formLayout, Link.BUTTON);
		createRoomButton.setIconLeftCSS("o_icon o_icon_add");
		createRoomButton.setVisible(!readOnly);

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		DefaultFlexiColumnModel refCol = new DefaultFlexiColumnModel(RoomCols.reference, TOGGLE_DETAILS_CMD);
		refCol.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(refCol);

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RoomCols.description));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RoomCols.status, new RoomStatusCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RoomCols.seats));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RoomCols.additionalInfo,
				new TruncatedInfoCellRenderer(
						cellValue -> ((RoomRow) cellValue).getRoom().getRoomInfo(),
						cellValue -> ((RoomRow) cellValue).getAdditionalInfoLink())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RoomCols.adminInfo,
				new TruncatedInfoCellRenderer(
						cellValue -> ((RoomRow) cellValue).getRoom().getAdminInfo(),
						cellValue -> ((RoomRow) cellValue).getAdminInfoLink())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RoomCols.building, new BuildingCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RoomCols.occupancyRate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RoomCols.nextEvent));
		DefaultFlexiColumnModel calendarIconCol = new DefaultFlexiColumnModel(RoomCols.calendarIcon);
		calendarIconCol.setHeaderTooltip(translate("room.calendar.title"));
		calendarIconCol.setIconHeader(RoomCols.calendarIcon.iconHeader());
		columnsModel.addFlexiColumnModel(calendarIconCol);

		DefaultFlexiColumnModel detailsIconCol = new DefaultFlexiColumnModel(RoomCols.detailsIcon);
		detailsIconCol.setHeaderTooltip(translate("room.detail.open.details"));
		detailsIconCol.setIconHeader(RoomCols.detailsIcon.iconHeader());
		columnsModel.addFlexiColumnModel(detailsIconCol);
		if (!readOnly) {
			columnsModel.addFlexiColumnModel(new ActionsColumnModel(RoomCols.tools));
		}

		dataModel = new RoomListDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "rooms", dataModel, 20, false,
				getTranslator(), formLayout);
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setSearchEnabled(true);
		tableEl.setAndLoadPersistedPreferences(ureq, "room-management-rooms");

		tableEl.setAvailableRendererTypes(FlexiTableRendererType.external, FlexiTableRendererType.classic);
		tableEl.setRendererType(FlexiTableRendererType.classic);
		tableEl.setExternalRenderer(new RoomCalendarRenderer(), "o_icon_calendar o_icon-lg");
		tableEl.getExternalTypeButton().setTitle(translate("room.view.calendar"));

		VelocityContainer detailsVC = createVelocityContainer("room_details");
		tableEl.setDetailsRenderer(detailsVC, this);

		calendarEl = new FullCalendarElement(ureq, RoomCalendarRenderer.CALENDAR_ITEM_NAME, new ArrayList<>(), getTranslator());
		calendarEl.setShowEventDuration(true);
		formLayout.add(RoomCalendarRenderer.CALENDAR_ITEM_NAME, calendarEl);

		initFilters();
		initFilterTabs(ureq);
	}

	@Override
	public boolean isDetailsRow(int row, Object rowObject) {
		return true;
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		if (rowObject instanceof RoomRow roomRow && roomRow.getDetailsController() != null) {
			return List.of(roomRow.getDetailsController().getInitialFormItem().getComponent());
		}
		return List.of();
	}

	private void initFilters() {
		tableEl.setFilters(true, buildFilters(List.of(RoomStatus.active)), false, false);
	}

	private List<FlexiTableExtendedFilter> buildFilters(List<RoomStatus> tabStatuses) {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();

		SelectionValues statusValues = new SelectionValues();
		statusValues.add(SelectionValues.entry(RoomStatus.active.name(), translate("building.status.active")));
		statusValues.add(SelectionValues.entry(RoomStatus.inactive.name(), translate("building.status.inactive")));
		if (!readOnly) {
			statusValues.add(SelectionValues.entry(RoomStatus.deleted.name(), translate("building.status.deleted")));
		}
		filters.add(new FlexiTableMultiSelectionFilter(translate("room.filter.status"),
				FILTER_STATUS, statusValues, true));

		initBuildingFilter(filters, tabStatuses);
		initRoomFilter(filters, tabStatuses);

		return filters;
	}

	private void reinitBuildingAndRoomFilters() {
		tableEl.setFilters(true, buildFilters(getTabStatuses()), false, false);
	}

	private List<RoomStatus> getTabStatuses() {
		FlexiFiltersTab selectedTab = tableEl.getSelectedFilterTab();
		if (tabDeleted != null && tabDeleted.equals(selectedTab)) {
			return List.of(RoomStatus.deleted);
		} else if (tabAll != null && tabAll.equals(selectedTab)) {
			return List.of(RoomStatus.active, RoomStatus.inactive, RoomStatus.deleted);
		}
		return List.of(RoomStatus.active);
	}

	private void initBuildingFilter(List<FlexiTableExtendedFilter> filters, List<RoomStatus> tabStatuses) {
		SelectionValues buildingValues = new SelectionValues();
		SearchBuildingParameters buildingParams = new SearchBuildingParameters();
		buildingParams.setStatus(tabStatuses);
		List<Building> buildings = roomManagementService.searchBuildings(buildingParams, roles);
		buildings.sort(Comparator.comparing(b -> {
			String sortLabel = StringHelper.containsNonWhitespace(b.getExternalRef()) ? b.getExternalRef() : b.getDescription();
			return sortLabel != null ? sortLabel.toLowerCase() : "";
		}));
		for (Building b : buildings) {
			String ref = b.getExternalRef();
			String desc = b.getDescription();
			boolean hasRef = StringHelper.containsNonWhitespace(ref);
			boolean hasDesc = StringHelper.containsNonWhitespace(desc);
			if (!hasRef && !hasDesc) {
				continue;
			}
			StringBuilder html = new StringBuilder();
			if (hasRef) {
				html.append("<span>").append(StringHelper.escapeHtml(ref)).append("</span>");
			}
			if (hasDesc) {
				html.append("<span class=\"o_building_filter text-muted\"> &middot; ").append(StringHelper.escapeHtml(desc)).append("</span>");
			}
			if (b.getStatus() != null) {
				String statusName = b.getStatus().name();
				String statusLabel = translate("building.status." + statusName);
				html.append("&nbsp;|&nbsp;");
				html.append("<div class=\"o_building_room_status_icon\">");
				html.append("<i class=\"o_icon o_icon_circle_color o_building_room_status_").append(StringHelper.escapeHtml(statusName)).append("\"> </i>");
				html.append("</div>");
				html.append("&nbsp;");
				html.append("<span>").append(StringHelper.escapeHtml(statusLabel)).append("</span>");
			}
			buildingValues.add(SelectionValues.entry(b.getKey().toString(), html.toString()));
		}
		if (!buildingValues.isEmpty()) {
			filters.add(new FlexiTableMultiSelectionFilter(translate("room.filter.buildings"),
					FILTER_BUILDINGS, buildingValues, true));
		}
	}

	private void initRoomFilter(List<FlexiTableExtendedFilter> filters, List<RoomStatus> tabStatuses) {
		SelectionValues roomValues = new SelectionValues();
		SearchRoomParameters roomParams = new SearchRoomParameters();
		roomParams.setStatus(tabStatuses);
		List<Room> rooms = roomManagementService.searchRooms(roomParams, roles);
		rooms.sort(Comparator.comparing(r -> {
			String sortLabel = StringHelper.containsNonWhitespace(r.getExternalRef()) ? r.getExternalRef() : r.getDescription();
			return sortLabel != null ? sortLabel.toLowerCase() : "";
		}));
		for (Room r : rooms) {
			String ref = r.getExternalRef();
			String desc = r.getDescription();
			boolean hasRef = StringHelper.containsNonWhitespace(ref);
			boolean hasDesc = StringHelper.containsNonWhitespace(desc);
			if (!hasRef && !hasDesc) {
				continue;
			}
			StringBuilder html = new StringBuilder();
			if (hasRef) {
				html.append("<span>").append(StringHelper.escapeHtml(ref)).append("</span>");
			}
			if (hasDesc) {
				html.append("<span class=\"o_room_filter text-muted\"> &middot; ").append(StringHelper.escapeHtml(desc)).append("</span>");
			}
			if (r.getStatus() != null) {
				String statusName = r.getStatus().name();
				String statusLabel = translate("building.status." + statusName);
				html.append("&nbsp;|&nbsp;");
				html.append("<div class=\"o_building_room_status_icon\">");
				html.append("<i class=\"o_icon o_icon_circle_color o_building_room_status_").append(StringHelper.escapeHtml(statusName)).append("\"> </i>");
				html.append("</div>");
				html.append("&nbsp;");
				html.append("<span>").append(StringHelper.escapeHtml(statusLabel)).append("</span>");
			}
			roomValues.add(SelectionValues.entry(r.getKey().toString(), html.toString()));
		}
		if (!roomValues.isEmpty()) {
			filters.add(new FlexiTableMultiSelectionFilter(translate("room.filter.rooms"),
					FILTER_ROOMS, roomValues, true));
		}
	}

	private void initFilterTabs(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>();

		tabAll = FlexiFiltersTabFactory.tab(
				TAB_ID_ALL,
				translate("room.filter.all"),
				TabSelectionBehavior.reloadData);
		tabs.add(tabAll);

		tabRelevant = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_RELEVANT,
				translate("room.filter.relevant"),
				TabSelectionBehavior.reloadData,
				List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, RoomStatus.active.name())));
		tabs.add(tabRelevant);

		if (!readOnly) {
			tabDeleted = FlexiFiltersTabFactory.tabWithImplicitFilters(
					TAB_ID_DELETED,
					translate("room.filter.deleted"),
					TabSelectionBehavior.reloadData,
					List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, RoomStatus.deleted.name())));
			tabs.add(tabDeleted);
		}

		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, tabRelevant);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(editRoomCtrl);
		removeAsListenerAndDispose(cmc);
		editRoomCtrl = null;
		cmc = null;
	}

	private void loadModel() {
		if (expandedRow != null) {
			doCloseDetails(expandedRow);
		}
		List<Room> rooms = loadRooms();
		List<RoomRow> rows = new ArrayList<>(rooms.size());
		for (Room room : rooms) {
			rows.add(forgeRow(room));
		}
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private List<Room> loadRooms() {
		SearchRoomParameters params = new SearchRoomParameters();

		String searchString = tableEl.getQuickSearchString();
		if (StringHelper.containsNonWhitespace(searchString)) {
			params.setSearchString(searchString);
		}

		List<RoomStatus> statuses = getTabStatuses();
		if (tabAll != null && tabAll.equals(tableEl.getSelectedFilterTab())) {
			List<FlexiTableFilter> filters = tableEl.getFilters();
			if (filters != null) {
				for (FlexiTableFilter filter : filters) {
					if (FILTER_STATUS.equals(filter.getFilter())
							&& filter instanceof FlexiTableMultiSelectionFilter multiFilter) {
						List<String> values = multiFilter.getValues();
						if (values != null && !values.isEmpty()) {
							List<RoomStatus> selected = values.stream().map(RoomStatus::valueOf).collect(Collectors.toList());
							List<RoomStatus> narrowed = statuses.stream().filter(selected::contains).collect(Collectors.toList());
							if (!narrowed.isEmpty()) {
								statuses = narrowed;
							}
						}
					}
				}
			}
		}
		params.setStatus(statuses);

		if (readOnly) {
			List<RoomStatus> status = params.getStatus();
			if (status == null || status.isEmpty()) {
				params.setStatus(List.of(RoomStatus.active, RoomStatus.inactive));
			} else {
				List<RoomStatus> noDeleted = status.stream()
						.filter(s -> s != RoomStatus.deleted)
						.collect(Collectors.toList());
				params.setStatus(noDeleted.isEmpty() ? List.of(RoomStatus.active, RoomStatus.inactive) : noDeleted);
			}
		}

		List<Room> rooms = roomManagementService.searchRooms(params, roles);

		Set<Long> selectedBuildingKeys = getSelectedLongKeys(FILTER_BUILDINGS);
		if (!selectedBuildingKeys.isEmpty()) {
			rooms = rooms.stream()
					.filter(r -> r.getBuilding() != null && selectedBuildingKeys.contains(r.getBuilding().getKey()))
					.collect(Collectors.toList());
		}

		Set<Long> selectedRoomKeys = getSelectedLongKeys(FILTER_ROOMS);
		if (!selectedRoomKeys.isEmpty()) {
			rooms = rooms.stream()
					.filter(r -> selectedRoomKeys.contains(r.getKey()))
					.collect(Collectors.toList());
		}
		
		return rooms;
	}

	private RoomRow forgeRow(Room room) {
		RoomRow row = new RoomRow(room);

		String refText = StringHelper.containsNonWhitespace(room.getExternalRef())
				? StringHelper.escapeHtml(room.getExternalRef()) : "-";
		FormLink referenceLink = uifactory.addFormLink(
				"ref_" + room.getKey(), "select", refText, null, null, Link.LINK | Link.NONTRANSLATED);
		referenceLink.setUserObject(row);
		row.setReferenceLink(referenceLink);

		Building building = room.getBuilding();
		if (building != null) {
			String buildingRef = StringHelper.containsNonWhitespace(building.getExternalRef())
					? building.getExternalRef() : building.getDescription();
			if (!StringHelper.containsNonWhitespace(buildingRef)) {
				buildingRef = "-";
			}
			FormLink buildingLink = uifactory.addFormLink("bld_" + room.getKey(), "building",
					StringHelper.escapeHtml(buildingRef), null, null, Link.LINK | Link.NONTRANSLATED);
			buildingLink.setUserObject(building);
			row.setBuildingLink(buildingLink);
		}

		FormLink calendarIconLink = uifactory.addFormLink("cal_" + room.getKey(), "calendar", "",
				null, null, Link.LINK | Link.NONTRANSLATED);
		calendarIconLink.setIconLeftCSS("o_icon o_icon_calendar");
		calendarIconLink.setUserObject(row);
		calendarIconLink.setTitle(translate("room.calendar.title"));
		row.setCalendarIconLink(calendarIconLink);

		FormLink detailsIconLink = uifactory.addFormLink("det_" + room.getKey(), "details", "",
				null, null, Link.LINK | Link.NONTRANSLATED);
		detailsIconLink.setIconLeftCSS("o_icon o_icon_lightbulb");
		detailsIconLink.setUserObject(row);
		detailsIconLink.setTitle(translate("room.detail.open.details"));
		row.setDetailsIconLink(detailsIconLink);

		if (RoomUIHelper.isColumnInfoTextTruncated(room.getRoomInfo())) {
			FormLink additionalInfoLink = uifactory.addFormLink("ail_" + room.getKey(), "details",
					"…", null, null, Link.LINK | Link.NONTRANSLATED);
			additionalInfoLink.setUserObject(row);
			row.setAdditionalInfoLink(additionalInfoLink);
		}
		if (RoomUIHelper.isColumnInfoTextTruncated(room.getAdminInfo())) {
			FormLink adminInfoLink = uifactory.addFormLink("adml_" + room.getKey(), "details",
					"…", null, null, Link.LINK | Link.NONTRANSLATED);
			adminInfoLink.setUserObject(row);
			row.setAdminInfoLink(adminInfoLink);
		}

		// Calculate occupancy rate for current month
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date startOfMonth = cal.getTime();
		cal.add(Calendar.MONTH, 1);
		Date endOfMonth = cal.getTime();

		RoomRef roomRef = new RoomRefImpl(room.getKey());
		List<RoomBooking> bookings = roomManagementService.getBookingsForRoom(roomRef, startOfMonth, endOfMonth);
		if (!bookings.isEmpty()) {
			long bookedMinutes = 0;
			for (RoomBooking booking : bookings) {
				if (booking.getStartDate() != null && booking.getEndDate() != null) {
					long durationMs = booking.getEndDate().getTime() - booking.getStartDate().getTime();
					bookedMinutes += durationMs / 60000L;
				}
			}
			int availableMinutes = 21 * 9 * 60; // 21 days * 9 hours * 60 min = 11340
			int pct = (int) Math.round(bookedMinutes * 100.0 / availableMinutes);
			row.setOccupancyRatePercent(pct);
		}

		List<RoomBooking> futureBookings = roomManagementService.getBookingsForRoom(roomRef, new Date(), null);
		if (!futureBookings.isEmpty()) {
			RoomBooking next = futureBookings.get(0);
			if (next.getStartDate() != null && next.getEndDate() != null) {
				row.setNextEvent(RoomUIHelper.formatNextEvent(next, getLocale()));
			}
		}

		if (room.getStatus() != RoomStatus.deleted) {
			FormLink toolsLink = ActionsColumnModel.createLink(uifactory, getTranslator());
			toolsLink.setUserObject(row);
			row.setToolsLink(toolsLink);
		}

		return row;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == editRoomCtrl) {
			if (event == Event.DONE_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == cmc) {
			cleanUp();
		} else if (source instanceof RoomDetailsController detailsCtrl) {
			if (event == Event.CHANGED_EVENT) {
				doEditRoom(ureq, detailsCtrl.getRoom());
			} else if ("viewCalendar".equals(event.getCommand())) {
				doOpenRoomCalendar(ureq, detailsCtrl.getRoom());
			}
		} else if (source == toolsCalloutWindowCtrl) {
			cleanUpToolsCallout();
		} else if (source == confirmDeactivateDialog) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				RoomRow row = (RoomRow) confirmDeactivateDialog.getUserObject();
				doDeactivate(row);
			}
			removeAsListenerAndDispose(confirmDeactivateDialog);
			confirmDeactivateDialog = null;
		} else if (source == confirmDeleteDialog) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				RoomRow row = (RoomRow) confirmDeleteDialog.getUserObject();
				doDelete(row);
			}
			removeAsListenerAndDispose(confirmDeleteDialog);
			confirmDeleteDialog = null;
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == createRoomButton) {
			doCreateRoom(ureq);
		} else if (source == tableEl) {
			if (event instanceof DetailsToggleEvent toggleEvent) {
				RoomRow row = dataModel.getObject(toggleEvent.getRowIndex());
				if (toggleEvent.isVisible()) {
					doOpenDetails(ureq, row, toggleEvent.getRowIndex());
				} else {
					doCloseDetails(row);
				}
			} else if (event instanceof SelectionEvent se && TOGGLE_DETAILS_CMD.equals(se.getCommand())) {
				RoomRow row = dataModel.getObject(se.getIndex());
				if (tableEl.isDetailsExpended(se.getIndex())) {
					doCloseDetails(row);
					tableEl.collapseDetails(se.getIndex());
				} else {
					doOpenDetails(ureq, row, se.getIndex());
					tableEl.expandDetails(se.getIndex());
				}
			} else if (event instanceof FlexiTableRenderEvent renderEvent
					&& FlexiTableRenderEvent.CHANGE_RENDER_TYPE.equals(renderEvent.getCommand())) {
				if (renderEvent.getRendererType() == FlexiTableRendererType.classic) {
					loadModel();
				} else {
					loadCalendar();
				}
			} else if (event instanceof FlexiTableFilterTabEvent) {
				reinitBuildingAndRoomFilters();
				if (tableEl.getRendererType() == FlexiTableRendererType.classic) {
					loadModel();
				} else {
					loadCalendar();
				}
			} else if (event instanceof FlexiTableSearchEvent) {
				if (tableEl.getRendererType() == FlexiTableRendererType.classic) {
					loadModel();
				} else {
					loadCalendar();
				}
			}
		} else if (source instanceof FormLink link) {
			String cmd = link.getCmd();
			if (link.getUserObject() instanceof RoomRow row) {
				if ("select".equals(cmd)) {
					int rowIndex = dataModel.getObjects().indexOf(row);
					if (rowIndex >= 0) {
						if (tableEl.isDetailsExpended(rowIndex)) {
							doCloseDetails(row);
							tableEl.collapseDetails(rowIndex);
						} else {
							doOpenDetails(ureq, row, rowIndex);
							tableEl.expandDetails(rowIndex);
						}
					}
				} else if ("calendar".equals(cmd)) {
					doOpenRoomCalendar(ureq, row);
				} else if ("details".equals(cmd)) {
					int rowIndex = dataModel.getObjects().indexOf(row);
					if (rowIndex >= 0) {
						if (tableEl.isDetailsExpended(rowIndex)) {
							doCloseDetails(row);
							tableEl.collapseDetails(rowIndex);
						} else {
							doOpenDetails(ureq, row, rowIndex);
							tableEl.expandDetails(rowIndex);
						}
					}
				} else if ("tools".equals(cmd)) {
					doOpenTools(ureq, row, link);
				}
			} else if ("building".equals(cmd)) {
				doOpenBuilding(ureq, link);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doOpenDetails(UserRequest ureq, RoomRow row, @SuppressWarnings("unused") int rowIndex) {
		if (expandedRow != null && expandedRow != row) {
			doCloseDetails(expandedRow);
		}
		doCloseDetails(row);
		Room room = roomManagementService.getRoom(new RoomRefImpl(row.getRoom().getKey()));
		if (room == null) return;
		RoomDetailsController detailsCtrl = new RoomDetailsController(ureq, getWindowControl(), room, mainForm);
		listenTo(detailsCtrl);
		row.setDetailsController(detailsCtrl);
		expandedRow = row;
		flc.add(detailsCtrl.getInitialFormItem());
	}

	private void doOpenRoomCalendar(UserRequest ureq, RoomRow row) {
		Room room = roomManagementService.getRoom(new RoomRefImpl(row.getRoom().getKey()));
		if (room == null) return;
		doOpenRoomCalendar(ureq, room);
	}

	private void doOpenRoomCalendar(UserRequest ureq, Room room) {
		RoomCalendarController calendarCtrl = new RoomCalendarController(ureq, getWindowControl(), room);
		String name = StringHelper.containsNonWhitespace(room.getExternalRef())
				? room.getExternalRef() : room.getDescription();
		stackPanel.pushController(name, calendarCtrl);
	}

	private void doOpenBuilding(UserRequest ureq, FormLink buildingLink) {
		if (buildingLink.getUserObject() instanceof Building building) {
			fireEvent(ureq, new OpenBuildingEvent(building.getKey()));
		}
	}

	private void doCloseDetails(RoomRow row) {
		RoomDetailsController detailsCtrl = row.getDetailsController();
		if (detailsCtrl == null) return;
		removeAsListenerAndDispose(detailsCtrl);
		flc.remove(detailsCtrl.getInitialFormItem());
		row.setDetailsController(null);
		if (row == expandedRow) {
			expandedRow = null;
		}
	}

	private void loadCalendar() {
		List<Room> rooms = loadRooms();
		Map<Long, Room> roomByKey = rooms.stream().collect(Collectors.toMap(Room::getKey, r -> r));

		// Group rooms by building key; rooms without a building are collected separately
		Map<Long, List<Room>> roomsByBuildingKey = new LinkedHashMap<>();
		List<Room> roomsWithoutBuilding = new ArrayList<>();
		for (Room room : rooms) {
			Building building = room.getBuilding();
			if (building != null) {
				roomsByBuildingKey.computeIfAbsent(building.getKey(), k -> new ArrayList<>()).add(room);
			} else {
				roomsWithoutBuilding.add(room);
			}
		}

		Map<Long, Building> buildingByKey = rooms.stream()
				.map(Room::getBuilding)
				.filter(Objects::nonNull)
				.collect(Collectors.toMap(Building::getKey, b -> b, (a, b) -> a));

		List<RoomBooking> allBookings = roomManagementService.getBookings(null, null);
		List<RoomBooking> visibleBookings = allBookings.stream()
				.filter(b -> b.getRoom() != null && roomByKey.containsKey(b.getRoom().getKey()))
				.toList();
		Set<Long> bookingKeysWithWarnings = RoomUIHelper.computeBookingKeysWithWarnings(visibleBookings, lectureService);

		// Group bookings by building key
		Map<Long, List<RoomBooking>> bookingsByBuildingKey = new LinkedHashMap<>();
		List<RoomBooking> bookingsWithoutBuilding = new ArrayList<>();
		for (RoomBooking booking : visibleBookings) {
			Room room = roomByKey.get(booking.getRoom().getKey());
			if (room != null && room.getBuilding() != null) {
				bookingsByBuildingKey.computeIfAbsent(room.getBuilding().getKey(), k -> new ArrayList<>()).add(booking);
			} else {
				bookingsWithoutBuilding.add(booking);
			}
		}

		List<KalendarRenderWrapper> wrappers = new ArrayList<>();

		for (Map.Entry<Long, List<Room>> entry : roomsByBuildingKey.entrySet()) {
			Long buildingKey = entry.getKey();
			Building building = buildingByKey.get(buildingKey);
			List<RoomBooking> buildingBookings = bookingsByBuildingKey.getOrDefault(buildingKey, List.of());

			String calId = "rooms.building." + buildingKey;
			Kalendar calendar = new Kalendar(calId, "Room");
			for (RoomBooking booking : buildingBookings) {
				addCalendarEvent(calendar, booking, roomByKey, bookingKeysWithWarnings);
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

		if (!roomsWithoutBuilding.isEmpty()) {
			String calId = "rooms.nobuilding";
			Kalendar calendar = new Kalendar(calId, "Room");
			for (RoomBooking booking : bookingsWithoutBuilding) {
				addCalendarEvent(calendar, booking, roomByKey, bookingKeysWithWarnings);
			}
			KalendarRenderWrapper wrapper = new KalendarRenderWrapper(calendar, "", calId);
			wrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_ONLY);
			wrapper.setPrivateEventsVisible(true);
			wrapper.setCssClass("o_rm_cal_pastel o_color_border " + colorService.getDefaultColor());
			wrappers.add(wrapper);
		}

		calendarEl.setCalendars(wrappers);
	}

	private void addCalendarEvent(Kalendar calendar, RoomBooking booking, Map<Long, Room> roomByKey,
			Set<Long> bookingKeysWithWarnings) {
		if (booking.getStartDate() == null || booking.getEndDate() == null) return;
		Room room = roomByKey.get(booking.getRoom().getKey());
		String subject = resolveCalendarSubject(booking, room);
		String eventId = CodeHelper.getGlobalForeverUniqueID();
		ZonedDateTime zStart = DateUtils.toZonedDateTime(booking.getStartDate(), calendarModule.getDefaultZoneId());
		ZonedDateTime zEnd = DateUtils.toZonedDateTime(booking.getEndDate(), calendarModule.getDefaultZoneId());
		KalendarEvent event = new KalendarEvent(eventId, null, subject, zStart, zEnd);
		if (bookingKeysWithWarnings.contains(booking.getKey())) {
			event.setComment("warning");
		}
		calendar.addEvent(event);
	}

	private String resolveCalendarSubject(RoomBooking booking, Room room) {
		String roomRef = room != null && StringHelper.containsNonWhitespace(room.getExternalRef())
				? room.getExternalRef() : (room != null ? room.getDescription() : null);
		String blockTitle = booking.getLectureBlock() != null
				? booking.getLectureBlock().getTitle() : null;
		if (StringHelper.containsNonWhitespace(roomRef) && StringHelper.containsNonWhitespace(blockTitle)) {
			return roomRef + " · " + blockTitle;
		}
		if (StringHelper.containsNonWhitespace(roomRef)) {
			return roomRef;
		}
		if (StringHelper.containsNonWhitespace(blockTitle)) {
			return blockTitle;
		}
		return "";
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (entries == null || entries.isEmpty()) {
			loadModel();
			return;
		}
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if ("Room".equalsIgnoreCase(type)) {
			Long roomKey = entries.get(0).getOLATResourceable().getResourceableId();
			selectRoom(ureq, roomKey);
		}
	}

	void selectRoom(UserRequest ureq, Long roomKey) {
		tableEl.setSelectedFilterTab(ureq, tabAll);
		loadModel();
		List<RoomRow> rows = dataModel.getObjects();
		for (int i = 0; i < rows.size(); i++) {
			RoomRow row = rows.get(i);
			if (row.getRoom().getKey().equals(roomKey)) {
				doOpenDetails(ureq, row, i);
				tableEl.expandDetails(i);
				break;
			}
		}
	}

	private void doEditRoom(UserRequest ureq, Room room) {
		removeAsListenerAndDispose(editRoomCtrl);
		editRoomCtrl = new EditRoomController(ureq, getWindowControl(), room);
		listenTo(editRoomCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				editRoomCtrl.getInitialComponent(), true, translate("room.edit"), true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doCreateRoom(UserRequest ureq) {
		removeAsListenerAndDispose(editRoomCtrl);
		editRoomCtrl = new EditRoomController(ureq, getWindowControl());
		listenTo(editRoomCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				editRoomCtrl.getInitialComponent(), true, translate("room.create"), true);
		listenTo(cmc);
		cmc.activate();
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

	private void cleanUpToolsCallout() {
		removeAsListenerAndDispose(toolsCalloutWindowCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		toolsCalloutWindowCtrl = null;
		toolsCtrl = null;
	}

	private void doOpenTools(UserRequest ureq, RoomRow row, FormLink link) {
		cleanUpToolsCallout();
		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);
		toolsCalloutWindowCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutWindowCtrl);
		toolsCalloutWindowCtrl.activate();
	}

	private void doConfirmDeactivate(UserRequest ureq, RoomRow row) {
		String title = translate("room.confirm.deactivate.title");
		String ref = StringHelper.containsNonWhitespace(row.getRoom().getExternalRef()) ? row.getRoom().getExternalRef() : "";
		String text = translate("room.confirm.deactivate", StringHelper.escapeHtml(ref));
		List<String> buttons = List.of(translate("room.tools.deactivate"), translate("cancel"));
		confirmDeactivateDialog = DialogBoxUIFactory.createGenericDialog(ureq, getWindowControl(), title, text, buttons);
		listenTo(confirmDeactivateDialog);
		confirmDeactivateDialog.setUserObject(row);
		confirmDeactivateDialog.activate();
	}

	private void doDeactivate(RoomRow row) {
		Room room = roomManagementService.getRoom(new RoomRefImpl(row.getRoom().getKey()));
		if (room == null) return;
		room.setStatus(RoomStatus.inactive);
		roomManagementService.updateRoom(room, getIdentity());
		loadModel();
	}

	private void doActivate(RoomRow row) {
		Room room = roomManagementService.getRoom(new RoomRefImpl(row.getRoom().getKey()));
		if (room == null) return;
		room.setStatus(RoomStatus.active);
		roomManagementService.updateRoom(room, getIdentity());
		loadModel();
	}

	private void doConfirmDelete(UserRequest ureq, RoomRow row) {
		List<RoomBooking> bookings = roomManagementService.getBookingsForRoom(
				new RoomRefImpl(row.getRoom().getKey()), new Date(), null);
		if (!bookings.isEmpty()) {
			showError("room.error.has.active.bookings");
			return;
		}
		String title = translate("room.confirm.delete.title");
		String ref = StringHelper.containsNonWhitespace(row.getRoom().getExternalRef()) ? row.getRoom().getExternalRef() : "";
		String text = translate("room.confirm.delete", StringHelper.escapeHtml(ref));
		List<String> buttons = List.of(translate("delete"), translate("cancel"));
		confirmDeleteDialog = DialogBoxUIFactory.createGenericDialog(ureq, getWindowControl(), title, text, buttons);
		confirmDeleteDialog.setDanger(0);
		listenTo(confirmDeleteDialog);
		confirmDeleteDialog.setUserObject(row);
		confirmDeleteDialog.activate();
	}

	private void doDelete(RoomRow row) {
		Room room = roomManagementService.getRoom(new RoomRefImpl(row.getRoom().getKey()));
		if (room == null) return;
		List<RoomBooking> bookings = roomManagementService.getBookingsForRoom(
				new RoomRefImpl(room.getKey()), new Date(), null);
		if (!bookings.isEmpty()) {
			showError("room.error.has.active.bookings");
			return;
		}
		roomManagementService.deleteRoom(new RoomRefImpl(room.getKey()), getIdentity());
		loadModel();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private class ToolsController extends BasicController {

		private final RoomRow row;

		public ToolsController(UserRequest ureq, WindowControl wControl, RoomRow row) {
			super(ureq, wControl);
			setTranslator(RoomListController.this.getTranslator());
			this.row = row;
			VelocityContainer mainVC = createVelocityContainer("tools");

			List<String> links = new ArrayList<>();
			addLink("edit", "edit", "o_icon o_icon-fw o_icon_edit", links, mainVC);
			if (row.getRoom().getStatus() == RoomStatus.active) {
				addLink("room.tools.deactivate", "deactivate", "o_icon o_icon-fw o_icon_ban", links, mainVC);
			} else {
				addLink("room.tools.activate", "activate", "o_icon o_icon-fw o_icon_check", links, mainVC);
			}
			if (row.getRoom().getStatus() == RoomStatus.inactive) {
				links.add("-");
				addLink("delete", "delete", "o_icon o_icon-fw o_icon_delete_item", links, mainVC);
			}

			mainVC.contextPut("links", links);
			putInitialPanel(mainVC);
		}

		private void addLink(String name, String cmd, String iconCSS, List<String> links, VelocityContainer vc) {
			Link link = LinkFactory.createLink(name, cmd, getTranslator(), vc, this, Link.LINK);
			link.setIconLeftCSS(iconCSS);
			vc.put(name, link);
			links.add(name);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if (source instanceof Link link) {
				switch (link.getCommand()) {
					case "edit" -> { close(); doEditRoom(ureq, row.getRoom()); }
					case "deactivate" -> { close(); doConfirmDeactivate(ureq, row); }
					case "activate" -> { close(); doActivate(row); }
					case "delete" -> { close(); doConfirmDelete(ureq, row); }
					default -> { /* ignore */ }
				}
			}
		}

		private void close() {
			toolsCalloutWindowCtrl.deactivate();
			cleanUpToolsCallout();
		}
	}

	private static final class BuildingCellRenderer extends AbstractBuildingCellRenderer {
		@Override
		protected FormLink getBuildingLink(Object cellValue) {
			return cellValue instanceof RoomRow roomRow ? roomRow.getBuildingLink() : null;
		}
	}

}
