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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.ui.components.FullCalendarElement;
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
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.roommanagement.Building;
import org.olat.modules.roommanagement.Room;
import org.olat.modules.roommanagement.RoomBooking;
import org.olat.modules.roommanagement.RoomManagementService;
import org.olat.modules.roommanagement.RoomRef;
import org.olat.modules.roommanagement.RoomStatus;
import org.olat.modules.roommanagement.model.RoomRefImpl;
import org.olat.modules.roommanagement.model.SearchRoomParameters;
import org.olat.modules.roommanagement.ui.RoomListDataModel.RoomCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 5 Jun 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RoomListController extends FormBasicController implements FlexiTableComponentDelegate {

	private static final String FILTER_STATUS = "status";
	private static final String TAB_ID_ALL = "all";
	private static final String TAB_ID_RELEVANT = "relevant";
	private static final String TAB_ID_DELETED = "deleted";
	private static final String TOGGLE_DETAILS_CMD = "toggle-details";

	private FormLink createRoomButton;
	private CloseableModalController cmc;
	private EditRoomController editRoomCtrl;
	private FlexiTableElement tableEl;
	private RoomListDataModel dataModel;
	private FullCalendarElement calendarEl;

	private FlexiFiltersTab tabAll;
	private FlexiFiltersTab tabRelevant;
	private FlexiFiltersTab tabDeleted;

	private final Roles roles;
	private final BreadcrumbedStackedPanel stackPanel;

	@Autowired
	private RoomManagementService roomManagementService;

	public RoomListController(UserRequest ureq, WindowControl wControl, BreadcrumbedStackedPanel stackPanel) {
		super(ureq, wControl, "rooms_admin");
		setTranslator(Util.createPackageTranslator(CalendarManager.class, ureq.getLocale(), getTranslator()));
		roles = ureq.getUserSession().getRoles();
		this.stackPanel = stackPanel;
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		createRoomButton = uifactory.addFormLink("create", formLayout, Link.BUTTON);
		createRoomButton.setIconLeftCSS("o_icon o_icon_add");

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		DefaultFlexiColumnModel refCol = new DefaultFlexiColumnModel(RoomCols.reference, TOGGLE_DETAILS_CMD);
		refCol.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(refCol);

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RoomCols.description));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RoomCols.status, new RoomStatusCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RoomCols.seats));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RoomCols.additionalInfo));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RoomCols.adminInfo));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RoomCols.building, new BuildingCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RoomCols.occupancyRate));
		DefaultFlexiColumnModel calendarIconCol = new DefaultFlexiColumnModel(RoomCols.calendarIcon);
		calendarIconCol.setIconHeader(RoomCols.calendarIcon.iconHeader());
		columnsModel.addFlexiColumnModel(calendarIconCol);

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

		VelocityContainer detailsVC = createVelocityContainer("room_details");
		tableEl.setDetailsRenderer(detailsVC, this);

		calendarEl = new FullCalendarElement(ureq, RoomCalendarRenderer.CALENDAR_ITEM_NAME, new ArrayList<>(), getTranslator());
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
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();

		SelectionValues statusValues = new SelectionValues();
		statusValues.add(SelectionValues.entry(RoomStatus.active.name(), translate("building.status.active")));
		statusValues.add(SelectionValues.entry(RoomStatus.inactive.name(), translate("building.status.inactive")));
		statusValues.add(SelectionValues.entry(RoomStatus.deleted.name(), translate("building.status.deleted")));
		filters.add(new FlexiTableMultiSelectionFilter(translate("room.filter.status"),
				FILTER_STATUS, statusValues, true));

		tableEl.setFilters(true, filters, false, false);
	}

	private void initFilterTabs(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>();

		tabAll = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_ALL,
				translate("room.filter.all"),
				TabSelectionBehavior.reloadData,
				List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS,
						List.of(RoomStatus.active.name(), RoomStatus.inactive.name()))));
		tabs.add(tabAll);

		tabRelevant = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_RELEVANT,
				translate("room.filter.relevant"),
				TabSelectionBehavior.reloadData,
				List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, RoomStatus.active.name())));
		tabs.add(tabRelevant);

		tabDeleted = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_DELETED,
				translate("room.filter.deleted"),
				TabSelectionBehavior.reloadData,
				List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, RoomStatus.deleted.name())));
		tabs.add(tabDeleted);

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
		SearchRoomParameters params = new SearchRoomParameters();

		List<FlexiTableFilter> filters = tableEl.getFilters();
		if (filters != null) {
			for (FlexiTableFilter filter : filters) {
				if (FILTER_STATUS.equals(filter.getFilter())
						&& filter instanceof FlexiTableMultiSelectionFilter multiFilter) {
					List<String> values = multiFilter.getValues();
					if (values != null && !values.isEmpty()) {
						params.setStatus(values.stream().map(RoomStatus::valueOf).collect(Collectors.toList()));
					}
				}
			}
		}

		List<Room> rooms = roomManagementService.searchRooms(params, roles);
		List<RoomRow> rows = new ArrayList<>(rooms.size());
		for (Room room : rooms) {
			rows.add(forgeRow(room));
		}
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	private RoomRow forgeRow(Room room) {
		RoomRow row = new RoomRow(room);

		String refText = StringHelper.containsNonWhitespace(room.getExternalRef())
				? room.getExternalRef() : "-";
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
			FormLink buildingLink = uifactory.addFormLink("bld_" + room.getKey(), "building", buildingRef, 
					null, null, Link.LINK | Link.NONTRANSLATED);
			buildingLink.setUserObject(building);
			row.setBuildingLink(buildingLink);
		}

		FormLink calendarIconLink = uifactory.addFormLink("cal_" + room.getKey(), "calendar", "", 
				null, null, Link.LINK | Link.NONTRANSLATED);
		calendarIconLink.setIconLeftCSS("o_icon o_icon_calendar");
		calendarIconLink.setUserObject(row);
		row.setCalendarIconLink(calendarIconLink);

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
		} else if (source instanceof RoomDetailsController detailsCtrl && event == Event.CHANGED_EVENT) {
			doEditRoom(ureq, detailsCtrl.getRoom());
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
			} else if (event instanceof FlexiTableFilterTabEvent || event instanceof FlexiTableSearchEvent) {
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
				}
			} else if ("building".equals(cmd)) {
				doOpenBuilding(ureq, link);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doOpenDetails(UserRequest ureq, RoomRow row, @SuppressWarnings("unused") int rowIndex) {
		doCloseDetails(row);
		Room room = roomManagementService.getRoom(new RoomRefImpl(row.getRoom().getKey()));
		if (room == null) return;
		RoomDetailsController detailsCtrl = new RoomDetailsController(ureq, getWindowControl(), room, mainForm);
		listenTo(detailsCtrl);
		row.setDetailsController(detailsCtrl);
		flc.add(detailsCtrl.getInitialFormItem());
	}

	private void doOpenRoomCalendar(UserRequest ureq, RoomRow row) {
		Room room = roomManagementService.getRoom(new RoomRefImpl(row.getRoom().getKey()));
		if (room == null) return;
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
	}

	private void loadCalendar() {
		//
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

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private static final class BuildingCellRenderer extends AbstractBuildingCellRenderer {
		@Override
		protected FormLink getBuildingLink(Object cellValue) {
			return cellValue instanceof RoomRow roomRow ? roomRow.getBuildingLink() : null;
		}
	}

}
