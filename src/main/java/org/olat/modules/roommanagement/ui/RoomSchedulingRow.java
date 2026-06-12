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
import org.olat.modules.roommanagement.RoomBooking;

/**
 * Initial date: 12 Jun 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RoomSchedulingRow {

	private final RoomBooking booking;
	private FormLink dateLink;
	private FormLink roomLink;
	private FormLink buildingLink;
	private FormLink eventLink;
	private List<String> warnings = List.of();
	private int numParticipants;

	public RoomSchedulingRow(RoomBooking booking) {
		this.booking = booking;
	}

	public RoomBooking getBooking() {
		return booking;
	}

	public FormLink getDateLink() {
		return dateLink;
	}

	public void setDateLink(FormLink dateLink) {
		this.dateLink = dateLink;
	}

	public FormLink getRoomLink() {
		return roomLink;
	}

	public void setRoomLink(FormLink roomLink) {
		this.roomLink = roomLink;
	}

	public FormLink getBuildingLink() {
		return buildingLink;
	}

	public void setBuildingLink(FormLink buildingLink) {
		this.buildingLink = buildingLink;
	}

	public FormLink getEventLink() {
		return eventLink;
	}

	public void setEventLink(FormLink eventLink) {
		this.eventLink = eventLink;
	}

	public List<String> getWarnings() {
		return warnings;
	}

	public void setWarnings(List<String> warnings) {
		this.warnings = warnings != null ? warnings : List.of();
	}

	public int getNumParticipants() {
		return numParticipants;
	}

	public void setNumParticipants(int numParticipants) {
		this.numParticipants = numParticipants;
	}
}
