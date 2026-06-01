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
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
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
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.modules.roommanagement.Building;
import org.olat.modules.roommanagement.RoomManagementService;
import org.olat.modules.roommanagement.RoomStatus;
import org.olat.modules.roommanagement.model.SearchBuildingParameters;
import org.olat.modules.roommanagement.model.SearchRoomParameters;
import org.olat.modules.roommanagement.ui.BuildingListDataModel.BuildingCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 1 Jun 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class BuildingListController extends FormBasicController {

	private static final String FILTER_STATUS = "status";
	private static final String TAB_ID_ALL = "all";
	private static final String TAB_ID_RELEVANT = "relevant";
	private static final String TAB_ID_DELETED = "deleted";

	private FormLink createBuildingButton;
	private FlexiTableElement tableEl;
	private BuildingListDataModel dataModel;

	private FlexiFiltersTab tabAll;
	private FlexiFiltersTab tabRelevant;
	private FlexiFiltersTab tabDeleted;

	private final Roles roles;

	@Autowired
	private RoomManagementService roomManagementService;

	public BuildingListController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "buildings_admin");
		roles = ureq.getUserSession().getRoles();
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		createBuildingButton = uifactory.addFormLink("create", formLayout, Link.BUTTON);
		createBuildingButton.setIconLeftCSS("o_icon o_icon_add");

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BuildingCols.color, new ColorCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BuildingCols.reference));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BuildingCols.description));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BuildingCols.status,
				new StatusCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BuildingCols.address));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BuildingCols.infoUrl));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BuildingCols.orgRestriction,
				new OrgRestrictionCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, BuildingCols.additionalInfo));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BuildingCols.rooms));

		dataModel = new BuildingListDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "buildings", dataModel, 20, false,
				getTranslator(), formLayout);
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setSearchEnabled(true);
		tableEl.setAndLoadPersistedPreferences(ureq, "room-management-buildings");

		initFilters();
		initFilterTabs(ureq);
	}

	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();

		SelectionValues statusValues = new SelectionValues();
		statusValues.add(SelectionValues.entry(RoomStatus.active.name(), translate("building.status.active")));
		statusValues.add(SelectionValues.entry(RoomStatus.inactive.name(), translate("building.status.inactive")));
		statusValues.add(SelectionValues.entry(RoomStatus.deleted.name(), translate("building.status.deleted")));
		filters.add(new FlexiTableMultiSelectionFilter(translate("building.filter.status"),
				FILTER_STATUS, statusValues, true));

		tableEl.setFilters(true, filters, false, false);
	}

	private void initFilterTabs(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>();

		tabAll = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_ALL,
				translate("building.filter.all"),
				TabSelectionBehavior.reloadData,
				List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS,
						List.of(RoomStatus.active.name(), RoomStatus.inactive.name()))));
		tabs.add(tabAll);

		tabRelevant = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_RELEVANT,
				translate("building.filter.relevant"),
				TabSelectionBehavior.reloadData,
				List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, RoomStatus.active.name())));
		tabs.add(tabRelevant);

		tabDeleted = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_DELETED,
				translate("building.filter.deleted"),
				TabSelectionBehavior.reloadData,
				List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, RoomStatus.deleted.name())));
		tabs.add(tabDeleted);

		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, tabRelevant);
	}

	private void loadModel() {
		SearchBuildingParameters params = new SearchBuildingParameters();

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

		List<Building> buildings = roomManagementService.searchBuildings(params, roles);
		List<BuildingRow> rows = new ArrayList<>(buildings.size());
		for (Building building : buildings) {
			rows.add(forgeRow(building));
		}
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	private BuildingRow forgeRow(Building building) {
		BuildingRow row = new BuildingRow(building);

		String refText = StringHelper.containsNonWhitespace(building.getExternalRef())
				? building.getExternalRef() : "-";
		FormLink referenceLink = uifactory.addFormLink(
				"ref_" + building.getKey(), "select", refText, null, null, Link.LINK | Link.NONTRANSLATED);
		referenceLink.setUserObject(row);
		row.setReferenceLink(referenceLink);

		if (StringHelper.containsNonWhitespace(building.getAddress())) {
			FormLink addressLink = uifactory.addFormLink(
					"addr_" + building.getKey(), "address",
					building.getAddress(), null, null, Link.LINK | Link.NONTRANSLATED);
			addressLink.setIconLeftCSS("o_icon o_icon_location");
			addressLink.setUserObject(row);
			row.setAddressLink(addressLink);
		}

		SearchRoomParameters roomParams = new SearchRoomParameters();
		roomParams.setBuilding(building);
		roomParams.setStatus(List.of(RoomStatus.active, RoomStatus.inactive));
		int roomCount = (int) roomManagementService.countRooms(roomParams);
		row.setRoomCount(roomCount);

		FormLink roomsLink = uifactory.addFormLink(
				"rooms_" + building.getKey(), "rooms",
				String.valueOf(roomCount), null, null, Link.LINK | Link.NONTRANSLATED);
		roomsLink.setUserObject(row);
		row.setRoomsLink(roomsLink);

		row.setOrganisations(roomManagementService.getOrganisations(building));

		return row;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == createBuildingButton) {
			doCreateBuilding(ureq);
		} else if (source == tableEl) {
			if (event instanceof FlexiTableFilterTabEvent || event instanceof FlexiTableSearchEvent) {
				loadModel();
			}
		} else if (source instanceof FormLink link) {
			String cmd = link.getCmd();
			if ("select".equals(cmd)) {
				// detail view — to be implemented
			} else if ("address".equals(cmd)) {
				// maps callout — to be implemented
			} else if ("rooms".equals(cmd)) {
				// rooms callout — to be implemented
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doCreateBuilding(UserRequest ureq) {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private static final class ColorCellRenderer implements org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer {
		@Override
		public void render(Renderer renderer, StringOutput target, Object cellValue,
				int row, FlexiTableComponent source, URLBuilder ubu, Translator translator) {
			if (cellValue instanceof String colorCss && StringHelper.containsNonWhitespace(colorCss)) {
				target.append("<div class=\"o_building_square_container\">");
				target.append("<div class=\"o_building_square o_color_background o_color_border_darken ").append(StringHelper.escapeHtml(colorCss)).append("\"> </div>");
				target.append("</div>");
			}
		}
	}

	private static final class StatusCellRenderer implements org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer {
		@Override
		public void render(Renderer renderer, StringOutput target, Object cellValue,
				int row, FlexiTableComponent source, URLBuilder ubu, Translator translator) {
			if (cellValue instanceof RoomStatus status) {
				target.append(translator.translate("building.status." + status.name()));
			}
		}
	}

	private static final class OrgRestrictionCellRenderer implements org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer {
		@Override
		public void render(Renderer renderer, StringOutput target, Object cellValue,
				int row, FlexiTableComponent source, URLBuilder ubu, Translator translator) {
			if (!(cellValue instanceof List<?> orgs) || orgs.isEmpty()) return;
			int shown = 0;
			for (Object o : orgs) {
				if (o instanceof Organisation org) {
					if (shown > 0) target.append(", ");
					target.append(StringHelper.escapeHtml(org.getDisplayName()));
					shown++;
					if (shown == 2) break;
				}
			}
			if (orgs.size() > 2) {
				target.append(" +").append(orgs.size() - 2);
			}
		}
	}
}
