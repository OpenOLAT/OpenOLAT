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
package org.olat.modules.roommanagement;

import java.util.Date;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRef;
import org.olat.modules.roommanagement.model.CollisionReport;
import org.olat.modules.roommanagement.model.RoomModuleLogSearchParameters;
import org.olat.modules.roommanagement.model.SearchBuildingParameters;
import org.olat.modules.roommanagement.model.SearchRoomParameters;

/**
 * Initial date: 4 May 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public interface RoomManagementService {

	// --- Buildings ---

	Building createBuilding(String description, Identity doer);

	Building updateBuilding(Building b, List<Organisation> orgs, Identity doer);

	Building getBuilding(BuildingRef ref);

	List<Building> searchBuildings(SearchBuildingParameters params, Roles roles);

	long countBuildings(SearchBuildingParameters params);

	List<Organisation> getOrganisations(BuildingRef ref);

	void deleteBuilding(BuildingRef ref, Identity doer);

	// --- Rooms ---

	Room createRoom(Building building, String description, Identity doer);

	Room updateRoom(Room r, Identity doer);

	Room getRoom(RoomRef ref);

	List<Room> searchRooms(SearchRoomParameters params, Roles roles);

	long countRooms(SearchRoomParameters params);

	void deleteRoom(RoomRef ref, Identity doer);

	// --- Bookings ---

	RoomBooking bookRoom(Room room, LectureBlock lb, Date start, Date end,
			int bufferBeforeMin, int bufferAfterMin, Identity doer);

	RoomBooking updateBooking(RoomBooking b, Identity doer);

	void deleteBooking(RoomBookingRef ref, Identity doer);

	List<RoomBooking> getBookings(LectureBlockRef lb);

	List<RoomBooking> getBookingsForRoom(RoomRef room, Date from, Date to);

	CollisionReport findCollisions(RoomRef room, Date start, Date end,
			int bufferBeforeMin, int bufferAfterMin, RoomBookingRef excluding);

	int deleteBookingsForLectureBlock(LectureBlock lb, Identity doer);

	int copyBookingsForLectureBlock(LectureBlock source, LectureBlock target, Identity doer);

	void updateBookingsForBlock(LectureBlock lb);

	// --- Access control ---

	boolean canEditBuilding(Building b, Roles roles);

	boolean canEditRoom(Room r, Roles roles);

	boolean canBookRoomOnLectureBlock(Room r, LectureBlock lb, Identity id, Roles roles);

	boolean isVisibleBuilding(Building b, Roles roles, Identity identity);

	// --- Audit ---

	List<RoomModuleLog> searchLogs(RoomModuleLogSearchParameters params);
}
