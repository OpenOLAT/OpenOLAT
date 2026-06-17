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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DetailsToggleEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.helpers.Settings;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.modules.roommanagement.Building;
import org.olat.modules.roommanagement.Room;
import org.olat.modules.roommanagement.RoomManagementService;
import org.olat.modules.roommanagement.RoomStatus;
import org.olat.modules.roommanagement.model.BuildingRefImpl;
import org.olat.modules.roommanagement.model.SearchBuildingParameters;
import org.olat.modules.roommanagement.model.SearchRoomParameters;
import org.olat.modules.roommanagement.ui.BuildingListDataModel.BuildingCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 1 Jun 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class BuildingListController extends FormBasicController implements FlexiTableComponentDelegate {

	private static final String FILTER_STATUS = "status";
	private static final String TAB_ID_ALL = "all";
	private static final String TAB_ID_RELEVANT = "relevant";
	private static final String TAB_ID_DELETED = "deleted";
	private static final String TOGGLE_DETAILS_CMD = "toggle-details";

	private FormLink createBuildingButton;
	private FlexiTableElement tableEl;
	private BuildingListDataModel dataModel;

	private FlexiFiltersTab tabAll;
	private FlexiFiltersTab tabRelevant;
	private FlexiFiltersTab tabDeleted;

	private CloseableModalController cmc;
	private EditBuildingController editBuildingCtrl;
	private ToolsController toolsCtrl;
	private CloseableCalloutWindowController toolsCalloutWindowCtrl;
	private DialogBoxController confirmDeactivateDialog;
	private MapsCalloutController mapsCalloutCtrl;
	private CloseableCalloutWindowController mapsCalloutWindowCtrl;
	private RoomsCalloutController roomsCalloutCtrl;
	private CloseableCalloutWindowController roomsCalloutWindowCtrl;

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
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BuildingCols.reference, TOGGLE_DETAILS_CMD));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BuildingCols.description));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BuildingCols.status, new RoomStatusCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BuildingCols.address));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BuildingCols.infoUrl));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BuildingCols.orgRestriction,
				new OrgRestrictionCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, BuildingCols.additionalInfo));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BuildingCols.rooms));
		columnsModel.addFlexiColumnModel(new ActionsColumnModel(BuildingCols.tools));

		dataModel = new BuildingListDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "buildings", dataModel, 20, false,
				getTranslator(), formLayout);
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setSearchEnabled(true);
		tableEl.setAndLoadPersistedPreferences(ureq, "room-management-buildings");

		VelocityContainer detailsVC = createVelocityContainer("building_details");
		tableEl.setDetailsRenderer(detailsVC, this);

		initFilters();
		initFilterTabs(ureq);
	}

	@Override
	public boolean isDetailsRow(int row, Object rowObject) {
		return true;
	}

	@Override
	public Iterable<org.olat.core.gui.components.Component> getComponents(int row, Object rowObject) {
		if (rowObject instanceof BuildingRow buildingRow && buildingRow.getDetailsController() != null) {
			return List.of(buildingRow.getDetailsController().getInitialFormItem().getComponent());
		}
		return List.of();
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
			FormLink addressLink = uifactory.addFormLink("addr_" + building.getKey(), "address", 
					building.getAddress(), null, null, Link.LINK | Link.NONTRANSLATED);
			addressLink.setIconLeftCSS("o_icon o_icon_location");
			addressLink.setUserObject(row);
			row.setAddressLink(addressLink);
		}

		if (StringHelper.containsNonWhitespace(building.getInfoUrl())) {
			FormLink infoUrlLink = uifactory.addFormLink("info_" + building.getKey(), "infoUrl",
					"building.information", null, null, Link.LINK);
			infoUrlLink.setIconLeftCSS("o_icon o_icon-fw o_icon_arrow_up_right_from_square");
			infoUrlLink.setUrl(building.getInfoUrl());
			infoUrlLink.setNewWindow(true, true, false);
			infoUrlLink.setUserObject(row);
			row.setInfoUrlLink(infoUrlLink);
		}

		SearchRoomParameters roomParams = new SearchRoomParameters();
		roomParams.setBuilding(building);
		roomParams.setStatus(List.of(RoomStatus.active, RoomStatus.inactive));
		int roomCount = (int) roomManagementService.countRooms(roomParams);
		row.setRoomCount(roomCount);

		if (roomCount > 0) {
			FormLink roomsLink = uifactory.addFormLink("rooms_" + building.getKey(), "rooms",
					String.valueOf(roomCount), null, null, Link.LINK | Link.NONTRANSLATED);
			roomsLink.setUserObject(row);
			row.setRoomsLink(roomsLink);
		}

		row.setOrganisations(roomManagementService.getOrganisations(building));

		if (building.getStatus() != RoomStatus.deleted) {
			FormLink toolsLink = ActionsColumnModel.createLink(uifactory, getTranslator());
			toolsLink.setUserObject(row);
			row.setToolsLink(toolsLink);
		}

		return row;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == editBuildingCtrl) {
			if (event == Event.DONE_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == cmc) {
			cleanUp();
		} else if (source instanceof BuildingDetailsController detailsCtrl && event == Event.CHANGED_EVENT) {
			doEditBuilding(ureq, detailsCtrl.getBuilding());
		} else if (source == mapsCalloutCtrl && event == Event.DONE_EVENT) {
			mapsCalloutWindowCtrl.deactivate();
			cleanUpMapsCallout();
		} else if (source == mapsCalloutWindowCtrl) {
			cleanUpMapsCallout();
		} else if (source == roomsCalloutCtrl && event == Event.DONE_EVENT) {
			roomsCalloutWindowCtrl.deactivate();
			cleanUpRoomsCallout();
		} else if (source == roomsCalloutWindowCtrl) {
			cleanUpRoomsCallout();
		} else if (source == toolsCalloutWindowCtrl) {
			cleanUpToolsCallout();
		} else if (source == confirmDeactivateDialog) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				BuildingRow row = (BuildingRow) confirmDeactivateDialog.getUserObject();
				doDeactivate(row);
			}
			removeAsListenerAndDispose(confirmDeactivateDialog);
			confirmDeactivateDialog = null;
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(editBuildingCtrl);
		removeAsListenerAndDispose(cmc);
		editBuildingCtrl = null;
		cmc = null;
	}

	private void cleanUpMapsCallout() {
		removeAsListenerAndDispose(mapsCalloutWindowCtrl);
		removeAsListenerAndDispose(mapsCalloutCtrl);
		mapsCalloutWindowCtrl = null;
		mapsCalloutCtrl = null;
	}

	private void cleanUpRoomsCallout() {
		removeAsListenerAndDispose(roomsCalloutWindowCtrl);
		removeAsListenerAndDispose(roomsCalloutCtrl);
		roomsCalloutWindowCtrl = null;
		roomsCalloutCtrl = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == createBuildingButton) {
			doCreateBuilding(ureq);
		} else if (source == tableEl) {
			if (event instanceof DetailsToggleEvent toggleEvent) {
				BuildingRow row = dataModel.getObject(toggleEvent.getRowIndex());
				if (toggleEvent.isVisible()) {
					doOpenDetails(ureq, row, toggleEvent.getRowIndex());
				} else {
					doCloseDetails(row);
				}
			} else if (event instanceof SelectionEvent se && TOGGLE_DETAILS_CMD.equals(se.getCommand())) {
				BuildingRow row = dataModel.getObject(se.getIndex());
				if (row.isDetailsControllerAvailable()) {
					doCloseDetails(row);
					tableEl.collapseDetails(se.getIndex());
				} else {
					doOpenDetails(ureq, row, se.getIndex());
					tableEl.expandDetails(se.getIndex());
				}
			} else if (event instanceof FlexiTableFilterTabEvent || event instanceof FlexiTableSearchEvent) {
				loadModel();
			}
		} else if (source instanceof FormLink link) {
			String cmd = link.getCmd();
			if (link.getUserObject() instanceof BuildingRow row) {
				if ("select".equals(cmd)) {
					int rowIndex = dataModel.getObjects().indexOf(row);
					if (rowIndex >= 0) {
						if (row.isDetailsControllerAvailable()) {
							doCloseDetails(row);
							tableEl.collapseDetails(rowIndex);
						} else {
							doOpenDetails(ureq, row, rowIndex);
							tableEl.expandDetails(rowIndex);
						}
					}
				} else if ("infoUrl".equals(cmd)) {
					getWindowControl().getWindowBackOffice().sendCommandTo(
							CommandFactory.createNewWindowRedirectTo(row.getBuilding().getInfoUrl()));
				} else if ("address".equals(cmd)) {
					doOpenMapsCallout(ureq, row, link);
				} else if ("rooms".equals(cmd)) {
					doOpenRoomsCallout(ureq, row, link);
				} else if ("tools".equals(cmd)) {
					doOpenTools(ureq, row, link);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doCreateBuilding(UserRequest ureq) {
		removeAsListenerAndDispose(editBuildingCtrl);
		editBuildingCtrl = new EditBuildingController(ureq, getWindowControl());
		listenTo(editBuildingCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				editBuildingCtrl.getInitialComponent(), true, translate("building.create"), true);
		listenTo(cmc);
		cmc.activate();
	}

	void selectBuilding(UserRequest ureq, Long buildingKey) {
		tableEl.setSelectedFilterTab(ureq, tabAll);
		loadModel();
		List<BuildingRow> rows = dataModel.getObjects();
		for (int i = 0; i < rows.size(); i++) {
			BuildingRow row = rows.get(i);
			if (row.getBuilding().getKey().equals(buildingKey)) {
				doOpenDetails(ureq, row, i);
				tableEl.expandDetails(i);
				break;
			}
		}
	}

	private void doOpenDetails(UserRequest ureq, BuildingRow row, @SuppressWarnings("unused") int rowIndex) {
		doCloseDetails(row);
		Building building = roomManagementService.getBuilding(new BuildingRefImpl(row.getBuilding().getKey()));
		if (building == null) return;
		BuildingDetailsController detailsCtrl = new BuildingDetailsController(ureq, getWindowControl(), building, mainForm);
		listenTo(detailsCtrl);
		row.setDetailsController(detailsCtrl);
		flc.add(detailsCtrl.getInitialFormItem());
	}

	private void doCloseDetails(BuildingRow row) {
		BuildingDetailsController detailsCtrl = row.getDetailsController();
		if (detailsCtrl == null) return;
		removeAsListenerAndDispose(detailsCtrl);
		flc.remove(detailsCtrl.getInitialFormItem());
		row.setDetailsController(null);
	}

	private void doEditBuilding(UserRequest ureq, Building building) {
		removeAsListenerAndDispose(editBuildingCtrl);
		List<Organisation> organisations = roomManagementService.getOrganisations(building);
		editBuildingCtrl = new EditBuildingController(ureq, getWindowControl(), building, organisations);
		listenTo(editBuildingCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				editBuildingCtrl.getInitialComponent(), true, translate("building.edit"), true);
		listenTo(cmc);
		cmc.activate();
	}

	private void cleanUpToolsCallout() {
		removeAsListenerAndDispose(toolsCalloutWindowCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		toolsCalloutWindowCtrl = null;
		toolsCtrl = null;
	}

	private void doOpenTools(UserRequest ureq, BuildingRow row, FormLink link) {
		cleanUpToolsCallout();
		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);
		toolsCalloutWindowCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutWindowCtrl);
		toolsCalloutWindowCtrl.activate();
	}

	private void doConfirmDeactivate(UserRequest ureq, BuildingRow row) {
		String title = translate("building.confirm.deactivate.title");
		String text = translate("building.confirm.deactivate", String.valueOf(row.getRoomCount()));
		List<String> buttons = List.of(translate("building.tools.deactivate"), translate("cancel"));
		confirmDeactivateDialog = DialogBoxUIFactory.createGenericDialog(ureq, getWindowControl(), title, text, buttons);
		listenTo(confirmDeactivateDialog);
		confirmDeactivateDialog.setUserObject(row);
		confirmDeactivateDialog.activate();
	}

	private void doDeactivate(BuildingRow row) {
		Building building = roomManagementService.getBuilding(new BuildingRefImpl(row.getBuilding().getKey()));
		if (building == null) return;
		building.setStatus(RoomStatus.inactive);
		List<Organisation> orgs = roomManagementService.getOrganisations(building);
		roomManagementService.updateBuilding(building, orgs, getIdentity());

		SearchRoomParameters params = new SearchRoomParameters();
		params.setBuilding(building);
		params.setStatus(List.of(RoomStatus.active));
		List<Room> activeRooms = roomManagementService.searchRooms(params, roles);
		for (Room room : activeRooms) {
			room.setStatus(RoomStatus.inactive);
			roomManagementService.updateRoom(room, getIdentity());
		}
		loadModel();
	}

	private void doReactivate(BuildingRow row) {
		Building building = roomManagementService.getBuilding(new BuildingRefImpl(row.getBuilding().getKey()));
		if (building == null) return;
		building.setStatus(RoomStatus.active);
		List<Organisation> orgs = roomManagementService.getOrganisations(building);
		roomManagementService.updateBuilding(building, orgs, getIdentity());
		loadModel();
	}

	private void doDelete(UserRequest ureq, BuildingRow row) {
		if (row.getRoomCount() > 0) {
			showWarning("building.error.has.rooms");
			return;
		}
		roomManagementService.deleteBuilding(new BuildingRefImpl(row.getBuilding().getKey()), getIdentity());
		loadModel();
	}

	private void doOpenMapsCallout(UserRequest ureq, BuildingRow row, FormLink link) {
		cleanUpMapsCallout();
		mapsCalloutCtrl = new MapsCalloutController(ureq, getWindowControl(), row.getBuilding());
		listenTo(mapsCalloutCtrl);
		mapsCalloutWindowCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				mapsCalloutCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(mapsCalloutWindowCtrl);
		mapsCalloutWindowCtrl.activate();
	}

	private void doOpenRoomsCallout(UserRequest ureq, BuildingRow row, FormLink link) {
		cleanUpRoomsCallout();
		SearchRoomParameters params = new SearchRoomParameters();
		params.setBuilding(row.getBuilding());
		params.setStatus(List.of(RoomStatus.active, RoomStatus.inactive));
		List<Room> rooms = roomManagementService.searchRooms(params, roles);
		roomsCalloutCtrl = new RoomsCalloutController(ureq, getWindowControl(), rooms, getTranslator());
		listenTo(roomsCalloutCtrl);
		roomsCalloutWindowCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				roomsCalloutCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(roomsCalloutWindowCtrl);
		roomsCalloutWindowCtrl.activate();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private class ToolsController extends BasicController {

		private final BuildingRow row;

		public ToolsController(UserRequest ureq, WindowControl wControl, BuildingRow row) {
			super(ureq, wControl);
			setTranslator(BuildingListController.this.getTranslator());
			this.row = row;
			VelocityContainer mainVC = createVelocityContainer("tools");

			List<String> links = new ArrayList<>();
			addLink("building.tools.edit", "edit", "o_icon o_icon-fw o_icon_edit", links, mainVC);
			if (row.getBuilding().getStatus() == RoomStatus.active) {
				addLink("building.tools.deactivate", "deactivate", "o_icon o_icon-fw o_icon_ban", links, mainVC);
			} else {
				addLink("building.tools.reactivate", "reactivate", "o_icon o_icon-fw o_icon_check", links, mainVC);
			}
			links.add("-");
			addLink("building.tools.delete", "delete", "o_icon o_icon-fw o_icon_delete_item", links, mainVC);

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
		protected void event(UserRequest ureq, org.olat.core.gui.components.Component source, org.olat.core.gui.control.Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if (source instanceof Link link) {
				switch (link.getCommand()) {
					case "edit" -> { close(); doEditBuilding(ureq, row.getBuilding()); }
					case "deactivate" -> { close(); doConfirmDeactivate(ureq, row); }
					case "reactivate" -> { close(); doReactivate(row); }
					case "delete" -> { close(); doDelete(ureq, row); }
					default -> { /* ignore */ }
				}
			}
		}

		private void close() {
			toolsCalloutWindowCtrl.deactivate();
			cleanUpToolsCallout();
		}
	}

	private static final class RoomsCalloutController extends BasicController {

		private final List<Link> roomLinks = new ArrayList<>();
		private final String roomsUrl;

		public RoomsCalloutController(UserRequest ureq, WindowControl wControl, List<Room> rooms, Translator translator) {
			super(ureq, wControl);
			VelocityContainer mainVC = createVelocityContainer("rooms_callout");

			roomsUrl = Settings.getServerContextPathURI() + "/auth/AdminSite/0/roommanagement/0/Rooms/0";

			mainVC.contextPut("title", translator.translate("building.rooms.callout", String.valueOf(rooms.size())));

			List<String> linkNames = new ArrayList<>();
			for (Room room : rooms) {
				String label = StringHelper.containsNonWhitespace(room.getExternalRef())
						? room.getExternalRef() : room.getDescription();
				if (!StringHelper.containsNonWhitespace(label)) {
					continue;
				}
				Link roomLink = LinkFactory.createCustomLink("room_" + room.getKey(), "room_" + room.getKey(), 
						"roomLink", StringHelper.escapeHtml(label), Link.LINK | Link.NONTRANSLATED, 
						mainVC, this);
				roomLink.setUrl(roomsUrl);
				roomLink.setNewWindow(true, true);
				roomLinks.add(roomLink);
				linkNames.add(roomLink.getComponentName());
			}
			mainVC.contextPut("roomLinkNames", linkNames);
			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, org.olat.core.gui.components.Component source, org.olat.core.gui.control.Event event) {
			if (roomLinks.contains(source)) {
				// Add route to exact room once the route is available
				getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(roomsUrl));
				fireEvent(ureq, Event.DONE_EVENT);
			}
		}
	}

	private static final class MapsCalloutController extends BasicController {

		private final Link appleMapsLink;
		private final Link googleMapsLink;
		private final String appleMapsUrl;
		private final String googleMapsUrl;

		public MapsCalloutController(UserRequest ureq, WindowControl wControl, Building building) {
			super(ureq, wControl);
			VelocityContainer mainVC = createVelocityContainer("maps_callout");

			String query = URLEncoder.encode(building.getAddress(), StandardCharsets.UTF_8);
			appleMapsUrl = "https://maps.apple.com/?q=" + query;
			googleMapsUrl = "https://www.google.com/maps/search/?api=1&query=" + query;

			appleMapsLink = LinkFactory.createLink("building.open.apple.maps", mainVC, this);
			appleMapsLink.setIconLeftCSS("o_icon o_icon-fw o_icon_arrow_up_right_from_square");
			appleMapsLink.setUrl(appleMapsUrl);
			appleMapsLink.setNewWindow(true, true);

			googleMapsLink = LinkFactory.createLink("building.open.google.maps", mainVC, this);
			googleMapsLink.setIconLeftCSS("o_icon o_icon-fw o_icon_arrow_up_right_from_square");
			googleMapsLink.setUrl(googleMapsUrl);
			googleMapsLink.setNewWindow(true, true);

			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, org.olat.core.gui.components.Component source,
				org.olat.core.gui.control.Event event) {
			if (source == appleMapsLink) {
				getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(appleMapsUrl));
				fireEvent(ureq, Event.DONE_EVENT);
			} else if (source == googleMapsLink) {
				getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(googleMapsUrl));
				fireEvent(ureq, Event.DONE_EVENT);
			}
		}
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
