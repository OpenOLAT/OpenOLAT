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

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.manager.LectureBlockDAO;
import org.olat.modules.roommanagement.Building;
import org.olat.modules.roommanagement.Room;
import org.olat.modules.roommanagement.RoomBooking;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 4 May 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RoomBookingDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private BuildingDAO buildingDAO;
	@Autowired
	private RoomDAO roomDAO;
	@Autowired
	private RoomBookingDAO roomBookingDAO;
	@Autowired
	private LectureBlockDAO lectureBlockDAO;

	private Building createBuilding() {
		return buildingDAO.create("BkgBld_" + UUID.randomUUID());
	}

	private Room createRoom(Building bld) {
		return roomDAO.create(bld, "BkgRoom_" + UUID.randomUUID());
	}

	private LectureBlock createLectureBlock() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lb = lectureBlockDAO.createLectureBlock(entry, null);
		lb.setTitle("LB_" + UUID.randomUUID());
		lb.setStartDate(date(8, 0));
		lb.setEndDate(date(9, 0));
		lb.setPlannedLecturesNumber(2);
		return lectureBlockDAO.update(lb);
	}

	private static Date date(int hour, int minute) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	@Test
	public void createAndLoad() {
		Building bld = createBuilding();
		Room room = createRoom(bld);
		LectureBlock lb = createLectureBlock();
		dbInstance.commitAndCloseSession();

		Date start = date(10, 0);
		Date end = date(11, 0);
		RoomBooking booking = roomBookingDAO.create(room, lb, start, end);
		dbInstance.commitAndCloseSession();

		List<RoomBooking> bookings = roomBookingDAO.getBookingsForRoom(room, null, null);
		Assertions.assertThat(bookings)
				.extracting(RoomBooking::getKey)
				.contains(booking.getKey());
	}

	@Test
	public void getBookingsForLectureBlock() {
		Building bld = createBuilding();
		Room room = createRoom(bld);
		LectureBlock lb = createLectureBlock();
		dbInstance.commitAndCloseSession();

		roomBookingDAO.create(room, lb, date(10, 0), date(11, 0));
		dbInstance.commitAndCloseSession();

		List<RoomBooking> bookings = roomBookingDAO.getBookingsForLectureBlock(lb);
		Assert.assertEquals(1, bookings.size());
		Assert.assertEquals(room.getKey(), bookings.get(0).getRoom().getKey());
	}

	@Test
	public void hardOverlap_detected() {
		Building bld = createBuilding();
		Room room = createRoom(bld);
		LectureBlock lb1 = createLectureBlock();
		LectureBlock lb2 = createLectureBlock();
		dbInstance.commitAndCloseSession();

		// Existing booking: 10:00 - 11:00
		roomBookingDAO.create(room, lb1, date(10, 0), date(11, 0));
		dbInstance.commitAndCloseSession();

		// Candidate overlaps: 10:30 - 11:30
		List<RoomBooking> overlaps = roomBookingDAO.findHardOverlapping(room, date(10, 30), date(11, 30), null);
		Assert.assertFalse("Should detect hard overlap", overlaps.isEmpty());
	}

	@Test
	public void noOverlap_notDetected() {
		Building bld = createBuilding();
		Room room = createRoom(bld);
		LectureBlock lb1 = createLectureBlock();
		dbInstance.commitAndCloseSession();

		// Existing booking: 10:00 - 11:00
		roomBookingDAO.create(room, lb1, date(10, 0), date(11, 0));
		dbInstance.commitAndCloseSession();

		// Candidate after: 11:00 - 12:00 — touching but not overlapping
		List<RoomBooking> overlaps = roomBookingDAO.findHardOverlapping(room, date(11, 0), date(12, 0), null);
		Assert.assertTrue("No hard overlap expected", overlaps.isEmpty());
	}

	@Test
	@Ignore
	public void bufferOverlap_detected() {
		Building bld = createBuilding();
		Room room = createRoom(bld);
		LectureBlock lb1 = createLectureBlock();
		dbInstance.commitAndCloseSession();

		// Existing booking: 10:00 - 11:00
		RoomBooking existing = roomBookingDAO.create(room, lb1, date(10, 0), date(11, 0));
		existing.setBufferAfter(30); // 30 min buffer after → envelope extends to 11:30
		roomBookingDAO.update(existing);
		dbInstance.commitAndCloseSession();

		// Candidate: 11:10 - 12:00 — no hard overlap, but inside buffer
		// sPrime = 11:10 - 0 = 11:10, ePrime = 12:00 + 0 = 12:00
		List<RoomBooking> bufferOverlaps = roomBookingDAO.findBufferOverlapping(
				room, date(11, 10), date(12, 0), date(11, 10), date(12, 0), null);
		Assert.assertFalse("Should detect buffer overlap", bufferOverlaps.isEmpty());
	}

	@Test
	public void bufferOverlap_notDetectedWhenHardOverlap() {
		Building bld = createBuilding();
		Room room = createRoom(bld);
		LectureBlock lb1 = createLectureBlock();
		dbInstance.commitAndCloseSession();

		roomBookingDAO.create(room, lb1, date(10, 0), date(11, 0));
		dbInstance.commitAndCloseSession();

		// Candidate hard-overlaps: 10:30 - 11:30 — buffer query excludes hard overlaps
		List<RoomBooking> bufferOverlaps = roomBookingDAO.findBufferOverlapping(
				room, date(10, 30), date(11, 30), date(10, 30), date(11, 30), null);
		Assert.assertTrue("Hard overlaps excluded from buffer result", bufferOverlaps.isEmpty());
	}

	@Test
	public void deleteForLectureBlock() {
		Building bld = createBuilding();
		Room room = createRoom(bld);
		LectureBlock lb = createLectureBlock();
		dbInstance.commitAndCloseSession();

		roomBookingDAO.create(room, lb, date(10, 0), date(11, 0));
		dbInstance.commitAndCloseSession();

		int deleted = roomBookingDAO.deleteForLectureBlock(lb);
		dbInstance.commitAndCloseSession();

		Assert.assertEquals(1, deleted);
		List<RoomBooking> remaining = roomBookingDAO.getBookingsForLectureBlock(lb);
		Assert.assertTrue(remaining.isEmpty());
	}

	@Test
	public void copyBookings_basic() {
		Building bld = createBuilding();
		Room room = createRoom(bld);
		LectureBlock src = createLectureBlock();
		LectureBlock tgt = createLectureBlock();
		dbInstance.commitAndCloseSession();

		RoomBooking srcBooking = roomBookingDAO.create(room, src, date(10, 0), date(11, 0));
		srcBooking.setBufferBefore(10);
		srcBooking.setBufferAfter(15);
		roomBookingDAO.update(srcBooking);
		dbInstance.commitAndCloseSession();

		int copied = roomBookingDAO.copyBookings(src, tgt);
		dbInstance.commitAndCloseSession();

		Assert.assertEquals(1, copied);
		List<RoomBooking> tgtBookings = roomBookingDAO.getBookingsForLectureBlock(tgt);
		Assert.assertEquals(1, tgtBookings.size());
		RoomBooking copy = tgtBookings.get(0);
		Assert.assertEquals(room.getKey(), copy.getRoom().getKey());
		Assert.assertEquals(10, copy.getBufferBefore());
		Assert.assertEquals(15, copy.getBufferAfter());
	}

	@Test
	public void copyBookings_noSourceBooking_isNoOp() {
		LectureBlock src = createLectureBlock();
		LectureBlock tgt = createLectureBlock();
		dbInstance.commitAndCloseSession();

		int copied = roomBookingDAO.copyBookings(src, tgt);
		Assert.assertEquals(0, copied);
	}
}
