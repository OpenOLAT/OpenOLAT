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

import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.id.Organisation;
import org.olat.modules.roommanagement.Building;

/**
 * Initial date: 1 Jun 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class BuildingRow {

	private final Building building;
	private FormLink referenceLink;
	private FormLink addressLink;
	private FormLink infoUrlLink;
	private FormLink roomsLink;
	private FormLink toolsLink;
	private List<Organisation> organisations;
	private int roomCount;
	private BuildingDetailsController detailsController;

	public BuildingRow(Building building) {
		this.building = building;
	}

	public Building getBuilding() {
		return building;
	}

	public FormLink getReferenceLink() {
		return referenceLink;
	}

	public void setReferenceLink(FormLink referenceLink) {
		this.referenceLink = referenceLink;
	}

	public FormLink getAddressLink() {
		return addressLink;
	}

	public void setAddressLink(FormLink addressLink) {
		this.addressLink = addressLink;
	}

	public BuildingDetailsController getDetailsController() {
		return detailsController;
	}

	public void setDetailsController(BuildingDetailsController detailsController) {
		this.detailsController = detailsController;
	}

	public boolean isDetailsControllerAvailable() {
		return detailsController != null;
	}

	public String getDetailsControllerName() {
		return detailsController != null ? detailsController.getInitialFormItem().getName() : null;
	}

	public FormLink getInfoUrlLink() {
		return infoUrlLink;
	}

	public void setInfoUrlLink(FormLink infoUrlLink) {
		this.infoUrlLink = infoUrlLink;
	}

	public FormLink getRoomsLink() {
		return roomsLink;
	}

	public void setRoomsLink(FormLink roomsLink) {
		this.roomsLink = roomsLink;
	}

	public FormLink getToolsLink() {
		return toolsLink;
	}

	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}

	public List<Organisation> getOrganisations() {
		return organisations;
	}

	public void setOrganisations(List<Organisation> organisations) {
		this.organisations = organisations;
	}

	public int getRoomCount() {
		return roomCount;
	}

	public void setRoomCount(int roomCount) {
		this.roomCount = roomCount;
	}
}
