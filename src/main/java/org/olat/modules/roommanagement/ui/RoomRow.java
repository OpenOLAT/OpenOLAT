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

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.modules.roommanagement.Room;

/**
 * Initial date: 5 Jun 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RoomRow {

	private final Room room;
	private FormLink referenceLink;
	private FormLink buildingLink;   // getUserObject() == Building
	private FormLink calendarIconLink;
	private int occupancyRatePercent = -1;  // -1 = N/A
	private String nextEvent;
	private FormLink toolsLink;
	private RoomDetailsController detailsController;  // stub, null for now

	public RoomRow(Room room) {
		this.room = room;
	}

	public Room getRoom() {
		return room;
	}

	public FormLink getReferenceLink() {
		return referenceLink;
	}

	public void setReferenceLink(FormLink referenceLink) {
		this.referenceLink = referenceLink;
	}

	public FormLink getBuildingLink() {
		return buildingLink;
	}

	public void setBuildingLink(FormLink buildingLink) {
		this.buildingLink = buildingLink;
	}

	public FormLink getCalendarIconLink() {
		return calendarIconLink;
	}

	public void setCalendarIconLink(FormLink calendarIconLink) {
		this.calendarIconLink = calendarIconLink;
	}

	public int getOccupancyRatePercent() {
		return occupancyRatePercent;
	}

	public void setOccupancyRatePercent(int occupancyRatePercent) {
		this.occupancyRatePercent = occupancyRatePercent;
	}

	public String getNextEvent() {
		return nextEvent;
	}

	public void setNextEvent(String nextEvent) {
		this.nextEvent = nextEvent;
	}

	public FormLink getToolsLink() {
		return toolsLink;
	}

	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}

	public RoomDetailsController getDetailsController() {
		return detailsController;
	}

	public void setDetailsController(RoomDetailsController detailsController) {
		this.detailsController = detailsController;
	}

	public boolean isDetailsControllerAvailable() {
		return detailsController != null;
	}

	public String getDetailsControllerName() {
		return detailsController != null ? detailsController.getInitialFormItem().getName() : null;
	}
}
