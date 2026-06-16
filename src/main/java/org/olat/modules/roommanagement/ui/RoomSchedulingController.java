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
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
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
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementRefImpl;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.model.Reference;
import org.olat.modules.lecture.ui.LectureListRepositoryController;
import org.olat.modules.lecture.ui.component.LectureBlockStatusCellRenderer;
import org.olat.modules.lecture.ui.component.ReferenceRenderer;
import org.olat.modules.roommanagement.Building;
import org.olat.modules.roommanagement.Room;
import org.olat.modules.roommanagement.RoomBooking;
import org.olat.modules.roommanagement.RoomManagementService;
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
public class RoomSchedulingController extends FormBasicController implements FlexiTableComponentDelegate {

	private static final String FILTER_WITH_WARNINGS = "withWarnings";
	private static final String FILTER_WITH_WARNINGS_ON = "on";
	private static final String FILTER_BUILDINGS = "buildings";
	private static final String FILTER_ROOMS = "rooms";
	private static final String TAB_ID_ALL = "all";
	private static final String TAB_ID_TODAY = "today";
	private static final String TAB_ID_UPCOMING = "upcoming";
	private static final String TAB_ID_WITH_WARNINGS = "withWarnings";
	private static final String TOGGLE_DETAILS_CMD = "toggle-details";
	private static final String EVENTS_BUSINESS_PATH = "[CurriculumAdmin:0][Events:0][All:0]";

	private FormDateScopeSelection scopeEl;
	private FlexiTableElement tableEl;
	private RoomSchedulingDataModel dataModel;

	private FlexiFiltersTab tabAll;
	private FlexiFiltersTab tabToday;
	private FlexiFiltersTab tabUpcoming;
	private FlexiFiltersTab tabWithWarnings;

	private final Roles roles;

	@Autowired
	private RoomManagementService roomManagementService;
	@Autowired
	private CurriculumService curriculumService;

	public RoomSchedulingController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "room_scheduling");
		setTranslator(Util.createPackageTranslator(LectureListRepositoryController.class, getLocale(), getTranslator()));

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

	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();

		SelectionValues warningsValues = new SelectionValues();
		warningsValues.add(SelectionValues.entry(FILTER_WITH_WARNINGS_ON, translate("room.scheduling.filter.with.warnings")));
		filters.add(new FlexiTableOneClickSelectionFilter(translate("room.scheduling.filter.with.warnings"),
				FILTER_WITH_WARNINGS, warningsValues, true));

		SelectionValues buildingValues = new SelectionValues();
		roomManagementService.searchBuildings(new SearchBuildingParameters(), roles).forEach(b -> {
			String label = StringHelper.containsNonWhitespace(b.getExternalRef()) ? b.getExternalRef() : b.getDescription();
			if (StringHelper.containsNonWhitespace(label)) {
				buildingValues.add(SelectionValues.entry(b.getKey().toString(), label));
			}
		});
		if (!buildingValues.isEmpty()) {
			filters.add(new FlexiTableMultiSelectionFilter(translate("room.scheduling.filter.buildings"),
					FILTER_BUILDINGS, buildingValues, true));
		}

		SelectionValues roomValues = new SelectionValues();
		roomManagementService.searchRooms(new SearchRoomParameters(), roles).forEach(r -> {
			String label = StringHelper.containsNonWhitespace(r.getExternalRef()) ? r.getExternalRef() : r.getDescription();
			if (StringHelper.containsNonWhitespace(label)) {
				roomValues.add(SelectionValues.entry(r.getKey().toString(), label));
			}
		});
		if (!roomValues.isEmpty()) {
			filters.add(new FlexiTableMultiSelectionFilter(translate("room.scheduling.filter.rooms"),
					FILTER_ROOMS, roomValues, true));
		}

		tableEl.setFilters(true, filters, false, false);
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

	private void loadModel() {
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

		List<RoomSchedulingRow> rows = new ArrayList<>(bookings.size());
		for (RoomBooking booking : bookings) {
			rows.add(forgeRow(booking));
		}

		boolean withWarningsFilterActive = tableEl.getFilters() != null && tableEl.getFilters().stream()
				.anyMatch(f -> FILTER_WITH_WARNINGS.equals(f.getFilter()) && f.isSelected());
		if (selectedTab == tabWithWarnings || withWarningsFilterActive) {
			rows = rows.stream().filter(r -> !r.getWarnings().isEmpty()).collect(Collectors.toList());
		}

		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
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
			eventLink.setUrl(BusinessControlFactory.getInstance().getRelativeURLFromBusinessPathString(EVENTS_BUSINESS_PATH));
			eventLink.setUserObject(row);
			row.setEventLink(eventLink);
		}

		return row;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == scopeEl) {
			loadModel();
		} else if (source == tableEl) {
			if (event instanceof FlexiTableFilterTabEvent || event instanceof FlexiTableSearchEvent) {
				loadModel();
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
			}
		} else if (source instanceof FormLink link && "building".equals(link.getCmd())) {
			doOpenBuilding(ureq, link);
		} else if (source instanceof FormLink link && "selectRoom".equals(link.getCmd())) {
			doOpenRoom(ureq, link);
		} else if (source instanceof FormLink link && "openEvent".equals(link.getCmd())) {
			doOpenEvent(ureq);
		}
		super.formInnerEvent(ureq, source, event);
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

	private void doOpenEvent(UserRequest ureq) {
		NewControllerFactory.getInstance().launch(EVENTS_BUSINESS_PATH, ureq, getWindowControl());
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

	private static final class WarningsCellRenderer implements FlexiCellRenderer {
		@Override
		public void render(Renderer renderer, StringOutput target, Object cellValue,
				int row, FlexiTableComponent source, URLBuilder ubu, Translator translator) {
			if (!(cellValue instanceof RoomSchedulingRow schedulingRow)) return;
			if (schedulingRow.getWarnings().isEmpty()) return;
			target.append("<i class=\"o_icon o_icon_warn\"> </i>");
		}
	}

	private static final class BuildingCellRenderer extends AbstractBuildingCellRenderer {
		@Override
		protected FormLink getBuildingLink(Object cellValue) {
			return cellValue instanceof RoomSchedulingRow schedulingRow ? schedulingRow.getBuildingLink() : null;
		}
	}
}
