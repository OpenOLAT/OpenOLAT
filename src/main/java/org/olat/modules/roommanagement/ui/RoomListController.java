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
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Roles;
import org.olat.modules.roommanagement.Room;
import org.olat.modules.roommanagement.RoomManagementService;
import org.olat.modules.roommanagement.RoomStatus;
import org.olat.modules.roommanagement.model.SearchRoomParameters;
import org.olat.modules.roommanagement.ui.RoomListDataModel.RoomCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 5 Jun 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RoomListController extends FormBasicController {

	private static final String FILTER_STATUS = "status";
	private static final String TAB_ID_ALL = "all";
	private static final String TAB_ID_RELEVANT = "relevant";
	private static final String TAB_ID_DELETED = "deleted";

	private FormLink createRoomButton;
	private FlexiTableElement tableEl;
	private RoomListDataModel dataModel;

	private FlexiFiltersTab tabAll;
	private FlexiFiltersTab tabRelevant;
	private FlexiFiltersTab tabDeleted;

	private final Roles roles;

	@Autowired
	private RoomManagementService roomManagementService;

	public RoomListController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "rooms_admin");
		roles = ureq.getUserSession().getRoles();
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		createRoomButton = uifactory.addFormLink("create", formLayout, Link.BUTTON);
		createRoomButton.setIconLeftCSS("o_icon o_icon_add");

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RoomCols.description));

		dataModel = new RoomListDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "rooms", dataModel, 20, false,
				getTranslator(), formLayout);
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setSearchEnabled(true);
		tableEl.setAndLoadPersistedPreferences(ureq, "room-management-rooms");

		initFilters();
		initFilterTabs(ureq);
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
			rows.add(new RoomRow(room));
		}
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == createRoomButton) {
			doCreateRoom(ureq);
		} else if (source == tableEl) {
			if (event instanceof FlexiTableFilterTabEvent || event instanceof FlexiTableSearchEvent) {
				loadModel();
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doCreateRoom(UserRequest ureq) {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
