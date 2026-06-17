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
package org.olat.modules.roommanagement.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.util.DateUtils;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockManagedFlag;
import org.olat.modules.lecture.LectureBlockRef;
import org.olat.modules.roommanagement.Building;
import org.olat.modules.roommanagement.BuildingRef;
import org.olat.modules.roommanagement.Room;
import org.olat.modules.roommanagement.RoomBooking;
import org.olat.modules.roommanagement.RoomBookingRef;
import org.olat.modules.roommanagement.RoomManagementService;
import org.olat.modules.roommanagement.RoomModuleLog;
import org.olat.modules.roommanagement.RoomModuleLogAction;
import org.olat.modules.roommanagement.RoomRef;
import org.olat.modules.roommanagement.RoomStatus;
import org.olat.modules.roommanagement.model.CollisionReport;
import org.olat.modules.roommanagement.model.RoomModuleLogSearchParameters;
import org.olat.modules.roommanagement.model.SearchBuildingParameters;
import org.olat.modules.roommanagement.model.SearchRoomParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: 4 May 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class RoomManagementServiceImpl implements RoomManagementService {

	@Autowired
	private BuildingDAO buildingDao;
	@Autowired
	private RoomDAO roomDao;
	@Autowired
	private RoomBookingDAO roomBookingDao;
	@Autowired
	private RoomModuleLogDAO roomModuleLogDao;

	// ========== Buildings ==========

	@Override
	public Building createBuilding(String description, Identity doer) {
		Building building = buildingDao.create(description);
		roomModuleLogDao.createLog(RoomModuleLogAction.building_create,
				null, null,
				building.getStatus().name(), RoomManagementXStream.toXml(building),
				building, null, null, null, doer);
		return building;
	}

	@Override
	public Building updateBuilding(Building b, List<Organisation> orgs, Identity doer) {
		Building old = buildingDao.loadByKey(b);
		String beforeXml = RoomManagementXStream.toXml(old);
		String beforeStatus = old.getStatus() != null ? old.getStatus().name() : null;

		Building updated = buildingDao.update(b);

		roomModuleLogDao.createLog(RoomModuleLogAction.building_update,
				beforeStatus, beforeXml,
				updated.getStatus() != null ? updated.getStatus().name() : null, RoomManagementXStream.toXml(updated),
				updated, null, null, null, doer);

		if (orgs != null) {
			reconcileOrganisations(updated, orgs, doer);
		}
		return updated;
	}

	private void reconcileOrganisations(Building building, List<Organisation> newOrgs, Identity doer) {
		List<Organisation> currentOrgs = buildingDao.getOrganisations(building);
		List<Long> currentKeys = currentOrgs.stream().map(Organisation::getKey).toList();
		List<Long> newKeys = newOrgs.stream().map(Organisation::getKey).toList();
		String keysBefore = currentKeys.stream().map(String::valueOf).collect(Collectors.joining(","));
		String keysAfter = newKeys.stream().map(String::valueOf).collect(Collectors.joining(","));

		boolean added = false;
		for (Organisation org : newOrgs) {
			if (!currentKeys.contains(org.getKey())) {
				buildingDao.addOrganisation(building, org);
				added = true;
			}
		}
		if (added) {
			roomModuleLogDao.createLog(RoomModuleLogAction.building_add_organisation,
					null, keysBefore, null, keysAfter,
					building, null, null, null, doer);
		}

		boolean removed = false;
		for (Organisation curr : currentOrgs) {
			if (!newKeys.contains(curr.getKey())) {
				buildingDao.removeOrganisation(building, curr);
				removed = true;
			}
		}
		if (removed) {
			roomModuleLogDao.createLog(RoomModuleLogAction.building_remove_organisation,
					null, keysBefore, null, keysAfter,
					building, null, null, null, doer);
		}
	}

	@Override
	public Building getBuilding(BuildingRef ref) {
		return buildingDao.loadByKey(ref);
	}

	@Override
	public List<Building> searchBuildings(SearchBuildingParameters params, Roles roles) {
		if (roles != null && roles.isSystemAdmin()) {
			params.setIdentity(null); // sysadmin sees everything; bypass visibility predicate
		}
		return buildingDao.search(params);
	}

	@Override
	public long countBuildings(SearchBuildingParameters params) {
		return buildingDao.count(params);
	}

	@Override
	public List<Organisation> getOrganisations(BuildingRef ref) {
		return buildingDao.getOrganisations(ref);
	}

	@Override
	public void deleteBuilding(BuildingRef ref, Identity doer) {
		Building building = buildingDao.loadByKey(ref);
		if (building == null) return;
		String beforeXml = RoomManagementXStream.toXml(building);
		String beforeStatus = building.getStatus() != null ? building.getStatus().name() : null;
		roomModuleLogDao.nullBuildingRef(ref);
		buildingDao.delete(ref);
		building.setStatus(RoomStatus.deleted);
		roomModuleLogDao.createLog(RoomModuleLogAction.building_delete,
				beforeStatus, beforeXml,
				RoomStatus.deleted.name(), RoomManagementXStream.toXml(building),
				building, null, null, null, doer);
	}

	// ========== Rooms ==========

	@Override
	public Room createRoom(Building building, String description, Identity doer) {
		Room room = roomDao.create(building, description);
		roomModuleLogDao.createLog(RoomModuleLogAction.room_create,
				null, null,
				room.getStatus().name(), RoomManagementXStream.toXml(room),
				building, room, null, null, doer);
		return room;
	}

	@Override
	public Room updateRoom(Room r, Identity doer) {
		Room old = roomDao.loadByKey(r);
		String beforeXml = RoomManagementXStream.toXml(old);
		String beforeStatus = old.getStatus() != null ? old.getStatus().name() : null;

		Room updated = roomDao.update(r);

		roomModuleLogDao.createLog(RoomModuleLogAction.room_update,
				beforeStatus, beforeXml,
				updated.getStatus() != null ? updated.getStatus().name() : null, RoomManagementXStream.toXml(updated),
				updated.getBuilding(), updated, null, null, doer);
		return updated;
	}

	@Override
	public Room getRoom(RoomRef ref) {
		return roomDao.loadByKey(ref);
	}

	@Override
	public List<Room> searchRooms(SearchRoomParameters params, Roles roles) {
		if (roles != null && roles.isSystemAdmin()) {
			params.setIdentity(null);
		}
		return roomDao.search(params);
	}

	@Override
	public long countRooms(SearchRoomParameters params) {
		return roomDao.count(params);
	}

	@Override
	public void deleteRoom(RoomRef ref, Identity doer) {
		Room room = roomDao.loadByKey(ref);
		if (room == null) return;
		String beforeXml = RoomManagementXStream.toXml(room);
		String beforeStatus = room.getStatus() != null ? room.getStatus().name() : null;
		roomModuleLogDao.nullRoomRef(ref);
		roomDao.delete(ref);
		room.setStatus(RoomStatus.deleted);
		roomModuleLogDao.createLog(RoomModuleLogAction.room_delete,
				beforeStatus, beforeXml,
				RoomStatus.deleted.name(), RoomManagementXStream.toXml(room),
				room.getBuilding(), room, null, null, doer);
	}

	// ========== Bookings ==========

	@Override
	public RoomBooking bookRoom(Room room, LectureBlock lb, Date start, Date end,
			int bufferBeforeMin, int bufferAfterMin, Identity doer) {
		RoomBooking booking = roomBookingDao.create(room, lb, start, end);
		booking.setBufferBefore(bufferBeforeMin);
		booking.setBufferAfter(bufferAfterMin);
		booking = roomBookingDao.update(booking);
		roomModuleLogDao.createLog(RoomModuleLogAction.booking_create,
				null, null,
				null, RoomManagementXStream.toXml(booking),
				null, room, booking, lb, doer);
		return booking;
	}

	@Override
	public RoomBooking updateBooking(RoomBooking b, Identity doer) {
		RoomBooking old = roomBookingDao.loadByKey(b);
		String beforeXml = RoomManagementXStream.toXml(old);
		RoomBooking updated = roomBookingDao.update(b);
		roomModuleLogDao.createLog(RoomModuleLogAction.booking_update,
				null, beforeXml,
				null, RoomManagementXStream.toXml(updated),
				null, updated.getRoom(), updated, updated.getLectureBlock(), doer);
		return updated;
	}

	@Override
	public void deleteBooking(RoomBookingRef ref, Identity doer) {
		if (ref == null || ref.getKey() == null) return;
		RoomBooking booking = roomBookingDao.loadByKey(ref);
		String beforeXml = booking != null ? RoomManagementXStream.toXml(booking) : null;
		Room room = booking != null ? booking.getRoom() : null;
		LectureBlock lb = booking != null ? booking.getLectureBlock() : null;
		roomModuleLogDao.nullBookingRef(ref);
		roomBookingDao.delete(ref);
		roomModuleLogDao.createLog(RoomModuleLogAction.booking_delete,
				null, beforeXml, null, null,
				null, room, null, lb, doer);
	}

	@Override
	public List<RoomBooking> getBookings(Date from, Date to) {
		return roomBookingDao.getBookings(from, to);
	}

	@Override
	public List<RoomBooking> getBookings(LectureBlockRef lb) {
		return roomBookingDao.getBookingsForLectureBlock(lb);
	}

	@Override
	public List<RoomBooking> getBookingsForRoom(RoomRef room, Date from, Date to) {
		return roomBookingDao.getBookingsForRoom(room, from, to);
	}

	@Override
	public CollisionReport findCollisions(RoomRef room, Date start, Date end,
			int bufferBeforeMin, int bufferAfterMin, RoomBookingRef excluding) {
		Long excludeKey = excluding != null ? excluding.getKey() : null;
		List<RoomBooking> hard = roomBookingDao.findHardOverlapping(room, start, end, excludeKey);
		List<RoomBooking> buffer = new ArrayList<>();
		if (bufferBeforeMin > 0 || bufferAfterMin > 0) {
			// Candidate's buffer envelope
			Date sPrime = DateUtils.addMinutes(start, -bufferBeforeMin);
			Date ePrime = DateUtils.addMinutes(end, bufferAfterMin);
			// Expand the pre-fetch window by the same buffer again so that existing bookings
			// whose own buffer zones extend into [sPrime, ePrime] are also fetched from the DB.
			Date preFetchStart = DateUtils.addMinutes(sPrime, -bufferBeforeMin);
			Date preFetchEnd = DateUtils.addMinutes(ePrime, bufferAfterMin);
			List<RoomBooking> candidates = roomBookingDao.findBufferOverlapping(room, start, end, preFetchStart, preFetchEnd, excludeKey);
			// Precise spec check in Java: the existing booking's buffer envelope must intersect
			// the candidate's buffer envelope [sPrime, ePrime].
			for (RoomBooking b : candidates) {
				Date bEnvStart = DateUtils.addMinutes(b.getStartDate(), -b.getBufferBefore());
				Date bEnvEnd   = DateUtils.addMinutes(b.getEndDate(),    b.getBufferAfter());
				if (bEnvEnd.after(sPrime) && bEnvStart.before(ePrime)) {
					buffer.add(b);
				}
			}
		}
		return new CollisionReport(hard, buffer);
	}

	@Override
	public int deleteBookingsForLectureBlock(LectureBlock lb, Identity doer) {
		List<RoomBooking> bookings = roomBookingDao.getBookingsForLectureBlock(lb);
		if (bookings.isEmpty()) return 0;
		for (RoomBooking booking : bookings) {
			roomModuleLogDao.nullBookingRef(booking);
			String beforeXml = RoomManagementXStream.toXml(booking);
			roomModuleLogDao.createLog(RoomModuleLogAction.booking_cascade_from_lecture_block,
					null, beforeXml, null, null,
					null, booking.getRoom(), null, null, doer);
		}
		roomModuleLogDao.nullLectureBlockRef(lb);
		return roomBookingDao.deleteForLectureBlock(lb);
	}

	@Override
	public void updateBookingsForBlock(LectureBlock lb) {
		List<RoomBooking> bookings = roomBookingDao.getBookingsForLectureBlock(lb);
		for (RoomBooking booking : bookings) {
			String beforeXml = RoomManagementXStream.toXml(roomBookingDao.loadByKey(booking));
			booking.setStartDate(lb.getStartDate());
			booking.setEndDate(lb.getEndDate());
			RoomBooking updated = roomBookingDao.update(booking);
			roomModuleLogDao.createLog(RoomModuleLogAction.booking_update,
					null, beforeXml,
					null, RoomManagementXStream.toXml(updated),
					null, updated.getRoom(), updated, lb, null);
		}
	}

	@Override
	public int copyBookingsForLectureBlock(LectureBlock source, LectureBlock target, Identity doer) {
		if (LectureBlockManagedFlag.isManaged(source, LectureBlockManagedFlag.room)) {
			return 0;
		}
		List<RoomBooking> sourceBookings = roomBookingDao.getBookingsForLectureBlock(source);
		if (sourceBookings.isEmpty()) return 0;
		int copied = 0;
		for (RoomBooking src : sourceBookings) {
			RoomBooking tgt = roomBookingDao.create(src.getRoom(), target, src.getStartDate(), src.getEndDate());
			tgt.setBufferBefore(src.getBufferBefore());
			tgt.setBufferAfter(src.getBufferAfter());
			tgt = roomBookingDao.update(tgt);
			roomModuleLogDao.createLog(RoomModuleLogAction.booking_create,
					null, null,
					null, RoomManagementXStream.toXml(tgt),
					null, tgt.getRoom(), tgt, target, doer);
			copied++;
		}
		return copied;
	}

	// ========== Access control ==========

	@Override
	public boolean canEditBuilding(Building b, Roles roles) {
		if (roles == null) return false;
		if (roles.isSystemAdmin()) return true;
		List<Organisation> orgs = buildingDao.getOrganisations(b);
		if (orgs.isEmpty()) return true;
		return roles.hasRole(orgs, OrganisationRoles.administrator);
	}

	@Override
	public boolean canEditRoom(Room r, Roles roles) {
		return canEditBuilding(r.getBuilding(), roles);
	}

	@Override
	public boolean canBookRoomOnLectureBlock(Room r, LectureBlock lb, Identity id, Roles roles) {
		return isVisibleBuilding(r.getBuilding(), roles, id);
	}

	@Override
	public boolean isVisibleBuilding(Building b, Roles roles, Identity identity) {
		if (roles != null && roles.isSystemAdmin()) return true;
		List<Organisation> orgs = buildingDao.getOrganisations(b);
		if (orgs.isEmpty()) return true;
		if (roles != null && roles.hasRole(orgs, OrganisationRoles.administrator)) return true;
		return roles != null && roles.hasRole(orgs, OrganisationRoles.user);
	}

	// ========== Audit ==========

	@Override
	public List<RoomModuleLog> searchLogs(RoomModuleLogSearchParameters params) {
		return roomModuleLogDao.loadLogs(params);
	}
}
