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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.modules.roommanagement.Building;
import org.olat.modules.roommanagement.Room;
import org.olat.modules.roommanagement.RoomManagementService;
import org.olat.modules.roommanagement.RoomStatus;
import org.olat.modules.roommanagement.model.SearchBuildingParameters;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 8 Jun 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class EditRoomController extends FormBasicController {

	private TextElement referenceEl;
	private TextElement descriptionEl;
	private TextElement seatsEl;
	private SingleSelection buildingEl;
	private TextAreaElement additionalInfoEl;
	private TextAreaElement adminInfoEl;

	private final List<Building> buildings;
	private Room room;

	@Autowired
	private RoomManagementService roomManagementService;

	public EditRoomController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.room = null;
		Roles roles = ureq.getUserSession().getRoles();
		SearchBuildingParameters params = new SearchBuildingParameters();
		params.setStatus(List.of(RoomStatus.active));
		this.buildings = roomManagementService.searchBuildings(params, roles);
		initForm(ureq);
	}

	public EditRoomController(UserRequest ureq, WindowControl wControl, Room room) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.room = room;
		Roles roles = ureq.getUserSession().getRoles();
		SearchBuildingParameters params = new SearchBuildingParameters();
		params.setStatus(List.of(RoomStatus.active));
		List<Building> activeBuildings = new ArrayList<>(roomManagementService.searchBuildings(params, roles));
		// Also include the current building if it's inactive (it's already assigned)
		if (room.getBuilding() != null && activeBuildings.stream().noneMatch(b -> b.getKey().equals(room.getBuilding().getKey()))) {
			activeBuildings.add(0, room.getBuilding());
		}
		this.buildings = activeBuildings;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		referenceEl = uifactory.addTextElement("room.reference", "room.col.reference", 255,
				room != null ? room.getExternalRef() : null, formLayout);
		referenceEl.setMandatory(true);

		descriptionEl = uifactory.addTextElement("room.description", "room.col.description", 1000,
				room != null ? room.getDescription() : null, formLayout);

		seatsEl = uifactory.addTextElement("room.seats", "room.detail.seats", 10,
				room != null && room.getSeats() != null ? String.valueOf(room.getSeats()) : null, formLayout);
		seatsEl.setMandatory(true);

		// Building dropdown
		String[] buildingKeys = buildings.stream().map(b -> b.getKey().toString()).toArray(String[]::new);
		String[] buildingValues = buildings.stream().map(this::buildingLabel).toArray(String[]::new);
		buildingEl = uifactory.addDropdownSingleselect("room.building", "room.detail.building",
				formLayout, buildingKeys, buildingValues, null);
		buildingEl.setMandatory(true);
		// Pre-select current building in edit mode
		if (room != null && room.getBuilding() != null) {
			String key = room.getBuilding().getKey().toString();
			for (String k : buildingKeys) {
				if (k.equals(key)) {
					buildingEl.select(key, true);
					break;
				}
			}
		} else if (buildingKeys.length > 0) {
			buildingEl.select(buildingKeys[0], true);
		}

		// Information section
		FormLayoutContainer infoCont = FormLayoutContainer.createVerticalFormLayout("room.information", getTranslator());
		infoCont.setFormTitle(translate("room.create.information"));
		formLayout.add(infoCont);

		additionalInfoEl = uifactory.addTextAreaElement("room.additional.info", "room.col.additional.info",
				-1, 6, 60, true, false, room != null ? room.getRoomInfo() : null, infoCont);

		adminInfoEl = uifactory.addTextAreaElement("room.admin.info", "room.col.admin.info",
				-1, 6, 60, true, false, room != null ? room.getAdminInfo() : null, infoCont);
		adminInfoEl.setHelpText(translate("room.create.admin.info.help"));

		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton(room == null ? "create" : "save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	private String buildingLabel(Building b) {
		String ref = StringHelper.containsNonWhitespace(b.getExternalRef()) ? b.getExternalRef() : "";
		String desc = StringHelper.containsNonWhitespace(b.getDescription()) ? b.getDescription() : "";
		if (StringHelper.containsNonWhitespace(ref) && StringHelper.containsNonWhitespace(desc)) {
			return ref + " · " + desc;
		} else if (StringHelper.containsNonWhitespace(ref)) {
			return ref;
		} else {
			return desc;
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		referenceEl.clearError();
		if (!StringHelper.containsNonWhitespace(referenceEl.getValue())) {
			referenceEl.setErrorKey("form.legende.mandatory");
			allOk = false;
		}

		seatsEl.clearError();
		if (!StringHelper.containsNonWhitespace(seatsEl.getValue())) {
			seatsEl.setErrorKey("form.legende.mandatory");
			allOk = false;
		} else {
			try {
				int seats = Integer.parseInt(seatsEl.getValue().trim());
				if (seats <= 0) {
					seatsEl.setErrorKey("room.seats.error");
					allOk = false;
				}
			} catch (NumberFormatException e) {
				seatsEl.setErrorKey("room.seats.error");
				allOk = false;
			}
		}

		if (!buildingEl.isOneSelected()) {
			buildingEl.setErrorKey("form.legende.mandatory");
			allOk = false;
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Building selectedBuilding = null;
		if (buildingEl.isOneSelected()) {
			String selectedKey = buildingEl.getSelectedKey();
			selectedBuilding = buildings.stream()
					.filter(b -> b.getKey().toString().equals(selectedKey))
					.findFirst().orElse(null);
		}

		if (room == null) {
			String description = descriptionEl.getValue();
			room = roomManagementService.createRoom(selectedBuilding, description, getIdentity());
		}

		room.setExternalRef(referenceEl.getValue());
		room.setDescription(descriptionEl.getValue());
		room.setSeats(Integer.parseInt(seatsEl.getValue().trim()));
		room.setBuilding(selectedBuilding);
		room.setRoomInfo(additionalInfoEl.getValue());
		room.setAdminInfo(adminInfoEl.getValue());

		roomManagementService.updateRoom(room, getIdentity());
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
