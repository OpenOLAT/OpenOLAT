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
package org.olat.modules.lecture.ui;

import org.olat.modules.roommanagement.Room;
import org.olat.modules.roommanagement.RoomBooking;

/**
 * Initial date: 30 Jun 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class AddRoomsRow {

	public enum RoomAvailability {
		AVAILABLE, OCCUPIED, MY_EVENT
	}

	private final Room room;
	private final RoomBooking occupiedBy;
	private final boolean myEvent;
	private final boolean seatWarning;

	public AddRoomsRow(Room room, RoomBooking occupiedBy, boolean myEvent, int participantCount) {
		this.room = room;
		this.occupiedBy = occupiedBy;
		this.myEvent = myEvent;
		this.seatWarning = room.getSeats() != null && participantCount > 0 && room.getSeats() < participantCount;
	}

	public Room getRoom() {
		return room;
	}

	public Long getKey() {
		return room.getKey();
	}

	public String getReference() {
		return room.getExternalRef();
	}

	public String getDescription() {
		String desc = room.getDescription();
		String ref = room.getExternalRef();
		return (desc != null && !desc.equals(ref)) ? desc : null;
	}

	public Integer getSeats() {
		return room.getSeats();
	}

	public RoomAvailability getAvailability() {
		if (myEvent) return RoomAvailability.MY_EVENT;
		return occupiedBy != null ? RoomAvailability.OCCUPIED : RoomAvailability.AVAILABLE;
	}

	public RoomBooking getOccupiedBy() {
		return occupiedBy;
	}

	public boolean isSeatWarning() {
		return seatWarning;
	}
}
